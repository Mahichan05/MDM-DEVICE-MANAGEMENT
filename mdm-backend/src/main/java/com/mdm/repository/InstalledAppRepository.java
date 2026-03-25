package com.mdm.repository;

import com.mdm.entity.InstalledApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstalledAppRepository extends JpaRepository<InstalledApp, Long> {

    List<InstalledApp> findByDeviceId(String deviceId);

    void deleteByDeviceId(String deviceId);
}
