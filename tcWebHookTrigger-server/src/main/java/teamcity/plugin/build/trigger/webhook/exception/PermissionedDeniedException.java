package teamcity.plugin.build.trigger.webhook.exception;

import javax.servlet.http.HttpServletResponse;

public class PermissionedDeniedException extends WebException {

	private static final long serialVersionUID = -2822082306660833644L;

	public PermissionedDeniedException(Exception ex) {
		super(ex);
	}

	@Override
	public int getStatusCode() {
		return HttpServletResponse.SC_FORBIDDEN;
	}

}
