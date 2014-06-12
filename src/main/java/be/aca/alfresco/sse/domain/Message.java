package be.aca.alfresco.sse.domain;

import org.apache.commons.lang.StringUtils;

public final class Message {

	private final String SEPARATOR = ":";
	private final String NEW_LINE = "\n";
	private final String ID = "id";
	private final String DATA = "data";
	private final String EVENT = "event";

	private String id;
	private String retry;
	private String data;
	private String event;

	public Message(String data) {
		this.data = data;
	}

	public Message(String event, String data) {
		this.data = data;
		this.event = event;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setRetry(String retry) {
		this.retry = retry;
	}

	@Override
	public String toString() {
		StringBuffer output = new StringBuffer();

		if (StringUtils.isNotBlank(id)) {
			output.append(ID + SEPARATOR);
			output.append(id);
			output.append(NEW_LINE);
		}

		if (StringUtils.isNotBlank(event)) {
			output.append(EVENT + SEPARATOR);
			output.append(event);
			output.append(NEW_LINE);
		}

		if (StringUtils.isNotBlank(data)) {
			output.append(DATA + SEPARATOR);
			output.append(data);
			output.append(NEW_LINE);
		}

		output.append(NEW_LINE);

		return output.toString();
	}
}
