import java.util.HashSet;

class ScoreBoard {
    HashSet<Integer> okPackets = new HashSet<>();
    HashSet<Integer> l2Err = new HashSet<>();
    HashSet<Integer> l4Err = new HashSet<>();
    HashSet<Integer> sigErr = new HashSet<>();

    public boolean verifyOutput(OutputBlock outputBlock)
    {
        boolean allOk = true;
        HashSet<Integer> ok,l2,fw,sig;
        ok = outputBlock.getOkList();
        l2 = outputBlock.getL2ErrList();
        fw = outputBlock.getFWErrList();
        sig = outputBlock.getSigErrList();
        if(!ok.equals(okPackets))
        {
/*            System.out.println("OK packets don't match");
            System.out.println("Expected: " + okPackets.size()+ "\tFound: " + ok.size());*/
            allOk = false;
        }
        if(!l2.equals(l2Err))
        {
/*            System.out.println("L2 errors don't match");
            System.out.println("Expected: " + l2Err.size()+ "\tFound: " + l2.size());*/
            allOk = false;
        }
        if(!fw.equals(l4Err))
        {
/*            System.out.println("FW errors don't match");
            System.out.println("Expected: " + l4Err.size()+ "\tFound: " + fw.size());*/
            allOk = false;
        }
        if(!sig.equals(sigErr))
        {
/*            System.out.println("Signature errors don't match");
            System.out.println("Expected: " + sigErr.size()+ "\tFound: " + sig.size());*/
            allOk = false;
        }
/*        if(allOk) System.out.println("all is well");
        else System.out.println("all is bad");*/
          return allOk;
    }

}
