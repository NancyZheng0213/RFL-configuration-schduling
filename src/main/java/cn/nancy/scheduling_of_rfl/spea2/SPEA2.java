package cn.nancy.scheduling_of_rfl.spea2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import cn.nancy.scheduling_of_rfl.*;

public class SPEA2 extends Algorithm{
    /**
     * 精英种群
     */
    private PopofSPEA2 archive;
    /**
     * 混合种群
     */
    private PopofSPEA2 mergePop;
    /**
     * k 近邻参数
     */
    private int k;

    /**
     * 初始化种群
     * @param popNum 种群规模，必须为偶数
     * @param bestpopNum 精英种群规模
     * @param MaxIteration 最大迭代次数
     * @param TournamentSize 竞标赛选择个数
     * @param qus 数据
     */
    public SPEA2(int popNum,int bestpopNum, int MaxIteration, int TournamentSize, Qus qus) {
        super(MaxIteration, TournamentSize);
        PopofSPEA2 pop = new PopofSPEA2(popNum);
        pop.encode(qus.getPartsNum(), qus.getMachine(), qus.getProcessNum(), qus.getProcess(), qus.getAlternativeMachine());
        pop.decode(qus.getMachineTime(), qus.getDemand(), qus.getSetupTime(), qus.getDueDays());
        super.setPop(pop);
        this.archive = new PopofSPEA2(bestpopNum);
        this.archive.encode(qus.getPartsNum(), qus.getMachine(), qus.getProcessNum(), qus.getProcess(), qus.getAlternativeMachine());
        this.archive.decode(qus.getMachineTime(), qus.getDemand(), qus.getSetupTime(), qus.getDueDays());
        this.k = (int) Math.round(Math.sqrt(popNum + bestpopNum));
    }

    /**
     * 将 pop 和 archive 混合在一起，此处只是复制储存原有的个体的地址，而不是深拷贝个体
     */
    public void MergePop() {
        int popsize = super.getPop().getPopsize();
        this.mergePop = new PopofSPEA2(popsize + this.archive.getPopsize());
        for (int i = 0; i < popsize; i++) {
            this.mergePop.deepsetIndividual(i, (IndividualofSPEA2)super.getPop().getIndividual(i));
        }
        for (int i = 0; i < this.archive.getPopsize(); i++) {
            this.mergePop.deepsetIndividual(i + popsize, this.archive.getIndividual(i));
        }
    }

    /**
     * Fitness assignment，计算混合种群的个体适应度，执行此函数前要保证种群已经正确解码
     */
    public void CalculateFitness(PopofSPEA2 pop) {
        // 计算 S，即 i 支配的个体数
        for (int i = 0; i < pop.getPopsize(); i++) {
            Decode decodingi = pop.getIndividual(i).getDecode();
            int totaldelay = decodingi.getTotalDelay();
            double utilization = decodingi.getUtilization();
            int s = 0;
            for (int j = 0; j < pop.getPopsize(); j++) {
                if (j != i) {
                    Decode decodingj = pop.getIndividual(j).getDecode();
                    // 当 j 被 i 完全支配时，i 的 S 值加一
                    if (
                        (totaldelay < decodingj.getTotalDelay() && utilization >= decodingj.getUtilization()) || 
                        (totaldelay <= decodingj.getTotalDelay() && utilization > decodingj.getUtilization())
                    ) {
                        s += 1;
                    }
                }
            }
            pop.setS(i, s);
        }
        // 计算 R，即支配 i 的个体的 S 值总和
        for (int i = 0; i < pop.getPopsize(); i++) {
            Decode decodingi = pop.getIndividual(i).getDecode();
            int totaldelay = decodingi.getTotalDelay();
            double utilization = decodingi.getUtilization();
            int r = 0;
            for (int j = 0; j < pop.getPopsize(); j++) {
                if (j != i) {
                    Decode decodingj = pop.getIndividual(j).getDecode();
                    // 当 i 被 j 完全支配时，j 的 R 值加上 i 的 S 值
                    if (
                        (totaldelay >= decodingj.getTotalDelay() && utilization < decodingj.getUtilization()) ||
                        (totaldelay > decodingj.getTotalDelay() && utilization <= decodingj.getUtilization())
                    ) {
                        r += pop.getIndividual(j).getS();
                    }
                }
            }
            pop.setR(i, r);
        }
        // 计算 D 和适应度
        for (int i = 0; i < pop.getPopsize(); i++) {
            pop.setD(i, CalculateDensity(i, pop));
            pop.setFitness(i, pop.getIndividual(i).getD() + pop.getIndividual(i).getR());
            // pop.setFitness(i, pop.getIndividual(i).getR());
        }
    }

