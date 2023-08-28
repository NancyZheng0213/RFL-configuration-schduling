package cn.nancy.scheduling_of_rfl.MOEA;

import java.util.ArrayList;
import java.util.Map;

import cn.nancy.scheduling_of_rfl.EncodingConf;

public class EncodingConfofMOEA extends EncodingConf {
    public EncodingConfofMOEA(Map<Integer, ArrayList<Integer>> OptMachineSet) {
        super(OptMachineSet);
    }
}
