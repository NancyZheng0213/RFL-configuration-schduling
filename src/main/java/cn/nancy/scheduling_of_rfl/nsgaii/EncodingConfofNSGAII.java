package cn.nancy.scheduling_of_rfl.nsgaii;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import cn.nancy.scheduling_of_rfl.EncodingConf;

/**
 * 此类为配置编码的实现，包含两种 isSameChoice 方法
 */
public class EncodingConfofNSGAII extends EncodingConf {
    /**
     * 初始化
     * @param OptMachineSet 记录每道工序对应的可选机器集合，如 {3:[3,5,7]} 表示工序 3 当前有 3、5、7 共三台机器可以选择
     */
    public EncodingConfofNSGAII(Map<Integer, ArrayList<Integer>> OptMachineSet) {
        super(OptMachineSet);
    }

    // /**
    //  * 判断是否有机器无交集的相邻工序，若有将其标记为不同工位放入 NoIntersectionProcess，并从中选择相邻的两个工序作为编码对象。否则随机选择编码对象。
    //  */
    // @Override
    // public boolean SearchNoIntersection() {
    //     boolean flag = false;
    //     // 注释开始*************************************************************************
    //     Set<Integer> NoIntersectionProcess = new HashSet<Integer>();    // 记录机器无交集的相邻工序，如 [2,3,3,4] 表示第二道工序和第三道工序没有交集，第三道工序和第四道工序没有交集

    //     // 遍历 UncodedProcess 中每一道工序，比较该工序的可用机器集合跟相邻工序是否有交集，若没有，则将将道工序按顺序存入放入 NoIntersectionProcess
    //     // list.retainAll(Collection<?> c) 方法保留 list 中包含在 c 中的元素，当保留元素为 0 时，说明 list 中没有 c 中的任何元素，即无交集。
    //     if (super.getUncodedProcess().contains(1)) {
    //         ArrayList<Integer> machine = new ArrayList<>(super.getOptMachineSet().get(1));
    //         machine.retainAll(super.getOptMachineSet().get(2));
    //         if (machine.size() == 0) {
    //             flag = true;
    //             NoIntersectionProcess.add(1);
    //             if (super.getUncodedProcess().contains(2)) {
    //                 NoIntersectionProcess.add(2);
    //             }
    //         }
    //     }
    //     for (int i = 2; i < super.getConfigurationCode().size(); i++) {
    //         if (super.getUncodedProcess().contains(i)) {
    //             // 比较 i - 1 和 i
    //             ArrayList<Integer> machine = new ArrayList<>(super.getOptMachineSet().get(i));
    //             machine.retainAll(super.getOptMachineSet().get(i - 1));
    //             if (machine.size() == 0) {
    //                 flag = true;
    //                 NoIntersectionProcess.add(i);
    //                 if (super.getUncodedProcess().contains(i - 1)) {
    //                     NoIntersectionProcess.add(i - 1);
    //                 }
    //             }
    //             // 比较 i + 1 和 i
    //             machine = new ArrayList<>(super.getOptMachineSet().get(i));
    //             machine.retainAll(super.getOptMachineSet().get(i + 1));
    //             if (machine.size() == 0) {
    //                 flag = true;
    //                 NoIntersectionProcess.add(i);
    //                 if (super.getUncodedProcess().contains(i + 1)) {
    //                     NoIntersectionProcess.add(i + 1);
    //                 }
    //             }
    //         }
    //     }
    //     if (super.getUncodedProcess().contains(super.getConfigurationCode().size())) {
    //         ArrayList<Integer> machine = new ArrayList<>(super.getOptMachineSet().get(super.getConfigurationCode().size()));
    //         machine.retainAll(super.getOptMachineSet().get(super.getConfigurationCode().size() - 1));
    //         if (machine.size() == 0) {
    //             flag = true;
    //             NoIntersectionProcess.add(super.getConfigurationCode().size());
    //             NoIntersectionProcess.add(super.getConfigurationCode().size() - 1);
    //         }
    //     }
    //     // 如果存在无交集的相邻两工序，则从中随机选择一对作为下一个编码的工序，否则从所有未编码工序中随机选择
    //     if (NoIntersectionProcess.size() != 0) {
    //         super.setProcessSelectMode(1);
    //         ArrayList<Integer> CurrentProcess = new ArrayList<Integer>();                         // 初始化记录
    //         Iterator<Integer> iterator = NoIntersectionProcess.iterator();
    //         while (iterator.hasNext()) {
    //             CurrentProcess.add(iterator.next());
    //         }
    //         super.setCurrentProcess(CurrentProcess);
    //     } else {
    //     // 注释结束*************************************************************************    
    //         super.setProcessSelectMode(-1);
    //         int selectIndex = new Random().nextInt(super.getUncodedProcess().size());
    //         super.getCurrentProcess().add(super.getUncodedProcess().get(selectIndex));
    //     // 注释开始*************************************************************************
    //     }
    //     // 注释结束*************************************************************************
    //     return flag;
    // }

