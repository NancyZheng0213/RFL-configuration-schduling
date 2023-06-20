package cn.nancy.scheduling_of_rfl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
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
    public static void run_experiment(String[] AlgorithemNameList, String[] casename, int exptime, int[] MaxIteration, int popsize, int archivesize, int[] TimeOfThread) {
        Experiment exp = new Experiment(casename, exptime);
        String path = "E:\\JavaProjects\\scheduling_of_rfl\\";
        String double_hr = String.join("", Collections.nCopies(40, "="));
        // String hr = String.join("-", Collections.nCopies(21, " -"));
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
            /**
             * 建立记录数据的文件
             */
            DataStore.createNewFile(path + "result\\" + instance + ".csv"); // 创建空文件
            // 编辑 csv 文件的列标题
            String ColumnHeading = "iteration,";
            for (String algorithem : AlgorithemNameList) {
                ColumnHeading += "time_" + algorithem + ",";
            }
            for (String algorithem : AlgorithemNameList) {
                ColumnHeading += "bestT_" + algorithem + ",";
            }
            for (String algorithem : AlgorithemNameList) {
                ColumnHeading += "bestU_" + algorithem + ",";
            }
            DataStore.writecsv(ColumnHeading, path + "result\\" + instance + ".csv");
            /**
             * 实验开始
             */
            for (int i = 0; i < exptime; i++) {
                String Directorys = instance + "_time" + (i+1);
                String filename = path + instance + ".xlsx";
                String storefile = path + "result\\" + Directorys;
                Qus qus = new Qus(filename);
                System.out.println("\033[31m" + double_hr +"  " + Directorys + ": " + qus.getPartsNum() + " products, " + qus.getProcessNum() + " processes, " + qus.getMachineTypesNum() + " machines  " + double_hr + "\033[0m");
                // NSGAII_1             
                AllInNSGAII nsgaii_1 = new AllInNSGAII(qus, storefile + "\\NSGAII_1", MaxIteration[caseindex-1], popsize, 0.8, 0.3, 0.8);
                // NSGAII_2             
                AllInNSGAII nsgaii_2 = new AllInNSGAII(qus, storefile + "\\NSGAII_2", MaxIteration[caseindex-1], popsize, 0.8, 0.3, 0.8);
                // NSGAII_3             
                AllInNSGAII nsgaii_3 = new AllInNSGAII(qus, storefile + "\\NSGAII_3", MaxIteration[caseindex-1], popsize, 0.8, 0.3, 0.8);
                // NSGAII_4             
                AllInNSGAII nsgaii_4 = new AllInNSGAII(qus, storefile + "\\NSGAII_4", MaxIteration[caseindex-1], popsize, 0.8, 0.3, 0.8);
                // SPEA2
                AllInSPEA2 spea2 = new AllInSPEA2(qus, storefile + "\\SPEA2_1", popsize, archivesize, MaxIteration[caseindex-1], 4);
                //random
                // System.out.println("\033[34m" + hr + "-  random " + hr + "\033[0m");

                /**
                 * 利用 ExecutorService 中断线程
                 */
                ExecutorService executor = Executors.newFixedThreadPool(2);
                List<Callable<String>> AlgorithemList = new ArrayList<> ();
                AlgorithemList.add(spea2);
                // AlgorithemList.add(nsgaii_1);
                AlgorithemList.add(nsgaii_2);
                AlgorithemList.add(nsgaii_3);
                AlgorithemList.add(nsgaii_4);
                for (int j = 0; j < AlgorithemList.size(); j++) {
                	executor = Executors.newFixedThreadPool(2);
                	Future<?> future1 = executor.submit(AlgorithemList.get(j));
                	// Future<?> future2 = executor.submit(AlgorithemList.get(j+1));
                	List<Future<?>> futures = new ArrayList<> ();
                	futures.add(future1);
                	// futures.add(future2);
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
                        for (Future<?> future : futures) {
                            System.out.println(future.get());
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } finally {
                        executor.shutdown();
                    }
				}
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    // TODO: handle exception
                }
                System.out.println("\033[34mSPEA2\t\t\t运行时间：" + spea2.runningtime/1000 + "s  \t迭代代数：" + spea2.lastiteration + "  \tbest total delay: " + spea2.besttotaldelay + "  \tbest utilization: " + spea2.bestutilization + "\033[0m");
                // System.out.println("\033[34mNSGAII_1\t\t运行时间：" + nsgaii_1.runningtime/1000 + "s  \t迭代代数：" + nsgaii_1.lastiteration + "  \tbest total delay: " + nsgaii_1.besttotaldelay + "  \tbest utilization: " + nsgaii_1.bestutilization + "\033[0m");
                System.out.println("\033[34mNSGAII_2\t\t运行时间：" + nsgaii_2.runningtime/1000 + "s  \t迭代代数：" + nsgaii_2.lastiteration + "  \tbest total delay: " + nsgaii_2.besttotaldelay + "  \tbest utilization: " + nsgaii_2.bestutilization + "\033[0m");
                System.out.println("\033[34mNSGAII_3\t\t运行时间：" + nsgaii_3.runningtime/1000 + "s  \t迭代代数：" + nsgaii_3.lastiteration + "  \tbest total delay: " + nsgaii_3.besttotaldelay + "  \tbest utilization: " + nsgaii_3.bestutilization + "\033[0m");
                System.out.println("\033[34mNSGAII_4\t\t运行时间：" + nsgaii_4.runningtime/1000 + "s  \t迭代代数：" + nsgaii_4.lastiteration + "  \tbest total delay: " + nsgaii_4.besttotaldelay + "  \tbest utilization: " + nsgaii_4.bestutilization + "\033[0m");
                String data = i+","+spea2.runningtime+","+nsgaii_1.runningtime+","+nsgaii_2.runningtime+","+nsgaii_3.runningtime+","+nsgaii_4.runningtime+","+spea2.besttotaldelay+","+nsgaii_1.besttotaldelay+","+nsgaii_2.besttotaldelay+","+nsgaii_3.besttotaldelay+","+nsgaii_4.besttotaldelay+","+spea2.bestutilization+","+nsgaii_1.bestutilization+","+nsgaii_2.bestutilization+","+nsgaii_3.bestutilization+","+nsgaii_4.bestutilization;
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
        // 对比的算法名称列表，将被对比对象放在第一个
        String[] AlgorithemNameList = new String[] {"SPEA2", "NSGAII_1", "NSGAII_2", "NSGAII_3", "NSGAII_4"};
        String[] casename = new String[] {"case4"};
        int exptime = 10;
        int[] MaxIteration = {200, 400, 500}; // {200, 400, 500};
        int popsize = 80;
        int archivesize = 60;
        int[] TimeOfThread = {3000, 18000, 240000};  // 每个实验运行时长，单位：s
        run_experiment(AlgorithemNameList, casename, exptime, MaxIteration, popsize, archivesize, TimeOfThread);
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
