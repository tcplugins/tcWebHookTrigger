package teamcity.plugin.build.trigger.webhook.rest.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.util.SessionUser;

@Service(UserProviderService.BEAN_NAME)
public class UserProviderService {

	public static final String BEAN_NAME = "plugins.teamcity.manager.rest.service.UserProviderService";

	public HttpServletRequest getRequest() {
        return ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
	}
	
	public SUser getUser() {
		return SessionUser.getUser(getRequest());

	}
}
