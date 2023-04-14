from matplotlib import pyplot as plt
from matplotlib.lines import Line2D
import numpy as np
import json
from mpl_toolkits.axes_grid1 import Size, Divider

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
    caselist = [3,2,1]
    casenum = len(caselist)
    algorithemlist = ["NSGAII", "SPEA2_2"]
    legendlist = ["NSGAII", "SPEA2"]
    titlelist = ["Total Delay", "Utilization"]
    colorlist = ['C0', 'C1']
    markerlist = ['x', '+', '1', 4]
    linelist = ['-', '--']
    ylabellist = ['delay/min', 'utilization/%']
    legend_elements = [Line2D(
        [0], [0], color=colorlist[i],
        # marker=markerlist[i],
        linestyle=linelist[i],
        label=legendlist[i]
    ) for i in range(len(colorlist))]
    
    fig = plt.figure(figsize=(7,3*len(caselist)))
    horiz = [
        Size.Scaled(1.5), Size.Fixed(.8), Size.Scaled(1.5), Size.Fixed(.1), Size.Scaled(0.8)
    ]
    vert = [
        Size.Scaled(1.), Size.Scaled(1.), Size.Fixed(1.),
        Size.Scaled(1.), Size.Scaled(1.), Size.Fixed(1.),
        Size.Scaled(1.), Size.Scaled(1.)
    ]
    rect = (0.1, 0.1, 0.8, 0.8)
    div = Divider(fig, rect, horiz, vert, aspect=False)
    plt.rcParams['font.sans-serif'] = ['Arial']
    axs = [[[] for _ in range(3)] for _ in range(2*len(caselist))]
    # for i in range(3):
    #     for j in range(3):
    #         ax1 = fig.add_axes(rect, axes_locator=div.new_locator(nx=2*i, ny=3*j, ny1=3*j+2))
    #         ax1.set_title("nx=%d, ny=%d" % (2*i,3*j))
    # plt.show()
    for caseindex in range(casenum):
        # 前沿面比较
        path = ("C:\\Users\\24449\\Desktop\\00_case%d" % caselist[caseindex])
        NSGAIITotalDelay, NSGAIIUtilization = readParetoData(path + "\\NSGAII\\ParetoFront.txt")
        SPEA2TotalDelay, SPEA2Utilization = readParetoData(path + "\\SPEA2_2\\ParetoFront.txt")
        NSGAIITotalDelay.sort()
        NSGAIIUtilization.sort()
        SPEA2TotalDelay.sort()
        SPEA2Utilization.sort()
        boxdata = [[np.array(NSGAIITotalDelay), np.array(SPEA2TotalDelay)], [NSGAIIUtilization, SPEA2Utilization]]

        axs[2*caseindex][0] = fig.add_axes(rect, axes_locator=div.new_locator(nx=0, ny=(3*caseindex), ny1=(3*caseindex+2)))
        axs[2*caseindex][0].plot(NSGAIITotalDelay, NSGAIIUtilization, marker='^', markersize='6', markerfacecolor='w', ls='-', label='NSGAII')
        axs[2*caseindex][0].plot(SPEA2TotalDelay, SPEA2Utilization, marker='o', markersize='6', markerfacecolor='w', ls='--', label='SPEA2')
        axs[2*caseindex][0].set_xlabel("total delay/min", fontdict={'size': 12}, fontproperties='Arial')
        axs[2*caseindex][0].set_ylabel("average utilization/%", fontdict={'size': 12}, fontproperties='Arial')
        axs[2*caseindex][0].legend(loc='best', fontsize=9)
        plt.grid()
       

        # 迭代曲线比较
        axs[2*caseindex][1] = fig.add_axes(rect, axes_locator=div.new_locator(nx=2, ny=3*caseindex+1))
        axs[2*caseindex+1][1] = fig.add_axes(rect, axes_locator=div.new_locator(nx=2, ny=3*caseindex))
        axs[2*caseindex+1][1].set_title(
            "(%c) Pareto front (left), convergence plots (middle), and box-plots (right) for instance %d" % (chr(96+(casenum-caseindex)),caselist[caseindex]),
            x=-0.05, y=-0.8, 
            fontdict={'size': 10, 'weight':'bold'},
        )
        for i in range(len(algorithemlist)):
            _, _, toputilization, _, _, bottomtotaldelay = readLineData(path + "\\" + algorithemlist[i])
            axs[2*caseindex][1].plot(range(len(bottomtotaldelay)), bottomtotaldelay, color=colorlist[i], linestyle=linelist[i], label=legendlist[i])
            # axs[2*caseindex][1].scatter(range(0,len(bottomtotaldelay),10), bottomtotaldelay[::10], color=colorlist[i], marker=markerlist[i])
            plt.setp(axs[2*caseindex][1].get_xticklabels(), visible=False)
            axs[2*caseindex+1][1].plot(range(len(toputilization)), toputilization, color=colorlist[i], linestyle=linelist[i], label=legendlist[i])
            # axs[2*caseindex+1][1].scatter(range(0,len(toputilization),10), toputilization[::10], color=colorlist[i], marker=markerlist[i])
        for i in range(2):
            axs[2*caseindex+i][1].set_ylabel(ylabellist[i], fontsize=10)
            axs[2*caseindex+i][1].legend(handles=legend_elements, fontsize=9)
        axs[2*caseindex+1][1].set_xlabel("iterations", fontsize=10)


        # 箱线图比较
        axs[2*caseindex][2] = fig.add_axes(rect, axes_locator=div.new_locator(nx=4, ny=3*caseindex+1), sharey=axs[2*caseindex][1])
        plt.setp(axs[2*caseindex][2].get_xticklabels(), visible=False)
        plt.setp(axs[2*caseindex][2].get_yticklabels(), visible=False)
        axs[2*caseindex+1][2] = fig.add_axes(rect, axes_locator=div.new_locator(nx=4, ny=3*caseindex), sharey=axs[2*caseindex+1][1])
        plt.setp(axs[2*caseindex+1][2].get_yticklabels(), visible=False)
        for i in range(2):
            # conf_intervalslist = [(5000,5200) for j in range(len(boxdata[i]))]
            bplot = axs[2*caseindex+i][2].boxplot(
                boxdata[i],
                # notch=True,
                widths=0.4,
                patch_artist=True,
                boxprops=dict(facecolor='C1'),
                # conf_intervals=(conf_intervalslist),
                bootstrap=10000,
                medianprops=dict(color='black')
            )
            axs[2*caseindex+i][2].set_xticklabels(legendlist, rotation=45, fontsize=9)
            for patch, color in zip(bplot['boxes'], colorlist):
                patch.set_facecolor(color)

        axs[2*caseindex][0].ticklabel_format(style='sci', scilimits=(0,0), axis='y', useMathText=True)
        axs[2*caseindex][1].ticklabel_format(style='sci', scilimits=(0,0), axis='y', useMathText=True)
        axs[2*caseindex][2].ticklabel_format(style='sci', scilimits=(0,0), axis='y', useMathText=True)
        
    plt.savefig("C:\\Users\\24449\\Desktop\\Compare.jpg", format='jpg', dpi=300)
    plt.show()
    