package com.example.algorithm.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter @Getter
@AllArgsConstructor
@NoArgsConstructor
public class Flow extends TimeStamped{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int l;

    @Column(nullable = false)
    private int p;

    @ManyToOne(targetEntity = VL.class, fetch = FetchType.LAZY)
    @JoinColumn(name="vl_id")
    private VL flow_vl;

    public void setVL_flow(VL flow_vl){
        if(this.flow_vl!=null){
            this.flow_vl.removeFlow(this);
        }
        this.flow_vl = flow_vl;
        flow_vl.addFlow(this);

    }
    public Flow(int l, int p, VL flow_vl){
        this.l = l;
        this.p = p;
        setVL_flow(flow_vl);
    }


}
