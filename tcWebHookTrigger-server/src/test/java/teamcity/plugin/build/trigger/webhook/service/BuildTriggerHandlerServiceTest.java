package teamcity.plugin.build.trigger.webhook.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMap;

import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.serverSide.BuildCustomizer;
import jetbrains.buildServer.serverSide.BuildCustomizerFactory;
import jetbrains.buildServer.serverSide.BuildPromotion;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import jetbrains.buildServer.serverSide.auth.AuthorityHolder;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.serverSide.auth.Permissions;
import jetbrains.buildServer.users.User;
import teamcity.plugin.build.trigger.webhook.TriggerParameters;
import teamcity.plugin.build.trigger.webhook.parser.JsonToPropertiesParser;
import teamcity.plugin.build.trigger.webhook.service.BuildTriggerResolverService.TriggersHolder;

@RunWith(MockitoJUnitRunner.class)
public class BuildTriggerHandlerServiceTest {

	private static final String MY_TEST_BUILD_EXTERNAL_ID = "MyTestBuildId";
	private static final String MY_TEST_PROJECT_INTERNAL_ID = "project1";
	private static final String MY_TEST_BUILD_INTERNAL_ID = "build01";
	private static final String TEST_DEFINITION_01 = "name=foo::required=true::defaultValue=bar::path=$.foo.bar";
	private static final String TEST_DEFINITION_02 = "name=branch::required=true::path=$.project.branch";

	@Mock
	BuildTriggerResolverService buildTriggerResolverService;
	
	@Mock
	JsonToPropertiesParser jsonToPropertiesParser = new JsonToPropertiesParser();
	
	@Mock
	BuildCustomizerFactory buildCustomizerFactory;
	
	@Mock
	BuildCustomizer buildCustomizer;
	
	@Mock
	BuildPromotion buildPromotion;
	
	@Mock
	AuthorityHolder currentUser;
	
	@Mock
	User user;
	
	@Mock
	SBuildType sBuildType;
	
	@Mock
	SQueuedBuild sQueuedBuild;

	@Mock BuildTriggerDescriptor triggerDescriptor;
	
	private static final String testJsonString = "{ 'project' : { 'branch' : 'main' } } }";
	
	@Before
	public void setup() {
		when(buildPromotion.addToQueue(anyString())).thenReturn(sQueuedBuild);
		when(sQueuedBuild.getItemId()).thenReturn(String.valueOf(1001001L));
		when(buildCustomizer.createPromotion()).thenReturn(buildPromotion);
		when(triggerDescriptor.getId()).thenReturn(UUID.randomUUID().toString());
		when(triggerDescriptor.getTriggerName()).thenReturn(WebHookBuildTriggerService.WEBHOOK_BUILD_TRIGGER_NAME);
		lenient().when(currentUser.isPermissionGrantedForProject(MY_TEST_PROJECT_INTERNAL_ID, Permission.RUN_BUILD)).thenReturn(true);
		lenient().when(currentUser.getPermissionsGrantedForProject(MY_TEST_BUILD_EXTERNAL_ID)).thenReturn(Permissions.NO_PERMISSIONS);
		lenient().when(currentUser.getPermissionsGrantedForProject(MY_TEST_BUILD_INTERNAL_ID)).thenReturn(Permissions.NO_PERMISSIONS);
		lenient().when(sBuildType.getExternalId()).thenReturn(MY_TEST_BUILD_EXTERNAL_ID);
		lenient().when(sBuildType.getProjectId()).thenReturn(MY_TEST_PROJECT_INTERNAL_ID);
	}
	
	@Test
	public void testDoesNotCallBuildCustomizerFactoryWhenBuildTypeIdIsNull() throws Exception {
		String buildTypeExternalId = MY_TEST_BUILD_EXTERNAL_ID;
		when(buildTriggerResolverService.findTriggersForBuildType(buildTypeExternalId)).thenReturn(new TriggersHolder(sBuildType, Collections.emptyList()));
		BuildTriggerHandlerService triggerHandlerService = new BuildTriggerHandlerService(buildTriggerResolverService, jsonToPropertiesParser, buildCustomizerFactory);
		triggerHandlerService.handleWebHook(currentUser, buildTypeExternalId, "{}");
		verify(buildCustomizerFactory, times(0)).createBuildCustomizer(any(), eq(null));
	}
	
