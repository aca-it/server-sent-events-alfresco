package be.aca.alfresco.sse.domain;

import java.util.Date;

public final class ClientKey {

	private String username;
	private Date connectTime;

	public ClientKey(String username) {
		this.username = username;
		this.connectTime = new Date();
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Date getConnectTime() {
		return connectTime;
	}

	public void setConnectTime(Date connectTime) {
		this.connectTime = connectTime;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ClientKey clientKey = (ClientKey) o;

		if (connectTime != null ? !connectTime.equals(clientKey.connectTime) : clientKey.connectTime != null)
			return false;
		if (username != null ? !username.equals(clientKey.username) : clientKey.username != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = username != null ? username.hashCode() : 0;
		result = 31 * result + (connectTime != null ? connectTime.hashCode() : 0);
		return result;
	}
}
