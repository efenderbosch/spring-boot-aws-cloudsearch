package net.fender.springboot.aws.cloudsearch.docs;

import java.io.IOException;

import net.fender.springboot.aws.cloudsearch.docs.DeleteDocument.DeleteDocumentSerializer;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@JsonSerialize(using = DeleteDocumentSerializer.class)
public class DeleteDocument extends Document {

	public DeleteDocument() {
		super();
	}

	public DeleteDocument(String id) {
		super(id);
	}

	public static class DeleteDocumentSerializer extends StdSerializer<DeleteDocument> {

		private static final String DELETE = "delete";

		public DeleteDocumentSerializer() {
			super(DeleteDocument.class);
		}

		@Override
		public void serialize(DeleteDocument doc, JsonGenerator jgen, SerializerProvider provider) throws IOException,
		JsonGenerationException {
			jgen.writeStartObject();
			jgen.writeStringField(TYPE, DELETE);
			jgen.writeStringField(ID, doc.getId());
			jgen.writeEndObject();
		}
	}

}
