package com.mdm.repository;

import com.mdm.entity.EnrolledDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnrolledDeviceRepository extends JpaRepository<EnrolledDevice, Long> {

    Optional<EnrolledDevice> findByDeviceId(String deviceId);

    boolean existsByDeviceId(String deviceId);
}
