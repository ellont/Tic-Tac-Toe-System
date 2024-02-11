import java.util.Scanner;

public class GameHelper {
	
	public int getUserInput(String prompt) {
		System.out.print(prompt);
		Scanner scanner = new Scanner(System.in);
		return scanner.nextInt();
		}
	
	public void printArr (int[] newLoca)
	{
		for (int x = 0; x < newLoca.length; x++)
		{
			System.out.println(newLoca[x]);
		}
	
	}
}
