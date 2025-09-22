package com.example.iot_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttConfig {

    @Value("${mqtt.host}")
    private String host;

    @Value("${mqtt.port}")
    private int port;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    @Value("${mqtt.clientId}")
    private String clientId;

    @Value("${mqtt.topicTemp}")
    private String topicTemp;

    @Value("${mqtt.qos:1}")
    private int qos;

    @Value("${mqtt.keepAlive:30}")
    private int keepAlive;

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getClientId() {
        return clientId;
    }

    public String getTopicTemp() {
        return topicTemp;
    }

    public int getQos() {
        return qos;
    }

    public int getKeepAlive() {
        return keepAlive;
    }
}