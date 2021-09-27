package teamcity.plugin.build.trigger.webhook.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import jetbrains.buildServer.serverSide.BuildCustomizer;
import jetbrains.buildServer.serverSide.BuildCustomizerFactory;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import jetbrains.buildServer.users.SUser;
import teamcity.plugin.build.trigger.webhook.Constants;
import teamcity.plugin.build.trigger.webhook.Loggers;
import teamcity.plugin.build.trigger.webhook.TriggerFilterDefinition;
import teamcity.plugin.build.trigger.webhook.TriggerParameterDefinition;
import teamcity.plugin.build.trigger.webhook.TriggerParameters;
import teamcity.plugin.build.trigger.webhook.TriggerUtils;
import teamcity.plugin.build.trigger.webhook.parser.JsonToPropertiesParser;
import teamcity.plugin.build.trigger.webhook.service.BuildTriggerResolverService.TriggersHolder;

public class BuildTriggerHandlerService {
	
	private static final String LOGGING_PREFIX = Constants.PLUGIN_NAME + "-" + BuildTriggerHandlerService.class.getSimpleName();

	private BuildTriggerResolverService myBuildTriggerResolverService;
	private JsonToPropertiesParser myJsonToPropertiesParser;
	private BuildCustomizerFactory myBuildCustomizerFactory;

	public BuildTriggerHandlerService(
			BuildTriggerResolverService buildTriggerResolverService,
			JsonToPropertiesParser jsonToPropertiesParser,
			BuildCustomizerFactory buildCustomizerFactory
			) {
		myBuildTriggerResolverService = buildTriggerResolverService;
		myJsonToPropertiesParser = jsonToPropertiesParser;
		myBuildCustomizerFactory = buildCustomizerFactory;
	}

	public void handleWebHook(SUser currentUser, String buildTypeExternalId, String payload) {
		TriggersHolder triggersHolder = myBuildTriggerResolverService.findTriggersForBuildType(buildTypeExternalId);
		triggersHolder.getTriggers().forEach(trigger -> {
			Loggers.ACTIVITIES.debug(String.format("%s: Starting Webhook Trigger processing. buildType='%s', triggerName='%s', triggerId='%s'", LOGGING_PREFIX, buildTypeExternalId, trigger.getTriggerName(), trigger.getId()));
			Loggers.ACTIVITIES.debug(LOGGING_PREFIX + ": Webhook Payload content: \n" + payload);
			List<TriggerParameterDefinition> parameterDefinitions = TriggerUtils.toDefinitions(trigger.getProperties().get(TriggerParameters.PATH_MAPPINGS));
			Map<String, String> resolvedParameters = myJsonToPropertiesParser.parse(parameterDefinitions, payload);
			List<TriggerFilterDefinition> filters = TriggerUtils.toFilters(trigger.getProperties().get(TriggerParameters.FILTERS));
			if (checkResolvedParameters(parameterDefinitions, filters, resolvedParameters)) {
				BuildCustomizer buildCustomiser = myBuildCustomizerFactory.createBuildCustomizer(triggersHolder.getsBuildType(), currentUser);
				buildCustomiser.setParameters(resolvedParameters);
				SQueuedBuild queuedBuild = buildCustomiser.createPromotion().addToQueue("Webhook Build Trigger");
				Loggers.ACTIVITIES.info(String.format("%s: Build queued by Webhook Trigger processing. buildType='%s', triggerName='%s', triggerId='%s', buildId='%s'", LOGGING_PREFIX, buildTypeExternalId, trigger.getTriggerName(), trigger.getId(), queuedBuild.getItemId()));
			} else {
				Loggers.ACTIVITIES.info(String.format("%s: Build not queued by Webhook Trigger processing. Trigger filters did not match webhook payload content. buildType='%s', triggerName='%s', triggerId='%s'", LOGGING_PREFIX, buildTypeExternalId, trigger.getTriggerName(), trigger.getId()));
			}
			Loggers.ACTIVITIES.debug(String.format("%s: Completed Webhook Trigger processing. buildType='%s', triggerName='%s', triggerId='%s'", LOGGING_PREFIX, buildTypeExternalId, trigger.getTriggerName(), trigger.getId()));
		});
	}

	private boolean checkResolvedParameters(List<TriggerParameterDefinition> parameterDefinitions, List<TriggerFilterDefinition> filters, Map<String, String> resolvedParameters) {
		boolean valid = true;
		if (filters.isEmpty()) {
			Loggers.ACTIVITIES.debug(LOGGING_PREFIX + ": No filters defined. Skipping regex validation.");
		}
		for (TriggerFilterDefinition filterEntry : filters) {
			final String parameterName = filterEntry.getTemplate();
			final String regex = filterEntry.getRegex();
			// TODO: Need to template this.
			if (resolvedParameters.containsKey(parameterName)) {
				final String parameterValue = resolvedParameters.get(parameterName);
				if (Pattern.matches(regex, parameterValue)) {
					Loggers.ACTIVITIES.debug(
							String.format("%s: Regex match found. name='%s', regex='%s', value='%s'", 
									LOGGING_PREFIX, parameterName, regex, parameterValue));
				} else {
					Loggers.ACTIVITIES.debug(
							String.format("%s: No regex match found. name='%s', regex='%s', value='%s'", 
									LOGGING_PREFIX, parameterName, regex, parameterValue));
					valid = false;
				}
//			} else {
//				Loggers.ACTIVITIES.debug(
//						String.format("%s: Regex defined against missing payload parameter. No value found in payload for name='%s', path='%s', regex='%s'", 
//								LOGGING_PREFIX, parameterName, parameterDefinitions.get(parameterName), regex));
//				valid = false;
			}
		}
		return valid;
	}
	
	private Map<String, String> xtoParameterDefinitionsMap(String toParameterDefinitionsString) {
		Map<String,String> parameters = new HashMap<>();
		if (toParameterDefinitionsString != null) {
			for (String line : toParameterDefinitionsString.split("\\n")) {
				String[] kv = line.trim().split("=>");
				parameters.put(kv[0].trim(), kv[1].trim());
			}
		}
		return parameters;

	}

}
