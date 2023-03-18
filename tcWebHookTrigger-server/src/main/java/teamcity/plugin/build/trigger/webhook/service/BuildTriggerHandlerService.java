package teamcity.plugin.build.trigger.webhook.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.serverSide.BuildCustomizer;
import jetbrains.buildServer.serverSide.BuildCustomizerFactory;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import jetbrains.buildServer.serverSide.auth.AuthorityHolder;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.vcs.SVcsModification;
import jetbrains.buildServer.vcs.VcsModificationHistoryEx;
import jetbrains.buildServer.vcs.VcsRootInstance;
import teamcity.plugin.build.trigger.webhook.Constants;
import teamcity.plugin.build.trigger.webhook.Loggers;
import teamcity.plugin.build.trigger.webhook.TriggerFilterDefinition;
import teamcity.plugin.build.trigger.webhook.TriggerParameterDefinition;
import teamcity.plugin.build.trigger.webhook.TriggerParameters;
import teamcity.plugin.build.trigger.webhook.TriggerUtils;
import teamcity.plugin.build.trigger.webhook.exception.PermissionedDeniedException;
import teamcity.plugin.build.trigger.webhook.parser.JsonToPropertiesParser;
import teamcity.plugin.build.trigger.webhook.service.BuildTriggerResolverService.TriggersHolder;
import teamcity.plugin.build.trigger.webhook.template.TemplatedTextReplacer;

public class BuildTriggerHandlerService {
	
	private static final String LOGGING_PREFIX = Constants.PLUGIN_NAME + "-" + BuildTriggerHandlerService.class.getSimpleName();

	private BuildTriggerResolverService myBuildTriggerResolverService;
	private JsonToPropertiesParser myJsonToPropertiesParser;
	private BuildCustomizerFactory myBuildCustomizerFactory;
	private VcsModificationHistoryEx myVcsModificationHistoryEx;

	public BuildTriggerHandlerService(
			BuildTriggerResolverService buildTriggerResolverService,
			JsonToPropertiesParser jsonToPropertiesParser,
			BuildCustomizerFactory buildCustomizerFactory,
			VcsModificationHistoryEx vcsModificationHistoryEx
			) {
		myBuildTriggerResolverService = buildTriggerResolverService;
		myJsonToPropertiesParser = jsonToPropertiesParser;
		myBuildCustomizerFactory = buildCustomizerFactory;
		myVcsModificationHistoryEx = vcsModificationHistoryEx;
	}

