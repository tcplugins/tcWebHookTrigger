package teamcity.plugin.build.trigger.webhook.parser;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import teamcity.plugin.build.trigger.webhook.TriggerParameterDefinition;
import teamcity.plugin.build.trigger.webhook.exception.UnparsablePayloadException;

public class JsonToPropertiesParserTest {

	@Test
	public void test() throws IOException {
		String jsonString = new String ( Files.readAllBytes( Paths.get("src/test/resources/example_payload.json") ));
		JsonToPropertiesParser parser = new JsonToPropertiesParser();
		TriggerParameterDefinition definition = new TriggerParameterDefinition();
		definition.setName("project_namespace");
		definition.setPath("$.project.namespace");
		List<TriggerParameterDefinition> parameterDefinitions = Collections.singletonList(definition);
		Map<String,String> vars = parser.parse(parameterDefinitions, jsonString);
		
		assertEquals("DevOps", vars.get("project_namespace"));
	}
	
	@Test(expected = UnparsablePayloadException.class)
	public void testUnparsableJson() throws IOException {
		String jsonString = " {'}";
		JsonToPropertiesParser parser = new JsonToPropertiesParser();
		TriggerParameterDefinition definition = new TriggerParameterDefinition();
		definition.setName("project_namespace");
		definition.setPath("$.project.namespace");
		List<TriggerParameterDefinition> parameterDefinitions = Collections.singletonList(definition);
		parser.parse(parameterDefinitions, jsonString);
		
	}

}
