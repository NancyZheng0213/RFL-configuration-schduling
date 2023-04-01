package cn.nancy.scheduling_of_rfl.spea2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import cn.nancy.scheduling_of_rfl.*;

public class SPEA2 {
    /**
     * 种群
     */
    private Pop pop;
    /**
     * 精英种群
     */
    private Pop archive;
    /**
     * 混合种群
     */
    private Pop mergePop;
    /**
     * k 近邻参数
     */
    private int k;
    /**
     * 竞标赛选择的参数大小
     */
    private int TournamentSize;
    /**
     * 最大迭代次数
     */
    private int MaxIteration;

    /**
     * 初始化种群
     * @param popNum 种群规模，必须为偶数
     * @param bestpopNum 精英种群规模
     * @param MaxIteration 最大迭代次数
     * @param TournamentSize 竞标赛选择个数
     * @param qus 数据
     */
    public SPEA2(int popNum,int bestpopNum, int MaxIteration, int TournamentSize, Qus qus) {
        this.pop = new Pop(popNum);
        this.archive = new Pop(bestpopNum);
        this.k = (int) Math.round(Math.sqrt(popNum + bestpopNum));
        this.TournamentSize = TournamentSize;
        this.MaxIteration = MaxIteration;
        this.pop.encode(qus.getPartsNum(), qus.getMachine(), qus.getProcessNum(), qus.getProcess(), qus.getAlternativeMachine());
        this.pop.decode(qus.getMachineTime(), qus.getDemand(), qus.getSetupTime(), qus.getDueDays());
        this.archive.encode(qus.getPartsNum(), qus.getMachine(), qus.getProcessNum(), qus.getProcess(), qus.getAlternativeMachine());
        this.archive.decode(qus.getMachineTime(), qus.getDemand(), qus.getSetupTime(), qus.getDueDays());
    }

    /**
     * 将 pop 和 archive 混合在一起，此处只是复制储存原有的个体的地址，而不是深拷贝个体
     */
    public void MergePop() {
        // // 为避免初始迭代精英集合为空带来的错误，需要判断精英集合是否为空
        // if (this.archive.getindividual(0).getcode().getSortCode().size() != 0) {
        //     int popnum = this.pop.getpopNum();
        //     this.mergePop = new Pop(popnum + this.archive.getpopNum());
        //     for (int i = 0; i < popnum; i++) {
        //         this.mergePop.deepsetIndividual(i, this.pop.getindividual(i));
        //     }
        //     for (int i = 0; i < this.archive.getpopNum(); i++) {
        //         this.mergePop.deepsetIndividual(i + popnum, this.archive.getindividual(i));
        //     }
        // } else {
        //     // this.mergePop = new Pop(this.pop.getpopNum());
        //     this.mergePop = this.pop;
        // }
        int popnum = this.pop.getpopNum();
        this.mergePop = new Pop(popnum + this.archive.getpopNum());
        for (int i = 0; i < popnum; i++) {
            this.mergePop.deepsetIndividual(i, this.pop.getindividual(i));
        }
        for (int i = 0; i < this.archive.getpopNum(); i++) {
            this.mergePop.deepsetIndividual(i + popnum, this.archive.getindividual(i));
        }
    }

