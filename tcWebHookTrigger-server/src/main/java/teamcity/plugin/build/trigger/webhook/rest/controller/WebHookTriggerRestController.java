package teamcity.plugin.build.trigger.webhook.rest.controller;

import static teamcity.plugin.build.trigger.webhook.Constants.APP_REST_PREFIX;
import static teamcity.plugin.build.trigger.webhook.Constants.REST_PREFIX;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jetbrains.buildServer.users.SUser;
import teamcity.plugin.build.trigger.webhook.rest.service.UserProviderService;
import teamcity.plugin.build.trigger.webhook.service.BuildTriggerHandlerService;
import teamcity.plugin.rest.core.Loggers;
import teamcity.plugin.rest.core.controller.BaseRestApiController;

@RestController
@RequestMapping(value = { REST_PREFIX, APP_REST_PREFIX}, produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
public class WebHookTriggerRestController extends BaseRestApiController {

	private BuildTriggerHandlerService myBuildTriggerHandlerService;
	private UserProviderService myUserProviderService;

    public WebHookTriggerRestController(
    		BuildTriggerHandlerService buildTriggerHandlerService,
    		UserProviderService userProviderService) {
		this.myBuildTriggerHandlerService = buildTriggerHandlerService;
		this.myUserProviderService = userProviderService;
    	Loggers.SERVER.info("PluginsApi :: WebHookTriggerRestController controller starting");
	}
    
    @PostMapping("/{buildTypeExternalId}")
    public ResponseEntity<String> restartServer(@PathVariable String buildTypeExternalId,  @RequestBody String payload) {
    	//checkIfAdministator();
		SUser sUser = myUserProviderService.getUser();
		myBuildTriggerHandlerService.handleWebHook(sUser, buildTypeExternalId, payload);
		return ResponseEntity.accepted().build();
    }
    
}
