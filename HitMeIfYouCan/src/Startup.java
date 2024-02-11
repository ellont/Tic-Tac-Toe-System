
public class Startup {
	private int[] locationCells;
	
 	public void setLocations(int[] locs)
 	{
		locationCells = locs;
	}
 	
 	public int[] getLocations() {
 		return locationCells;
 	}
 	
	public String checkHit(int guess)
	{
		String result = "miss";
		for (int loc:locationCells)
		{
			if (guess == loc)
			{
				result = "hit";
				break;
			}
		}
		return result;
	}
		
	//consolidate the new location array, set to locationCells
	public void newLoc(String result,int guess)
	{
		if (result == "hit")
		{
			int[] newLocation = new int[getLocations().length-1];
			int index = 0;
			for (int i = 0; i < locationCells.length; i++)
			{
				if (guess != locationCells[i])
				{
					newLocation[index] = locationCells[i];
					index++;
				}
			}
			setLocations(newLocation);
			getLocations();	
		}

	}
	
	//if len of locationCells < 1, then win
	public boolean win(int leng)
	{
		boolean win = false;
		if (leng < 1)
		{
			win = true;
		}
		return win;
	}
	

}

	
