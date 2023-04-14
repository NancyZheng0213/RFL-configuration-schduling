# -*- coding: utf-8 -*-

# %%
import datetime
from matplotlib import pyplot as plt
from matplotlib.patches import ConnectionPatch
import pandas as pd
import plotly.express as px
import json
import time

""" 读取支配解的甘特图数据
"""

def readtimetable(timetable):

    """记录编码"""
    code = timetable.get("code")
    ConfigurationCode = code.get("ConfigurationCode")
    OperationCode = code.get("OperationCode")
    SortCode = code.get("SortCode")
    """记录解码"""
    decode = timetable.get("decode")
    StartTime = decode.get("StartTime")
    FinishTime = decode.get("FinishTime")
    ProcessingTime = decode.get("ProcessingTime")
    SetupStartTime = decode.get("SetupStartTime")
    SetupFinishTime = decode.get("SetupFinishTime")
    
    """记录结果指标"""
    Makespan = decode.get("Makespan")
    TotalDelay = decode.get("TotalDelay")
    Utilization = decode.get("Utilization")
    R = timetable.get("R")

    return StartTime, FinishTime, ProcessingTime, SetupStartTime, SetupFinishTime, ConfigurationCode, SortCode, TotalDelay, Utilization

""" 读取所有外部存档的目标值
"""

def readScatterData(path):

    TotalDelay = []
    Utilization = []

    with open(path) as f:
        for line in f.readlines():
            datas = json.loads(line)
            for data in datas:
                TotalDelay.append(datas[data][0])
                Utilization.append(datas[data][1])
    # for map in data:
    #     TotalDelay.append(data[map][0])
    #     Utilization.append(data[map][1])

    return TotalDelay, Utilization

"""甘特图绘制，输入：
时间表：[{OP1:[T1,T2,T3...], OP2:[T4,T5...]},{...}]
解：{"ConfigurationCode":[1,3,5,2,4,7,6],"OperationCode":{"1":{"1":1,"2":2,"5":4,"6":5,"7":6,"8":6,"9":6},"2":{"1":1,"2":2,"3":2,"5":4,"6":5,"7":6,"8":6,"9":6},"3":{"2":5,"7":7,"8":7,"9":7},"4":{"2":2,"3":2,"4":4,"7":6,"8":6,"9":6},"5":{"2":3,"9":6}},"SortCode":[3,1,4,5,2]}
title：甘特图名
"""
def DrawGantt(S_table, F_table, Setup_S, Setup_F, ConfigurationCode, SortCode, title):

    struct_time = (2022, 4, 1, 8, 0, 0, 1, 152, 0)    # 2022.04.01 8:00:00开始
    start_time = time.mktime(struct_time)

    task = []                               # 记录工件种类
    start = []                              # 记录工件开始时间
    finish = []                             # 记录工件结束时间
    Resource = []                           # 记录工件所在机器
    text = []                               # 记录注释

    PartName = ["001-00536","02JH585","100-02653","KF17A38386","KF17A38396","KF17A38988","KF17A38989","KF17A40120","100-04729-G","100-04731-G","100-04738-G","100-04739-G","100-04740-G","100-04743-G","100-04753-G","100-04770-G","108-00855-G","100-04583-G","100-04585","100-04587","100-04588","100-04590","100-04591","100-04592","100-04593","100-04594","100-04595","100-04630","100-04830","WB00062-00","WB56038-00","WB80120-00","WB80121-00"]

    for machineindex in range(len(ConfigurationCode)):  # 机器序号
        machine = ConfigurationCode[machineindex]
        for OP in S_table[machineindex]:                # 该机器上的操作
            for partindex in range(len(SortCode)):      # 工件序号
                Resource.append("RMT" + str(machine) + "-OP" + str(OP))
                # duration.append(Processing[machineindex].get(OP).get(partindex))
                task.append('P' + str(SortCode[partindex]))
                text.append(str(SortCode[partindex]))
                # task.append(PartName[partindex])
                start.append(datetime.datetime.fromtimestamp(S_table[machineindex].get(OP)[partindex]*60 + start_time).isoformat())
                finish.append(datetime.datetime.fromtimestamp(F_table[machineindex].get(OP)[partindex]*60 + start_time).isoformat())
    for machineindex in range(len(ConfigurationCode)):                  # 机器序号
        machine = ConfigurationCode[machineindex]
        for OP in Setup_S[machineindex]:                            # 该机器上的操作
            for index in range(len(Setup_S[machineindex].get(OP))): # 工件序号
                Resource.append("RMT" + str(machine) + "-OP" + str(OP))
                task.append('换模')
                text.append('')
                start.append(datetime.datetime.fromtimestamp(Setup_S[machineindex].get(OP)[index]*60 + start_time).isoformat())
                finish.append(datetime.datetime.fromtimestamp(Setup_F[machineindex].get(OP)[index]*60 + start_time).isoformat())

    df = pd.DataFrame(data=Resource, columns=['Machine'])
    df['Start'] = start
    df['Finish'] = finish
    df['Task'] = task
    df['Text'] = text

    # print(df)

    return px.timeline(
        data_frame=df,
        x_start="Start",
        x_end="Finish",
        y="Machine",
        color="Task",
        text="Text",
        title=str(title),  
        # color_discrete_sequence=["white"], # 选择颜色
        color_discrete_sequence=[
            "#E57373","#F06292","#BA68C8","#9575CD","#7986CB","#64B5F6","#4FC3F7","#4DD0E1","#4DB6AC","#81C784","#AED581",
            "#DCE775","#FFF176","#FFD54F","#FFB74D","#FF8A65","#A1887F","#B71C1C","#880E4F","#4A148C","#311B92","#1A237E",
            "#0D47A1","#01579B","#006064","#004D40","#1B5E20","#33691E","#827717","#F57F17","#FF6F00","#E65100","#BF360C",
            "#90A4AE",
        ],
        color_discrete_map={"换模":"black"},
        # category_orders={"Task": ["D1", "D2", "D3", "D4"]}, # 标签排序
    )


