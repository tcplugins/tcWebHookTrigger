package teamcity.plugin.build.trigger.webhook;

import com.intellij.openapi.diagnostic.Logger;

public final class Loggers {
	public static final Logger SERVER 		= Logger.getInstance("jetbrains.buildServer.SERVER");
	public static final Logger ACTIVITIES  	= Logger.getInstance("jetbrains.buildServer.ACTIVITIES");
	
	private Loggers(){}
}
