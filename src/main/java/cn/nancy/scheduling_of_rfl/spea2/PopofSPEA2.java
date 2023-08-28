package cn.nancy.scheduling_of_rfl.spea2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import cn.nancy.scheduling_of_rfl.Pop;

/**
 * 
 */
public class PopofSPEA2 extends Pop {
    // /**
    // * population
    // */
    // private IndividualofSPEA2[] individuals;

    public PopofSPEA2(int popsize) {
        super(popsize);
        IndividualofSPEA2[] individuals = new IndividualofSPEA2[popsize];
        for (int i = 0; i < popsize; i++) {
            individuals[i] = new IndividualofSPEA2();
        }
        setIndividuals(individuals);
    }

    // /**
    // * 编码
    // * @param Machine 每台机器对应的可加工操作
    // * @param ProcessNum 工序总数
    // * @param Process 每个工件对应的工艺流程
    // * @param AlternativeMachine 可替代机器集合，记录每台机器相同类型的可以互相替代的机器编号
    // */
    // public void encode(int PartsNum, Map<Integer, ArrayList<Integer>> Machine,
    // int ProcessNum, Map<Integer, ArrayList<Integer>> Process, Map<Integer,
    // ArrayList<Integer>> AlternativeMachine) {
    // for (int i = 0; i < this.popsize; i++) {
    // // 编码开始
    // EncodeofSPEA2 encoding = new EncodeofSPEA2(PartsNum, Machine, ProcessNum,
    // Process, AlternativeMachine);
    // this.individuals[i].getCode().setConfigurationCode(encoding.getCode().getConfigurationCode());
    // this.individuals[i].getCode().setOperationCode(encoding.getCode().getOperationCode());
    // this.individuals[i].getCode().setSortCode(encoding.getCode().getSortCode());
    // }

    // }

    // /**
    // * 解码
    // * @param MachineTime 每台机器对应的可加工操作的操作时间
    // * @param Demand 工件需求量
    // * @param SetupTime 每个操作的切换时间
    // * @param DueDays 工件交付时间
    // */
    // public void decode(Map<Integer, TreeMap<Integer, Double>> MachineTime,
    // Map<Integer, Integer> Demand, Map<Integer, Double> SetupTime, Map<Integer,
    // Integer> DueDays) {
    // for (int i = 0; i < this.popsize; i++) {
    // // 解码
    // this.individuals[i].getDecode().setdecoding(this.individuals[i].getCode(),
    // MachineTime, Demand, SetupTime);
    // // 计算目标函数值
    // this.individuals[i].getDecode().utilization(Demand.size());
    // this.individuals[i].getDecode().totaldelay(this.individuals[i].getCode().getSortCode(),
    // DueDays);
    // }
    // }

    /**
     * 根据每个个体的 fitness 进行排序
     * @return void
     */
    public ArrayList<IndividualofSPEA2> SortFitness() {
        ArrayList<IndividualofSPEA2> list = new ArrayList<>(
                Arrays.asList((IndividualofSPEA2[]) super.getIndividuals()));
        Collections.sort(list, new Comparator<IndividualofSPEA2>() {
            @Override
            public int compare(IndividualofSPEA2 individual1, IndividualofSPEA2 individual2) {
                return individual1.getFitness() > individual2.getFitness() ? 1
                        : (individual1.getFitness() == individual2.getFitness() ? 0 : -1);
            }
        });

        return list;
    }

    // SET methods
    /**
     * set the <i>i</i>th individual
     * 
     * <p> Different from the <code>setIndividual(int i, Individual individual)</code>, 
     * the individual here is class <code>IndividualofSPEA2</code></P>
     * @param i
     * @param individual
     */
    public void setIndividual(int i, IndividualofSPEA2 individual) {
        super.setIndividual(i, individual);
    }
    /**
     * deep copy the <i>i</i>th individual
     * 
     * <p>Different from the <code>deepsetIndividual(int i, Individual individual)</code>, 
     * the individual here is class <code>IndividualofSPEA2</code></p>
     * @param i
     * @param individual
     */
    public void deepsetIndividual(int i, IndividualofSPEA2 individual) {
        super.setIndividual(i, new IndividualofSPEA2(individual));
    }
    public void setS(int i, int n) {
        getIndividual(i).setS(n);
    }
    public void setD(int i, double n) {
        getIndividual(i).setD(n);
    }
    public void setR(int i, int n) {
        getIndividual(i).setR(n);
    }
    public void setFitness(int i, double fitness) {
        getIndividual(i).setFitness(fitness);
    }

    // GET methods
    @Override
    public IndividualofSPEA2[] getIndividuals() {
        return (IndividualofSPEA2[]) super.getIndividuals();
    }

    @Override
    public IndividualofSPEA2 getIndividual(int i) {
        return (IndividualofSPEA2) super.getIndividual(i);
    }
}
