package cn.nancy.scheduling_of_rfl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import cn.nancy.scheduling_of_rfl.nsgaii.*;
import cn.nancy.scheduling_of_rfl.spea2.*;

public class Experiment {
    /**
     * the number of cases for experiment.
     */
    private int caseNum;

    /**
     * The number of times for running each case.
     */
    private int runTime;

    /**
     * an array of case names/paths
     */
    private String[] CaseName;

    public Experiment(String[] casename, int exptime) {
        this.caseNum = casename.length;
        this.CaseName = casename;
        this.runTime = exptime;
    }

    /**
     * 实验框架
     * @param casename 实验数据文件名称，case1、case2……
     * @param exptime 每个case的实验次数
     * @param MaxIteration 数组，每个case的最大迭代代数
     * @param popsize
     * @param archivesize
     * @param TimeOfThread 数组，每个case的最大运行时长
     */
    public static void run_experiment(String[] casename, int exptime, int[] MaxIteration, int popsize, int archivesize, int[] TimeOfThread) {
        Experiment exp = new Experiment(casename, exptime);
        String path = "E:\\JavaProjects\\zhengnanxi\\scheduling_of_rfl\\";
        String double_hr = String.join("", Collections.nCopies(40, "="));
        String hr = String.join("-", Collections.nCopies(21, " -"));
        String[] hrlist = {"\033[34m" + hr + "-  NSGAII " + hr + "\033[0m", "\033[34m" + hr + "- SPEA2_1 " + hr + "\033[0m", "\033[34m" + hr + "- SPEA2_2 " + hr + "\033[0m", "\033[34m" + hr + "- SPEA2_3 " + hr + "\033[0m"};
        for (String instance : casename) {
        	int caseindex;
        	switch (instance.substring(instance.length() - 1)) {
	        	case "1":
	        		caseindex = 1;
	        		break;
	        	case "2":
	        	case "3":
	        		caseindex = 2;
	        		break;
	        	case "4":
	        	case "5":
	        		caseindex = 3;
	        		break;
	        	default:
	                throw new IllegalArgumentException("The CASE num. must be 1, 3 or 4.");
        	}
            DataStore.createNewFile(path + "result\\" + instance + ".csv");
            DataStore.writecsv("iteration,time_nsgaii,time_spea2_1,time_spea2_2,time_spea2_3,bestT_nsgaii,bestT_spea2_1,bestT_spea2_2,bestT_spea2_3,bestT_nsgaii,bestT_spea2_1,bestT_spea2_2,bestT_spea2_3,", path + "result\\" + instance + ".csv");
            for (int i = 0; i < exptime; i++) {
                String Directorys = instance + "_time" + (i+1);
                String filename = path + instance + ".xlsx";
                String storefile = path + "result\\" + Directorys;
                Qus qus = new Qus(filename);
                System.out.println("\033[31m" + double_hr +"  " + Directorys + ": " + qus.getPartsNum() + " products, " + qus.getProcessNum() + " processes, " + qus.getMachineTypesNum() + " machines  " + double_hr + "\033[0m");
                // NSGAII                
                AllInNSGAII nsgaii = new AllInNSGAII(qus, storefile + "\\NSGAII", MaxIteration[caseindex-1], popsize, 0.8, 0.3, 0.8);
                // SPEA2_1                
                AllInSPEA2 spea2_1 = new AllInSPEA2(qus, storefile + "\\SPEA2_1", popsize, archivesize, MaxIteration[caseindex-1], 4);
                // SPEA2_2                
                AllInSPEA2 spea2_2 = new AllInSPEA2(qus, storefile + "\\SPEA2_2", popsize, archivesize, MaxIteration[caseindex-1], 4);
                // SPEA2_3                
                AllInSPEA2 spea2_3 = new AllInSPEA2(qus, storefile + "\\SPEA2_3", popsize, archivesize, MaxIteration[caseindex-1], 4);
                //random
                // System.out.println("\033[34m" + hr + "-  random " + hr + "\033[0m");

                /**
                 * 利用 ExecutorService 中断线程
                 */
                ExecutorService executor = Executors.newFixedThreadPool(2);
                List<Thread> AlgorithemList = new ArrayList<> ();
                AlgorithemList.add(spea2_1);
                AlgorithemList.add(spea2_2);
                AlgorithemList.add(spea2_3);
                AlgorithemList.add(nsgaii);
                for (int j = 0; j < AlgorithemList.size(); j++) {
                	executor = Executors.newFixedThreadPool(1);
                	Future<?> future1 = executor.submit(AlgorithemList.get(j));
                	// Future<?> future2 = executor.submit(AlgorithemList.get(j+1));
                	List<Future<?>> futures = new ArrayList<> ();
                	futures.add(future1);
                	// futures.add(future2);
//                	System.out.println(hrlist[j]);
                	executor.shutdown();
                    try {
                        if (executor.awaitTermination(TimeOfThread[caseindex-1], TimeUnit.SECONDS)) {
                           System.out.println("All tasks finished.");
                        } else {
                            System.out.println("time out. TERMINATE!");
                            for (Future<?> future : futures) {
                            	if (!future.isDone()) {
    								future.cancel(true);
    							}
							}
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                    } finally {
                        executor.shutdown();
                    }
				}
//                ExecutorService executor = Executors.newCachedThreadPool();
//                Future<?> future1 = executor.submit(nsgaii);
//                Future<?> future2 = executor.submit(spea2_1);
//                Future<?> future3 = executor.submit(spea2_2);
//                Future<?> future4 = executor.submit(spea2_3);
//                List<Future<?>> futures = new ArrayList<>();
//                futures.add(future1);
//                futures.add(future2);
//                futures.add(future3);
//                futures.add(future4);
//                executor.shutdown();
//                try {
//                    if (executor.awaitTermination(TimeOfThread[caseindex-1], TimeUnit.SECONDS)) {
//                        System.out.println("All tasks finished.");
//                    } else {
//                        System.out.println("time out. TERMINATE!");
//                        for (Future<?> f : futures) {
//                            if (!f.isDone()) {
//                                f.cancel(true);
//                            }
//                        }
//                    }
//                } catch (Exception e) {
//                    // TODO: handle exception
//                } finally {
//                    executor.shutdown();
//                }

             

//                 System.out.println("\033[34m" + hr + "-  NSGAII " + hr + "\033[0m");
//                 System.out.println("\033[34m" + hr + "- SPEA2_1 " + hr + "\033[0m");
//                 System.out.println("\033[34m" + hr + "- SPEA2_2 " + hr + "\033[0m");
//                 System.out.println("\033[34m" + hr + "- SPEA2_3 " + hr + "\033[0m");
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    // TODO: handle exception
                }
                System.out.println("\033[34mNSGAII\t\t运行时间：" + nsgaii.runningtime/1000 + "s  \t迭代代数：" + nsgaii.lastiteration + "  \tbest total delay: " + nsgaii.besttotaldelay + "  \tbest utilization: " + nsgaii.bestutilization + "\033[0m");
                System.out.println("\033[34mSPEA2_1\t\t运行时间：" + spea2_1.runningtime/1000 + "s  \t迭代代数：" + spea2_1.lastiteration + "  \tbest total delay: " + spea2_1.besttotaldelay + "  \tbest utilization: " + spea2_1.bestutilization + "\033[0m");
                System.out.println("\033[34mSPEA2_2\t\t运行时间：" + spea2_2.runningtime/1000 + "s  \t迭代代数：" + spea2_2.lastiteration + "  \tbest total delay: " + spea2_2.besttotaldelay + "  \tbest utilization: " + spea2_2.bestutilization + "\033[0m");
                System.out.println("\033[34mSPEA2_3\t\t运行时间：" + spea2_3.runningtime/1000 + "s  \t迭代代数：" + spea2_3.lastiteration + "  \tbest total delay: " + spea2_3.besttotaldelay + "  \tbest utilization: " + spea2_3.bestutilization + "\033[0m");
                String data = i+","+nsgaii.runningtime+","+spea2_1.runningtime+","+spea2_2.runningtime+","+spea2_3.runningtime+","+nsgaii.besttotaldelay+","+spea2_1.besttotaldelay+","+spea2_2.besttotaldelay+","+spea2_3.besttotaldelay+","+nsgaii.bestutilization+","+spea2_1.bestutilization+","+spea2_2.bestutilization+","+spea2_3.bestutilization;
                DataStore.writecsv(data, path + "result\\" + instance + ".csv");
                System.out.println();
                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        }
    }
    
    public static void main(String[] args) {
        String[] casename = new String[] {"case1", "case3", "case4"};
        int exptime = 10;
        int[] MaxIteration = {200, 400, 500}; // {200, 400, 500};
        int popsize = 80;
        int archivesize = 60;
        int[] TimeOfThread = {3000, 18000, 240000};  // 每个实验运行时长，单位：s
        run_experiment(casename, exptime, MaxIteration, popsize, archivesize, TimeOfThread);
    }

	public int getCaseNum() {
		return caseNum;
	}

	public int getRunTime() {
		return runTime;
	}

	public String[] getCaseName() {
		return CaseName;
	}
}
