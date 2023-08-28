# scheduling of rfl

为了对比多个算法框架，本项目将所有算法相同的代码块放入超类 ```Algorithem``` 中，对比的算法作为子类继承 ```Algorithem```。目录结构如下：

1. 超类  
├─ Algorithem.java
├─ Code.java
├─ compare.py
├─ DataStore.java
├─ Decode.java
├─ Encode.java
├─ EncodingConf.java
├─ EncodingOP.java
├─ EncodingSort.java
├─ Experiment.java
├─ Individual.java
├─ MOEA
│  ├─ AllInMOEA.java
│  ├─ EncodeofMOEA.java
│  ├─ EncodingConfofMOEA.java
│  ├─ EncodingOPofMOEA.java
│  ├─ IndividualofMOEA.java
│  ├─ MOEA.java
│  └─ PopofMOEA.java
├─ multiCompare.py
├─ multiCompare2.py
├─ nsgaii
│  ├─ AllInNSGAII.java
│  ├─ draw.py
│  ├─ EncodeofNSGAII.java
│  ├─ EncodingConfofNSGAII.java
│  ├─ EncodingOPofNSGAII.java
│  ├─ IndividualofNSGAII.java
│  ├─ NSGAII.java
│  └─ PopofNSGAII.java
├─ Pop.java
├─ Qus.java
├─ spea2
│  ├─ AllInSPEA2.java
│  ├─ draft.xlsx
│  ├─ draw.py
│  ├─ drawexperiment.py
│  ├─ EncodeofSPEA2.java
│  ├─ EncodingConfofSPEA2.java
│  ├─ EncodingOPofSPEA2.java
│  ├─ experiment.png
│  ├─ experiment.xlsx
│  ├─ Experiment_of_SPEA2.java
│  ├─ IndividualofSPEA2.java
│  ├─ PopofSPEA2.java
│  └─ SPEA2.java
