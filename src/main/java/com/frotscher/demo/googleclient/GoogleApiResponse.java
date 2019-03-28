package com.frotscher.demo.googleclient;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleApiResponse<T> {

	public static final String STATUS_OK = "OK";
	public static final String STATUS_ZERO_RESULTS = "ZERO_RESULTS";
	public static final String STATUS_OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT";
	public static final String STATUS_REQUEST_DENIED = "REQUEST_DENIED";
	public static final String STATUS_INVALID_REQUEST = "INVALID_REQUEST";
	public static final String STATUS_UNKNOWN_ERROR = "UNKNOWN_ERROR";

	private List<T> results;
	private String status;

	@JsonProperty("error_message")
	private String errorMessage;

	public List<T> getResults() {
		return results;
	}

	public void setResults(List<T> results) {
		this.results = results;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
