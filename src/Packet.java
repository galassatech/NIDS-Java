class Packet {
    L2Header l2Header;
    L3Header l3Header;
    L4Header l4Header;
    L5PLD pld;
    Integer id;
    int fragNum=0;
    int totalFragments=0;

    public Packet(L2Header l2Header, L3Header l3Header, L4Header l4Header, L5PLD pld, Integer id, int fragNum, int totalFragments) {
        this.l2Header = l2Header;
        this.l3Header = l3Header;
        this.l4Header = l4Header;
        this.pld = new L5PLD(pld.message, pld.senderId, pld.signature,pld.sigErr);;
        this.id = id;
        this.fragNum = fragNum;
        this.totalFragments = totalFragments;
    }

    public Packet(L2Header l2Header, L3Header l3Header, L4Header l4Header, L5PLD pld, Integer id) {
        this.l2Header = l2Header;
        this.l3Header = l3Header;
        this.l4Header = l4Header;
        this.pld = new L5PLD(pld.message, pld.senderId, pld.signature,pld.sigErr);
        this.id = id;
        this.fragNum = 1;
        this.totalFragments = 1;
    }


    public Packet()
    {
        this.l2Header = null;
        this.l3Header = null;
        this.l4Header = null;
        this.pld = null;
        this.id = null;
    }


    static byte getCheckSum(byte[] msg)
    {
        byte res = 0;
        for(Byte b : msg)
        {
            res = (byte)(res ^ b);
        }
        return res;

    }

    public byte[] getSrcIp()
    {
        return l3Header.getSrcIP();
    }


    class L2Header{
        byte checkSum;

        L2Header(byte cs) {
           checkSum = cs;
        }

        /*public boolean checkSum(byte[] msg) {
            byte res = getCheckSum(msg);
            return checkSum.equals(res);
        }*/

    }

    class L3Header{
        byte[] srcIP;
        byte[] destIP;


        public L3Header()
        {
            srcIP = new byte[4];
            destIP = new byte[4];
            for(int i = 0 ; i < 4 ; i++)
            {
                srcIP[i] = (byte)0;
                destIP[i] = (byte)255;
            }
        }

        public L3Header(byte[] src , byte[] dest)
        {
            srcIP = src;
            destIP = dest;
        }

        public byte[] getSrcIP() {
            return srcIP;
        }
        public byte[] getDestIP() {
            return destIP;
        }
    }

    public class L4Header{
        String protocol;
        int srcPort , destPort;

        public L4Header()
        {
            protocol = "TCP";
            srcPort = destPort = 0;
        }

        public L4Header(String prot, int src , int dest)
        {
            protocol = prot;
            srcPort = src ;
            destPort = dest;
        }

        public String getProtocol() {
            return protocol;
        }
    }

    public class L5PLD{
        byte[] message;
        int senderId;
        byte[] signature;
        boolean sigErr;

        public L5PLD(byte[] msg, int id, byte[] signature, boolean sigErr){
            this.senderId = id;
            this.message = new byte[256];
            System.arraycopy(msg,0,message,0,256);
            this.signature = signature;
            this.sigErr = sigErr;
        }

        public void setMessage(byte[] message) {
            byte[] newMessage = new byte[message.length];
            System.arraycopy(message,0,newMessage,0,message.length);
            this.message = newMessage;
        }

    }
}
