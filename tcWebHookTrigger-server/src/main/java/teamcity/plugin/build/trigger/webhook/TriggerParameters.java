package teamcity.plugin.build.trigger.webhook;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TriggerParameters {
	
	public static final String PATH_MAPPINGS = "webhook.build.trigger.path.mappings";
	public static final String FILTERS = "webhook.build.trigger.path.filters";
	public static final String INCLUDE_WHOLE_PAYLOAD = "webhook.build.trigger.include.payload";
	public static final String TOP_OF_QUEUE = "webhook.build.trigger.top.of.queue";
	
	@NotNull
	private String myPathMappings;
	@Nullable
	private String myFilters;
	@Nullable
	private Boolean myIncludePayload;
	@Nullable
	private Boolean myTopOfQueue;

	public TriggerParameters(@NotNull String pathMappings, @Nullable String filters, @Nullable Boolean includePayload, @Nullable Boolean topOfQueue) {
		myPathMappings = pathMappings;
		myFilters = filters;
		myIncludePayload = includePayload;
		myTopOfQueue = topOfQueue;
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
	
	@Nullable
	public Boolean getTopOfQueue() {
		return myTopOfQueue;
	}
	
	public void setTopOfQueue(Boolean topOfQueue) {
		this.myTopOfQueue = topOfQueue;
	}
}