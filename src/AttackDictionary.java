import java.util.*;

public class AttackDictionary {

    ArrayList<List> knownAttacks;

    public AttackDictionary(int k, Random random, int fromLen, int toLen) {
        knownAttacks = new ArrayList<>(k);
        int range = toLen - fromLen;
        assert (range >= 0): "Illegal argument";
        if(range == 0) {
            for (int i = 0; i < k; i++) {
                byte[] bytes = new byte[fromLen];
                random.nextBytes(bytes);
                knownAttacks.add(Arrays.asList(bytes));
            }
        }
        else
        {
            for (int i = 0; i < k; i++) {
                int size = random.nextInt(range + 1);
                byte[] bytes = new byte[fromLen + size];
                random.nextBytes(bytes);
                knownAttacks.add(Arrays.asList(bytes));
            }
        }
    }


    public boolean findAttack(byte[] msg)
    {
        int i = 0;
        List message = Arrays.asList(msg);
        for(List attack : knownAttacks)
        {
            if(Collections.indexOfSubList(message,attack) > -1)
            {
                i++;
                //return true;
            }
        }
        if(i < 0) System.out.println(i);
        return false;
    }

    public Byte[] getAttack(int n)
    {
        List<Byte> attack = knownAttacks.get(n%knownAttacks.size());
        Byte [] retArr = new Byte[attack.size()];
        attack.toArray(retArr);
        return retArr;
    }

}
