package com.example.iot_backend.controller;

import com.example.iot_backend.model.Device;
import com.example.iot_backend.service.DeviceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {
    private final DeviceService service;

    public DeviceController(DeviceService service) {
        this.service = service;
    }

    @GetMapping
    public List<Device> getAllDevices() {
        return service.findAll();
    }
}