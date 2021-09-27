package teamcity.plugin.build.trigger.webhook.template;

import java.util.Map;

public class TemplatedTextReplacer {
	
	//Private constructor for utility class
	private TemplatedTextReplacer() {}
	
	private static final TemplateMatcher matcher = new TemplateMatcher("${", "}");
	
	public static String resolve(String template, Map<String,String> variables) {
		return matcher.replace(matcher.replace(template, variables),variables);
	}

}
