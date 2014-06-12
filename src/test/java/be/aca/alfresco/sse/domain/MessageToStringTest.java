package be.aca.alfresco.sse.domain;

import org.junit.Assert;
import org.junit.Test;

public class MessageToStringTest {

	@Test
	public void messageWithDataToStringShouldReturnCorrectRepresentation() {
		Assert.assertEquals("data:testData\n\n", new Message("testData").toString());
	}

	@Test
	public void messageWithDataAndEventToStringShouldReturnCorrectRepresentation() {
		Assert.assertEquals("event:event\ndata:testData\n\n", new Message("event", "testData").toString());
	}

	@Test
	public void messageWithDataEventAndIdToStringShouldReturnCorrectRepresentation() {
		Message message = new Message("testData");
		message.setId("1234");

		Assert.assertEquals("id:1234\ndata:testData\n\n", message.toString());
	}
}