	@Test
	public void testDoesNotCallBuildCustomizerFactoryWhenBuildTypeIdIsMockedButNoTriggersAreFound() throws Exception {
		String buildTypeExternalId = MY_TEST_BUILD_EXTERNAL_ID;
		when(buildTriggerResolverService.findTriggersForBuildType(buildTypeExternalId)).thenReturn(new TriggersHolder(sBuildType, Collections.emptyList()));
		BuildTriggerHandlerService triggerHandlerService = new BuildTriggerHandlerService(buildTriggerResolverService, jsonToPropertiesParser, buildCustomizerFactory);
		triggerHandlerService.handleWebHook(currentUser, buildTypeExternalId, "{}");
		verify(buildCustomizerFactory, times(0)).createBuildCustomizer(any(), eq(null));
	}
	@Test
	public void testCallsBuildCustomizerFactoryWhenTriggersAreFound() throws Exception {
		String buildTypeExternalId = MY_TEST_BUILD_EXTERNAL_ID;
		when(triggerDescriptor.getProperties()).thenReturn(Collections.singletonMap(TriggerParameters.PATH_MAPPINGS, TEST_DEFINITION_01));
		when(buildCustomizerFactory.createBuildCustomizer(sBuildType, null)).thenReturn(buildCustomizer);
		when(buildTriggerResolverService.findTriggersForBuildType(buildTypeExternalId)).thenReturn(new TriggersHolder(sBuildType, Collections.singletonList(triggerDescriptor)));
		BuildTriggerHandlerService triggerHandlerService = new BuildTriggerHandlerService(buildTriggerResolverService, jsonToPropertiesParser, buildCustomizerFactory);
		triggerHandlerService.handleWebHook(currentUser, buildTypeExternalId, testJsonString);
		verify(buildCustomizerFactory, times(1)).createBuildCustomizer(any(), eq(null));
	}
	@Test
	public void testCallsBuildCustomizerFactoryWhenTriggersAreFoundAndFiltersMatch() throws Exception {
		String buildTypeExternalId = MY_TEST_BUILD_EXTERNAL_ID;
		jsonToPropertiesParser = new JsonToPropertiesParser();
		when(triggerDescriptor.getProperties()).thenReturn(ImmutableMap.of(
				TriggerParameters.PATH_MAPPINGS, TEST_DEFINITION_02,
				TriggerParameters.FILTERS, "name=branch::template=${branch}::regex=\\w+"));
		when(buildCustomizerFactory.createBuildCustomizer(sBuildType, null)).thenReturn(buildCustomizer);
		when(buildTriggerResolverService.findTriggersForBuildType(buildTypeExternalId)).thenReturn(new TriggersHolder(sBuildType, Collections.singletonList(triggerDescriptor)));
		BuildTriggerHandlerService triggerHandlerService = new BuildTriggerHandlerService(buildTriggerResolverService, jsonToPropertiesParser, buildCustomizerFactory);
		triggerHandlerService.handleWebHook(currentUser, buildTypeExternalId, testJsonString);
		verify(buildCustomizerFactory, times(1)).createBuildCustomizer(any(), eq(null));
	}
	@Test
	public void testDoesNotCallBuildCustomizerFactoryWhenTriggersAreFoundButFiltersDontMatch() throws Exception {
		String buildTypeExternalId = MY_TEST_BUILD_EXTERNAL_ID;
		when(triggerDescriptor.getProperties()).thenReturn(ImmutableMap.of(
				TriggerParameters.PATH_MAPPINGS, TEST_DEFINITION_01,
				TriggerParameters.FILTERS, "name=branch::template=${branch}::regex=\\s+"));
		when(buildTriggerResolverService.findTriggersForBuildType(buildTypeExternalId)).thenReturn(new TriggersHolder(sBuildType, Collections.singletonList(triggerDescriptor)));
		BuildTriggerHandlerService triggerHandlerService = new BuildTriggerHandlerService(buildTriggerResolverService, jsonToPropertiesParser, buildCustomizerFactory);
		triggerHandlerService.handleWebHook(currentUser, buildTypeExternalId, testJsonString);
		verify(buildCustomizerFactory, times(0)).createBuildCustomizer(any(), eq(null));
	}

}
