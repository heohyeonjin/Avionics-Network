package com.example.algorithm.repository;

import com.example.algorithm.model.VL;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VLRepository extends JpaRepository<VL,Long> {
    VL findVLByName(String vl_name);
}
