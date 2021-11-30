import TDSL.Consumer;
import TDSL.TX;
import TDSL.TXLibExceptions;

public class SignatureMatcher implements Consumer<ProcessedPacket>, Runnable {

    PKI pki;
    OutputBlock outputBlock;
    AttackDictionary dictionary;
    MatchingQueue matchingQueue;
    private int nLogsPerThread;


    public SignatureMatcher(PKI pki, OutputBlock outputBlock, AttackDictionary dictionary,
                            MatchingQueue matchingQueue, int nLogsPerThread) {
        this.pki = pki;
        this.outputBlock = outputBlock;
        this.dictionary = dictionary;
        this.matchingQueue = matchingQueue;
        this.nLogsPerThread = nLogsPerThread;
    }

    public SignatureMatcher(PKI pki, OutputBlock outputBlock, AttackDictionary dictionary) {
        this.pki = pki;
        this.outputBlock = outputBlock;
        this.dictionary = dictionary;
    }

    public void consume(ProcessedPacket packet) throws Exception {
        if (packet.errortype.equals(ProcessedPacket.ERRORTYPE.OK))
        {
/*            System.out.println("sig check");
            System.out.println("f = " + packet.fragNum + " of " + packet.totalFragments);
            System.out.println(packet.pld.message.length);*/
            if(pki.verify(packet.pld)) //TODO: Add variant w/o pki
            {
                if(!dictionary.findAttack(packet.pld.message))
                    packet.errortype = ProcessedPacket.ERRORTYPE.OK;//SIGERR;
                else
                    packet.errortype = ProcessedPacket.ERRORTYPE.SIGERR;
            }
            else
                packet.errortype = ProcessedPacket.ERRORTYPE.SIGERR;
        }

        outputBlock.write(packet);
    }


    @Override
    public void run() {
        for (int i = 0; i < nLogsPerThread; i++) {
            while (true) {
                try {
                    try {
                        TX.TXbegin();
                        matchingQueue.get(this);
                    } catch (TXLibExceptions.AbortException exp) {
                        if (TX.DEBUG_MODE_TX) System.out.println("abort");
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        TX.TXend();
                    }
                } catch (TXLibExceptions.AbortException exp) {
                    if (TX.DEBUG_MODE_TX) System.out.println("abort");
                    continue;
                }
                break;
            }
        }
    }
}
