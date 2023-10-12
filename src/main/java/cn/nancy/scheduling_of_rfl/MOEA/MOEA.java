package cn.nancy.scheduling_of_rfl.MOEA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cn.nancy.scheduling_of_rfl.Algorithm;
import cn.nancy.scheduling_of_rfl.Decode;
import cn.nancy.scheduling_of_rfl.Individual;
import cn.nancy.scheduling_of_rfl.Pop;
import cn.nancy.scheduling_of_rfl.Qus;

/**
 * MOEA/D 算法，采用切比雪夫分解
 */
public class MOEA extends Algorithm {
    enum OBJECTIVE {
        MAX, MIN
    };

    /**
     * 权重参数
     * <p>
     * 对双目标问题而言，种群规模=H+1
     * </p>
     * <p>
     * e.g.: H = 3, N = 2, then all the cases of the weight vector
     * are {[0,1], [1,0], [1/3,2/3], [2/3,1/3]}
     * </p>
     */
    int H;
    /**
     * 目标函数名称以及该目标是追求最大化或最小化
     * <p>
     * e.g.: {"Utilization": OBJECTIVE.MAX}
     * </p>
     */
    HashMap<String, OBJECTIVE> objective;
    /**
     * 权重向量的邻居个数
     */
    int T;
    /**
     * 每个权重向量的 T 个邻居对应的下标
     */
    ArrayList<ArrayList<Integer>> B;
    /**
     * 权重集合
     */
    ArrayList<double[]> weightList;
    /**
     * EP 集合，存储搜索过程中找到的非支配解
     */
    ArrayList<IndividualofMOEA> EP;
    /**
     * FV 集合，对应每个权重向量的目标函数值向量
     */
    ArrayList<double[]> FV;
    /**
     * z*，每个目标分量上的最优值，顺序为 [最大平均利用率,最小总延误时间]
     */
    HashMap<String, Double> Z;

