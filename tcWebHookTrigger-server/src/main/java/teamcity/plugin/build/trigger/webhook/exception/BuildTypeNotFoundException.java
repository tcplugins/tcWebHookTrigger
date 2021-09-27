package teamcity.plugin.build.trigger.webhook.exception;

public class BuildTypeNotFoundException extends WebException {
	private static final long serialVersionUID = -6584921927732186846L;

	public BuildTypeNotFoundException(String buildTypeExternalId) {
		super("No matching build configuration found with configuration id '" + buildTypeExternalId + "'");
	}

	@Override
	public int getStatusCode() {
		// TODO Auto-generated method stub
		return 0;
	}


}
