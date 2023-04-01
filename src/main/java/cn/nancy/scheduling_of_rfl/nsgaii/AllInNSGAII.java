package cn.nancy.scheduling_of_rfl.nsgaii;

import java.util.ArrayList;
import java.util.Collections;

import cn.nancy.scheduling_of_rfl.*;

public class AllInNSGAII extends Thread {
    /**
     * 记录运行时间，单位：s
     */
    public long runningtime;
    /**
     * 记录运行代数
     */
    public int lastiteration;
    /**
     * 记录最佳 total delay，单位：min
     */
    public double besttotaldelay;
    /**
     * 记录最佳 utilization，单位：%
     */
    public double bestutilization;

    Qus qus;
    private String p;
    private int MaxIteration;
    private int popsize;
    private double pc;
    private double pms;
    private double pmv;

    /**
     * 规定迭代次数的NSGAII
     * @param filename 读取问题数据的地址
     * @param p 结果保留的地址
     * @param MaxIteration
     * @param popsize
     * @param pc
     * @param pms
     * @param pmv
     */
    public AllInNSGAII(Qus qus, String p, int MaxIteration, int popsize, double pc, double pms, double pmv) {
        this.runningtime = Long.MIN_VALUE;
        this.lastiteration = 0;
        this.besttotaldelay = Double.MAX_VALUE;
        this.bestutilization = 0;
        this.qus = qus;
        this.p = p;
        this.MaxIteration = MaxIteration;
        this.popsize = popsize;
        this.pc = pc;
        this.pms = pms;
        this.pmv = pmv;
        DataStore.createNewDirectorys(p);
        DataStore.createNewFile(p + "\\uOfA.txt");
        DataStore.createNewFile(p + "\\tOfA.txt");
        DataStore.createNewFile(p + "\\topuOfA.txt");
        DataStore.createNewFile(p + "\\bottomtOfA.txt");
        DataStore.createNewFile(p + "\\bottomuOfA.txt");
        DataStore.createNewFile(p + "\\toptOfA.txt");
        // String filename = "E:\\JavaProjects\\configuration and scheduling of MRFL\\case4.xlsx";
        // String p = "E:\\JavaProjects\\configuration and scheduling of MRFL\\result\\case4_group2\\NSGAII";
    }