if __name__ == '__main__':

    """"""
    # %% 画甘特图
    with open("E:\\JavaProjects\\scheduling_of_rfl\\result\\00_case2_time2\\NSGAII\\1.txt", "r") as f:
        index = 1
        for line in f:
            timetable = json.loads(line)
            S_table, F_table, Processing, Setup_S, Setup_F, ConfigurationCode, SortCode, TotalDelay, Utilization = readtimetable(timetable)
            fig = DrawGantt(
                S_table, F_table, Setup_S, Setup_F, ConfigurationCode, SortCode,
                ("The delay time is %d minutes and the utilization is %.4f%%" % (TotalDelay, Utilization))     # 甘特图标题
            )
            print('%d:\t %d minutes, %.4f%%' % (index, TotalDelay, Utilization))
            index += 1
            fig.update_traces(textposition='inside')
            fig.update_yaxes(autorange="reversed")
            fig.update_layout(legend=dict(
                orientation="h",    # 开启水平显示
            ))
            fig.show()

    # %% 画散点图
    # p = "E:\\JavaProjects\\zhengnanxi\\scheduling_of_rfl\\result\\case1_time1\\NSGAII"
    # allTotalDelay, allUtilization = readScatterData(p + "\\archiveObj.txt")
    # TotalDelay, Utilization = readScatterData(p + "\\ParetoFront.txt")
    # # with open(p + "\\archiveObj.txt") as f:
    # #     lines = f.readlines()
    # #     line = lines[0]
    # #     scatterData = json.loads(line)
    # #     allTotalDelay, allUtilization = readScatterData(scatterData)
    # # with open(p + "\\ParetoFront.txt") as f:
    # #     lines = f.readlines()
    # #     line = lines[0]
    # #     scatterData = json.loads(line)
    # #     TotalDelay, Utilization = readScatterData(scatterData)
    # fig = plt.figure(1, figsize=(4,3.8))
    # plt.rcParams['font.sans-serif'] = ['SimHei'] #显示中文
    # plt.scatter(allTotalDelay, allUtilization, s=15, label='种群解')
    # plt.scatter(TotalDelay, Utilization, color='C1', s=20, label='Pareto解')
    # plt.xlabel("总拖期时间/min", fontdict={'size': 12}, fontproperties='SimHei')
    # plt.ylabel("平均设备利用率/%", fontdict={'size': 12}, fontproperties='SimHei')
    # plt.title("算法解散点图", fontdict={'size': 14}, fontproperties='SimHei')
    # plt.legend(loc='best')
    # plt.grid()
    # plt.savefig(p + "\\解散点图.png", format='png')


    # # %% 画全局解散点图和pareto散点图
    # allTotalDelay, allUtilization = readScatterData(p + "\\archiveObj.txt")
    # TotalDelay, Utilization = readScatterData(p + "\\ParetoFront.txt")
    # # with open(p + "\\archiveObj.txt") as f:
    # #     lines = f.readlines()
    # #     line = lines[0]
    # #     scatterData = json.loads(line)
    # #     allTotalDelay, allUtilization = readScatterData(scatterData)
    # # with open(p + "\\ParetoFront.txt") as f:
    # #     lines = f.readlines()
    # #     line = lines[0]
    # #     scatterData = json.loads(line)
    # #     TotalDelay, Utilization = readScatterData(scatterData)
    # fig = plt.figure(constrained_layout=True, figsize=(8,5))
    # # plt.rcParams['font.sans-serif'] = ['SimHei'] #显示中文
    # gs = fig.add_gridspec(2, 3)
    # # 全局解
    # ax1 = fig.add_subplot(gs[0:,0:2])
    # ax1.scatter(allTotalDelay, allUtilization, s=30, color='C0', label='population')
    # ax1.scatter(TotalDelay, Utilization, color='C1', s=20, label='Pareto solution')
    # ax1.tick_params(labelsize=14)
    # # ax1.set_xlabel("总拖期时间/min", fontdict={'size': 12}, fontproperties='SimHei')
    # # ax1.set_ylabel("平均设备利用率/%", fontdict={'size': 12}, fontproperties='SimHei')
    # ax1.set_xlabel("total delay/min", fontdict={'size': 15})
    # ax1.set_ylabel("average utilization/%", fontdict={'size': 15})
    # ax1.legend(loc=3, fontsize=14, edgecolor='C0', facecolor='0.6', framealpha=0.3, handletextpad=0, labelspacing=0.2, borderpad=0.2)
    # ax1.spines['top'].set_visible(False)
    # ax1.spines['left'].set_visible(False)
    # ax1.spines['bottom'].set_visible(False)
    # ax1.spines['right'].set_visible(False)
    # ax1.grid()
    # # pareto
    # ax2 = fig.add_subplot(gs[1,2])
    # ax2.scatter(TotalDelay, Utilization, color='C1', s=5)
    # ax2.tick_params(labelsize=14)
    # ax2.yaxis.set_ticks_position('right')
    # # ax2.spines['top'].set_visible(False)
    # # ax2.spines['left'].set_visible(False)
    # # ax2.spines['bottom'].set_visible(False)
    # # ax2.spines['right'].set_visible(False)
    # ax2.grid()
    # # text
    # ax3 = fig.add_subplot(gs[0,2])
    # ax3.text(
    #     0.051, 0.6, '%d non-dominated \n(pareto) solutions' % len(TotalDelay), fontsize=15,
    # )
    # ax3.axis(False)
    # ax3.set_position([0.68, 0.55, 0.32, 0.5])
    # con = ConnectionPatch(
    #     xyA=(125000, 79),coordsA=ax1.transData,
    #     xyB=(0.5, 0),coordsB=ax3.transData,
    #     arrowstyle="Fancy, head_length=.6, head_width=.6, tail_width=.8", connectionstyle="arc3, rad=-0.3",
    #     facecolor="C1", edgecolor="C1",
    # )
    # fig.add_artist(con)
    # # plt.show()
    # plt.savefig("Double column scatter chart.png", format='png')


    # # %% 画迭代曲线图
    # utilization = []
    # totaldelay = []
    # toptotaldelay = []
    # toputilization = []
    # bottomtotaldelay = []
    # bottomutilization = []

    # with open(p + "\\uOfA.txt") as f:
    #     for line in f.readlines():
    #         utilization.append(json.loads(line))
    # with open(p + "\\tOfA.txt") as f:
    #     for line in f.readlines():
    #         totaldelay.append(json.loads(line))
    # with open(p + "\\topuOfA.txt") as f:
    #     for line in f.readlines():
    #         toputilization.append(json.loads(line))
    # with open(p + "\\bottomtOfA.txt") as f:
    #     for line in f.readlines():
    #         bottomtotaldelay.append(json.loads(line))
    # with open(p + "\\bottomuOfA.txt") as f:
    #     for line in f.readlines():
    #         bottomutilization.append(json.loads(line))
    # with open(p + "\\toptOfA.txt") as f:
    #     for line in f.readlines():
    #         toptotaldelay.append(json.loads(line))
    # fig = plt.figure(2, figsize=(4,2))
    # # plt.rcParams['font.sans-serif'] = ['SimHei'] #显示中文
    # # 绘制利用率
    # ax1 = fig.add_subplot(111)
    # line1 = ax1.plot(range(0,len(utilization)), utilization, color='C0', linestyle='--',label="average utilization")
    # line2 = ax1.plot(range(0,len(toputilization)), toputilization, color='C0', label="optimum utilization")
    # # line1 = ax1.plot(range(0,len(utilization)), utilization, color='grey', linestyle='--',label="average utilization")
    # # line2 = ax1.plot(range(0,len(toputilization)), toputilization, color='grey', label="optimum utilization")
    # # ax1.fill_between(range(0,len(toputilization)), toputilization, bottomutilization, color='C0', alpha=.3, linewidth=0)
    # ax1.set_ylabel('average utilization/%', fontsize=10)
    # ax1.set_xlabel('iteration', fontsize=10)
    # ax1.tick_params(labelsize=9)
    # # ax1.legend(loc='best', handletextpad=0.2, labelspacing=0.2, borderpad=0.5)
    # # 绘制延期时间
    # ax2 = ax1.twinx()
    # line3 = ax2.plot(range(0,len(totaldelay)), totaldelay, color='C1', linestyle='--', label="average total delay")
    # line4 = ax2.plot(range(0,len(bottomtotaldelay)), bottomtotaldelay, color='C1', label="optimal total delay")
    # # line3 = ax2.plot(range(0,len(totaldelay)), totaldelay, color='black', linestyle='--', label="average total delay")
    # # line4 = ax2.plot(range(0,len(bottomtotaldelay)), bottomtotaldelay, color='black', label="optimal total delay")
    # # ax2.fill_between(range(0,len(toptotaldelay)), toptotaldelay, bottomtotaldelay, color='C1', alpha=.3, linewidth=0)
    # ax2.set_ylabel('total delay/min', fontsize=10)
    # ax2.tick_params(labelsize=9)
    # plt.ticklabel_format(style='sci',scilimits=(0,0), axis='both', useMathText=True)
    # # 合并图例
    # lines = line1 + line2 + line3 + line4
    # labels = [l.get_label() for l in lines]
    # # ax2.legend(loc='best', handletextpad=0.2, labelspacing=0.2, borderpad=0.5)
    # ax1.legend(
    #     lines, labels, ncol=2,
    #     handlelength=1.5, handletextpad=0.2, labelspacing=0.2, borderpad=0.5, columnspacing=0.5, borderaxespad=1,
    #     fontsize=9, 
    #     loc='center', bbox_to_anchor=(0.5, 0.75)
    # )
    # # plt.title('迭代曲线图')
    # plt.savefig(p + "\\迭代曲线图.png", format='png')
    # plt.show()

    

# %%
