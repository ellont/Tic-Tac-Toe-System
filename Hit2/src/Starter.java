import java.util.ArrayList;

public class Starter {
    private int[] locationCells;
    private ArrayList<Integer> locationList = new ArrayList<>();
    private int body;

    
    public ArrayList<Integer> setList(){
        body = (int) (Math.random() * 5);
        locationList.add(body);
        locationList.add(body+1);
        locationList.add(body+2);
        return locationList;
    }
    public ArrayList<Integer> getList(){
        return locationList;
    }
    // public void setLocations(int[] locs)
    // {
    //     locationCells = locs;
    // }

    // public int[] getLocations() {
    //     return locationCells;
    // }

    public String checkHit(int guess)
    {
        String result = "miss";
        if (locationList.contains(guess))
        {
            result = "hit";
            locationList.remove(Integer.valueOf(guess));
        }
        return result;
    }
        //if len of locationCells < 1, then win
        public boolean win()
        {
            boolean win = locationList.isEmpty();
            return win;
        }

    //consolidate the new location arrayList
    // public void newLoc(String result,int guess)
    // {
    //     if (result == "hit")
    //     {
    //         int[] newLocation = new int[getLocations().length-1];
    //         int index = 0;
    //         for (int i = 0; i < locationCells.length; i++)
    //         {
    //             if (guess != locationCells[i])
    //             {
    //                 newLocation[index] = locationCells[i];
    //                 index++;
    //             }
    //         }
    //         setLocations(newLocation);
    //         getLocations();
    //     }

    // }




}
