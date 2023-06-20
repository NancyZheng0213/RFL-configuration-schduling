package cn.nancy.scheduling_of_rfl.spea2;

import cn.nancy.scheduling_of_rfl.Individual;

public class IndividualofSPEA2 extends Individual {
    /**
     * row fitness
     */
    private int R;
    /**
     * strength value
     */
    private int S;
    /**
     * density
     */
    private double D;
    /**
     * xigema_k
     */
    private double xigema;
    /**
     * fitness
     */
    private double Fitness;

    public IndividualofSPEA2() {
        super();
        this.R = 0;
        this.S = 0;
        this.D = 0.0;
    }

    public IndividualofSPEA2(IndividualofSPEA2 individual) {
        super(individual);
        setD(individual.getD());
        setFitness(individual.getFitness());
        setR(individual.getR());
        setS(individual.getS());
        setCode(individual.getCode());
        setDecode(individual.getDecode());
        setXigema(individual.getXigema());
    }

    // SET method
    public void setS(int S) {
        this.S = S;
    }
    public void setD(double D) {
        this.D = D;
    }
    public void setR(int R) {
        this.R = R;
    }
    public void setFitness(double fitness) {
        this.Fitness = fitness;
    }
    public void setXigema(double xigema) {
        this.xigema = xigema;
    }

    // GET method
    public int getS() {
        return this.S;
    }
    public int getR() {
        return this.R;
    }
    public double getD() {
        return this.D;
    }
    public double getFitness() {
        return this.Fitness;
    }
    public double getXigema() {
        return this.xigema;
    } 
}
