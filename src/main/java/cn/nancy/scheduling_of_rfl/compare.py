# -*- coding: utf-8 -*-

# %%
import datetime
from matplotlib import pyplot as plt
from matplotlib.lines import Line2D
from matplotlib.patches import ConnectionPatch, Patch
import numpy as np
import pandas as pd
import plotly.express as px
import json
import time

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

def readLineData(path):
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

if __name__ == '__main__':
    #%% 前沿面比较图
    for i in range(1,2):
        path = ("E:\\JavaProjects\\zhengnanxi\\scheduling_of_rfl\\result\\case1_time%d" % (i+1))
        NSGAIITotalDelay, NSGAIIUtilization = readParetoData(path + "\\NSGAII\\ParetoFront.txt")
        # SPEA2_1TotalDelay, SPEA2_1Utilization = readParetoData(path + "\\SPEA2_1\\ParetoFront.txt")
        SPEA2_2TotalDelay, SPEA2_2Utilization = readParetoData(path + "\\SPEA2_2\\ParetoFront.txt")
        # SPEA2_3TotalDelay, SPEA2_3Utilization = readParetoData(path + "\\SPEA2_3\\ParetoFront.txt")
        NSGAIITotalDelay.sort()
        NSGAIIUtilization.sort()
        # SPEA2_1TotalDelay.sort()
        # SPEA2_1Utilization.sort()
        SPEA2_2TotalDelay.sort()
        SPEA2_2Utilization.sort()
        # SPEA2_3TotalDelay.sort()
        # SPEA2_3Utilization.sort()
        
        fig = plt.figure((2*i+1), figsize=(5,5))
        plt.rcParams['font.sans-serif'] = ['Arial']
        plt.plot(NSGAIITotalDelay, NSGAIIUtilization, marker='^', markersize='6', markerfacecolor='w', ls='-', label='NSGAII')
        # plt.plot(SPEA2_1TotalDelay, SPEA2_1Utilization, marker='*', markersize='6', markerfacecolor='w', ls=':', label='SPEA2_1')
        plt.plot(SPEA2_2TotalDelay, SPEA2_2Utilization, marker='o', markersize='6', markerfacecolor='w', ls='--', label='SPEA2')
        # plt.plot(SPEA2_3TotalDelay, SPEA2_3Utilization, marker='s', markersize='6', markerfacecolor='w', ls='-.', label='SPEA2_3')
        # plt.scatter(NSGAIITotalDelay, NSGAIIUtilization, marker='^', s=20, label='NSGAII')
        # plt.scatter(SPEA2_1TotalDelay, SPEA2_1Utilization, s=20, label='SPEA2_1')
        # plt.scatter(SPEA2_2TotalDelay, SPEA2_2Utilization, marker='*', s=20, label='SPEA2_2')
        # plt.scatter(SPEA2_3TotalDelay, SPEA2_3Utilization, marker='x', s=20, label='SPEA2_3')
        plt.xlabel("total delay/min", fontdict={'size': 12}, fontproperties='Arial')
        plt.ylabel("average utilization/%", fontdict={'size': 12}, fontproperties='Arial')
        # plt.title("Comparison of the Pareto Fronts for %s" % path[-11:], fontdict={'size': 14}, fontproperties='SimHei')
        plt.legend(loc='best', fontsize=10)
        plt.grid()
        fig.tight_layout()
        plt.savefig(path + "\\算法最优解比较图.jpg", format='jpg', dpi=300)
        print('NSGAII：\t%d个Pareto解' % len(NSGAIITotalDelay))
        # print('SPEA2_1：\t%d个Pareto解' % len(SPEA2_1TotalDelay))
        print('SPEA2_2：\t%d个Pareto解' % len(SPEA2_2TotalDelay))
        # print('SPEA2_3：\t%d个Pareto解\n' % len(SPEA2_3TotalDelay))
    
        # %% 迭代曲线比较图
        algorithemlist = ["NSGAII", "SPEA2_2"]
        legendlist = ["NSGAII", "SPEA2"]
        titlelist = ["Total Delay", "Utilization"]
        colorlist = ['C0', 'C1']
        markerlist = ['x', '+', '1', 4]
        ylabellist = ['best total delay/min', 'best utilization/%']
        boxdata = [[np.array(NSGAIITotalDelay), np.array(SPEA2_2TotalDelay)], [NSGAIIUtilization, SPEA2_2Utilization]]
        legend_elements = [Line2D([0], [0], color=colorlist[i], marker=markerlist[i], linestyle='-.', label=legendlist[i]) for i in range(len(colorlist))]

        fig = plt.figure(2*(i+1), figsize=(5,5))
        plt.rcParams['font.sans-serif'] = ['Arial']
        # fig.suptitle("Iterative Curve Comparison for %s" % path[-11:])
        spec = fig.add_gridspec(nrows=2, ncols=2, width_ratios=[3,1])
        axs = [[[] for _ in range(2)] for _ in range(2)]
        for i in range(2):
            for j in range(2):
                axs[i][j] = fig.add_subplot(spec[i, j])
        
        for i in range(len(algorithemlist)):
            utilization, totaldelay, toputilization, toptotaldelay, bottomutilization, bottomtotaldelay = readLineData(path + "\\" + algorithemlist[i])
            axs[0][0].plot(range(len(bottomtotaldelay)), bottomtotaldelay, color=colorlist[i], linestyle='-.', label=legendlist[i])
            axs[0][0].scatter(range(0,len(bottomtotaldelay),10), bottomtotaldelay[::10], color=colorlist[i], marker=markerlist[i])
            axs[1][0].plot(range(len(toputilization)), toputilization, color=colorlist[i], linestyle='-.', label=legendlist[i])
            axs[1][0].scatter(range(0,len(toputilization),10), toputilization[::10], color=colorlist[i], marker=markerlist[i])
        for i in range(2):
            axs[i][0].set_ylabel(ylabellist[i], fontsize=10)
            axs[i][0].set_xlabel("iterations", fontsize=10)
            axs[i][0].legend(handles=legend_elements, fontsize=10)
            # axs[i][0].set_title(titlelist[i], fontdict={'size': 14}, fontproperties='SimHei', y=-0.25)
        # axs[0][0].sharex(axs[1][0])

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
            axs[i][1].set_xticklabels(legendlist, rotation=45, fontsize=10)
            for patch, color in zip(bplot['boxes'], colorlist):
                patch.set_facecolor(color)
        
        axs[0][0].ticklabel_format(style='sci',scilimits=(0,0), axis='y', useMathText=True)
        axs[0][1].ticklabel_format(style='sci',scilimits=(0,0), axis='y', useMathText=True)

        fig.tight_layout()
        plt.savefig(path + "\\算法迭代曲线比较图.jpg", format='jpg', dpi=300)

    plt.show()

        #%% 前沿面C-metric
        # c = 0
        # for i in range(len(NSGAIITotalDelay)):
        #     for j in range(len(SPEA2_1TotalDelay)):
        #         if NSGAIIUtilization[i] <= SPEA2_1Utilization[j] and NSGAIITotalDelay[i] > SPEA2_1TotalDelay[j] or NSGAIIUtilization[i] < SPEA2_1Utilization[j] and NSGAIITotalDelay[i] == SPEA2_1TotalDelay[j]:
        #             c+=1
        #             break
        # c = c/len(NSGAIITotalDelay)
        # print('C(SPEA2_1, NSGAII) = %f, ' % c, end=' ')
        # c = 0
        # for i in range(len(SPEA2_1Utilization)):
        #     for j in range(len(NSGAIITotalDelay)):
        #         if NSGAIIUtilization[j] >= SPEA2_1Utilization[i] and NSGAIITotalDelay[j] < SPEA2_1TotalDelay[i] or NSGAIIUtilization[j] > SPEA2_1Utilization[i] and NSGAIITotalDelay[j] == SPEA2_1TotalDelay[i]:
        #             c+=1
        #             break
        # c = c/len(SPEA2_1Utilization)
        # print('C(NSGAII, SPEA2_1) = %f; ' % c)

        # c = 0
        # for i in range(len(NSGAIITotalDelay)):
        #     for j in range(len(SPEA2_2TotalDelay)):
        #         if NSGAIIUtilization[i] <= SPEA2_2Utilization[j] and NSGAIITotalDelay[i] > SPEA2_2TotalDelay[j] or NSGAIIUtilization[i] < SPEA2_2Utilization[j] and NSGAIITotalDelay[i] == SPEA2_2TotalDelay[j]:
        #             c+=1
        #             break
        # c = c/len(NSGAIITotalDelay)
        # print('C(SPEA2_2, NSGAII) = %f, ' % c, end=' ')
        # c = 0
        # for i in range(len(SPEA2_2Utilization)):
        #     for j in range(len(NSGAIITotalDelay)):
        #         if NSGAIIUtilization[j] >= SPEA2_2Utilization[i] and NSGAIITotalDelay[j] < SPEA2_2TotalDelay[i] or NSGAIIUtilization[j] > SPEA2_2Utilization[i] and NSGAIITotalDelay[j] == SPEA2_2TotalDelay[i]:
        #             c+=1
        #             break
        # c = c/len(SPEA2_2Utilization)
        # print('C(NSGAII, SPEA2_2) = %f; ' % c)

        # c = 0
        # for i in range(len(NSGAIITotalDelay)):
        #     for j in range(len(SPEA2_3TotalDelay)):
        #         if NSGAIIUtilization[i] <= SPEA2_3Utilization[j] and NSGAIITotalDelay[i] > SPEA2_3TotalDelay[j] or NSGAIIUtilization[i] < SPEA2_3Utilization[j] and NSGAIITotalDelay[i] == SPEA2_3TotalDelay[j]:
        #             c+=1
        #             break
        # c = c/len(NSGAIITotalDelay)
        # print('C(SPEA2_3, NSGAII) = %f, ' % c, end=' ')
        # c = 0
        # for i in range(len(SPEA2_3Utilization)):
        #     for j in range(len(NSGAIITotalDelay)):
        #         if NSGAIIUtilization[j] >= SPEA2_3Utilization[i] and NSGAIITotalDelay[j] < SPEA2_3TotalDelay[i] or NSGAIIUtilization[j] > SPEA2_3Utilization[i] and NSGAIITotalDelay[j] == SPEA2_3TotalDelay[i]:
        #             c+=1
        #             break
        # c = c/len(SPEA2_3Utilization)
        # print('C(NSGAII, SPEA2_3) = %f. ' % c)

        # # %% 超体积指标HV
        # r_TotalDelay, r_Utilization = (1500000, 40)
        # NSGAIITotalDelay.insert(0, r_TotalDelay)
        # SPEA2_1TotalDelay.insert(0, r_TotalDelay)
        # SPEA2_2TotalDelay.insert(0, r_TotalDelay)
        # SPEA2_3TotalDelay.insert(0, r_TotalDelay)
        # NSGAIIUtilization.insert(0, r_Utilization)
        # SPEA2_1Utilization.insert(0, r_Utilization)
        # SPEA2_2Utilization.insert(0, r_Utilization)
        # SPEA2_3Utilization.insert(0, r_Utilization)
        # HV = 0
        # for i in range(1, len(NSGAIITotalDelay)):
        #     HV += (r_TotalDelay - NSGAIITotalDelay[i]) * (NSGAIIUtilization[i] - NSGAIIUtilization[i-1])
        # print('HV(NSGAII) =\t%-f, ' % HV)
        # HV = 0
        # for i in range(1, len(SPEA2_1TotalDelay)):
        #     HV += (r_TotalDelay - SPEA2_1TotalDelay[i]) * (SPEA2_1Utilization[i] - SPEA2_1Utilization[i-1])
        # print('HV(SPEA2_1) = \t%-f, ' % HV)
        # HV = 0
        # for i in range(1, len(SPEA2_2TotalDelay)):
        #     HV += (r_TotalDelay - SPEA2_2TotalDelay[i]) * (SPEA2_2Utilization[i] - SPEA2_2Utilization[i-1])
        # print('HV(SPEA2_2) =\t%-f. ' % HV)
        # HV = 0
        # for i in range(1, len(SPEA2_3TotalDelay)):
        #     HV += (r_TotalDelay - SPEA2_3TotalDelay[i]) * (SPEA2_3Utilization[i] - SPEA2_3Utilization[i-1])
        # print('HV(SPEA2_3) =\t%-f. ' % HV)

        # %%
