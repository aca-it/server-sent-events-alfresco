package be.aca.alfresco.sse.test.util;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.LogConfig;
import com.jayway.restassured.config.RestAssuredConfig;

import java.io.IOException;

public class AuthenticationUtil {
	private static final String LOGIN_URL = "http://localhost:%s/%s/service/api/login?u={userName}&pw={password}";

	public static final String ADMIN_USERNAME = "admin";
	public static final String ADMIN_PASSWORD = "admin";

	public static String login(String username, String password) throws IOException {
		String url = String.format(LOGIN_URL, JettyUtil.INSTANCE.getPort(), JettyUtil.INSTANCE.getContext());

		RestAssured.config = RestAssuredConfig.config().logConfig(LogConfig.logConfig().enablePrettyPrinting(false));
		return RestAssured.get(url, username, password).path("ticket");
	}

	public static String loginAsAdmin() throws IOException {
		return login(ADMIN_USERNAME, ADMIN_PASSWORD);
	}
}
