package teamcity.plugin.build.trigger.webhook.exception;

import javax.servlet.http.HttpServletResponse;

public class UnparsablePayloadException extends WebException {

	public UnparsablePayloadException(Exception ex) {
		super(ex);
	}

	private static final long serialVersionUID = -7467304438646814820L;

	@Override
	public int getStatusCode() {
		return HttpServletResponse.SC_BAD_REQUEST;
	}

}