    /**
     * 计算 pop 中第 i 个个体的密度值，返回 D 值
     * 
     * <p>采用 k-th 近邻法，引入了 density information 区分相同 raw fitness value 的个体
     * <p>SPEA2中的 density estimation technique 是一种适应性的K近邻方法，任意点的 density 是 k 个邻居点的距离函数。这篇文章直接取第 k 个邻居的距离倒数作为 density estimate。
     */
    public double CalculateDensity(int i, PopofSPEA2 pop) {
        // 为了方便变邻域搜索时计算适应度，此处跳过变邻域中规模小于 k 的种群
        if (this.k <= pop.getPopsize()) {
            // 无量纲化
            double[] utilization = new double[pop.getPopsize()];
            double[] totalDelay = new double[pop.getPopsize()];
            double maxutilization = -1;
            double maxtotaldelay = -1;
            for (int index = 0; index < totalDelay.length; index++) {
                utilization[index] = pop.getIndividual(index).getDecode().getUtilization();
                if (utilization[index] > maxutilization) {
                    maxutilization = utilization[index];
                }
                totalDelay[index] = pop.getIndividual(index).getDecode().getTotalDelay();
                if (totalDelay[index] > maxtotaldelay) {
                    maxtotaldelay = totalDelay[index];
                }
            }
            for (int index = 0; index < totalDelay.length; index++) {
                utilization[index] /= maxutilization;
                totalDelay[index] /= maxtotaldelay;
            }
            // 计算 σ
            ArrayList<Double> xigemaList = new ArrayList<>();
            for (int index = 0; index < totalDelay.length; index++) {
                if (index != i) {
                    double xigema = Math.sqrt(
                        Math.pow((utilization[i] - utilization[index]), 2) + Math.pow((totalDelay[i] - totalDelay[index]), 2)
                    );
                    xigemaList.add(xigema);
                }
            }
            // 通过比较器实现 σ 的排序
            Collections.sort(xigemaList);
            pop.getIndividual(i).setXigema(xigemaList.get(this.k - 1));

            return 1 / (xigemaList.get(this.k - 1) + 2);
        } else {
            return 0;
        }
    }

    public double CalculateDensity(int i, ArrayList<IndividualofSPEA2> poplist) {
        // 为了方便变邻域搜索时计算适应度，此处跳过变邻域中规模小于 k 的种群
        if (this.k <= poplist.size()) {
            // 无量纲化
            double[] utilization = new double[poplist.size()];
            double[] totalDelay = new double[poplist.size()];
            double maxutilization = -1;
            double maxtotaldelay = -1;
            for (int index = 0; index < totalDelay.length; index++) {
                utilization[index] = poplist.get(index).getDecode().getUtilization();
                if (utilization[index] > maxutilization) {
                    maxutilization = utilization[index];
                }
                totalDelay[index] = poplist.get(index).getDecode().getTotalDelay();
                if (totalDelay[index] > maxtotaldelay) {
                    maxtotaldelay = totalDelay[index];
                }
            }
            for (int index = 0; index < totalDelay.length; index++) {
                utilization[index] /= maxutilization;
                totalDelay[index] /= maxtotaldelay;
            }
            // 计算 σ
            ArrayList<Double> xigemaList = new ArrayList<>();
            for (int index = 0; index < totalDelay.length; index++) {
                if (index != i) {
                    double xigema = Math.sqrt(
                        Math.pow((utilization[i] - utilization[index]), 2) + Math.pow((totalDelay[i] - totalDelay[index]), 2)
                    );
                    xigemaList.add(xigema);
                }
            }
            // 通过比较器实现 σ 的排序
            Collections.sort(xigemaList);
            poplist.get(i).setXigema(xigemaList.get(this.k - 1));

            return 1 / (xigemaList.get(this.k - 1) + 2);
        } else {
            return 0;
        }
    }

