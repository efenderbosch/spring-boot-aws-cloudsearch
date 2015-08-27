package net.fender.springboot.aws.cloudsearch.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;

public class CloudSearchModule extends SimpleModule {

	private static final long serialVersionUID = 1L;

	public CloudSearchModule() {
		addSerializer(new ZonedDateTimeSerializer());
	}

}