	public void handleWebHook(AuthorityHolder user, String buildTypeExternalId, String payload) {
		TriggersHolder triggersHolder = myBuildTriggerResolverService.findTriggersForBuildType(buildTypeExternalId);
		// Check that the representation of the user (could be a user for Basic Auth, or a token instance for Bearer) has the required permission.
		if (! user.isPermissionGrantedForProject(triggersHolder.getsBuildType().getProjectId(), Permission.RUN_BUILD)) {
			throw new PermissionedDeniedException(String.format("RUN_BUILD permission is not granted for user '%s' on build '%s'.", user.getAssociatedUser().getUsername(), triggersHolder.getsBuildType().getExternalId()));
		}
		for (BuildTriggerDescriptor trigger : triggersHolder.getTriggers()) {
			Loggers.ACTIVITIES.debug(String.format("%s: Starting Webhook Trigger processing. buildType='%s', triggerName='%s', triggerId='%s'", LOGGING_PREFIX, buildTypeExternalId, trigger.getTriggerName(), trigger.getId()));
			Loggers.ACTIVITIES.debug(LOGGING_PREFIX + ": Webhook Payload content: \n" + payload);
			List<TriggerParameterDefinition> parameterDefinitions = TriggerUtils.toDefinitions(trigger.getProperties().get(TriggerParameters.PATH_MAPPINGS));
			
			// Get the parameters definitions and try to parse them out of the JSON/XML document. 
			Map<String, String> resolvedParameters = myJsonToPropertiesParser.parse(parameterDefinitions, payload);
			
			List<TriggerFilterDefinition> filters = TriggerUtils.toFilters(trigger.getProperties().get(TriggerParameters.FILTERS));
			
			// Get the list of parameters filters, and try to resolve them with using the parameter values.
			ResolvedValuesHolder valuesHolder = checkResolvedParameters(parameterDefinitions, filters, resolvedParameters);
						
			if (valuesHolder.parametersAreValid() && valuesHolder.triggersAreValid()) {
				BuildCustomizer buildCustomiser = myBuildCustomizerFactory.createBuildCustomizer(triggersHolder.getsBuildType(), null);
				
				// Populate the parameters from the payload.
				Map<String, String> customParameters = valuesHolder.getAllResolvedValues();
				
				if (Boolean.parseBoolean(trigger.getProperties().get(TriggerParameters.INCLUDE_WHOLE_PAYLOAD))) {
					customParameters.put("payload", payload);
				}
				
				buildCustomiser.setParameters(customParameters);
				
				// Look for a trigger named "branch". If defined, build that branch.
				if (valuesHolder.getResolvedTriggers().containsKey(Constants.BRANCH_NAME_KEYWORD)) {
					String branchName = valuesHolder.getResolvedTriggers().get(Constants.BRANCH_NAME_KEYWORD);
					Loggers.ACTIVITIES.debug(String.format("%s: Found filter named 'branch'. Build will be requested against branch '%s'. buildType='%s', triggerName='%s', branchName='%s', triggerId='%s'", LOGGING_PREFIX, branchName, buildTypeExternalId, trigger.getTriggerName(), branchName, trigger.getId()));
					buildCustomiser.setDesiredBranchName(branchName);
				
				// If we've not found a trigger, try looking for a parameter named "branch".
				} else if (resolvedParameters.containsKey(Constants.BRANCH_NAME_KEYWORD)) {
					String branchName = resolvedParameters.get(Constants.BRANCH_NAME_KEYWORD);
					Loggers.ACTIVITIES.debug(String.format("%s: Found parameter named 'branch'. Build will be requested against branch '%s'. buildType='%s', triggerName='%s', branchName='%s', triggerId='%s'", LOGGING_PREFIX, branchName, buildTypeExternalId, trigger.getTriggerName(), branchName, trigger.getId()));
					buildCustomiser.setDesiredBranchName(branchName);
				
				// Else. don't try to set the branch to build. We will therefore build the <default> branch.
				} else {
					Loggers.ACTIVITIES.debug(String.format("%s: No filter or parameter named 'branch' found. Build will be requested against the 'default' branch. buildType='%s', triggerName='%s', triggerId='%s'", LOGGING_PREFIX, buildTypeExternalId, trigger.getTriggerName(), trigger.getId()));
				}
				
				String commitId = null;
				// Look for a trigger named "commit". If defined, try to build that commit id.
				if (valuesHolder.getResolvedTriggers().containsKey(Constants.COMMIT_ID_KEYWORD)) {
					commitId = valuesHolder.getResolvedTriggers().get(Constants.COMMIT_ID_KEYWORD);
					
				// If we've not found a trigger, try looking for a parameter named "commit".
				} else if (resolvedParameters.containsKey(Constants.COMMIT_ID_KEYWORD)) {
					commitId = resolvedParameters.get(Constants.COMMIT_ID_KEYWORD);

				// Else. don't try to set the commit to build. We will therefore build the latest commit on the branch.
				} else {
					Loggers.ACTIVITIES.debug(String.format("%s: No filter or parameter named 'commit' found. Build will be requested against the latest commit on the branch. buildType='%s', triggerName='%s', triggerId='%s'", LOGGING_PREFIX, buildTypeExternalId, trigger.getTriggerName(), trigger.getId()));
					queueBuild(buildTypeExternalId, trigger, buildCustomiser);
				}
			
				if (commitId != null) {
					SVcsModification foundModification = null;
					for (VcsRootInstance vcsRoot : triggersHolder.getsBuildType().getVcsRootInstances()) {
						SVcsModification modification =  myVcsModificationHistoryEx.findModificationByVersion(vcsRoot, commitId);
						if (modification != null) {
							foundModification = modification;
							break;
						}
					}
					if (foundModification != null) {
						Loggers.ACTIVITIES.debug(String.format("%s: Found specific modifcation in VCS for commit '%s'. Build will be triggered against this commit. buildType='%s', triggerName='%s', triggerId='%s'", LOGGING_PREFIX, commitId, buildTypeExternalId, trigger.getTriggerName(), trigger.getId()));	
						buildCustomiser.setChangesUpTo(foundModification);
						queueBuild(buildTypeExternalId, trigger, buildCustomiser);
					} else {
						Loggers.ACTIVITIES.info(String.format("%s: No modifcation found in VCS for commit '%s'. Build will not be triggered. buildType='%s', triggerName='%s', triggerId='%s'", LOGGING_PREFIX, commitId, buildTypeExternalId, trigger.getTriggerName(), trigger.getId()));
						continue;
					}
				}
				
			} else {
				Loggers.ACTIVITIES.info(String.format("%s: Build not queued by Webhook Trigger processing. Trigger filters did not match webhook payload content. buildType='%s', triggerName='%s', triggerId='%s'", LOGGING_PREFIX, buildTypeExternalId, trigger.getTriggerName(), trigger.getId()));
			}
			Loggers.ACTIVITIES.debug(String.format("%s: Completed Webhook Trigger processing. buildType='%s', triggerName='%s', triggerId='%s'", LOGGING_PREFIX, buildTypeExternalId, trigger.getTriggerName(), trigger.getId()));
		}
	}

	private SQueuedBuild queueBuild(String buildTypeExternalId, BuildTriggerDescriptor trigger, BuildCustomizer buildCustomiser) {
		// Now queue the build, and set a comment that says we triggered it.
		SQueuedBuild queuedBuild = buildCustomiser.createPromotion().addToQueue(Constants.PLUGIN_DESCRIPTION);
		Loggers.ACTIVITIES.info(String.format("%s: Build queued by Webhook Trigger processing. buildType='%s', triggerName='%s', triggerId='%s', buildId='%s'", LOGGING_PREFIX, buildTypeExternalId, trigger.getTriggerName(), trigger.getId(), queuedBuild.getItemId()));
		return queuedBuild;
	}

