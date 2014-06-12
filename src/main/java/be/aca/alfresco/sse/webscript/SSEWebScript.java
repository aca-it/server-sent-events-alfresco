package be.aca.alfresco.sse.webscript;

import be.aca.alfresco.sse.config.SSEConfig;
import be.aca.alfresco.sse.domain.ClientKey;
import be.aca.alfresco.sse.service.ClientEventService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.WrappingWebScriptResponse;
import org.springframework.extensions.webscripts.servlet.WebScriptServletResponse;

import java.io.IOException;
import java.io.Writer;

public class SSEWebScript extends AbstractWebScript{

	private static final Log LOGGER = LogFactory.getLog(SSEWebScript.class);

	private ClientEventService clientEventService;
	private SSEConfig sseConfig;

	@Override
	public void execute(WebScriptRequest request, WebScriptResponse response) throws IOException {
		WebScriptServletResponse webScriptServletResponse = ((WebScriptServletResponse) ((WrappingWebScriptResponse) response).getNext());

		initHeader(webScriptServletResponse);
		Writer writer = webScriptServletResponse.getWriter();
		String fullyAuthenticatedUser = AuthenticationUtil.getFullyAuthenticatedUser();

		ClientKey clientKey = new ClientKey(fullyAuthenticatedUser);
		clientEventService.registerClient(clientKey, writer);
		LOGGER.debug("Created SSE session for " + fullyAuthenticatedUser);

		try {
			Thread.sleep(seconsToMillis(sseConfig.getSessionLifetimeInSeconds()));
		} catch (InterruptedException e) {
			LOGGER.error("Interrupted SSE service");
		} finally {
			clientEventService.unregisterClient(clientKey);
			LOGGER.debug("Removed SSE session for " + fullyAuthenticatedUser);
		}

	}

	private void initHeader(WebScriptServletResponse webScriptServletResponse) {
		webScriptServletResponse.setHeader("Content-Type", "text/event-stream; charset=utf-8");
		webScriptServletResponse.setHeader("Cache-Control", "no-cache");
		webScriptServletResponse.setHeader("Connection", "keep-alive");
	}

	private long seconsToMillis(int seconds) {
		return seconds * 1000;
	}

	public void setClientEventService(ClientEventService clientEventService) {
		this.clientEventService = clientEventService;
	}

	public void setSseConfig(SSEConfig sseConfig) {
		this.sseConfig = sseConfig;
	}
}

