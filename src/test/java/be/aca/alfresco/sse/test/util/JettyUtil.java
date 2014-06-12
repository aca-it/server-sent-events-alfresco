package be.aca.alfresco.sse.test.util;

import java.io.IOException;
import java.util.Properties;

public enum JettyUtil {
	INSTANCE;

	private static final String PATH_TO_ALFRESCO_GLOBAL_PROPERTIES = "/alfresco-global.properties";

	private final String ALFRESCO_PORT_PROPERTY = "alfresco.port";
	private final String ALFRESCO_CONTEXT_PROPERTY = "alfresco.context";

	public String getContext() throws IOException {
		return getAlfrescoGlobalProperties().getProperty(ALFRESCO_CONTEXT_PROPERTY);
	}

	public String getPort() throws IOException {
		return getAlfrescoGlobalProperties().getProperty(ALFRESCO_PORT_PROPERTY);
	}

	private Properties getAlfrescoGlobalProperties() throws IOException {
		Properties alfrescoGlobalProperties = new Properties();
		alfrescoGlobalProperties.load(JettyUtil.class.getResourceAsStream(PATH_TO_ALFRESCO_GLOBAL_PROPERTIES));

		return alfrescoGlobalProperties;
	}
}