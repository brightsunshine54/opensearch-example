package com.filantrop.opensearchExample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication(exclude = ElasticsearchDataAutoConfiguration.class)
@EnableElasticsearchRepositories
public class Main {

	public static void main(String[] args) {
		SpringApplication.run(Main.class, args);
	}

}
