package cn.nancy.scheduling_of_rfl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public abstract class Algorithm {
    private int MaxIteration;
    /**
     * 竞标赛选择的参数大小
     */
    private int TournamentPara;
    /**
     * population
     */
    private Pop pop;

    public Algorithm(int MaxIteration, int TournamentPara) {
        this.MaxIteration = MaxIteration;
        this.TournamentPara = TournamentPara;
        this.pop = null;
    }

    /**
     * 获取交配池，子类必须重写该方法
     * @param MatingPoolSize 交配池大小
     * @return
     */
    public abstract Pop MatingSelection(int MatingPoolSize);
    
    public Pop Variation(String KEY, Pop pop, Qus qus) {
        switch (KEY) {
            case "1":
                return Variation1(pop, qus);
            case "2":
                return Variation2(pop, qus);
            case "3":
                return Variation3(pop, qus);
            default:
                throw new IllegalArgumentException("The KEY must be 1, 2 or 3.");
        }
    }


    /**
     * 交叉变异 1，子类按需覆盖
     * 
     * <p> 按顺序每两个一组进行交叉变异，得到子代
     * 
     * @param ParentPop 父代种群
     * @param qus 数据
     * @return Pop 子代种群
     */
    public Pop Variation1(Pop ParentPop, Qus qus) {
        
        int popsize = ParentPop.getPopsize();
        Pop MatingPool = MatingSelection(popsize/2+1);
        Pop chilidPop = new Pop(popsize);                   // 子代

        for (int i = 0; i < popsize/2; i++) {
            Individual parent1 = MatingPool.getIndividual(i);
            Individual parent2 = MatingPool.getIndividual(i+1);
            Individual child1 = new Individual(parent1);
            Individual child2 = new Individual(parent2);
            // 交配池中按顺序滚动，两两进行排序编码的交叉
            ArrayList<ArrayList<Integer>> sortingList = new ArrayList<>(
                PBX(parent1.getCode().getSortCode(), parent2.getCode().getSortCode())
            );
            child1.getCode().setSortCode(sortingList.get(0));
            child2.getCode().setSortCode(sortingList.get(1));
            // 交配池中每个个体的排序编码执行反转突变
            child1.setCode(SwapMutation(child1.getCode()));
            child2.setCode(SwapMutation(child2.getCode()));
            // 交配池中每个个体采用邻域搜索，对配置编码和操作编码进行变异搜索
            List<Individual> childlist = VNS(child1, qus);
            chilidPop.deepsetIndividual(i, childlist.get(new Random().nextInt(childlist.size())));
            childlist = VNS(child2, qus);
            chilidPop.deepsetIndividual(popsize/2 + i, childlist.get(new Random().nextInt(childlist.size())));
        }

        return chilidPop;
    }

    // /**
    //  * 交叉变异
    //  * 
    //  * <p>将交配池中每个个体VNO，每次获取邻域中最优的个体替换父代个体，然后进行PBX和SwapMutation
    //  * 
    //  * @param pop 父代种群
    //  * @param qus 数据
    //  * @return 子代直接替入self.pop，成为下一代种群
    //  */
    // public Pop Variation2(Pop pop, Qus qus) {
    //     // 每次从混合种群中选出 tournamentPara 个个体，并将里面最优个体放入交配池
    //     int popsize = pop.getPopsize();
    //     Pop childPop = new Pop(popsize);                 // 子代种群
    //     Pop seletPop = new Pop(this.TournamentPara);    // 记录待选择个体
    //     int index = 0;  // 当前正在选择第几个个体
    //     while (index < popsize) {
    //         for (int i = 0; i < this.TournamentPara; i++) {
    //             seletPop.setIndividual(i, pop.getIndividual(new Random().nextInt(pop.getPopsize())));
    //         }
    //         ArrayList<Individual> sortList = seletPop.SortFitness();// 按适应度从小到大排序
    //         // 每个个体采用邻域搜索，对配置编码和操作编码进行变邻域搜索填入子代种群
    //         List<Individual> childlist = VNS(sortList.get(0), qus);
    //         for (int j = 0; j < childlist.size() && index < childPop.getPopsize(); j++, index++) {
    //             childPop.setIndividual(index, childlist.get(j));
    //         }
    //     }
        
    //     // 子代按顺序滚动，两两进行排序编码的交叉
    //     for (int i = 0; i < popsize - 1; i=i+2) {
    //         Individual child1 = new Individual(childPop.getIndividual(i));
    //         Individual child2 = new Individual(childPop.getIndividual(i+1));
    //         ArrayList<ArrayList<Integer>> sortingList = new ArrayList<>(CrossOver(child1.getCode().getSortCode(), child2.getCode().getSortCode()));
    //         childPop.getIndividual(i).getCode().setSortCode(sortingList.get(0));
    //             childPop.getIndividual(i+1).getCode().setSortCode(sortingList.get(1));
    //     }
    //     // 对交叉后的每个个体的排序编码执行反转突变
    //     for (int i = 0; i < popsize; i++) {
    //         Individual child = new Individual(childPop.getIndividual(i));
    //         child.setCode(SwapMutation(child.getCode()));
    //         // 赋值pop
    //         pop.deepsetIndividual(i, child);
    //     }    
    // }


    /**
     * 交叉变异 2，子类按需覆盖
     * 
     * <p>将交配池中每个个体VNS，扩大交配池三倍，然后随机进行PBX和SwapMutation
     * 
     * @param ParentPop 父代种群
     * @param qus 数据
     * @return Pop，下一代种群
     */
    public Pop Variation2(Pop ParentPop, Qus qus) {
        // 每次从混合种群中选出 tournamentPara 个个体，并将里面最优个体VNO后放入交配池
        int popsize = ParentPop.getPopsize();
        Pop childPop = new Pop(popsize);                // 子代种群
        Pop MatingPool = MatingSelection(popsize);      // 记录配置和操作编码变化的个体
        // VNS
        for (int i = 0; i < popsize; i++) {
            List<Individual> childlist = VNS(MatingPool.getIndividual(i), qus);
            MatingPool.setIndividual(i, childlist.get(new Random().nextInt(childlist.size())));
        }
        // 子代随机选择，两两进行排序编码的交叉
        for (int i = 0; i < popsize - 1; i=i+2) {
            int r1 = new Random().nextInt(popsize);
            int r2 = new Random().nextInt(popsize);
            while (r1 == r2) {
                r2 = new Random().nextInt(popsize);
            }
            Individual child1 = new Individual(MatingPool.getIndividual(r1));
            Individual child2 = new Individual(MatingPool.getIndividual(r2));
            ArrayList<ArrayList<Integer>> sortingList = new ArrayList<>(
                PBX(child1.getCode().getSortCode(), child2.getCode().getSortCode())
            );
            child1.getCode().setSortCode(sortingList.get(0));
            child2.getCode().setSortCode(sortingList.get(1));
            childPop.deepsetIndividual(i, child1);
            childPop.deepsetIndividual(i+1, child2);
        }
        // 对交叉后的每个个体的排序编码执行反转突变
        for (int i = 0; i < popsize; i++) {
            Individual child = new Individual(childPop.getIndividual(i));
            child.setCode(SwapMutation(child.getCode()));
            // 赋值pop
            childPop.deepsetIndividual(i, child);
        }

        return childPop;
    }

    /**
     * 交叉变异
     * 
     * <p> 用于对比
     * @param ParentPop 父代种群
     * @param qus
     * @return Pop 子代种群
     */
    public Pop Variation3(Pop ParentPop, Qus qus) {
        int popsize = ParentPop.getPopsize();
        Pop MatingPool = MatingSelection(popsize/2);
        Pop childPop = new Pop(popsize);
        // randomly select parents to perfrom VNO, PBX, and SM for configuration code and sorting code
        for (int i = 0; i < popsize - 1; i = i + 2) {
            Individual parent = MatingPool.getIndividual(i/2);
            Individual child1 = new Individual(parent);
            Individual child2 = new Individual(parent);
            // VNO
            ArrayList<Individual> VNSlist = VNS(parent, qus);
            if (VNSlist.size() >= 2) {
                int r1 = new Random().nextInt(popsize);
                int r2 = new Random().nextInt(popsize);
                while (r1 == r2) {
                    r2 = new Random().nextInt(popsize);
                }
                child1 = new Individual(VNSlist.get(r1));
                child2 = new Individual(VNSlist.get(r2));
            } else {
                child1 = new Individual(VNSlist.get(0));
                VNSlist = VND(child2, qus);
                child2 = new Individual(VNSlist.get(0));
            }
            // PBX
            ArrayList<ArrayList<Integer>> sortingList = new ArrayList<>(
                PBX(parent.getCode().getSortCode(), MatingPool.getIndividual(new Random().nextInt(popsize/2)).getCode().getSortCode())
            );
            child1.getCode().setSortCode(sortingList.get(0));
            child2.getCode().setSortCode(sortingList.get(1));
            // SM
            Code childCode1 = SwapMutation(child1.getCode());
            Code childCode2 = SwapMutation(child2.getCode());
            child1.setCode(childCode1);
            child2.setCode(childCode2);
            // 赋值 childpop
            childPop.deepsetIndividual(i, child1);
            childPop.deepsetIndividual(i+1, child2);
        }

        return childPop;
    }

    /**
     * 采用 PBX （Position-based Crossover） 算子对排序编码执行交叉操作
     * @param code1
     * @param code2
     * @return Code list
     */
    public ArrayList<ArrayList<Integer>> PBX(ArrayList<Integer> code1, ArrayList<Integer> code2) {
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
    public Code SwapMutation(Code code) {
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
        Code childcode = new Code();
        childcode.setConfigurationCode(code.getConfigurationCode());
        childcode.setOperationCode(code.getOperationCode());
        childcode.setSortCode(child);

        return childcode;
    }

    /**
     * Variable Neighborhood Search，对配置编码和操作编码部分进行交叉操作
     */
    public ArrayList<Individual> VNS(Individual individual, Qus qus) {
        Decode individualDecode = individual.getDecode();
        ArrayList<Individual> childlist = new ArrayList<>();
        // Shaking
        Pop shakingPop = new Pop(0);
        while (shakingPop.getPopsize() == 0) {
            int RandomActionIndex = new Random().nextInt(3);
            switch (RandomActionIndex) {
                case 1:
                    shakingPop = Action1(individual, qus);
                    break;
                case 2:
                    shakingPop = Action2(individual, qus);
                    break;
                default:
                    shakingPop = Action3(individual, qus);
                    break;
            }
        }
        // 对 Shaking pop 中每一个个体进行判断，若差于父代则 VND
        for (int i = 0; i < shakingPop.getPopsize(); i++) {
            Individual shakingneighbor = shakingPop.getIndividual(i);
            Decode shakingneighbordecode = shakingneighbor.getDecode();
            if (    // 当 shaking neighbor 不劣于父代个体时，直接放入 childlist 中
                shakingneighbordecode.getTotalDelay() <= individualDecode.getTotalDelay() && shakingneighbordecode.getUtilization() >= individualDecode.getUtilization()
            ) {
                childlist.add(shakingneighbor);
            } else {
                ArrayList<Pop> VNDneighborhoods = SearchNeighborhoods(shakingneighbor, qus);
                while (VNDneighborhoods.size() > 0) {
                    // 找到 shakingneighbor 的邻域中的最优解 bestneighbor，比较 bestneighbor 和 shakingneighbor 的适应度
                    Pop VNDneighborhood = VNDneighborhoods.get(0);
                    VNDneighborhoods.remove(0);
                    if (VNDneighborhood.getPopsize() == 0) {
                        continue;
                    } else {    // 如果 bestneighbor 优于 shakingneighbor，则替代，并且重新寻找邻域
                        for (int j = 0; j < VNDneighborhood.getPopsize(); j++) {
                            Individual neighbor = VNDneighborhood.getIndividual(j);
                            Decode neighbordecode = neighbor.getDecode();
                            if (
                                (neighbordecode.getTotalDelay() <= shakingneighbordecode.getTotalDelay() && neighbordecode.getUtilization() > shakingneighbordecode.getUtilization()) ||
                                (neighbordecode.getTotalDelay() < shakingneighbordecode.getTotalDelay() && neighbordecode.getUtilization() >= shakingneighbordecode.getUtilization())
                            ) {
                                shakingneighbor = neighbor;
                                shakingneighbordecode = shakingneighbor.getDecode();
                                VNDneighborhoods = SearchNeighborhoods(shakingneighbor, qus);
                                break;
                            }
                        }   
                    }
                }
                childlist.add(shakingneighbor);
            }
        }

        return childlist;
    }

    /**
     * VND算子，对配置编码和操作编码部分进行交叉操作
     */
    public ArrayList<Individual> VND(Individual individual, Qus qus) {
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
     * 搜索 individual 的所有邻域
     * @param individual
     * @param qus
     * @return
     */
    public ArrayList<Pop> SearchNeighborhoods(Individual individual, Qus qus) {
        ArrayList<Pop> Neighborhoods = new ArrayList<>();   // 记录所有邻域
        // 邻域1
        Pop Neighborhood1 = Action1(individual, qus);
        Neighborhoods.add(Neighborhood1);
        // 邻域2
        Pop Neighborhood2 = Action2(individual, qus);
        Neighborhoods.add(Neighborhood2);
        // 邻域3
        Pop Neighborhood3 = Action3(individual, qus);
        Neighborhoods.add(Neighborhood3);

        return Neighborhoods;
    }

    /**
     * 邻域搜索动作1，随机选择一个工位，搜索可替换 RMT 并进行替换和插入
     * @param individual
     * @param AlternativeMachine
     * @return
     */
    public Pop Action1(Individual individual, Qus qus) {
        ArrayList<Integer> ConfigurationCode = new ArrayList<>(individual.getCode().getConfigurationCode());
        ArrayList<ArrayList<Integer>> configurationCodList = new ArrayList<>();
        // 随机选择一个编码位置
        int machineindex = new Random().nextInt(ConfigurationCode.size());
        Integer machine = ConfigurationCode.get(machineindex);
        // 寻找可替代机器并判断是否可以替代
        for (Integer integer : qus.getAlternativeMachine().get(machine)) {
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

        return Neighborhood;
    }

    /**
     * 邻域搜索动作2，随机选择一个工件的一个操作，用可替代工位替代当前工位
     * @param individual
     * @param Machine
     * @param ProcessNum
     * @return
     */
    public Pop Action2(Individual individual, Qus qus) {
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
        Map<Integer, ArrayList<Integer>> allOptStageSet = code.SearchOptStageSet(qus.getMachine(), qus.getProcessNum());
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

        List<Code> codes = new ArrayList<>();
        for (int i = 0; i < operationCodeList.size(); i++) {
            code = new Code();
            code.setConfigurationCode(individual.getCode().getConfigurationCode());
            code.setOperationCode(operationCodeList.get(i));
            code.setSortCode(individual.getCode().getSortCode());
            codes.add(code);
        }

        // 将 code 邻域放入 pop 中封装
        Pop Neighborhood = new Pop(operationCodeList.size());
        for (int i = 0; i < Neighborhood.getPopsize(); i++) {
            Neighborhood.deepsetIndividual(i, individual);
            Neighborhood.getIndividual(i).setCode(codes.get(i));
        }
        Neighborhood.decode(qus.getMachineTime(), qus.getDemand(), qus.getSetupTime(), qus.getDueDays());

        return Neighborhood;
    }

    /**
     * 邻域搜索动作3，将操作编码每个工件的随机一个工位更换为随机一个可替代工位
     * @param individual
     * @param Machine
     * @param ProcessNum
     * @return
     */
    public Pop Action3(Individual individual, Qus qus) {
        Code code = individual.getCode();
        Map<Integer, TreeMap<Integer, Integer>> newOperationCode =  new HashMap<>();
        // 记录所有操作的可选工位
        Map<Integer, ArrayList<Integer>> allOptStageSet = code.SearchOptStageSet(qus.getMachine(), qus.getProcessNum());
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

        code = new Code();
        code.setConfigurationCode(individual.getCode().getConfigurationCode());
        code.setOperationCode(newOperationCode);
        code.setSortCode(individual.getCode().getSortCode());

        // 将 code 邻域放入 pop 中封装
        Pop Neighborhood = new Pop(1);
        Neighborhood.deepsetIndividual(0, individual);
        Neighborhood.getIndividual(0).setCode(code);
        Neighborhood.decode(qus.getMachineTime(), qus.getDemand(), qus.getSetupTime(), qus.getDueDays());

        return Neighborhood;
    }

    /**
     * 终止条件判读，当达到迭代最大次数时终止迭代
     * @param iter 当前迭代次数
     * @return
     */
    public boolean TerminateIteration(int iter) {
        return iter >= this.MaxIteration;
    }

    // SET methods
    public void setPop(Pop pop) {
        this.pop = pop;
    }
    public void setTournamentPara(int TournamentSize) {
        this.TournamentPara = TournamentSize;
    }

    // SET methods
    public Pop getPop() {
        return this.pop;
    }
    public int getTournamentPara() {
        return this.TournamentPara;
    }
}
