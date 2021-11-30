import java.util.ArrayList;

public class HeaderExtractor implements /*Consumer<Packet>,*/ Runnable {


    Firewall firewall;
    StateInfo stateInfo;
    PacketPool packetPool;
    MatchingQueue matchingQueue;
    boolean stop = false;
    int nFragments;
    SignatureMatcher signatureMatcher;
    long[] counter;
    boolean split;
    boolean nestRemove;
    boolean nestPIA = false;


    public HeaderExtractor(Firewall firewall, PacketPool packetPool, StateInfo stateInfo, MatchingQueue matchingQueue,
                           int nFragments, SignatureMatcher signatureMatcher, long[] counter, boolean nestPIA) {
        this.firewall = firewall;
        this.stateInfo = stateInfo;
        this.packetPool = packetPool;
        this.matchingQueue = matchingQueue;
        this.nFragments = nFragments;
        this.signatureMatcher = signatureMatcher;
        split = false;
        this.nestRemove = true;
        this.counter = counter;
        this.nestPIA = nestPIA;
    }


    public HeaderExtractor(Firewall firewall, PacketPool packetPool, StateInfo map, MatchingQueue matchingQueue, int nFragments) {
        this.firewall = firewall;
        this.packetPool = packetPool;
        this.stateInfo = map;
        this.matchingQueue = matchingQueue;
        this.nFragments = nFragments;
        split = true;
        this.nestRemove = false;
        this.counter = counter;
    }


    //@Override
    public void consume(Packet pkt) throws Exception {
        if (pkt == null) {
            stop = true;
            return;
        }
        counter[pkt.totalFragments * pkt.id + pkt.fragNum]++;
        if (pkt.l2Header.checkSum != Packet.getCheckSum(pkt.pld.message)) {
            logPacket(pkt, ProcessedPacket.ERRORTYPE.L2);
        } else if (!firewall.checkRule(pkt)) {
            logPacket(pkt, ProcessedPacket.ERRORTYPE.FW);
        } else logPacket(pkt, ProcessedPacket.ERRORTYPE.OK);

        if (stateInfo.complete(pkt.id)) {
            ArrayList<ProcessedPacket> packets = stateInfo.getFragments(pkt.id);
            ProcessedPacket.ERRORTYPE errortype = ProcessedPacket.ERRORTYPE.OK;
            byte[] msg = new byte[256 * pkt.totalFragments];
            int i = 0;
            for (ProcessedPacket packet : packets) {
                System.arraycopy(packet.pld.message, 0, msg, i * 256, 256);
                i++;
                if (packet.errortype == ProcessedPacket.ERRORTYPE.OK || errortype == ProcessedPacket.ERRORTYPE.L2) {
                    continue;
                } else {
                    errortype = packet.errortype;
                }
            }

            Packet p = new Packet(pkt.l2Header, pkt.l3Header, pkt.l4Header, pkt.pld, pkt.id, 1, 1);
            p.pld.setMessage(msg);
            ProcessedPacket processedPacket = new ProcessedPacket(p, errortype, msg);
            if (split)
                matchingQueue.put(processedPacket);
            else
                signatureMatcher.consume(processedPacket);

        }

    }


    private void logPacket(Packet p, ProcessedPacket.ERRORTYPE errortype) {
        ProcessedPacket processedPacket = new ProcessedPacket(p, errortype);
        // Perform Atomically:
            stateInfo.put(processedPacket);
    }

    @Override
    public void run() {
        for (int i = 0; i < nFragments; i++) {
            // this should be ATOMIC
            if (!stop) packetPool.consume(this);
        }


    }
}
