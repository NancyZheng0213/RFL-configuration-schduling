package cn.nancy.scheduling_of_rfl.nsgaii;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class EncodingOP {
    /**
     * 操作编码，采用整数编码，如 [1,1,1,3,3,5] 表示第一个工序在工位 1 上加工，第二个工序在工位 3 上加工
     */
    private TreeMap<Integer, Integer> OperationCode;
    /**
     * 存储当前工件的可选工位集合，实时更新，如 {5: [2,3,4]} 表示第一道工序是操作 5 ，可以在工位 2、3、4 上加工
     */
    private Map<Integer, ArrayList<Integer>> OptStageSet;
    /**
     * 记录未编码的工序，如 [2,3] 表示工件还有两个工序未编码
     */
    private ArrayList<Integer> UncodedProcess;
    /**
     * 记录当前工件正在编码的工序，如 [2,3] 表示目前的工件正编码两个工序，分别是 2、3 
     */
    private ArrayList<Integer> CurrentProcess;
    /**
     * 记录当前编码工件的选择工位
     */
    private Map<Integer, Integer> OptStage;
    /**
     * 记录当前工位的选择模式，-1：随机选择，0：单个工位
     */
    private int StageSelectMode;
    private boolean isNoSolution;

    
    /**
     * 初始化
     * @param workNum 工位数量
     * @param OptStageSet 所有操作的可选工位集合
     * @param ProcessOfP 工件 p 的工艺流程记录
     */
    EncodingOP(int workNum, Map<Integer, ArrayList<Integer>> OptStageSet, ArrayList<Integer> ProcessOfP) {
        // 属性初始化
        this.OperationCode = new TreeMap<Integer, Integer>();
        this.UncodedProcess = new ArrayList<Integer>();
        this.CurrentProcess = new ArrayList<Integer>();
        this.OptStage = new HashMap<Integer, Integer>();
        this.OptStageSet = new HashMap<Integer, ArrayList<Integer>>();
        this.StageSelectMode = -1;
        this.isNoSolution = false;
        this.UncodedProcess.addAll(ProcessOfP);
        for (Integer integer : ProcessOfP) {
            this.OperationCode.put(integer, -1);
        }
        initOptStageSet(workNum, OptStageSet, ProcessOfP);
    }

    /**
     * 针对一个工件初始化 OptStageSet
     * @param workNum 工位数量
     * @param OptStageSet 所有操作的可选工位集合
     * @param ProcessOfP 工件 p 的工艺流程记录
     */
    public void initOptStageSet(int workNum, Map<Integer, ArrayList<Integer>> OptStageSet, ArrayList<Integer> ProcessOfP) {
        // 找出工件 p 的每个工序的可用工位
        Map<Integer, ArrayList<Integer>> map = new HashMap<Integer, ArrayList<Integer>>();  // 记录工件 p 每道工序的可用工位
        int processSize = ProcessOfP.size();                                                // 记录工件 p 的工序总数
        for (int processIndex = 0; processIndex < processSize; processIndex++) {
            int process = ProcessOfP.get(processIndex);                                     // 记录工件 p 的第 processIndex 道工序的编号
            ArrayList<Integer> worklist = new ArrayList<Integer>(OptStageSet.get(process)); // 记录工件 p 工序 process 的可用工位
            // // 注释开始*************************************************************************
            // // 应用规则，缩小集合范围
            // if (processIndex + 1 <= processSize - 3) {  // 当操作序号小于等于 processSize - 3，删去可选工位中大于 workNum - 1 的
            //     for (Integer integer : OptStageSet.get(process)) {
            //         if (integer > workNum - 1) {
            //             worklist.remove(integer);
            //         }
            //     }
            // }
            // if (processIndex + 1 > 3) {                 // 当操作序号大于 3 ，删去可选工位中小于等于 1 的
            //     for (Integer integer : OptStageSet.get(process)) {
            //         if (integer <= 1) {
            //             worklist.remove(integer);
            //         }
            //     }
            // }
            // // 注释结束*************************************************************************
            map.put(process, worklist);
        }
        this.OptStageSet.putAll(map);
    }

    /**
     * 选择工件 p 的下一个编码工序，并为其选择工位，将结果存储在 this.OptStage 中，更新 this.OperationCode 和 this.UncodedProcess
     * @param ProcessOfP
     */
    public void setOptStage(ArrayList<Integer> ProcessOfP) {
        // 初始化记录
        this.CurrentProcess = new ArrayList<>();
        this.OptStage = new HashMap<>();

        // 寻找 p 是否有单个可选工位的工序，若无，随机选择工序
        if (SearchWithOneStage()) {
            this.StageSelectMode = 0;
        } else {
            // 随机选择工序
            this.StageSelectMode = -1;
            int selectIndex = new Random().nextInt(this.UncodedProcess.size());
            int process = this.UncodedProcess.get(selectIndex);
            this.CurrentProcess.add(process);
            // 为工序随机选择工位
            selectIndex = new Random().nextInt(this.OptStageSet.get(process).size());
            this.OptStage.put(process, this.OptStageSet.get(process).get(selectIndex));
        }
        // 更新记录
        for (Map.Entry<Integer, Integer> entry : this.OptStage.entrySet()) {
            this.OperationCode.put(entry.getKey(), entry.getValue());   // 记录工件 p 当前的编码
            this.UncodedProcess.remove(entry.getKey());
        }
    }

    /**
     * 对工件 p 的每道工序进行判断，寻找是否有仅有一个工位可选的工序，若有，将其记录在 this.CurrentProcess 中，并为工序选择工位，记录在 this.OptStage 中
     * @return boolean
     */
    public boolean SearchWithOneStage() {
        boolean flag = false;

        // 遍历工件 p 的每一道未编码工序，判断该工序是否仅有一个工位可以选择
        for (Integer process : this.UncodedProcess) {
            if (this.OptStageSet.get(process).size() == 1) {
                flag = true;
                this.CurrentProcess.add(process);
                this.OptStage.put(process, this.OptStageSet.get(process).get(0));
            }
        }

        return flag;
    }

    public void UpdateOptStageSet() {
        // 为随机选择的工序删除未被选择的工位
        if (this.StageSelectMode == -1) {
            for (Integer integer : this.CurrentProcess) {
                this.OptStageSet.get(integer).clear();
                this.OptStageSet.get(integer).add(this.OptStage.get(integer));
            }
        }
        // 删除当前工序的后续集合中小于已选择工位的选项，删除前端集合中大于已选择工位的选项
        for (Integer integer : this.CurrentProcess) {
            for (Map.Entry<Integer, ArrayList<Integer>> entry : this.OptStageSet.entrySet()) {
                if (entry.getKey() < integer) {
                    // 用遍历器迭代，否则会出现删除后实际长度与记录长度不一致的问题，参考https://blog.csdn.net/qq_35056292/article/details/79751233#:~:text=java.%20util.%20ConcurrentModificationException%20is%20a%20very%20common%20exception,whi...%20Caused%20by%3A%20java.%20util.%20ConcurrentModificationException%20%E5%B9%B6%E5%8F%91%E4%BF%AE%E6%94%B9%20%E5%BC%82%E5%B8%B8.
                    for (Iterator<Integer> iterator = entry.getValue().iterator(); iterator.hasNext();) {
                        Integer stage = iterator.next();
                        if (stage > this.OptStage.get(integer)) {
                            iterator.remove();
                        }
                    }
                } else {
                    for (Iterator<Integer> iterator = entry.getValue().iterator(); iterator.hasNext(); ) {
                        Integer stage = iterator.next();
                        if (stage < this.OptStage.get(integer)) {
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }

    /**
     * 判断是否可选工位集合为空集的工序，若有，说明该解不成立，需要重新寻找解；判断上轮迭代是否无解，即 this.isNoSolution 是否为 true ，若是，也重新寻找
     * @return boolean
     */
    public boolean isNoSolution() {
        if (this.isNoSolution) {
            return true;
        } else {
            for (Map.Entry<Integer, ArrayList<Integer>> entry : this.OptStageSet.entrySet()) {
                if (entry.getValue().isEmpty()) {
                    this.isNoSolution = true;
                    break;
                }
            }
            return this.isNoSolution;
        }
    }


    /**
     * 终止条件判读，当编码完成时终止，即 this.UncodedProcess 为空
     * @return
     */
    public boolean TerminateIteration() {
        boolean flag = true;

        for (Iterator<Map.Entry<Integer, Integer>> iterator = this.OperationCode.entrySet().iterator(); iterator.hasNext();) {
            if (iterator.next().getValue() == -1) {
                flag = false;
                break;
            }
        }

        return flag;
    }


    /**
     * 获取当前正在编码的工序
     * @return
     */
    public ArrayList<Integer> getCurrentProcess() {
        return this.CurrentProcess;
    }
    /**
     * 获取操作编码
     * @return
     */
    public TreeMap<Integer, Integer> getOperationCode() {
        return this.OperationCode;
    }
    /**
     * 获取当前编码工序所选择的工位
     * @return
     */
    public Map<Integer, Integer> getOptStage() {
        return this.OptStage;
    }
    /**
     * 获取实时的可选工位集合
     * @return
     */
    public Map<Integer, ArrayList<Integer>> getOptStageSet() {
        return this.OptStageSet;
    }
    /**
     * 获取当前选择模式
     * @return
     */
    public int getStageSelectMode() {
        return this.StageSelectMode;
    }
    /**
     * 获取当前未编码的工序
     * @return
     */
    public ArrayList<Integer> getUncodedProcess() {
        return this.UncodedProcess;
    }

}