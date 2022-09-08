package com.example.algorithm.repository;

import com.example.algorithm.model.Switch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SwitchRepository extends JpaRepository<Switch, Long> {
    Switch findSwitchByName(String switch_name);
}
