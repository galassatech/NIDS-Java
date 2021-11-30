import java.security.*;
import java.util.ArrayList;

class PKI {
    Integer size;
    ArrayList<KeyPair> pki;

    public PKI(int K)
    {
        size = K;
        KeyPairGenerator keyPairGen;
        pki = new ArrayList<>();
        try {
            keyPairGen = KeyPairGenerator.getInstance("DSA");
            keyPairGen.initialize(2048);
            for (int i = 0 ; i < K ; i ++)
            {
                KeyPair pair = keyPairGen.generateKeyPair();
                pki.add(i,pair);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public KeyPair get(int idx)
    {
        return pki.get(idx);
    }

    public Integer getSize() {
        return size;
    }


    public byte[] sign(int id, byte[] message) throws Exception{
        KeyPair pair = pki.get(id);
        PrivateKey privKey = pair.getPrivate();
        Signature sign = Signature.getInstance("SHA256withDSA");
        sign.initSign(privKey);
        sign.update(message);
        return sign.sign();
    }

    public boolean verify (Packet.L5PLD msg) throws Exception
    {
        Signature sign = Signature.getInstance("SHA256withDSA");
        KeyPair pair = pki.get(msg.senderId);
        //Initializing the signature
        sign.initVerify(pair.getPublic());
        sign.update(msg.message);
        //Verifying the signature
        return sign.verify(msg.signature);
    }

}
