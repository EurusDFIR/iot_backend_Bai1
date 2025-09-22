package com.example.iot_backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "commands")
public class Command {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "command_type", nullable = false)
    private String commandType; // "LED_ON", "LED_OFF", "SET_TEMP", etc.

    @Column(name = "command_data", columnDefinition = "TEXT")
    private String commandData; // JSON parameters

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CommandStatus status = CommandStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "executed_at")
    private Instant executedAt;

    @Column(name = "result")
    private String result; // Response from device

    // Constructors
    public Command() {
        this.createdAt = Instant.now();
    }

    public Command(Device device, String commandType, String commandData) {
        this();
        this.device = device;
        this.commandType = commandType;
        this.commandData = commandData;
    }

    // Enum for command status
    public enum CommandStatus {
        PENDING, // Created but not sent
        SENT, // Sent to device via MQTT
        EXECUTED, // Device confirmed execution
        FAILED, // Execution failed
        TIMEOUT // No response from device
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getCommandData() {
        return commandData;
    }

    public void setCommandData(String commandData) {
        this.commandData = commandData;
    }

    public CommandStatus getStatus() {
        return status;
    }

    public void setStatus(CommandStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }

    public Instant getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(Instant executedAt) {
        this.executedAt = executedAt;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "Command{" +
                "id=" + id +
                ", commandType='" + commandType + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}