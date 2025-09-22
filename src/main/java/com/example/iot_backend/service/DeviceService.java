package com.example.iot_backend.service;

import com.example.iot_backend.model.Device;
import com.example.iot_backend.repository.DeviceRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public Optional<Device> findById(Long id) {
        return repo.findById(id);
    }

    public Device save(Device device) {
        return repo.save(device);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public boolean existsById(Long id) {
        return repo.existsById(id);
    }
}