package com.example.algorithm.model;

import lombok.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Entity
public class Switch extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private int bandwidth;

    @OneToMany(mappedBy="switc")
    private List<Switch_VL> siwtch_vls = new ArrayList<>(); // switch 안에 vl들 정보

    public Switch(String name, int bandwidth){
       this.name = name;
       this.bandwidth = bandwidth;
    }
}
