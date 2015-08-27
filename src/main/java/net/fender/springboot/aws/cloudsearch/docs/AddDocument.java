package net.fender.springboot.aws.cloudsearch.docs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import net.fender.springboot.aws.cloudsearch.docs.AddDocument.AddDocumentSerializer;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@JsonSerialize(using = AddDocumentSerializer.class)
public class AddDocument extends Document {

	private Map<String, Object> fields = new HashMap<>();

	public AddDocument() {
		super();
	}

	public AddDocument(String id) {
		super(id);
	}

	public static AddDocument withRandomId() {
		return new AddDocument(UUID.randomUUID().toString());
	}

	public void put(String key, Object value) {
		fields.put(key, value);
	}

	public static class AddDocumentSerializer extends StdSerializer<AddDocument> {

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
			jgen.writeObjectFieldStart(FIELDS);
			for (Entry<String, Object> field : doc.fields.entrySet()) {
				jgen.writeObjectField(field.getKey(), field.getValue());
			}
			jgen.writeEndObject();
			jgen.writeEndObject();
		}
	}

}
