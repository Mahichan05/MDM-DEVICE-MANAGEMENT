package com.mdm.repository;

import com.mdm.entity.DeviceLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceLocationRepository extends JpaRepository<DeviceLocation, Long> {

    Optional<DeviceLocation> findTopByDeviceIdOrderByRecordedAtDesc(String deviceId);

    List<DeviceLocation> findByDeviceIdOrderByRecordedAtDesc(String deviceId);
}
