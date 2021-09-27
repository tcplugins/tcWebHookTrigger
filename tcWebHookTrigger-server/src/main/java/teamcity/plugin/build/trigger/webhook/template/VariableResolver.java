package teamcity.plugin.build.trigger.webhook.template;
public interface VariableResolver {
	public String resolve(String variable);
}