package cn.nancy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 此类为配置编码的实现，包含两种 isSameChoice 方法
 */
public class EncodingConf {

    /**
     * 配置编码，采用整数编码，如 [1,1,1,3,3,5] 表示第一个工位放置机器 1 ，第二个工位放置机器 3 ，第三个工位放置机器 5
     */
    private ArrayList<Integer> ConfigurationCode;
    /**
     * 存储当前每道工序的可选机器集合，实时更新
     */
    private Map<Integer, ArrayList<Integer>> OptMachineSet;
    /**
     * 记录未编码的工序
     */
    private ArrayList<Integer> UncodedProcess;
    /**
     * 记录当前正在编码的工序，列表大小为 [1,2]
     */
    private List<Integer> CurrentProcess;
    /**
     * 记录当前编码工序的选择机器
     */
    private Map<Integer, Integer> OptMachine;
    /**
     * 记录当前编码工序的选择模式，-1：随机选择，0：单个机器，1：无交集工序
     */
    private int ProcessSelectMode;
    /**
     * 记录当前是否无解
     */
    private boolean isNoSolution;

    /**
     * 初始化
     * @param OptMachineSet 记录每道工序对应的可选机器集合，如 {3:[3,5,7]} 表示工序 3 当前有 3、5、7 共三台机器可以选择
     */
    EncodingConf(Map<Integer, ArrayList<Integer>> OptMachineSet) {
        // 属性初始化
        this.OptMachineSet = new HashMap<>();
        // this.OptMachineSet.putAll(OptMachineSet) 不可用，因为此处要实现的是深拷贝，而 putAll 方法作用在 list 时，仅起到引用内存地址的作用，因为 list 是一个具体对象。更好的方法见https://blog.csdn.net/aiynmimi/article/details/76268851
        Iterator<Map.Entry<Integer, ArrayList<Integer>>> iterator = OptMachineSet.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, ArrayList<Integer>> entry = iterator.next();
            ArrayList<Integer> machine = new ArrayList<>();
            machine.addAll(entry.getValue());
            this.OptMachineSet.put(entry.getKey(), machine);
        }
        this.ConfigurationCode = new ArrayList<Integer>(Collections.nCopies(OptMachineSet.size(), -1));
        this.UncodedProcess = new ArrayList<Integer>();
        for (int i = 0; i < OptMachineSet.size(); i++) {
            this.UncodedProcess.add(i + 1);
        }
        this.CurrentProcess = new ArrayList<Integer>();
        this.OptMachine = new HashMap<Integer,Integer>();
        this.ProcessSelectMode = -1;
        this.isNoSolution = false;
    }

    /**
     * 选择下一个编码工序并为其选择机器，将结果存储在 this.OptMachine 中，更新 this.ConfigurationCode 和 this.UncodedProcess
     */
    public void setOptMachine() {
        // 初始化记录
        this.CurrentProcess = new ArrayList<Integer>();
        this.OptMachine = new HashMap<Integer,Integer>();

        // 寻找是否有单个可选机器的工序，若无，寻找是否有无交集的相邻工序，若无，随机选择工序
        if (SearchWithOneMachine()) {
            this.ProcessSelectMode = 0;
        } else {
            SearchNoIntersection();
            selectMachine();
        }
        // 将 this.OptMachine 中储存的机器选择记录在 this.ConfigurationCode 中，在 this.UncodedProcess 中删除该工序
        for (Map.Entry<Integer, Integer> entry : this.OptMachine.entrySet()) {
            this.ConfigurationCode.set(entry.getKey() - 1, entry.getValue());
            this.UncodedProcess.remove(entry.getKey());
        }
    }

    /**
     * 寻找是否有可选机器仅有一台的工序，若是，将其记录在 this.CurrentProcess 中，并为工序选择机器，记录在 this.OptMachine 中
     * @return boolean
     */
    public boolean SearchWithOneMachine() {
        boolean flag = false;   // 记录是否存在单个可选机器的工序

        // 遍历 UncodedProcess 中每一道工序，判断该工序是否只有一台机器可选，若是，选择机器
        for (Integer process : this.UncodedProcess) {
            if (this.OptMachineSet.get(process).size() == 1) {
                flag = true;
                this.CurrentProcess.add(process);
                this.OptMachine.put(process, this.OptMachineSet.get(process).get(0));
            }
        }

        return flag;
    }

    /**
     * 判断是否有机器无交集的相邻工序，若有将其标记为不同工位放入 NoIntersectionProcess，并从中选择相邻的两个工序作为编码对象。否则随机选择编码对象。
     */
    public boolean SearchNoIntersection() {
        boolean flag = false;
        ArrayList<Integer> NoIntersectionProcess = new ArrayList<Integer>();    // 记录机器无交集的相邻工序，如 [2,3,3,4] 表示第二道工序和第三道工序没有交集，第三道工序和第四道工序没有交集

        // 遍历 UncodedProcess 中每一道工序，比较该工序的可用机器集合跟下一道工序是否有交集，若没有，则将将道工序按顺序存入放入 NoIntersectionProcess
        for (int i = 1; i < this.ConfigurationCode.size(); i++) {
            if (this.UncodedProcess.contains(i) && this.UncodedProcess.contains(i + 1)) {
                ArrayList<Integer> machine = new ArrayList<>(this.OptMachineSet.get(i));
                // list.retainAll(Collection<?> c) 方法保留 list 中包含在 c 中的元素，当保留元素为 0 时，说明 list 中没有 c 中的任何元素，即无交集。
                machine.retainAll(this.OptMachineSet.get(i + 1));
                if (machine.size() == 0) {
                    flag = true;
                    NoIntersectionProcess.add(i);
                    NoIntersectionProcess.add(i + 1);
                }
            }   
        }
        // 如果存在无交集的相邻两工序，则从中随机选择一对作为下一个编码的工序，否则从所有未编码工序中随机选择
        if (NoIntersectionProcess.size() != 0) {
            this.ProcessSelectMode = 1;
            this.CurrentProcess = new ArrayList<Integer>();                         // 初始化记录
            int selectIndex = new Random().nextInt(NoIntersectionProcess.size());   // 随机生成的下标
            this.CurrentProcess.add(NoIntersectionProcess.get(selectIndex));
            if (selectIndex % 2 == 0) {
                this.CurrentProcess.add(NoIntersectionProcess.get(selectIndex + 1));
            } else {
                this.CurrentProcess.add(NoIntersectionProcess.get(selectIndex - 1));
            }
        } else {
            this.ProcessSelectMode = -1;
            int selectIndex = new Random().nextInt(this.UncodedProcess.size());
            this.CurrentProcess.add(this.UncodedProcess.get(selectIndex));
        }

        return flag;
    }

    /**
     * 为 CurrentProcess 中的工序选择机器，这里用到了规则确定每台机器被选择的概率 
     * @return Map<Integer, Integer>
     * 
     * <p>返回类型 Map<\Integer, Integer>\，key：工序编号，value：选择的机器</p>
     */
    public void selectMachine() {
        Map<Integer, double[]> OptMachineProb = new HashMap<Integer, double[]>();   // 记录每道工序的每台可选机器的概率
        Map<Integer, Integer> OptMachine = new HashMap<>();                         // 记录每道工序选择的机器

        // 选择机器
        for (Integer i : this.CurrentProcess) {
            ArrayList<Integer> optmachineset = new ArrayList<>(this.OptMachineSet.get(i));
            OptMachine.put(i, optmachineset.get(new Random().nextInt(optmachineset.size())));
        }
        this.OptMachine = OptMachine;

        // // 计算每台机器的选择概率
        // for (Integer process : this.CurrentProcess) {
        //     int machineNum = this.OptMachineSet.get(process).size();    // 工序可选机器数量
        //     double[] probability = new double[machineNum];              // 记录每台机器的概率
        //     double basicProbability = 1d / machineNum;                  // 记录每台机器的初始概率（均分）
        //     Arrays.fill(probability, basicProbability);                 // 均匀初始化每台机器的概率
        //     for (int i = 0; i < machineNum; i++) {
        //         // 向左侧搜索
        //         for (int j = process - 1; j > 0; j--) {
        //             if (this.OptMachineSet.get(j).contains(this.OptMachineSet.get(process).get(i))) {
        //                 probability[i] += basicProbability * Math.pow(0.5, process - j);
        //             } else {
        //                 break;
        //             }
        //         }
        //         // 向右侧搜索
        //         for (int j = process + 1; j <= this.OptMachineSet.size(); j++) {
        //             if (this.OptMachineSet.get(j).contains(this.OptMachineSet.get(process).get(i))) {
        //                 probability[i] += basicProbability * Math.pow(0.5, j - process);
        //             } else {
        //                 break;
        //             }
        //         }
        //     }
        //     OptMachineProb.put(process, probability);
        // }

        // // 将概率处理到和为 1 
        // for (Map.Entry<Integer, double[]> entry : OptMachineProb.entrySet()) {
        //     double sumProb = 0;     // 概率和
        //     double[] probability = new double[entry.getValue().length];

        //     for (double d : entry.getValue()) {
        //         sumProb += d;
        //     }
        //     for (int i = 0; i < entry.getValue().length; i++) {
        //         probability[i] = entry.getValue()[i] / sumProb;
        //     }
        //     entry.setValue(probability);
        // }

        // // 根据概率选择机器
        // for (Map.Entry<Integer, double[]> entry : OptMachineProb.entrySet()) {
        //     double p = new Random().nextDouble();
        //     for (int i = 0; i < entry.getValue().length; i++) {
        //         p -= entry.getValue()[i];
        //         if (p <= 0) {
        //             OptMachine.put(entry.getKey(), this.OptMachineSet.get(entry.getKey()).get(i));
        //             break;
        //         }
        //     }
        // }
        // this.OptMachine = OptMachine;
    }

    /**
     * 实时更新可选机器集合
     */
    public void UpdateOptMachineSet() {
        // 为随机选择和无交集工序删除未被选择的机器
        if ((this.ProcessSelectMode == -1) || (this.ProcessSelectMode == 1)) {
            for (Integer integer : this.CurrentProcess) {
                this.OptMachineSet.get(integer).clear();
                this.OptMachineSet.get(integer).add(this.OptMachine.get(integer));
            }
        }
        // 遍历已编码的工序，删除不连续工序的可选机器集合中被已编码工序选择的机器
        for (int processIndex = 0; processIndex < this.ConfigurationCode.size(); processIndex++) {
            int machine = this.ConfigurationCode.get(processIndex);  // 工序所选择的机器编号
            if (machine > 0) {
                // 向左搜索，直到找到第一个包含 machine 但不连续且未编码的工序 i
                for (int i = processIndex - 1; i > 0; i--) {
                    if (! this.OptMachineSet.get(i + 1).contains(machine)) {
                        // 将 i 左侧所有工序的 machine 都删除
                        for (int j = i - 1; j >= 0; j--) {
                            if (this.OptMachineSet.get(j + 1).contains(machine) && this.ConfigurationCode.get(j) == -1) {
                            // 由于 ArrayList.remove() 存在 remove(Object o) 和 remove(int i) 两种方法，此处直接传入 machine 会被识别为后者，因此用 this.OptMachine.get(integer) 代替 machine
                            this.OptMachineSet.get(j + 1).remove(this.OptMachine.get(processIndex + 1));
                            }
                        }
                        break;
                    }
                }
                // 向右搜索
                for (int i = processIndex + 1; i < this.OptMachineSet.size(); i++) {
                    if (! this.OptMachineSet.get(i + 1).contains(machine)) {
                        for (int j = i + 1; j < this.OptMachineSet.size(); j++) {
                            if (this.OptMachineSet.get(j + 1).contains(machine) && this.ConfigurationCode.get(j) == -1) {
                                this.OptMachineSet.get(j + 1).remove(this.OptMachine.get(processIndex + 1));
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * （不存在替代机器）判断是否存在选择相同机器的工序，若是，判断是否可以对两个工序间所有的工序选择同一台机器
     * @return
     */
    public boolean isSameChoice() {
        boolean flag = false;

        // 遍历编码，对已编码的工序进行检查
        for (int i = 0; i < this.ConfigurationCode.size() - 1; i++) {
            // 如果工序已编码（不等于-1），则查找余下工序中是否有相同编码的工序
            if (this.ConfigurationCode.get(i) != -1) {
                for (int j = i + 1; j < this.ConfigurationCode.size(); j++) {
                    // 若存在相同编码的工序，判断两个工序之间的所有工序是否都可以选择该机器，如果可以，选择
                    if (this.ConfigurationCode.get(i) == this.ConfigurationCode.get(j)) {
                        int machine = this.ConfigurationCode.get(i);
                        this.OptMachine = new HashMap<Integer, Integer>();
                        this.CurrentProcess = new ArrayList<Integer>();
                        for (int j2 = i + 1; j2 < j; j2++) {
                            if (this.ConfigurationCode.get(j2) != -1) {
                                this.isNoSolution = true;
                                break;
                            } else {
                                flag = true;
                                this.CurrentProcess.add(j2 + 1);
                                this.OptMachine.put(j2 + 1, machine);
                                this.ConfigurationCode.set(j2, machine);
                                // 将 j2 实例化，实现 remove(objcet o) 方法，避免 remove(int i)
                                this.UncodedProcess.remove(this.CurrentProcess.get(j2 - i - 1));
                                this.ProcessSelectMode = -1;
                            }
                        }
                        break;
                    }
                }
            }
            // 如果已找到一对机器相同的工序，则不再继续搜索
            if (flag) {
                break;
            }
        }

        return flag;
    }

    /**
     * （存在替代机器）
     * @param AlternativeMachine
     * @return
     */
    public boolean isSameChoice(Map<Integer, ArrayList<Integer>> AlternativeMachine) {
        boolean flag = false;

        // 遍历编码，对已编码的工序进行检查
        for (int i = 0; i < this.ConfigurationCode.size() - 1; i++) {
            // 如果工序已编码（不等于-1），则查找余下工序中是否有相同编码的工序
            if (this.ConfigurationCode.get(i) != -1) {
                for (int j = i + 1; j < this.ConfigurationCode.size(); j++) {
                    // 若存在相同编码的工序，判断两个工序之间的所有工序是否都可以选择该机器，如果可以，选择
                    if (this.ConfigurationCode.get(i) == this.ConfigurationCode.get(j)) {
                        int machine = this.ConfigurationCode.get(i);
                        this.OptMachine = new HashMap<Integer, Integer>();
                        this.CurrentProcess = new ArrayList<Integer>();
                        for (int j2 = i + 1; j2 < j; j2++) {
                            if (this.ConfigurationCode.get(j2) == -1) {
                                // 当中间的工序尚未编码时，判断是否可以选择相同的机器
                                if (this.OptMachineSet.get(j2 + 1).contains(machine)) {
                                    flag = true;
                                    this.CurrentProcess.add(j2 + 1);
                                    this.OptMachine.put(j2 + 1, machine);
                                    this.ConfigurationCode.set(j2, machine);
                                    // 将 j2 实例化，实现 remove(objcet o) 方法，避免 remove(int i)
                                    this.UncodedProcess.remove(Integer.valueOf(j2 + 1));
                                    this.ProcessSelectMode = -1;
                                } else {
                                    // 否则判断是否可以替代
                                    for (Integer alter : AlternativeMachine.get(machine)) {
                                        if (this.ConfigurationCode.contains(alter) || (! this.OptMachineSet.get(j2 + 1).contains(machine))) {
                                            this.isNoSolution = true;
                                        } else {
                                            flag = true;
                                            this.isNoSolution = false;
                                            List<Integer> list = new ArrayList<Integer>();
                                            list.add(i);
                                            list.add(j);
                                            int index = new Random().nextInt(2);
                                            this.CurrentProcess.add(list.get(index)+1);
                                            this.OptMachine.put(list.get(index)+1, alter);
                                            this.ConfigurationCode.set(list.get(index), alter);
                                            this.ProcessSelectMode = -1;
                                            break;
                                        }
                                    }
                                }
                            } else {
                                // 当已编码时，遍历工序 i 的可替代机器，若可替代机器未被选择，则替代
                                for (Integer alter : AlternativeMachine.get(machine)) {
                                    if (this.ConfigurationCode.contains(alter)) {
                                        this.isNoSolution = true;
                                    } else {
                                        flag = true;
                                        this.isNoSolution = false;
                                        List<Integer> list = new ArrayList<Integer>();
                                        list.add(i);
                                        list.add(j);
                                        int index = new Random().nextInt(2);
                                        this.CurrentProcess.add(list.get(index)+1);
                                        this.OptMachine.put(list.get(index)+1, alter);
                                        this.ConfigurationCode.set(list.get(index), alter);
                                        this.ProcessSelectMode = -1;
                                        break;
                                    }
                                } 
                            }
                        }
                        break;
                    }
                }
            }
            // // 如果已找到一对机器相同的工序，则不再继续搜索
            // if (flag) {
            //     break;
            // }
        }

        return flag;
    }

    /**
     * 判断是否有未选择机器但可选机器集合为空集的工序，若有，说明该解不成立，需要重新寻找解；判断上轮迭代是否无解，即 this.isNoSolution 是否为 true ，若是，也重新寻找
     * @return boolean
     */
    public boolean isNoSolution() {
        if (this.isNoSolution) {
            return true;
        } else {
            for (Integer integer : this.UncodedProcess) {
                if (this.OptMachineSet.get(integer).isEmpty()) {
                    this.isNoSolution = true;
                }
            }
            return this.isNoSolution;
        }
    }

    /**
     * 终止条件判读，当编码完成时终止，即 this.ConfigurationCode 中不存在 -1
     * @return boolean
     */
    public boolean TerminateIteration() {
        boolean flag = true;

        for (Integer integer : this.ConfigurationCode) {
            if (integer == -1) {
                flag = false;
                break;
            }
        }

        return flag;
    }

    /**
     * 缩短配置编码，删除现有编码的重复数字，即将 [1,1,1,3,3,5] 输出为 [1,3,5]
     * @return ArrayList<\Integer\>
     */
    public void undateConfigurationCode() {
        ArrayList<Integer> list = new ArrayList<Integer>();

        for (Integer integer : this.ConfigurationCode) {
            if (! list.contains(integer)) {
                list.add(integer);
            }
        }
        this.ConfigurationCode = list;
    }

    /**
     * 获取配置编码
     * @return
     */
    public ArrayList<Integer> getConfigurationCode() {
        return this.ConfigurationCode;
    }
    /**
     * 获取当前在编码的工序
     * @return List<\Integer\>
     */
    public List<Integer> getCurrentProcess() {
        return this.CurrentProcess;
    }
    /**
     * 获取当前的工序可选合集
     * @return Map<\Integer, ArrayList<\Integer\>\>
     */
    public Map<Integer, ArrayList<Integer>> OptMachineSet() {
        return this.OptMachineSet;
    }
    /**
     * 获取当前未编码的工序
     * @return ArrayList<\Integer\>
     */
    public ArrayList<Integer> getUncodedProcess() {
        return this.UncodedProcess;
    }
    /**
     * 获取当前工序选择的机器
     * @return
     */
    public Map<Integer, Integer> getOptMachine() {
        return this.OptMachine;
    }
    /**
     * 获取当前选择工序的模式
     * @return
     */
    public int getProcessSelectMode() {
        return this.ProcessSelectMode;
    }
}
