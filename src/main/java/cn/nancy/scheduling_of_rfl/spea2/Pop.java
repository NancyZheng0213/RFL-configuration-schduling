package cn.nancy.scheduling_of_rfl.spea2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class Pop {
    /**
     * 种群规模
     */
    private int popNum;
    /**
     * 种群
     */
    private Individual[] individuals;

    public Pop(int popNum) {
        this.popNum = popNum;
        this.individuals = new Individual[popNum];
        for (int i = 0; i < popNum; i++) {
            this.individuals[i] = new Individual();
        }
    }

    /**
     * 编码
     * @param Machine 每台机器对应的可加工操作
     * @param ProcessNum 工序总数
     * @param Process 每个工件对应的工艺流程
     * @param AlternativeMachine 可替代机器集合，记录每台机器相同类型的可以互相替代的机器编号
     */
    public void encode(int PartsNum, Map<Integer, ArrayList<Integer>> Machine, int ProcessNum, Map<Integer, ArrayList<Integer>> Process, Map<Integer, ArrayList<Integer>> AlternativeMachine) {
        for (int i = 0; i < this.popNum; i++) {
            // 编码开始
            Encode encoding = new Encode(PartsNum, Machine, ProcessNum, Process, AlternativeMachine);
            this.individuals[i].getcode().setConfigurationCode(encoding.getCode().getConfigurationCode());
            this.individuals[i].getcode().setOperationCode(encoding.getCode().getOperationCode());
            this.individuals[i].getcode().setSortCode(encoding.getCode().getSortCode());
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
        for (int i = 0; i < this.popNum; i++) {
            // 解码
            this.individuals[i].getdecode().setdecoding(this.individuals[i].getcode(), MachineTime, Demand, SetupTime);
            // 计算目标函数值
            this.individuals[i].getdecode().utilization(Demand.size());
            this.individuals[i].getdecode().totaldelay(this.individuals[i].getcode().getSortCode(), DueDays);
        }
    }

    /**
     * 根据每个个体的 fitness 进行排序
     */
    public ArrayList<Individual> SortFitness() {
        ArrayList<Individual> list = new ArrayList<>(Arrays.asList(this.individuals));
        Collections.sort(list, new Comparator<Individual>() {
            @Override
            public int compare(Individual individual1, Individual individual2) {
                return individual1.getFitness() > individual2.getFitness() ? 1 : (individual1.getFitness() == individual2.getFitness() ? 0 : -1);
            }
        });

        return list;
    }

    public void setIndividual(int i, Individual individual) {
        this.individuals[i] = individual;
    }
    public void deepsetIndividual(int i, Individual individual) {
        this.individuals[i] = new Individual(individual);
    }
    public void setS(int i, int n) {
        // this.S[i] = n;
        this.individuals[i].setS(n);
    }
    public void setD(int i, double n) {
        // this.D[i] = n;
        this.individuals[i].setD(n);
    }
    public void setR(int i, int n) {
        // this.R[i] = n;
        this.individuals[i].setR(n);
    }
    public void setFitness(int i, double fitness) {
        this.individuals[i].setFitness(fitness);
    }

    public int getpopNum() {
        return this.popNum;
    }
    public Individual getindividual(int i) {
        return this.individuals[i];
    }
}
