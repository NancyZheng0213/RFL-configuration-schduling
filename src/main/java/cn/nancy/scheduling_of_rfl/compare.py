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
    p = "E:\\JavaProjects\\scheduling_of_rfl\\result\\case1_"
    CSVpath = p + "compare.csv"
    algorithemlist = ["SPEA2_2","NSGAII_1","NSGAII_2", "NSGAII_3", "NSGAII_4"]
    ColumnHeading = ["iteration"]
    for algorithem in algorithemlist:
        ColumnHeading.append("n_" + algorithem)
    for i in range(1, len(algorithemlist)):
        ColumnHeading.append("C(%s_%s)" % (algorithemlist[i], algorithemlist[0]))
        ColumnHeading.append("C(%s,%s)" % (algorithemlist[0], algorithemlist[i]))
    for algorithem in algorithemlist:
        ColumnHeading.append("HV(%s)" % algorithem)
    with open(CSVpath, 'w', newline='') as f:
        writer = csv.writer(f)
        writer.writerow(ColumnHeading)
    #%% 前沿面比较图
    for caseindex in range(7):
        path = (p + "time%d" % (caseindex+1))
        print('\n\n----------' + path[-11:] + '----------')
        SPEA2TotalDelay, SPEA2Utilization = readParetoData(path + "\\SPEA2_2\\ParetoFront.txt")
        NSGAII_1TotalDelay, NSGAII_1Utilization = readParetoData(path + "\\NSGAII_1\\ParetoFront.txt")
        NSGAII_2TotalDelay, NSGAII_2Utilization = readParetoData(path + "\\NSGAII_2\\ParetoFront.txt")
        NSGAII_3TotalDelay, NSGAII_3Utilization = readParetoData(path + "\\NSGAII_3\\ParetoFront.txt")
        NSGAII_4TotalDelay, NSGAII_4Utilization = readParetoData(path + "\\NSGAII_4\\ParetoFront.txt")
        SPEA2TotalDelay.sort()
        SPEA2Utilization.sort()
        NSGAII_1TotalDelay.sort()
        NSGAII_1Utilization.sort()
        NSGAII_2TotalDelay.sort()
        NSGAII_2Utilization.sort()
        NSGAII_3TotalDelay.sort()
        NSGAII_3Utilization.sort()
        NSGAII_4TotalDelay.sort()
        NSGAII_4Utilization.sort()
        
        fig = plt.figure((2*caseindex+1), figsize=(4,4))
        plt.rcParams['font.sans-serif'] = ['Arial']
        plt.plot(SPEA2TotalDelay, SPEA2Utilization, marker='^', markersize='6', markerfacecolor='w', ls='-', label='SPEA2')
        plt.plot(NSGAII_1TotalDelay, NSGAII_1Utilization, marker='*', markersize='6', markerfacecolor='w', ls=':', label='NSGAII_1')
        plt.plot(NSGAII_2TotalDelay, NSGAII_2Utilization, marker='o', markersize='6', markerfacecolor='w', ls='--', label='NSGAII_2')
        plt.plot(NSGAII_3TotalDelay, NSGAII_3Utilization, marker='s', markersize='6', markerfacecolor='w', ls='-.', label='NSGAII_3')
        plt.plot(NSGAII_4TotalDelay, NSGAII_4Utilization, marker='s', markersize='6', markerfacecolor='w', ls='-.', label='NSGAII_4')
        plt.xlabel("total delay/min", fontdict={'size': 12}, fontproperties='Arial')
        plt.ylabel("average utilization/%", fontdict={'size': 12}, fontproperties='Arial')
        # plt.title("Comparison of the Pareto Fronts for %s" % path[-11:], fontdict={'size': 14}, fontproperties='SimHei')
        plt.legend(loc='best', fontsize=10)
        plt.grid()
        fig.tight_layout()
        plt.savefig(path + "\\算法最优解比较图.jpg", format='jpg', dpi=300)
        print('SPEA2：\t%d个Pareto解' % len(SPEA2TotalDelay))
        print('NSGAII_1：\t%d个Pareto解' % len(NSGAII_1TotalDelay))
        print('NSGAII_2：\t%d个Pareto解' % len(NSGAII_2TotalDelay))
        print('NSGAII_3：\t%d个Pareto解' % len(NSGAII_3TotalDelay))
        print('NSGAII_4：\t%d个Pareto解' % len(NSGAII_4TotalDelay))
    
        # %% 迭代曲线比较图
        legendlist = algorithemlist
        titlelist = ["Total Delay", "Utilization"]
        colorlist = ['C0', 'C1', 'C2', 'C3', 'C4']
        markerlist = ['x', '+', '1', 4, '*']
        ylabellist = ['best total delay/min', 'best utilization/%']
        boxdata = [[np.array(SPEA2TotalDelay), np.array(NSGAII_1TotalDelay), np.array(NSGAII_2TotalDelay), np.array(NSGAII_3TotalDelay), np.array(NSGAII_4TotalDelay)], [SPEA2Utilization, NSGAII_1Utilization, NSGAII_2Utilization, NSGAII_3Utilization, NSGAII_4Utilization]]
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


        #%% 前沿面C-metric
        data = [caseindex+1,len(SPEA2TotalDelay),len(NSGAII_1TotalDelay),len(NSGAII_2TotalDelay),len(NSGAII_3TotalDelay),len(NSGAII_4TotalDelay)]
        c = 0
        for i in range(len(SPEA2TotalDelay)):
            for j in range(len(NSGAII_1TotalDelay)):
                if SPEA2Utilization[i] <= NSGAII_1Utilization[j] and SPEA2TotalDelay[i] > NSGAII_1TotalDelay[j] or SPEA2Utilization[i] < NSGAII_1Utilization[j] and SPEA2TotalDelay[i] == NSGAII_1TotalDelay[j]:
                    c+=1
                    break
        c = c/len(SPEA2TotalDelay)
        print('C(NSGAII_1, SPEA2) = %f, ' % c, end=' ')
        data.append(c)
        c = 0
        for i in range(len(NSGAII_1Utilization)):
            for j in range(len(SPEA2TotalDelay)):
                if SPEA2Utilization[j] >= NSGAII_1Utilization[i] and SPEA2TotalDelay[j] < NSGAII_1TotalDelay[i] or SPEA2Utilization[j] > NSGAII_1Utilization[i] and SPEA2TotalDelay[j] == NSGAII_1TotalDelay[i]:
                    c+=1
                    break
        c = c/len(NSGAII_1Utilization)
        print('C(SPEA2, NSGAII_1) = %f; ' % c)
        data.append(c)

        c = 0
        for i in range(len(SPEA2TotalDelay)):
            for j in range(len(NSGAII_2TotalDelay)):
                if SPEA2Utilization[i] <= NSGAII_2Utilization[j] and SPEA2TotalDelay[i] > NSGAII_2TotalDelay[j] or SPEA2Utilization[i] < NSGAII_2Utilization[j] and SPEA2TotalDelay[i] == NSGAII_2TotalDelay[j]:
                    c+=1
                    break
        c = c/len(SPEA2TotalDelay)
        print('C(NSGAII_2, SPEA2) = %f, ' % c, end=' ')
        data.append(c)
        c = 0
        for i in range(len(NSGAII_2Utilization)):
            for j in range(len(SPEA2TotalDelay)):
                if SPEA2Utilization[j] >= NSGAII_2Utilization[i] and SPEA2TotalDelay[j] < NSGAII_2TotalDelay[i] or SPEA2Utilization[j] > NSGAII_2Utilization[i] and SPEA2TotalDelay[j] == NSGAII_2TotalDelay[i]:
                    c+=1
                    break
        c = c/len(NSGAII_2Utilization)
        print('C(SPEA2, NSGAII_2) = %f; ' % c)
        data.append(c)

        c = 0
        for i in range(len(SPEA2TotalDelay)):
            for j in range(len(NSGAII_3TotalDelay)):
                if SPEA2Utilization[i] <= NSGAII_3Utilization[j] and SPEA2TotalDelay[i] > NSGAII_3TotalDelay[j] or SPEA2Utilization[i] < NSGAII_3Utilization[j] and SPEA2TotalDelay[i] == NSGAII_3TotalDelay[j]:
                    c+=1
                    break
        c = c/len(SPEA2TotalDelay)
        data.append(c)
        print('C(NSGAII_3, SPEA2) = %f, ' % c, end=' ')
        c = 0
        for i in range(len(NSGAII_3Utilization)):
            for j in range(len(SPEA2TotalDelay)):
                if SPEA2Utilization[j] >= NSGAII_3Utilization[i] and SPEA2TotalDelay[j] < NSGAII_3TotalDelay[i] or SPEA2Utilization[j] > NSGAII_3Utilization[i] and SPEA2TotalDelay[j] == NSGAII_3TotalDelay[i]:
                    c+=1
                    break
        c = c/len(NSGAII_3Utilization)
        data.append(c)
        print('C(SPEA2, NSGAII_3) = %f. ' % c)

        c = 0
        for i in range(len(SPEA2TotalDelay)):
            for j in range(len(NSGAII_4TotalDelay)):
                if SPEA2Utilization[i] <= NSGAII_4Utilization[j] and SPEA2TotalDelay[i] > NSGAII_4TotalDelay[j] or SPEA2Utilization[i] < NSGAII_4Utilization[j] and SPEA2TotalDelay[i] == NSGAII_4TotalDelay[j]:
                    c+=1
                    break
        c = c/len(SPEA2TotalDelay)
        data.append(c)
        print('C(NSGAII_4, SPEA2) = %f, ' % c, end=' ')
        c = 0
        for i in range(len(NSGAII_4Utilization)):
            for j in range(len(SPEA2TotalDelay)):
                if SPEA2Utilization[j] >= NSGAII_4Utilization[i] and SPEA2TotalDelay[j] < NSGAII_4TotalDelay[i] or SPEA2Utilization[j] > NSGAII_4Utilization[i] and SPEA2TotalDelay[j] == NSGAII_4TotalDelay[i]:
                    c+=1
                    break
        c = c/len(NSGAII_4Utilization)
        data.append(c)
        print('C(SPEA2, NSGAII_4) = %f; ' % c)

        # %% 超体积指标HV
        r_TotalDelay, r_Utilization = (1500000, 40)
        SPEA2TotalDelay.insert(0, r_TotalDelay)
        NSGAII_1TotalDelay.insert(0, r_TotalDelay)
        NSGAII_2TotalDelay.insert(0, r_TotalDelay)
        NSGAII_4TotalDelay.insert(0, r_TotalDelay)
        NSGAII_3TotalDelay.insert(0, r_TotalDelay)
        SPEA2Utilization.insert(0, r_Utilization)
        NSGAII_1Utilization.insert(0, r_Utilization)
        NSGAII_2Utilization.insert(0, r_Utilization)
        NSGAII_4Utilization.insert(0, r_Utilization)
        NSGAII_3Utilization.insert(0, r_Utilization)
        HV = 0
        for i in range(1, len(SPEA2TotalDelay)):
            HV += (r_TotalDelay - SPEA2TotalDelay[i]) * (SPEA2Utilization[i] - SPEA2Utilization[i-1])
        print('HV(SPEA2) =\t%-f, ' % HV)
        data.append(HV)
        HV = 0
        for i in range(1, len(NSGAII_1TotalDelay)):
            HV += (r_TotalDelay - NSGAII_1TotalDelay[i]) * (NSGAII_1Utilization[i] - NSGAII_1Utilization[i-1])
        print('HV(NSGAII_1) = \t%-f, ' % HV)
        data.append(HV)
        HV = 0
        for i in range(1, len(NSGAII_2TotalDelay)):
            HV += (r_TotalDelay - NSGAII_2TotalDelay[i]) * (NSGAII_2Utilization[i] - NSGAII_2Utilization[i-1])
        print('HV(NSGAII_2) = \t%-f, ' % HV)
        data.append(HV)
        HV = 0
        for i in range(1, len(NSGAII_3TotalDelay)):
            HV += (r_TotalDelay - NSGAII_3TotalDelay[i]) * (NSGAII_3Utilization[i] - NSGAII_3Utilization[i-1])
        print('HV(NSGAII_3) =\t%-f. ' % HV)
        data.append(HV)
        HV = 0
        for i in range(1, len(NSGAII_4TotalDelay)):
            HV += (r_TotalDelay - NSGAII_4TotalDelay[i]) * (NSGAII_4Utilization[i] - NSGAII_4Utilization[i-1])
        print('HV(NSGAII_4) =\t%-f. ' % HV)
        data.append(HV)

        # %% 数据写入 CSV
        with open(CSVpath, 'a', newline='') as f:
            writer = csv.writer(f)
            writer.writerow(data)

    plt.show()

        # %%
