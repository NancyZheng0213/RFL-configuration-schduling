# -*- coding: utf-8 -*-

# %%
from matplotlib import pyplot as plt
from matplotlib.lines import Line2D
import numpy as np
import json
import csv

# %%
def readParetoData(path):
    """ 读取所有 pareto front 的坐标值，排除了重复的点，便于计算方案数量
    """
    TotalDelay = []
    Utilization = []

    with open(path) as f:
        for line in f.readlines():
            datas = json.loads(line)
            for data in datas:
                if datas[data][0] not in TotalDelay:
                    TotalDelay.append(datas[data][0])
                if datas[data][1] not in Utilization:
                    Utilization.append(datas[data][1])

    return TotalDelay, Utilization


def readScatterData(path):
    """ 读取所有方案的目标值
    """
    TotalDelay = []
    Utilization = []

    with open(path) as f:
        for line in f.readlines():
            datas = json.loads(line)
            for data in datas:
                TotalDelay.append(datas[data][0])
                Utilization.append(datas[data][1])

    return TotalDelay, Utilization

def readLineData(path:str):
    """读取迭代曲线数据
    """
    utilization = []
    totaldelay = []
    toptotaldelay = []
    toputilization = []
    bottomtotaldelay = []
    bottomutilization = []

    with open(path + "\\uOfA.txt") as f:
        for line in f.readlines():
            utilization.append(json.loads(line))
    with open(path + "\\tOfA.txt") as f:
        for line in f.readlines():
            totaldelay.append(json.loads(line))
    with open(path + "\\topuOfA.txt") as f:
        for line in f.readlines():
            toputilization.append(json.loads(line))
    with open(path + "\\bottomtOfA.txt") as f:
        for line in f.readlines():
            bottomtotaldelay.append(json.loads(line))
    with open(path + "\\bottomuOfA.txt") as f:
        for line in f.readlines():
            bottomutilization.append(json.loads(line))
    with open(path + "\\toptOfA.txt") as f:
        for line in f.readlines():
            toptotaldelay.append(json.loads(line))
    
    return utilization, totaldelay, toputilization, toptotaldelay, bottomutilization, bottomtotaldelay

def drawParetoFront(caseNUM:int, path:str, AlgorithmList:list):
    figlist = list()
    markerlist = ['^', 'o', '*', 's']
    lslist = ['-', ':', '--', '-.']
    for caseindex in range(caseNUM):
        p = path + "time%d" % (caseindex + 1)
        TotalDelayList = list()
        UtilizationList = list()
        for algorithem in AlgorithmList:
            TotalDelay, Utilization = readParetoData(p + "\\" + algorithem + "\\ParetoFront.txt")
            TotalDelay.sort()
            Utilization.sort()
            TotalDelayList.append(TotalDelay)
            UtilizationList.append(Utilization)
        fig = plt.figure((2*caseindex+1), figsize=(4,4))
        for i in range(len(AlgorithmList)):
            plt.plot(TotalDelayList[i], UtilizationList[i], marker=markerlist[i], markersize='6', markerfacecolor='w', ls=lslist[i], label=AlgorithmList[i])
        plt.xlabel("total delay/min", fontdict={'size': 12}, fontproperties='Arial')
        plt.ylabel("average utilization/%", fontdict={'size': 12}, fontproperties='Arial')
        plt.title("Comparison of the Pareto Fronts for %s" % p[-11:], fontdict={'size': 14}, fontproperties='SimHei')
        plt.legend(loc='best', fontsize=10)
        plt.grid()
        fig.tight_layout()
        plt.savefig(p + " Comparsion of Pareto Fronts.png", format='png', dpi=300)
        plt.savefig(p + "\\Comparsion of Pareto Fronts.png", format='png', dpi=300)
        figlist.append(fig)

    return figlist
    
