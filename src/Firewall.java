import java.util.ArrayList;

class Firewall {

    ArrayList<Rule> firewalll;
    Rule defaultRule;

    public Firewall(boolean defaultVal) {
        this.firewalll = new ArrayList<>();
        this.defaultRule = new Rule(null,null,0,0,null,defaultVal);
    }

    enum FW_MATCH{ALLOW,DENY,NO_MATCH}

    class Rule{
        byte[] srcIpAddress, destIpAddress;
        int maskFrom, maskTo;
        String protocol;
        boolean action; // allow or deny


        public Rule(byte[] srcIpAddress, byte[] destIpAddress, int maskFrom, int maskTo, String protocol, boolean action) {
            this.srcIpAddress = srcIpAddress;
            this.destIpAddress = destIpAddress;
            this.maskFrom = maskFrom;
            this.maskTo = maskTo;
            this.protocol = protocol;
            this.action = action;
        }

        public FW_MATCH checkAgainstRule(byte[] pktSrcIpAddress, byte[] pktDestIpAddress, String protocol){
            if(maskFrom == 0 && maskTo == 0)
                return (this.action)? FW_MATCH.ALLOW: FW_MATCH.DENY;
            if(matchAddress(pktSrcIpAddress,srcIpAddress,maskFrom))
            {
                if(matchAddress(pktDestIpAddress,destIpAddress,maskTo))
                {
                    if(protocol.equals(this.protocol))
                        return (this.action)? FW_MATCH.ALLOW: FW_MATCH.DENY;
                }
            }
            return FW_MATCH.NO_MATCH;
        }

        private boolean matchAddress(byte[] packet_address, byte[] rule_address, int mask ){
            String adr = "", rule="";
            for(int i = 0 ; i < 4 ; i++)
            {
                adr = adr.concat(String.format("%8s",Integer.toBinaryString(packet_address[i] & 0xff)).replace(' ', '0'));
                rule = rule.concat(String.format("%8s",Integer.toBinaryString(rule_address[i] & 0xff)).replace(' ', '0'));
            }
            for(int i = 0 ; i < mask ; i++)
            {
                if(adr.charAt(i)!=rule.charAt(i)) return false;
            }
            return true;
        }


    }


    public boolean checkRule(Packet packet)
    {
        if(1==1) return true;
        byte[] from, to;
        from = packet.l3Header.getSrcIP();
        to = packet.l3Header.getDestIP();
        String prot = packet.l4Header.getProtocol();
        for(Rule r: firewalll)
        {
            FW_MATCH res = r.checkAgainstRule(from,to,prot);
            switch (res) {
                case ALLOW:
                    return true;
                case DENY:
                    return false;
                default: continue;
            }

        }
        return (defaultRule.checkAgainstRule(from,to,prot).equals(FW_MATCH.ALLOW));
    }


}
