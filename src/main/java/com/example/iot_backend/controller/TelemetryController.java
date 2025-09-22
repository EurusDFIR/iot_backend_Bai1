package com.example.iot_backend.controller;

import com.example.iot_backend.model.Telemetry;
import com.example.iot_backend.service.TelemetryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/telemetry")
public class TelemetryController {

    private final TelemetryService telemetryService;

    public TelemetryController(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    @GetMapping
    public List<Telemetry> getAllTelemetry() {
        return telemetryService.getAllTelemetry();
    }

    @GetMapping("/device/{deviceId}")
    public List<Telemetry> getTelemetryByDevice(@PathVariable Long deviceId) {
        return telemetryService.getTelemetryByDeviceId(deviceId);
    }
}