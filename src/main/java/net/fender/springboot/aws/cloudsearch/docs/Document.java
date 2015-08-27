package net.fender.springboot.aws.cloudsearch.docs;

import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.core.JsonProcessingException;

public abstract class Document {

	protected static final String TYPE = "type";
	protected static final String ID = "id";

	private String id;

	protected Document() {
		//
	}

	protected Document(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String toJsonString() {
		try {
			return Jackson.getObjectMapper().writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

}
