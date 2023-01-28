package cn.nancy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AllInNSGAII {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();    //获取开始时间
        String filename = "E:\\JavaProjects\\configuration and scheduling of MRFL\\case3.xlsx";

        ArrayList<Double> tListOfA = new ArrayList<>();
        ArrayList<Double> uListOfA = new ArrayList<>();
        ArrayList<Double> bottomtListOfA = new ArrayList<>();
        ArrayList<Double> topuListOfA = new ArrayList<>();
        ArrayList<Double> toptListOfA = new ArrayList<>();
        ArrayList<Double> bottomutListOfA = new ArrayList<>();
        double tOfA = 0;
        double uOfA = 0;
        double bottomtOfA;
        double topuOfA;
        double toptOfA;
        double bottomuOfA;

        Qus qus = new Qus(filename);
        int popsize = 80;
        NSGAII nsgaii = new NSGAII(500, popsize, 0.8, 0.2, 0.8, qus);
        int iter = 1;
        while (! nsgaii.TerminateIteration(iter)) {
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
            tListOfA.add(tOfA);
            uListOfA.add(uOfA);
            bottomtListOfA.add(bottomtOfA);
            topuListOfA.add(topuOfA);
            toptListOfA.add(toptOfA);
            bottomutListOfA.add(bottomuOfA);
            System.out.println(iter + ": " + uOfA);

            iter++;
        }

        long endTime = System.currentTimeMillis();    //获取结束时间

        DataStore.createNewFile("E:\\JavaProjects\\configuration and scheduling of MRFL\\nsgaii\\sourse\\uOfA.txt");
        DataStore.createNewFile("E:\\JavaProjects\\configuration and scheduling of MRFL\\nsgaii\\sourse\\tOfA.txt");
        DataStore.createNewFile("E:\\JavaProjects\\configuration and scheduling of MRFL\\nsgaii\\sourse\\topuOfA.txt");
        DataStore.createNewFile("E:\\JavaProjects\\configuration and scheduling of MRFL\\nsgaii\\sourse\\bottomtOfA.txt");
        DataStore.createNewFile("E:\\JavaProjects\\configuration and scheduling of MRFL\\nsgaii\\sourse\\bottomuOfA.txt");
        DataStore.createNewFile("E:\\JavaProjects\\configuration and scheduling of MRFL\\nsgaii\\sourse\\toptOfA.txt");
        DataStore.writefile(DataStore.TimetabletoJOSN(uListOfA), "E:\\JavaProjects\\configuration and scheduling of MRFL\\nsgaii\\sourse\\uOfA.txt");
        DataStore.writefile(DataStore.TimetabletoJOSN(tListOfA), "E:\\JavaProjects\\configuration and scheduling of MRFL\\nsgaii\\sourse\\tOfA.txt");
        DataStore.writefile(DataStore.TimetabletoJOSN(topuListOfA), "E:\\JavaProjects\\configuration and scheduling of MRFL\\nsgaii\\sourse\\topuOfA.txt");
        DataStore.writefile(DataStore.TimetabletoJOSN(bottomtListOfA), "E:\\JavaProjects\\configuration and scheduling of MRFL\\nsgaii\\sourse\\bottomtOfA.txt");
        DataStore.writefile(DataStore.TimetabletoJOSN(bottomutListOfA), "E:\\JavaProjects\\configuration and scheduling of MRFL\\nsgaii\\sourse\\bottomuOfA.txt");
        DataStore.writefile(DataStore.TimetabletoJOSN(toptListOfA), "E:\\JavaProjects\\configuration and scheduling of MRFL\\nsgaii\\sourse\\toptOfA.txt");

        DataStore.createNewFile("E:\\JavaProjects\\configuration and scheduling of MRFL\\nsgaii\\sourse\\GantteData.txt");
        DataStore.createNewFile("E:\\JavaProjects\\configuration and scheduling of MRFL\\nsgaii\\sourse\\ParetoFront.txt");
        DataStore.createNewFile("E:\\JavaProjects\\configuration and scheduling of MRFL\\nsgaii\\sourse\\archiveObj.txt");
        Map<Integer, ArrayList<Object>> objectivMap = new HashMap<>();  // 记录 ParetoFront.txt
        Map<Integer, ArrayList<Object>> archiveMap = new HashMap<>();   // 记录 archiveObj.txt
        for (int i = 0; i < popsize; i++) {
            ArrayList<Object> objective =  new ArrayList<>();
            objective.add(nsgaii.getPop().getIndividual(i).getDecode().getTotalDelay());
            objective.add(nsgaii.getPop().getIndividual(i).getDecode().getUtilization());
            if (nsgaii.getPop().getIndividual(i).getR() == 1) {
                objectivMap.put(i + 1, objective);
                DataStore.writefile(DataStore.TimetabletoJOSN(nsgaii.getPop().getIndividual(i)), "E:\\JavaProjects\\configuration and scheduling of MRFL\\nsgaii\\sourse\\GantteData.txt");
            }
            archiveMap.put(i + 1, objective);
        }
        DataStore.writefile(DataStore.TimetabletoJOSN(archiveMap), "E:\\JavaProjects\\configuration and scheduling of MRFL\\nsgaii\\sourse\\archiveObj.txt");
        DataStore.writefile(DataStore.TimetabletoJOSN(objectivMap), "E:\\JavaProjects\\configuration and scheduling of MRFL\\nsgaii\\sourse\\ParetoFront.txt");
        System.out.println("共有" + objectivMap.size() + "个pareto解");

        System.out.println("程序运行时间：" + (endTime - startTime) + "ms");    //输出程序运行时间
    }
}
