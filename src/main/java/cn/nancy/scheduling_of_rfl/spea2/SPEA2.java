package cn.nancy.scheduling_of_rfl.spea2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import cn.nancy.scheduling_of_rfl.*;

public class SPEA2 extends Algorithem{
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
            this.mergePop.deepsetIndividual(i, super.getPop().getIndividual(i));
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
            // TODO: 检查是否真的成果设置了 S
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
            // pop.setD(i, CalculateDensity(i, pop));
            // pop.setFitness(i, pop.getIndividual(i).getD() + pop.getIndividual(i).getR());
            pop.setFitness(i, pop.getIndividual(i).getR());
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
        int index = 0;
        while (index < this.archive.getPopsize() || (index < sortlist.size() && sortlist.get(index).getFitness() < 1)) {
            selectPop.add(sortlist.get(index));
            index++;
        }
        // 判断非劣解个数是否等于 archive 要求的大小，若超出，则用 k-th 近邻法删去
        if (index > this.archive.getPopsize()) {
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
        }
        this.archive = new PopofSPEA2(this.archive.getPopsize());
        for (int i = 0; i < selectPop.size(); i++) {
            this.archive.deepsetIndividual(i, selectPop.get(i));
        }
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
