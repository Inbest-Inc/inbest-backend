package com.inbest.backend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/*
    WebClient's primary purpose is to configure and provide a reusable WebClient bean
    for making HTTP requests to the Yahoo Finance API. After defining the configuration bean in this class,
    it is injected into StockService to make asynchronous HTTP requests. RestTemplate is used for the same purpose
    but is synchronous. WebClient is asynchronous.
*/
@Configuration
public class WebClientConfig
{
    // Reusable WebClient bean for making HTTP requests
    @Bean
    public WebClient webClient(WebClient.Builder builder)
    {
        return builder.baseUrl("https://query1.finance.yahoo.com").build();
    }
}