def drawIteration(caseNUM:int, path:str, AlgorithmList:list):
    figlist = list()
    titlelist = ["Total Delay", "Utilization"]
    ylabellist = ['best total delay/min', 'best utilization/%']
    for caseindex in range(caseNUM):
        p = path + "time%d" % (caseindex + 1)
        colorlist = ['C0', 'C1', 'C2', 'C3', 'C4']
        markerlist = ['x', '+', '1', 4, '*']
        TotalDelayList = list()
        UtilizationList = list()
        for algorithem in AlgorithmList:
            TotalDelay, Utilization = readParetoData(p + "\\" + algorithem + "\\ParetoFront.txt")
            TotalDelay.sort()
            Utilization.sort()
            TotalDelayList.append(TotalDelay)
            UtilizationList.append(Utilization)
        boxdata = [[np.array(TotalDelayList[j]) for j in range(len(TotalDelayList))], [UtilizationList[j] for j in range(len(UtilizationList))]]
        legend_elements = [Line2D([0], [0], color=colorlist[i], marker=markerlist[i], linestyle='-.', label=AlgorithmList[i]) for i in range(len(AlgorithmList))]
        fig = plt.figure(2*(caseindex+1), figsize=(5,5))
        fig.suptitle("Iterative Curve Comparison for %s" % p[-11:])
        spec = fig.add_gridspec(nrows=2, ncols=2, width_ratios=[3,1])
        axs = [[[] for _ in range(2)] for _ in range(2)]
        for i in range(2):
            for j in range(2):
                axs[i][j] = fig.add_subplot(spec[i, j])
        for i in range(len(AlgorithmList)):
            utilization, totaldelay, toputilization, toptotaldelay, bottomutilization, bottomtotaldelay = readLineData(p + "\\" + AlgorithmList[i])
            axs[0][0].plot(range(len(bottomtotaldelay)), bottomtotaldelay, color=colorlist[i], linestyle='-.', label=AlgorithmList[i])
            axs[0][0].scatter(range(0,len(bottomtotaldelay),10), bottomtotaldelay[::10], color=colorlist[i], marker=markerlist[i])
            axs[1][0].plot(range(len(toputilization)), toputilization, color=colorlist[i], linestyle='-.', label=AlgorithmList[i])
            axs[1][0].scatter(range(0,len(toputilization),10), toputilization[::10], color=colorlist[i], marker=markerlist[i])
        for i in range(2):
            axs[i][0].set_ylabel(ylabellist[i], fontsize=10)
            axs[i][0].set_xlabel("iterations", fontsize=10)
            axs[i][0].legend(handles=legend_elements, fontsize=10)
            axs[i][0].set_title(titlelist[i], fontdict={'size': 14}, fontproperties='SimHei', y=-0.25)
        for i in range(2):
            conf_intervalslist = [(5000,5200) for j in range(len(boxdata[i]))]
            bplot = axs[i][1].boxplot(
                boxdata[i],
                # notch=True,
                widths=0.5,
                patch_artist=True,
                boxprops=dict(facecolor='C1'),
                # conf_intervals=(conf_intervalslist),
                bootstrap=10000,
                medianprops=dict(color='black')
            )
            axs[i][1].set_xticklabels(AlgorithmList, rotation=45, fontsize=10)
            for patch, color in zip(bplot['boxes'], colorlist):
                patch.set_facecolor(color)
        axs[0][0].ticklabel_format(style='sci',scilimits=(0,0), axis='y', useMathText=True)
        axs[0][1].ticklabel_format(style='sci',scilimits=(0,0), axis='y', useMathText=True)
        fig.tight_layout()
        plt.savefig(p + " Comparision of Iteration.png", format='png', dpi=300)
        plt.savefig(p + "\\Comparision of Iteration.png", format='png', dpi=300)
        figlist.append(fig)

    return figlist

