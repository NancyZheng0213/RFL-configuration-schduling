package cn.nancy.scheduling_of_rfl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 此类为编码类，整合配置编码与调度编码
 * 
 */
public class Encode {
    /**
     * 记录每道工序对应的可选机器集合，如 {3:[3,5,7]} 表示工序 3 当前有 3、5、7 共三台机器可以选择
     */
    private Map<Integer, ArrayList<Integer>> OptMachineSet;
    /**
     * 存储每个操作的可选工位集合，如 {1: [2,3,4]} 表示操作 1 可以在工位 2、3、4 上加工
     */
    private Map<Integer, ArrayList<Integer>> OptStageSet;
    /**
     * 完整编码
     */
    private Code Code;
    /**
     * 配置编码方法
     */
    private EncodingConf encodingConf;
    /**
     * 操作编码方法
     */
    private EncodingOP encodingOP;
    /**
     * 排序编码方法
     */
    private EncodingSort encodingSort;

    /**
     * 初始化
     * @param PartNum 工件种类数量
     * @param Machine 每台机器对应的可加工操作
     * @param ProcessNum 工序数量
     * @param Process 每种工件对应的加工工序列表，<code>Map&lt;Integer, ArrayList&gt;</code>
     * @param AlternativeMachine 每台设备对应的可替换设备列表，<code>Map&lt;Integer, ArrayList&gt;</code>
     */
    public Encode(int PartsNum, Map<Integer, ArrayList<Integer>> Machine, int ProcessNum, Map<Integer, ArrayList<Integer>> Process, Map<Integer, ArrayList<Integer>> AlternativeMachine){
        // 属性初始化
        this.OptMachineSet = new HashMap<Integer, ArrayList<Integer>>();
        this.OptStageSet = new HashMap<Integer, ArrayList<Integer>>();
        this.Code = new Code();
        // 获取工序的可选机器集合
        SearchOptMachineSet(Machine, ProcessNum);
        // 配置编码
        encodingConf(this.encodingConf, AlternativeMachine);
        this.Code.setConfigurationCode(this.encodingConf.getConfigurationCode());
        // 操作编码
        SearchOptStageSet(this.encodingConf.getConfigurationCode(), Machine, ProcessNum);
        for (int p = 0; p < PartsNum; p++) {
            encodingOP(this.encodingConf, this.encodingOP, Process.get(p+1));
            this.Code.setOperationCode(p+1, this.encodingOP.getOperationCode());
        }
        // 排序编码
        this.encodingSort = new EncodingSort(PartsNum);
        this.Code.setSortCode(this.encodingSort.getSortCode());
    }

    /**
     * 不存在可替代机器的配置编码
     */
    public void encodingConf(EncodingConf encodingConf) {
        encodingConf = new EncodingConf(this.OptMachineSet);
        while (! encodingConf.TerminateIteration()) {
            encodingConf.setOptMachine();           // 选择机器
            encodingConf.UpdateOptMachineSet();     // 更新可选机器集合
            if (encodingConf.isSameChoice()) {      // 若存在机器相同的工序并可解，则更新可选机器集合
                encodingConf.UpdateOptMachineSet();
            }
            if (encodingConf.isNoSolution()) {      // 当无解时重新开始循环
                encodingConf = new EncodingConf(this.OptMachineSet);
                continue;
            }
        }
        encodingConf.undateConfigurationCode();
    }

    /**
     * 存在可替代机器的配置编码
     * @param AlternativeMachine
     */
    public void encodingConf(EncodingConf encodingConf, Map<Integer, ArrayList<Integer>> AlternativeMachine) {
        encodingConf = new EncodingConf(this.OptMachineSet);
        while (! encodingConf.TerminateIteration()) {
            encodingConf.setOptMachine();           // 选择机器
            encodingConf.UpdateOptMachineSet();     // 更新可选机器集合
            if (encodingConf.isSameChoice(AlternativeMachine)) {      // 若存在机器相同的工序并可解，则更新可选机器集合
                encodingConf.UpdateOptMachineSet();
            }
            if (encodingConf.isNoSolution()) {      // 当无解时重新开始循环
                encodingConf = new EncodingConf(this.OptMachineSet);
                continue;
            }
        }
        encodingConf.undateConfigurationCode();
    }

