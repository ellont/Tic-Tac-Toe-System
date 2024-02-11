
public class SimpleStartupGame {
	public static void main(String[] args) {
		int numOfGuess = 0;

		// Create an helper object, and a startup object
		GameHelper helper = new GameHelper();
		Startup startup = new Startup();

		// build a startup (location 0-5)
		int body = (int) (Math.random() * 5);
		int[] locations = {body, body + 1, body + 2};
		// set locations
		startup.setLocations(locations);
		// get locations and work on the getlocations
		startup.getLocations();

		// len at the starting point
		int len = startup.getLocations().length;
		// while win is false
		while (!startup.win(len)) {
			int guess = helper.getUserInput("What's your guess: ");
			numOfGuess++;
			String result = startup.checkHit(guess);
			System.out.println(result);
			if (result == "hit") {
				len--;
			}
			//consolidate locations and give a newloc array
			startup.newLoc(result, guess);
		}
		System.out.println("win");
		System.out.println("You guessed "+ numOfGuess + " times");


	}

}