def calculate(caseNUM:int, path:str, AlgorithmList:list):
    Clist = list()
    Nlist = list()
    HVlist = list()
    r_TotalDelay, r_Utilization = (3000000, 40)
    for caseindex in range(caseNUM):
        CmetricDict= {}
        NDict = {}
        HVDict = {}
        p = path + "time%d" % (caseindex + 1)
        TotalDelayDict = {}
        UtilizationDict = {}
        for algorithem in AlgorithmList:
            TotalDelay, Utilization = readParetoData(p + "\\" + algorithem + "\\ParetoFront.txt")
            TotalDelay.sort()
            Utilization.sort()
            TotalDelayDict[algorithem] = TotalDelay
            UtilizationDict[algorithem] = Utilization
        for algorithmi in AlgorithmList:
            NDict["n_" + algorithmi] = len(TotalDelayDict.get(algorithmi))
            for algorithmj in AlgorithmList:
                if algorithmi != algorithmj:
                    TotalDelayi = TotalDelayDict.get(algorithmi)
                    TotalDelayj = TotalDelayDict.get(algorithmj)
                    Utilizationi = UtilizationDict.get(algorithmi)
                    Utilizationj = UtilizationDict.get(algorithmj)
                    c = 0
                    for i in range(len(TotalDelayi)):
                        for j in range(len(TotalDelayj)):
                            if Utilizationi[i] <= Utilizationj[j] and TotalDelayi[i] > TotalDelayj[j] or Utilizationi[i] < Utilizationj[j] and TotalDelayi[i] == TotalDelayj[j]:
                                c+=1
                                break
                    c = c/len(TotalDelayi)
                    CmetricDict["C(%s,%s)" % (algorithmj, algorithmi)] = c
        for algorithm in AlgorithmList:
            TotalDelay = list(TotalDelayDict.get(algorithm))
            Utilization = list(UtilizationDict.get(algorithm))
            TotalDelay.insert(0, r_TotalDelay)
            Utilization.insert(0,r_Utilization)
            HV = 0
            for i in range(1, len(TotalDelay)):
                HV += (r_TotalDelay - TotalDelay[i])*(Utilization[i] - Utilization[i-1])/r_TotalDelay
            HVDict["HV(%s)" % algorithm] = HV
        Clist.append(CmetricDict)
        Nlist.append(NDict)
        HVlist.append(HVDict)

    return Clist, Nlist, HVlist


if __name__ == '__main__':
    """
    单个案例多图输出，每个实验两张图（Pareto Front & 迭代曲线）
    """
    
    p = "E:\\JavaProjects\\scheduling_of_rfl\\result\\case3_"
    CSVpath = p + "compare.csv"
    algorithemlist = ["SPEA2_2","NSGAII_2","MOEA"]
    ColumnHeading = ["NO."]
    for algorithm in algorithemlist:
        ColumnHeading.append("n_" + algorithm)
    for i in range(len(algorithemlist)):
        for j in range(i+1, len(algorithemlist)):
            ColumnHeading.append("C(%s,%s)" % (algorithemlist[i], algorithemlist[j]))
            ColumnHeading.append("C(%s,%s)" % (algorithemlist[j], algorithemlist[i]))
    for algorithm in algorithemlist:
        ColumnHeading.append("HV(%s)" % algorithm)
    with open(CSVpath, 'w', newline='') as f:
        writer = csv.writer(f)
        writer.writerow(ColumnHeading)

    ParetoFrontFigList = drawParetoFront(5, p, algorithemlist)
    IterationFigList = drawIteration(5, p, algorithemlist)
    CList, NList, HVList = calculate(5, p, algorithemlist)
    
    for caseindex in range(5):
        path = (p + "time%d" % (caseindex+1))
        print('\n\n----------' + path[-11:] + '----------')
        data = [caseindex + 1]
        C = CList[caseindex]
        N = NList[caseindex]
        HV = HVList[caseindex]
        for heading in ColumnHeading[1:]:
            if heading in C:
                data.append(C.get(heading))
                continue
            if heading in N:
                data.append(N.get(heading))
                continue
            if heading in HV:
                data.append(HV.get(heading))
                continue
        with open(CSVpath, 'a', newline='') as f:
            writer = csv.writer(f)
            writer.writerow(data)
        fig1 = ParetoFrontFigList[caseindex]
        fig2 = IterationFigList[caseindex]
        plt.rcParams['font.sans-serif'] = ['Arial']
    plt.show()

