package com.example.es1.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info().title("nfElasticsearch API").version("1.0.0").description("文档搜索系统接口文档").license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}
