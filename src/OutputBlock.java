import java.util.HashSet;

public class OutputBlock {


    private OutputEntity ok=new OutputEntity(),l2=new OutputEntity(),fw=new OutputEntity(),sig=new OutputEntity();

    static boolean CLOSED_NESTING = true;

    public static void setClosedNesting(boolean closedNesting) {
        CLOSED_NESTING = closedNesting;
    }

    public void write(ProcessedPacket packet)
    {
        writeWrapper(packet);
    }

    private void writeWrapper(ProcessedPacket packet) {
        OutputEntity outputEntity;
        switch (packet.errortype){
            case L2: outputEntity = l2; break;
            case FW: outputEntity = fw; break;
            case SIGERR: outputEntity = sig; break;
            default: outputEntity = ok; break;
        }
        if(CLOSED_NESTING)
        {
            nestedWrite(packet,outputEntity);
        }
        else
            flatWrite(packet,outputEntity);
    }

    private void flatWrite(ProcessedPacket packet, OutputEntity outputEntity) {
        outputEntity.write(packet.id);
    }


    private void nestedWrite(ProcessedPacket packet, OutputEntity outputEntity) {
        // TODO: this should be ATOMIC
        outputEntity.write(packet.id);
    }

    public HashSet<Integer> getL2ErrList()
    {//Return output as arraylist for verification
        return l2.getHashSet();
    }

    public HashSet<Integer> getFWErrList()
    {//Return output as arraylist for verification
        return fw.getHashSet();
    }

    public HashSet<Integer> getSigErrList()
    {//Return output as arraylist for verification
        return  sig.getHashSet();
    }

    public HashSet<Integer> getOkList()
    {//Return output as arraylist for verification
        return ok.getHashSet();
    }

}
