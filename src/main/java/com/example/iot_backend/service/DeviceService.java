package com.example.iot_backend.service;

import com.example.iot_backend.model.Device;
import com.example.iot_backend.repository.DeviceRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceService {
    private final DeviceRepository repo;

    public DeviceService(DeviceRepository repo) {
        this.repo = repo;
    }

    public List<Device> findAll() {
        // gợi ý bài tập 3: sắp xếp createdAt giảm dần
        return repo.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}