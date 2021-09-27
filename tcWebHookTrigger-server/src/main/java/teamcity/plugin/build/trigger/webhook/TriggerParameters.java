package teamcity.plugin.build.trigger.webhook;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TriggerParameters {
	
	public static final String PATH_MAPPINGS = "webhook.build.trigger.path.mappings";
	public static final String FILTERS = "webhook.build.trigger.path.filters";
	public static final String INCLUDE_WHOLE_PAYLOAD = "webhook.build.trigger.include.payload";
	
	@NotNull
	private String myPathMappings;
	@Nullable
	private String myFilters;
	@Nullable
	private Boolean myIncludePayload;

	public TriggerParameters(@NotNull String pathMappings, @Nullable String filters, @Nullable Boolean includePayload) {
		myPathMappings = pathMappings;
		myFilters = filters;
		myIncludePayload = includePayload;
	}

	@NotNull
	public String getPathMappings() {
		return myPathMappings;
	}

	public void setPathMappings(String pathMappings) {
		this.myPathMappings = pathMappings;
	}
	
	@Nullable
	public String getFilters() {
		return myFilters;
	}
	
	public void setFilters(String filters) {
		this.myFilters = filters;
	}

	@Nullable
	public Boolean getIncludePayload() {
		return myIncludePayload;
	}

	public void setIncludePayload(Boolean includePayload) {
		this.myIncludePayload = includePayload;
	}
}