package net.fender.springboot.aws.cloudsearch;

import java.io.IOException;

import net.fender.springboot.aws.cloudsearch.Facet.FacetSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.base.Joiner;

@JsonSerialize(using = FacetSerializer.class)
public class Facet {

	private static final Logger log = LoggerFactory.getLogger(Facet.class);

	private static final ObjectMapper OBJECT_MAPPER = Jackson.getObjectMapper();

	public enum Sort {
		count, bucket;
	}

	private final String fieldName;
	private String buckets;
	private Integer size;
	private Sort sort;

	public Facet(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	public String getBuckets() {
		return buckets;
	}

	public void setBuckets(String buckets) {
		this.buckets = buckets;
	}

	@SuppressWarnings("hiding")
	public Facet withBuckets(String buckets) {
		this.buckets = buckets;
		return this;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	@SuppressWarnings("hiding")
	public Facet withSize(Integer size) {
		this.size = size;
		return this;
	}

	public Sort getSort() {
		return sort;
	}

	public void setSort(Sort sort) {
		this.sort = sort;
	}

	@SuppressWarnings("hiding")
	public Facet withSort(Sort sort) {
		this.sort = sort;
		return this;
	}

	@Override
	public String toString() {
		try {
			String json = OBJECT_MAPPER.writeValueAsString(this);
			return json;
		} catch (JsonProcessingException e) {
			log.error("fail", e);
		}
		return null;
	}

	public static String toJson(Facet... facets) {
		if (facets == null) {
			return null;
		}
		if (facets.length == 0) {
			// TODO or "{}"?
			return "";
		}
		return "{" + Joiner.on(",").join(facets) + "}";
	}

	public static String toJson(String... facetNames) {
		if (facetNames == null) {
			return null;
		}
		if (facetNames.length == 0) {
			return "";
		}
		Facet[] facets = new Facet[facetNames.length];
		for (int i = 0; i < facetNames.length; i++) {
			facets[i] = new Facet(facetNames[i]);
		}
		return toJson(facets);
	}

	public static class FacetSerializer extends StdSerializer<Facet> {

		public FacetSerializer() {
			super(Facet.class);
		}

		@Override
		public void serialize(Facet facet, JsonGenerator jgen, SerializerProvider provider) throws IOException,
		JsonGenerationException {
			jgen.writeRaw("\"" + facet.fieldName + "\":{");
			// jgen.writeStartObject();
			if (facet.buckets != null) {
				jgen.writeStringField("buckets", facet.buckets);
			}
			if (facet.size != null) {
				jgen.writeNumberField("size", facet.size);
			}
			if (facet.sort != null) {
				jgen.writeStringField("sort", facet.sort.name());
			}
			jgen.writeRaw("}");
		}

	}

}
