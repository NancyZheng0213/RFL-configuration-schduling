package cn.nancy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.nancy.Arithmetic.SPEA2;

public class AllInOne {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();    //获取开始时间
        String filename = "E:\\JavaProjects\\configuration and scheduling of MRFL\\case3.xlsx";

        Qus que = new Qus(filename);
        SPEA2 speaii = new SPEA2(80, 60, 500, 4, que);
        int iter = 1;
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
        ArrayList<Double> tListOfM = new ArrayList<>();
        ArrayList<Double> uListOfM = new ArrayList<>();
        ArrayList<Double> bottomtListOfM = new ArrayList<>();
        ArrayList<Double> topuListOfM = new ArrayList<>();
        ArrayList<Double> toptListOfM = new ArrayList<>();
        ArrayList<Double> bottomutListOfM = new ArrayList<>();
        double tOfM = 0;
        double uOfM = 0;
        double bottomtOfM;
        double topuOfM;
        double toptOfM;
        double bottomuOfM;
        while (true) {
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
            
            for (int i = 0; i < speaii.getarchive().getpopNum(); i++) {
                double u = speaii.getarchive(i).getdecode().getUtilization();
                double t = speaii.getarchive(i).getdecode().getTotalDelay();
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
            tOfA /= speaii.getarchive().getpopNum();
            uOfA /= speaii.getarchive().getpopNum();
            tListOfA.add(tOfA);
            uListOfA.add(uOfA);
            bottomtListOfA.add(bottomtOfA);
            topuListOfA.add(topuOfA);
            toptListOfA.add(toptOfA);
            bottomutListOfA.add(bottomuOfA);
            
            for (int i = 0; i < speaii.getmergePop().getpopNum(); i++) {
                double u = speaii.getmergePop(i).getdecode().getUtilization();
                double t = speaii.getmergePop(i).getdecode().getTotalDelay();
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
            tOfM /= speaii.getmergePop().getpopNum();
            uOfM /= speaii.getmergePop().getpopNum();
            tListOfM.add(tOfM);
            uListOfM.add(uOfM);
            bottomtListOfM.add(bottomtOfM);
            topuListOfM.add(topuOfM);
            toptListOfM.add(toptOfM);
            bottomutListOfM.add(bottomuOfM);
            System.out.println(iter + ": " + uOfM);

            // 判断是否终止迭代
            if (! speaii.TerminateIteration(iter)) {
                iter++;
            } else {
                break;
            }
            // 选择交叉变异
            speaii.Variation3(speaii.getmergePop(), que);  // 需要注意，此处的pop均为浅拷贝
            speaii.getpop().decode(que.getMachineTime(), que.getDemand(), que.getSetupTime(), que.getDueDays());
        }

        long endTime = System.currentTimeMillis();    //获取结束时间

        DataStore.createNewFile("E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\uOfA.txt");
        DataStore.createNewFile("E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\tOfA.txt");
        DataStore.createNewFile("E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\topuOfA.txt");
        DataStore.createNewFile("E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\bottomtOfA.txt");
        DataStore.createNewFile("E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\bottomuOfA.txt");
        DataStore.createNewFile("E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\toptOfA.txt");
        DataStore.writefile(DataStore.TimetabletoJOSN(uListOfA), "E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\uOfA.txt");
        DataStore.writefile(DataStore.TimetabletoJOSN(tListOfA), "E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\tOfA.txt");
        DataStore.writefile(DataStore.TimetabletoJOSN(topuListOfA), "E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\topuOfA.txt");
        DataStore.writefile(DataStore.TimetabletoJOSN(bottomtListOfA), "E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\bottomtOfA.txt");
        DataStore.writefile(DataStore.TimetabletoJOSN(bottomutListOfA), "E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\bottomuOfA.txt");
        DataStore.writefile(DataStore.TimetabletoJOSN(toptListOfA), "E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\toptOfA.txt");

        DataStore.createNewFile("E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\uOfM.txt");
        DataStore.createNewFile("E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\tOfM.txt");
        DataStore.createNewFile("E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\topuOfM.txt");
        DataStore.createNewFile("E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\bottomtOfM.txt");
        DataStore.createNewFile("E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\bottomuOfM.txt");
        DataStore.createNewFile("E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\toptOfM.txt");
        DataStore.writefile(DataStore.TimetabletoJOSN(uListOfM), "E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\uOfM.txt");
        DataStore.writefile(DataStore.TimetabletoJOSN(tListOfM), "E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\tOfM.txt");
        DataStore.writefile(DataStore.TimetabletoJOSN(topuListOfM), "E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\topuOfM.txt");
        DataStore.writefile(DataStore.TimetabletoJOSN(bottomtListOfM), "E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\bottomtOfM.txt");
        DataStore.writefile(DataStore.TimetabletoJOSN(bottomutListOfM), "E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\bottomuOfM.txt");
        DataStore.writefile(DataStore.TimetabletoJOSN(toptListOfM), "E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\toptOfM.txt");

        DataStore.createNewFile("E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\GantteData.txt");
        DataStore.createNewFile("E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\ParetoFront.txt");
        DataStore.createNewFile("E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\archiveObj.txt");
        Map<Integer, ArrayList<Object>> objectivMap = new HashMap<>();  // 记录 ParetoFront.txt
        Map<Integer, ArrayList<Object>> archiveMap = new HashMap<>();   // 记录 archiveObj.txt
        for (int i = 0; i < speaii.getarchive().getpopNum(); i++) {
            ArrayList<Object> objective =  new ArrayList<>();
            objective.add(speaii.getarchive(i).getdecode().getTotalDelay());
            objective.add(speaii.getarchive(i).getdecode().getUtilization());
            if (speaii.getarchive(i).getR() == 0) {
                objectivMap.put(i + 1, objective);
                DataStore.writefile(DataStore.TimetabletoJOSN(speaii.getarchive(i)), "E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\GantteData.txt");
            }
            archiveMap.put(i + 1, objective);
        }
        DataStore.writefile(DataStore.TimetabletoJOSN(archiveMap), "E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\archiveObj.txt");
        DataStore.writefile(DataStore.TimetabletoJOSN(objectivMap), "E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\ParetoFront.txt");
        System.out.println("共有" + objectivMap.size() + "个pareto解");

        DataStore.createNewFile("E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\popObj.txt");
        objectivMap = new HashMap<>();
        for (int i = 0; i < speaii.getpop().getpopNum(); i++) {
            ArrayList<Object> objective =  new ArrayList<>();
            objective.add(speaii.getpop(i).getdecode().getTotalDelay());
            objective.add(speaii.getpop(i).getdecode().getUtilization());
            objectivMap.put(i+1, objective);
        }
        DataStore.writefile(DataStore.TimetabletoJOSN(objectivMap), "E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\popObj.txt");

        System.out.println("程序运行时间：" + (endTime - startTime) + "ms");    //输出程序运行时间
    }
}