    @Override
    public void run() {
    	System.out.println(
    			"\033[32m" + String.join(" -", Collections.nCopies(11, " -"))
    			+ " NSGAII start \tMaxIteration: " + this.MaxIteration
    			+ ", Popsize: " + this.popsize
    			+ String.join(" -", Collections.nCopies(11, " -")) + "\t\t\033[0m");
        double tOfA = 0;
        double uOfA = 0;
        double bottomtOfA = Double.MAX_VALUE;
        double topuOfA = Double.MIN_VALUE;
        double toptOfA = Double.MIN_VALUE;
        double bottomuOfA  = Double.MAX_VALUE;

        long startTime = System.currentTimeMillis();    //获取开始时间
        NSGAII nsgaii = new NSGAII(MaxIteration, popsize, pc, pms, pmv, qus);
        while (! Thread.currentThread().isInterrupted() && ! nsgaii.TerminateIteration(this.lastiteration)) {
            // Perform nondominated sorting of parent population. categorize population into several fronts with different ranks.
            nsgaii.NondominatedRank();
            // Generate offspring population
            Pop childPop = nsgaii.Variation(qus);
            // Combine parent population and offspring population to form a combined population.
            nsgaii.setPopulation(nsgaii.getMergePop(childPop, nsgaii.getPop()));
            nsgaii.getPop().decode(qus.getMachineTime(), qus.getDemand(), qus.getSetupTime(), qus.getDueDays());
            // Perform nondominated sorting to obtain different fronts and compute crowding distances of the combined population.
            nsgaii.NondominatedRank();
            // Create new offspring population by selecting fronts with highest ranks.
            nsgaii.UpdatePop(popsize);
            nsgaii.getPop().decode(qus.getMachineTime(), qus.getDemand(), qus.getSetupTime(), qus.getDueDays());

            tOfA = 0;
            uOfA = 0;
            bottomtOfA = Double.MAX_VALUE;
            topuOfA = Double.MIN_VALUE;
            toptOfA = Double.MIN_VALUE;
            bottomuOfA = Double.MAX_VALUE;
            
            for (int i = 0; i < popsize; i++) {
                double u = nsgaii.getPop().getIndividual(i).getDecode().getUtilization();
                double t = nsgaii.getPop().getIndividual(i).getDecode().getTotalDelay();
                tOfA += t;
                uOfA += u;
                if (u > topuOfA) {
                    topuOfA = u;
                }
                if (t < bottomtOfA) {
                    bottomtOfA = t;
                }
                if (u < bottomuOfA) {
                    bottomuOfA = u;
                }
                if (t > toptOfA) {
                    toptOfA = t;
                }
            }
            tOfA /= popsize;
            uOfA /= popsize;  
            DataStore.writefile(uOfA + "", p + "\\uOfA.txt");
            DataStore.writefile(tOfA + "", p + "\\tOfA.txt");
            DataStore.writefile(topuOfA + "", p + "\\topuOfA.txt");
            DataStore.writefile(bottomtOfA + "", p + "\\bottomtOfA.txt");
            DataStore.writefile(bottomuOfA + "", p + "\\bottomuOfA.txt");
            DataStore.writefile(toptOfA + "", p + "\\toptOfA.txt");
            besttotaldelay = bottomtOfA;
            bestutilization = topuOfA; 
            if (this.lastiteration % 10 == 0) {
            	System.out.println("\033[32mNSGAII\t\t\033[0miteration " + this.lastiteration + "\t\tbest total delay: " + bottomtOfA + "\t\tbest utilization: " + topuOfA);
			}
            this.lastiteration++;
        }

        long endTime = System.currentTimeMillis();    //获取结束时间

        DataStore.createNewFile(p + "\\GantteData.txt");
        DataStore.createNewFile(p + "\\ParetoFront.txt");
        DataStore.createNewFile(p + "\\archiveObj.txt");
        for (int i = 0; i < popsize; i++) {
            ArrayList<Object> objective =  new ArrayList<>();
            objective.add(nsgaii.getPop().getIndividual(i).getDecode().getTotalDelay());
            objective.add(nsgaii.getPop().getIndividual(i).getDecode().getUtilization());
            DataStore.writefile("{\"" + (i + 1) + "\":" + objective + "}", p + "\\archiveObj.txt");
            if (nsgaii.getPop().getIndividual(i).getR() == 1) {
                // objectivMap.put(i + 1, objective);
                DataStore.writefile("{\"" + (i + 1) + "\"" + ":" + objective + "}", p + "\\ParetoFront.txt");
                DataStore.writefile(DataStore.TimetabletoJOSN(nsgaii.getPop().getIndividual(i)), p + "\\GantteData.txt");
            }
            // archiveMap.put(i + 1, objective); 
        }
        
        // System.out.print("共有" + objectivMap.size() + "个pareto解，");
        
        // System.out.print("\033[32mNSGAII\033[0m\t\t程序运行时间：");
        runningtime = endTime - startTime;
        // long time = (endTime - startTime)/1000;
        // int s;
        // String timestrings[] = {" s", " min, ", " h, "};
        // int timeint[] = new int[3];
        // for (int i = 0; i < timestrings.length; i++) {
        //     s = (int)(time % 60);
        //     timeint[i] = s;
        //     time = (time - s)/60;
        // }
        // for (int i = timeint.length - 1; i >= 0 ; i--) {
        //     System.out.print(timeint[i] + timestrings[i]);
        // }
        // System.out.println("\t\tbest total delay: " + bottomtOfA + "\t\tbest utilization: " + topuOfA);

        if (!this.isAlive()) {
            System.out.println("\033[33mNSGAII\t\t运行时间：" + this.runningtime/1000 + "s  \t迭代代数：" + this.lastiteration + "  \tbest total delay: " + this.besttotaldelay + "  \tbest utilization: " + this.bestutilization + "\033[0m");
        }
    }
}
