package me.travja.crave.common;

import me.travja.crave.common.filters.RestTemplateResponseErrorHandler;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TemplateConfiguration {
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.errorHandler(new RestTemplateResponseErrorHandler()).build();
    }
}

