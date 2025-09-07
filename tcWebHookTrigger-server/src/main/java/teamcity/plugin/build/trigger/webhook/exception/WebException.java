package teamcity.plugin.build.trigger.webhook.exception;

@SuppressWarnings("serial")
public abstract class WebException extends RuntimeException {
	
	protected WebException(Exception ex) {
		super(ex);
	}

	protected WebException(String message) {
		super(message);
	}

	public abstract int getStatusCode();

}
