package teamcity.plugin.build.trigger.webhook;

public class TriggerFilterDefinition {
	
	String template;
	String regex;
	
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
		return     template != null
				&& regex != null
			;
	}
	

}
