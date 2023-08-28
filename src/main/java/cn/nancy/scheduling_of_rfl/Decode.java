package cn.nancy.scheduling_of_rfl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class Decode {
    /**
     * 每台 RMT 每个操作所有工件的开始加工时间，如 [{1:[0.0, 1.0], 2:[0, 0]},{2:[1.0, 0]}] 表示第一台 RMT 的两个操作起始时间分别是 [0.0, 1.0] 和 [0, 0]
     */
    private ArrayList<Map<Integer, ArrayList<Double>>> StartTime;
    /**
     * 每台 RMT 每个操作所有工件的结束加工时间，如 [{1:[0.0, 1.0], 2:[0.5, 0]},{2:[1.0, 0]}] 表示第一台 RMT 的两个操作结束时间分别是 [0.0, 1.0] 和 [0.5, 0]
     */
    private ArrayList<Map<Integer, ArrayList<Double>>> FinishTime;
    /**
     * 每台 RMT 每个操作所有工件的工时，如 [{1:[2.0, 1.0], 2:[0.5, 0]},{2:[1.0, 0]}] 表示第一台 RMT 的两个操作的加工时长分别是 [2.0, 1.0] 和 [0.5, 0]
     */
    private ArrayList<Map<Integer, ArrayList<Double>>> ProcessingTime;
    /**
     * 每台 RMT 的每个操作换型开始时间，如 [{1:[0.5, 1.0], 2:[0.6, 0.7]},{2:[1.0, 1.7]}] 表示第一台 RMT 的两个操作换型时间分别是 0.0 和 1.0 开始
     */
    private ArrayList<Map<Integer, ArrayList<Double>>> SetupStartTime;
    /**
     * 每台 RMT 的每个操作换型结束时间
     */
    private ArrayList<Map<Integer, ArrayList<Double>>> SetupFinishTime;
    /**
     * 该编码的完工时间
     */
    private double Makespan;
    /**
     * 该编码的总拖期时间
     */
    private int TotalDelay;
    /**
     * 设备利用率
     */
    private double Utilization;

    public Decode() {
        // 实例化属性
        this.StartTime = new ArrayList<Map<Integer, ArrayList<Double>>>();
        this.FinishTime = new ArrayList<Map<Integer, ArrayList<Double>>>();
        this.ProcessingTime = new ArrayList<Map<Integer, ArrayList<Double>>>();
        this.SetupStartTime = new ArrayList<Map<Integer, ArrayList<Double>>>();
        this.SetupFinishTime = new ArrayList<Map<Integer, ArrayList<Double>>>();
        this.Makespan = 0;
        this.Utilization = 0;
        this.TotalDelay = 0;
    }

    /**
     * 初始化及完整解码过程
     * @param code 编码
     * @param MachineTime 每台机器对应的可加工操作的操作时间
     * @param Demand 工件需求量
     * @param SetupTime 每个操作的换模时间
     */
    public void setdecoding(Code code, Map<Integer, TreeMap<Integer, Double>> MachineTime, Map<Integer, Integer> Demand, Map<Integer, Double> SetupTime) {
        this.StartTime = new ArrayList<Map<Integer, ArrayList<Double>>>();
        this.FinishTime = new ArrayList<Map<Integer, ArrayList<Double>>>();
        this.ProcessingTime = new ArrayList<Map<Integer, ArrayList<Double>>>();
        this.SetupStartTime = new ArrayList<Map<Integer, ArrayList<Double>>>();
        this.SetupFinishTime = new ArrayList<Map<Integer, ArrayList<Double>>>();
        // 按配置编码顺序遍历机器，初始化时间表
        for (Integer machine : code.getConfigurationCode()) {   // 机器编号
            Map<Integer, ArrayList<Double>> timemap = new HashMap<Integer, ArrayList<Double>>();
            // 按工艺约束遍历 machine 的每个操作
            for (Iterator<Map.Entry<Integer, Double>> machinetimeIterator = MachineTime.get(machine).entrySet().iterator(); machinetimeIterator.hasNext();) {
                Map.Entry<Integer, Double> entry = machinetimeIterator.next();
                int process = entry.getKey();                   // 操作编号
                ArrayList<Double> startlist = new ArrayList<Double>(Collections.nCopies(code.getSortCode().size(), 0.0));
                timemap.put(process, startlist);
            }
            this.StartTime.add(timemap);
            this.FinishTime.add(new HashMap<>(timemap));
            this.ProcessingTime.add(new HashMap<>(timemap));
        }
        // 计算所有时间表
        // 按配置编码顺序遍历机器，初始化时间表
        for (int machineindex = 0; machineindex < code.getConfigurationCode().size(); machineindex++) {
            int machine = code.getConfigurationCode().get(machineindex);    // 机器编号
            Map<Integer, ArrayList<Double>> setupstartmap = new TreeMap<>();
            Map<Integer, ArrayList<Double>> setupfinishmap = new TreeMap<>();
            // 按工艺约束遍历 machine 的每个操作
            for (int process : MachineTime.get(machine).keySet()) {         // 操作编号      
                ArrayList<Double> startlist = new ArrayList<Double>(this.StartTime.get(machineindex).get(process));
                ArrayList<Double> finishlist = new ArrayList<Double>(this.FinishTime.get(machineindex).get(process));
                ArrayList<Double> setupstartlist = new ArrayList<>();
                ArrayList<Double> setupfinishlist = new ArrayList<>();
                // 按排序编码的顺序遍历每个工件
                for (int partindex = 0; partindex < code.getSortCode().size(); partindex++) {
                    int part = code.getSortCode(partindex);           // 工件编号
                    ArrayList<Integer> processlist = new ArrayList<Integer>();
                    processlist.addAll(code.getOperationCode(part).keySet());
                    // 当该工件有前道工件时，开始时间要与前道工件的结束时间比较
                    if (partindex > 0 && startlist.get(partindex) < finishlist.get(partindex - 1)) {
                        if (code.getOperationCode(part).containsKey(process) && code.getOperationCode(part).get(process) == machineindex + 1) {
                            double starttime = finishlist.get(partindex - 1) + SetupTime.get(process);
                            startlist.set(partindex, starttime);
                            setupstartlist.add(finishlist.get(partindex - 1));
                            setupfinishlist.add(starttime);
                        } else {
                            startlist.set(partindex, finishlist.get(partindex - 1));
                        }
                    }
                    // 当确定该工件的该操作是在当前机器上执行时，且该工件有前道工序时，开始时间要与前道工序的结束时间比较，结束时间=开始时间+工时*需求，否则结束时间=开始时间
                    if (code.getOperationCode(part).containsKey(process) && code.getOperationCode(part).get(process) == machineindex + 1) {
                        int processindex = processlist.indexOf(process);
                        if (processindex > 0) {
                            int previousmachineindex = code.getOperationCode(part).get(processlist.get(processindex - 1));
                            double finishtime = this.FinishTime.get(previousmachineindex - 1).get(processlist.get(processindex - 1)).get(partindex);
                            if (startlist.get(partindex) < finishtime) {
                                startlist.set(partindex, finishtime);
                            }
                        }
                        double processtime = MachineTime.get(machine).get(process); // 操作时间
                        finishlist.set(partindex, (
                            startlist.get(partindex) + processtime * Demand.get(part)
                        ));
                        this.ProcessingTime.get(machineindex).get(process).set(partindex, processtime * Demand.get(part));
                    } else {
                        finishlist.set(partindex, startlist.get(partindex));
                    }
                    this.StartTime.get(machineindex).put(process, new ArrayList<>(startlist));
                    this.FinishTime.get(machineindex).put(process, new ArrayList<>(finishlist));
                }
                setupstartmap.put(process, setupstartlist);
                setupfinishmap.put(process, setupfinishlist);
            }
            this.SetupStartTime.add(setupstartmap);
            this.SetupFinishTime.add(setupfinishmap);
        }
        // 计算完工时间
        makespan();
    }

    /**
     * 计算完工时间
     */
    public void makespan() {
        this.Makespan = 0;
        for (Iterator<Map<Integer, ArrayList<Double>>> timeiterator = this.FinishTime.iterator(); timeiterator.hasNext();) {
            Map<Integer, ArrayList<Double>> map = timeiterator.next();
            for (Iterator<Map.Entry<Integer, ArrayList<Double>>> processIterator = map.entrySet().iterator(); processIterator.hasNext();) {
                ArrayList<Double> list  = processIterator.next().getValue();
                double makespan = list.get(list.size() - 1);
                if (this.Makespan < makespan) {
                    this.Makespan = makespan;
                }
            }
        }
    }

    /**
     * 计算利用率
     */
    public void utilization(int PartsNum) {
        this.Utilization = 0;
        for (int machineIndex = 0; machineIndex < this.ProcessingTime.size(); machineIndex++) {
            ArrayList<Double> timeList = new ArrayList<>();
            // 记录该机器第一道工序的所有加工时间段
            Map<Integer, ArrayList<Double>> startMap = this.StartTime.get(machineIndex);
            ArrayList<Integer> processList = new ArrayList<>(startMap.keySet());
            Integer process = processList.get(0);
            for (int partIndex = 0; partIndex < PartsNum; partIndex++) {
                if (this.ProcessingTime.get(machineIndex).get(process).get(partIndex) != 0) {
                    timeList.add(startMap.get(process).get(partIndex));
                    timeList.add(this.FinishTime.get(machineIndex).get(process).get(partIndex));
                }
            }
            // 遍历剩下的工序，判断加工时间段是否已被记录在 timelist 中
            for (int processIndex = 1; processIndex < processList.size(); processIndex++) {
                process = processList.get(processIndex);
                for (int partIndex = 0; partIndex < PartsNum; partIndex++) {
                    if (this.ProcessingTime.get(machineIndex).get(process).get(partIndex) != 0) {
                        double start = this.StartTime.get(machineIndex).get(process).get(partIndex);
                        double finish = this.FinishTime.get(machineIndex).get(process).get(partIndex);
                        // System.out.print(timeList);
                        // System.out.println();
                        if (timeList.isEmpty() || start >= timeList.get(timeList.size() - 1)) {
                            timeList.add(start);
                            timeList.add(finish);
                        } else {
                            int index1;
                            int index2;
                            int i = 0;
                            while (timeList.get(i) < start) {
                                i++;
                            }
                            index1 = i;
                            while (i < timeList.size() && timeList.get(i) < finish) {
                                i++;
                            }
                            index2 = i;
                            if (index1 % 2 == 0 && index2 == index1) {
                                timeList.add(index1, start);
                                timeList.add(index1 + 1, finish);
                                continue;
                            }
                            if (index1 % 2 == 0 && index2 - index1 == 1) {
                                timeList.set(index1, start);
                                continue;
                            }
                            if (index1 % 2 == 0 && index2 - index1 == 2) {
                                timeList.set(index1, start);
                                timeList.set(index1 + 1, finish);
                                continue;
                            }
                            if (index1 % 2 == 0 && index2 - index1 > 2) {
                                timeList.set(index1, start);
                                if (index2 % 2 == 0) {
                                    for (int j = index2 - 1; j > index1; j--) {
                                        timeList.remove(j);
                                    }
                                    timeList.add(index1 + 1, finish);
                                } else {
                                    for (int j = index2 - 1; j > index1; j--) {
                                        timeList.remove(j);
                                    }
                                }
                                continue;
                            }
                            if (index1 % 2 == 1 && index2 - index1 == 1) {
                                timeList.set(index1, finish);
                                continue;
                            }
                            if (index1 % 2 == 1 && index2 - index1 == 2) {
                                for (int j = index2 - 1; j >= index1; j--) {
                                    timeList.remove(j);
                                }
                                continue;
                            }
                            if (index1 % 2 == 1 && index2 - index1 > 2) {
                                if (index2 % 2 == 0) {
                                    for (int j = index2 - 1; j > index1; j--) {
                                        timeList.remove(j);
                                    }
                                    timeList.set(index1, finish);
                                } else {
                                    for (int j = index2 - 1; j >= index1; j--) {
                                        timeList.remove(j);
                                    }
                                }
                                continue;
                            }
                        }
                    }
                }
            }
            // 计算该机器总加工时长
            double utilytime = 0;
            for (int i = 0; i < timeList.size(); i = i + 2) {
                utilytime += timeList.get(i + 1) - timeList.get(i);
            }
            // 计算该机器的利用率
            this.Utilization += utilytime * 100 / this.Makespan;
        }
        this.Utilization /= this.ProcessingTime.size(); // 求平均
    }

    /**
     * 计算总拖期
     * @param SortCode 排序编码
     * @param DueDays 交付时间
     */
    public void totaldelay(ArrayList<Integer> SortCode, Map<Integer, Integer> DueDays) {
        this.TotalDelay = 0;
        for (int machineindex = this.FinishTime.size() - 1; machineindex >= 0; machineindex--) {
            Map<Integer, ArrayList<Double>> finishmap = this.FinishTime.get(machineindex);
            for (int i = 0; i < SortCode.size(); i++) {
                Iterator<Map.Entry<Integer, ArrayList<Double>>> finishmapIterator = finishmap.entrySet().iterator();
                if (finishmapIterator.hasNext()) {
                    ArrayList<Double> finishlist = finishmapIterator.next().getValue();
                    Double finishtime = finishlist.get(i);
                    if (finishtime > 0) {
                        Integer due = DueDays.get(SortCode.get(i));
                        this.TotalDelay += (due < finishlist.get(i)) ? (finishlist.get(i) - due) : 0;
                        break;
                    }
                }
            }
        }
        Map<Integer, ArrayList<Double>> lastfinishmap = this.FinishTime.get(this.FinishTime.size() - 1);    // 最后一台机器上所有工件的完工时间
        int lastprocess = Collections.max(lastfinishmap.keySet());
        ArrayList<Double> lastfinishlist = lastfinishmap.get(lastprocess);
        for (int i = 0; i < SortCode.size(); i++) {
            Integer due = DueDays.get(SortCode.get(i));
            this.TotalDelay += (due < lastfinishlist.get(i)) ? (lastfinishlist.get(i) - due) : 0;
        }
    }

    public void setStartTime(ArrayList<Map<Integer, ArrayList<Double>>> StartTime) {
        this.StartTime = new ArrayList<Map<Integer, ArrayList<Double>>>();
        for (Iterator<Map<Integer, ArrayList<Double>>> timeiterator = StartTime.iterator(); timeiterator.hasNext();) {
            Map<Integer, ArrayList<Double>> timemap = timeiterator.next();
            Map<Integer, ArrayList<Double>> map = new HashMap<Integer, ArrayList<Double>>();
            for (Iterator<Map.Entry<Integer, ArrayList<Double>>> mapIterator = timemap.entrySet().iterator(); mapIterator.hasNext();) {
                Map.Entry<Integer, ArrayList<Double>> entry = mapIterator.next();
                map.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            this.StartTime.add(map);
        }
    }
    public void setFinishTime(ArrayList<Map<Integer, ArrayList<Double>>> FinishTime) {
        this.FinishTime = new ArrayList<Map<Integer, ArrayList<Double>>>();
        for (Iterator<Map<Integer, ArrayList<Double>>> timeiterator = FinishTime.iterator(); timeiterator.hasNext();) {
            Map<Integer, ArrayList<Double>> timemap = timeiterator.next();
            Map<Integer, ArrayList<Double>> map = new HashMap<Integer, ArrayList<Double>>();
            for (Iterator<Map.Entry<Integer, ArrayList<Double>>> mapIterator = timemap.entrySet().iterator(); mapIterator.hasNext();) {
                Map.Entry<Integer, ArrayList<Double>> entry = mapIterator.next();
                map.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            this.FinishTime.add(map);
        }
    }
    public void setProcessingTime(ArrayList<Map<Integer, ArrayList<Double>>> ProcessingTime) {
        this.ProcessingTime = new ArrayList<>();
        for (Iterator<Map<Integer, ArrayList<Double>>> timeiterator = ProcessingTime.iterator(); timeiterator.hasNext();) {
            Map<Integer, ArrayList<Double>> timemap = timeiterator.next();
            Map<Integer, ArrayList<Double>> map = new HashMap<Integer, ArrayList<Double>>();
            for (Iterator<Map.Entry<Integer, ArrayList<Double>>> mapIterator = timemap.entrySet().iterator(); mapIterator.hasNext();) {
                Map.Entry<Integer, ArrayList<Double>> entry = mapIterator.next();
                map.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            this.ProcessingTime.add(map);
        }
    }
    public void setSetupStartTime(ArrayList<Map<Integer, ArrayList<Double>>> SetupStartTime) {
        this.SetupStartTime = new ArrayList<>();
        for (Iterator<Map<Integer, ArrayList<Double>>> timeiterator = SetupStartTime.iterator(); timeiterator.hasNext();) {
            Map<Integer, ArrayList<Double>> timemap = timeiterator.next();
            Map<Integer, ArrayList<Double>> map = new HashMap<Integer, ArrayList<Double>>();
            for (Iterator<Map.Entry<Integer, ArrayList<Double>>> mapIterator = timemap.entrySet().iterator(); mapIterator.hasNext();) {
                Map.Entry<Integer, ArrayList<Double>> entry = mapIterator.next();
                map.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            this.SetupStartTime.add(map);
        }
    }
    public void setSetupFinishTime(ArrayList<Map<Integer, ArrayList<Double>>> SetupFinishTime) {
        this.SetupFinishTime = new ArrayList<>();
        for (Iterator<Map<Integer, ArrayList<Double>>> timeiterator = SetupFinishTime.iterator(); timeiterator.hasNext();) {
            Map<Integer, ArrayList<Double>> timemap = timeiterator.next();
            Map<Integer, ArrayList<Double>> map = new HashMap<Integer, ArrayList<Double>>();
            for (Iterator<Map.Entry<Integer, ArrayList<Double>>> mapIterator = timemap.entrySet().iterator(); mapIterator.hasNext();) {
                Map.Entry<Integer, ArrayList<Double>> entry = mapIterator.next();
                map.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            this.SetupFinishTime.add(map);
        }
    }
    public void setMakespan(double Makespan) {
        this.Makespan = Makespan;
    }
    public void setTotalDelay(int TotalDelay) {
        this.TotalDelay = TotalDelay;
    }
    public void setUtilization(double Utilization) {
        this.Utilization = Utilization;
    }

    public ArrayList<Map<Integer, ArrayList<Double>>> getFinishTime() {
        return this.FinishTime;
    }
    public double getMakespan() {
        return this.Makespan;
    }
    public ArrayList<Map<Integer, ArrayList<Double>>> getProcessingTime() {
        return this.ProcessingTime;
    }
    public ArrayList<Map<Integer, ArrayList<Double>>> getSetupFinishTime() {
        return this.SetupFinishTime;
    }
    public ArrayList<Map<Integer, ArrayList<Double>>> getSetupStartTime() {
        return this.SetupStartTime;
    }
    public ArrayList<Map<Integer, ArrayList<Double>>> getStartTime() {
        return this.StartTime;
    }
    public double getUtilization() {
        return this.Utilization;
    }
    public int getTotalDelay() {
        return this.TotalDelay;
    }
    /**
     * 获取目标函数值
     * @param ObjectiveName 目标名称，“Utilization”或者“TotalDelay”
     * @return
     */
    public double getObjectiveValue(String ObjectiveName) {
        switch (ObjectiveName) {
            case "Utilization":
                return this.Utilization;
            case "TotalDelay":
                return this.TotalDelay;
            default:
                throw new IllegalArgumentException("The name of objective MUST be \"Utilization\" OR \"TotalDelay\".");
        }
    }
}
