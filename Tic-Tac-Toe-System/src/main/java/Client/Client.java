package Client;
import java.rmi.RemoteException;
import java.util.Scanner;

import javax.swing.*;

/**
 * Author: Yiran Wang <yirwang10@student.unimelb.edu.au>
 * Student ID: 1366272
 */
public class Client {
    private static int port;
    private static String ip;
    private static String username;

    public static void main(String[] args) {
        // Get username
//        Scanner input = new Scanner(System.in);
//        System.out.print("Your name: ");
//        String username = input.nextLine(); //args[0]
        String[] command = args;//{username, "localhost", "2000"}; //
        if (isValidCommand(command)){
            username = command[0];
            ip = command[1];
            port = Integer.parseInt(command[2]);

            try {
                RemoteClient remoteClient = new RemoteClient(username, ip, port);
                remoteClient.JoinGame();
            }
            catch(RemoteException e){
                JOptionPane.showMessageDialog(null, "Server unavailable.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    private static boolean isThreeArgs(String[] args){
        boolean isValid = false;
        if (args.length != 3){
            JOptionPane.showMessageDialog(null,
                    "Please follow the correct command format: java -jar Client.jar username server_ip server_port" );
        } else {
            isValid = true;
        }
        return isValid;
    }
    private static boolean portIsValid(String arg){
        int port;
        boolean isValid = false;
        try {
            port = Integer.parseInt(arg);
            if(port > 1024 && port <= 65535) {
                isValid = true;
            } else {
                JOptionPane.showMessageDialog(null,
                        "Error: Invalid port number." );
            }
        } catch(NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "Error: Invalid port number." );
        }
        return isValid;
    }

    private static boolean isValidName(String arg){
        boolean isValid = false;
        if (arg.isBlank()){
            JOptionPane.showMessageDialog(null,
                    "Error: Username cannot be blank." );
        } else {
            isValid = true;
        }
        return isValid;
    }


    private static boolean isValidCommand(String[] args){
        if(isThreeArgs(args)){
            return portIsValid(args[2]) && isValidName(args[0]);
        } else {
            return false;
        }
    }

}
