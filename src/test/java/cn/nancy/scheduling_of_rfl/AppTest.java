package cn.nancy.scheduling_of_rfl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import cn.nancy.scheduling_of_rfl.spea2.AllInSPEA2;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    enum OBJECTIVE {
            MAX, MIN
    };
    /**
     * Rigorous Test :-)
     * @throws Exception
     */
    @Test
    public void shouldAnswerWithTrue() throws Exception
    {
        ArrayList<Individual> individuals = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            individuals.add(new Individual());
            System.out.println(individuals.get(i));
        }
        ArrayList<Individual> individualsCopy = new ArrayList<>(individuals);
        Individual special = individuals.get(1);
        Iterator<Individual> iterator = individualsCopy.iterator();
        while (iterator.hasNext()) {
            Individual individual = iterator.next();
            System.out.println(individual);
            if (individual.equals(special)) {
                individuals.remove(individual);
            }
        }
        System.out.println("----\n\n");
        iterator = individuals.iterator();
        while (iterator.hasNext()) {
            Individual individual = iterator.next();
            System.out.println(individual);
        }
    }
}
