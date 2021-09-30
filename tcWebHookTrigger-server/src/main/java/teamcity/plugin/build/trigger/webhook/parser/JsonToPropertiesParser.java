package teamcity.plugin.build.trigger.webhook.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import teamcity.plugin.build.trigger.webhook.Constants;
import teamcity.plugin.build.trigger.webhook.Loggers;
import teamcity.plugin.build.trigger.webhook.TriggerParameterDefinition;
import teamcity.plugin.build.trigger.webhook.exception.UnparsablePayloadException;

public class JsonToPropertiesParser {
	
	private static final String LOGGING_PREFIX = Constants.PLUGIN_NAME + "-" + JsonToPropertiesParser.class.getSimpleName();

	
	public Map<String, String> parse(List<TriggerParameterDefinition> parameterDefinitions, String jsonString) {
		Map<String,String> resolvedParameters = new HashMap<>();
		parameterDefinitions.forEach(definition -> {
			try {
				String value = convertObject(JsonPath.read(jsonString, definition.getPath()));
				resolvedParameters.put(definition.getName(), value);
			} catch (InvalidJsonException ex) {
				throw new UnparsablePayloadException(ex);
			} catch (PathNotFoundException ex) {
				Loggers.ACTIVITIES.debug(String.format("%s: Path not found in payload. name='%s', path='%s'", LOGGING_PREFIX, definition.getName(), definition.getPath()));
			}
		});
		return resolvedParameters;
	}


	private String convertObject(Object read) {
		if (read == null) {
			return null;
		}
		return String.valueOf(read);
	}

}
