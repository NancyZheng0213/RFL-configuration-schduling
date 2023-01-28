package cn.nancy;

import java.util.ArrayList;

public class Individual {
    private Code code;
    private Decode decode;
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

    public Individual() {
        this.code = new Code();
        this.decode = new Decode();
        this.n = 0;
        this.S = new ArrayList<>();
        this.r = 0;
        this.d = 0;
    }

    public Individual(Individual individual) {
        this.code = new Code();
        this.decode = new Decode();
        setCode(individual.getCode());
        setDecode(individual.getDecode());
        setN(individual.getN());
        setS(individual.getS());
        setR(individual.getR());
        setD(individual.getD());
    }

    // SET method
    public void setCode(Code code) {
        this.code.setConfigurationCode(code.getConfigurationCode());
        this.code.setOperationCode(code.getOperationCode());
        this.code.setSortCode(code.getSortCode());
    }
    public void setDecode(Decode decode) {
        this.decode.setFinishTime(decode.getFinishTime());
        this.decode.setMakespan(decode.getMakespan());
        this.decode.setProcessingTime(decode.getProcessingTime());
        this.decode.setSetupFinishTime(decode.getSetupFinishTime());
        this.decode.setSetupStartTime(decode.getSetupStartTime());
        this.decode.setStartTime(decode.getStartTime());
        this.decode.setTotalDelay(decode.getTotalDelay());
        this.decode.setUtilization(decode.getUtilization());
    }
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
    public Code getCode() {
        return this.code;
    }
    public Decode getDecode() {
        return this.decode;
    }
    public int getN() {
        return this.n;
    }
    public ArrayList<Integer> getS() {
        return this.S;
    }
    public int getR() {
        return this.r;
    }
    public double getD() {
        return this.d;
    }
}