    /**
     * 针对某一工件的操作编码
     * @param Machine
     * @param ProcessOfP
     */
    public void encodingOP(EncodingConf encodingConf, EncodingOP encodingOP, ArrayList<Integer> ProcessOfP) {
        encodingOP = new EncodingOP(encodingConf.getConfigurationCode().size(), this.OptStageSet, ProcessOfP);
        while (! encodingOP.TerminateIteration()) {
            // 选择工件 p 的下一个编码工序，并为其选择工位
            encodingOP.setOptStage(ProcessOfP);
            encodingOP.UpdateOptStageSet();
            if (encodingOP.isNoSolution()) {
                encodingOP = new EncodingOP(encodingConf.getConfigurationCode().size(), this.OptStageSet, ProcessOfP);
                continue;
            }
        }
    }

    /**
     * 获取实际工序的可选机器集合
     * @param Machine 每台机器对应的可加工操作
     */
    public void SearchOptMachineSet(Map<Integer, ArrayList<Integer>> Machine, int ProcessNum){
        // TODO: 考虑直接在excel中获取OptMachineSet
        this.OptMachineSet = new HashMap<Integer, ArrayList<Integer>>();

        // 遍历工序集合，在 Machine 中判断每道工序的可执行机器并将他们与机器关联起来，放入 OptMachineSet 中
        for (int i = 0; i < ProcessNum; i++) {
            ArrayList<Integer> machineList = new ArrayList<Integer>();  // 存储当前工序的可执行机器
            // 创建一个迭代器，遍历机器集合，如果 machineEntry 的 value 中包含当前的工序，那么说明当前的机器可以加工当前的操作，将 machineEntry 的 key 放入 OptMachineSet 当前的工序映射中
            Iterator<Map.Entry<Integer, ArrayList<Integer>>> machineIterator = Machine.entrySet().iterator();
            while (machineIterator.hasNext()) {
                Map.Entry<Integer, ArrayList<Integer>> machineEntry = machineIterator.next();
                if (machineEntry.getValue().contains(i + 1)) {
                    machineList.add(machineEntry.getKey());
                }
            }
            this.OptMachineSet.put(i + 1, machineList);
        }
    }

    /**
     * 根据配置编码得到当前每个工位的可选操作集合
     * @param Machine 每台机器对应的可加工操作
     */
    public void SearchOptStageSet(ArrayList<Integer> ConfigurationCode, Map<Integer, ArrayList<Integer>> Machine, int ProcessNum) {
        // 找出当前配置编码每个工位可提供的操作
        Map<Integer, ArrayList<Integer>> OptOPSet = new HashMap<Integer, ArrayList<Integer>>();
        for (int i = 0; i < ConfigurationCode.size(); i++) {
            ArrayList<Integer> list = new ArrayList<Integer>(); // 存储当前机器的操作集合
            for (Integer process : Machine.get(ConfigurationCode.get(i))) {
                list.add(process);
            }
            OptOPSet.put(i+1, list);
        }
        // 找出每个操作的可用工位
        Map<Integer, ArrayList<Integer>> map = new HashMap<Integer, ArrayList<Integer>>();  // 记录每道工序的可用工位
        for (int processIndex = 0; processIndex < ProcessNum; processIndex++) {
            ArrayList<Integer> worklist = new ArrayList<Integer>();                         // 记录工序 process 的可用工位
            for (Map.Entry<Integer, ArrayList<Integer>> processlist : OptOPSet.entrySet()) {
                if (processlist.getValue().contains(processIndex + 1)) {
                    worklist.add(processlist.getKey());
                }
            }
            map.put(processIndex + 1, worklist);
        }
        this.OptStageSet.putAll(map);
    }


    // GET methods
    /**
     * 获取工序可选机器集合
     * @return Map<Integer, ArrayList<Integer>>
     */
    public Map<Integer, ArrayList<Integer>> getOptMachineSet() {
        return this.OptMachineSet;
    }
    /**
     * 获取编码
     * @return cn.nancy.Code
     */
    public Code getCode() {
        return this.Code;
    }
    /**
     * 获取排序编码
     * @return EncodingSort
     */
    public EncodingSort getencodingSort() {
        return this.encodingSort;
    }
    /**
     * 获取配置编码
     * @return EncodingConf
     */
    public EncodingConf getencodingConf() {
        return this.encodingConf;
    }
    /**
     * 获取工序的工位编码
     * @return EncodingOP
     */
    public EncodingOP getencodingOP() {
        return this.encodingOP;
    }
}
