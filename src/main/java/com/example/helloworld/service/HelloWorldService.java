package com.example.helloworld.service;

import com.example.helloworld.HelloWorldConfiguration;
import com.example.helloworld.resources.HelloWorldResource;
import com.example.helloworld.resources.VectorTileServiceResource;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.client.HttpClientBuilder;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.cache.CachingHttpClient;

public class HelloWorldService extends Service<HelloWorldConfiguration> {
    public static void main(String[] args) throws Exception {
        new HelloWorldService().run(args);
    }

    @Override
    public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
        bootstrap.setName("hello-world");
    }

    @Override
    public void run(HelloWorldConfiguration configuration,
                    Environment environment) {
        environment.addResource(new HelloWorldResource(configuration.getTemplate(), configuration.getDefaultName()));

        // System.setProperty("java.awt.headless", "true");

        // TODO user-agent
        final HttpClient httpClient = new CachingHttpClient(new HttpClientBuilder()
                .using(configuration.getHttpClientConfiguration()).build());

        environment.addResource(new VectorTileServiceResource(httpClient, configuration.getDefaultSource()));
    }
}