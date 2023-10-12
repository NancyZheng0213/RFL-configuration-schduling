package cn.nancy.scheduling_of_rfl.spea2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Callable;

import cn.nancy.scheduling_of_rfl.*;

public class AllInSPEA2 implements Callable<String> {
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

    private Qus qus;
    private String p;
    private int popNum;
    private int bestpopNum;
    private int MaxIteration;
    private int TournamentSize;
    
    /**
     * 规定迭代次数的 SPEA2
     * @param filename 读取问题数据的地址
     * @param p 结果保留的地址
     * @param popNum
     * @param bestpopNum
     * @param MaxIteration
     * @param TournamentSize
     */
    public AllInSPEA2(Qus qus, String p, int popNum,int bestpopNum, int MaxIteration, int TournamentSize) {
        this.runningtime = Long.MIN_VALUE;
        this.lastiteration = 0;
        this.besttotaldelay = Double.MAX_VALUE;
        this.bestutilization = 0;
        this.qus = qus;
        this.p = p;
        this.popNum = popNum;
        this.bestpopNum = bestpopNum;
        this.MaxIteration = MaxIteration;
        this.TournamentSize = TournamentSize;
        // String filename = "E:\\JavaProjects\\configuration and scheduling of MRFL\\case4.xlsx";
        // String p = "E:\\JavaProjects\\configuration and scheduling of MRFL\\result\\case4_group2\\SPEA2_1";
        DataStore.createNewDirectorys(p);
        DataStore.createNewFile(p + "\\uOfA.txt");
        DataStore.createNewFile(p + "\\tOfA.txt");
        DataStore.createNewFile(p + "\\topuOfA.txt");
        DataStore.createNewFile(p + "\\bottomtOfA.txt");
        DataStore.createNewFile(p + "\\bottomuOfA.txt");
        DataStore.createNewFile(p + "\\toptOfA.txt");
        DataStore.createNewFile(p + "\\uOfM.txt");
        DataStore.createNewFile(p + "\\tOfM.txt");
        DataStore.createNewFile(p + "\\topuOfM.txt");
        DataStore.createNewFile(p + "\\bottomtOfM.txt");
        DataStore.createNewFile(p + "\\bottomuOfM.txt");
        DataStore.createNewFile(p + "\\toptOfM.txt");
        DataStore.createNewFile(p + "\\GantteData.txt");
        DataStore.createNewFile(p + "\\ParetoFront.txt");
        DataStore.createNewFile(p + "\\archiveObj.txt");
        DataStore.createNewFile(p + "\\popObj.txt");

        
    }

