package com.example.iot_backend.repository;

import com.example.iot_backend.model.Command;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommandRepository extends JpaRepository<Command, Long> {

    // Tìm commands theo device ID
    List<Command> findByDeviceIdOrderByCreatedAtDesc(Long deviceId);

    // Tìm commands theo status
    List<Command> findByStatusOrderByCreatedAtDesc(Command.CommandStatus status);

    // Tìm commands pending (chưa gửi)
    List<Command> findByStatusOrderByCreatedAtAsc(Command.CommandStatus status);

    // Tìm commands theo device và status
    List<Command> findByDeviceIdAndStatusOrderByCreatedAtDesc(Long deviceId, Command.CommandStatus status);
}