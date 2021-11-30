
public class MatchingQueue {

    Integer size;
    ProducerConsumerPool pool; //TODO: Replace Producer-consumer pool with your own  data structure

    public MatchingQueue(Integer size) {
        this.size = size;
        this.pool = new ProducerConsumerPool(size);
    }


    public void get(SignatureMatcher matcher) throws Exception {
        pool.consume(matcher);
    }

    public boolean put(ProcessedPacket p)
    {
        return pool.produce(p);
    }


}