    // /**
    //  * 为 CurrentProcess 中的工序选择机器
    //  * @return Map<Integer, Integer>
    //  * 
    //  * <p>返回类型 Map<\Integer, Integer>\，key：工序编号，value：选择的机器</p>
    //  */
    // public void selectMachine() {
    //     super.selectMachine();
    //     ArrayList<Integer> CurrentProcess = super.getCurrentProcess();
    //     Map<Integer, Integer> OptMachine = super.getOptMachine();

    //     // 注释开始*************************************************************************
    //     // 计算每台机器的选择概率
    //     Map<Integer, double[]> OptMachineProb = new HashMap<Integer, double[]>();   // 记录每道工序的每台可选机器的概率
    //     for (Integer process : CurrentProcess) {
    //         int machineNum = super.getOptMachineSet().get(process).size();    // 工序可选机器数量
    //         double[] probability = new double[machineNum];              // 记录每台机器的概率
    //         double basicProbability = 1d / machineNum;                  // 记录每台机器的初始概率（均分）
    //         Arrays.fill(probability, basicProbability);                 // 均匀初始化每台机器的概率
    //         for (int i = 0; i < machineNum; i++) {
    //             // 向左侧搜索
    //             for (int j = process - 1; j > 0; j--) {
    //                 if (super.getOptMachineSet().get(j).contains(super.getOptMachineSet().get(process).get(i))) {
    //                     probability[i] += basicProbability * Math.pow(0.5, process - j);
    //                 } else {
    //                     break;
    //                 }
    //             }
    //             // 向右侧搜索
    //             for (int j = process + 1; j <= super.getOptMachineSet().size(); j++) {
    //                 if (super.getOptMachineSet().get(j).contains(super.getOptMachineSet().get(process).get(i))) {
    //                     probability[i] += basicProbability * Math.pow(0.5, j - process);
    //                 } else {
    //                     break;
    //                 }
    //             }
    //         }
    //         OptMachineProb.put(process, probability);
    //     }

    //     // 将概率处理到和为 1 
    //     for (Map.Entry<Integer, double[]> entry : OptMachineProb.entrySet()) {
    //         double sumProb = 0;     // 概率和
    //         double[] probability = new double[entry.getValue().length];

    //         for (double d : entry.getValue()) {
    //             sumProb += d;
    //         }
    //         for (int i = 0; i < entry.getValue().length; i++) {
    //             probability[i] = entry.getValue()[i] / sumProb;
    //         }
    //         entry.setValue(probability);
    //     }

    //     // 根据概率选择机器
    //     for (Map.Entry<Integer, double[]> entry : OptMachineProb.entrySet()) {
    //         double p = new Random().nextDouble();
    //         for (int i = 0; i < entry.getValue().length; i++) {
    //             p -= entry.getValue()[i];
    //             if (p <= 0) {
    //                 OptMachine.put(entry.getKey(), super.getOptMachineSet().get(entry.getKey()).get(i));
    //                 break;
    //             }
    //         }
    //     }
    //     // 注释结束*************************************************************************
    //     super.setOptMachine(OptMachine);
    // }
}
