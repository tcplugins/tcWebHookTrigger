package teamcity.plugin.build.trigger.webhook.exception;

import javax.servlet.http.HttpServletResponse;

public class BuildTypeNotFoundException extends WebException {
	private static final long serialVersionUID = -6584921927732186846L;

	public BuildTypeNotFoundException(String buildTypeExternalId) {
		super("No matching build configuration found with configuration id '" + buildTypeExternalId + "'");
	}

	@Override
	public int getStatusCode() {
		return HttpServletResponse.SC_NOT_FOUND;
	}


}
