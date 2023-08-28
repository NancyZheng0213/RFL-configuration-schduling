package cn.nancy.scheduling_of_rfl;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class Pop {
    /**
     * population size
     */
    private int popsize;
    /**
     * population
     */
    private Individual[] individuals;

    public Pop(int popsize) {
        this.popsize = popsize;
    }

    /**
     * 编码，子类需覆写
     * @param Machine 每台机器对应的可加工操作
     * @param ProcessNum 工序总数
     * @param Process 每个工件对应的工艺流程
     * @param AlternativeMachine 可替代机器集合，记录每台机器相同类型的可以互相替代的机器编号
     */
    public void encode(int PartsNum, Map<Integer, ArrayList<Integer>> Machine, int ProcessNum, Map<Integer, ArrayList<Integer>> Process, Map<Integer, ArrayList<Integer>> AlternativeMachine) {
        for (int i = 0; i < this.popsize; i++) {
            // 编码开始
            Encode encoding = new Encode(PartsNum, Machine, ProcessNum, Process, AlternativeMachine);
            this.individuals[i].getCode().setConfigurationCode(encoding.getCode().getConfigurationCode());
            this.individuals[i].getCode().setOperationCode(encoding.getCode().getOperationCode());
            this.individuals[i].getCode().setSortCode(encoding.getCode().getSortCode());
        }
        
    }

    /**
     * 解码
     * @param MachineTime 每台机器对应的可加工操作的操作时间
     * @param Demand 工件需求量
     * @param SetupTime 每个操作的切换时间
     * @param DueDays 工件交付时间
     */
    public void decode(Map<Integer, TreeMap<Integer, Double>> MachineTime, Map<Integer, Integer> Demand, Map<Integer, Double> SetupTime, Map<Integer, Integer> DueDays) {
        for (int i = 0; i < this.popsize; i++) {
            // 解码
            this.individuals[i].getDecode().setdecoding(this.individuals[i].getCode(), MachineTime, Demand, SetupTime);
            // 计算目标函数值
            this.individuals[i].getDecode().utilization(Demand.size());
            this.individuals[i].getDecode().totaldelay(this.individuals[i].getCode().getSortCode(), DueDays);
        }
    }

    // SET methods
    public void setIndividuals(Individual[] individuals) {
        this.individuals = individuals;
    }
    public void setIndividual(int i, Individual individual) {
        this.individuals[i] = individual;
    }
    public void deepsetIndividual(int i, Individual individual) {
        this.individuals[i] = new Individual(individual);
    }

    // GET methods
    public int getPopsize() {
        return this.popsize;
    }
    public Individual[] getIndividuals() {
        return this.individuals;
    };
    public Individual getIndividual(int i) {
        return this.individuals[i];
    };
}