	private ResolvedValuesHolder checkResolvedParameters(List<TriggerParameterDefinition> parameterDefinitions, List<TriggerFilterDefinition> filters, Map<String, String> resolvedParameters) {
		ResolvedValuesHolder resolvedValuesHolder = new ResolvedValuesHolder();
		
		if (parameterDefinitions.isEmpty()) {
			Loggers.ACTIVITIES.debug(LOGGING_PREFIX + ": No parameters defined. Skipping parameter resolution and validation.");
		}
		for (TriggerParameterDefinition parameterDefinition : parameterDefinitions) {
			final String name = parameterDefinition.getName();
			
			// Firstly,.set the value to a default if there is one.
			String value = parameterDefinition.getDefaultValue();
			
			// Next, try to then find a resolved value via the parameters obtained in the XPath/JSONPath search.
			if (resolvedParameters.containsKey(name) && StringUtils.isNotEmpty(resolvedParameters.get(name))) {
				value = resolvedParameters.get(name);
			}
			
			// We've done our best to populate the value. If it's still unpopulated and is required, 
			// we fail here. We shouldn't trigger the build if a required value is not resolved.
			if (Boolean.TRUE.equals(parameterDefinition.getRequired() && StringUtils.isEmpty(value))) {
				Loggers.ACTIVITIES.debug(
						String.format("%s: Required parameter is not resolved. Build will not be triggered. name='%s', value='%s'", 
								LOGGING_PREFIX, name, value));
				resolvedValuesHolder.setParametersAreValid(false);
				
			// If it's not resolved, but is also not required, we will still allow the build to run, but we won't define the 
			// parameter. If the parameter is defined on the build, we won't be overwriting it.
			} else if (StringUtils.isEmpty(value)){
				Loggers.ACTIVITIES.debug(
						String.format("%s: Parameter is not resolved. Parameter will not be passed to the build. name='%s', value='%s'", 
								LOGGING_PREFIX, name, value));
			
			// Else, the parameter is good. Set it to pass to the build.
			} else {
				Loggers.ACTIVITIES.debug(
						String.format("%s: Parameter is resolved. Parameter will be passed to the build. name='%s', value='%s'", 
								LOGGING_PREFIX, name, value));
				resolvedValuesHolder.addParameter(name, value);
			}
		}
		
		if (filters.isEmpty()) {
			Loggers.ACTIVITIES.debug(LOGGING_PREFIX + ": No filters defined. Skipping regex validation.");
		}
		for (TriggerFilterDefinition filterEntry : filters) {
			final String name = filterEntry.getName();
			final String filterTemplate = filterEntry.getTemplate();
			final String regex = filterEntry.getRegex();
			
			final String resolvedFilter = TemplatedTextReplacer.resolve(filterTemplate, resolvedValuesHolder.getResolvedParameters());
			if (Pattern.matches(regex, resolvedFilter)) {
				resolvedValuesHolder.addTrigger(name, resolvedFilter);
				Loggers.ACTIVITIES.debug(
						String.format("%s: Regex match found. name='%s', regex='%s', value='%s'", 
								LOGGING_PREFIX, name, regex, resolvedFilter));
			} else {
				Loggers.ACTIVITIES.debug(
						String.format("%s: No regex match found. name='%s', regex='%s', value='%s'", 
								LOGGING_PREFIX, name, regex, resolvedFilter));
				resolvedValuesHolder.setTriggersAreValid(false);
			}
		}
		return resolvedValuesHolder;
	}
	
	public static class ResolvedValuesHolder {
		Map<String, String> resolvedParameters = new HashMap<>();
		Map<String, String> resolvedTriggers = new HashMap<>();
		boolean parametersAreValid = true;
		boolean triggersAreValid = true;
		
		public void addParameter(String name, String value) {
			this.resolvedParameters.put(name, value);
		}
		public Map<String, String> getAllResolvedValues() {
			Map<String, String> allValues = new HashMap<>();
			allValues.putAll(getResolvedParameters());
			allValues.putAll(resolvedTriggers);
			return allValues;
		}
		public void addTrigger(String name, String value) {
			this.resolvedTriggers.put(name, value);
		}
		public Map<String, String> getResolvedParameters() {
			return resolvedParameters;
		}
		public Map<String, String> getResolvedTriggers() {
			return resolvedTriggers;
		}
		public boolean parametersAreValid() {
			return parametersAreValid;
		}
		public void setParametersAreValid(boolean parametersAreValid) {
			this.parametersAreValid = parametersAreValid;
		}
		public boolean triggersAreValid() {
			return triggersAreValid;
		}
		public void setTriggersAreValid(boolean triggersAreValid) {
			this.triggersAreValid = triggersAreValid;
		}
	}

}
