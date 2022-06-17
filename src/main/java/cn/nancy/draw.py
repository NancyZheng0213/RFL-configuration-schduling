# -*- coding: utf-8 -*-

from cProfile import label
import datetime
import re
from turtle import color
from matplotlib import legend, pyplot as plt
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
    print(R)

    return StartTime, FinishTime, ProcessingTime, SetupStartTime, SetupFinishTime, ConfigurationCode, SortCode, TotalDelay, Utilization

""" 读取所有外部存档的目标值
"""
def readScatterData(data):

    TotalDelay = []
    Utilization = []

    for map in data:
        TotalDelay.append(data[map][0])
        Utilization.append(data[map][1])

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

    PartName = ["001-00536","02JH585","100-02653","KF17A38386","KF17A38396","KF17A38988","KF17A38989","KF17A40120","100-04729-G","100-04731-G","100-04738-G","100-04739-G","100-04740-G","100-04743-G","100-04753-G","100-04770-G","108-00855-G","100-04583-G","100-04585","100-04587","100-04588","100-04590","100-04591","100-04592","100-04593","100-04594","100-04595","100-04630","100-04830","WB00062-00","WB56038-00","WB80120-00","WB80121-00"]

    for machineindex in range(len(ConfigurationCode)):  # 机器序号
        machine = ConfigurationCode[machineindex]
        for OP in S_table[machineindex]:            # 该机器上的操作
            for partindex in range(len(SortCode)):      # 工件序号
                Resource.append("RMT" + str(machine) + "-OP" + str(OP))
                # duration.append(Processing[machineindex].get(OP).get(partindex))
                # task.append('P' + str(SortCode[partindex]))
                task.append(PartName[partindex])
                start.append(datetime.datetime.fromtimestamp(S_table[machineindex].get(OP)[partindex]*60 + start_time).isoformat())
                finish.append(datetime.datetime.fromtimestamp(F_table[machineindex].get(OP)[partindex]*60 + start_time).isoformat())
    for machineindex in range(len(ConfigurationCode)):                  # 机器序号
        machine = ConfigurationCode[machineindex]
        for OP in Setup_S[machineindex]:                            # 该机器上的操作
            for index in range(len(Setup_S[machineindex].get(OP))): # 工件序号
                Resource.append("RMT" + str(machine) + "-OP" + str(OP))
                task.append('换模')
                start.append(datetime.datetime.fromtimestamp(Setup_S[machineindex].get(OP)[index]*60 + start_time).isoformat())
                finish.append(datetime.datetime.fromtimestamp(Setup_F[machineindex].get(OP)[index]*60 + start_time).isoformat())

    df = pd.DataFrame(data=Resource, columns=['Machine'])
    df['Start'] = start
    df['Finish'] = finish
    df['Task'] = task

    print(df)

    return px.timeline(
        data_frame=df,
        x_start="Start",
        x_end="Finish",
        y="Machine",
        color="Task",
        title=str(title),  
        # color_discrete_sequence=px.colors.qualitative.Alphabet, # 选择颜色
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

    """画甘特图"""
    with open("E:\\JavaProjects\\graduation\\sourse\\1.txt", "r") as f:
        for line in f:
            timetable = json.loads(line)
            S_table, F_table, Processing, Setup_S, Setup_F, ConfigurationCode, SortCode, TotalDelay, Utilization = readtimetable(timetable)
            fig = DrawGantt(
                S_table, F_table, Setup_S, Setup_F, ConfigurationCode, SortCode,
                ("延误时间为%d分钟，利用率为%.4f%%" % (TotalDelay, Utilization))     # 甘特图标题
            )
            fig.update_yaxes(autorange="reversed")
            fig.update_layout(legend=dict(
                orientation="h",    # 开启水平显示
            ))
            fig.show()

    # """画散点图"""
    # with open("E:\\JavaProjects\\graduation\\sourse\\popObj.txt") as f:
    #     lines = f.readlines()
    #     line = lines[0]
    #     scatterData = json.loads(line)
    #     allTotalDelay, allUtilization = readScatterData(scatterData)
    # with open("E:\\JavaProjects\\graduation\\sourse\\ParetoFront.txt") as f:
    #     lines = f.readlines()
    #     line = lines[0]
    #     scatterData = json.loads(line)
    #     TotalDelay, Utilization = readScatterData(scatterData)
    # fig = plt.figure(1, figsize=(5,4))
    # plt.rcParams['font.sans-serif'] = ['SimHei'] #显示中文
    # plt.scatter(allTotalDelay, allUtilization, s=15, label='种群解')
    # plt.scatter(TotalDelay, Utilization, color='C1', s=20, label='Pareto解')
    # plt.xlabel("总拖期时间/min", fontdict={'size': 12}, fontproperties='SimHei')
    # plt.ylabel("平均设备利用率/%", fontdict={'size': 12}, fontproperties='SimHei')
    # plt.title("算法解散点图", fontdict={'size': 14}, fontproperties='SimHei')
    # plt.legend(loc='best')
    # plt.grid()
    # plt.savefig("解散点图.png", format='png')

    # """画全局解散点图和pareto散点图"""
    # with open("E:\\JavaProjects\\graduation\\sourse\\popObj.txt") as f:
    #     lines = f.readlines()
    #     line = lines[0]
    #     scatterData = json.loads(line)
    #     allTotalDelay, allUtilization = readScatterData(scatterData)
    # with open("E:\\JavaProjects\\graduation\\sourse\\ParetoFront.txt") as f:
    #     lines = f.readlines()
    #     line = lines[0]
    #     scatterData = json.loads(line)
    #     TotalDelay, Utilization = readScatterData(scatterData)
    # fig = plt.figure(constrained_layout=True, figsize=(8,5))
    # plt.rcParams['font.sans-serif'] = ['SimHei'] #显示中文
    # gs = fig.add_gridspec(2, 3)
    # # 全局解
    # ax1 = fig.add_subplot(gs[0:,0:2])
    # ax1.scatter(allTotalDelay, allUtilization, s=15, label='种群解')
    # ax1.scatter(TotalDelay, Utilization, color='C1', s=20, label='Pareto解')
    # ax1.set_xlabel("总拖期时间/min", fontdict={'size': 12}, fontproperties='SimHei')
    # ax1.set_ylabel("平均设备利用率/%", fontdict={'size': 12}, fontproperties='SimHei')
    # ax1.legend(loc='best')
    # ax1.spines['top'].set_visible(False)
    # ax1.spines['left'].set_visible(False)
    # ax1.spines['bottom'].set_visible(False)
    # ax1.spines['right'].set_visible(False)
    # ax1.grid()
    # # pareto
    # ax2 = fig.add_subplot(gs[1,2])
    # ax2.scatter(TotalDelay, Utilization, color='C1', s=5)
    # ax2.yaxis.set_ticks_position('right')
    # # ax2.spines['top'].set_visible(False)
    # # ax2.spines['left'].set_visible(False)
    # # ax2.spines['bottom'].set_visible(False)
    # # ax2.spines['right'].set_visible(False)
    # ax2.grid()
    # plt.savefig("双栏散点图.png", format='png')


    # """画迭代曲线图"""
    # with open("E:\\JavaProjects\\graduation\\sourse\\uOfM.txt") as f:
    #     lines = f.readlines()
    #     line = lines[0]
    #     utilization = json.loads(line)
    # with open("E:\\JavaProjects\\graduation\\sourse\\tOfM.txt") as f:
    #     lines = f.readlines()
    #     line = lines[0]
    #     totaldelay = json.loads(line)
    # with open("E:\\JavaProjects\\graduation\\sourse\\topuOfA.txt") as f:
    #     lines = f.readlines()
    #     line = lines[0]
    #     toputilization = json.loads(line)
    # with open("E:\\JavaProjects\\graduation\\sourse\\bottomtOfA.txt") as f:
    #     lines = f.readlines()
    #     line = lines[0]
    #     bottomtotaldelay = json.loads(line)
    # with open("E:\\JavaProjects\\graduation\\sourse\\bottomuOfM.txt") as f:
    #     lines = f.readlines()
    #     line = lines[0]
    #     bottomutilization = json.loads(line)
    # with open("E:\\JavaProjects\\graduation\\sourse\\toptOfM.txt") as f:
    #     lines = f.readlines()
    #     line = lines[0]
    #     toptotaldelay = json.loads(line)
    # fig = plt.figure(2, figsize=(8,5))
    # plt.rcParams['font.sans-serif'] = ['SimHei'] #显示中文
    # # 绘制利用率
    # ax1 = fig.add_subplot(111)
    # # line1 = ax1.plot(range(0,300), utilization[0:300], color='C0', linestyle='--',label="平均设备利用率")
    # # line2 = ax1.plot(range(0,300), toputilization[0:300], color='C0', label="最优设备利用率")
    # line1 = ax1.plot(range(0,len(utilization)), utilization, color='C0', linestyle='--',label="平均设备利用率")
    # line2 = ax1.plot(range(0,len(toputilization)), toputilization, color='C0', label="最优设备利用率")
    # # ax1.fill_between(range(0,len(toputilization)), toputilization, bottomutilization, color='C0', alpha=.3, linewidth=0)
    # ax1.set_ylabel('平均设备利用率')
    # ax1.set_xlabel('迭代次数')
    # # 绘制延期时间
    # ax2 = ax1.twinx()
    # # line3 = ax2.plot(range(0,300), totaldelay[0:300], color='C1', linestyle='--', label="平均总延期时间")
    # # line4 = ax2.plot(range(0,300), bottomtotaldelay[0:300], color='C1', label="最优总延期时间")
    # line3 = ax2.plot(range(0,len(totaldelay)), totaldelay, color='C1', linestyle='--', label="平均总延期时间")
    # line4 = ax2.plot(range(0,len(bottomtotaldelay)), bottomtotaldelay, color='C1', label="最优总延期时间")
    # # ax2.fill_between(range(0,len(toptotaldelay)), toptotaldelay, bottomtotaldelay, color='C1', alpha=.3, linewidth=0)
    # ax2.set_ylabel('总延期时间/min')
    # # 合并图例
    # lines = line1 + line2 + line3 + line4
    # labels = [l.get_label() for l in lines]
    # # ax1.legend(lines, labels, loc=7)
    # plt.title('迭代曲线图')
    # # plt.savefig("迭代曲线图.png", format='png')
    # plt.show()

    # with open("E:\\JavaProjects\\graduation\\sourse\\ParetoFront.txt") as f:
    #     lines = f.readlines()
    #     line = lines[0]
    #     scatterData = json.loads(line)
    #     TotalDelay, Utilization = readScatterData(scatterData)
    # print(TotalDelay, Utilization)
    
