package cn.nancy.scheduling_of_rfl.nsgaii;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import cn.nancy.scheduling_of_rfl.*;

public class NSGAII {
    /**
     * probability of PBX crossover
     */
    private double pc;
    /**
     * probability of swap mutation
     */
    private double pms;
    /**
     * probability of VNO
     */
    private double pmv;
    /**
     * population
     */
    private Pop pop;
    /**
     * sets of rank
     */
    private ArrayList<ArrayList<Integer>> F;
    private int MaxIteration;

    public NSGAII(int MaxIteration, int popsize, double pc, double pms, double pmv, Qus qus) {
        this.MaxIteration = MaxIteration;
        this.pop = new Pop(popsize);
        this.pc = pc;
        this.pms = pms;
        this.pmv = pmv;
        this.pop.encode(qus.getPartsNum(), qus.getMachine(), qus.getProcessNum(), qus.getProcess(), qus.getAlternativeMachine());
        this.pop.decode(qus.getMachineTime(), qus.getDemand(), qus.getSetupTime(), qus.getDueDays());
    }

    /**
     * fast nondominated sorting approach
     * 
     * <p>Set S and n for every individual in population, rank the population, set R for each individual.<\p>
     */
    public void NondominatedRank() {
        // set S and n for every individual in this.pop
        ResetSNR();
        for (int i = 0; i < this.pop.getPopsize(); i++) {
            Individual individuali = this.pop.getIndividual(i);
            for (int j = i+1; j < this.pop.getPopsize(); j++) {
                Individual individualj = this.pop.getIndividual(j);
                if (        // individuali dominates individualj
                    individuali.getDecode().getTotalDelay()<=individualj.getDecode().getTotalDelay() && individuali.getDecode().getUtilization()>individualj.getDecode().getUtilization() ||
                    individuali.getDecode().getTotalDelay()<individualj.getDecode().getTotalDelay() && individuali.getDecode().getUtilization()>=individualj.getDecode().getUtilization()
                ) {
                    individuali.addS(j);
                    individualj.setN(individualj.getN()+1);
                } else {
                    if (    // individualj dominates individuali
                        individualj.getDecode().getTotalDelay()<=individuali.getDecode().getTotalDelay() && individualj.getDecode().getUtilization()>individuali.getDecode().getUtilization() ||
                        individualj.getDecode().getTotalDelay()<individuali.getDecode().getTotalDelay() && individualj.getDecode().getUtilization()>=individuali.getDecode().getUtilization()
                    ) {
                        individuali.setN(individuali.getN()+1);
                        individualj.addS(i);
                    }
                }
            }
        }
        // rank the population, set R & D for each individual
        this.F = new ArrayList<>();
        ArrayList<Integer> Fset = new ArrayList<>();
        int rank = 1;
        for (int i = 0; i < this.pop.getPopsize(); i++) {
            Individual individual = this.pop.getIndividual(i);
            if (individual.getN() == 0) {
                Fset.add(i);
                individual.setR(rank);
            }
        }
        F.add(Fset);
        ComparisonFor(Fset);
        while (Fset.size() > 0) {
            Fset = new ArrayList<>();
            rank++;
            for (Integer i : F.get(rank-2)) {
                for (Integer j : this.pop.getIndividual(i).getS()) {
                    Individual individual = this.pop.getIndividual(j);
                    individual.setN(individual.getN()-1);
                    if (individual.getN() == 0) {
                        Fset.add(j);
                        individual.setR(rank);
                    }
                }
            }
            if (Fset.size() == 0) {
				break;
			}
            F.add(Fset);
            ComparisonFor(Fset);
        }
    }

    /**
     * reset the R & N & S for every individual in the pop
     */
    private void ResetSNR() {
        for (Individual individual : this.pop.getIndividuals()) {
            individual.setD(0);
            individual.setN(0);
            individual.setR(0);
            individual.getS().clear();
        }
    }


