package teamcity.plugin.build.trigger.webhook.template;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Test;

public class TemplateMatcherTest {

	@Test
	public void test() {
		TemplateMatcher templateMatcher = new TemplateMatcher("${", "}");
		String processedString = templateMatcher.replace("this is a ${test}", Collections.singletonMap("test", "panther"));
		assertEquals("this is a panther", processedString);
	}
	
	@Test
	public void testTemplateEngine() {
		final String regex = "^(?!.*(closed|review_requested|review_request_removed|enqueued|dequeued)).*$";
		TemplateMatcher templateMatcher = new TemplateMatcher("${", "}");
		String processedString = templateMatcher.replace(regex, Collections.singletonMap("test", "panther"));
		assertEquals(regex, processedString);
	}

}
