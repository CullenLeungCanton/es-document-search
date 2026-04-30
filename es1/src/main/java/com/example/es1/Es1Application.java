package com.example.es1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EntityScan(basePackages = "com.example.es1.entity")
@EnableJpaRepositories(
        basePackages = "com.example.es1.repository.jpa",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com.example.es1.repository.es.*"
        )
)
@EnableElasticsearchRepositories(
        basePackages = "com.example.es1.repository.es"
)
public class Es1Application {

    public static void main(String[] args) {
        SpringApplication.run(Es1Application.class, args);
    }

}
