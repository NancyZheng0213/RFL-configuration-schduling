package cn.nancy;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 此类为数据读取类，获取案例数据.  
 * 
 * @attribute 零件种类数 PartssNum、机器种类数量 MachineTypesNum、每种机器的数量 MachineNum、工序总数 ProcessNum、加工工序合集 ProcessSet、工件对应的工艺流程 Process、机器对应的可加工操作 Machine
 */
public class Qus {
    /**
     * 零件种类数
     */
    private int PartsNum;
    /**
     * 工序总数
     */
    private int ProcessNum;
    /**
     * 机器种类数量
     */
    private int MachineTypesNum;
    /**
     * 可替代机器集合，记录每台机器相同类型的可以互相替代的机器编号，如 {1:[2,3]} 表示机器 1 可以用机器 2 或 3 替代
     */
    private Map<Integer, ArrayList<Integer>> AlternativeMachine;
    /**
     * 每个工件对应的工艺流程，采用整数编号，如 {4:[1,3,5,6,7]} 表示工件 4 需要经过的工艺操作为编号 1、3、5、6、7 的工艺
     */
    private Map<Integer, ArrayList<Integer>> Process;
    /**
     * 每台机器对应的可加工操作，机器采用整数编码，如 {1:[1,2,3,5,6]} 表示机器 1 可以提供编号为 1、2、3、5、6 的工艺操作
     */
    private Map<Integer, ArrayList<Integer>> Machine;
    /**
     * 每台机器对应的可加工操作的操作时间，如 {1:{1:1, 3:2, 6:3}} 表示机器 1 的工艺操作时间分别为 1 min、2 min、3 min
     */
    private Map<Integer, TreeMap<Integer, Double>> MachineTime;
    /**
     * 工件需求量
     */
    private Map<Integer, Integer> Demand;
    /**
     * 工件交付时间，单位：分钟
     */
    private Map<Integer, Integer> DueDays;
    /**
     * 每个操作的切换时间
     */
    private Map<Integer, Double> SetupTime;

