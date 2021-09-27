package teamcity.plugin.build.trigger.webhook.exception;

@SuppressWarnings("serial")
public abstract class WebException extends RuntimeException {
	
	public WebException(Exception ex) {
		super(ex);
	}

	public WebException(String message) {
		super(message);
	}

	public abstract int getStatusCode();

}
