

public class PacketPool {

    /*
    * Should be of fixed size and support consume consume and put operations
    * */

    Integer size;
    ProducerConsumerPool pool; // TODO: use your own producer-consumer

    public PacketPool(Integer size) {
        this.size = size;
        this.pool = new ProducerConsumerPool(size);
    }

    public boolean consume(HeaderExtractor extractor) throws Exception {
        pool.consume(extractor);
        return true;
    }

    public boolean put(Packet p)
    {
        return pool.produce(p);
    }

}
