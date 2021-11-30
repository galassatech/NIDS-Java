

import java.util.ArrayList;

public class StateInfo {
    /**
     *
     * Fragments are inserted to Map after headers were processed
     * Map maps from flowIds to collections of fragments
     */
    LinkedList map = new LinkedList(); // TODO: Replace LinkedList with your own data structure

    public void put(ProcessedPacket fragment)
    {
        int id = fragment.id;
        LinkedList fragments = (LinkedList) map.get(id);
        if(fragments == null)
        {
//            System.out.println("p1");
            fragments = new LinkedList();
            fragments.put(fragment.fragNum,fragment);
            map.put(id,fragments);
//            return;
        }
        else
        {
            fragments.put(fragment.fragNum,fragment);
        }

    }

    public boolean complete(Integer id)
    {
        if(map.containsKey(id))
        {
            LinkedList fragments = (LinkedList) map.get(id);
            Packet p = (Packet)fragments.get(1);
            if(p != null) {
                for (int i = 2; i <= p.totalFragments ; i++)
                {
                    if(!fragments.containsKey(i)) return false;
                }
                return true;
            }
            else
                return false;
        }
        else {
            return false;
        }
    }

    public ArrayList<ProcessedPacket> getFragments(Integer id)
    {
        if(map.containsKey(id)) {
            LinkedList fragments = (LinkedList) map.get(id);
            ProcessedPacket p = (ProcessedPacket) fragments.get(1);
            if (p != null) {
                ArrayList<ProcessedPacket> packets = new ArrayList<>();
                packets.add(p);
                for (int i = 2; i <= p.totalFragments; i++) {
                    p = (ProcessedPacket) fragments.get(i);
                    packets.add(p);
                }
                return packets;
            } else {
                return null;
            }
        }
        return null;
    }

    public void remove(Integer id)
    {
        map.remove(id);
    }


}
