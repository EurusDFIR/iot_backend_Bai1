package com.example.iot_backend.controller;

import com.example.iot_backend.model.Command;
import com.example.iot_backend.service.CommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/commands")
public class CommandController {

    private final CommandService commandService;

    public CommandController(CommandService commandService) {
        this.commandService = commandService;
    }

    /**
     * Lấy tất cả commands
     */
    @GetMapping
    public List<Command> getAllCommands() {
        return commandService.getAllCommands();
    }

    /**
     * Lấy commands theo device ID
     */
    @GetMapping("/device/{deviceId}")
    public List<Command> getCommandsByDevice(@PathVariable Long deviceId) {
        return commandService.getCommandsByDevice(deviceId);
    }

    /**
     * Lấy commands theo status
     */
    @GetMapping("/status/{status}")
    public List<Command> getCommandsByStatus(@PathVariable String status) {
        try {
            Command.CommandStatus commandStatus = Command.CommandStatus.valueOf(status.toUpperCase());
            return commandService.getCommandsByStatus(commandStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "Invalid status: " + status + ". Valid values: PENDING, SENT, EXECUTED, FAILED, TIMEOUT");
        }
    }

    /**
     * Lấy command theo ID
     */
    @GetMapping("/{commandId}")
    public ResponseEntity<Command> getCommand(@PathVariable Long commandId) {
        Optional<Command> command = commandService.getCommand(commandId);
        return command.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Gửi command tùy chỉnh
     */
    @PostMapping("/send")
    public ResponseEntity<Command> sendCommand(@RequestBody Map<String, Object> request) {
        try {
            Long deviceId = Long.valueOf(request.get("deviceId").toString());
            String commandType = request.get("commandType").toString();
            String commandData = request.get("commandData").toString();

            Command command = commandService.sendCommand(deviceId, commandType, commandData);
            return ResponseEntity.ok(command);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Bật LED cho device
     */
    @PostMapping("/device/{deviceId}/led/on")
    public ResponseEntity<Command> turnLedOn(@PathVariable Long deviceId) {
        try {
            Command command = commandService.turnLedOn(deviceId);
            return ResponseEntity.ok(command);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Tắt LED cho device
     */
    @PostMapping("/device/{deviceId}/led/off")
    public ResponseEntity<Command> turnLedOff(@PathVariable Long deviceId) {
        try {
            Command command = commandService.turnLedOff(deviceId);
            return ResponseEntity.ok(command);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Set nhiệt độ cho device
     */
    @PostMapping("/device/{deviceId}/temperature")
    public ResponseEntity<Command> setTemperature(@PathVariable Long deviceId,
            @RequestBody Map<String, Double> request) {
        try {
            double temperature = request.get("temperature");
            Command command = commandService.setTemperature(deviceId, temperature);
            return ResponseEntity.ok(command);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cập nhật kết quả command (được gọi khi device response)
     */
    @PutMapping("/{commandId}/result")
    public ResponseEntity<Void> updateCommandResult(@PathVariable Long commandId,
            @RequestBody Map<String, Object> request) {
        try {
            String result = request.get("result").toString();
            boolean success = Boolean.parseBoolean(request.get("success").toString());

            commandService.updateCommandResult(commandId, result, success);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}