    /**
     * Fitness assignment，计算混合种群的个体适应度，执行此函数前要保证种群已经正确解码
     */
    public void CalculateFitness(Pop pop) {
        // 计算 S，即 i 支配的个体数
        for (int i = 0; i < pop.getpopNum(); i++) {
            Decode decodingi = pop.getindividual(i).getdecode();
            int totaldelay = decodingi.getTotalDelay();
            double utilization = decodingi.getUtilization();
            int s = 0;
            for (int j = 0; j < pop.getpopNum(); j++) {
                if (j != i) {
                    Decode decodingj = pop.getindividual(j).getdecode();
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
        for (int i = 0; i < pop.getpopNum(); i++) {
            Decode decodingi = pop.getindividual(i).getdecode();
            int totaldelay = decodingi.getTotalDelay();
            double utilization = decodingi.getUtilization();
            int r = 0;
            for (int j = 0; j < pop.getpopNum(); j++) {
                if (j != i) {
                    Decode decodingj = pop.getindividual(j).getdecode();
                    // 当 i 被 j 完全支配时，j 的 R 值加上 i 的 S 值
                    if (
                        (totaldelay >= decodingj.getTotalDelay() && utilization < decodingj.getUtilization()) ||
                        (totaldelay > decodingj.getTotalDelay() && utilization <= decodingj.getUtilization())
                    ) {
                        r += pop.getindividual(j).getS();
                    }
                }
            }
            pop.setR(i, r);
        }
        // 计算 D 和适应度
        for (int i = 0; i < pop.getpopNum(); i++) {
            // pop.setD(i, CalculateDensity(i, pop));
            // pop.setFitness(i, pop.getindividual(i).getD() + pop.getindividual(i).getR());
            pop.setFitness(i, pop.getindividual(i).getR());
        }
    }

    /**
     * 计算 pop 中第 i 个个体的密度值，返回 D 值
     * 
     * <p>采用 k-th 近邻法，引入了 density information 区分相同 raw fitness value 的个体
     * <p>SPEA2中的 density estimation technique 是一种适应性的K近邻方法，任意点的 density 是 k 个邻居点的距离函数。这篇文章直接取第 k 个邻居的距离倒数作为 density estimate。
     */
    public double CalculateDensity(int i, Pop pop) {
        // 为了方便变邻域搜索时计算适应度，此处跳过变邻域中规模小于 k 的种群
        if (this.k <= pop.getpopNum()) {
            // 无量纲化
            double[] utilization = new double[pop.getpopNum()];
            double[] totalDelay = new double[pop.getpopNum()];
            double maxutilization = -1;
            double maxtotaldelay = -1;
            for (int index = 0; index < totalDelay.length; index++) {
                utilization[index] = pop.getindividual(index).getdecode().getUtilization();
                if (utilization[index] > maxutilization) {
                    maxutilization = utilization[index];
                }
                totalDelay[index] = pop.getindividual(index).getdecode().getTotalDelay();
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
            pop.getindividual(i).setxigema(xigemaList.get(this.k - 1));

            return 1 / (xigemaList.get(this.k - 1) + 2);
        } else {
            return 0;
        }
    }

    public double CalculateDensity(int i, ArrayList<Individual> poplist) {
        // 为了方便变邻域搜索时计算适应度，此处跳过变邻域中规模小于 k 的种群
        if (this.k <= poplist.size()) {
            // 无量纲化
            double[] utilization = new double[poplist.size()];
            double[] totalDelay = new double[poplist.size()];
            double maxutilization = -1;
            double maxtotaldelay = -1;
            for (int index = 0; index < totalDelay.length; index++) {
                utilization[index] = poplist.get(index).getdecode().getUtilization();
                if (utilization[index] > maxutilization) {
                    maxutilization = utilization[index];
                }
                totalDelay[index] = poplist.get(index).getdecode().getTotalDelay();
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
            poplist.get(i).setxigema(xigemaList.get(this.k - 1));

            return 1 / (xigemaList.get(this.k - 1) + 2);
        } else {
            return 0;
        }
    }

    /**
     * Environmental selection，将非劣解填满 archive
     * 
     */
    public void Selection() {
        ArrayList<Individual> selectPop = new ArrayList<>();  // 记录选择的个体
        ArrayList<Individual> sortlist = this.mergePop.SortFitness();
        // 按 R 从小到大填满 selestPop
        int index = 0;
        while (index < this.archive.getpopNum() || (index < sortlist.size() && sortlist.get(index).getFitness() < 1)) {
            selectPop.add(sortlist.get(index));
            index++;
        }
        // 判断非劣解个数是否等于 archive 要求的大小，若超出，则用 k-th 近邻法删去
        if (index > this.archive.getpopNum()) {
            for (int i = 0; i < selectPop.size(); i++) {
                selectPop.get(i).setD(CalculateDensity(i, selectPop));
            }
            // 删除 σ 最小的点，直至大小满足需求
            Collections.sort(selectPop, new Comparator<Individual>() {
                @Override
                public int compare(Individual individual1, Individual individual2) {
                    double xigema1 = individual1.getxigema();
                    double xigema2 = individual2.getxigema();
                    return xigema1 > xigema2 ? 1 : (xigema1 == xigema2 ? 0 : -1);
                }
            });
            while (selectPop.size() > this.archive.getpopNum()) {
                selectPop.remove(0);
            }
        }
        this.archive = new Pop(this.archive.getpopNum());
        for (int i = 0; i < selectPop.size(); i++) {
            this.archive.deepsetIndividual(i, selectPop.get(i));
        }
    }

    public void Variation(String KEY, Pop pop, Qus qus) {
        switch (KEY) {
            case "1":
                Variation1(pop, qus);
                break;
            case "2":
                Variation2(pop, qus);
                break;
            case "3":
                Variation4(pop, qus);
                break;
            default:
                throw new IllegalArgumentException("The KEY must be 1, 2 or 3.");
        }
    }

    /**
     * 交叉变异
     * 
     * <p>一半子代是配置和操作编码VNO得到的，另一半是调度排序编码重组变异得到的
     * 
     * @param pop 父代种群
     * @param qus 数据
     * @return 子代直接替入self.pop，成为下一代种群
     */
    private void Variation1(Pop pop, Qus qus) {
        // 每次从混合种群中选出 this.TournamentSize 个个体，并将里面最优个体放入交配池
        Pop MatingPool = new Pop(this.pop.getpopNum() / 2);     // 记录选择的个体
        Pop MatingPool1 = new Pop(this.pop.getpopNum() / 2);    // 记录排序编码变化的个体
        Pop MatingPool2 = new Pop(this.pop.getpopNum() / 2);    // 记录配置和操作编码变化的个体
        Pop seletPop = new Pop(this.TournamentSize);            // 记录待选择个体
        int index = 0;  // 当前正在选择第几个个体
        while (index < MatingPool.getpopNum()) {
            for (int i = 0; i < this.TournamentSize; i++) {
                seletPop.setIndividual(i, pop.getindividual(new Random().nextInt(pop.getpopNum())));
            }
            ArrayList<Individual> sortList = seletPop.SortFitness();// 按适应度从小到大排序
            MatingPool.deepsetIndividual(index, sortList.get(0));
            index++;
        }
        // 交配池中按顺序滚动，两两进行排序编码的交叉
        for (int i = 0; i < MatingPool.getpopNum() - 1; i++) {
            MatingPool1.getindividual(i).setcode(CrossOver(MatingPool.getindividual(i).getcode(), MatingPool.getindividual(i + 1).getcode()));
        }
        MatingPool1.getindividual(MatingPool.getpopNum() - 1).setcode(
            CrossOver(MatingPool.getindividual(MatingPool.getpopNum() - 1).getcode(), MatingPool.getindividual(0).getcode())
        );
        // 交配池中每个个体的排序编码执行反转突变
        for (int i = 0; i < MatingPool.getpopNum(); i++) {
            MatingPool1.getindividual(i).setcode(SwapMutation(MatingPool.getindividual(i).getcode()));
        }
        // 交配池中每个个体采用邻域搜索，对配置编码和操作编码进行变异搜索
        index = 0;
        for (int i = 0; i < MatingPool.getpopNum() && index < MatingPool2.getpopNum(); i++) {
            List<Individual> childlist = VNS(MatingPool.getindividual(i), qus);
            for (int j = 0; j < childlist.size() && index < MatingPool2.getpopNum(); j++, index++) {
                MatingPool2.setIndividual(index, childlist.get(j));
            }
        }

        // for (int i = 0; i < MatingPool2.getpopNum(); i++) {
        //     Individual child = VNS(MatingPool.getindividual(i), qus);
        //     MatingPool2.setIndividual(i, child);
        // }
        
        // for (int i = 0; i < MatingPool2.getpopNum();) {
        //     ArrayList<Individual> VNDlist = VND(MatingPool.getindividual(i), qus);
        //     for (int j = 0; j < VNDlist.size() && i < MatingPool2.getpopNum(); j++) {
        //         MatingPool2.setIndividual(i, VNDlist.get(j));
        //         i++;
        //     }
        // }
        // 合并两个变化种群
        for (int i = 0; i < MatingPool.getpopNum(); i++) {
            this.pop.deepsetIndividual(i, MatingPool1.getindividual(i));
        }
        for (int i = 0; i < MatingPool.getpopNum(); i++) {
            this.pop.deepsetIndividual(i + MatingPool1.getpopNum(), MatingPool2.getindividual(i));
        }
    }
    

    /**
     * 交叉变异
     * 
     * <p>将交配池中每个个体VNO，每次获取邻域中最优的个体替换父代个体，然后进行PBX和SwapMutation
     * 
     * @param pop 父代种群
     * @param qus 数据
     * @return 子代直接替入self.pop，成为下一代种群
     */
    private void Variation2(Pop pop, Qus qus) {
        // 每次从混合种群中选出 this.TournamentSize 个个体，并将里面最优个体放入交配池
        int popnum = this.pop.getpopNum();
        Pop childpop = new Pop(popnum);                 // 子代种群
        Pop MatingPool = new Pop(popnum);               // 记录配置和操作编码变化的个体
        Pop seletPop = new Pop(this.TournamentSize);    // 记录待选择个体
        int index = 0;  // 当前正在选择第几个个体
        while (index < MatingPool.getpopNum()) {
            for (int i = 0; i < this.TournamentSize; i++) {
                seletPop.setIndividual(i, pop.getindividual(new Random().nextInt(pop.getpopNum())));
            }
            ArrayList<Individual> sortList = seletPop.SortFitness();// 按适应度从小到大排序
            MatingPool.setIndividual(index, sortList.get(0));
            index++;
        }
        // MatingPool 中每个个体采用邻域搜索，对配置编码和操作编码进行变邻域搜索，将最优的填入子代种群
        index = 0;
        for (int i = 0; i < MatingPool.getpopNum() && index < childpop.getpopNum(); i++) {
            List<Individual> childlist = VNS(MatingPool.getindividual(i), qus);
            for (int j = 0; j < childlist.size() && index < childpop.getpopNum(); j++, index++) {
                childpop.setIndividual(index, childlist.get(j));
            }
        }
        
        // for (int i = 0; i < MatingPool.getpopNum(); i++) {
        //     Individual bestIndividual = VNS(MatingPool.getindividual(i), qus);

        //     // ArrayList<Individual> VNDlist = VND(bestIndividual, qus);
        //     // for (int j = 0; j < VNDlist.size(); j++) {
        //     //     Individual neighbor = VNDlist.get(j);
        //     //     neighbor.getdecode().setdecoding(neighbor.getcode(), qus.getMachineTime(), qus.getDemand(), qus.getSetupTime());
        //     //     neighbor.getdecode().utilization(qus.getPartsNum());
        //     //     neighbor.getdecode().totaldelay(neighbor.getcode().getSortCode(), qus.getDueDays());
        //     //     if (
        //     //         neighbor.getdecode().getUtilization()>=bestIndividual.getdecode().getUtilization() && neighbor.getdecode().getTotalDelay()<=bestIndividual.getdecode().getTotalDelay()
        //     //     ) {
        //     //         bestIndividual = new Individual(neighbor);
        //     //     }
        //     // }
            
        //     childpop.deepsetIndividual(i, bestIndividual);
        // }
        // 子代按顺序滚动，两两进行排序编码的交叉
        for (int i = 0; i < popnum - 1; i++) {
            Individual child = new Individual(childpop.getindividual(i));
            child.setcode(CrossOver(child.getcode(), childpop.getindividual(i + 1).getcode()));
            childpop.deepsetIndividual(i, child);
        }
        childpop.getindividual(popnum - 1).setcode(
            CrossOver(childpop.getindividual(popnum - 1).getcode(), childpop.getindividual(0).getcode())
        );
        // 对交叉后的每个个体的排序编码执行反转突变
        for (int i = 0; i < popnum; i++) {
            Individual child = new Individual(childpop.getindividual(i));
            child.setcode(SwapMutation(child.getcode()));
            // 赋值pop
            this.pop.deepsetIndividual(i, child);
        }    
    }


    /**
     * 交叉变异
     * 
     * <p>将交配池中每个个体VNO，扩大交配池三倍，然后进行PBX和SwapMutation
     * 
     * @param pop 父代种群
     * @param qus 数据
     * @return 子代直接替入self.pop，成为下一代种群
     */
    private void Variation3(Pop pop, Qus qus) {
        // 每次从混合种群中选出 this.TournamentSize 个个体，并将里面最优个体VNO后放入交配池
        int popnum = this.pop.getpopNum();
        Pop childpop = new Pop(popnum);                 // 子代种群
        Pop MatingPool = new Pop(popnum);               // 记录配置和操作编码变化的个体
        Pop seletPop = new Pop(this.TournamentSize);    // 记录待选择个体
        int index = 0;  // 当前正在选择第几个个体
        while (index < popnum) {
            for (int i = 0; i < this.TournamentSize; i++) {
                seletPop.setIndividual(i, pop.getindividual(new Random().nextInt(pop.getpopNum())));
            }
            ArrayList<Individual> sortList = seletPop.SortFitness();// 按适应度从小到大排序
            // 最优个体采用邻域搜索，得到的个体的填入交配池
            ArrayList<Individual> VNDlist = VND(sortList.get(0), qus);
            for (int j = 0; j < VNDlist.size() && index < popnum; j++) {
                MatingPool.setIndividual(index, VNDlist.get(j));
                index++;
            }
        }
        // 子代随机选择，两两进行排序编码的交叉
        for (int i = 0; i < popnum; i++) {
            Individual child = new Individual(MatingPool.getindividual(i));
            child.setcode(CrossOver(child.getcode(), MatingPool.getindividual(new Random().nextInt(popnum)).getcode()));
            childpop.deepsetIndividual(i, child);
        }
        // 对交叉后的每个个体的排序编码执行反转突变
        for (int i = 0; i < popnum; i++) {
            Individual child = new Individual(childpop.getindividual(i));
            child.setcode(SwapMutation(child.getcode()));
            // 赋值pop
            this.pop.deepsetIndividual(i, child);
        }    
    }

    /**
     * 交叉变异
     * 
     * <p>将交配池中每个个体VNS，扩大交配池三倍，然后随机进行PBX和SwapMutation
     * 
     * @param pop 父代种群
     * @param qus 数据
     * @return 子代直接替入self.pop，成为下一代种群
     */
    private void Variation4(Pop pop, Qus qus) {
        // 每次从混合种群中选出 this.TournamentSize 个个体，并将里面最优个体VNO后放入交配池
        int popnum = this.pop.getpopNum();
        Pop childpop = new Pop(popnum);                 // 子代种群
        Pop MatingPool = new Pop(popnum);               // 记录配置和操作编码变化的个体
        Pop seletPop = new Pop(this.TournamentSize);    // 记录待选择个体
        int index = 0;  // 当前正在选择第几个个体
        while (index < popnum) {
            for (int i = 0; i < this.TournamentSize; i++) {
                seletPop.setIndividual(i, pop.getindividual(new Random().nextInt(pop.getpopNum())));
            }
            ArrayList<Individual> sortList = seletPop.SortFitness();// 按适应度从小到大排序
            // 最优个体采用VNS，得到的个体的填入交配池
            List<Individual> childlist = VNS(sortList.get(0), qus);
            for (int i = 0; i < childlist.size() && index < popnum; i++, index++) {
                MatingPool.setIndividual(index, childlist.get(i));
            }
            // Individual child = VNS(sortList.get(0), qus);
            // MatingPool.setIndividual(index, child);
            // index++;
        }
        // 子代随机选择，两两进行排序编码的交叉
        for (int i = 0; i < popnum; i++) {
            Individual child = new Individual(MatingPool.getindividual(i));
            child.setcode(CrossOver(child.getcode(), MatingPool.getindividual(new Random().nextInt(popnum)).getcode()));
            childpop.deepsetIndividual(i, child);
        }
        // 对交叉后的每个个体的排序编码执行反转突变
        for (int i = 0; i < popnum; i++) {
            Individual child = new Individual(childpop.getindividual(i));
            child.setcode(SwapMutation(child.getcode()));
            // 赋值pop
            this.pop.deepsetIndividual(i, child);
        }    
    }


    /**
     * 采用 PBX （Position-based Crossover） 算子对排序编码执行交叉操作
     * @param code1
     * @param code2
     * @return Code
     */
    public Code CrossOver(Code code1, Code code2) {
        ArrayList<Integer> parent1 = new ArrayList<>(code1.getSortCode());
        ArrayList<Integer> parent2 = new ArrayList<>(code2.getSortCode());
        ArrayList<Integer> child = new ArrayList<>(Collections.nCopies(parent1.size(),-1));
        // 随机生成 0-1 序列，表示是否选择该基因
        ArrayList<Integer> randomtemp = new ArrayList<>();
        for (int i = 0; i < parent1.size(); i++) {
            randomtemp.add(new Random().nextInt(2));
        }
        for (int i = 0; i < parent1.size(); i++) {
            if (randomtemp.get(i) == 1) {
                child.set(i, parent1.get(i));
                parent2.remove(parent1.get(i));
            }
        }
        for (int i = 0; i < parent1.size(); i++) {
            if (child.get(i) == -1) {
                child.set(i, parent2.get(0));
                parent2.remove(0);
            }
        }
        Code childcode = new Code();
        childcode.setConfigurationCode(code1.getConfigurationCode());
        childcode.setOperationCode(code1.getOperationCode());
        childcode.setSortCode(child);

        return childcode;
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
    public List<Individual> VNS(Individual individual, Qus qus) {
        Decode individualDecode = individual.getdecode();
        List<Individual> childlist = new ArrayList<>();
        // Shaking
        Pop shakingPop = new Pop(0);
        while (shakingPop.getpopNum() == 0) {
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
        // 对 Shaking pop 中每一个个体进行 VND
        for (int i = 0; i < shakingPop.getpopNum(); i++) {
            Individual shakingneighbor = shakingPop.getindividual(i);
            ArrayList<Pop> VNDneighborhoods = SearchNeighborhoods(shakingneighbor, qus);
            Decode shakingneighbordecode = shakingneighbor.getdecode();
            while (VNDneighborhoods.size() > 0) {
                // 找到 shakingneighbor 的邻域中的最优解 bestneighbor，比较 bestneighbor 和 shakingneighbor 的适应度
                Pop VNDneighborhood = VNDneighborhoods.get(0);
                VNDneighborhoods.remove(0);
                if (VNDneighborhood.getpopNum() == 0) {
                    continue;
                } else {    // 如果 bestneighbor 优于 shakingneighbor，则替代，并且重新寻找邻域
                    for (int j = 0; j < VNDneighborhood.getpopNum(); j++) {
                        Individual neighbor = VNDneighborhood.getindividual(j);
                        Decode neighbordecode = neighbor.getdecode();
                        if (
                            (neighbordecode.getTotalDelay() <= shakingneighbordecode.getTotalDelay() && neighbordecode.getUtilization() > shakingneighbordecode.getUtilization()) ||
                            (neighbordecode.getTotalDelay() < shakingneighbordecode.getTotalDelay() && neighbordecode.getUtilization() >= shakingneighbordecode.getUtilization())
                        ) {
                            shakingneighbor = neighbor;
                            shakingneighbordecode = shakingneighbor.getdecode();
                            VNDneighborhoods = SearchNeighborhoods(shakingneighbor, qus);
                            break;
                        }
                    }   
                }
            }
            childlist.add(shakingneighbor);
        }

        return childlist;

        // ArrayList<Pop> Neighborhoods = SearchNeighborhoods(individual, qus);
        // // local search by VND
        // while (Neighborhoods.size() > 0) {
        //     int r = new Random().nextInt(Neighborhoods.size());
        //     Pop shakingPop = Neighborhoods.get(r);
        //     Neighborhoods.remove(r);
        //     if (shakingPop.getpopNum() == 0) {
        //         continue;
        //     } else {
        //         // 从 Shaking pop 中随机选择一个个体进行 VND
        //         int neighborindex = new Random().nextInt(shakingPop.getpopNum());
        //         Individual shakingneighbor = shakingPop.getindividual(neighborindex);
        //         shakingPop = null;  // 销毁变量，释放内存
        //         ArrayList<Pop> VNDneighborhoods = SearchNeighborhoods(shakingneighbor, qus);
        //         Decode shakingneighbordecode = shakingneighbor.getdecode();
        //         while (VNDneighborhoods.size() > 0) {
        //             // 找到 shakingneighbor 的邻域中的最优解 bestneighbor，比较 bestneighbor 和 shakingneighbor 的适应度
        //             Pop VNDneighborhood = VNDneighborhoods.get(0);
        //             VNDneighborhoods.remove(0);
        //             if (VNDneighborhood.getpopNum() == 0) {
        //                 continue;
        //             } else {
        //                 ArrayList<Individual> sortlist = VNDneighborhood.SortFitness();
        //                 Individual bestneighbor = sortlist.get(0);
        //                 // 如果 bestneighbor 优于 shakingneighbor，则替代，并且重新寻找邻域
        //                 Decode bestneighbordecode = bestneighbor.getdecode();
        //                 if (
        //                     (bestneighbordecode.getTotalDelay() <= shakingneighbordecode.getTotalDelay() && bestneighbordecode.getUtilization() > shakingneighbordecode.getUtilization()) ||
        //                     (bestneighbordecode.getTotalDelay() < shakingneighbordecode.getTotalDelay() && bestneighbordecode.getUtilization() >= shakingneighbordecode.getUtilization())
        //                 ) {
        //                     shakingneighbor = bestneighbor;
        //                     shakingneighbordecode = shakingneighbor.getdecode();
        //                     VNDneighborhoods = SearchNeighborhoods(shakingneighbor, qus);
        //                 }
        //             }
        //         }
        //         Decode childdecode = child.getdecode();
        //         if (
        //             (shakingneighbordecode.getTotalDelay() <= childdecode.getTotalDelay() && shakingneighbordecode.getUtilization() > childdecode.getUtilization()) ||
        //             (shakingneighbordecode.getTotalDelay() < childdecode.getTotalDelay() && shakingneighbordecode.getUtilization() >= childdecode.getUtilization())
        //         ) {
        //             child = new Individual(shakingneighbor);
        //             // Neighborhoods = SearchNeighborhoods(child, qus);
        //         }
        //     }
        // }

        // return child;
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
            if (Neighborhood.getpopNum() == 0) {
                continue;
            } else {
                ArrayList<Individual> sortlist = Neighborhood.SortFitness();
                Individual bestneighbor = sortlist.get(0);
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
        ArrayList<Integer> ConfigurationCode = new ArrayList<>(individual.getcode().getConfigurationCode());
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
            code.setSortCode(individual.getcode().getSortCode());
            codes.add(code);
        }

        // 将 code 邻域放入 pop 中封装
        Pop Neighborhood = new Pop(configurationCodList.size());
        for (int i = 0; i < Neighborhood.getpopNum(); i++) {
            Neighborhood.deepsetIndividual(i, individual);
            Neighborhood.getindividual(i).setcode(codes.get(i));
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
        Code code = individual.getcode();
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
            code.setConfigurationCode(individual.getcode().getConfigurationCode());
            code.setOperationCode(operationCodeList.get(i));
            code.setSortCode(individual.getcode().getSortCode());
            codes.add(code);
        }

        // 将 code 邻域放入 pop 中封装
        Pop Neighborhood = new Pop(operationCodeList.size());
        for (int i = 0; i < Neighborhood.getpopNum(); i++) {
            Neighborhood.deepsetIndividual(i, individual);
            Neighborhood.getindividual(i).setcode(codes.get(i));
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
        Code code = individual.getcode();
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
        code.setConfigurationCode(individual.getcode().getConfigurationCode());
        code.setOperationCode(newOperationCode);
        code.setSortCode(individual.getcode().getSortCode());

        // 将 code 邻域放入 pop 中封装
        Pop Neighborhood = new Pop(1);
        Neighborhood.deepsetIndividual(0, individual);
        Neighborhood.getindividual(0).setcode(code);
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

    public int getMaxIteration() {
        return this.MaxIteration;
    }
    public Pop getpop() {
        return this.pop;
    }
    public Individual getpop(int i) {
        return this.pop.getindividual(i);
    }
    public Pop getarchive() {
        return this.archive;
    }
    public Individual getarchive(int i) {
        return this.archive.getindividual(i);
    }
    public Pop getmergePop() {
        return this.mergePop;
    }
    public Individual getmergePop(int i) {
        return this.mergePop.getindividual(i);
    }
    public double getk() {
        return this.k;
    }
}
