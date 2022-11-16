package com.example.algorithm.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
    @Getter
    @Setter
    public class GlobalNode implements Comparable<GlobalNode>{
        private int level;
        private double[] bound;
        private double[] jitter;
        private ArrayList<BAG_MTU_VL> S;

        public GlobalNode(int level, double[] bound, double[] jitter, ArrayList<BAG_MTU_VL> s){
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
        public int compareTo(GlobalNode o) {
            double total_bound = 0;
            for(double x : bound) total_bound+=x;
            double total_jitter = 0;
            for(double x : jitter) total_jitter+=x;

            double total_obound = 0;
            for(double x : o.bound) total_obound+=x;
            double total_ojitter = 0;
            for(double x : o.jitter) total_ojitter+=x;



            if(total_bound==total_obound){
                return (int) (total_jitter-total_ojitter);
            }
            else
                return (int) (total_bound-total_obound);
        }
        public ArrayList<BAG_MTU_VL> getS(){
            return this.S;
        }

}
