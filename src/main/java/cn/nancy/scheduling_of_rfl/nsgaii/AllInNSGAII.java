package cn.nancy.scheduling_of_rfl.nsgaii;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import cn.nancy.scheduling_of_rfl.*;

public class AllInNSGAII implements Callable<String> {
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
     * 记录最终 Pareto 解数量
     */
    public int solutionNUM;

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
        this.solutionNUM = 0;
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
    public String call() throws Exception{
    	System.out.println(
    			"\033[32m" + String.join(" -", Collections.nCopies(11, " -"))
    			+ " NSGAII_" + p.substring(p.length() - 1) + " start \tMaxIteration: " + this.MaxIteration
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
            PopofNSGAII childPop = (PopofNSGAII) nsgaii.Variation(p.substring(p.length() - 1), nsgaii.getPop(), qus);
            // Combine parent population and offspring population to form a combined population.
            nsgaii.setPop(nsgaii.getMergePop(childPop, nsgaii.getPop()));
            nsgaii.getPop().decode(qus.getMachineTime(), qus.getDemand(), qus.getSetupTime(), qus.getDueDays());
            // Perform nondominated sorting to obtain different fronts and compute crowding distances of the combined population.
            nsgaii.NondominatedRank();
            // Create new offspring population by selecting fronts with highest ranks.
            nsgaii.UpdatePop(popsize);
            // nsgaii.getPop().decode(qus.getMachineTime(), qus.getDemand(), qus.getSetupTime(), qus.getDueDays());

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
            	System.out.println("\033[32mNSGAII_" + p.substring(p.length() - 1) + " \t\033[0miteration " + this.lastiteration + "\t\tbest total delay: " + bottomtOfA + "\t\tbest utilization: " + topuOfA);
			}
            this.lastiteration++;
        }

        long endTime = System.currentTimeMillis();    //获取结束时间

        DataStore.createNewFile(p + "\\GantteData.txt");
        DataStore.createNewFile(p + "\\ParetoFront.txt");
        DataStore.createNewFile(p + "\\popObj.txt");
        for (int i = 0; i < popsize; i++) {
            ArrayList<Object> objective =  new ArrayList<>();
            objective.add(nsgaii.getPop().getIndividual(i).getDecode().getTotalDelay());
            objective.add(nsgaii.getPop().getIndividual(i).getDecode().getUtilization());
            DataStore.writefile("{\"" + (i + 1) + "\":" + objective + "}", p + "\\popObj.txt");
            if (nsgaii.getPop().getIndividual(i).getR() == 1) {
                this.solutionNUM++;
                DataStore.writefile("{\"" + (i + 1) + "\"" + ":" + objective + "}", p + "\\ParetoFront.txt");
                DataStore.writefile(DataStore.TimetabletoJOSN(nsgaii.getPop().getIndividual(i)), p + "\\GantteData.txt");
            }
            // archiveMap.put(i + 1, objective); 
        }
        
        runningtime = endTime - startTime;

        return "\033[33mNSGAII_" + p.substring(p.length() - 1) + "\t\t运行时间：" + this.runningtime/1000 + "s  \t迭代代数：" + this.lastiteration + "  \tbest total delay: " + this.besttotaldelay + "  \tbest utilization: " + this.bestutilization + "\033[0m";
    }

    public static void main(String[] args) throws Exception {
        String instance = "case";
        String path = "E:\\JavaProjects\\scheduling_of_rfl\\";
        String filename = path + instance + ".xlsx";
        String double_hr = String.join("", Collections.nCopies(40, "="));
        /**
         * 建立记录数据的文件
         */
        DataStore.createNewFile(path + "result\\" + instance + ".csv"); // 创建空文件
        // 编辑 csv 文件的列标题
        String ColumnHeading = "iteration,";
        ColumnHeading += "time,n,bestT,bestU,";
        DataStore.writecsv(ColumnHeading, path + "result\\" + instance + ".csv");
        for (int j = 0; j < 2; j++) {
            String Directorys = instance + "_time" + (j+1);
            Qus qus = new Qus(filename);
            System.out.println("\033[31m" + double_hr +"  " + Directorys + ": " + qus.getPartsNum() + " products, " + qus.getProcessNum() + " processes, " + qus.getMachineTypesNum() + " machines  " + double_hr + "\033[0m");
            String storefile = path + "result\\" + Directorys;
            AllInNSGAII nsgaii = new AllInNSGAII(qus, storefile + "\\NSGAII_2", 500, 70, 0.8, 0.3, 0.8);
            try {
                nsgaii.call();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            System.out.println("\033[34mNSGAII\t\t\t运行时间：" + nsgaii.runningtime/1000 + "s  \t迭代代数：" + nsgaii.lastiteration + "  \tbest total delay: " + nsgaii.besttotaldelay + "  \tbest utilization: " + nsgaii.bestutilization + "\033[0m");
            String data = (j+1)+",";
            data += nsgaii.runningtime + "," + nsgaii.solutionNUM + "," + nsgaii.besttotaldelay + "," + nsgaii.bestutilization + ",";
            DataStore.writecsv(data, path + "result\\" + instance + ".csv");
            System.out.println();
        }
    }
}
