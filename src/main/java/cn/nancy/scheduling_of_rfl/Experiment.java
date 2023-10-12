package cn.nancy.scheduling_of_rfl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import cn.nancy.scheduling_of_rfl.MOEA.AllInMOEA;
import cn.nancy.scheduling_of_rfl.MOEA.MOEA;
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
    public static void run_experiment(String[] AlgorithmNameList, String[] casename, int exptime, int[] MaxIteration, int[] popsize, int[] archivesize, int[] TimeOfThread) {
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
	        		caseindex = 2;
	        		break;
                case "3":
	        	case "4":
	        	case "5":
	        		caseindex = 3;
	        		break;
	        	default:
	                throw new IllegalArgumentException("The CASE num. must be from 1 to 5.");
        	}
            /**
             * 建立记录数据的文件
             */
            DataStore.createNewFile(path + "result\\" + instance + ".csv"); // 创建空文件
            // 编辑 csv 文件的列标题
            String ColumnHeading = "iteration,";
            for (String algorithem : AlgorithmNameList) {
                ColumnHeading += "time_" + algorithem + ",";
            }
            for (String algorithem : AlgorithmNameList) {
                ColumnHeading += "bestT_" + algorithem + ",";
            }
            for (String algorithem : AlgorithmNameList) {
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
                // NSGAII         
                AllInNSGAII nsgaii = new AllInNSGAII(qus, storefile + "\\NSGAII_2", MaxIteration[caseindex-1], popsize[caseindex-1], 0.8, 0.3, 0.8);
                // SPEA2
                AllInSPEA2 spea2 = new AllInSPEA2(qus, storefile + "\\SPEA2_2", popsize[caseindex-1], archivesize[caseindex-1], MaxIteration[caseindex-1], 2);
                // MOEA
                AllInMOEA moea = new AllInMOEA(qus, storefile + "\\MOEA", MaxIteration[caseindex-1], popsize[caseindex-1]-1, archivesize[caseindex-1]);
                //random
                // System.out.println("\033[34m" + hr + "-  random " + hr + "\033[0m");

                /**
                 * 利用 ExecutorService 中断线程
                 */
                ExecutorService executor = Executors.newFixedThreadPool(2);
                List<Callable<String>> AlgorithmList = new ArrayList<> ();
                for (String algorithm : AlgorithmNameList) {
                    switch (algorithm.substring(0, 4)) {
                        case "SPEA":
                            AlgorithmList.add(spea2);
                            break;
                        case "NSGA":
                            AlgorithmList.add(nsgaii);
                            break;
                        case "MOEA":
                            AlgorithmList.add(moea);
                            break;
                        default:
                            throw new IllegalArgumentException("The NAME of algorithm must be \"SPEA2\", \"NSGAII\" or \"MOEA/D\".");
                    }
                }
                for (int j = 0; j < AlgorithmNameList.length; j++) {
                	executor = Executors.newFixedThreadPool(2);
                	Future<?> future1 = executor.submit(AlgorithmList.get(j));
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
                System.out.println("\033[34mNSGAII\t\t\t运行时间：" + nsgaii.runningtime/1000 + "s  \t迭代代数：" + nsgaii.lastiteration + "  \tbest total delay: " + nsgaii.besttotaldelay + "  \tbest utilization: " + nsgaii.bestutilization + "\033[0m");
                System.out.println("\033[34mMOEA/D\t\t\t运行时间：" + moea.runningtime/1000 + "s  \t迭代代数：" + moea.lastiteration + "  \tbest total delay: " + moea.besttotaldelay + "  \tbest utilization: " + moea.bestutilization + "\033[0m");
                // 结果储存
                String data = (i+1)+",";
                for (String algorithm : AlgorithmNameList) {
                    switch (algorithm.substring(0, 4)) {
                        case "SPEA":
                            data += spea2.runningtime+",";
                            break;
                        case "NSGA":
                            data += nsgaii.runningtime+",";
                            break;
                        case "MOEA":
                            data += moea.runningtime+",";
                            break;
                        default:
                            throw new IllegalArgumentException("The NAME of algorithm must be \"SPEA2\", \"NSGAII\" or \"MOEA/D\".");
                    }
                }
                for (String algorithm : AlgorithmNameList) {
                    switch (algorithm.substring(0, 4)) {
                        case "SPEA":
                            data += spea2.besttotaldelay+",";
                            break;
                        case "NSGA":
                            data += nsgaii.besttotaldelay+",";
                            break;
                        case "MOEA":
                            data += moea.besttotaldelay+",";
                            break;
                        default:
                            throw new IllegalArgumentException("The NAME of algorithm must be \"SPEA2\", \"NSGAII\" or \"MOEA/D\".");
                    }
                }
                for (String algorithm : AlgorithmNameList) {
                    switch (algorithm.substring(0, 4)) {
                        case "SPEA":
                            data += spea2.bestutilization+",";
                            break;
                        case "NSGA":
                            data += nsgaii.bestutilization+",";
                            break;
                        case "MOEA":
                            data += moea.bestutilization+",";
                            break;
                        default:
                            throw new IllegalArgumentException("The NAME of algorithm must be \"SPEA2\", \"NSGAII\" or \"MOEA/D\".");
                    }
                }
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
        String[] AlgorithmNameList = new String[] {"SPEA2", "NSGAII", "MOEA/D", };
        String[] casename = new String[] {"case3"};
        int exptime = 2;
        int[] MaxIteration = {200, 400, 500};       // {200, 400, 500};
        int[] popsize = {60, 70, 80};               // {}
        int[] archivesize = {30, 40, 60};
        int[] TimeOfThread = {3000, 18000, Integer.MAX_VALUE}; // 每个实验运行时长，单位：s
        run_experiment(AlgorithmNameList, casename, exptime, MaxIteration, popsize, archivesize, TimeOfThread);
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
