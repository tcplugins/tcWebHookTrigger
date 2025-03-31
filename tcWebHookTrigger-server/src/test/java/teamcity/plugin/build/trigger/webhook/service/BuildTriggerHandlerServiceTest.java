package teamcity.plugin.build.trigger.webhook.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import jetbrains.buildServer.vcs.SVcsModification;
import jetbrains.buildServer.vcs.VcsModificationHistoryEx;
import jetbrains.buildServer.vcs.VcsRootInstance;
import teamcity.plugin.build.trigger.webhook.Constants;
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
	private static final String TEST_DEFINITION_03 = "name=commit::required=true::path=$.project.commit";

	@Mock
	BuildTriggerResolverService buildTriggerResolverService;
	
	@Mock
	JsonToPropertiesParser jsonToPropertiesParser = new JsonToPropertiesParser();
	
	@Mock
	BuildCustomizerFactory buildCustomizerFactory;
	
	@Mock
	BuildCustomizer buildCustomizer;
	
	@Mock
	VcsModificationHistoryEx vcsModificationHistoryEx;
	
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
	
	@Mock VcsRootInstance vcsRootInstance; 
	@Mock SVcsModification svcsModification; 
	
	private static final String testJsonString = "{ 'project' : { 'branch' : 'main', 'commit' : '1234567' } } }";
	
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
		BuildTriggerHandlerService triggerHandlerService = new BuildTriggerHandlerService(buildTriggerResolverService, jsonToPropertiesParser, buildCustomizerFactory, vcsModificationHistoryEx);
		triggerHandlerService.handleWebHook(currentUser, buildTypeExternalId, "{}");
		verify(buildCustomizerFactory, times(0)).createBuildCustomizer(any(), eq(null));
	}
	
	@Test
	public void testDoesNotCallBuildCustomizerFactoryWhenBuildTypeIdIsMockedButNoTriggersAreFound() throws Exception {
		String buildTypeExternalId = MY_TEST_BUILD_EXTERNAL_ID;
		when(buildTriggerResolverService.findTriggersForBuildType(buildTypeExternalId)).thenReturn(new TriggersHolder(sBuildType, Collections.emptyList()));
		BuildTriggerHandlerService triggerHandlerService = new BuildTriggerHandlerService(buildTriggerResolverService, jsonToPropertiesParser, buildCustomizerFactory, vcsModificationHistoryEx);
		triggerHandlerService.handleWebHook(currentUser, buildTypeExternalId, "{}");
		verify(buildCustomizerFactory, times(0)).createBuildCustomizer(any(), eq(null));
	}
	@Test
	public void testCallsBuildCustomizerFactoryWhenTriggersAreFound() throws Exception {
		String buildTypeExternalId = MY_TEST_BUILD_EXTERNAL_ID;
		when(triggerDescriptor.getProperties()).thenReturn(Collections.singletonMap(TriggerParameters.PATH_MAPPINGS, TEST_DEFINITION_01));
		when(buildCustomizerFactory.createBuildCustomizer(sBuildType, null)).thenReturn(buildCustomizer);
		when(buildTriggerResolverService.findTriggersForBuildType(buildTypeExternalId)).thenReturn(new TriggersHolder(sBuildType, Collections.singletonList(triggerDescriptor)));
		BuildTriggerHandlerService triggerHandlerService = new BuildTriggerHandlerService(buildTriggerResolverService, jsonToPropertiesParser, buildCustomizerFactory, vcsModificationHistoryEx);
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
		BuildTriggerHandlerService triggerHandlerService = new BuildTriggerHandlerService(buildTriggerResolverService, jsonToPropertiesParser, buildCustomizerFactory, vcsModificationHistoryEx);
		triggerHandlerService.handleWebHook(currentUser, buildTypeExternalId, testJsonString);
		verify(buildCustomizerFactory, times(1)).createBuildCustomizer(any(), eq(null));
	}
	@Test
	public void testCallsBuildCustomizerFactoryWhenTriggersAreFoundWithRegexFilters() throws Exception {
		String buildTypeExternalId = MY_TEST_BUILD_EXTERNAL_ID;
		jsonToPropertiesParser = new JsonToPropertiesParser();
		when(triggerDescriptor.getProperties()).thenReturn(ImmutableMap.of(
				TriggerParameters.PATH_MAPPINGS, "name=branch_var::required=true::path=$.project.branch",
				TriggerParameters.FILTERS, "name=branch_var::template=${branch_var}::regex=refs/(bugfix|feature)/((.+))\nname=branch::template=${branch_var_2}::regex=\\w+"));
		when(buildCustomizerFactory.createBuildCustomizer(sBuildType, null)).thenReturn(buildCustomizer);
		when(buildTriggerResolverService.findTriggersForBuildType(buildTypeExternalId)).thenReturn(new TriggersHolder(sBuildType, Collections.singletonList(triggerDescriptor)));
		BuildTriggerHandlerService triggerHandlerService = new BuildTriggerHandlerService(buildTriggerResolverService, jsonToPropertiesParser, buildCustomizerFactory, vcsModificationHistoryEx);
		triggerHandlerService.handleWebHook(currentUser, buildTypeExternalId, "{ 'project' : { 'branch' : 'refs/feature/my_feature_01', 'commit' : '1234567' } } }");
		verify(buildCustomizerFactory, times(1)).createBuildCustomizer(any(), eq(null));
	}
	@Test
	public void testCallsBuildCustomizerFactoryWhenTriggersAreFoundWithNegativeRegexFilters() throws Exception {
		String buildTypeExternalId = MY_TEST_BUILD_EXTERNAL_ID;
		jsonToPropertiesParser = new JsonToPropertiesParser();
		when(triggerDescriptor.getProperties()).thenReturn(ImmutableMap.of(
				TriggerParameters.PATH_MAPPINGS, "name=action::required=true::path=$.action",
				TriggerParameters.FILTERS, "name=action::template=${action}::regex=^(?!.*(closed|review_requested|review_request_removed|enqueued|dequeued)).*$"));
		when(buildCustomizerFactory.createBuildCustomizer(sBuildType, null)).thenReturn(buildCustomizer);
		when(buildTriggerResolverService.findTriggersForBuildType(buildTypeExternalId)).thenReturn(new TriggersHolder(sBuildType, Collections.singletonList(triggerDescriptor)));
		BuildTriggerHandlerService triggerHandlerService = new BuildTriggerHandlerService(buildTriggerResolverService, jsonToPropertiesParser, buildCustomizerFactory, vcsModificationHistoryEx);
		triggerHandlerService.handleWebHook(currentUser, buildTypeExternalId, "{ 'action': 'checks_requested' }");
		verify(buildCustomizerFactory, times(1)).createBuildCustomizer(any(), eq(null));
	}
	@Test
	public void testDoesNotCallBuildCustomizerFactoryWhenTriggersAreFoundButFiltersDontMatch() throws Exception {
		String buildTypeExternalId = MY_TEST_BUILD_EXTERNAL_ID;
		when(triggerDescriptor.getProperties()).thenReturn(ImmutableMap.of(
				TriggerParameters.PATH_MAPPINGS, TEST_DEFINITION_01,
				TriggerParameters.FILTERS, "name=branch::template=${branch}::regex=\\s+"));
		when(buildTriggerResolverService.findTriggersForBuildType(buildTypeExternalId)).thenReturn(new TriggersHolder(sBuildType, Collections.singletonList(triggerDescriptor)));
		BuildTriggerHandlerService triggerHandlerService = new BuildTriggerHandlerService(buildTriggerResolverService, jsonToPropertiesParser, buildCustomizerFactory, vcsModificationHistoryEx);
		triggerHandlerService.handleWebHook(currentUser, buildTypeExternalId, testJsonString);
		verify(buildCustomizerFactory, times(0)).createBuildCustomizer(any(), eq(null));
	}
	@Test
	public void testCallsAddToQueueWhenCommitTriggersAreFound() throws Exception {
		String buildTypeExternalId = MY_TEST_BUILD_EXTERNAL_ID;
		jsonToPropertiesParser = new JsonToPropertiesParser();
		when(triggerDescriptor.getProperties()).thenReturn(ImmutableMap.of(
				TriggerParameters.PATH_MAPPINGS, TEST_DEFINITION_03));
		when(buildCustomizerFactory.createBuildCustomizer(sBuildType, null)).thenReturn(buildCustomizer);
		when(sBuildType.getVcsRootInstances()).thenReturn(Collections.singletonList(vcsRootInstance));
		when(vcsModificationHistoryEx.findModificationByVersion(vcsRootInstance, "1234567")).thenReturn(svcsModification);
		when(buildTriggerResolverService.findTriggersForBuildType(buildTypeExternalId)).thenReturn(new TriggersHolder(sBuildType, Collections.singletonList(triggerDescriptor)));
		BuildTriggerHandlerService triggerHandlerService = new BuildTriggerHandlerService(buildTriggerResolverService, jsonToPropertiesParser, buildCustomizerFactory, vcsModificationHistoryEx);
		triggerHandlerService.handleWebHook(currentUser, buildTypeExternalId, testJsonString);
		verify(buildCustomizerFactory).createBuildCustomizer(any(), eq(null));
		verify(buildCustomizer).createPromotion();
		verify(buildCustomizer).setChangesUpTo(svcsModification);
		verify(buildPromotion).addToQueue(eq(Constants.PLUGIN_DESCRIPTION));
	}
	@Test
	public void testDoesNotCallAddToQueueWhenCommitTriggersDoNotFindCommit() throws Exception {
		String buildTypeExternalId = MY_TEST_BUILD_EXTERNAL_ID;
		jsonToPropertiesParser = new JsonToPropertiesParser();
		when(triggerDescriptor.getProperties()).thenReturn(ImmutableMap.of(
				TriggerParameters.PATH_MAPPINGS, TEST_DEFINITION_03));
		when(buildCustomizerFactory.createBuildCustomizer(sBuildType, null)).thenReturn(buildCustomizer);
		when(sBuildType.getVcsRootInstances()).thenReturn(Collections.emptyList());
		when(buildTriggerResolverService.findTriggersForBuildType(buildTypeExternalId)).thenReturn(new TriggersHolder(sBuildType, Collections.singletonList(triggerDescriptor)));
		BuildTriggerHandlerService triggerHandlerService = new BuildTriggerHandlerService(buildTriggerResolverService, jsonToPropertiesParser, buildCustomizerFactory, vcsModificationHistoryEx);
		triggerHandlerService.handleWebHook(currentUser, buildTypeExternalId, testJsonString);
		verify(buildCustomizerFactory).createBuildCustomizer(any(), eq(null));
		verify(buildCustomizer,times(0)).createPromotion();
		verify(buildCustomizer, times(0)).setChangesUpTo(svcsModification);
		verify(buildPromotion, times(0)).addToQueue(eq(Constants.PLUGIN_DESCRIPTION));
	}
	
	@Test
	public void testMatcher() {
		final String regex = "[Ffred]{4}";
		assertTrue(Pattern.matches(regex, "Fred"));
		assertTrue(Pattern.matches(regex, "fred"));
		assertTrue(Pattern.matches(regex, "derf"));
		
		assertFalse(Pattern.matches(regex, "fran"));
		assertFalse(Pattern.matches(regex, "Fre"));
		assertFalse(Pattern.matches(regex, "Friend"));
		assertFalse(Pattern.matches(regex, "Freddy"));
	}
	
	@Test
	public void testMatcherWithNegativeRegex() {
		final String regex = "^(?!.*(closed|review_requested|review_request_removed|enqueued|dequeued)).*$";
		assertFalse(Pattern.matches(regex, "closed"));
		assertFalse(Pattern.matches(regex, "review_requested"));
		assertFalse(Pattern.matches(regex, "review_request_removed"));
		assertFalse(Pattern.matches(regex, "enqueued"));
		assertFalse(Pattern.matches(regex, "dequeued"));
		
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher("opened"); 
		assertTrue(m.matches());
		assertEquals(1, m.groupCount());
		assertEquals("opened", m.group(0));
		assertEquals(null, m.group(1));
	}
	
	@Test
	public void testMatcherWithPositiveRegex() {
		final String regex = "^(.*(closed|review_requested|review_request_removed|enqueued|dequeued)).*$";
		assertTrue(Pattern.matches(regex, "closed"));
		assertTrue(Pattern.matches(regex, "review_requested"));
		assertTrue(Pattern.matches(regex, "review_request_removed"));
		assertTrue(Pattern.matches(regex, "enqueued"));
		assertTrue(Pattern.matches(regex, "dequeued"));
		
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher("closed"); 
		assertTrue(m.matches());
		assertEquals(2, m.groupCount());
		assertEquals("closed", m.group(0));
		assertEquals("closed", m.group(1));
		assertEquals("closed", m.group(2));
	}

}
