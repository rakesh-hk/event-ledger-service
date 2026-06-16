package com.example.eventgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.eventgateway.client")
public class EventGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventGatewayApplication.class, args);
    }
}
