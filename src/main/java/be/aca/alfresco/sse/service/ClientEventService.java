package be.aca.alfresco.sse.service;

import be.aca.alfresco.sse.domain.ClientKey;
import be.aca.alfresco.sse.domain.Message;

import java.io.Writer;

public interface ClientEventService {
	
	void registerClient(ClientKey clientKey, Writer writer);

	void sendMessageToAll(Message message);

	void sendMessageToUser(String username, Message message);

	void sendMessageToGroup(String groupname, Message message);

	void unregisterClient(ClientKey clientKey);
}
