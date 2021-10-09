package teamcity.plugin.build.trigger.webhook.exception;

public class MalformedTriggerDefinitionException extends RuntimeException {

	private static final long serialVersionUID = -4028568788563286383L;

	public MalformedTriggerDefinitionException(String message) {
		super(message);
	}

}
