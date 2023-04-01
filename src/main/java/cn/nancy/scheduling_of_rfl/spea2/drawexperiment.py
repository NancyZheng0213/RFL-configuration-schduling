# %%
from matplotlib import pyplot as plt
from matplotlib import ticker as tic
import pandas as pd

""" 读取实验结果数据为dataframe，绘制多子图的箱线图"""
# %%     objectiveList = ['totaldelay', 'utilization', 'AVGtotaldelay', 'AVGutilization']
def readtable(design, result, objectiveList):
    xlsx = pd.ExcelFile('E:\JavaProjects\graduation\sourse\experiment.xlsx')
    df1 = pd.read_excel(xlsx, sheet_name=design, index_col=0, )
    levelmap = {}
    for index in df1.index: # 读取参数名和对应的水平值
        levelmap[index] = df1.loc[index].values
    df2 = pd.read_excel(xlsx, sheet_name=result)
    boxdata = {}
    for objective in objectiveList:
        datamap = {}
        for index in df1.index:
            data = []
            for levelvalue in levelmap[index]:
                if levelvalue != -1:
                    data.append(df2[df2[index]==levelvalue][objective])
            datamap[index] = data
        boxdata[objective] = datamap

    return boxdata, levelmap


if __name__ == '__main__':
    # %% 读取数据
    # plt.rcParams['font.sans-serif'] = ['SimHei'] #显示中文
    objectiveList = ['totaldelay', 'utilization', 'AVGtotaldelay', 'AVGutilization']
    xlabellist = ['population \nsize', 'archive \nsize', 'iteration \ntime', 'tournament \nparameter']
    ylabellist = ['MIN Total \nDelay/min', 'MAX Machine \nUtilization/%', 'AVG total \ndelay/min', 'AVG machine \nutilization/%']
    boxdata, levelmap = readtable('design', 'result', objectiveList)
    levellist = list(levelmap.keys())
    fig, axs = plt.subplots(len(objectiveList), len(levelmap), sharey='row', sharex='col', figsize=(6,6), constrained_layout=True)
    for i in range(len(objectiveList)):
        data = boxdata[objectiveList[i]]
        for j in range(len(levelmap)):
            para = levellist[j]
            labellist = levelmap[para][levelmap[para] != -1]
            axs[i,j].boxplot(
                data[para],
                labels=labellist,
                patch_artist = True,
                boxprops={'color':'black','facecolor':'darkgray','lw':0.7},
                flierprops={'marker':'+'},
                medianprops={'color':'black'},
            )
            axs[i,j].tick_params(axis="both", direction="in")
        axs[i,0].set_ylabel(ylabellist[i])
        axs[i,0].ticklabel_format(style='sci',scilimits=(0,2), axis='y', useMathText=True)
    for j in range(len(levelmap)):
        axs[i,j].set_xlabel(xlabellist[j])
    axs[3,2].ticklabel_format(style='sci',scilimits=(0,0), axis='x', useMathText=True)
    # fig.subplots_adjust(left=0.5, hspace=0.7)

    # %% 原方法
    """totaldelay"""
    # N
    axs[0,0].errorbar(
        [30, 50, 65, 80, 100], [39522.7, 25918.7, 32944.8, 22398.2, 23479.0], yerr=[7721.3245, 6686.863, 5980.912, 5980.912, 5980.912]
    )
    axs[0,0].set_ylabel('MIN Total \nDelay/min')
    
    # N0
    axs[0,1].errorbar(
        [10, 20, 30, 50, 60], [25765.360, 25476.360, 29883.520, 34260.750, 24561.267], yerr=[5980.912235, 5980.912235, 5980.912235, 6686.863163, 7721.324494]
    )
    # T
    axs[0,2].errorbar(
        [100, 200, 300, 400, 500], [33756.640, 35955.600, 23850.400, 28943.450, 18514.400], yerr=[5980.912235, 6686.863163, 6686.863163, 6686.863163, 5980.912235]
    )
    # k
    axs[0,3].errorbar(
        [2, 3, 4], [32096.867, 24363.825, 26514.120], yerr=[4457.908775, 4728.326287, 5980.912235]
    )

    """utilization"""
    # N
    axs[1,0].errorbar(
        [30, 50, 65, 80, 100], [71.061, 73.592, 74.087, 73.659, 74.238], yerr=[1.8965, 1.6425, 1.469, 1.469, 1.4695]
    )
    axs[1,0].set_ylabel('MAX Machine \nUtilization/%')
    # N0
    axs[1,1].errorbar(
        [10, 20, 30, 50, 60], [68.828, 71.305, 74.202, 77.825, 78.165], yerr=[1.46910383,1.46910383,1.46910383,1.642508015,1.896604889]
    )
    # T
    axs[1,2].errorbar(
        [100, 200, 300, 400, 500], [74.016, 75.613, 69.266, 74.634, 73.868], yerr=[1.46910383,1.642508015,1.642508015,1.642508015,1.46910383]
    )
    # k
    axs[1,3].errorbar(
        [2, 3, 4], [73.256, 73.391, 74.206], yerr=[1.095005343,1.161428555,1.46910383]
    )

    """AVGtotaldelay"""
    # N
    axs[2,0].errorbar(
        [30, 50, 65, 80, 100], [59520.166, 52159.858, 52708.172, 40639.389, 40816.029], yerr=[10156.46,8795.7525,7867.16,7867.1605,7867.1605]
    )
    axs[2,0].set_ylabel('AVG total \ndelay/min')
    # N0
    axs[2,1].errorbar(
        [10, 20, 30, 50, 60], [28147.213, 33495.456, 59823.734, 69164.124, 58009.790], yerr=[7867.160203,7867.160203,7867.160203,8795.752502,10156.46015]
    )
    # T
    axs[2,2].errorbar(
        [100, 200, 300, 400, 500], [62023.997, 75791.542, 29060.834, 44497.626, 30099.579], yerr=[7867.160203,8795.752502,8795.752502,8795.752502,7867.160203]
    )
    # k
    axs[2,3].errorbar(
        [2, 3, 4], [49109.435, 45018.550, 51176.913], yerr=[5863.835001,6219.53624,7867.160203]
    )
    
    """AVGutilization"""
    # N
    axs[3,0].errorbar(
        [30, 50, 65, 80, 100], [68.855, 69.325, 71.154, 70.522, 70.856], yerr=[1.8125,1.5695,1.404,1.4035,1.404]
    )
    axs[3,0].set_ylabel('AVG machine \nutilization/%')
    axs[3,0].set_xticks([30, 50, 65, 80, 100])
    # N0
    axs[3,1].errorbar(
        [10, 20, 30, 50, 60], [68.314, 69.115, 70.305, 71.733, 73.640], yerr=[1.403786957,1.403786957,1.403786957,1.569481531,1.812281168]
    )
    axs[3,1].set_xticks([10, 20, 30, 50, 60])
    # T
    axs[3,2].errorbar(
        [100, 200, 300, 400, 500], [69.351, 71.490, 67.793, 71.441, 71.375], yerr=[1.403786957,1.569481531,1.569481531,1.569481531,1.403786957]
    )
    axs[3,2].set_xticks([100, 200, 300, 400, 500])
    # k
    axs[3,3].errorbar(
        [2, 3, 4], [70.103, 70.040, 71.056], yerr=[1.04632102,1.109791033,1.403786957]
    )
    axs[3,3].set_xticks([2, 3, 4])
    axs[3,0].set_xlabel('population \nsize')
    axs[3,1].set_xlabel('archive \nsize')
    axs[3,2].set_xlabel('iteration \ntime')
    axs[3,3].set_xlabel('tournament \nparameter')

    for i in range(4):
        axs[i,0].ticklabel_format(style='sci',scilimits=(0,2), axis='y', useMathText=True)
        for j in range(4):
            axs[i,j].tick_params(axis="both", direction="in")
    axs[3,2].ticklabel_format(style='sci',scilimits=(0,0), axis='x', useMathText=True)
    # fig.subplots_adjust(left=0.5, hspace=0.7)
    
    plt.savefig("experiment.png", format='png')

    plt.show()