package net.fender.springboot.aws.cloudsearch.docs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import net.fender.springboot.aws.cloudsearch.docs.AddDocument.AddDocumentSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@JsonSerialize(using = AddDocumentSerializer.class)
public class AddDocument extends Document {

	private static final Logger log = LoggerFactory.getLogger(AddDocument.class);

	private Map<String, Object> fields = new HashMap<>();
	private Object pojo;

	public AddDocument() {
		super();
	}

	public AddDocument(String id) {
		super(id);
	}

	public static AddDocument withRandomId() {
		return new AddDocument(UUID.randomUUID().toString());
	}

	public AddDocument withField(String key, Object value) {
		fields.put(key, value);
		return this;
	}

	public void setPojo(Object pojo) {
		this.pojo = pojo;
	}

	@SuppressWarnings("hiding")
	public AddDocument withPojo(Object pojo) {
		this.pojo = pojo;
		return this;
	}

	public static class AddDocumentSerializer extends StdSerializer<AddDocument> {

		private static final ObjectMapper OBJECT_MAPPER = Jackson.getObjectMapper();

		private static final String FIELDS = "fields";
		private static final String ADD = "add";

		public AddDocumentSerializer() {
			super(AddDocument.class);
		}

		@Override
		public void serialize(AddDocument doc, JsonGenerator jgen, SerializerProvider provider) throws IOException,
		JsonGenerationException {
			jgen.writeStartObject();
			jgen.writeStringField(TYPE, ADD);
			jgen.writeStringField(ID, doc.getId());
			if (doc.pojo != null) {
				jgen.writeRaw(",\"fields\"");
				jgen.writeRawValue(OBJECT_MAPPER.writeValueAsString(doc.pojo));
			} else {
				jgen.writeObjectFieldStart(FIELDS);
				for (Entry<String, Object> field : doc.fields.entrySet()) {
					jgen.writeObjectField(field.getKey(), field.getValue());
				}
				jgen.writeEndObject();
			}
			jgen.writeEndObject();
		}
	}

}
