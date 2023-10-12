package cn.nancy.scheduling_of_rfl.MOEA;

import cn.nancy.scheduling_of_rfl.Individual;
import cn.nancy.scheduling_of_rfl.Pop;

public class PopofMOEA extends Pop {
    public PopofMOEA(int popsize) {
        super(popsize);
        IndividualofMOEA[] individuals = new IndividualofMOEA[popsize];
        for (int i = 0; i < popsize; i++) {
            individuals[i] = new IndividualofMOEA();
        }
        setIndividuals(individuals);
    }

    // SET methods
    /**
     * set the <i>i</i>th individual
     * 
     * <p> Different from the <code>setIndividual(int i, Individual individual)</code>, 
     * the individual here is class <code>IndividualofMOEA</code></P>
     * @param i
     * @param individual
     */
    public void setIndividual(int i, IndividualofMOEA individual) {
        super.setIndividual(i, individual);
    }
    public void setIndividual(int i, Individual individual) {
        if (IndividualofMOEA.class.isInstance(individual)) {
            super.setIndividual(i, individual);
        } else {
            IndividualofMOEA newindividual = new IndividualofMOEA();
            newindividual.setCode(individual.getCode());
            newindividual.setDecode(individual.getDecode());
            super.setIndividual(i, newindividual); 
        }
    }
    /**
     * deep copy the <i>i</i>th individual
     * 
     * <p>Different from the <code>deepsetIndividual(int i, Individual individual)</code>, 
     * the individual here is class <code>IndividualofMOEA</code></p>
     * @param i
     * @param individual
     */
    public void deepsetIndividual(int i, IndividualofMOEA individual) {
        super.deepsetIndividual(i, new IndividualofMOEA(individual));
    }

    // GET methods
    @Override
    public IndividualofMOEA[] getIndividuals() {
        return (IndividualofMOEA[])super.getIndividuals();
    }
    @Override
    public IndividualofMOEA getIndividual(int i) {
        return (IndividualofMOEA)super.getIndividual(i);
    }
}
