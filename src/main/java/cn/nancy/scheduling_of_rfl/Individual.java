package cn.nancy.scheduling_of_rfl;

public class Individual {
    /**
     * 个体编码
     */
    private Code code;
    /**
     * 个体解码
     */
    private Decode decode;

    public Individual() {
        this.code = new Code();
        this.decode = new Decode();
    }

    public Individual(Individual individual) {
        this.code = new Code();
        this.decode = new Decode();
        setCode(individual.getCode());
        setDecode(individual.getDecode());
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


    // GET method
    public Code getCode() {
        return this.code;
    }
    public Decode getDecode() {
        return this.decode;
    }
}
