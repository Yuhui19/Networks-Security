import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Router {

    private HashMap<Router, Integer> distances;
    private String name;
    public Router(String name) {
        this.distances = new HashMap<>();
        this.name = name;
    }

    public void onInit() throws InterruptedException {

        //TODO: IMPLEMENT ME
        //As soon as the network is online,
        //fill in your initial distance table and broadcast it to your neighbors

        HashSet<Neighbor> neighbors = Network.getNeighbors(this);

        for (Neighbor neighbor : neighbors) {
            distances.put(neighbor.router, neighbor.cost);
        }

        for (Neighbor neighbor : neighbors) {
            Message message = new Message(this, neighbor.router, distances);
            Network.sendDistanceMessage(message);
        }
    }

    public void onDistanceMessage(Message message) throws InterruptedException {
        //update your distance table and broadcast it to your neighbors if it changed

        boolean changed = false;
        int distanceToSender = distances.get(message.sender);

        for (Map.Entry<Router, Integer> entry : message.distances.entrySet()) {
            Router router = entry.getKey();
            Integer distance = entry.getValue();
            if (router == this) {
                continue;
            }
            if (!distances.containsKey(router)) {
                distances.put(router, distanceToSender + distance);
                changed = true;
            }
            if (distances.containsKey(router)) {
                int presentDistance = distances.get(router);
                int newDistance = distanceToSender + distance;
                if (newDistance < presentDistance) {
                    distances.put(router, newDistance);
                    changed = true;
                }
            }
        }

        if (changed) {
            HashSet<Neighbor> neighbors = Network.getNeighbors(this);
            for (Neighbor neighbor : neighbors) {
                if (neighbor.router != message.sender) {
                    Message newMessage = new Message(this, neighbor.router, distances);
                    Network.sendDistanceMessage(newMessage);
                }
            }
        }
    }


    public void dumpDistanceTable() {
        System.out.println("router: " + this);
        for(Router r : distances.keySet()){
            System.out.println("\t" + r + "\t" + distances.get(r));
        }
    }

    @Override
    public String toString(){
        return "Router: " + name;
    }
}