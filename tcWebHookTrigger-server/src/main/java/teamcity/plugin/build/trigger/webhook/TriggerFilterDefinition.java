package teamcity.plugin.build.trigger.webhook;

public class TriggerFilterDefinition {
	
	String name;
	String template;
	String regex;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTemplate() {
		return template;
	}
	public void setTemplate(String template) {
		this.template = template;
	}
	public String getRegex() {
		return regex;
	}
	public void setRegex(String validatorRegex) {
		this.regex = validatorRegex;
	}
	public boolean isPopulated() {
		return     name != null
				&& template != null
				&& regex != null
			;
	}
	

}
