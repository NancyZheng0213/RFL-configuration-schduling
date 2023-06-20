package cn.nancy.scheduling_of_rfl.nsgaii;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import cn.nancy.scheduling_of_rfl.*;

public class NSGAII extends Algorithem{
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
     * sets of rank
     */
    private ArrayList<ArrayList<Integer>> F;

    /**
     * NSGAII 算法初始化，默认锦标赛参数为 2
     * @param MaxIteration
     * @param popsize
     * @param pc
     * @param pms
     * @param pmv
     * @param qus
     */
    public NSGAII(int MaxIteration, int popsize, double pc, double pms, double pmv, Qus qus) {
        super(MaxIteration, 2);
        this.pc = pc;
        this.pms = pms;
        this.pmv = pmv;
        PopofNSGAII pop = new PopofNSGAII(popsize);
        pop.encode(qus.getPartsNum(), qus.getMachine(), qus.getProcessNum(), qus.getProcess(), qus.getAlternativeMachine());
        pop.decode(qus.getMachineTime(), qus.getDemand(), qus.getSetupTime(), qus.getDueDays());
        super.setPop(pop);
    }

    /**
     * fast nondominated sorting approach
     * 
     * <p>Set S and n for every individual in population, rank the population, set R for each individual.<\p>
     */
    public void NondominatedRank() {
        // set S and n for every individual in this.pop
        ResetSNR();
        for (int i = 0; i < super.getPop().getPopsize(); i++) {
            IndividualofNSGAII individuali = (IndividualofNSGAII) super.getPop().getIndividual(i);
            for (int j = i+1; j < super.getPop().getPopsize(); j++) {
                IndividualofNSGAII individualj = (IndividualofNSGAII) super.getPop().getIndividual(j);
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
        for (int i = 0; i < super.getPop().getPopsize(); i++) {
            IndividualofNSGAII individual = (IndividualofNSGAII) super.getPop().getIndividual(i);
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
                IndividualofNSGAII individualofNSGAII = (IndividualofNSGAII) super.getPop().getIndividual(i);
                for (Integer j : individualofNSGAII.getS()) {
                    IndividualofNSGAII individual = (IndividualofNSGAII) super.getPop().getIndividual(j);
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
        for (IndividualofNSGAII individual : (IndividualofNSGAII[]) super.getPop().getIndividuals()) {
            individual.setD(0);
            individual.setN(0);
            individual.setR(0);
            individual.getS().clear();
        }
    }


    /**
     * crowding distance-based comparison operator (for a rank)
     * @param rank the individuals group of one rank
     * @return ArrayList<IndividualofNSGAII>
     */
    public ArrayList<IndividualofNSGAII> Comparison(ArrayList<IndividualofNSGAII> rank) {

        ArrayList<IndividualofNSGAII> Fset = new ArrayList<>(rank);
//        // sort total delays in ascending order 
//        Collections.sort(Fset, new Comparator<IndividualofNSGAII>() {
//            @Override
//            public int compare(IndividualofNSGAII i, IndividualofNSGAII j) {
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
//        Collections.sort(Fset, new Comparator<IndividualofNSGAII>() {
//            @Override
//            public int compare(IndividualofNSGAII i, IndividualofNSGAII j) {
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
        Collections.sort(Fset, new Comparator<IndividualofNSGAII>() {
            @Override
            public int compare(IndividualofNSGAII i, IndividualofNSGAII j) {
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
     * crowding distance-based comparison operator (for a rank) and set D for each individual
     * @param rank the index of individuals group of one rank
     */
    private void ComparisonFor(ArrayList<Integer> rank) {
        ArrayList<Integer> Fset = new ArrayList<>(rank);
        final IndividualofNSGAII[] Findividuals = (IndividualofNSGAII[]) super.getPop().getIndividuals();
        // sort total delays in ascending order 
        Collections.sort(Fset, new Comparator<Integer>() {
        	@Override
        	public int compare(Integer i, Integer j) {
        		return Findividuals[i].getDecode().getTotalDelay() - Findividuals[j].getDecode().getTotalDelay();
        	}
        });
        int minT = super.getPop().getIndividual(Fset.get(0)).getDecode().getTotalDelay();
        int maxT = super.getPop().getIndividual(Fset.get(Fset.size()-1)).getDecode().getTotalDelay();
        IndividualofNSGAII individual1 = (IndividualofNSGAII) super.getPop().getIndividual(Fset.get(0));
        IndividualofNSGAII individual2 = (IndividualofNSGAII) super.getPop().getIndividual(Fset.get(Fset.size()-1));
        individual1.setD(Double.MAX_VALUE);
        // TODO: 检查更新 D 之后，原有的 pop 是否也随着跟新了 D
        individual2.setD(Double.MAX_VALUE);
        if (minT == maxT) {
            for (int i = 1; i < Fset.size()-1; i++) {
                IndividualofNSGAII individual = (IndividualofNSGAII) super.getPop().getIndividual(Fset.get(i));
            	individual.setD(Double.MAX_VALUE);
            }
        } else {
            for (int i = 1; i < Fset.size()-1; i++) {
                IndividualofNSGAII individual = (IndividualofNSGAII) super.getPop().getIndividual(Fset.get(i));
                individual1 = (IndividualofNSGAII) super.getPop().getIndividual(Fset.get(i));
            	individual.setD(individual1.getD()+(super.getPop().getIndividual(Fset.get(i+1)).getDecode().getTotalDelay()-super.getPop().getIndividual(Fset.get(i-1)).getDecode().getTotalDelay())/(maxT-minT));
            }
        }
        // sort utilization in ascending order
        Collections.sort(Fset, new Comparator<Integer>() {
            @Override
            public int compare(Integer i, Integer j) {
                if (Findividuals[i].getDecode().getUtilization() < Findividuals[j].getDecode().getUtilization()) {
                    return -1;
                } else if(Findividuals[i].getDecode().getUtilization() == Findividuals[j].getDecode().getUtilization()) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        double maxU = super.getPop().getIndividual(Fset.get(Fset.size()-1)).getDecode().getUtilization();
        double minU = super.getPop().getIndividual(Fset.get(0)).getDecode().getUtilization();
        individual1 = (IndividualofNSGAII) super.getPop().getIndividual(Fset.get(0));
        individual2 = (IndividualofNSGAII) super.getPop().getIndividual(Fset.get(Fset.size()-1));
        individual1.setD(Double.MAX_VALUE);
        individual2.setD(Double.MAX_VALUE);
        if (maxU == minU) {
            for (int i = 1; i < Fset.size()-1; i++) {
                IndividualofNSGAII individual = (IndividualofNSGAII) super.getPop().getIndividual(Fset.get(i));
                individual.setD(Double.MAX_VALUE);
            }
        } else {
            for (int i = 1; i < Fset.size()-1; i++) {
                IndividualofNSGAII individual = (IndividualofNSGAII) super.getPop().getIndividual(Fset.get(i));
                individual1 = (IndividualofNSGAII) super.getPop().getIndividual(Fset.get(i));
                individual.setD(individual1.getD()+(super.getPop().getIndividual(Fset.get(i+1)).getDecode().getUtilization()-super.getPop().getIndividual(Fset.get(i-1)).getDecode().getUtilization())/(maxU-minU));
            }
        }
    }

    @Override
    public PopofNSGAII MatingSelection(int MatingPoolSize) {
        int popsize = super.getPop().getPopsize();
        PopofNSGAII MatingPool = new PopofNSGAII(MatingPoolSize);
        // Generate offspring population from parent population by tournament selection
        for (int i = 0; i < MatingPoolSize; i++) {
            // 选出 TournamentPara 个备选个体，比较备选个体，选出最优的放入交配池
            int r = new Random().nextInt(popsize);
            IndividualofNSGAII bestIndividual = (IndividualofNSGAII)super.getPop().getIndividual(r);
            int bestR = bestIndividual.getR();
            Double bestD = bestIndividual.getD();
            for (int j = 0; j < super.getTournamentPara()-1; j++) {
                r = new Random().nextInt(popsize);
                IndividualofNSGAII individual = (IndividualofNSGAII)super.getPop().getIndividual(r);
                if (individual.getR() < bestR || individual.getR() == bestR && individual.getD() > bestD) {
                    bestIndividual = individual;
                }
            }
            MatingPool.deepsetIndividual(i, bestIndividual);
        }

        return MatingPool;
    }

    /**
     * NSGAII 交叉变异 1
     * 
     * <p> 按顺序每两个一组，概率进行交叉变异，得到子代
     * 
     * @param pop 父代种群
     * @param qus 数据
     * @return pop，下一代种群
     */
    @Override
    public PopofNSGAII Variation1(Pop ParentPop, Qus qus) {
        PopofNSGAII pop = (PopofNSGAII)ParentPop;
        int popsize = pop.getPopsize();
        PopofNSGAII childPop = new PopofNSGAII(popsize);
        PopofNSGAII MatingPool = MatingSelection(popsize/2+1);
        // 按顺序两两进行交叉变异
        for (int i = 0; i < popsize/2; i++) {
            IndividualofNSGAII parent1 = MatingPool.getIndividual(i);
            IndividualofNSGAII parent2 = MatingPool.getIndividual(i+1);
            IndividualofNSGAII child1 = new IndividualofNSGAII(parent1);
            IndividualofNSGAII child2 = new IndividualofNSGAII(parent2);
            // 交配池中按顺序滚动，两两进行排序编码的交叉，将 MatingPool 扩展到 popsize 的规模
            if (new Random().nextDouble() < this.pc) {
                ArrayList<ArrayList<Integer>> sortingList = new ArrayList<>(
                    PBX(parent1.getCode().getSortCode(), parent2.getCode().getSortCode())
                );
                child1.getCode().setSortCode(sortingList.get(0));
                child2.getCode().setSortCode(sortingList.get(1));
            }
            childPop.deepsetIndividual(i, child1);
            childPop.deepsetIndividual(popsize/2+i, child2);
        }
        for (int i = 0; i < popsize; i++) {
            // 交配池中每个个体的排序编码执行反转突变
            if (new Random().nextDouble() < this.pms) {
                Code childCode = SwapMutation(childPop.getIndividual(i).getCode());
                childPop.getIndividual(i).setCode(childCode);
            }
            // 交配池中每个个体采用邻域搜索，对配置编码和操作编码进行变异搜索
            if (new Random().nextDouble() < this.pmv) {
                ArrayList VNSlist = VNS(childPop.getIndividual(i), qus);
                childPop.setIndividual(i, (IndividualofNSGAII)VNSlist.get(new Random().nextInt(VNSlist.size())));
            }
        }

        return childPop;
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
    // @Override
    // public PopofNSGAII Variation2(Pop pop, Qus qus) {
    //     // 每次从混合种群中选出 this.TournamentSize 个个体，并将里面最优个体放入交配池
    //     int popsize = pop.getPopsize();
    //     PopofNSGAII childpop = new PopofNSGAII(popsize);                    // 子代种群
    //     // Generate offspring population from parent population by binary tournament selection and then VNO
    //     int index = 0;
    //     for (int i = 0; i < popsize && index < popsize; i++) {
    //         int r1 = new Random().nextInt(popsize);
    //         int r2 = new Random().nextInt(popsize);
    //         while (r1 == r2) {
    //             r2 = new Random().nextInt(popsize);
    //         }
    //         IndividualofNSGAII individual1 = pop.getIndividual(r1);
    //         IndividualofNSGAII individual2 = pop.getIndividual(r2);
    //         IndividualofNSGAII selectIndividual = new IndividualofNSGAII();
    //         if (individual1.getR() < individual2.getR()) {
    //             selectIndividual = individual1;
    //         } else {
    //             if (individual2.getR() < individual1.getR()) {
    //                 selectIndividual = individual2;
    //             } else {
    //                 if (individual1.getD() >= individual2.getD()) {
    //                     selectIndividual = individual1;
    //                 } else {
    //                     selectIndividual = individual2;
    //                 }
    //             }
    //         }
    //         if (new Random().nextDouble() < this.pmv) {
    //             List<IndividualofNSGAII> childlist = VNS(selectIndividual, qus);
    //             for (int j = 0; j < childlist.size() && index < childpop.getPopsize(); j++, index++) {
    //                 childpop.setIndividual(index, childlist.get(j));
    //             }
    //         } else {
    //             childpop.setIndividual(index, selectIndividual);
    //             index++;
    //         }
    //     }

    //     // 子代按顺序滚动，两两进行排序编码的交叉
    //     for (int i = 0; i < popsize - 1; i=i+2) {
    //         if (new Random().nextDouble() < this.pc) {
    //             IndividualofNSGAII child1 = new IndividualofNSGAII(childpop.getIndividual(i));
    //             IndividualofNSGAII child2 = new IndividualofNSGAII(childpop.getIndividual(i+1));
    //             ArrayList<ArrayList<Integer>> sortingList = new ArrayList<>(
    //                 PBX(child1.getCode().getSortCode(), child2.getCode().getSortCode())
    //             );
    //             childpop.getIndividual(i).getCode().setSortCode(sortingList.get(0));
    //             childpop.getIndividual(i+1).getCode().setSortCode(sortingList.get(1));
    //         }
    //     }
    //     // 对交叉后的每个个体的排序编码执行反转突变
    //     for (int i = 0; i < popsize; i++) {
    //         if (new Random().nextDouble() < this.pms) {
    //             IndividualofNSGAII child = new IndividualofNSGAII(childpop.getIndividual(i));
    //             Code childCode = SwapMutation(child.getCode());
    //             childpop.getIndividual(i).setCode(childCode);
    //         }
    //     }

    //     return childpop;
    // }


    /**
     * 交叉变异
     * 
     * <p> 随机选择个体进行锦标赛，然后概率 VNS 得到交配池，再随机进行 PBX 和 SwapMutation
     * 
     * @param pop 父代种群
     * @param qus 数据
     * @return 子代直接替入self.pop，成为下一代种群
     */
    @Override
    public PopofNSGAII Variation2(Pop ParentPop, Qus qus) {
        PopofNSGAII pop = (PopofNSGAII) ParentPop;
        // 每次从混合种群中选出 this.TournamentSize 个个体，并将里面最优个体VNO后放入交配池
        int popsize = pop.getPopsize();
        PopofNSGAII childPop = new PopofNSGAII(popsize);                 // 子代种群
        PopofNSGAII MatingPool = MatingSelection(popsize);               // 记录配置和操作编码变化的个体
        // VNS
        for (int i = 0; i < popsize; i++) {
            ArrayList VNSlist = VNS(childPop.getIndividual(i), qus);
            MatingPool.setIndividual(i, (IndividualofNSGAII)VNSlist.get(new Random().nextInt(VNSlist.size())));
        }
        // 子代随机选择，两两进行排序编码的交叉
        for (int i = 0; i < popsize-1; i=i+2) {
            int r1 = new Random().nextInt(popsize);
            int r2 = new Random().nextInt(popsize);
            while (r1 == r2) {
                r2 = new Random().nextInt(popsize);
            }
            IndividualofNSGAII child1 = new IndividualofNSGAII(MatingPool.getIndividual(r1));
            IndividualofNSGAII child2 = new IndividualofNSGAII(MatingPool.getIndividual(r2));
            if (new Random().nextDouble() < this.pc) {
                ArrayList<ArrayList<Integer>> sortingList = new ArrayList<>(
                    PBX(child1.getCode().getSortCode(), child2.getCode().getSortCode())
                );
                child1.getCode().setSortCode(sortingList.get(0));
                child2.getCode().setSortCode(sortingList.get(1));
                childPop.deepsetIndividual(i, child1);
                childPop.deepsetIndividual(i+1, child2);
            } else {
                childPop.deepsetIndividual(i, child1);
                childPop.deepsetIndividual(i+1, child2);
            }
        }
        // 对交叉后的每个个体的排序编码执行反转突变
        for (int i = 0; i < popsize; i++) {
            IndividualofNSGAII child = new IndividualofNSGAII(childPop.getIndividual(i));
            childPop.getIndividual(i).setCode(SwapMutation(child.getCode()));
        }

        return childPop;
    }

    /**
     * Generate offspring population and perform variation
     * @param pop parent population
     * @param qus data
     * @return Pop 子代种群
     */
    @Override
    public PopofNSGAII Variation3(Pop pop, Qus qus) {
        // 注释开始*************************************************
        int popsize = pop.getPopsize();
        PopofNSGAII MatingPool = MatingSelection(popsize/2);
        PopofNSGAII childPop = new PopofNSGAII(popsize);
        // randomly select parents to perfrom VNO, PBX, and SM for configuration code and sorting code
        for (int i = 0; i < popsize - 1; i = i + 2) {
            IndividualofNSGAII parent = MatingPool.getIndividual(i/2);
            IndividualofNSGAII child1 = new IndividualofNSGAII(parent);
            IndividualofNSGAII child2 = new IndividualofNSGAII(parent);
            // VNO
            if (new Random().nextDouble() < this.pmv) {
                ArrayList VNSlist = VNS(parent, qus);
                if (VNSlist.size() >= 2) {
                    int r1 = new Random().nextInt(popsize);
                    int r2 = new Random().nextInt(popsize);
                    while (r1 == r2) {
                        r2 = new Random().nextInt(popsize);
                    }
                    child1 = new IndividualofNSGAII((IndividualofNSGAII)VNSlist.get(r1));
                    child2 = new IndividualofNSGAII((IndividualofNSGAII)VNSlist.get(r2));
                } else {
                    child1 = new IndividualofNSGAII((IndividualofNSGAII)VNSlist.get(0));
                    VNSlist = VND(child2, qus);
                    child2 = new IndividualofNSGAII((IndividualofNSGAII)VNSlist.get(0));
                }
            }
            // PBX
            if (new Random().nextDouble() < this.pc) {
                ArrayList<ArrayList<Integer>> sortingList = new ArrayList<>(
                    PBX(parent.getCode().getSortCode(), MatingPool.getIndividual(new Random().nextInt(popsize/2)).getCode().getSortCode())
                );
                child1.getCode().setSortCode(sortingList.get(0));
                child2.getCode().setSortCode(sortingList.get(1));
            }
            // SM
            if (new Random().nextDouble() < this.pms) {
                Code childCode1 = SwapMutation(child1.getCode());
                Code childCode2 = SwapMutation(child2.getCode());
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
     * Combine parent population and offspring population to form a combined population
     * @param pop1
     * @param pop2
     * @return
     */
    public PopofNSGAII getMergePop(PopofNSGAII pop1, PopofNSGAII pop2) {
        PopofNSGAII mergePop = new PopofNSGAII(pop1.getPopsize() + pop2.getPopsize());
        IndividualofNSGAII[] poplist1 = pop1.getIndividuals();
        IndividualofNSGAII[] poplist2 = pop2.getIndividuals();
        for (int i = 0; i < poplist1.length; i++) {
            mergePop.deepsetIndividual(i, poplist1[i]);
        }
        for (int i = 0; i < poplist2.length; i++) {
            mergePop.deepsetIndividual(i + pop1.getPopsize(), poplist2[i]);
        }

        return mergePop;
    }

    public void UpdatePop(int popsize) {
        int count = 0;
        PopofNSGAII childPop = new PopofNSGAII(popsize);
        for (ArrayList<Integer> arrayList : this.F) {
            if (popsize - count >= arrayList.size()) {
                for (int i = 0; i < arrayList.size() && count < popsize; i++) {
                    childPop.deepsetIndividual(count, super.getPop().getIndividual(arrayList.get(i)));
                    count++;
                }
            } else {
                ArrayList<IndividualofNSGAII> rankIndividuals = new ArrayList<>();
                for (Integer integer : arrayList) {
                    rankIndividuals.add((IndividualofNSGAII) super.getPop().getIndividual(integer));
                }
                ArrayList<IndividualofNSGAII> CrowdedComparison = Comparison(rankIndividuals);
                for (int i = 0; i < CrowdedComparison.size() && count < popsize; i++) {
                    childPop.deepsetIndividual(count, CrowdedComparison.get(i));
                    count++;
                }
                break;
            }
        }
        super.setPop(childPop);
    }


    // SET methods
    public void setIndividual(int i, IndividualofNSGAII individual) {
        super.getPop().setIndividual(i, individual);
    }
    public void setPC(double pc) {
        this.pc = pc;
    }
    public void setPM(double pms) {
        this.pms = pms;
    }
    public void setPMV(double pmv) {
        this.pmv = pmv;
    }
    public void setF(ArrayList<ArrayList<Integer>> F) {
        this.F = F;
    }

    // GET methods
    public PopofNSGAII getPop() {
        return (PopofNSGAII) super.getPop();
    }
    public IndividualofNSGAII getpop(int i) {
        return (IndividualofNSGAII) super.getPop().getIndividual(i);
    }
    public double getPC() {
        return this.pc;
    }
    public double getPMS() {
        return this.pms;
    }
    public double getPMV() {
        return this.pmv;
    }
    public ArrayList<ArrayList<Integer>> getF() {
        return this.F;
    }
}