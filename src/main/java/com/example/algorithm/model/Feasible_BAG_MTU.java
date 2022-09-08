package com.example.algorithm.model;

import lombok.*;

import javax.persistence.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Feasible_BAG_MTU extends TimeStamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int BAG;

    @Column(nullable = false)
    private int MTU;

    @ManyToOne(targetEntity = VL.class, fetch = FetchType.LAZY)
    @JoinColumn(name="vl_id") // feasible pair
    private VL bag_mtu_vl;


    public void setVL(VL bag_mtu_vl){
        if(this.bag_mtu_vl!=null){
            this.bag_mtu_vl.removeFeasible_pair(this);
        }
        this.bag_mtu_vl = bag_mtu_vl;
        bag_mtu_vl.addFeasible_pair(this);
    }
    public Feasible_BAG_MTU(int BAG, int MTU, VL bag_mtu_vl){
       this.BAG = BAG;
       this.MTU = MTU;
       setVL(bag_mtu_vl);
   }

}
