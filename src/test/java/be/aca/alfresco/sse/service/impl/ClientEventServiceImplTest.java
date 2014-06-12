package be.aca.alfresco.sse.service.impl;

import be.aca.alfresco.sse.config.SSEConfig;
import be.aca.alfresco.sse.domain.ClientKey;
import be.aca.alfresco.sse.domain.Message;
import be.aca.alfresco.sse.service.ClientEventService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.test.junitrules.AlfrescoPerson;
import org.alfresco.util.test.junitrules.ApplicationContextInit;
import org.alfresco.util.test.junitrules.RunAsFullyAuthenticatedRule;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.Writer;

@RunWith(MockitoJUnitRunner.class)
public class ClientEventServiceImplTest {
	@ClassRule public static ApplicationContextInit APP_CONTEXT_INIT = new ApplicationContextInit();

	@Rule public RunAsFullyAuthenticatedRule runAsFullyAuthenticatedRule = new RunAsFullyAuthenticatedRule();
	@Rule public AlfrescoPerson john = new AlfrescoPerson(APP_CONTEXT_INIT, USERNAME_JOHN);
	@Rule public AlfrescoPerson jane = new AlfrescoPerson(APP_CONTEXT_INIT, USERNAME_JANE);

	private static final String GROUP_SSE_NAME = "SSE";
	private static final String USERNAME_ADMIN = "admin";
	private static final String USERNAME_JOHN = "john.doe";
	private static final String USERNAME_JANE = "jane.doe";

	@Mock private Writer writerJohn;
	@Mock private Writer writerJane;
	private final ClientKey clientKeyJohn = new ClientKey(USERNAME_JOHN);
	private final ClientKey clientKeyJane = new ClientKey(USERNAME_JANE);
	private Message message = new Message("data");

	private ClientEventService clientEventService;
	private AuthorityService authorityService;
	private SSEConfig sseConfig;

	@Before
	public void init() {
		clientEventService = APP_CONTEXT_INIT.getApplicationContext().getBean(ClientEventService.class);
		authorityService = (AuthorityService) APP_CONTEXT_INIT.getApplicationContext().getBean("AuthorityService");
		sseConfig = APP_CONTEXT_INIT.getApplicationContext().getBean(SSEConfig.class);

		clientEventService.registerClient(clientKeyJohn, writerJohn);
		clientEventService.registerClient(clientKeyJane, writerJane);

		addUsersToSSEGroup();
	}

	@Test
	@RunAsFullyAuthenticatedRule.RunAsUser(userName = USERNAME_ADMIN)
	public void sendMessageToSpecificRegisteredClientWritesToOnlyThatClient() throws IOException {
		clientEventService.sendMessageToUser(USERNAME_JOHN, message);

		Mockito.verify(writerJohn).write(message.toString());
		Mockito.verifyZeroInteractions(writerJane);
	}

	@Test
	@RunAsFullyAuthenticatedRule.RunAsUser(userName = USERNAME_ADMIN)
	public void sendMessageToAllRegisteredClientsWritesToAllClients() throws IOException {
		clientEventService.sendMessageToAll(message);

		Mockito.verify(writerJohn).write(message.toString());
		Mockito.verify(writerJane).write(message.toString());
	}

	@Test
	@RunAsFullyAuthenticatedRule.RunAsUser(userName = USERNAME_ADMIN)
	public void sendMessageToGroupSendsToAllMembersEvenIfNotRegistered() throws IOException {
		clientEventService.sendMessageToGroup(PermissionService.GROUP_PREFIX + GROUP_SSE_NAME, message);

		Mockito.verify(writerJohn).write(message.toString());
		Mockito.verify(writerJane).write(message.toString());
	}

	@Test
	@RunAsFullyAuthenticatedRule.RunAsUser(userName = USERNAME_ADMIN)
	public void sendMessageRegisteredClientQueuesMesageWhenWritingFails() throws IOException {
		Mockito.doThrow(IOException.class).when(writerJohn).write(message.toString());
		clientEventService.sendMessageToGroup(PermissionService.GROUP_PREFIX + GROUP_SSE_NAME, message);

		Mockito.verify(writerJohn).write(message.toString());
		Mockito.verify(writerJane).write(message.toString());

		Mockito.doNothing().when(writerJohn).write(Mockito.anyString());
		clientEventService.registerClient(clientKeyJohn, writerJohn);

		Mockito.verify(writerJohn, Mockito.times(2)).write(message.toString());
		Mockito.verify(writerJane).write(message.toString());
	}

	@Test
	@RunAsFullyAuthenticatedRule.RunAsUser(userName = USERNAME_ADMIN)
	public void messageInQueueExpiresAfterConfiguredTimeAndIsThereforeNotSent() throws IOException, InterruptedException {
		Mockito.doThrow(IOException.class).when(writerJohn).write(message.toString());
		clientEventService.sendMessageToGroup(PermissionService.GROUP_PREFIX + GROUP_SSE_NAME, message);

		Mockito.verify(writerJohn).write(message.toString());
		Mockito.verify(writerJane).write(message.toString());

		waitForQueuedMessageToExpire();

		Mockito.doNothing().when(writerJohn).write(Mockito.anyString());
		clientEventService.registerClient(clientKeyJohn, writerJohn);

		Mockito.verify(writerJohn).write(message.toString());
		Mockito.verify(writerJane).write(message.toString());
	}

	@After
	public void clean() {
		AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
			@Override
			public Object doWork() throws Exception {
				authorityService.deleteAuthority(PermissionService.GROUP_PREFIX + GROUP_SSE_NAME);

				return null;
			}
		});
	}

	private void addUsersToSSEGroup() {
		AuthenticationUtil.runAsSystem(new AuthenticationUtil.RunAsWork<Object>() {
			@Override
			public Object doWork() throws Exception {
				String sseGroupName = authorityService.createAuthority(AuthorityType.GROUP, GROUP_SSE_NAME);
				authorityService.addAuthority(sseGroupName, USERNAME_JOHN);
				authorityService.addAuthority(sseGroupName, USERNAME_JANE);

				return null;
			}
		});
	}

	private void waitForQueuedMessageToExpire() throws InterruptedException {
		Thread.sleep((sseConfig.getQueuedMessageLifetimeInSeconds() + 2) * 1000);
	}
}
