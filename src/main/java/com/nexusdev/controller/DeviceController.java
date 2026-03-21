package com.nexusdev.controller;

import com.nexusdev.model.Device;
import com.nexusdev.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    @Autowired
    private DeviceRepository deviceRepository;

    @GetMapping
    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    @GetMapping("/{id}")
    public Device getDevice(@PathVariable Integer id) {
        return deviceRepository.findById(id).orElse(null);
    }

    @PostMapping
    public Device registerDevice(@RequestBody Device device) {
        device.setFirstSeen(LocalDateTime.now());
        device.setLastSeen(LocalDateTime.now());
        return deviceRepository.save(device);
    }

    @DeleteMapping("/{id}")
    public String deleteDevice(@PathVariable Integer id) {
        deviceRepository.deleteById(id);
        return "Device " + id + " deleted";
    }
}