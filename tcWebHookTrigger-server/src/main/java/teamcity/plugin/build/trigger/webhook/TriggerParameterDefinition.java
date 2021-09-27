package teamcity.plugin.build.trigger.webhook;

public class TriggerParameterDefinition {
	
	String name;
	String path;
	String defaultValue;
	Boolean required;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String xPath) {
		this.path = xPath;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public Boolean getRequired() {
		return required;
	}
	public void setRequired(Boolean required) {
		this.required = required;
	}
	public boolean isPopulated() {
		return     name != null
				&& path != null
				&& required != null
			;
	}
	
	
}
