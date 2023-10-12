package cn.nancy.scheduling_of_rfl.MOEA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Callable;

import cn.nancy.scheduling_of_rfl.DataStore;
import cn.nancy.scheduling_of_rfl.Individual;
import cn.nancy.scheduling_of_rfl.Qus;

public class AllInMOEA implements Callable<String> {
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
    /**
     * 问题数据
     */
    Qus qus;
    /**
     * 读取、存储路径
     */
    private String p;
    /**
     * 最大迭代次数
     */
    private int MaxIteration;
    /**
     * 种群规模
     */
    private int popsize;
    /**
     * 权重参数
     */
    private int H;
    /**
     * 邻居参数
     */
    int T;

    public AllInMOEA(Qus qus, String p, int MaxIteration, int H, int T) {
        this.runningtime = Long.MIN_VALUE;
        this.lastiteration = 0;
        this.besttotaldelay = Double.MAX_VALUE;
        this.bestutilization = 0;
        this.qus = qus;
        this.p = p;
        this.MaxIteration = MaxIteration;
        this.popsize = H + 1;
        this.H = H;
        this.T = T;
        DataStore.createNewDirectorys(p);
        DataStore.createNewFile(p + "\\uOfA.txt");
        DataStore.createNewFile(p + "\\tOfA.txt");
        DataStore.createNewFile(p + "\\topuOfA.txt");
        DataStore.createNewFile(p + "\\bottomtOfA.txt");
        DataStore.createNewFile(p + "\\bottomuOfA.txt");
        DataStore.createNewFile(p + "\\toptOfA.txt");
    }

    @Override
    public String call() throws Exception {
        System.out.println(
    			"\033[32m" + String.join(" -", Collections.nCopies(11, " -"))
    			+ " MOEA/D_" + " start \tMaxIteration: " + this.MaxIteration
    			+ ", Popsize: " + this.popsize
    			+ String.join(" -", Collections.nCopies(11, " -")) + "\t\t\033[0m");
        double tOfA = 0;
        double uOfA = 0;
        double bottomtOfA = Double.MAX_VALUE;
        double topuOfA = Double.MIN_VALUE;
        double toptOfA = Double.MIN_VALUE;
        double bottomuOfA  = Double.MAX_VALUE;

        long startTime = System.currentTimeMillis();    //获取开始时间
        MOEA moea = new MOEA(this.MaxIteration, this.H, this.T, null, qus);
        while (! Thread.currentThread().isInterrupted() && ! moea.TerminateIteration(this.lastiteration)) {
            // 更新
            moea.Update(qus);

            for (int i = 0; i < popsize; i++) {
                double u = moea.getPop().getIndividual(i).getDecode().getUtilization();
                double t = moea.getPop().getIndividual(i).getDecode().getTotalDelay();
                tOfA += t;
                uOfA += u;
                if (u > topuOfA) topuOfA = u;
                if (t < bottomtOfA) bottomtOfA = t;
                if (u < bottomuOfA) bottomuOfA = u;
                if (t > toptOfA) toptOfA = t;
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
            	System.out.println("\033[32mMOEA/D" + " \t\t\033[0miteration " + this.lastiteration + "\t\tbest total delay: " + bottomtOfA + "\t\tbest utilization: " + topuOfA);
			}
            this.lastiteration++;

            tOfA = 0;
            uOfA = 0;
            bottomtOfA = Double.MAX_VALUE;
            topuOfA = Double.MIN_VALUE;
            toptOfA = Double.MIN_VALUE;
            bottomuOfA = Double.MAX_VALUE;
        }

        long endTime = System.currentTimeMillis();    //获取结束时间

        DataStore.createNewFile(p + "\\GantteData.txt");
        DataStore.createNewFile(p + "\\ParetoFront.txt");
        DataStore.createNewFile(p + "\\popObj.txt");
        for (int i = 0; i < popsize; i++) {
            ArrayList<Object> objective =  new ArrayList<>();
            objective.add(moea.getPop().getIndividual(i).getDecode().getTotalDelay());
            objective.add(moea.getPop().getIndividual(i).getDecode().getUtilization());
            DataStore.writefile("{\"" + (i + 1) + "\":" + objective + "}", p + "\\popObj.txt");
        }
        int i = 0;
        for (IndividualofMOEA ep : moea.getEP()) {
                ArrayList<Object> objective =  new ArrayList<>();
                objective.add(ep.getDecode().getTotalDelay());
                objective.add(ep.getDecode().getUtilization());
                DataStore.writefile("{\"" + (i + 1) + "\"" + ":" + objective + "}", p + "\\ParetoFront.txt");
                DataStore.writefile(DataStore.TimetabletoJOSN((Individual)ep), p + "\\GantteData.txt");
        }

        runningtime = endTime - startTime;

        return "\033[33mMOEA/D" + "\t\t运行时间：" + this.runningtime/1000 + "s  \t迭代代数：" + this.lastiteration + "  \tbest total delay: " + this.besttotaldelay + "  \tbest utilization: " + this.bestutilization + "\033[0m";
    }
}
