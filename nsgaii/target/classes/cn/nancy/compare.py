# -*- coding: utf-8 -*-

# %%
import datetime
from matplotlib import pyplot as plt
from matplotlib.patches import ConnectionPatch
import pandas as pd
import plotly.express as px
import json
import time

def readScatterData(data):
    """ 读取所有外部存档的目标值
    """

    TotalDelay = []
    Utilization = []

    for map in data:
        TotalDelay.append(data[map][0])
        Utilization.append(data[map][1])

    return TotalDelay, Utilization

#%%
"""
前沿面比较图
"""
with open("E:\\JavaProjects\\configuration and scheduling of MRFL\\nsgaii\\sourse\\case3\\ParetoFront.txt") as f:
    lines = f.readlines()
    line = lines[0]
    scatterData = json.loads(line)
    NSGAIITotalDelay, NSGAIIUtilization = readScatterData(scatterData)
with open("E:\\JavaProjects\\configuration and scheduling of MRFL\\SPEA2\\sourse\\ParetoFront.txt") as f:
    lines = f.readlines()
    line = lines[0]
    scatterData = json.loads(line)
    SPEA2TotalDelay, SPEA2Utilization = readScatterData(scatterData)
fig = plt.figure(1, figsize=(5,5))
plt.rcParams['font.sans-serif'] = ['SimHei'] #显示中文
plt.scatter(NSGAIITotalDelay, NSGAIIUtilization, marker='^', s=15, label='NSGAII')
plt.scatter(SPEA2TotalDelay, SPEA2Utilization, color='C1', s=20, label='SPEA2')
plt.xlabel("总拖期时间/min", fontdict={'size': 12}, fontproperties='SimHei')
plt.ylabel("平均设备利用率/%", fontdict={'size': 12}, fontproperties='SimHei')
plt.title("算法最优解比较图", fontdict={'size': 14}, fontproperties='SimHei')
plt.legend(loc='best')
plt.grid()
plt.savefig("E:\\JavaProjects\\configuration and scheduling of MRFL\\算法最优解比较图.png", format='png')
"""
前沿面C-metric
"""
c = 0
for i in range(len(SPEA2Utilization)):
    for j in range(len(NSGAIITotalDelay)):
        if NSGAIIUtilization[j] >= SPEA2Utilization[i] and NSGAIITotalDelay[j] < SPEA2TotalDelay[i] or NSGAIIUtilization[j] > SPEA2Utilization[i] and NSGAIITotalDelay[j] == SPEA2TotalDelay[i]:
            c+=1
            break
c = c/len(SPEA2Utilization)
print('C-metric for SPEA2: %f' % c)
for i in range(len(NSGAIITotalDelay)):
    for j in range(len(SPEA2TotalDelay)):
        if NSGAIIUtilization[i] <= SPEA2Utilization[j] and NSGAIITotalDelay[i] > SPEA2TotalDelay[j] or NSGAIIUtilization[i] < SPEA2Utilization[j] and NSGAIITotalDelay[i] == SPEA2TotalDelay[j]:
            c+=1
            break
c = c/len(NSGAIITotalDelay)
print('C-metric for NSGAII: %f' % c)

# %%
