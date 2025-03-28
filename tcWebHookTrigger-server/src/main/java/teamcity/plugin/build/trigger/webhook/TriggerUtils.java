package teamcity.plugin.build.trigger.webhook;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.util.StringUtils;

import teamcity.plugin.build.trigger.webhook.exception.MalformedTriggerDefinitionException;


public class TriggerUtils {
	
	public static final String PARAM_NAME = "name";
	public static final String PARAM_XPATH = "path";
	public static final String PARAM_DEFAULTVALUE = "defaultValue";
	public static final String PARAM_REQUIRED = "required";
	public static final String PARAM_TEMPLATE = "template";
	public static final String PARAM_REGEX = "regex";
	
	private static final String[] PATH_PARAMS = { PARAM_NAME, PARAM_XPATH, PARAM_DEFAULTVALUE, PARAM_REQUIRED };
	private static final String[] TRIGGER_PARAMS = { PARAM_NAME, PARAM_TEMPLATE, PARAM_REGEX };
	
	public static List<TriggerParameterDefinition> toDefinitions(String pathMappingsStr) {
		List<TriggerParameterDefinition> triggerParameterDefinitions = new ArrayList<>();
		if (!StringUtils.isEmpty(pathMappingsStr)) {
			for (String line : pathMappingsStr.split("\\n")) {
				TriggerParameterDefinition definition = new TriggerParameterDefinition();
				String[] params = line.split("::", 5);
				for (String param : params) {
					for (String p : PATH_PARAMS) {
						final String prefix = p + "=";
						if (param.trim().toLowerCase().startsWith(prefix.toLowerCase())) {
							String value = param.trim().substring(prefix.length()).trim();
							try {
								BeanUtils.setProperty(definition, p, value);
							} catch (IllegalAccessException | InvocationTargetException e) {
								Loggers.TRIGGERS.warn(String.format("Failed to set definition '%s' to value '%s'. Full string was '%s'", p, value, line));
							}
						}
					}
				}
				if (definition.isPopulated()) {
					triggerParameterDefinitions.add(definition);
				} else {
					throw new MalformedTriggerDefinitionException(String.format("TriggerParameterDefinition looks malformed: '%s'", line));
				}
			}
			
		}
		return triggerParameterDefinitions;
	}
	
	public static List<TriggerFilterDefinition> toFilters(String filtersStr) {
		List<TriggerFilterDefinition> triggerFilterDefinitions = new ArrayList<>();
		if (!StringUtils.isEmpty(filtersStr)) {
			for (String line : filtersStr.split("\\n")) {
				TriggerFilterDefinition definition = new TriggerFilterDefinition();
				String[] params = line.split("::", 5);
				for (String param : params) {
					for (String p : TRIGGER_PARAMS) {
						final String prefix = p + "=";
						if (param.trim().toLowerCase().startsWith(prefix.toLowerCase())) {
							String value = param.trim().substring(prefix.length()).trim();
							try {
								BeanUtils.setProperty(definition, p, value);
							} catch (IllegalAccessException | InvocationTargetException e) {
								Loggers.TRIGGERS.warn(String.format("Failed to set trigger '%s' to value '%s'. Full string was '%s'", p, value, line));
							}
						}
					}
				}
				if (definition.isPopulated()) {
					triggerFilterDefinitions.add(definition);
				} else {
					throw new MalformedTriggerDefinitionException(String.format("TriggerFilterDefinition looks malformed or incomplete. Please specify 'name','template' and 'regex' values. Provided string was: '%s'", line));
				}
			}
			
		}
		return triggerFilterDefinitions;
	}

}
