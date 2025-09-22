package com.example.iot_backend.service;

import com.example.iot_backend.model.Command;
import com.example.iot_backend.model.Device;
import com.example.iot_backend.repository.CommandRepository;
import com.example.iot_backend.repository.DeviceRepository;
import com.example.iot_backend.mqtt.HiveMqttService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class CommandService {
    private static final Logger log = LoggerFactory.getLogger(CommandService.class);

    private final CommandRepository commandRepo;
    private final DeviceRepository deviceRepo;
    private final HiveMqttService mqttService;

    public CommandService(CommandRepository commandRepo,
            DeviceRepository deviceRepo,
            HiveMqttService mqttService) {
        this.commandRepo = commandRepo;
        this.deviceRepo = deviceRepo;
        this.mqttService = mqttService;
    }

    /**
     * Tạo và gửi command tới device
     */
    public Command sendCommand(Long deviceId, String commandType, String commandData) {
        Optional<Device> deviceOpt = deviceRepo.findById(deviceId);
        if (deviceOpt.isEmpty()) {
            throw new RuntimeException("Device not found with id: " + deviceId);
        }

        Device device = deviceOpt.get();

        // Tạo command trong database
        Command command = new Command(device, commandType, commandData);
        command = commandRepo.save(command);

        // Gửi qua MQTT
        boolean sent = mqttService.publishCommand(device, command);

        if (sent) {
            command.setStatus(Command.CommandStatus.SENT);
            command.setSentAt(Instant.now());
            command = commandRepo.save(command);
            log.info("Command sent successfully: {}", command);
        } else {
            command.setStatus(Command.CommandStatus.FAILED);
            command = commandRepo.save(command);
            log.error("Failed to send command: {}", command);
        }

        return command;
    }

    /**
     * Cập nhật trạng thái command khi nhận response từ device
     */
    public void updateCommandResult(Long commandId, String result, boolean success) {
        Optional<Command> commandOpt = commandRepo.findById(commandId);
        if (commandOpt.isPresent()) {
            Command command = commandOpt.get();
            command.setResult(result);
            command.setExecutedAt(Instant.now());
            command.setStatus(success ? Command.CommandStatus.EXECUTED : Command.CommandStatus.FAILED);
            commandRepo.save(command);

            log.info("Command {} result: {}", commandId, success ? "SUCCESS" : "FAILED");
        }
    }

    /**
     * Lấy tất cả commands
     */
    public List<Command> getAllCommands() {
        return commandRepo.findAll();
    }

    /**
     * Lấy commands theo device ID
     */
    public List<Command> getCommandsByDevice(Long deviceId) {
        return commandRepo.findByDeviceIdOrderByCreatedAtDesc(deviceId);
    }

    /**
     * Lấy commands theo status
     */
    public List<Command> getCommandsByStatus(Command.CommandStatus status) {
        return commandRepo.findByStatusOrderByCreatedAtDesc(status);
    }

    /**
     * Lấy command theo ID
     */
    public Optional<Command> getCommand(Long commandId) {
        return commandRepo.findById(commandId);
    }

    /**
     * Tạo command đơn giản cho LED
     */
    public Command turnLedOn(Long deviceId) {
        return sendCommand(deviceId, "LED_ON", "{\"led\": \"on\"}");
    }

    public Command turnLedOff(Long deviceId) {
        return sendCommand(deviceId, "LED_OFF", "{\"led\": \"off\"}");
    }

    /**
     * Tạo command set nhiệt độ
     */
    public Command setTemperature(Long deviceId, double temperature) {
        String commandData = String.format("{\"target_temp\": %.1f}", temperature);
        return sendCommand(deviceId, "SET_TEMP", commandData);
    }
}