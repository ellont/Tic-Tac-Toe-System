import java.util.ArrayList;
import java.util.Arrays;

public class hit2 {
    public static void main(String[] args) {
        int numOfGuess = 0;

        // Create an helper object, and a startup object
        Helper helper = new Helper();
        Starter startup = new Starter();

        //set ArrayList for locations
        startup.setList();
        startup.getList();
        System.out.println(startup.getList());

        
        while (!startup.win()) {
            int guess = helper.getUserInput("What's your guess: ");
            numOfGuess++;
            String result = startup.checkHit(guess);
            System.out.println(result);
            System.out.println(startup.getList());

        }
        System.out.println("win");
        System.out.println("You guessed "+ numOfGuess + " times");


    }
}
