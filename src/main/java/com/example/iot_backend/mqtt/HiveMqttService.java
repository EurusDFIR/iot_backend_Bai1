package com.example.iot_backend.mqtt;

import com.example.iot_backend.config.MqttConfig;
import com.example.iot_backend.model.Command;
import com.example.iot_backend.model.Device;
import com.example.iot_backend.service.DeviceMonitoringService;
import com.example.iot_backend.service.TelemetryService;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class HiveMqttService {
    private static final Logger log = LoggerFactory.getLogger(HiveMqttService.class);
    private final MqttConfig cfg;
    private final TelemetryService telemetryService;
    private final DeviceMonitoringService monitoringService;
    private Mqtt3AsyncClient client;

    public HiveMqttService(MqttConfig cfg, TelemetryService telemetryService,
            DeviceMonitoringService monitoringService) {
        this.cfg = cfg;
        this.telemetryService = telemetryService;
        this.monitoringService = monitoringService;
    }

    @Bean
    public ApplicationRunner mqttRunner() {
        return args -> {
            connectAndSubscribe();
        };
    }

    private void connectAndSubscribe() {
        client = MqttClient.builder()
                .useMqttVersion3()
                .identifier(cfg.getClientId())
                .serverHost(cfg.getHost())
                .serverPort(cfg.getPort())
                .buildAsync();

        client.connectWith()
                .keepAlive(cfg.getKeepAlive())
                .send()
                .whenComplete((ack, ex) -> {
                    if (ex != null) {
                        log.error("MQTT connect failed: {}", ex.getMessage(), ex);
                        return;
                    }
                    log.info("MQTT connected as clientId={}", cfg.getClientId());

                    // Subscribe to demo topic (Lab 2)
                    client.subscribeWith()
                            .topicFilter(cfg.getTopicTemp())
                            .qos(MqttQos.fromCode(cfg.getQos()))
                            .callback(publish -> {
                                String payload = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
                                log.info("[Demo] Received topic='{}' payload={}", publish.getTopic(), payload);
                                processMessage(publish.getTopic().toString(), payload);
                            })
                            .send()
                            .whenComplete((subAck, subEx) -> {
                                if (subEx != null) {
                                    log.error("MQTT subscribe failed: {}", subEx.getMessage(), subEx);
                                } else {
                                    log.info("Subscribed to topic {}", cfg.getTopicTemp());
                                }
                            });

                    // Subscribe to telemetry topics (Lab 4)
                    client.subscribeWith()
                            .topicFilter("iot/device/+/telemetry")
                            .qos(MqttQos.fromCode(cfg.getQos()))
                            .callback(publish -> {
                                String topic = publish.getTopic().toString();
                                String payload = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
                                log.info("[Telemetry] Received topic='{}' payload={}", topic, payload);
                                processTelemetryMessage(topic, payload);
                            })
                            .send()
                            .whenComplete((subAck, subEx) -> {
                                if (subEx != null) {
                                    log.error("MQTT telemetry subscribe failed: {}", subEx.getMessage(), subEx);
                                } else {
                                    log.info("Subscribed to telemetry topic: iot/device/+/telemetry");
                                }
                            });

                    // Subscribe to heartbeat topics (Lab 6)
                    client.subscribeWith()
                            .topicFilter("iot/device/+/heartbeat")
                            .qos(MqttQos.fromCode(cfg.getQos()))
                            .callback(publish -> {
                                String topic = publish.getTopic().toString();
                                String payload = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
                                log.info("[Heartbeat] Received topic='{}' payload={}", topic, payload);
                                processHeartbeatMessage(topic, payload);
                            })
                            .send()
                            .whenComplete((subAck, subEx) -> {
                                if (subEx != null) {
                                    log.error("MQTT heartbeat subscribe failed: {}", subEx.getMessage(), subEx);
                                } else {
                                    log.info("Subscribed to heartbeat topic: iot/device/+/heartbeat");
                                }
                            });

                    // Subscribe to status topics (Lab 6)
                    client.subscribeWith()
                            .topicFilter("iot/device/+/status")
                            .qos(MqttQos.fromCode(cfg.getQos()))
                            .callback(publish -> {
                                String topic = publish.getTopic().toString();
                                String payload = new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);
                                log.info("[Status] Received topic='{}' payload={}", topic, payload);
                                processStatusMessage(topic, payload);
                            })
                            .send()
                            .whenComplete((subAck, subEx) -> {
                                if (subEx != null) {
                                    log.error("MQTT status subscribe failed: {}", subEx.getMessage(), subEx);
                                } else {
                                    log.info("Subscribed to status topic: iot/device/+/status");
                                }
                            });
                });
    }

    private void processTelemetryMessage(String topic, String payload) {
        try {
            // Extract deviceId from topic: iot/device/{id}/telemetry
            String[] parts = topic.split("/");
            if (parts.length >= 3) {
                Long deviceId = Long.parseLong(parts[2]);

                // Save telemetry data
                telemetryService.saveTelemetry(deviceId, payload);

                // Mark device as online và update heartbeat
                Map<String, Object> metadata = parseDeviceMetadata(payload);
                monitoringService.markDeviceOnline(deviceId, metadata);

                // Dynamic subscribe to device if not already subscribed
                subscribeToDevice(deviceId);

            } else {
                log.warn("Invalid telemetry topic format: {}", topic);
            }
        } catch (Exception e) {
            log.error("Failed to process telemetry message from topic {}: {}", topic, e.getMessage());
        }
    }

    private void processMessage(String topic, String payload) {
        // Xử lý message nhận được
        log.info("Processing message from topic: {} with payload: {}", topic, payload);

        // Ví dụ: Parse JSON và kiểm tra nhiệt độ
        try {
            if (payload.contains("temp")) {
                // Simple JSON parsing (có thể dùng Jackson ObjectMapper cho phức tạp hơn)
                String tempStr = payload.replaceAll("[^0-9.-]", "");
                if (!tempStr.isEmpty()) {
                    double temp = Double.parseDouble(tempStr);
                    if (temp > 30.0) {
                        log.warn("⚠️ HIGH TEMPERATURE ALERT: {}°C", temp);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not parse temperature from payload: {}", payload);
        }
    }

    @PreDestroy
    public void shutdown() {
        if (client != null) {
            client.disconnect();
            log.info("MQTT disconnected");
        }
    }

    /**
     * Lab 5: Publish command to device
     */
    public boolean publishCommand(Device device, Command command) {
        if (client == null || !client.getState().isConnected()) {
            log.error("MQTT client not connected, cannot send command");
            return false;
        }

        try {
            // Topic pattern: iot/device/{id}/command
            String topic = String.format("iot/device/%d/command", device.getId());

            // Create command payload JSON
            String payload = String.format(
                    "{\"commandId\": %d, \"type\": \"%s\", \"data\": %s, \"timestamp\": \"%s\"}",
                    command.getId(),
                    command.getCommandType(),
                    command.getCommandData() != null ? command.getCommandData() : "{}",
                    command.getCreatedAt().toString());

            // Publish command
            client.publishWith()
                    .topic(topic)
                    .payload(payload.getBytes(StandardCharsets.UTF_8))
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .send()
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish command to topic {}: {}", topic, ex.getMessage());
                        } else {
                            log.info("📤 Command published to topic: {} payload: {}", topic, payload);
                        }
                    });

            return true;
        } catch (Exception e) {
            log.error("Error publishing command: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Lab 5: Publish simple message to any topic
     */
    public boolean publishMessage(String topic, String message) {
        if (client == null || !client.getState().isConnected()) {
            log.error("MQTT client not connected, cannot publish message");
            return false;
        }

        try {
            client.publishWith()
                    .topic(topic)
                    .payload(message.getBytes(StandardCharsets.UTF_8))
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .send()
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish to topic {}: {}", topic, ex.getMessage());
                        } else {
                            log.info("📤 Published to topic: {} message: {}", topic, message);
                        }
                    });

            return true;
        } catch (Exception e) {
            log.error("Error publishing message: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Lab 6: Process heartbeat message from device
     */
    private void processHeartbeatMessage(String topic, String payload) {
        try {
            // Extract deviceId from topic: iot/device/{id}/heartbeat
            String[] parts = topic.split("/");
            if (parts.length >= 3) {
                Long deviceId = Long.parseLong(parts[2]);

                // Parse metadata từ payload
                Map<String, Object> metadata = parseDeviceMetadata(payload);

                // Update heartbeat
                monitoringService.updateHeartbeat(deviceId, metadata);

                log.debug("💓 Heartbeat updated for device {}", deviceId);
            } else {
                log.warn("Invalid heartbeat topic format: {}", topic);
            }
        } catch (Exception e) {
            log.error("Failed to process heartbeat message from topic {}: {}", topic, e.getMessage());
        }
    }

    /**
     * Lab 6: Process status message from device
     */
    private void processStatusMessage(String topic, String payload) {
        try {
            // Extract deviceId from topic: iot/device/{id}/status
            String[] parts = topic.split("/");
            if (parts.length >= 3) {
                Long deviceId = Long.parseLong(parts[2]);

                // Parse status từ payload
                Map<String, Object> metadata = parseDeviceMetadata(payload);
                String status = payload.toLowerCase().trim();

                if (status.contains("online") || status.contains("connected")) {
                    monitoringService.markDeviceOnline(deviceId, metadata);
                    log.info("🟢 Device {} reported ONLINE", deviceId);
                } else if (status.contains("offline") || status.contains("disconnected")) {
                    monitoringService.markDeviceOffline(deviceId);
                    log.info("🔴 Device {} reported OFFLINE", deviceId);
                } else {
                    // Treat any status message as heartbeat
                    monitoringService.updateHeartbeat(deviceId, metadata);
                }
            } else {
                log.warn("Invalid status topic format: {}", topic);
            }
        } catch (Exception e) {
            log.error("Failed to process status message from topic {}: {}", topic, e.getMessage());
        }
    }

    /**
     * Lab 6: Parse device metadata from JSON payload
     */
    private Map<String, Object> parseDeviceMetadata(String payload) {
        Map<String, Object> metadata = new HashMap<>();

        try {
            // Simple JSON parsing (có thể thay bằng ObjectMapper cho phức tạp hơn)
            if (payload.contains("firmware")) {
                String firmware = extractJsonValue(payload, "firmware");
                if (firmware != null)
                    metadata.put("firmware", firmware);
            }

            if (payload.contains("ip")) {
                String ip = extractJsonValue(payload, "ip");
                if (ip != null)
                    metadata.put("ip", ip);
            }

            if (payload.contains("signal")) {
                String signal = extractJsonValue(payload, "signal");
                if (signal != null) {
                    try {
                        metadata.put("signal", Integer.parseInt(signal));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            if (payload.contains("battery")) {
                String battery = extractJsonValue(payload, "battery");
                if (battery != null) {
                    try {
                        metadata.put("battery", Integer.parseInt(battery));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

        } catch (Exception e) {
            log.debug("Could not parse metadata from payload: {}", payload);
        }

        return metadata;
    }

    /**
     * Extract value from simple JSON
     */
    private String extractJsonValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*\"?([^,}\"]+)\"?";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1).trim();
            }
        } catch (Exception e) {
            log.debug("Could not extract {} from JSON: {}", key, json);
        }
        return null;
    }

    /**
     * Lab 6: Subscribe dynamically to new device topics
     */
    public void subscribeToDevice(Long deviceId) {
        if (client == null || !client.getState().isConnected()) {
            log.warn("Cannot subscribe to device {}: MQTT client not connected", deviceId);
            return;
        }

        if (!monitoringService.shouldSubscribeToDevice(deviceId)) {
            log.debug("Already subscribed to device {}", deviceId);
            return;
        }

        // Subscribe to all device topics: telemetry, heartbeat, status
        String[] topicPatterns = {
                String.format("iot/device/%d/telemetry", deviceId),
                String.format("iot/device/%d/heartbeat", deviceId),
                String.format("iot/device/%d/status", deviceId)
        };

        for (String topic : topicPatterns) {
            client.subscribeWith()
                    .topicFilter(topic)
                    .qos(MqttQos.fromCode(cfg.getQos()))
                    .send()
                    .whenComplete((subAck, subEx) -> {
                        if (subEx != null) {
                            log.error("Failed to subscribe to topic {}: {}", topic, subEx.getMessage());
                        } else {
                            log.info("🔔 Dynamically subscribed to topic: {}", topic);
                            monitoringService.markTopicSubscribed(topic);
                        }
                    });
        }
    }
}