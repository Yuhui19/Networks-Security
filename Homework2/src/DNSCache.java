import java.util.ArrayList;
import java.util.HashMap;

public class DNSCache {

    private static HashMap<DNSQuestion, DNSRecord> cache=new HashMap<>();

    public static void insertCache(DNSQuestion q, DNSRecord r){
        cache.put(q,r);
    }

    public static boolean containsDomain(DNSQuestion q){
        if(cache.containsKey(q)){
            DNSRecord current= cache.get(q);

            if(current.timestampValid())
                return true;
            else{
                cache.remove(q);
                return false;
            }
        }
        return false;
    }

    public static ArrayList<DNSRecord> query(DNSQuestion q){
        ArrayList<DNSRecord> output=new ArrayList<>();
        output.add(cache.get(q));
        return output;
    }
}
