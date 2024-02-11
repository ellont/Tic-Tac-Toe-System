import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class MyObject {
    private AtomicInteger points;
    private AtomicInteger rank;

    public MyObject(int points) {
        this.points = new AtomicInteger(points);
        this.rank = new AtomicInteger(0);
    }

    public int getPoints() {
        return points.get();
    }

    public void setPoints(int points) {
        this.points.set(points);
    }

    public int getRank() {
        return rank.get();
    }

    public void setRank(int rank) {
        this.rank.set(rank);
    }
}

public class RankUpdater {
    public static void main(String[] args) {
        // Create a concurrent data structure to store objects
        Map<String, MyObject> objectMap = new ConcurrentHashMap<>();

        // Populate the map with objects (replace this with your own data)
        objectMap.put("Object1", new MyObject(50));
        objectMap.put("Object2", new MyObject(30));
        objectMap.put("Object3", new MyObject(70));
        System.out.println(objectMap.entrySet());

        // Perform some updates to points (replace this with your own logic)
        for (MyObject obj : objectMap.values()) {
            obj.setPoints(obj.getPoints() + 10); // Example: increase points by 10
        }

        // Update the rank based on the updated points
        updateRank(objectMap);

        // Print the updated ranks
        for (Map.Entry<String, MyObject> entry : objectMap.entrySet()) {
            System.out.println(entry.getKey() + ": Points=" + entry.getValue().getPoints() + ", Rank=" + entry.getValue().getRank());
        }
    }

    private static void updateRank(Map<String, MyObject> objectMap) {
        List<MyObject> objects = new ArrayList<>(objectMap.values());
        objects.sort(Comparator.comparingInt(MyObject::getPoints).reversed());

        for (int i = 0; i < objects.size(); i++) {
            objects.get(i).setRank(i + 1);
        }
    }
}
