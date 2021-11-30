import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.max;

public class NIDS {

    public static class ParallelGenerator implements Runnable
    {

        PacketGenerator generator;
        int nFragmentsPerPacket, nPackets;
        ArrayList<ArrayList> packets;

        public ParallelGenerator(PacketGenerator generator,  int nPackets, int nFragmentsPerPacket, ArrayList<ArrayList> packets) {
            this.generator = generator;
            this.nFragmentsPerPacket = nFragmentsPerPacket;
            this.nPackets = nPackets;
            this.packets = packets;
        }

        @Override
        public void run() {
            for(int i = 0 ; i < nPackets ; i++) {
                ArrayList<Packet> fragments = null;
                try {
                    fragments = generator.getRandomFragmentedPacket(nFragmentsPerPacket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                packets.add(fragments);
            }
        }
    }


    public static void main(String[] args) throws Exception {
        int nPackets = 4092;
        int nFragmentsPerPacket = 2;
        int nSenders = 10;
        int nHdrThreads = 0 ,nSigThreads = 0,nThreads = 4, nProducingThreads = 2, nInitThreads = 2 ;
        int seed = 2709;
        int csProb = 50, sigProb = 50;
        boolean splitTx = false;
        boolean nestLog = true;
        boolean nestPIA = false;
        int poolSize = 0 ;
        int i=0;
        String arg;
        while ( i< args.length) {
            if(args[i].charAt(0) == '-') {
                arg = args[i++];
                //check options
                if(arg.equals("-t")) {
                    nThreads = Integer.parseInt(args[i++]);
                }
                else if(arg.equals("-p")) {
                    nProducingThreads = Integer.parseInt(args[i++]);
                }
                else if(arg.equals("-k")) {
                    poolSize = Integer.parseInt(args[i++]);
                }
                else if(arg.equals("-n")) {
                    nPackets = Integer.parseInt(args[i++]);
                }
                else if(arg.equals("-l")) {
                    nFragmentsPerPacket = Integer.parseInt(args[i++]);
                }
                else if(arg.equals("-b")) {
                    nestLog = Boolean.parseBoolean(args[i++]);
                }
                else if(arg.equals("-c")) {
                    csProb = Integer.parseInt(args[i++]);
                    assert (csProb >= 0 && csProb <= 100);
                }
                else if(arg.equals("-v")) {
                    sigProb = Integer.parseInt(args[i++]);
                    assert (sigProb >= 0 && sigProb <= 100);
                }
                else if(arg.equals("-m")) {
                    nSenders = Integer.parseInt(args[i++]);
                }
                else if(arg.equals("-s")) {
                    seed = Integer.parseInt(args[i++]);
                }
                else if(arg.equals("-h")) {
                    nHdrThreads = Integer.parseInt(args[i++]);
                    assert(nHdrThreads< nThreads);
                }
                else if(arg.equals("-o")) {
                    nSigThreads = Integer.parseInt(args[i++]);
                    assert(nSigThreads< nThreads);
                }
                else if(arg.equals("-d")) {
                    splitTx = Boolean.parseBoolean(args[i++]);
                }
                else if(arg.equals("-i")) {
                    nestPIA = Boolean.parseBoolean(args[i++]);
                }
                else {
                    System.out.println("Non-option argument: " + args[i]);
                    System.exit(-1);
                }
            }
        }

        if(poolSize == 0 ) poolSize = 2 * nThreads;

        OutputBlock.setClosedNesting(nestLog);
        if(nThreads == 1) poolSize = nPackets*nFragmentsPerPacket;
        long start, stop, diff;
        PKI pki = new PKI(nSenders);
        Firewall firewall = new Firewall(true);
        PacketPool packetPool = new PacketPool(poolSize); /*was: nPackets*nFragmentsPerPacket*/
        StateInfo stateInfo = new StateInfo();
        ScoreBoard scoreBoard = new ScoreBoard();
        MatchingQueue matchingQueue = new MatchingQueue(nPackets);
        PacketGenerator generator = new PacketGenerator(seed,csProb,sigProb,pki,scoreBoard);
        OutputBlock outputBlock = new OutputBlock();
        AttackDictionary dictionary = new AttackDictionary(50,new Random(1234),1,6);
        ExecutorService threadPool;
        ExecutorService initThreadPool = Executors.newFixedThreadPool(nInitThreads);
        ExecutorService prducersThreadPool;
        ArrayList<ArrayList> allPackets = new ArrayList<>();
        ArrayList<ArrayList> somePackets = new ArrayList<>();
        long[] abortCounter = new long[nFragmentsPerPacket*nPackets+1]; for (int j=0 ; j <= nFragmentsPerPacket ; j++) abortCounter[j] = 0;
        start = System.currentTimeMillis();

        // Init fragment pool

        if(nThreads == 1)
        {
            nProducingThreads = 1;
            threadPool = Executors.newFixedThreadPool(nThreads);
        }
        else {
            if (nProducingThreads == 0 || nProducingThreads >= nThreads) nProducingThreads = max(1,nThreads/2);
            threadPool = Executors.newFixedThreadPool(nThreads - nProducingThreads);
        }
        prducersThreadPool = Executors.newFixedThreadPool(nProducingThreads);

        for(int j = 0 ; j <nInitThreads ; j++)
        {
            somePackets.add(new ArrayList<>());
        }

        for(int j = 0 ; j <nInitThreads ; j++)
        {int pPt = nPackets / nInitThreads;
            if(j < nPackets % nInitThreads) pPt++;
            initThreadPool.execute(new ParallelGenerator(generator,pPt,nFragmentsPerPacket,somePackets.get(j)));
        }


        initThreadPool.shutdown();
        initThreadPool.awaitTermination(1, TimeUnit.HOURS);

        for(int j = 0 ; j <nInitThreads ; j++)
        {
            for(Object list : somePackets.get(j))
            {
                allPackets.add((ArrayList)list);

            }
        }

        if (splitTx) {
            if (nSigThreads == 0)
            {
                if(nHdrThreads > 0) nSigThreads = nThreads - nHdrThreads;
                else
                {
                    nSigThreads = nThreads / 2;
                    nHdrThreads = nThreads - nSigThreads;
                }
            }
            else
            {
                if(nHdrThreads == 0) nHdrThreads = nThreads - nSigThreads;
            }
            if(nThreads == 1 )
            {
                nSigThreads = 1;
                nHdrThreads = 1;
            }
            int totalNFragments = nFragmentsPerPacket * nPackets;
            int[] packetDist = new int[nHdrThreads];
            int[] sigDist = new int[nSigThreads];
            for (i = 0; i < nHdrThreads; i++) {
                packetDist[i] = totalNFragments / nHdrThreads;
                if (i < totalNFragments % nHdrThreads) {
                    packetDist[i] += 1;
                }
            }

            for (i = 0; i < nSigThreads; i++) {
                sigDist[i] = nPackets / nSigThreads;
                if (i < nPackets % nSigThreads) {
                    sigDist[i] += 1;
                }
            }

            start = System.currentTimeMillis();
            // run processing threads

            for (i = 0; i < nHdrThreads; i++)
                threadPool.execute(new HeaderExtractor(firewall, packetPool, stateInfo, matchingQueue, packetDist[i]));
            for (i = 0; i < nSigThreads; i++)
                threadPool.execute(new SignatureMatcher(pki, outputBlock, dictionary, matchingQueue, sigDist[i]));

        }
        else
        {
            int totalNFragments = nFragmentsPerPacket * nPackets;
            SignatureMatcher matcher = new SignatureMatcher(pki,outputBlock,dictionary);


            start = System.currentTimeMillis();
            if(nThreads == 1)
            {
                MyProducer prod;
                for (i = 0; i < nPackets; i++){
                    prod = new MyProducer(packetPool,allPackets,i);
                    prod.run();
                }
                HeaderExtractor ext = new HeaderExtractor(firewall, packetPool, stateInfo, matchingQueue, /*packetDist[i]*/totalNFragments ,matcher, abortCounter,nestPIA);
                ext.run();
            }
            else {

                for (i = 0; i < nPackets; i++)
                    prducersThreadPool.execute(new MyProducer(packetPool, allPackets, i));
                for (i = 0; i < totalNFragments; i++)
                    threadPool.execute(new HeaderExtractor(firewall, packetPool, stateInfo, matchingQueue, /*packetDist[i]*/1, matcher, abortCounter,nestPIA));
            }
        }

        threadPool.shutdown();
        threadPool.awaitTermination(1, TimeUnit.HOURS);
        prducersThreadPool.shutdown();
        prducersThreadPool.awaitTermination(1, TimeUnit.HOURS);
        stop = System.currentTimeMillis();
        diff = stop - start;
        double abortRate = getAbortRate(nFragmentsPerPacket*nPackets,abortCounter);
        if(scoreBoard.verifyOutput(outputBlock))
        {
            System.out.println("OK\t" + diff  +"\t" + nThreads + "\t" + (nThreads - nProducingThreads) + "\t" + nPackets + "\t"
                    + nFragmentsPerPacket  + "\t" + poolSize + "\t" + abortRate);
        }
        else
        {
            System.out.println("BAD\t" + diff  +"\t" + nThreads + "\t" + (nThreads - nProducingThreads) + "\t" + nPackets + "\t"
                    + nFragmentsPerPacket  + "\t" + poolSize + "\t" + abortRate);
        }


    }

    private static double getAbortRate(int nfrags, long[] counter)
    {
        int attmpts = 0;
        for(int i = 0 ; i < nfrags ; i++)
        {
            attmpts += counter[i];
        }
        return 1- nfrags / (double) attmpts;
    }


    private static class MyProducer implements Runnable {
        PacketPool packetPool;
        ArrayList<ArrayList> allPackets;
        int idx;

        public MyProducer(PacketPool packetPool, ArrayList<ArrayList> allPackets, int idx) {
            this.packetPool = packetPool;
            this.allPackets = allPackets;
            this.idx = idx;
        }



        @Override
        public void run() {
            for(Object p: allPackets.get(idx))
                packetPool.put((Packet) p);
        }
    }
}
