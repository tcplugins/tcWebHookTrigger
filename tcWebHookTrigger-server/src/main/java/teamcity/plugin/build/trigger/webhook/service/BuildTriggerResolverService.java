package teamcity.plugin.build.trigger.webhook.service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.auth.AccessDeniedException;
import teamcity.plugin.build.trigger.webhook.Constants;
import teamcity.plugin.build.trigger.webhook.Loggers;
import teamcity.plugin.build.trigger.webhook.exception.BuildTypeNotFoundException;
import teamcity.plugin.build.trigger.webhook.exception.PermissionedDeniedException;

public class BuildTriggerResolverService {
	
	private static final String LOGGING_PREFIX = Constants.PLUGIN_NAME + "-" + BuildTriggerResolverService.class.getSimpleName();

	private ProjectManager myProjectManager;

	public BuildTriggerResolverService(ProjectManager projectManager) {
		myProjectManager = projectManager;
	}
	
	public TriggersHolder findTriggersForBuildType(String buildTypeExternalId) throws BuildTypeNotFoundException, PermissionedDeniedException {
		try {
			SBuildType sBuildType = myProjectManager.findBuildTypeByExternalId(buildTypeExternalId);
			Loggers.ACTIVITIES.debug(String.format("%s: Found matching SBuildType. buildType='%s'", LOGGING_PREFIX, buildTypeExternalId));
			if (sBuildType == null) {
				throw new BuildTypeNotFoundException(buildTypeExternalId);
			}
			Collection<BuildTriggerDescriptor> buildTriggers = sBuildType.getBuildTriggersCollection();
			return new TriggersHolder(sBuildType,buildTriggers.stream()
					.filter(trigger -> WebHookBuildTriggerService.WEBHOOK_BUILD_TRIGGER_NAME.equals(trigger.getBuildTriggerService().getName()))
					.collect(Collectors.toList()));
		} catch (AccessDeniedException ex) {
			throw new PermissionedDeniedException(ex);
		}
	}
	
	public static class TriggersHolder {
		private SBuildType sBuildType;
		private List<BuildTriggerDescriptor> triggers;

		public TriggersHolder(SBuildType sBuildType, List<BuildTriggerDescriptor> triggers) {
			this.sBuildType = sBuildType;
			this.triggers  = triggers;
		}
		
		public SBuildType getsBuildType() {
			return sBuildType;
		}
		
		public List<BuildTriggerDescriptor> getTriggers() {
			return triggers;
		}
	}

}
