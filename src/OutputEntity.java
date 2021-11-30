import java.util.HashSet;

public class OutputEntity {

    /*
    * YOUR IMPLEMENTATION GOES HERE
    * Replace Log with your own
    * */

    Log log = new Log();

    public void write(Integer id)
    {
        log.append(id);
    }

    public HashSet<Integer> getHashSet()
    {
        HashSet<Integer> values = new HashSet<>();
        for(Object i : log.getLog())
        {
            values.add((Integer)i);
        }
        return values;
    }



}
