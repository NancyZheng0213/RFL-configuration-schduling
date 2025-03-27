# configuration & scheduling of reconfigurable flow line

核心算法代码位于文件夹src\main\java\cn\nancy\scheduling_of_rfl

## 数据说明

+ case1.xlsx
+ case2.xlsx
+ case3.xlsx
以上三个表格数据是论文中用于算法比较的算例数据，规模分别是：
ID|number of jobs|number of RMTs|number of OPs
1|8|10|12
2|33|10|12
3|60|31|17

+ case.xlsx
论文中案例分析部分的数据，规模是 <28, 10, 12>

+ testcase.xlsx
代码编写过程中测试用数据。

```
├─ result 存放算例数据和结果，包含结果比较分析
│  ├─ 00_case1
│  │  ├─ NSGAII
│  │  │  ├─ archiveObj.txt 记录archive种群
│  │  │  ├─ bottomtOfA.txt 记录迭代过程中archive种群的完工时间下界
│  │  │  ├─ bottomtOfM.txt 记录迭代过程中普通种群的完工时间下界
│  │  │  ├─ bottomuOfA.txt 记录迭代过程中archive种群的利用率下界
│  │  │  ├─ bottomuOfM.txt 记录迭代过程中普通种群的利用率下界
│  │  │  ├─ GantteData.txt 记录甘特图数据
│  │  │  ├─ ParetoFront.txt 记录Pareto前沿面数据
│  │  │  ├─ tOfA.txt 记录迭代过程中archive种群的完工时间均值
│  │  │  ├─ tOfM.txt 记录迭代过程中普通种群的完工时间均值
│  │  │  ├─ toptOfA.txt 记录迭代过程中archive种群的完工时间上界
│  │  │  ├─ toptOfM.txt 记录迭代过程中普通种群的完工时间上界
│  │  │  ├─ topuOfA.txt 记录迭代过程中archive种群的利用率上界
│  │  │  ├─ topuOfM.txt 记录迭代过程中普通种群的利用率上界
│  │  │  └─ uOfA.txt 记录迭代过程中archive种群的利用率均值
│  │  │  └─ uOfM.txt 记录迭代过程中普通种群的利用率均值
│  │  ├─ SPEA2_1
│  │  │  ├─ archiveObj.txt
│  │  │  ├─ bottomtOfA.txt
│  │  │  ├─ bottomtOfM.txt
│  │  │  ├─ ...
│  │  ├─ 算法最优解比较图.jpg
│  │  ├─ 算法最优解比较图.png
│  │  ├─ 算法迭代曲线比较图.jpg
│  │  └─ 算法迭代曲线比较图.png
│  │  ├─ case1.csv
│  │  ├─ case2.csv
│  │  ├─ case3.csv
│  │  └─ Compare.jpg
│  ├─ case1.csv
│  ├─ case1_compare.csv
│  ├─ case2.csv
│  ├─ case2_compare.csv
│  ├─ ...
├─ src
│  ├─ main
│  │  └─ java
│  │     └─ cn
│  │        └─ nancy
│  │           └─ scheduling_of_rfl
│  │              ├─ .ipynb_checkpoints
│  │              │  └─ Untitled-checkpoint.ipynb
│  │              ├─ Algorithem.java 【公用】moea、spea2、nsga-ii共用算法模块
│  │              ├─ Code.java 【公用】算法种群code类
│  │              ├─ compare.py 用于对比算法结果并输出图表
│  │              ├─ DataStore.java 用于数据读取和存储
│  │              ├─ Decode.java 【公用】算法解码类
│  │              ├─ Encode.java 【公用】算法编码类
│  │              ├─ EncodingConf.java 【公用】配置问题编码类
│  │              ├─ EncodingOP.java 【公用】操作分配问题编码类
│  │              ├─ EncodingSort.java 【公用】排序问题编码类
│  │              ├─ Experiment.java 从此处调用不同算例不同算法的实验接口
│  │              ├─ Individual.java 【公用】种群个体类
│  │              ├─ MOEA
│  │              │  ├─ AllInMOEA.java
│  │              │  ├─ EncodeofMOEA.java
│  │              │  ├─ EncodingConfofMOEA.java
│  │              │  ├─ EncodingOPofMOEA.java
│  │              │  ├─ IndividualofMOEA.java
│  │              │  ├─ MOEA.java
│  │              │  └─ PopofMOEA.java
│  │              ├─ multiCompare.py 用于绘制算法比较图
│  │              ├─ multiCompare2.py 用于绘制算法比较图
│  │              ├─ nsgaii
│  │              │  ├─ AllInNSGAII.java
│  │              │  ├─ draw.py
│  │              │  ├─ EncodeofNSGAII.java
│  │              │  ├─ EncodingConfofNSGAII.java
│  │              │  ├─ EncodingOPofNSGAII.java
│  │              │  ├─ IndividualofNSGAII.java
│  │              │  ├─ NSGAII.java
│  │              │  └─ PopofNSGAII.java
│  │              ├─ Pop.java 【公用】种群类
│  │              ├─ Qus.java 【公用】用于问题读取、数据转化、数据导入
│  │              ├─ spea2
│  │              │  ├─ AllInSPEA2.java
│  │              │  ├─ draft.xlsx
│  │              │  ├─ draw.py
│  │              │  ├─ drawexperiment.py
│  │              │  ├─ EncodeofSPEA2.java
│  │              │  ├─ EncodingConfofSPEA2.java
│  │              │  ├─ EncodingOPofSPEA2.java
│  │              │  ├─ experiment.png
│  │              │  ├─ experiment.xlsx
│  │              │  ├─ Experiment_of_SPEA2.java
│  │              │  ├─ IndividualofSPEA2.java
│  │              │  ├─ PopofSPEA2.java
│  │              │  └─ SPEA2.java
│  │              ├─ TimeOutTask.java
│  └─ test
└─ target
```
