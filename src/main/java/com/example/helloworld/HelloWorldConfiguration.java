package com.example.helloworld;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.client.HttpClientConfiguration;
import com.yammer.dropwizard.config.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class HelloWorldConfiguration extends Configuration {
    @NotEmpty
    @JsonProperty
    private String template;

    @NotEmpty
    @JsonProperty
    private String defaultName = "Stranger";

    @Valid
    @NotNull
    @JsonProperty
    private HttpClientConfiguration httpClient = new HttpClientConfiguration();

    @NotEmpty
    @JsonProperty
    private String defaultSource;

    public String getTemplate() {
        return template;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public HttpClientConfiguration getHttpClientConfiguration() {
        return httpClient;
    }

    public String getDefaultSource() {
        return defaultSource;
    }
}