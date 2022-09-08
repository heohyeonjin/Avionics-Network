package com.example.algorithm.repository;

import com.example.algorithm.model.Switch;
import com.example.algorithm.model.Switch_VL;
import com.example.algorithm.model.VL;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SwitchVLRepository extends JpaRepository<Switch_VL,Long > {
    List<Switch_VL> findSwitch_VLSBySwitc(Switch switc);
}
