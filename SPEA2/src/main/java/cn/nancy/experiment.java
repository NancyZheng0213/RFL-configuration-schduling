package cn.nancy;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import cn.nancy.Arithmetic.Individual;
import cn.nancy.Arithmetic.SPEA2;

public class experiment {
    public static void main(String[] args) {
        
        // 试验记录
        ArrayList<ArrayList<Double>> result = new ArrayList<>();
        // 实验
        result = startexperiment();
        writeEXCEL1(result);

        // 实验分析
        ArrayList<ArrayList<Double>> analysis = new ArrayList<>();
        analysis = analysis();
        writeEXCEL2(analysis);

    }

    /**
     * 实验
     * @return
     */
    public static ArrayList<ArrayList<Double>> startexperiment() {
        String datafile = "E:\\JavaProjects\\graduation\\sourse\\case4.xlsx";
        String experimentdata = "E:\\JavaProjects\\graduation\\sourse\\experiment.xlsx";

        // 读取实验数据
        ArrayList<Integer> popNumList = new ArrayList<>();
        ArrayList<Integer> bestpopNumList = new ArrayList<>();
        ArrayList<Integer> maxIterationList = new ArrayList<>();
        ArrayList<Integer> tournamentSizeList = new ArrayList<>();
        int experimenttimes = -1;
        try {
            //创建工作簿对象
            XSSFWorkbook xssfWorkbook = new XSSFWorkbook(new FileInputStream(experimentdata));
            XSSFSheet sheet = xssfWorkbook.getSheetAt(2);
            experimenttimes = sheet.getPhysicalNumberOfRows() - 1;
            for (int i = 0; i <experimenttimes; i++) {
                popNumList.add((int)sheet.getRow(i + 1).getCell(1).getNumericCellValue());
                bestpopNumList.add((int)sheet.getRow(i + 1).getCell(2).getNumericCellValue());
                maxIterationList.add((int)sheet.getRow(i + 1).getCell(3).getNumericCellValue());
                tournamentSizeList.add((int)sheet.getRow(i + 1).getCell(4).getNumericCellValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 试验记录
        ArrayList<ArrayList<Double>> result = new ArrayList<>();
        ArrayList<Double> totaldelay = new ArrayList<>();
        ArrayList<Double> utilization = new ArrayList<>();
        ArrayList<Double> AVGtotaldelay = new ArrayList<>();
        ArrayList<Double> AVGutilization = new ArrayList<>();
        // 实验
        Qus que = new Qus(datafile);
        for (int count = 0; count < experimenttimes; count++) {
            System.out.printf("实验%d: ", count+1);
            SPEA2 speaii = new SPEA2(popNumList.get(count), bestpopNumList.get(count), maxIterationList.get(count), tournamentSizeList.get(count), que);
            int iter = 1;

            // 记录每次实验的平均解之和
            double avgtotaldelay = 0;
            double avgutilization = 0;
            // 记录每次实验的最优解之和
            double besttotaldelay = Double.MAX_VALUE;
            double bestutilization = Double.MIN_VALUE;

            // 本次运行的均解
            avgtotaldelay = 0;
            avgutilization = 0;
            // 本次运行的最优解
            besttotaldelay = Double.MAX_VALUE;
            bestutilization = Double.MIN_VALUE;

            while (true) {
                avgtotaldelay = 0;
                avgutilization = 0;
                // 计算适应度
                speaii.MergePop();
                speaii.CalculateFitness(speaii.getmergePop());
                // 更新外部存档
                speaii.Selection();
                // 判断是否终止迭代
                if (! speaii.TerminateIteration(iter)) {
                    iter++;
                } else {
                    int ParetoNum = 0;
                    for (int j = 0; j < speaii.getarchive().getpopNum(); j++) {
                        if (speaii.getarchive(j).getFitness() < 1) {
                            ParetoNum++;
                            Individual individual = speaii.getarchive(j);
                            double u = individual.getdecode().getUtilization();
                            double t = individual.getdecode().getTotalDelay();
                            avgtotaldelay += t;
                            avgutilization += u;
                            // 记录最佳延期时间
                            if (t < besttotaldelay) {
                                besttotaldelay = t;
                            }
                            // 记录最佳利用率
                            if (u > bestutilization) {
                                bestutilization = u;
                            }
                        }
                    }
                    System.out.printf("%d  ", ParetoNum);
                    avgtotaldelay /= ParetoNum;
                    avgutilization /= ParetoNum;
                    break;
                }
                // 选择交叉变异
                speaii.Variation3(speaii.getarchive(), que);  // 需要注意，此处的pop均为浅拷贝
                speaii.getpop().decode(que.getMachineTime(), que.getDemand(), que.getSetupTime(), que.getDueDays());
            }
            AVGtotaldelay.add(avgtotaldelay);
            AVGutilization.add(avgutilization);
            totaldelay.add(besttotaldelay);
            utilization.add(bestutilization);

            System.out.println();
        }
        result.add(totaldelay);
        result.add(utilization);
        result.add(AVGtotaldelay);
        result.add(AVGutilization);

        return result;
    }

    /**
     * 试验记录写入excel
     * @param datalist
     */
    public static void writeEXCEL1(ArrayList<ArrayList<Double>> datalist) {
        OutputStream out = null;
        try {
            XSSFWorkbook xssfWorkbook = new XSSFWorkbook(new FileInputStream("E:\\JavaProjects\\graduation\\sourse\\experiment.xlsx"));
            XSSFSheet sheet = xssfWorkbook.getSheetAt(2);
            for (int j = 0; j < datalist.size(); j++) {
                ArrayList<Double> data = datalist.get(j);
                for (int row = 0; row < data.size(); row++) {
                    sheet.getRow(row + 1).getCell(j + 5).setCellValue(data.get(row));
                }
            }
            out = new FileOutputStream("E:\\JavaProjects\\graduation\\sourse\\experiment.xlsx");
            xssfWorkbook.write(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 实验数据分析
     * @return 
     */
    public static ArrayList<ArrayList<Double>> analysis() {
        String experimentdata = "E:\\JavaProjects\\graduation\\sourse\\experiment.xlsx";

        // 读取实验数据
        ArrayList<Double> totaldelay = new ArrayList<>();
        ArrayList<Double> utilization = new ArrayList<>();
        ArrayList<Double> AVGtotaldelay = new ArrayList<>();
        ArrayList<Double> AVGutilization = new ArrayList<>();
        try {
            //创建工作簿对象
            XSSFWorkbook xssfWorkbook = new XSSFWorkbook(new FileInputStream(experimentdata));
            XSSFSheet sheet = xssfWorkbook.getSheetAt(2);
            int experimenttimes = sheet.getPhysicalNumberOfRows() - 1;
            for (int i = 0; i < experimenttimes; i++) {
                totaldelay.add(sheet.getRow(i + 1).getCell(5).getNumericCellValue());
                utilization.add(sheet.getRow(i + 1).getCell(6).getNumericCellValue());
                AVGtotaldelay.add(sheet.getRow(i + 1).getCell(7).getNumericCellValue());
                AVGutilization.add(sheet.getRow(i + 1).getCell(8).getNumericCellValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 计算S/N
        ArrayList<Double> SNtotaldelay = new ArrayList<>();
        ArrayList<Double> SNutilization = new ArrayList<>();
        ArrayList<Double> SNAVGtotaldelay = new ArrayList<>();
        ArrayList<Double> SNAVGutilization = new ArrayList<>();
        double sumt = 0;
        double sumu = 0;
        double sumAVGt = 0;
        double sumAVGu = 0;
        for (int i = 0; i < totaldelay.size(); i++) {
            sumt += Math.pow(totaldelay.get(i), 2);
            sumu += 1 / Math.pow(utilization.get(i), 2);
            sumAVGt += Math.pow(AVGtotaldelay.get(i), 2);
            sumAVGu += 1 / Math.pow(AVGutilization.get(i), 2);
            if (i % 5 == 4) {
                SNtotaldelay.add(- 10 * Math.log10(sumt / 5));
                SNutilization.add(- 10 * Math.log10(sumu / 5));
                SNAVGtotaldelay.add(- 10 * Math.log10(sumAVGt / 5));
                SNAVGutilization.add(-10 * Math.log10(sumAVGu / 5));
                sumt = 0;
                sumu = 0;
                sumAVGt = 0;
                sumAVGu = 0;
            }
        }

        ArrayList<ArrayList<Double>> datalist = new ArrayList<>();
        datalist.add(SNtotaldelay);
        datalist.add(SNutilization);
        datalist.add(SNAVGtotaldelay);
        datalist.add(SNAVGutilization);

        return datalist;
    }

    public static void writeEXCEL2(ArrayList<ArrayList<Double>> datalist) {
        OutputStream out = null;
        try {
            XSSFWorkbook xssfWorkbook = new XSSFWorkbook(new FileInputStream("E:\\JavaProjects\\graduation\\sourse\\experiment.xlsx"));
            XSSFSheet sheet = xssfWorkbook.getSheetAt(3);
            for (int i = 0; i < datalist.size(); i++) {
                ArrayList<Double> data = datalist.get(i);
                for (int row = 0; row < data.size(); row++) {
                    sheet.getRow(row + 1).getCell(i + 5).setCellValue(data.get(row));
                }
            }
            out = new FileOutputStream("E:\\JavaProjects\\graduation\\sourse\\experiment.xlsx");
            xssfWorkbook.write(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
