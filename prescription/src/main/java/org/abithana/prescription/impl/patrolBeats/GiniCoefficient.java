package org.abithana.prescription.impl.patrolBeats;

import java.util.ArrayList;
import java.util.Collections;

public class GiniCoefficient
{
        public double getGiniCoefficient(ArrayList<Long> population) {
            Collections.sort(population);
            int n = population.size();
            double giniCoefficient = 0;

            double upperSigma = 0;
            double lowerSigma = 0;

            for (int i =1 ; i <= n; i++) {
                upperSigma += (n+1-i)*population.get(i-1);

                lowerSigma += population.get(i-1);

            }

            giniCoefficient = (n+1 -(2 * upperSigma/lowerSigma))/n;

            System.out.println(" "+ giniCoefficient);

            return giniCoefficient;
        }

    public static void main(String[] args) {
        GiniCoefficient gini = new GiniCoefficient();
        ArrayList<Long> pop = new ArrayList<Long>();
        pop.add((long)1);
        pop.add((long)5);
        pop.add((long)3);
        pop.add((long)2);
        gini.getGiniCoefficient(pop);
    }
}