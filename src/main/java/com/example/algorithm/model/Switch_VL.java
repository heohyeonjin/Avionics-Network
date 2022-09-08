package com.example.algorithm.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class Switch_VL {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="switch_vl_id")
    private Long Id;

    // Switch
    @ManyToOne(targetEntity = Switch.class, fetch = FetchType.LAZY)
    @JoinColumn(name="switch_id")
    private Switch switc;

    // VL
    @ManyToOne(targetEntity = VL.class, fetch = FetchType.LAZY)
    @JoinColumn(name="vl_id")
    private VL vl;

    // 연관관계 편의 메서드
    public void setSwitch(Switch switc){
        if(this.switc!=null){
            this.switc.getSiwtch_vls().remove(this);
        }
        this.switc = switc;
        switc.getSiwtch_vls().add(this);
    }

    public void setVL(VL vl){
        if(this.vl!=null){
            this.vl.getSwitch_vls().remove(this);
        }
        this.vl = vl;
        vl.getSwitch_vls().add(this);
    }

    public Switch_VL(Switch switc, VL vl){
        setSwitch(switc);
        setVL(vl);
    }
}
