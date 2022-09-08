package com.example.algorithm.dto;

import com.example.algorithm.model.VL;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BAG_MTU_VL {
    private int BAG;
    private int MTU;
    private VL vl;

    public BAG_MTU_VL(int BAG, int MTU, VL vl){
        this.BAG = BAG;
        this.MTU = MTU;
        this.vl = vl;
    }
}