    /**
     * 采用锦标赛从 archive 中选择个体填充交配池
     */
    @Override
    public PopofSPEA2 MatingSelection(int MatingPoolSize) {
        int popsize = this.archive.getPopsize();
        PopofSPEA2 MatingPool = new PopofSPEA2(MatingPoolSize);
        PopofSPEA2 seletPop = new PopofSPEA2(super.getTournamentPara());
        // 每次从混合种群中选出 TournamentPara 个个体，并将里面最优个体放入交配池
        int index = 0;  // 当前正在选择第几个个体
        while (index < MatingPool.getPopsize()) {
            for (int i = 0; i < super.getTournamentPara(); i++) {
                seletPop.setIndividual(i, this.archive.getIndividual(new Random().nextInt(popsize)));
            }
            ArrayList<IndividualofSPEA2> sortList = seletPop.SortFitness();// 按适应度从小到大排序
            MatingPool.deepsetIndividual(index, sortList.get(0));
            index++;
        }
        
        return MatingPool;
    }

    /**
     * Environmental selection，将非劣解填满 archive
     * 
     */
    public void Selection() {
        ArrayList<IndividualofSPEA2> selectPop = new ArrayList<>();  // 记录选择的个体
        ArrayList<IndividualofSPEA2> sortlist = this.mergePop.SortFitness();
        // 按 R 从小到大填满 selestPop
        for (IndividualofSPEA2 individual : sortlist) {
            if (individual.getFitness() < 1) {
                selectPop.add(individual);
            }
        }
        // 判断非劣解个数是否等于 archive 要求的大小，若超出，则用 k-th 近邻法删去
        if (selectPop.size() > this.archive.getPopsize()) {
            for (int i = 0; i < selectPop.size(); i++) {
                selectPop.get(i).setD(CalculateDensity(i, selectPop));
            }
            // 删除 σ 最小的点，直至大小满足需求
            Collections.sort(selectPop, new Comparator<IndividualofSPEA2>() {
                @Override
                public int compare(IndividualofSPEA2 individual1, IndividualofSPEA2 individual2) {
                    double xigema1 = individual1.getXigema();
                    double xigema2 = individual2.getXigema();
                    return xigema1 > xigema2 ? 1 : (xigema1 == xigema2 ? 0 : -1);
                }
            });
            while (selectPop.size() > this.archive.getPopsize()) {
                selectPop.remove(0);
            }
        } else for (int i = 0; i < this.mergePop.getPopsize() && selectPop.size() < this.archive.getPopsize(); i++) {
            selectPop.add(this.mergePop.getIndividual(i));
        }
        this.archive = new PopofSPEA2(this.archive.getPopsize());
        for (int i = 0; i < selectPop.size(); i++) {
            this.archive.deepsetIndividual(i, selectPop.get(i));
        }
    }

    /**
     * 交叉变异 1，子类按需覆盖
     * 
     * <p> 按顺序每两个一组进行交叉变异，得到子代
     * 
     * @param ParentPop 父代种群
     * @param qus 数据
     * @return PopofSPEA2 子代种群
     */
    public PopofSPEA2 Variation1(Pop ParentPop, Qus qus) {
        
        int popsize = ParentPop.getPopsize();
        PopofSPEA2 MatingPool = MatingSelection(popsize/2+1);
        PopofSPEA2 chilidPop = new PopofSPEA2(popsize);        // 子代

        for (int i = 0; i < popsize/2; i++) {
            IndividualofSPEA2 parent1 = MatingPool.getIndividual(i);
            IndividualofSPEA2 parent2 = MatingPool.getIndividual(i+1);
            IndividualofSPEA2 child1 = new IndividualofSPEA2(parent1);
            IndividualofSPEA2 child2 = new IndividualofSPEA2(parent2);
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
            ArrayList<Individual> childlist = VNS(child1, qus);
            chilidPop.deepsetIndividual(i, childlist.get(new Random().nextInt(childlist.size())));
            childlist = VNS(child2, qus);
            chilidPop.deepsetIndividual(popsize/2 + i, childlist.get(new Random().nextInt(childlist.size())));
        }

        return chilidPop;
    }

