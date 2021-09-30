package teamcity.plugin.build.trigger.webhook;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import teamcity.plugin.build.trigger.webhook.exception.MalformedTriggerDefinitionException;

public class TriggerUtilsTest {

	@Test
	public void testToDefinitionsWithEmptyString() {
		List<TriggerParameterDefinition> definitions = TriggerUtils.toDefinitions(null);
		assertTrue(definitions.isEmpty());
	}
	
	@Test(expected=MalformedTriggerDefinitionException.class)
	public void testToDefinitionsWithMalformedString() {
		TriggerUtils.toDefinitions("testString");
	}
	
	@Test
	public void testToDefinitionsWithValidDefinitionString() {
		List<TriggerParameterDefinition> definitions = TriggerUtils.toDefinitions("name=foo::required=true::defaultValue=bar::path=$.foo.bar");
		TriggerParameterDefinition definition = definitions.get(0);
		assertTrue(definition.isPopulated());
		assertEquals("foo", definition.getName());
		assertEquals(true, definition.getRequired());
		assertEquals("bar", definition.getDefaultValue());
		assertEquals("$.foo.bar", definition.getPath());
		
	}

}
