package cn.nancy.scheduling_of_rfl.nsgaii;

import java.util.ArrayList;
import cn.nancy.scheduling_of_rfl.Individual;

public class IndividualofNSGAII extends Individual {
    /**
     * the number of being dominated
     */
    private int n;
    /**
     * dominate set, the Integer represents the index of individual in pop
     */
    private ArrayList<Integer> S;
    /**
     * front rank
     */
    private int r;
    /**
     * crowding distance
     */
    private double d;

    public IndividualofNSGAII() {
        super();
        this.n = 0;
        this.S = new ArrayList<>();
        this.r = 0;
        this.d = 0;
    }

    /**
     * deep copy from another individual
     * @param individual
     */
    public IndividualofNSGAII(IndividualofNSGAII individual) {
        super(individual);
        setN(individual.getN());
        setS(individual.getS());
        setR(individual.getR());
        setD(individual.getD());
    }

    // SET method
    public void setN(int n) {
        this.n = n;
    }
    public void setS(ArrayList<Integer> S) {
        this.S = new ArrayList<>(S);
    }
    public void addS(Integer s) {
        this.S.add(s);
    }
    public void setR(int r) {
        this.r = r;
    }
    public void setD(double d) {
        this.d = d;
    }


    // GET method
    /**
     * get the number N of being dominated
     * @return int
     */
    public int getN() {
        return this.n;
    }
    /**
     * get the dominate set
     * <p> the Integer represents the index of individual in pop
     * @return ArrayList<Integer>
     */
    public ArrayList<Integer> getS() {
        return this.S;
    }
    /**
     * get the individual front rank
     * @return int
     */
    public int getR() {
        return this.r;
    }
    /**
     * get the individual crowding distance
     * @return
     */
    public double getD() {
        return this.d;
    }
}
