import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

class PacketGenerator {
    Random random;
    int csErrProb, sigErrProb; // percentage
    AtomicInteger flowId = new AtomicInteger(0);
    PKI pki;
    ScoreBoard scoreBoard;
    final static byte parityErr = 1;


    PacketGenerator(int seed, int cs, int sig, PKI pki, ScoreBoard scoreBoard){
        random = new Random(seed);
        csErrProb = cs;
        sigErrProb = sig;
        this.pki = pki ;
        this.scoreBoard = scoreBoard;
    }


    public Packet getRandomPacket() throws Exception{ //TODO: consume scoreboard as well
        // Create L5 packet
        return getRandomFragmentedPacket(1).get(0);
    }

    public ArrayList<Packet> getRandomFragmentedPacket(int nFragments) throws Exception//int pid)
    {
        ArrayList<Packet> packets = new ArrayList(nFragments);
        byte[] msg = getMessage(nFragments);
        Integer pid = flowId.getAndIncrement();
        int id = random.nextInt(pki.getSize());
        boolean sigerr = random.nextInt(100) < sigErrProb;
        boolean cSErr = false;
        Packet p = new Packet();

        byte[] signature = getSignature(id,msg,sigerr);

        Packet.L4Header l4 = p.new L4Header(random.nextBoolean()?"TCP":"UDP", random.nextInt(), random.nextInt());
        byte[] src = new byte[4];
        byte[] dest = new byte[4];
        random.nextBytes(src);
        random.nextBytes(dest);
        Packet.L3Header l3 = p.new L3Header(src,dest);

        for(short i = 0 ; i < nFragments ; i++)
        {
            byte[] fragment = new byte[256];
            fragment = Arrays.copyOfRange(msg,i*256, (i+1)*256);
            Packet.L5PLD pld = p.new L5PLD(fragment,id,signature,sigerr);
            byte cs = Packet.getCheckSum(fragment);
            if( random.nextInt(100) < csErrProb) {
                cs = (byte)(cs ^ parityErr);
                cSErr = true;
            }
            Packet.L2Header l2Header = p.new L2Header(cs);
            packets.add(new Packet(l2Header, l3, l4,pld, pid,i+1,nFragments));
        }
        if(cSErr)
        {
            synchronized (scoreBoard.l2Err) {
                scoreBoard.l2Err.add(pid);
            }
        }
        else if(sigerr)
        {
            synchronized (scoreBoard.sigErr) {
                scoreBoard.sigErr.add(pid);
            }
        }
        else
        {
            synchronized (scoreBoard.okPackets) {
                scoreBoard.okPackets.add(pid);
            }
        }

        return packets;
    }

    private byte[] getMessage(int nFragments) {
        byte[] msg = new byte[256*nFragments];
        random.nextBytes(msg);
        return  msg;
    }

    private byte[] getSignature(int id, byte[] msg, boolean sigErr) throws Exception {
        Packet p = new Packet();

        byte[] signature;
        if(sigErr) signature = pki.sign((id+1) % pki.getSize(),msg);
        else signature = pki.sign(id,msg);
        return signature;
    }


}
