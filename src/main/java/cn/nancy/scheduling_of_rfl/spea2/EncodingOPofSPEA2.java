package cn.nancy.scheduling_of_rfl.spea2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import cn.nancy.scheduling_of_rfl.EncodingOP;

public class EncodingOPofSPEA2 extends EncodingOP{
    /**
     * 初始化
     * @param workNum 工位数量
     * @param OptStageSet 所有操作的可选工位集合
     * @param ProcessOfP 工件 p 的工艺流程记录
     */
    EncodingOPofSPEA2(int workNum, Map<Integer, ArrayList<Integer>> OptStageSet, ArrayList<Integer> ProcessOfP) {
        super(workNum, OptStageSet, ProcessOfP);
    }
    
    /**
     * 针对一个工件初始化 OptStageSet
     * @param workNum 工位数量
     * @param OptStageSet 所有操作的可选工位集合
     * @param ProcessOfP 工件 p 的工艺流程记录
     */
    @Override
    public void initOptStageSet(int workNum, Map<Integer, ArrayList<Integer>> OptStageSet, ArrayList<Integer> ProcessOfP) {
        // 找出工件 p 的每个工序的可用工位
        Map<Integer, ArrayList<Integer>> map = new HashMap<Integer, ArrayList<Integer>>();  // 记录工件 p 每道工序的可用工位
        int processSize = ProcessOfP.size();                                                // 记录工件 p 的工序总数
        for (int processIndex = 0; processIndex < processSize; processIndex++) {
            int process = ProcessOfP.get(processIndex);                                     // 记录工件 p 的第 processIndex 道工序的编号
            ArrayList<Integer> worklist = new ArrayList<Integer>(OptStageSet.get(process)); // 记录工件 p 工序 process 的可用工位
            // 注释开始*************************************************************************
            // 应用规则，缩小集合范围
            if (processIndex + 1 <= processSize - 3) {  // 当操作序号小于等于 processSize - 3，删去可选工位中大于 workNum - 1 的
                for (Integer integer : OptStageSet.get(process)) {
                    if (integer > workNum - 1) {
                        worklist.remove(integer);
                    }
                }
            }
            if (processIndex + 1 > 3) {                 // 当操作序号大于 3 ，删去可选工位中小于等于 1 的
                for (Integer integer : OptStageSet.get(process)) {
                    if (integer <= 1) {
                        worklist.remove(integer);
                    }
                }
            }
            // 注释结束*************************************************************************
            map.put(process, worklist);
        }
        super.getOptStageSet().putAll(map);
    }
}
