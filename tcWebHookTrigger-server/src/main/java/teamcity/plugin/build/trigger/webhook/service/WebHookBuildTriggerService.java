package teamcity.plugin.build.trigger.webhook.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.util.text.StringUtil;

import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.buildTriggers.BuildTriggerService;
import jetbrains.buildServer.buildTriggers.BuildTriggeringPolicy;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import teamcity.plugin.build.trigger.webhook.Constants;
import teamcity.plugin.build.trigger.webhook.TriggerParameters;
import teamcity.plugin.build.trigger.webhook.TriggerUtils;
import teamcity.plugin.build.trigger.webhook.exception.MalformedTriggerDefinitionException;

public class WebHookBuildTriggerService extends BuildTriggerService {
	
	public static final String WEBHOOK_BUILD_TRIGGER_NAME = "webhookBuildTrigger";
	private final PluginDescriptor myPluginDescriptor;
	private final BuildTriggeringPolicy myPolicy;
	
	public WebHookBuildTriggerService(@NotNull final PluginDescriptor pluginDescriptor) {
			    myPluginDescriptor = pluginDescriptor;
			    myPolicy = new NonPollingBuildTriggeringPolicy();
	}

	@Override
	public String getName() {
		return WEBHOOK_BUILD_TRIGGER_NAME;
	}

	@Override
	public String getDisplayName() {
		return Constants.PLUGIN_DESCRIPTION;
	}

	@Override
	public String describeTrigger(BuildTriggerDescriptor buildTriggerDescriptor) {
		Map<String, String> props = buildTriggerDescriptor.getProperties();
		String pathMappings = props.get(TriggerParameters.PATH_MAPPINGS);
		String filters = props.get(TriggerParameters.FILTERS);
		StringBuilder sb =  new StringBuilder()
				.append("Trigger build when webhook received.\n");
		sb.append("\nPath Mappings\n");
		Arrays.stream(pathMappings.split("\\n")).forEachOrdered(s -> sb.append(" - ").append(s).append("\n"));
		sb.append("\nFilters\n");
		if (filters != null) {
			Arrays.stream(filters.split("\\n")).forEachOrdered(s -> sb.append(" - ").append(s).append("\n"));
		} else {
			sb.append(" - No Filters configured\n");
		}
		return sb.toString();
	}

	@Override
	public BuildTriggeringPolicy getBuildTriggeringPolicy() {
		return myPolicy;
	}

	@Override
	public boolean isMultipleTriggersPerBuildTypeAllowed() {
		return true;
	}

	@Override
	@SuppressWarnings("squid:S1604") // Just use interface, not lamda
	public PropertiesProcessor getTriggerPropertiesProcessor() {
		return new PropertiesProcessor() {
			public Collection<InvalidProperty> process(Map<String, String> properties) {
				final ArrayList<InvalidProperty> invalidProps = new ArrayList<>();
				final String pathMappings = properties.get(TriggerParameters.PATH_MAPPINGS);
				if (StringUtil.isEmptyOrSpaces(pathMappings)) {
					invalidProps.add(new InvalidProperty(TriggerParameters.PATH_MAPPINGS, "Path Mappings must be specified"));
				}
				try {
					TriggerUtils.toDefinitions(pathMappings);
				} catch (MalformedTriggerDefinitionException e) {
					invalidProps.add(new InvalidProperty(TriggerParameters.PATH_MAPPINGS, e.getMessage()));
				}
				
				final String filters = properties.get(TriggerParameters.FILTERS);
				try {
					TriggerUtils.toFilters(filters);
				} catch (MalformedTriggerDefinitionException e) {
					invalidProps.add(new InvalidProperty(TriggerParameters.FILTERS, e.getMessage()));
				}
				
				return invalidProps;
			}
		};
	}
	
	  @Override
	  public String getEditParametersUrl() {
	    return myPluginDescriptor.getPluginResourcesPath("editWebHookBuildTrigger.jsp");
	  }


}