    /**
     * 初始化，种群规模 popsize = H+1
     * @param MaxIteration
     * @param H 权重参数
     * @param T 权重向量的邻居个数
     * @param objective 目标函数名称以及该目标是追求最大化或最小化，默认 {"Utilization": OBJECTIVE.MAX}
     * @param qus
     */
    public MOEA(int MaxIteration, int H, int T, HashMap<String, OBJECTIVE> objective, Qus qus) {
        super(MaxIteration, 2);
        int popsize = H + 1;
        this.H = H;
        this.objective = new HashMap<String, OBJECTIVE>();
        this.objective.put("Utilization", OBJECTIVE.MAX);
        this.objective.put("TotalDelay", OBJECTIVE.MIN);
        this.T = T;
        this.EP = new ArrayList<IndividualofMOEA>();
        // 权重初始均匀分布
        this.weightList = new ArrayList<double[]>(H + 1);
        for (int i = 0; i < popsize; i++) {
            double[] weight = new double[] { i / (double)H, (H - i) / (double)H };
            this.weightList.add(i, weight);
        }
        // 计算任意两个权重向量之间的欧几里得距离
        double[][] distance = new double[popsize][popsize];
        for (int i = 0; i < popsize; i++) {
            for (int j = i + 1; j < popsize; j++) {
                distance[i][j] = WeightDistance(this.weightList.get(i), this.weightList.get(j));
                distance[j][i] = distance[i][j];
            }
        }
        // 记录每个权重最近的 T 个权重向量下标
        this.B = new ArrayList<ArrayList<Integer>>();
        this.B.ensureCapacity(popsize);
        for (int i = 0; i < popsize; i++) {
            ArrayList<Integer> IndexForI = new ArrayList<>(this.T);
            IndexForI.ensureCapacity(this.T);
            for (int index = 0; IndexForI.size() < this.T; index++) {
                if (i != index) {
                    IndexForI.add(index);
                }
            };
            for (int index = 0; index < popsize; index++) {
                if (i != index) {
                    for (int k1 = 0; k1 < this.T; k1++) {
                        if (distance[i][index] < distance[i][IndexForI.get(k1)]) {
                            if (IndexForI.contains(index)) {
                                if (IndexForI.indexOf(index) < k1) {
                                    IndexForI.remove((Integer)index);
                                    IndexForI.add(k1-1, index);
                                } else {
                                    IndexForI.remove((Integer)index);
                                    IndexForI.add(k1, index);
                                }
                            } else {
                                IndexForI.remove(IndexForI.size()-1);
                                IndexForI.add(k1, index);
                            }
                            break;
                        }
                    }
                } else {
                    continue;
                }
            }
            this.B.add(i, IndexForI);
        }
        PopofMOEA pop = new PopofMOEA(popsize);
        pop.encode(qus.getPartsNum(), qus.getMachine(), qus.getProcessNum(), qus.getProcess(), qus.getAlternativeMachine());
        pop.decode(qus.getMachineTime(), qus.getDemand(), qus.getSetupTime(), qus.getDueDays());
        super.setPop(pop);
        // 初始化 FV
        this.FV = new ArrayList<>();
        for (int i = 0; i < popsize; i++) {
            Decode decode = pop.getIndividual(i).getDecode();
            double[] objectives = new double[this.getObjectiveNum()];
            int j = 0;
            Iterator<Map.Entry<String, OBJECTIVE>> iterator = this.objective.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, OBJECTIVE> entry = iterator.next();
                objectives[j] = decode.getObjectiveValue(entry.getKey());
                j++;
            }
            this.FV.add(objectives);
        }
        // 初始化 Z
        this.Z = new HashMap<String, Double>(2);
        for (HashMap.Entry<String, OBJECTIVE> entry : this.objective.entrySet()) {
            if (entry.getValue() == OBJECTIVE.MAX) {
                this.Z.put(entry.getKey(), Double.MIN_VALUE);
            } else {
                this.Z.put(entry.getKey(), Double.MAX_VALUE);
            }
        }
        for (IndividualofMOEA individual : pop.getIndividuals()) {
            Iterator<HashMap.Entry<String, OBJECTIVE>> iterator = this.objective.entrySet().iterator();
            while (iterator.hasNext()) {
                HashMap.Entry<String, OBJECTIVE> entry = iterator.next();
                String ObjectiveName = entry.getKey();
                OBJECTIVE obj = entry.getValue();
                if (obj == OBJECTIVE.MAX) {
                    if (individual.getDecode().getObjectiveValue(ObjectiveName) > this.Z.get(ObjectiveName)) {
                        this.Z.put(ObjectiveName, individual.getDecode().getObjectiveValue(ObjectiveName));
                    }
                } else {
                    if (individual.getDecode().getObjectiveValue(ObjectiveName) < this.Z.get(ObjectiveName)) {
                        this.Z.put(ObjectiveName, individual.getDecode().getObjectiveValue(ObjectiveName));
                    }
                }
            }
        }
    }

    /**
     * 更新
     * @param qus
     */
    public void Update(Qus qus) {
        int popsize = super.getPop().getPopsize();
        PopofMOEA ChildPop = new PopofMOEA(popsize);
        // 对每一个 i， 从 B(i) 中随机选择两个序号 k、l，由 x_k、x_l 产生新的解
        for (int i = 0; i < popsize; i++) {
            ArrayList<Integer> neighbor = new ArrayList<>(this.B.get(i));
            int k = neighbor.get(new Random().nextInt(this.T));
            int l = neighbor.get(new Random().nextInt(this.T));
            IndividualofMOEA child = Variation(super.getPop().getIndividual(k), super.getPop().getIndividual(l), qus);
            ChildPop.setIndividual(i, child);
        }
        ChildPop.decode(qus.getMachineTime(), qus.getDemand(), qus.getSetupTime(), qus.getDueDays());
        // 归一化
        double MAXUtilization = Double.MIN_VALUE;
        double MINUtilization = Double.MAX_VALUE;
        double MAXTotalDelay = Double.MIN_VALUE;
        double MINTotalDelay = Double.MAX_VALUE;
        for (IndividualofMOEA individual : ChildPop.getIndividuals()) {
            Decode IndividualDecode = individual.getDecode();
            if (IndividualDecode.getUtilization() > MAXUtilization) MAXUtilization = IndividualDecode.getUtilization();
            if (IndividualDecode.getUtilization() < MINUtilization) MINUtilization = IndividualDecode.getUtilization();
            if (IndividualDecode.getTotalDelay() > MAXTotalDelay) MAXTotalDelay = IndividualDecode.getTotalDelay();
            if (IndividualDecode.getTotalDelay() < MINTotalDelay) MINTotalDelay = IndividualDecode.getTotalDelay();
        }
        for (IndividualofMOEA individual : (IndividualofMOEA[]) super.getPop().getIndividuals()) {
            Decode IndividualDecode = individual.getDecode();
            if (IndividualDecode.getUtilization() > MAXUtilization) MAXUtilization = IndividualDecode.getUtilization();
            if (IndividualDecode.getUtilization() < MINUtilization) MINUtilization = IndividualDecode.getUtilization();
            if (IndividualDecode.getTotalDelay() > MAXTotalDelay) MAXTotalDelay = IndividualDecode.getTotalDelay();
            if (IndividualDecode.getTotalDelay() < MINTotalDelay) MINTotalDelay = IndividualDecode.getTotalDelay();
        }
        double[] x1 = new double[popsize];    // 父代归一后的利用率
        double[] x2 = new double[popsize];    // 父代归一后的延误时间
        double[] y1 = new double[popsize];    // 子代归一后的利用率
        double[] y2 = new double[popsize];    // 子代归一后的延误时间
        double Max1 = Double.MIN_VALUE;
        double Min2 = Double.MAX_VALUE;
        for (int i = 0; i < popsize; i++) {
            IndividualofMOEA individual = (IndividualofMOEA) super.getPop().getIndividual(i);
            Decode IndividualDecode = individual.getDecode();
            x1[i] = (IndividualDecode.getUtilization() - MINUtilization)/(MAXUtilization - MINUtilization);
            x2[i] = (IndividualDecode.getTotalDelay() - MINTotalDelay)/(MAXTotalDelay - MINTotalDelay);
            if (x1[i] > Max1) Max1 = x1[i];
            if (x2[i] < Min2) Min2 = x2[i];
        }
        for (int i = 0; i < popsize; i++) {
            IndividualofMOEA individual = ChildPop.getIndividual(i);
            Decode IndividualDecode = individual.getDecode();
            y1[i] = (IndividualDecode.getUtilization() - MINUtilization)/(MAXUtilization - MINUtilization);
            y2[i] = (IndividualDecode.getTotalDelay() - MINTotalDelay)/(MAXTotalDelay - MINTotalDelay);
            if (y1[i] > Max1) Max1 = y1[i];
            if (y2[i] < Min2) Min2 = y2[i];
        }
        // 更新 Z
        this.Z.put("Utilization", MAXUtilization);
        this.Z.put("TotalDelay", MINTotalDelay);
        // 计算 gte
        for (int i = 0; i < popsize; i++) {
            IndividualofMOEA individual = ChildPop.getIndividual(i);
            for (int j : this.B.get(i)) {
                double GteX = Math.max(this.weightList.get(j)[0]*Math.abs(x1[j] - Max1), this.weightList.get(j)[1]*Math.abs(x2[j] - Min2));
                double GteY = Math.max(this.weightList.get(j)[0]*Math.abs(y1[i] - Max1), this.weightList.get(j)[1]*Math.abs(y2[i] - Min2));
                if (GteY <= GteX) {
                    super.getPop().setIndividual(j, individual);
                    this.FV.set(j, new double[]{individual.getDecode().getUtilization(), individual.getDecode().getTotalDelay()});
                }
            }
            // 更新 EP，移除被 y 支配的向量；如果无支配 y 的向量，则把 y 加入 EP 中
            ArrayList<IndividualofMOEA> copyEP = new ArrayList<>(this.EP);
            Iterator<IndividualofMOEA> EPiterator = copyEP.iterator();
            Boolean flag = false;       // EP 中是否有支配 y 的向量
            while (EPiterator.hasNext()) {
                IndividualofMOEA EPindividual = EPiterator.next();
                Decode decode = EPindividual.getDecode();
                if (decode.getUtilization() <= individual.getDecode().getUtilization() && decode.getTotalDelay() > individual.getDecode().getTotalDelay() || 
                decode.getUtilization() < individual.getDecode().getUtilization() && decode.getTotalDelay() >= individual.getDecode().getTotalDelay()) {
                    this.EP.remove(EPindividual);
                } else if ((!flag) && decode.getUtilization() >= individual.getDecode().getUtilization() && decode.getTotalDelay() <= individual.getDecode().getTotalDelay()) {
                    flag = true;
                }
            }
            if (! flag) {
                addEP(individual);
            }
        }
    }

    @Override
    public Pop MatingSelection(int MatingPoolSize) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'MatingSelection'");
    }

    /**
     * MOEA/D 交叉变异
     * 
     * @param parent1
     * @param parent2
     * @param qus
     * @return
     */
    public IndividualofMOEA Variation(Individual individual1, Individual individual2, Qus qus) {
        IndividualofMOEA parent1 = new IndividualofMOEA(individual1);
        IndividualofMOEA parent2 = new IndividualofMOEA(individual2);
        IndividualofMOEA child = new IndividualofMOEA(parent1);
        // 排序编码的交叉
        ArrayList<ArrayList<Integer>> sortingList = new ArrayList<>(
                PBX(parent1.getCode().getSortCode(), parent2.getCode().getSortCode()));
        child.getCode().setSortCode(sortingList.get(new Random().nextInt(2)));
        // 排序编码的突变
        child.setCode(SwapMutation(child.getCode()));
        // 对配置编码和操作编码进行变异搜索
        List<Individual> childlist = VNS(child, qus);

        return new IndividualofMOEA(childlist.get(new Random().nextInt(childlist.size())));
    }

    /**
     * 计算两个向量的欧几里得距离
     * 
     * @param weight1 double[]
     * @param weight2 double[]
     * @return
     */
    public double WeightDistance(double[] weight1, double[] weight2) {
        double SUM = 0;
        for (int i = 0; i < this.objective.size(); i++)
            SUM += Math.pow((weight1[i] - weight2[i]), 2);

        return Math.pow(SUM, 1.0 / this.objective.size());
    }


    // SET methods
    /**
     * set the parameter of objective weight
     * <p>
     * eg.: H = 3, objective.size() = 2, then all the cases of the weight vector
     * are {[0,1], [1,0], [1/3,2/3], [2/3,1/3]}
     * </p>
     * 
     * @param h
     */
    public void setH(int h) {
        this.H = h;
    }

    /**
     * set the objective
     * @param name1 the name of objective 1
     * @param obj1 OBJECTIVE.MAX or OBJECTIVE.MIN
     * @param args arrange the String & OBJECTIVE in pairs
     */
    public void setObjective(String name1, OBJECTIVE obj1, Object... args) {
        this.objective = new HashMap<String, OBJECTIVE>();
        this.objective.put(name1, obj1);
        for (int i = 0; i < args.length; i+=2) {
            if (args[i].getClass().toString() == "String" && args[i+1].getClass().toString() == "OBJECTIVE") {
                String name = args[0].toString();
                OBJECTIVE obj = OBJECTIVE.valueOf(name);
                this.objective.put(name, obj);
            }
        }
    }

    /**
     * set the number of neighbors of weight
     * 
     * @param t
     */
    public void setT(int t) {
        if (t <= super.getPop().getPopsize()) {
            this.T = t;
        } else {
            throw new UnsupportedOperationException("the size of neighbors out of bounds!\nT here should be less than "
                    + super.getPop().getPopsize() + " but now it's " + t + ".");
        }
    }

    /**
     * set all the weight vector
     * 
     * @param weightlist
     */
    public void setWeightList(ArrayList<double[]> weightlist) {
        if (weightlist.size() == T + 1) {
            this.weightList = new ArrayList<>(weightlist);
        } else {
            throw new IllegalArgumentException(
                    "the size of the weight list MUST be " + this.T + 1 + " but now it's " + weightlist.size() + ".");
        }
    }

    /**
     * set the <i>i</i>th objective weight vector
     * 
     * @param i
     * @param weight eg. [1/2,1/2]
     * @throws Exception
     */
    public void setWeightList(int i, double[] weight) {
        if (weight.length == this.objective.size()) {
            this.weightList.set(i, weight);
        } else {
            throw new IllegalArgumentException("the vector dimension can NOT match the objective number N!\nN here is "
                    + this.objective.size() + "but the size of weight is now " + weight.length + ".");
        }
    }

    /**
     * add a new solution at the end of the EP list
     * 
     * @param EP
     */
    public void addEP(IndividualofMOEA EP) {
        this.EP.add(EP);
    }

    /**
     * remove one individual from EP
     * <p>
     * if calling this method continuously, make sure the remove action is in a
     * iterator
     * 
     * @param individual
     */
    public void removeEP(IndividualofMOEA individual) {
        this.EP.remove(individual);
    }

    /**
     * remove the <i>i</i>th individual from EP
     * <p>
     * if calling this method continuously, make sure the remove action is in a
     * iterator
     * 
     * @param i
     */
    public void removeEP(int i) {
        this.EP.remove(i);
    }

    /**
     * @param fV the fV to set
     */
    public void setFV(ArrayList<double[]> fV) {
        FV = fV;
    }

    /**
     * @param fV the <i>i</i>th fV to set
     * @param i
     */
    public void setFV(int i, double[] fV) {
        FV.set(i, fV);
    }

    /**
     * set the <i>i</i>th individual in EP set
     * 
     * @param i
     * @param individual
     */
    public void setEP(int i, IndividualofMOEA individual) {
        this.EP.set(i, individual);
    }

    /**
     * set the best objective value for Utilization OR Delay.
     * 
     * @param s ObjectiveName, "Utilization" OR "TotalDelay"
     * @param z value
     */
    public void setZ(String s, double z) {
        switch (s) {
            case "Utilization":
                if (z <= 1)
                    this.Z.replace(s, z);
                else
                    throw new IllegalArgumentException("the iuput value of utilization is OUT OF BOUNDS(100%).");
                break;
            case "TotalDelay":
                this.Z.replace(s, z);
            default:
                throw new IllegalArgumentException("the value of input String type MUST be \"U\" OR \"D\".");
        }
    }

    /**
     * set all the best objective value
     * 
     * @param z HashMap<String, Double>
     */
    public void setZ(HashMap<String, Double> z) {
        if (z.get("Utilization") <= 1) {
            this.Z.clear();
            this.Z.putAll(z);
        }
        else
            throw new IllegalArgumentException("the iuput value of utilization is OUT OF BOUNDS(100%).");
    }

    // GET methods
    /**
     * 权重参数
     * <p>
     * 对双目标问题而言，种群规模=H+1
     * </p>
     * <p>
     * eg.: H = 3, N = 2, then all the cases of the weight vector
     * are {[0,1], [1,0], [1/3,2/3], [2/3,1/3]}
     * </p>
     * 
     * @return the H
     */
    public int getH() {
        return H;
    }

    /**
     * 获取目标函数个数
     * 
     * @return the N
     */
    public int getObjectiveNum() {
        return this.objective.size();
    }

    /**
     * @return the objective
     */
    public HashMap<String, OBJECTIVE> getObjective() {
        return objective;
    }

    /**
     * 权重向量的邻居个数
     * 
     * @return the T
     */
    public int getT() {
        return T;
    }

    /**
     * 每个权重向量的 T 个邻居对应的下标
     * 
     * @return the B
     */
    public ArrayList<ArrayList<Integer>> getB() {
        return B;
    }

    /**
     * 权重集合
     * 
     * @return the weightList
     */
    public ArrayList<double[]> getWeightList() {
        return weightList;
    }

    /**
     * @return the fV
     */
    public ArrayList<double[]> getFV() {
        return FV;
    }

    /**
     * @return the EP
     */
    public ArrayList<IndividualofMOEA> getEP() {
        return EP;
    }

    /**
     * z*，每个目标分量上的最优值，顺序为 [最大平均利用率,最小总延误时间]
     * 
     * @return the Z
     */
    public HashMap<String, Double> getZ() {
        return Z;
    }
}
