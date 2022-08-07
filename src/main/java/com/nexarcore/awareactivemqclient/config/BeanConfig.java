package com.nexarcore.awareactivemqclient.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@Component
public class BeanConfig {

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    public String getBrokerUrl() {
        return brokerUrl;
    }
}
