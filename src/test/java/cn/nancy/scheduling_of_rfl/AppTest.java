package cn.nancy.scheduling_of_rfl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.Test;

import cn.nancy.scheduling_of_rfl.spea2.AllInSPEA2;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    enum OBJECTIVE {
            MAX, MIN
    };
    /**
     * Rigorous Test :-)
     * @throws Exception
     */
    @Test
    public void shouldAnswerWithTrue() throws Exception
    {
        String[] AlgorithemNameList = new String[] {"SPEA2"};
        String[] casename = new String[] {"case1"};
        int exptime = 10;
        int[] MaxIteration = {50, 400, 500}; // {200, 400, 500};
        int popsize = 80;
        int archivesize = 60;
        int[] TimeOfThread = {3000, 18000, 240000};  // 每个实验运行时长，单位：s

        int i = 0;
        String instance = "case1";
        int caseindex = 1;
        String path = "E:\\JavaProjects\\scheduling_of_rfl\\";
        String filename = path + instance + ".xlsx";
        String Directorys = instance + "_time" + (i+1);
        String storefile = path + "result\\" + Directorys;
        Qus qus = new Qus(filename);
        AllInSPEA2 spea2 = new AllInSPEA2(qus, storefile + "\\SPEA2_1", popsize, archivesize, MaxIteration[caseindex-1], 4);
        spea2.call();
    }
}
