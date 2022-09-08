package com.example.algorithm.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class VL extends TimeStamped{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @OneToMany(mappedBy="flow_vl", orphanRemoval = true)
    private List<Flow> flows = new ArrayList<>();

    @OneToMany(fetch= FetchType.LAZY, mappedBy = "vl")
    private List<Switch_VL> switch_vls = new ArrayList<>(); // routing 정보

    @OneToMany(mappedBy="bag_mtu_vl")
    private List<Feasible_BAG_MTU> feasible_pair = new ArrayList<>();

    @OneToMany(mappedBy = "vl")
    private List<Optimal_BAG_MTU> optimal_pair;

    public VL(String name){
        this.name = name;
    }

    public void addFlow(Flow flow){
        this.flows.add(flow);
    }
    public void removeFlow(Flow flow){
        this.flows.remove(flow);
    }

    public void removeFeasible_pair(Feasible_BAG_MTU bag_mtu){
        this.feasible_pair.remove(bag_mtu);
    }
    public void addFeasible_pair(Feasible_BAG_MTU bag_mtu){
        this.feasible_pair.add(bag_mtu);
    }

    public void removeOptimal_bag_mtu(Optimal_BAG_MTU bag_mtu){
        this.optimal_pair.remove(bag_mtu);
    }
    public void addOptimal_bag_mtu(Optimal_BAG_MTU bag_mtu){
        this.optimal_pair.add(bag_mtu);
    }

}
