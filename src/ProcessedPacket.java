public class ProcessedPacket extends Packet {

    enum ERRORTYPE {
        OK, // ok
        L2, // l2 err
        FW, // Firewall Err
        SIGERR, // Signature err
    }

    ERRORTYPE errortype = ERRORTYPE.OK;

    public ProcessedPacket(Packet packet, ERRORTYPE errortype) {
        super(packet.l2Header, packet.l3Header, packet.l4Header, packet.pld, packet.id, packet.fragNum, packet.totalFragments);
        pld.setMessage(packet.pld.message);
        this.errortype = errortype;
    }

    public ProcessedPacket(Packet packet, ERRORTYPE errortype, byte[] message) {
        super(packet.l2Header, packet.l3Header, packet.l4Header, packet.pld, packet.id, packet.fragNum, packet.totalFragments);
        pld.setMessage(message);
        this.errortype = errortype;
    }

    public ERRORTYPE getErrortype() {
        return errortype;
    }

    public void setErrortype(ERRORTYPE errortype) {
        this.errortype = errortype;
    }
}
