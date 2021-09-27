package teamcity.plugin.build.trigger.webhook;

import static org.junit.Assert.*;
import static org.hamcrest.collection.IsEmptyCollection.*;

import java.util.List;

import org.junit.Test;

import teamcity.plugin.build.trigger.webhook.exception.MalformedTriggerDefinitionException;

public class TriggerUtilsTest {

	@Test
	public void testToDefinitionsWithEmptyString() {
		List<TriggerParameterDefinition> definitions = TriggerUtils.toDefinitions(null);
		assertThat(definitions, empty());
	}
	
	@Test(expected=MalformedTriggerDefinitionException.class)
	public void testToDefinitionsWithMalformedString() {
		List<TriggerParameterDefinition> definitions = TriggerUtils.toDefinitions("testString");
		assertThat(definitions, empty());
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
