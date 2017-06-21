package io.skypvp.uhc.jedis;

import java.util.Map;

import redis.clients.jedis.Response;

public class UHCLobbyResponse {

	private final String serverName;
	private final Response<Map<String, String>> responseData;
	private Map<String, String> data;

	public UHCLobbyResponse(String name, Response<Map<String, String>> response) {
		this.serverName = name;
		this.responseData = response;
		this.data = null;
	}

	public String getName() {
		return this.serverName;
	}

	public void setData(Map<String, String> data) {
		this.data = data;
	}

	public Map<String, String> getData() {
		return this.data;
	}

	public Response<Map<String, String>> getResponseData() {
		return this.responseData;
	}
}
