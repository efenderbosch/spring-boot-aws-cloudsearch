package net.fender.springboot.aws.cloudsearch;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class TestBootstrap {

	public static void main(String[] args) throws Exception {
		new SpringApplicationBuilder().sources(TestBootstrap.class).run(args);
	}
}