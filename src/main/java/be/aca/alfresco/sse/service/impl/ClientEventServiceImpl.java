package be.aca.alfresco.sse.service.impl;

import be.aca.alfresco.sse.config.SSEConfig;
import be.aca.alfresco.sse.domain.ClientKey;
import be.aca.alfresco.sse.domain.Message;
import be.aca.alfresco.sse.service.ClientEventService;
import com.google.common.collect.MapMaker;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class ClientEventServiceImpl implements ClientEventService, InitializingBean {

	private AuthorityService authorityService;
	private SSEConfig sseConfig;

	private ConcurrentMap<String, List<Message>> messageQueue;

	private Map<ClientKey, Writer> writers = new HashMap<ClientKey, Writer>();

	@Override
	public void registerClient(ClientKey clientKey, Writer writer) {
		writers.put(clientKey, writer);
		
		sendQueuedMessages(clientKey);
	}

	@Override
	public void sendMessageToAll(Message message) {
		for (Map.Entry<ClientKey, Writer> writerEntry : writers.entrySet()) {
			writeMessageToStream(writerEntry, message);
		}
	}

	@Override
	public void sendMessageToUser(String username, final Message message) {
		for (Map.Entry<ClientKey, Writer> writerEntry : writers.entrySet()) {
			if (writerEntry.getKey().getUsername().equals(username)) {
				if (!writeMessageToStream(writerEntry, message)) {
					queueMessage(username, message);
					return;
				}
			}
		}
	}

	@Override
	public void sendMessageToGroup(String groupname, final Message message) {
		Set<String> users = authorityService.getContainedAuthorities(AuthorityType.USER, groupname, false);

		for (String user : users) {
			sendMessageToUser(user, message);
		}
	}

	@Override
	public void unregisterClient(ClientKey clientKey) {
		writers.remove(clientKey);
	}

	private void sendQueuedMessages(ClientKey clientKey) {
		for (Map.Entry<String, List<Message>> messageQueueEntry : messageQueue.entrySet()) {
			if (messageQueueEntry.getKey().equals(clientKey.getUsername())) {
				List<Message> messages = Arrays.asList(messageQueueEntry.getValue().toArray(new Message[] {}));

				for (Message message : messages) {
					sendMessageToUser(clientKey.getUsername(), message);
					messageQueue.remove(clientKey.getUsername());
				}
			}
		}
	}

	private void queueMessage(String username, final Message message) {
		if (messageQueue.containsKey(username)) {
			messageQueue.get(username).add(message);
		} else {
			messageQueue.put(username, new ArrayList<Message>() {{ this.add(message); }});
		}
	}

	private boolean writeMessageToStream(Map.Entry<ClientKey, Writer> writerEntry, Message message) {
		try {
			writerEntry.getValue().write(message.toString());
			writerEntry.getValue().flush();

			return true;
		} catch (IOException e) {
			unregisterClient(writerEntry.getKey());

			return false;
		}
	}

	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	public void setSseConfig(SSEConfig sseConfig) {
		this.sseConfig = sseConfig;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		messageQueue = new MapMaker().expiration(sseConfig.getQueuedMessageLifetimeInSeconds(), TimeUnit.SECONDS).makeMap();
	}
}
