package teamcity.plugin.build.trigger.webhook.controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.web.servlet.ModelAndView;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.SessionUser;
import teamcity.plugin.build.trigger.webhook.exception.BuildTypeNotFoundException;
import teamcity.plugin.build.trigger.webhook.exception.PermissionedDeniedException;
import teamcity.plugin.build.trigger.webhook.exception.UnparsablePayloadException;
import teamcity.plugin.build.trigger.webhook.service.BuildTriggerHandlerService;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class TriggerController extends BaseController {
	
	public static final String TRIGGER_BASE_URI = "/app/webhook-trigger";
	private static final Pattern buildTypeIdPattern = Pattern.compile("^" + TRIGGER_BASE_URI + "/(\\S+?)$");

	
	private BuildTriggerHandlerService myBuildTriggerHandlerService;

	public TriggerController(
			SBuildServer sBuildServer,
			WebControllerManager webControllerManager,
			BuildTriggerHandlerService buildTriggerHandlerService) {
		super(sBuildServer);
		myBuildTriggerHandlerService = buildTriggerHandlerService;
		webControllerManager.registerController(TRIGGER_BASE_URI + "/*", this);
	}

	@Override
	protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (isPost(request)) {
			try {
				String uriPath = request.getPathInfo();
				SUser sUser = SessionUser.getUser(request);
				Matcher matcher = buildTypeIdPattern.matcher(uriPath);
				if (matcher.matches()) {
					String buildTypeExternalId = matcher.group(1);
					String payload = IOUtils.toString(request.getReader());
					myBuildTriggerHandlerService.handleWebHook(sUser, buildTypeExternalId, payload);
					response.setStatus(HttpServletResponse.SC_ACCEPTED);
					return null;
				} else {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The URL was unexpected. Please use a TeamCity Build configuration ID");
					return null;
				}
			} catch (BuildTypeNotFoundException | PermissionedDeniedException | UnparsablePayloadException ex) {
				response.sendError(ex.getStatusCode(), ex.getMessage());
				return null;
			}
		}
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "WebHooks must be POSTed");
		return null;
	}

}