    /**
     * crowding distance-based comparison operator (for a rank)
     * @param rank the individuals group of one rank
     * @return ArrayList<Individual>
     */
    public ArrayList<Individual> Comparison(ArrayList<Individual> rank) {

        ArrayList<Individual> Fset = new ArrayList<>(rank);
//        // sort total delays in ascending order 
//        Collections.sort(Fset, new Comparator<Individual>() {
//            @Override
//            public int compare(Individual i, Individual j) {
//                return i.getDecode().getTotalDelay() - j.getDecode().getTotalDelay();
//            }
//        });
//        int minT = Fset.get(0).getDecode().getTotalDelay();
//        int maxT = Fset.get(Fset.size()-1).getDecode().getTotalDelay();
//        Fset.get(0).setD(Double.MAX_VALUE);
//        Fset.get(Fset.size()-1).setD(Double.MAX_VALUE);
//        if (minT == maxT) {
//            for (int i = 1; i < Fset.size()-1; i++) {
//                Fset.get(i).setD(Double.MAX_VALUE);
//            }
//        } else {
//            for (int i = 1; i < Fset.size()-1; i++) {
//                Fset.get(i).setD(Fset.get(i).getD()+(Fset.get(i+1).getDecode().getTotalDelay()-Fset.get(i-1).getDecode().getTotalDelay())/(maxT-minT));
//            }
//        }
//        // sort utilization in ascending order
//        Collections.sort(Fset, new Comparator<Individual>() {
//            @Override
//            public int compare(Individual i, Individual j) {
//                if (i.getDecode().getUtilization() < j.getDecode().getUtilization()) {
//                    return -1;
//                } else if(i.getDecode().getUtilization() == j.getDecode().getUtilization()) {
//                    return 0;
//                } else {
//                    return 1;
//                }
//            }
//        });
//        double maxU = Fset.get(Fset.size()-1).getDecode().getUtilization();
//        double minU = Fset.get(0).getDecode().getUtilization();
//        Fset.get(0).setD(Double.MAX_VALUE);
//        Fset.get(Fset.size()-1).setD(Double.MAX_VALUE);
//        if (maxU == minU) {
//            for (int i = 1; i < Fset.size()-1; i++) {
//                Fset.get(i).setD(Double.MAX_VALUE);
//            }
//        } else {
//            for (int i = 1; i < Fset.size()-1; i++) {
//                Fset.get(i).setD(Fset.get(i).getD()+(Fset.get(i+1).getDecode().getUtilization()-Fset.get(i-1).getDecode().getUtilization())/(maxU-minU));
//            }
//        }

        // sort crowding distance in descending order
        Collections.sort(Fset, new Comparator<Individual>() {
            @Override
            public int compare(Individual i, Individual j) {
                if (i.getD() < j.getD()) {
                    return 1;
                } else if(i.getD() == j.getD()) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });

        return Fset;
    }
    
    /**
     * crowding distance-based comparison operator (for a rank)
     * @param rank the index of individuals group of one rank
     */
    private void ComparisonFor(ArrayList<Integer> rank) {

        ArrayList<Integer> Fset = new ArrayList<>(rank);
     // sort total delays in ascending order 
        Collections.sort(Fset, new Comparator<Integer>() {
        	@Override
        	public int compare(Integer i, Integer j) {
        		return pop.getIndividual(i).getDecode().getTotalDelay() - pop.getIndividual(j).getDecode().getTotalDelay();
        	}
        });
        int minT = this.pop.getIndividual(Fset.get(0)).getDecode().getTotalDelay();
        int maxT = this.pop.getIndividual(Fset.get(Fset.size()-1)).getDecode().getTotalDelay();
        this.pop.getIndividual(Fset.get(0)).setD(Double.MAX_VALUE);
        this.pop.getIndividual(Fset.get(Fset.size()-1)).setD(Double.MAX_VALUE);
        if (minT == maxT) {
            for (int i = 1; i < Fset.size()-1; i++) {
            	this.pop.getIndividual(Fset.get(i)).setD(Double.MAX_VALUE);
            }
        } else {
            for (int i = 1; i < Fset.size()-1; i++) {
            	this.pop.getIndividual(Fset.get(i)).setD(this.pop.getIndividual(Fset.get(i)).getD()+(this.pop.getIndividual(Fset.get(i+1)).getDecode().getTotalDelay()-this.pop.getIndividual(Fset.get(i-1)).getDecode().getTotalDelay())/(maxT-minT));
            }
        }
        // sort utilization in ascending order
        Collections.sort(Fset, new Comparator<Integer>() {
            @Override
            public int compare(Integer i, Integer j) {
                if (pop.getIndividual(i).getDecode().getUtilization() < pop.getIndividual(j).getDecode().getUtilization()) {
                    return -1;
                } else if(pop.getIndividual(i).getDecode().getUtilization() == pop.getIndividual(j).getDecode().getUtilization()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        double maxU = this.pop.getIndividual(Fset.get(Fset.size()-1)).getDecode().getUtilization();
        double minU = this.pop.getIndividual(Fset.get(0)).getDecode().getUtilization();
        this.pop.getIndividual(Fset.get(0)).setD(Double.MAX_VALUE);
        this.pop.getIndividual(Fset.get(Fset.size()-1)).setD(Double.MAX_VALUE);
        if (maxU == minU) {
            for (int i = 1; i < Fset.size()-1; i++) {
                this.pop.getIndividual(Fset.get(i)).setD(Double.MAX_VALUE);
            }
        } else {
            for (int i = 1; i < Fset.size()-1; i++) {
            	this.pop.getIndividual(Fset.get(i)).setD(this.pop.getIndividual(Fset.get(i)).getD()+(this.pop.getIndividual(Fset.get(i+1)).getDecode().getUtilization()-this.pop.getIndividual(Fset.get(i-1)).getDecode().getUtilization())/(maxU-minU));
            }
        }
    }


    /**
     * Generate offspring population and perform variation
     * @param pop parent population
     * @param qus data
     */
    public Pop Variation(Qus qus) {
        // 注释开始*************************************************
        int popsize = this.pop.getPopsize();
        Pop matingpool = new Pop(popsize/2);
        // Generate offspring population from parent population by binary tournament selection
        for (int i = 0; i < popsize/2; i++) {
            Individual individual1 = this.pop.getIndividual(new Random().nextInt(popsize));
            Individual individual2 = this.pop.getIndividual(new Random().nextInt(popsize));
            if (individual1.getR() < individual2.getR()) {
                matingpool.deepsetIndividual(i, individual1);
            } else {
                if (individual2.getR() < individual1.getR()) {
                    matingpool.deepsetIndividual(i, individual2);
                } else {
                    if (individual1.getD() >= individual2.getD()) {
                        matingpool.deepsetIndividual(i, individual1);
                    } else {
                        matingpool.deepsetIndividual(i, individual2);
                    }
                }
            }
        }
        // randomly select parents to perfrom VNO, PBX, and SM for configuration code and sorting code
        Pop childPop = new Pop(popsize);
        for (int i = 0; i < popsize - 1; i = i + 2) {
            int r1 = new Random().nextInt(popsize/2);
            int r2 = new Random().nextInt(popsize/2);
            while (r1 == r2) {
                r2 = new Random().nextInt(popsize/2);
            }
            Individual parent1 = matingpool.getIndividual(r1);
            Individual parent2 = matingpool.getIndividual(r2);
            Individual child1 = new Individual(parent1);
            Individual child2 = new Individual(parent2);
            // VNO
//            if (new Random().nextDouble() < this.pmv) {
//                child1 = new Individual(VND(parent1, qus));
//                child2 = new Individual(VND(parent2, qus));
//            }
            if (new Random().nextDouble() < this.pmv) {
                ArrayList<Individual> VNDlist = VND(child1, qus);
                if (VNDlist.size() >= 2) {
                    child1 = new Individual(VNDlist.get(0));
                    child2 = new Individual(VNDlist.get(1));
                } else {
                    child1 = new Individual(VNDlist.get(0));
                    VNDlist = VND(child2, qus);
                    child2 = new Individual(VNDlist.get(0));
                }
            }
            // PBX
            if (new Random().nextDouble() < this.pc) {
                ArrayList<ArrayList<Integer>> sortingList = new ArrayList<>(
                    PBX(parent1.getCode().getSortCode(), parent2.getCode().getSortCode())
                );
                child1.getCode().setSortCode(sortingList.get(0));
                child2.getCode().setSortCode(sortingList.get(1));
            }
            // SM
            if (new Random().nextDouble() < this.pms) {
                Code childCode1 = SwapMutation(parent1.getCode());
                Code childCode2 = SwapMutation(parent2.getCode());
                child1.setCode(childCode1);
                child2.setCode(childCode2);
            }
            childPop.deepsetIndividual(i, child1);
            childPop.deepsetIndividual(i + 1, child2);
        }
        // 注释结束*************************************************

        return childPop;
    }

    /**
     * VND算子，对配置编码和操作编码部分进行交叉操作
     * 
     * @param individual
     * @param qus
     * @return ArrayList<Individual>
     */
//    private Individual VND(Individual individual, Qus qus) {
//        ArrayList<Pop> Neighborhoods = SearchNeighborhoods(individual, qus);
//        // double bestU = Double.MIN_VALUE;
//        // double bestD = Double.MAX_VALUE;
//        Individual bestneighbor = new Individual();
//        // local search by VND
//        for (int k = 0; k < Neighborhoods.size(); k++) {
//            // 找到 Neighborhood 中的最优解 bestneighbor
//            Pop Neighborhood = Neighborhoods.get(k);
//            if (Neighborhood.getPopsize() != 0) {
//                bestneighbor = Neighborhood.getIndividual(new Random().nextInt(Neighborhood.getPopsize()));
//            }
//            // for (Individual i : Neighborhood.getIndividuals()) {
//            //     if (i.getDecode().getUtilization() >= bestU && i.getDecode().getTotalDelay() <= bestD) {
//            //         bestD = i.getDecode().getTotalDelay();
//            //         bestU = i.getDecode().getUtilization();
//            //         bestneighbor = i;
//            //     }
//            // }
//        }
//
//        return bestneighbor;
//    }
    
    private ArrayList<Individual> VND(Individual individual, Qus qus) {
        ArrayList<Individual> childList = new ArrayList<Individual>();
        ArrayList<Pop> Neighborhoods = SearchNeighborhoods(individual, qus);
        // local search by VND
        for (int k = 0; k < Neighborhoods.size(); k++) {
            // 找到 Neighborhood 中的最优解 bestneighbor
            Pop Neighborhood = Neighborhoods.get(k);
            if (Neighborhood.getPopsize() == 0) {
                continue;
            } else {
                double bestU = Double.MIN_VALUE;
                double bestD = Double.MAX_VALUE;
                Individual bestneighbor = new Individual();
                for (Individual i : Neighborhood.getIndividuals()) {
                    if (i.getDecode().getUtilization() >= bestU && i.getDecode().getTotalDelay() <= bestD) {
                        bestneighbor = i;
                    }
                }
                childList.add(bestneighbor);
            }
        }

        return childList;
    }

    /**
     * 采用 PBX （Position-based Crossover） 算子执行交叉操作
     * @param code1 ArrayList<Integer>
     * @param code2 ArrayList<Integer>
     * @return list of two ArrayList<Integer>
     */
    private ArrayList<ArrayList<Integer>> PBX(ArrayList<Integer> code1, ArrayList<Integer> code2) {
        ArrayList<Integer> parent1 = new ArrayList<>(code1);
        ArrayList<Integer> parent2 = new ArrayList<>(code2);
        ArrayList<Integer> child1 = new ArrayList<>(Collections.nCopies(parent1.size(),-1));
        ArrayList<Integer> child2 = new ArrayList<>(Collections.nCopies(parent1.size(),-1));
        // 随机生成 0-1 序列，表示是否选择该基因
        ArrayList<Integer> randomtemp = new ArrayList<>();
        for (int i = 0; i < code1.size(); i++) {
            randomtemp.add(new Random().nextInt(2));
        }
        for (int i = 0; i < code1.size(); i++) {
            if (randomtemp.get(i) == 1) {
                Integer num1 = code1.get(i);
                Integer num2 = code2.get(i);
                child1.set(i, num1);
                child2.set(i, num2);
                parent1.remove(num2);
                parent2.remove(num1);
            }
        }
        for (int i = 0; i < code1.size(); i++) {
            if (child1.get(i) == -1) {
                child1.set(i, parent2.get(0));
                parent2.remove(0);
            }
            if (child2.get(i) == -1) {
                child2.set(i, parent1.get(0));
                parent1.remove(0);
            }
        }
        ArrayList<ArrayList<Integer>> result = new ArrayList<>();
        result.add(child1);
        result.add(child2);
        
        return result;
    }


    /**
     * 采用反转突变算子对排序编码执行突变操作
     * @param code
     * @return Code
     */
    private Code SwapMutation(Code code) {
        Code childcode = new Code();
        childcode.setConfigurationCode(code.getConfigurationCode());
        childcode.setOperationCode(code.getOperationCode());
        ArrayList<Integer> parent = new ArrayList<>(code.getSortCode());
        ArrayList<Integer> child = new ArrayList<>(parent);
        int index1 = new Random().nextInt(parent.size());
        int index2 = new Random().nextInt(parent.size());
        while (Math.abs(index1 - index2) < 1) {
            index1 = new Random().nextInt(parent.size());
            index2 = new Random().nextInt(parent.size());
        }
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = Math.max(index1, index2); i >= Math.min(index1, index2); i--) {
            list.add(parent.get(i));
        }
        for (int i = Math.min(index1, index2); i <= Math.max(index1, index2); i++) {
            child.set(i, list.get(i - Math.min(index1, index2)));
        }
        childcode.setSortCode(child);      

        return childcode;
    }

    /**
     * 搜索 individual 的所有邻域
     * @param individual
     * @param qus
     * @return
     */
    private ArrayList<Pop> SearchNeighborhoods(Individual individual, Qus qus) {
        ArrayList<Pop> Neighborhoods = new ArrayList<>();   // 记录所有邻域
        // 邻域1
        ArrayList<ArrayList<Integer>> configurationCodList = Action1(individual, qus.getAlternativeMachine());
        ArrayList<Code> codes = new ArrayList<>();
        for (int i = 0; i < configurationCodList.size(); i++) {
            Code code = new Code();
            code.setConfigurationCode(configurationCodList.get(i));
            code.updateOperationCode(qus.getPartsNum(), qus.getMachine(), qus.getProcessNum(), qus.getProcess());
            code.setSortCode(individual.getCode().getSortCode());
            codes.add(code);
        }
        // 将 code 邻域放入 pop 中封装
        Pop Neighborhood = new Pop(configurationCodList.size());
        for (int i = 0; i < Neighborhood.getPopsize(); i++) {
            Neighborhood.deepsetIndividual(i, individual);
            Neighborhood.getIndividual(i).setCode(codes.get(i));
        }
        Neighborhood.decode(qus.getMachineTime(), qus.getDemand(), qus.getSetupTime(), qus.getDueDays());
        Neighborhoods.add(Neighborhood);
        // 邻域2
        ArrayList<Map<Integer, TreeMap<Integer, Integer>>> OperationCodeList = Action2(individual, qus.getMachine(), qus.getProcessNum());
        codes = new ArrayList<>();
        for (int i = 0; i < OperationCodeList.size(); i++) {
            Code code = new Code();
            code.setConfigurationCode(individual.getCode().getConfigurationCode());
            code.setOperationCode(OperationCodeList.get(i));
            code.setSortCode(individual.getCode().getSortCode());
            codes.add(code);
        }
        // 将 code 邻域放入 pop 中封装
        Neighborhood = new Pop(OperationCodeList.size());
        for (int i = 0; i < Neighborhood.getPopsize(); i++) {
            Neighborhood.deepsetIndividual(i, individual);
            Neighborhood.getIndividual(i).setCode(codes.get(i));
        }
        Neighborhood.decode(qus.getMachineTime(), qus.getDemand(), qus.getSetupTime(), qus.getDueDays());
        Neighborhoods.add(Neighborhood);
        // 邻域3
        Map<Integer, TreeMap<Integer, Integer>> newOperationCode = Action3(individual, qus.getMachine(), qus.getProcessNum());
        Code code = new Code();
        code.setConfigurationCode(individual.getCode().getConfigurationCode());
        code.setOperationCode(newOperationCode);
        code.setSortCode(individual.getCode().getSortCode());
        // 将 code 邻域放入 pop 中封装
        Neighborhood = new Pop(1);
        Neighborhood.deepsetIndividual(0, individual);
        Neighborhood.getIndividual(0).setCode(code);
        Neighborhood.decode(qus.getMachineTime(), qus.getDemand(), qus.getSetupTime(), qus.getDueDays());
        Neighborhoods.add(Neighborhood);

        return Neighborhoods;
    }

    /**
     * 邻域搜索动作1，随机选择一个工位，搜索可替换 RMT 并进行替换和插入
     * @param individual
     * @param AlternativeMachine
     * @return
     */
    private ArrayList<ArrayList<Integer>> Action1(Individual individual, Map<Integer, ArrayList<Integer>> AlternativeMachine) {
        ArrayList<Integer> ConfigurationCode = new ArrayList<>(individual.getCode().getConfigurationCode());
        ArrayList<ArrayList<Integer>> configurationCodList = new ArrayList<>();
        // 随机选择一个编码位置
        int machineindex = new Random().nextInt(ConfigurationCode.size());
        Integer machine = ConfigurationCode.get(machineindex);
        // 寻找可替代机器并判断是否可以替代
        for (Integer integer : AlternativeMachine.get(machine)) {
            if (!ConfigurationCode.contains(integer)) {
                ArrayList<Integer> ConfigurationCode1 = new ArrayList<>(ConfigurationCode);
                ConfigurationCode1.set(machineindex, integer);
                configurationCodList.add(ConfigurationCode1);
                ArrayList<Integer> ConfigurationCode2 = new ArrayList<>(ConfigurationCode);
                ConfigurationCode2.add(machineindex, integer);
                configurationCodList.add(ConfigurationCode2);
                ArrayList<Integer> ConfigurationCode3 = new ArrayList<>(ConfigurationCode);
                ConfigurationCode3.add(machineindex + 1, integer);
                configurationCodList.add(ConfigurationCode3);
            } else {
                int AlternativeIndex = ConfigurationCode.indexOf(integer);
                if (Math.abs(AlternativeIndex - machineindex) == 1) {
                    // 交换位置
                    ArrayList<Integer> ConfigurationCode1 = new ArrayList<>(ConfigurationCode);
                    ConfigurationCode1.set(machineindex, integer);
                    ConfigurationCode1.set(AlternativeIndex, machine);
                    configurationCodList.add(ConfigurationCode1);
                    // 仅留 machine
                    ArrayList<Integer> ConfigurationCode2 = new ArrayList<>(ConfigurationCode);
                    ConfigurationCode2.remove(integer);
                    configurationCodList.add(ConfigurationCode2);
                    // 仅留 integer
                    ArrayList<Integer> ConfigurationCode3 = new ArrayList<>(ConfigurationCode);
                    ConfigurationCode3.remove(machine);
                    configurationCodList.add(ConfigurationCode3);
                }
            }
            
        }

        return configurationCodList;
    }

    /**
     * 邻域搜索动作2，随机选择一个工件的一个操作，用可替代工位替代当前工位
     * @param individual
     * @param Machine
     * @param ProcessNum
     * @return
     */
    private ArrayList<Map<Integer, TreeMap<Integer, Integer>>> Action2(Individual individual, Map<Integer, ArrayList<Integer>> Machine, int ProcessNum) {
        Code code = individual.getCode();
        ArrayList<Map<Integer, TreeMap<Integer, Integer>>> operationCodeList = new ArrayList<>();
        // 随机选择一个工件
        int partindex = new Random().nextInt(code.getOperationCode().size());
        int part = code.getSortCode().get(partindex);
        TreeMap<Integer, Integer> OperationCodeofP = new TreeMap<>(code.getOperationCode(part));
        ArrayList<Integer> processList = new ArrayList<>(OperationCodeofP.keySet());
        // 随机选择该工件的一个操作
        int processindex = new Random().nextInt(OperationCodeofP.size());
        int process = processList.get(processindex);
        int lowerprocess = (OperationCodeofP.lowerKey(process) != null) ? OperationCodeofP.lowerKey(process) : -1;
        int ceilingprocess = (OperationCodeofP.higherKey(process) != null) ? OperationCodeofP.higherKey(process) : -1;
        // 记录所有操作的可选工位
        Map<Integer, ArrayList<Integer>> allOptStageSet = code.SearchOptStageSet(Machine, ProcessNum);
        // 更新选中操作的可选工位集合
        ArrayList<Integer> machineList = allOptStageSet.get(process);
        for (int i = 0; i < machineList.size(); i++) {
            if (lowerprocess != -1 && machineList.get(i) < OperationCodeofP.get(lowerprocess)) {
                machineList.remove(allOptStageSet.get(process).get(i));
                i--;
                continue;
            }
            if (ceilingprocess != -1 && machineList.get(i) > OperationCodeofP.get(ceilingprocess)) {
                machineList.remove(allOptStageSet.get(process).get(i));
                i--;
            }
        }
        // 用剩余的可选工位替换当前工位，每个可选工位代表一个新解
        for (Integer integer : machineList) {
            if (integer != OperationCodeofP.get(process)) {
                TreeMap<Integer, Integer> newOperationCodeofP = new TreeMap<>(OperationCodeofP);
                newOperationCodeofP.put(process, integer);
                Map<Integer, TreeMap<Integer, Integer>> newOperationCode = new HashMap<>();
                for (Iterator<Map.Entry<Integer, TreeMap<Integer, Integer>>> iterator = code.getOperationCode().entrySet().iterator(); iterator.hasNext();) {
                    Map.Entry<Integer, TreeMap<Integer, Integer>> entry = iterator.next();
                    if (entry.getKey() != part) {
                        TreeMap<Integer, Integer> map = new TreeMap<>(entry.getValue());
                        newOperationCode.put(entry.getKey(), map);
                    } else {
                        newOperationCode.put(part, newOperationCodeofP);
                    }
                }
                operationCodeList.add(newOperationCode);
            }
        }

        return operationCodeList;
    }

    /**
     * 邻域搜索动作3，将操作编码每个工件的随机一个工位更换为随机一个可替代工位
     * @param individual
     * @param Machine
     * @param ProcessNum
     * @return
     */
    private Map<Integer, TreeMap<Integer, Integer>> Action3(Individual individual, Map<Integer, ArrayList<Integer>> Machine, int ProcessNum) {
        Code code = individual.getCode();
        Map<Integer, TreeMap<Integer, Integer>> newOperationCode =  new HashMap<>();
        // 记录所有操作的可选工位
        Map<Integer, ArrayList<Integer>> allOptStageSet = code.SearchOptStageSet(Machine, ProcessNum);
        // 对每个工件执行一次操作
        for (int partindex = 0; partindex < code.getSortCode().size(); partindex++) {
            Integer part = code.getSortCode().get(partindex);
            TreeMap<Integer, Integer> OperationCodeofP = new TreeMap<>(code.getOperationCode(part));
            ArrayList<Integer> processList = new ArrayList<>(OperationCodeofP.keySet());
            // 随机选择该工件的一个操作
            int processindex = new Random().nextInt(OperationCodeofP.size());
            int process = processList.get(processindex);
            int lowerprocess = (OperationCodeofP.lowerKey(process) != null) ? OperationCodeofP.lowerKey(process) : -1;
            int ceilingprocess = (OperationCodeofP.higherKey(process) != null) ? OperationCodeofP.higherKey(process) : -1;
            // 更新选中操作的可选工位集合
            ArrayList<Integer> machineList = allOptStageSet.get(process);
            for (int i = 0; i < machineList.size(); i++) {
                if (lowerprocess != -1 && machineList.get(i) < OperationCodeofP.get(lowerprocess)) {
                    machineList.remove(allOptStageSet.get(process).get(i));
                    i--;
                    continue;
                }
                if (ceilingprocess != -1 && machineList.get(i) > OperationCodeofP.get(ceilingprocess)) {
                    machineList.remove(allOptStageSet.get(process).get(i));
                    i--;
                }
            }
            // 随机选择一个可选工位替换当前工位
            if (! machineList.isEmpty()) {
                TreeMap<Integer, Integer> newOperationCodeofP = new TreeMap<>(OperationCodeofP);
                newOperationCodeofP.put(process, machineList.get(new Random().nextInt(machineList.size())));
                newOperationCode.put(part, newOperationCodeofP);
            } else {
                newOperationCode.put(part, OperationCodeofP);
            }
        }

        return newOperationCode;
    }

    /**
     * Combine parent population and offspring population to form a combined population
     * @param pop1
     * @param pop2
     * @return
     */
    public Pop getMergePop(Pop pop1, Pop pop2) {
        Pop mergePop = new Pop(pop1.getPopsize() + pop2.getPopsize());
        ArrayList<Individual> poplist1 = pop1.getIndividuals();
        ArrayList<Individual> poplist2 = pop2.getIndividuals();
        for (int i = 0; i < poplist1.size(); i++) {
            mergePop.deepsetIndividual(i, poplist1.get(i));
        }
        for (int i = 0; i < poplist2.size(); i++) {
            mergePop.deepsetIndividual(i + pop1.getPopsize(), poplist2.get(i));
        }

        return mergePop;
    }

    public void UpdatePop(int popsize) {
        int count = 0;
        Pop childPop = new Pop(popsize);
        for (ArrayList<Integer> arrayList : this.F) {
            if (popsize - count >= arrayList.size()) {
                for (int i = 0; i < arrayList.size() && count < popsize; i++) {
                    childPop.deepsetIndividual(count, this.pop.getIndividual(arrayList.get(i)));
                    count++;
                }
            } else {
                ArrayList<Individual> rankIndividuals = new ArrayList<>();
                for (Integer integer : arrayList) {
                    rankIndividuals.add(this.pop.getIndividual(integer));
                }
                ArrayList<Individual> CrowdedComparison = Comparison(rankIndividuals);
                for (int i = 0; i < CrowdedComparison.size() && count < popsize; i++) {
                    childPop.deepsetIndividual(count, CrowdedComparison.get(i));
                    count++;
                }
                break;
            }
        } 
        this.pop = childPop;
    }


    public boolean TerminateIteration(int iter) {
        return iter >= this.MaxIteration;
    }

    // SET methods
    public void setPopulation(Pop newpop) {
        this.pop = newpop;
    }

    // GET methods
    public ArrayList<ArrayList<Integer>> getF() {
        return this.F;
    }
    public Pop getPop() {
        return this.pop;
    }
}