    /**
     * 交叉变异 2，子类按需覆盖
     * 
     * <p>将交配池中每个个体VNS，扩大交配池三倍，然后随机进行PBX和SwapMutation
     * 
     * @param ParentPop 父代种群
     * @param qus 数据
     * @return PopofSPEA2，下一代种群
     */
    public PopofSPEA2 Variation2(Pop ParentPop, Qus qus) {
        // 每次从混合种群中选出 tournamentPara 个个体，并将里面最优个体VNO后放入交配池
        int popsize = ParentPop.getPopsize();
        PopofSPEA2 childPop = new PopofSPEA2(popsize);         // 子代种群
        PopofSPEA2 MatingPool = MatingSelection(popsize);      // 记录配置和操作编码变化的个体
        // VNS
        for (int i = 0; i < popsize; i++) {
            ArrayList<Individual> childlist = VNS(MatingPool.getIndividual(i), qus);
            MatingPool.setIndividual(i, childlist.get(new Random().nextInt(childlist.size())));
        }
        // 子代随机选择，两两进行排序编码的交叉
        for (int i = 0; i < popsize - 1; i=i+2) {
            int r1 = new Random().nextInt(popsize);
            int r2 = new Random().nextInt(popsize);
            while (r1 == r2) {
                r2 = new Random().nextInt(popsize);
            }
            IndividualofSPEA2 child1 = new IndividualofSPEA2(MatingPool.getIndividual(r1));
            IndividualofSPEA2 child2 = new IndividualofSPEA2(MatingPool.getIndividual(r2));
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
            IndividualofSPEA2 child = new IndividualofSPEA2(childPop.getIndividual(i));
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
     * @return PopofSPEA2 子代种群
     */
    public PopofSPEA2 Variation3(Pop ParentPop, Qus qus) {
        int popsize = ParentPop.getPopsize();
        PopofSPEA2 MatingPool = MatingSelection(popsize/2);
        PopofSPEA2 childPop = new PopofSPEA2(popsize);
        // randomly select parents to perfrom VNO, PBX, and SM for configuration code and sorting code
        for (int i = 0; i < popsize - 1; i = i + 2) {
            IndividualofSPEA2 parent = MatingPool.getIndividual(i/2);
            IndividualofSPEA2 child1 = new IndividualofSPEA2(parent);
            IndividualofSPEA2 child2 = new IndividualofSPEA2(parent);
            // VNO
            ArrayList<Individual> VNSlist = VNS(parent, qus);
            if (VNSlist.size() >= 2) {
                int r1 = new Random().nextInt(popsize);
                int r2 = new Random().nextInt(popsize);
                while (r1 == r2) {
                    r2 = new Random().nextInt(popsize);
                }
                child1 = new IndividualofSPEA2((IndividualofSPEA2)VNSlist.get(r1));
                child2 = new IndividualofSPEA2((IndividualofSPEA2)VNSlist.get(r2));
            } else {
                child1 = new IndividualofSPEA2((IndividualofSPEA2)VNSlist.get(0));
                VNSlist = VND(child2, qus);
                child2 = new IndividualofSPEA2((IndividualofSPEA2)VNSlist.get(0));
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


    // SET methods
    public void setIndividual(int i, IndividualofSPEA2 individual) {
        super.getPop().setIndividual(i, individual);
    }
    public void setK(int k) {
        this.k = k;
    }

    // GET methods
    public PopofSPEA2 getpop() {
        return (PopofSPEA2) super.getPop();
    }
    public IndividualofSPEA2 getpop(int i) {
        return (IndividualofSPEA2) super.getPop().getIndividual(i);
    }
    public PopofSPEA2 getarchive() {
        return this.archive;
    }
    public IndividualofSPEA2 getarchive(int i) {
        return this.archive.getIndividual(i);
    }
    public PopofSPEA2 getmergePop() {
        return this.mergePop;
    }
    public IndividualofSPEA2 getmergePop(int i) {
        return this.mergePop.getIndividual(i);
    }
    public double getk() {
        return this.k;
    }
}