    @Override
    public String call() throws Exception{
    	System.out.println(
    			"\033[32m" + String.join(" -", Collections.nCopies(11, " -"))
    			+ " SPEA2"
    			+ " start \tMaxIteration: " + this.MaxIteration
    			+ ", Popsize: " + this.popNum
    			+ String.join(" -", Collections.nCopies(11, " -")) + "\t\t\033[0m");
        // 初始化数据记录
        double tOfA = 0;
        double uOfA = 0;
        double bottomtOfA = Double.MAX_VALUE;
        double topuOfA = Double.MAX_VALUE;
        double toptOfA = Double.MAX_VALUE;
        double bottomuOfA = Double.MAX_VALUE;
        double tOfM = 0;
        double uOfM = 0;
        double bottomtOfM = Double.MAX_VALUE;
        double topuOfM = Double.MAX_VALUE;
        double toptOfM = Double.MAX_VALUE;
        double bottomuOfM = Double.MAX_VALUE;

        // 算法开始
        long startTime = System.currentTimeMillis();    //获取开始时间
        SPEA2 speaii = new SPEA2(popNum, bestpopNum, MaxIteration, TournamentSize, qus);
        while (! Thread.currentThread().isInterrupted()) {
            // 计算适应度
            speaii.MergePop();
            speaii.CalculateFitness(speaii.getmergePop());
            // 更新外部存档
            speaii.Selection();

            tOfA = 0;
            uOfA = 0;
            bottomtOfA = Double.MAX_VALUE;
            topuOfA = Double.MIN_VALUE;
            toptOfA = Double.MIN_VALUE;
            bottomuOfA = Double.MAX_VALUE;

            tOfM = 0;
            uOfM = 0;
            bottomtOfM = Double.MAX_VALUE;
            topuOfM = Double.MIN_VALUE;
            toptOfM = Double.MIN_VALUE;
            bottomuOfM = Double.MAX_VALUE;
            
            for (int i = 0; i < speaii.getarchive().getPopsize(); i++) {
                double u = speaii.getarchive(i).getDecode().getUtilization();
                double t = speaii.getarchive(i).getDecode().getTotalDelay();
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
            tOfA /= speaii.getarchive().getPopsize();
            uOfA /= speaii.getarchive().getPopsize();
            DataStore.writefile(uOfA + "", p + "\\uOfA.txt");
            DataStore.writefile(tOfA + "", p + "\\tOfA.txt");
            DataStore.writefile(topuOfA + "", p + "\\topuOfA.txt");
            DataStore.writefile(bottomtOfA + "", p + "\\bottomtOfA.txt");
            DataStore.writefile(bottomuOfA + "", p + "\\bottomuOfA.txt");
            DataStore.writefile(toptOfA + "", p + "\\toptOfA.txt");
            
            for (int i = 0; i < speaii.getmergePop().getPopsize(); i++) {
                double u = speaii.getmergePop(i).getDecode().getUtilization();
                double t = speaii.getmergePop(i).getDecode().getTotalDelay();
                tOfM += t;
                uOfM += u;
                if (u > topuOfM) {
                    topuOfM = u;
                }
                if (t < bottomtOfM) {
                    bottomtOfM = t;
                }
                if (u < bottomuOfM) {
                    bottomuOfM = u;
                }
                if (t > toptOfM) {
                    toptOfM = t;
                }
            }
            tOfM /= speaii.getmergePop().getPopsize();
            uOfM /= speaii.getmergePop().getPopsize();
            DataStore.writefile(uOfM + "", p + "\\uOfM.txt");
            DataStore.writefile(tOfM + "", p + "\\tOfM.txt");
            DataStore.writefile(topuOfM + "", p + "\\topuOfM.txt");
            DataStore.writefile(bottomtOfM + "", p + "\\bottomtOfM.txt");
            DataStore.writefile(bottomuOfM + "", p + "\\bottomuOfM.txt");
            DataStore.writefile(toptOfM + "", p + "\\toptOfM.txt");
            besttotaldelay = bottomtOfA;
            bestutilization = topuOfA;

            // 判断是否终止迭代
            // System.out.println("iteration " + (iter+1) + "\t\tbest total delay: " + bottomtOfA + "\t\tbest utilization: " + topuOfA);
            if (! speaii.TerminateIteration(this.lastiteration)) {
            	if (this.lastiteration % 10 == 0) {
            		System.out.println("\033[32mSPEA2" + " \t\t\033[0miteration "
                    + this.lastiteration + "\t\tbest total delay: " + bottomtOfA + "\t\tbest utilization: " + topuOfA);
				}
                this.lastiteration++;
            } else {
                break;
            }
            // 选择交叉变异
            speaii.setPop(speaii.Variation(p.substring(p.length() - 1), speaii.getarchive(), qus));
            speaii.getpop().decode(qus.getMachineTime(), qus.getDemand(), qus.getSetupTime(), qus.getDueDays());
        }

        long endTime = System.currentTimeMillis();    //获取结束时间

        for (int i = 0; i < speaii.getarchive().getPopsize(); i++) {
            ArrayList<Object> objective =  new ArrayList<>();
            objective.add(speaii.getarchive(i).getDecode().getTotalDelay());
            objective.add(speaii.getarchive(i).getDecode().getUtilization());
            DataStore.writefile("{\"" + (i + 1) + "\":" + objective + "}", p + "\\archiveObj.txt");
            if (speaii.getarchive(i).getR() == 0) {
                // objectiveMap.put(i + 1, objective);
                DataStore.writefile("{\"" + (i + 1) + "\"" + ":" + objective + "}", p + "\\ParetoFront.txt");
                DataStore.writefile(DataStore.TimetabletoJOSN(speaii.getarchive(i)), p + "\\GantteData.txt");
            }
        }

        for (int i = 0; i < speaii.getpop().getPopsize(); i++) {
            ArrayList<Object> objective =  new ArrayList<>();
            objective.add(speaii.getpop(i).getDecode().getTotalDelay());
            objective.add(speaii.getpop(i).getDecode().getUtilization());
            // objectiveMap.put(i+1, objective);
            DataStore.writefile("{\"" + (i + 1) + "\"" + ":" + objective + "}", p + "\\popObj.txt");
        }

        // System.out.println(qus.getPartsNum() + " products, " + qus.getProcessNum() + " processes, " + que.getMachineTypesNum() + " machines");
        // System.out.print("共有" + objectiveMap.size() + "个pareto解，");
        runningtime = endTime - startTime;
        // long time = runningtime/1000;
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

        // if (!this.isAlive()) {
        //     System.out.println("\033[33mSPEA2" + "\t\t运行时间：" + this.runningtime/1000 + "s  \t迭代代数：" + this.lastiteration + "  \tbest total delay: " + this.besttotaldelay + "  \tbest utilization: " + this.bestutilization + "\033[0m");
        // }

        return "\033[33mSPEA2" + "\t\t\t运行时间：" + this.runningtime/1000 + "s  \t迭代代数：" + this.lastiteration + "  \tbest total delay: " + this.besttotaldelay + "  \tbest utilization: " + this.bestutilization + "\033[0m";
    }
}
