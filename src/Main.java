import java.util.ArrayList;
import java.util.Arrays;

public class Main
{
    public final int FERTILITY_RATE = 1; // chance of childbirth, 0-1
    public final double MALE_CHANCE = 0.5; // chance of male birth out of 1
    public final int BEGIN_CHILDBIRTH_YEARS = 20;
    public final int END_CHILDBIRTH_YEARS = 60; // no longer able to give birth
    public final int MAX_AGE = 120; // everyone over this age (next year) automatically dies
    public final int MAX_MARRIAGE_GAP = 5; // maximum difference in age between partners in marriage

    public final int YEARS_TO_RUN = 210;

    public ArrayList<int[]> popPool = new ArrayList();

    // age pool layout:
    // [unmarried male, unmarried female, married male, married female]

    public Main()
    {
        populateInitialPools(20);

        // add 50 singles; to become 25 breeding pairs
        int[] temp = popPool.get(19); // age 19; becomes 20 at year cycle
        temp[0] = 25;
        temp[1] = 25;

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
        popPool.add(0, new int[]{0, 0, 0, 0});

        // kill everyone beyond maximum age
        if (popPool.size() - 1 == MAX_AGE + 1)
            popPool.remove(MAX_AGE + 1);

        // calculate environmental deaths

        // calculate marriages
        calculateMarriages();

        // calculate childbirths
        int[] newChildren = calculateChildbirth();
        popPool.get(0)[0] = newChildren[0];
        popPool.get(0)[1] = newChildren[1];
    }

    /**
     * Calculate all childbirths in the year.
     */
    public int[] calculateChildbirth()
    {
        int[] newChildren = {0, 0};

        int end = popPool.size() - 1;
        if (end > END_CHILDBIRTH_YEARS) end = END_CHILDBIRTH_YEARS - 1;
        for (int i = end; i >= BEGIN_CHILDBIRTH_YEARS; i--) // calculate births from oldest to youngest
        {
            int[] temp = calculateChildbirthUtil(i);

            newChildren[0] += temp[0];
            newChildren[1] += temp[1];
        }

        return newChildren;
    }

    /**
     * Calculate childbirths for a single age pool in a year.
     */
    public int[] calculateChildbirthUtil(int age)
    {
        // TODO chance of childbirth
        // TODO chance of twin, triplets, etc
        // TODO chance of childbirth deaths

        int[] newChildren = {0, 0};
        for (int i = 0; i < popPool.get(age)[3]; i++) // for each married female
        {
            if (Math.random() < FERTILITY_RATE) // give birth
            {
                // currently only single births; 50/50 male/female
                if (Math.random() < MALE_CHANCE)
                    newChildren[0] += 1; // male
                else
                    newChildren[1] += 1; // female
            }
        }

        return newChildren;
    }

    public void calculateMarriages()
    {
        // get highest marriagable age
        int end = popPool.size() - 1;
        if (end > END_CHILDBIRTH_YEARS) end = END_CHILDBIRTH_YEARS - 1;

        for (int age = end; age > BEGIN_CHILDBIRTH_YEARS - 1; age--)
        {
            int[] currentAgePool = popPool.get(age);
            // attempt same age batch marriages
            if (currentAgePool[0] != 0 && currentAgePool[1] != 0)
            {
                // get lower number in age, males or females
                int num = Math.min(currentAgePool[0], currentAgePool[1]);

                // subtract that number from singles and add to marriages
                currentAgePool[0] -= num; currentAgePool[2] += num; // male
                currentAgePool[1] -= num; currentAgePool[3] += num; // female
            }
            // at this point, either no single males or no single females (or both) remaining
            if (currentAgePool[0] != 0) // males remaining
            {
                for (int i = 1; i <= MAX_MARRIAGE_GAP && age - i >= BEGIN_CHILDBIRTH_YEARS // within 5 years; least 20 years old
                        && currentAgePool[0] != 0; i++) // and single males remaining
                {
                    if (popPool.get(age - i)[1] != 0) // available in age pool
                    {
                        int num = Math.min(currentAgePool[0], popPool.get(age - i)[1]);

                        // subtract that number from singles and add to marriages
                        currentAgePool[0] -= num; currentAgePool[2] += num; // male
                        popPool.get(age - i)[1] -= num; popPool.get(age - i)[3] += num; // female
                    }
                }
            }
            if (currentAgePool[1] != 0) // females remaining
            {
                for (int i = 1; i <= MAX_MARRIAGE_GAP && age - i >= BEGIN_CHILDBIRTH_YEARS // within 5 years; least 20 years old
                        && currentAgePool[1] != 0; i++) // and single females remaining
                {
                    if (popPool.get(age - i)[0] != 0) // available in age pool
                    {
                        int num = Math.min(currentAgePool[1], popPool.get(age - i)[0]);

                        // subtract that number from singles and add to marriages
                        currentAgePool[1] -= num; currentAgePool[3] += num; // female
                        popPool.get(age - i)[0] -= num; popPool.get(age - i)[2] += num; // male
                    }
                }
            }
            // all possible marriages made; singles may remain
        }
    }

    public void populateInitialPools(int numGenerations)
    {
        for (int i = 0; i < numGenerations; i++)
            popPool.add(0, new int[]{0, 0, 0, 0});
    }

    public int getPopulationOfAge(int age)
    {
        int sum = 0;
        int[] array = popPool.get(age);
        for (int i = 0; i < array.length; i++)
            sum += array[i];
        return sum;
    }

    public int getTotalPopulation()
    {
        int total = 0;
        for (int i = 0; i < popPool.size(); i++)
            total += getPopulationOfAge(i);
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
