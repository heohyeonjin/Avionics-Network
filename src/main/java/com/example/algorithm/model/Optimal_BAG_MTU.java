package com.example.algorithm.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Optimal_BAG_MTU extends TimeStamped{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String switch_name;

    @Column(nullable = false)
    private int BAG;

    @Column(nullable = false)
    private int MTU;

    @ManyToOne(targetEntity = VL.class, fetch = FetchType.LAZY)
    @JoinColumn(name="vl")
    private VL vl;

    public void setVL(VL vl){
        if(this.vl!=null){
            this.vl.removeOptimal_bag_mtu(this);
        }
        this.vl = vl;
        vl.addOptimal_bag_mtu(this);
    }

    public Optimal_BAG_MTU(String switch_name, int BAG, int MTU, VL vl){
        this.switch_name = switch_name;
        this.BAG = BAG;
        this.MTU = MTU;
        setVL(vl);
    }
}
