package cn.nancy.scheduling_of_rfl.nsgaii;

import java.util.ArrayList;
import cn.nancy.scheduling_of_rfl.Pop;

/**
 * Pop
 */
public class PopofNSGAII extends Pop{
    // /**
    //  * population
    //  */
    // private IndividualofNSGAII[] individuals;
    
    /*
     * initialization of population
     */
    public PopofNSGAII(int popsize) {
        super(popsize);
        IndividualofNSGAII[] individuals = new IndividualofNSGAII[popsize];
        for (int i = 0; i < popsize; i++) {
            individuals[i] = new IndividualofNSGAII();
        }
        setIndividuals(individuals);
    }

    // /**
    //  * 编码
    //  * @param Machine 每台机器对应的可加工操作
    //  * @param ProcessNum 工序总数
    //  * @param Process 每个工件对应的工艺流程
    //  * @param AlternativeMachine 可替代机器集合，记录每台机器相同类型的可以互相替代的机器编号
    //  */
    // public void encode(int PartsNum, Map<Integer, ArrayList<Integer>> Machine, int ProcessNum, Map<Integer, ArrayList<Integer>> Process, Map<Integer, ArrayList<Integer>> AlternativeMachine) {
    //     for (int i = 0; i < this.popsize; i++) {
    //         // 编码开始
    //         Encode encoding = new Encode(PartsNum, Machine, ProcessNum, Process, AlternativeMachine);
    //         this.individuals[i].getCode().setConfigurationCode(encoding.getCode().getConfigurationCode());
    //         this.individuals[i].getCode().setOperationCode(encoding.getCode().getOperationCode());
    //         this.individuals[i].getCode().setSortCode(encoding.getCode().getSortCode());
    //     }
        
    // }

    // /**
    //  * 解码
    //  * @param MachineTime 每台机器对应的可加工操作的操作时间
    //  * @param Demand 工件需求量
    //  * @param SetupTime 每个操作的切换时间
    //  * @param DueDays 工件交付时间
    //  */
    // public void decode(Map<Integer, TreeMap<Integer, Double>> MachineTime, Map<Integer, Integer> Demand, Map<Integer, Double> SetupTime, Map<Integer, Integer> DueDays) {
    //     for (int i = 0; i < this.popsize; i++) {
    //         // 解码
    //         this.individuals[i].getDecode().setdecoding(this.individuals[i].getCode(), MachineTime, Demand, SetupTime);
    //         // 计算目标函数值
    //         this.individuals[i].getDecode().utilization(Demand.size());
    //         this.individuals[i].getDecode().totaldelay(this.individuals[i].getCode().getSortCode(), DueDays);
    //     }
    // }

    // SET methods
    /**
     * set the <i>i</i>th individual
     * 
     * <p> Different from the <code>setIndividual(int i, Individual individual)</code>, 
     * the individual here is class <code>IndividualofNSGAII</code></P>
     * @param i
     * @param individual
     */
    public void setIndividual(int i, IndividualofNSGAII individual) {
        super.setIndividual(i, individual); 
    }
    /**
     * deep copy the <i>i</i>th individual
     * 
     * <p>Different from the <code>deepsetIndividual(int i, Individual individual)</code>, 
     * the individual here is class <code>IndividualofNSGAII</code></p>
     * @param i
     * @param individual
     */
    public void deepsetIndividual(int i, IndividualofNSGAII individual) {
        super.setIndividual(i, new IndividualofNSGAII(individual));
    }
    public void setN(int i, int n) {
        getIndividual(i).setN(n);
    }
    public void setS(int i, ArrayList<Integer> S) {
        getIndividual(i).setS(S);
    }
    public void setR(int i, int n) {
        getIndividual(i).setR(n);
    }
    public void setD(int i, int n) {
        getIndividual(i).setD(n);
    }

    // GET methods
    @Override
    public IndividualofNSGAII[] getIndividuals() {
        return (IndividualofNSGAII[])super.getIndividuals();
    }
    @Override
    public IndividualofNSGAII getIndividual(int i) {
        return (IndividualofNSGAII)super.getIndividual(i);
    }
}