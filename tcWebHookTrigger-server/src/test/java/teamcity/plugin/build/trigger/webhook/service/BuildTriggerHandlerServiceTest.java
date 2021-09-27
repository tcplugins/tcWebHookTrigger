package teamcity.plugin.build.trigger.webhook.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
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
import jetbrains.buildServer.users.SUser;
import teamcity.plugin.build.trigger.webhook.TriggerParameterDefinition;
import teamcity.plugin.build.trigger.webhook.TriggerParameters;
import teamcity.plugin.build.trigger.webhook.TriggerUtils;
import teamcity.plugin.build.trigger.webhook.parser.JsonToPropertiesParser;
import teamcity.plugin.build.trigger.webhook.service.BuildTriggerResolverService.TriggersHolder;

@RunWith(MockitoJUnitRunner.class)
public class BuildTriggerHandlerServiceTest {

	private static final String TEST_DEFINITION_01 = "name=foo::required=true::defaultValue=bar::path=$.foo.bar";

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
	SUser currentUser;
	
	@Mock
	SBuildType sBuildType;
	
	@Mock
	SQueuedBuild sQueuedBuild;

	@Mock BuildTriggerDescriptor triggerDescriptor;
	
	private static final String testJsonString = "{ 'project' : { 'branch' : 'main' } } }";
	
	private List<TriggerParameterDefinition> testDefinitions;
	
	@Before
	public void setup() {
		when(buildPromotion.addToQueue(anyString())).thenReturn(sQueuedBuild);
		when(sQueuedBuild.getItemId()).thenReturn(String.valueOf(1001001L));
		when(buildCustomizer.createPromotion()).thenReturn(buildPromotion);
		when(triggerDescriptor.getId()).thenReturn(UUID.randomUUID().toString());
		when(triggerDescriptor.getTriggerName()).thenReturn(WebHookBuildTriggerService.WEBHOOK_BUILD_TRIGGER_NAME);
		testDefinitions = TriggerUtils.toDefinitions(TEST_DEFINITION_01);
	}
	
	@Test
	public void testDoesNotCallBuildCustomizerFactoryWhenBuildTypeIdIsNull() throws Exception {
		String buildTypeExternalId = "MyTestBuildId";
		when(buildTriggerResolverService.findTriggersForBuildType(buildTypeExternalId)).thenReturn(new TriggersHolder(null, Collections.emptyList()));
		BuildTriggerHandlerService triggerHandlerService = new BuildTriggerHandlerService(buildTriggerResolverService, jsonToPropertiesParser, buildCustomizerFactory);
		triggerHandlerService.handleWebHook(currentUser, buildTypeExternalId, "{}");
		verify(buildCustomizerFactory, times(0)).createBuildCustomizer(any(), eq(currentUser));
	}
	
	@Test
	public void testDoesNotCallBuildCustomizerFactoryWhenBuildTypeIdIsMockedButNoTriggersAreFound() throws Exception {
		String buildTypeExternalId = "MyTestBuildId";
		when(buildTriggerResolverService.findTriggersForBuildType(buildTypeExternalId)).thenReturn(new TriggersHolder(sBuildType, Collections.emptyList()));
		BuildTriggerHandlerService triggerHandlerService = new BuildTriggerHandlerService(buildTriggerResolverService, jsonToPropertiesParser, buildCustomizerFactory);
		triggerHandlerService.handleWebHook(currentUser, buildTypeExternalId, "{}");
		verify(buildCustomizerFactory, times(0)).createBuildCustomizer(any(), eq(currentUser));
	}
	@Test
	public void testCallsBuildCustomizerFactoryWhenTriggersAreFound() throws Exception {
		String buildTypeExternalId = "MyTestBuildId";
		when(triggerDescriptor.getProperties()).thenReturn(Collections.singletonMap(TriggerParameters.PATH_MAPPINGS, TEST_DEFINITION_01));
		when(buildCustomizerFactory.createBuildCustomizer(sBuildType, currentUser)).thenReturn(buildCustomizer);
		when(buildTriggerResolverService.findTriggersForBuildType(buildTypeExternalId)).thenReturn(new TriggersHolder(sBuildType, Collections.singletonList(triggerDescriptor)));
		BuildTriggerHandlerService triggerHandlerService = new BuildTriggerHandlerService(buildTriggerResolverService, jsonToPropertiesParser, buildCustomizerFactory);
		triggerHandlerService.handleWebHook(currentUser, buildTypeExternalId, testJsonString);
		verify(buildCustomizerFactory, times(1)).createBuildCustomizer(any(), eq(currentUser));
	}
	@Test
	public void testCallsBuildCustomizerFactoryWhenTriggersAreFoundAndFiltersMatch() throws Exception {
		String buildTypeExternalId = "MyTestBuildId";
		when(triggerDescriptor.getProperties()).thenReturn(ImmutableMap.of(
				TriggerParameters.PATH_MAPPINGS, TEST_DEFINITION_01,
				TriggerParameters.FILTERS, "template=${branch}::regex=\\s"));
		when(buildCustomizerFactory.createBuildCustomizer(sBuildType, currentUser)).thenReturn(buildCustomizer);
		when(buildTriggerResolverService.findTriggersForBuildType(buildTypeExternalId)).thenReturn(new TriggersHolder(sBuildType, Collections.singletonList(triggerDescriptor)));
		BuildTriggerHandlerService triggerHandlerService = new BuildTriggerHandlerService(buildTriggerResolverService, jsonToPropertiesParser, buildCustomizerFactory);
		triggerHandlerService.handleWebHook(currentUser, buildTypeExternalId, testJsonString);
		verify(buildCustomizerFactory, times(1)).createBuildCustomizer(any(), eq(currentUser));
	}
	@Test
	@Ignore
	public void testDoesNotCallBuildCustomizerFactoryWhenTriggersAreFoundButFiltersDontMatch() throws Exception {
		String buildTypeExternalId = "MyTestBuildId";
		when(triggerDescriptor.getProperties()).thenReturn(ImmutableMap.of(
				TriggerParameters.PATH_MAPPINGS, TEST_DEFINITION_01,
				TriggerParameters.FILTERS, "template=${branch}::regex=\\s"));
		when(buildCustomizerFactory.createBuildCustomizer(sBuildType, currentUser)).thenReturn(buildCustomizer);
		when(buildTriggerResolverService.findTriggersForBuildType(buildTypeExternalId)).thenReturn(new TriggersHolder(sBuildType, Collections.singletonList(triggerDescriptor)));
		BuildTriggerHandlerService triggerHandlerService = new BuildTriggerHandlerService(buildTriggerResolverService, jsonToPropertiesParser, buildCustomizerFactory);
		triggerHandlerService.handleWebHook(currentUser, buildTypeExternalId, testJsonString);
		verify(buildCustomizerFactory, times(0)).createBuildCustomizer(any(), eq(currentUser));
	}

}
