package teamcity.plugin.build.trigger.webhook.rest.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jetbrains.buildServer.serverSide.auth.AuthorityHolder;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.util.SessionUser;

@Service(UserProviderService.BEAN_NAME)
public class UserProviderService {
	
	private SecurityContext mySecurityContext;

	public UserProviderService(SecurityContext securityContext) {
		mySecurityContext = securityContext;
	}

	public static final String BEAN_NAME = "teamcity.plugin.build.trigger.webhook.rest.service.UserProviderService";

	public HttpServletRequest getRequest() {
        return ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
	}
	
	public SUser getUser() {
		return SessionUser.getUser(getRequest());

	}
	
	public AuthorityHolder getAuthorityHolder() {
		return this.mySecurityContext.getAuthorityHolder();
	}
}
