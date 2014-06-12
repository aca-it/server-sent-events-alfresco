package be.aca.alfresco.sse.webscript;

import be.aca.alfresco.sse.test.util.AuthenticationUtil;
import be.aca.alfresco.sse.test.util.JettyUtil;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.atmosphere.wasync.*;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.awaitility.Awaitility.fieldIn;

public class SSEWebScriptIT {
	private static final String SSE_WEBSCRIPT_URL_FORMAT = "http://localhost:%s/%s/service/events?alf_ticket=%s";
	private static final String CMIS_URL_FORMAT = "http://localhost:%s/%s/cmisatom";
	private static final String DOCUMENT_NAME = "dummy";

	private Session session;
	private Document document;

	private String receivedMessage;

	@Test
	public void sseWebScriptReturnsMessageWhenBehaviourIsTriggered() throws IOException {
		setupSSEConnection();
		session = createSession(AuthenticationUtil.ADMIN_USERNAME, AuthenticationUtil.ADMIN_PASSWORD);

		Folder rootFolder = session.getRootFolder();
		document = rootFolder.createDocument(new HashMap<String, String>() {{
			put(PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value());
			put(PropertyIds.NAME, DOCUMENT_NAME);
		}}, null, VersioningState.MAJOR);

		await().until(fieldIn(this).ofType(String.class).andWithName("receivedMessage"), Matchers.notNullValue());

		Assert.assertEquals(DOCUMENT_NAME, receivedMessage);
	}

	@After
	public void clean() {
		if (document != null) {
			session.delete(document);
		}
	}

	private void setupSSEConnection() throws IOException {
		Client client = ClientFactory.getDefault().newClient();

		RequestBuilder request = client.newRequestBuilder()
				.method(Request.METHOD.GET)
				.uri(getSSEWebScriptURL())
				.transport(Request.TRANSPORT.SSE);

		Socket socket = client.create();
		socket.on(new Function<String>() {
			@Override
			public void on(String message) {
				receivedMessage = message;
			}

		}).open(request.build());
	}

	private Session createSession(String username, String password) throws IOException {
		SessionFactory factory = SessionFactoryImpl.newInstance();

		Map<String, String> parameter = new HashMap<String, String>();

		parameter.put(SessionParameter.USER, username);
		parameter.put(SessionParameter.PASSWORD, password);

		parameter.put(SessionParameter.ATOMPUB_URL, getCMISURL());
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
		parameter.put(SessionParameter.COMPRESSION, "true");

		return factory.getRepositories(parameter).get(0).createSession();
	}

	private String getSSEWebScriptURL() throws IOException {
		String ticket = AuthenticationUtil.loginAsAdmin();

		return String.format(SSE_WEBSCRIPT_URL_FORMAT, JettyUtil.INSTANCE.getPort(), JettyUtil.INSTANCE.getContext(), ticket);
	}

	private String getCMISURL() throws IOException {
		return String.format(CMIS_URL_FORMAT, JettyUtil.INSTANCE.getPort(), JettyUtil.INSTANCE.getContext());
	}
}
