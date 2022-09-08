package com.example.algorithm.dto;


import com.example.algorithm.model.VL;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
@Getter
@Setter
public class Node implements Comparable<Node>{
    private int level;
    private double bound;
    private double jitter;
    private ArrayList<BAG_MTU_VL> S;

    public Node(int level, double bound, double jitter, ArrayList<BAG_MTU_VL> s){
        this.level = level;
        this.bound = bound;
        this.jitter = jitter;
        if(s!=null)
            this.S = s;
        else{
            this.S = new ArrayList<>();
        }
    }

    @Override
    public int compareTo(Node o) {
        if(this.bound==o.bound){
            return (int) (this.jitter-o.jitter);
        }
        else
            return (int) (this.bound-o.bound);
    }
    public ArrayList<BAG_MTU_VL> getS(){
        return this.S;
    }

}
