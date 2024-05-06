import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

import static java.math.BigInteger.*;

public class Main
{
    public final int FERTILITY_RATE = 1; // chance of childbirth, 0-1
    public final double MALE_CHANCE = 0.5; // chance of male birth out of 1
    public final int BEGIN_CHILDBIRTH_YEARS = 20;
    public final int END_CHILDBIRTH_YEARS = 60; // no longer able to give birth
    public final int MAX_AGE = 120; // everyone over this age (next year) automatically dies
    public final int MAX_MARRIAGE_GAP = 5; // maximum difference in age between partners in marriage

    public final int YEARS_TO_RUN = 210;

    public ArrayList<BigInteger[]> popPool = new ArrayList();

    // age pool layout:
    // [unmarried male, unmarried female, married male, married female]

    public Main()
    {
        populateInitialPools(20);

        // add 50 singles; to become 25 breeding pairs
        BigInteger[] temp = popPool.get(19); // age 19; becomes 20 at year cycle
        temp[0] = valueOf(25);
        temp[1] = valueOf(25);

        for (int i = 0; i < YEARS_TO_RUN; i++)
        {
            yearCycle();
            System.out.println("Finished year " + i);
        }

        displayPools();
        System.out.println(getTotalPopulation());

    }

    public void yearCycle()
    {
        // add year (cause everyone to age up as well)
        popPool.add(0, new BigInteger[]{ZERO, ZERO, ZERO, ZERO});

        // kill everyone beyond maximum age
        if (popPool.size() - 1 == MAX_AGE + 1)
            popPool.remove(MAX_AGE + 1);

        // calculate environmental deaths

        // calculate marriages
        calculateMarriages();

        // calculate childbirths
        BigInteger[] newChildren = calculateChildbirth();
        popPool.get(0)[0] = newChildren[0];
        popPool.get(0)[1] = newChildren[1];
    }

    /**
     * Calculate all childbirths in the year.
     */
    public BigInteger[] calculateChildbirth()
    {
        BigInteger[] newChildren = {ZERO, ZERO};

        int end = popPool.size() - 1;
        if (end > END_CHILDBIRTH_YEARS) end = END_CHILDBIRTH_YEARS - 1;
        for (int i = end; i >= BEGIN_CHILDBIRTH_YEARS; i--) // calculate births from oldest to youngest
        {
            BigInteger[] temp = calculateChildbirthUtil(i);

            newChildren[0] = newChildren[0].add(temp[0]);
            newChildren[1] = newChildren[1].add(temp[1]);
        }

        return newChildren;
    }

    /**
     * Calculate childbirths for a single age pool in a year.
     */
    public BigInteger[] calculateChildbirthUtil(int age)
    {
        // TODO chance of childbirth
        // TODO chance of twin, triplets, etc
        // TODO chance of childbirth deaths

        BigInteger[] newChildren = {ZERO, ZERO};
        for (BigInteger i = ZERO; i.compareTo(popPool.get(age)[3]) < 0; i = i.add(ONE)) // for each married female
        {
            if (Math.random() < FERTILITY_RATE) // give birth
            {
                // currently only single births; 50/50 male/female
                if (Math.random() < MALE_CHANCE)
                    newChildren[0] = newChildren[0].add(ONE); // male
                else
                    newChildren[1] = newChildren[1].add(ONE); // female
            }
        }

        return newChildren;
    }

    public void calculateMarriages()
    {
        // get highest marriageable age
        int end = popPool.size() - 1;
        if (end > END_CHILDBIRTH_YEARS) end = END_CHILDBIRTH_YEARS - 1;

        for (int age = end; age > BEGIN_CHILDBIRTH_YEARS - 1; age--)
        {
            BigInteger[] currentAgePool = popPool.get(age);
            // attempt same age batch marriages
            if (!currentAgePool[0].equals(ZERO) && !currentAgePool[1].equals(ZERO))
            {
                // get lower number in age, males or females
                BigInteger num = currentAgePool[0].min(currentAgePool[1]);

                // subtract that number from singles and add to marriages
                currentAgePool[0] = currentAgePool[0].subtract(num); // male
                currentAgePool[2] = currentAgePool[2].add(num); // male
                currentAgePool[1] = currentAgePool[1].subtract(num); // female
                currentAgePool[3] = currentAgePool[3].add(num); // female
            }
            // at this point, either no single males or no single females (or both) remaining
            if (!currentAgePool[0].equals(ZERO)) // males remaining
            {
                for (int i = 1; i <= MAX_MARRIAGE_GAP && age - i >= BEGIN_CHILDBIRTH_YEARS // within 5 years; least 20 years old
                        && !currentAgePool[0].equals(ZERO); i++) // and single males remaining
                {
                    if (!popPool.get(age - i)[1].equals(ZERO)) // available in age pool
                    {
                        BigInteger num = currentAgePool[0].min(popPool.get(age - i)[1]);

                        // subtract that number from singles and add to marriages
                        currentAgePool[0] = currentAgePool[0].subtract(num); // male
                        currentAgePool[2] = currentAgePool[2].add(num); // male
                        popPool.get(age - i)[1] = popPool.get(age - 1)[1].subtract(num); // female
                        popPool.get(age - i)[3] = popPool.get(age - 1)[3].add(num); // female
                    }
                }
            }
            if (!currentAgePool[1].equals(ZERO)) // females remaining
            {
                for (int i = 1; i <= MAX_MARRIAGE_GAP && age - i >= BEGIN_CHILDBIRTH_YEARS // within 5 years; least 20 years old
                        && !currentAgePool[1].equals(ZERO); i++) // and single females remaining
                {
                    if (!popPool.get(age - i)[0].equals(ZERO)) // available in age pool
                    {
                        BigInteger num = currentAgePool[1].min(popPool.get(age - i)[0]);

                        // subtract that number from singles and add to marriages
                        currentAgePool[1] = currentAgePool[1].subtract(num); // female
                        currentAgePool[3] = currentAgePool[3].add(num); // female
                        popPool.get(age - i)[0] = popPool.get(age - 1)[0].subtract(num); // male
                        popPool.get(age - i)[2] = popPool.get(age - 1)[2].add(num); // male
                    }
                }
            }
            // all possible marriages made; singles may remain
        }
    }

    public void populateInitialPools(int numGenerations)
    {
        for (int i = 0; i < numGenerations; i++)
            popPool.add(0, new BigInteger[]{ZERO, ZERO, ZERO, ZERO});
    }

    public BigInteger getPopulationOfAge(int age)
    {
        BigInteger sum = ZERO;
        BigInteger[] array = popPool.get(age);
        for (int i = 0; i < array.length; i++)
            sum = sum.add(array[i]);
        return sum;
    }

    public BigInteger getTotalPopulation()
    {
        BigInteger total = ZERO;
        for (int i = 0; i < popPool.size(); i++)
            total = total.add(getPopulationOfAge(i));
        return total;
    }

    public void displayPools()
    {
        for (int i = 0; i < popPool.size(); i++)
            System.out.println(String.format("%2d", i) + " : " + Arrays.toString(popPool.get(i)));
    }

    public static void main(String[] args)
    {
        new Main();
    }
}
