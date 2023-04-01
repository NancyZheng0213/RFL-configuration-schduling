package cn.nancy.scheduling_of_rfl.spea2;

public class Individual {
   /**
     * 个体编码
     */
    private Code code;
    /**
     * 个体解码
     */
    private Decode decode;
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

    public Individual() {
        this.code = new Code();
        this.decode = new Decode();
        this.R = 0;
        this.S = 0;
        this.D = 0.0;
    }
    public Individual(Individual individual) {
        this.code = new Code();
        this.decode = new Decode();
        setD(individual.getD());
        setFitness(individual.getFitness());
        setR(individual.getR());
        setS(individual.getS());
        setcode(individual.getcode());
        setdecode(individual.getdecode());
        setxigema(individual.getxigema());
    }

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
    public void setxigema(double xigema) {
        this.xigema = xigema;
    }
    public void setcode(Code code) {
        this.code.setConfigurationCode(code.getConfigurationCode());
        this.code.setOperationCode(code.getOperationCode());
        this.code.setSortCode(code.getSortCode());
    }
    public void setdecode(Decode decode) {
        this.decode.setFinishTime(decode.getFinishTime());
        this.decode.setMakespan(decode.getMakespan());
        this.decode.setProcessingTime(decode.getProcessingTime());
        this.decode.setSetupFinishTime(decode.getSetupFinishTime());
        this.decode.setSetupStartTime(decode.getSetupStartTime());
        this.decode.setStartTime(decode.getStartTime());
        this.decode.setTotalDelay(decode.getTotalDelay());
        this.decode.setUtilization(decode.getUtilization());
    }

    public Code getcode() {
        return this.code;
    }
    public Decode getdecode() {
        return this.decode;
    }
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
    public double getxigema() {
        return this.xigema;
    } 
}