    /**
     * 当机器存在替代集合时，获取零件种类数、机器总数、工序总数、工艺流程、机器操作集合和机器替代集合
     * @param filename excel文件路径
     * @param sheetname1 工件工艺流程表表名
     * @param sheetname2 机器操作集合表名
     * @param sheetname3 机器替代集合表名
     */
    Qus (String filename) {
        try {
            //创建工作簿对象
            XSSFWorkbook xssfWorkbook = new XSSFWorkbook(new FileInputStream(filename));
            // 表1：工件工艺流程；表2：机器可提供操作集合；表3：机器替代集合；表4：工件需求量及交付时间；表5：切换时间
            XSSFSheet sheet1 = xssfWorkbook.getSheetAt(0);
            XSSFSheet sheet2 = xssfWorkbook.getSheetAt(1);
            XSSFSheet sheet3 = xssfWorkbook.getSheetAt(2);
            XSSFSheet sheet4 = xssfWorkbook.getSheetAt(3);
            XSSFSheet sheet5 = xssfWorkbook.getSheetAt(4);
            // 获取零件种类数、工序总数、机器种类数量
            this.PartsNum = sheet1.getPhysicalNumberOfRows() - 1;
            this.ProcessNum = sheet1.getRow(0).getLastCellNum() - 1;
            this.MachineTypesNum = sheet2.getPhysicalNumberOfRows() - 1;
            // 获取每种零件的工艺流程
            this.Process = new HashMap<Integer, ArrayList<Integer>>();
            for (int i = 0; i < this.PartsNum; i++) {
                ArrayList<Integer> processList = new ArrayList<>();
                for (int j = 0; j < this.ProcessNum; j++) {
                    if (sheet1.getRow(i + 1).getCell(j + 1).getNumericCellValue() != 0) {
                        processList.add(j + 1);
                    }
                }
                if (processList.isEmpty()) {
                    throw new Exception("存在空的工艺流程集合");
                } else {
                    this.Process.put(i + 1, processList);
                }
            }
            // 获取每台机器的可操作集合及操作时间
            this.Machine = new HashMap<Integer, ArrayList<Integer>>();
            this.MachineTime = new HashMap<Integer, TreeMap<Integer, Double>>();
            for (int i = 0; i < this.MachineTypesNum; i++) {
                ArrayList<Integer> processList = new ArrayList<>();
                TreeMap<Integer, Double> timeMap = new TreeMap<>();
                for (int j = 0; j < this.ProcessNum; j++) {
                    if (sheet2.getRow(i+1).getCell(j+1).getNumericCellValue() != 0) {
                        processList.add(j+1);
                        timeMap.put(j+1, sheet2.getRow(i+1).getCell(j+1).getNumericCellValue());
                    }
                }
                if (processList.isEmpty()) {
                    throw new Exception("存在空的机器操作集合");
                } else {
                    this.Machine.put(i+1, processList);
                    this.MachineTime.put(i+1, timeMap);
                }
            }
            // 获取每台机器的可替代机器集合
            this.AlternativeMachine = new HashMap<Integer, ArrayList<Integer>>();
            for (int i = 0; i < this.MachineTypesNum; i++) {
                ArrayList<Integer> alternative = new ArrayList<>();
                for (int j = 0; j < this.MachineTypesNum; j++) {
                    if (sheet3.getRow(i+1).getCell(j+1).getNumericCellValue() != 0) {
                        alternative.add(j+1);
                    }
                }
                this.AlternativeMachine.put(i + 1, alternative);
            }
            // 获取工件需求量及交付时间
            this.Demand = new HashMap<Integer, Integer>();
            this.DueDays = new HashMap<Integer, Integer>();
            for (int i = 0; i < this.PartsNum; i++) {
                this.Demand.put(i+1, (int) sheet4.getRow(i+1).getCell(1).getNumericCellValue());
                this.DueDays.put(i+1, (int) sheet4.getRow(i+1).getCell(2).getNumericCellValue() * 24 * 60);
            }
            // 获取切换时间
            this.SetupTime = new HashMap<Integer, Double>();
            for (int i = 0; i < this.ProcessNum; i++) {
                this.SetupTime.put(i+1, sheet5.getRow(1).getCell(i).getNumericCellValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取零件种类数
     */
    public int getPartsNum() {
        return this.PartsNum;
    }
    /**
     * 获取机器种类数量
     */
    public int getMachineTypesNum() {
        return this.MachineTypesNum;
    }
    /**
     * 获取工序总数
     */
    public int getProcessNum() {
        return this.ProcessNum;
    }
    /**
     * 返回可替代机器集合
     * @return
     */
    public Map<Integer, ArrayList<Integer>> getAlternativeMachine() {
        return this.AlternativeMachine;
    }
    /**
     * 获取每台机器对应的可加工操作
     * @return Map<Integer, ArrayList<Integer>>
     */
    public Map<Integer, ArrayList<Integer>> getMachine() {
        return this.Machine;
    }
    /**
     * 获取每个工件对应的工艺流程
     * @return Map<Integer, ArrayList<Integer>>
     */
    public Map<Integer, ArrayList<Integer>> getProcess() {
        return this.Process;
    }
    public ArrayList<Integer> getProcess(int i) {
        return this.Process.get(i);
    }
    /**
     * 获取工件需求量
     * @return
     */
    public Map<Integer, Integer> getDemand() {
        return this.Demand;
    }
    public Integer getDemand(int i) {
        return this.Demand.get(i);
    }
    /**
     * 获取工件交付时间
     * @return
     */
    public Map<Integer, Integer> getDueDays() {
        return this.DueDays;
    }
    public Integer getDueDays(int i) {
        return this.DueDays.get(i);
    }
    /**
     * 获取每台机器每个操作的加工时间
     * @return
     */
    public Map<Integer, TreeMap<Integer, Double>> getMachineTime() {
        return this.MachineTime;
    }
    public TreeMap<Integer, Double> getMachineTime(int machine) {
        return this.MachineTime.get(machine);
    }
    public Double getMachineTime(int key, int i) {
        return this.MachineTime.get(key).get(i);
    }
    /**
     * 获取每个操作的切换时间
     * @return
     */
    public Map<Integer, Double> getSetupTime() {
        return this.SetupTime;
    }
    public Double getSetupTime(int i) {
        return this.SetupTime.get(i);
    }
}
