package cn.nancy.scheduling_of_rfl.MOEA;

import java.util.ArrayList;
import java.util.Map;

import cn.nancy.scheduling_of_rfl.EncodingOP;

public class EncodingOPofMOEA extends EncodingOP {
    public EncodingOPofMOEA(int workNum, Map<Integer, ArrayList<Integer>> OptStageSet, ArrayList<Integer> ProcessOfP) {
        super(workNum, OptStageSet, ProcessOfP);
    }
}
