package be.aca.alfresco.sse.config;

import java.util.Properties;

public class SSEConfig {

	private static final String QUEUED_MESSAGE_LIFETIME_SECONDS = "sse.queued.message.lifetime.seconds";
	private static final String SESSION_LIFETIME_SECONDS = "sse.session.lifetime.seconds";

	private Properties alfrescoGlobalProperties;

	public int getQueuedMessageLifetimeInSeconds() {
		return Integer.valueOf(alfrescoGlobalProperties.getProperty(QUEUED_MESSAGE_LIFETIME_SECONDS));
	}

	public int getSessionLifetimeInSeconds() {
		return Integer.valueOf(alfrescoGlobalProperties.getProperty(SESSION_LIFETIME_SECONDS));
	}

	public void setAlfrescoGlobalProperties(Properties alfrescoGlobalProperties) {
		this.alfrescoGlobalProperties = alfrescoGlobalProperties;
	}

}
