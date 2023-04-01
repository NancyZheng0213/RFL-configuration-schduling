package cn.nancy.scheduling_of_rfl.spea2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

public class Code {
    /**
     * 删除重复后的配置编码，如 [1,3,4] 表示依次放置机器 1、机器 3、机器 4
     */
    private ArrayList<Integer> ConfigurationCode;
    /**
     * 操作编码，如 {1:{1:1, 2:2}} 表示工件 1 的第一道工序在工位 1 上加工，第二道工序在工位 2 上加工
     */
    private Map<Integer, TreeMap<Integer, Integer>> OperationCode;
    /**
     * 产品排序
     */
    private ArrayList<Integer> SortCode;

    /**
     * 初始化
     */
    public Code () {
        this.ConfigurationCode = new ArrayList<Integer>();
        this.OperationCode = new HashMap<Integer, TreeMap<Integer, Integer>>();
        this.SortCode = new ArrayList<Integer>();
    }

    /**
     * 设置配置编码
     * @param ConfigurationCode
     */
    public void setConfigurationCode(ArrayList<Integer> ConfigurationCode) {
        this.ConfigurationCode = new ArrayList<Integer>();
        this.ConfigurationCode.addAll(ConfigurationCode);
    }

    /**
     * 设置某个产品的操作编码
     * @param p 产品编号
     * @param OperationCode
     */
    public void setOperationCode(int p, TreeMap<Integer, Integer> OperationCode) {
        TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>();
        map.putAll(OperationCode);
        this.OperationCode.put(p, map);
    }

    /**
     * 设置所有产品的操作编号
     * @param OperationCode
     */
    public void setOperationCode(Map<Integer, TreeMap<Integer, Integer>> OperationCode) {
        this.OperationCode = new HashMap<Integer, TreeMap<Integer, Integer>>();
        for (Iterator<Entry<Integer, TreeMap<Integer, Integer>>> iterator = OperationCode.entrySet().iterator(); iterator.hasNext(); ) {
            Entry<Integer, TreeMap<Integer, Integer>> entry = iterator.next();
            TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>();
            map.putAll(entry.getValue());
            this.OperationCode.put(entry.getKey(), map);
        }
    }

    /**
     * 设置排序编码
     * @param SortCode
     */
    public void setSortCode(ArrayList<Integer> SortCode) {
        this.SortCode = new ArrayList<Integer>();
        this.SortCode.addAll(SortCode);
    }


    /**
     * 更新操作编码
     * @param PartsNum
     * @param Machine
     * @param ProcessNum
     * @param Process
     */
    public void updateOperationCode(int partnum, Map<Integer, ArrayList<Integer>> Machine, int ProcessNum, Map<Integer, ArrayList<Integer>> Process) {
        // 根据 OptOPSet 获取所有操作的可选工位集合
        Map<Integer, ArrayList<Integer>> OptStageSet = SearchOptStageSet(Machine, ProcessNum);
        // 产品操作编码
        for (int p = 0; p < partnum; p++) {
            setOperationCode(p + 1, encodingOP(this.ConfigurationCode.size(), OptStageSet, Process.get(p + 1)));
        }
    }

    /**
     * 根据配置编码得到当前每个工位的可选操作集合
     * @param Machine 每台机器对应的可加工操作
     */
    public Map<Integer, ArrayList<Integer>> SearchOptStageSet(Map<Integer, ArrayList<Integer>> Machine, int ProcessNum) {
        // 找出当前配置编码每个工位可提供的操作
        Map<Integer, ArrayList<Integer>> OptOPSet = new HashMap<Integer, ArrayList<Integer>>();
        for (int i = 0; i < this.ConfigurationCode.size(); i++) {
            ArrayList<Integer> list = new ArrayList<Integer>(); // 存储当前机器的操作集合
            for (Integer process : Machine.get(this.ConfigurationCode.get(i))) {
                list.add(process);
            }
            OptOPSet.put(i+1, list);
        }
        // 找出每个操作的可用工位
        Map<Integer, ArrayList<Integer>> OptStageSet = new HashMap<Integer, ArrayList<Integer>>();  // 记录每道工序的可用工位
        for (int processIndex = 0; processIndex < ProcessNum; processIndex++) {
            ArrayList<Integer> worklist = new ArrayList<Integer>();                         // 记录工序 process 的可用工位
            for (Map.Entry<Integer, ArrayList<Integer>> processlist : OptOPSet.entrySet()) {
                if (processlist.getValue().contains(processIndex + 1)) {
                    worklist.add(processlist.getKey());
                }
            }
            OptStageSet.put(processIndex + 1, worklist);
        }

        return OptStageSet;
    }

    /**
     * 针对某一工件的操作编码
     * @param allOptStageSet
     * @param ProcessOfP
     * @return 
     */
    public TreeMap<Integer, Integer> encodingOP(int workNum, Map<Integer, ArrayList<Integer>> allOptStageSet, ArrayList<Integer> ProcessOfP) {
        Map<Integer, ArrayList<Integer>> OptStageSet = new HashMap<>();
        for (Iterator<Map.Entry<Integer, ArrayList<Integer>>> iterator = allOptStageSet.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<Integer, ArrayList<Integer>> entry = iterator.next();
            ArrayList<Integer> list = new ArrayList<>(entry.getValue());
            OptStageSet.put(entry.getKey(), list);
        }
        EncodingOP encodingOP = new EncodingOP(workNum, OptStageSet, ProcessOfP);
        while (! encodingOP.TerminateIteration()) {
            // 选择工件 p 的下一个编码工序，并为其选择工位
            encodingOP.setOptStage(ProcessOfP);
            encodingOP.UpdateOptStageSet();
            if (encodingOP.isNoSolution()) {
                encodingOP = new EncodingOP(workNum, OptStageSet, ProcessOfP);
                continue;
            }
        }

        return encodingOP.getOperationCode();
    }

    /**
     * 获取配置编码
     * @return
     */
    public ArrayList<Integer> getConfigurationCode() {
        return this.ConfigurationCode;
    }
    /**
     * 获取操作编码
     * @return
     */
    public Map<Integer, TreeMap<Integer, Integer>> getOperationCode() {
        return this.OperationCode;
    }
    public TreeMap<Integer, Integer> getOperationCode(int part) {
        return this.OperationCode.get(part);
    }
    /**
     * 获取排序编码
     * @return
     */
    public ArrayList<Integer> getSortCode() {
        return this.SortCode;
    }
}
