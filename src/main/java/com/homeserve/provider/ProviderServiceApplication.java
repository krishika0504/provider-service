package com.homeserve.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class ProviderServiceApplication {

    public static void main(String[] args) {
        com.homeserve.provider.config.EnvLoader.load();
        SpringApplication.run(ProviderServiceApplication.class, args);
    }
}
