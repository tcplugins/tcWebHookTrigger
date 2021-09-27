package teamcity.plugin.build.trigger.webhook.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.JsonPath;

import teamcity.plugin.build.trigger.webhook.Constants;
import teamcity.plugin.build.trigger.webhook.TriggerParameterDefinition;
import teamcity.plugin.build.trigger.webhook.exception.UnparsablePayloadException;

public class JsonToPropertiesParser {
	
	private static final String LOGGING_PREFIX = Constants.PLUGIN_NAME + "-" + JsonToPropertiesParser.class.getSimpleName();

	
	public Map<String, String> parse(List<TriggerParameterDefinition> parameterDefinitions, String jsonString) {
		Map<String,String> parameters = new HashMap<>();
		parameterDefinitions.forEach(definition -> {
			try {
				String value = JsonPath.read(jsonString, definition.getPath());
				parameters.put(definition.getName(), value);
			} catch (InvalidJsonException ex) {
				throw new UnparsablePayloadException(ex);
			}
		});
		return parameters;
	}

}
