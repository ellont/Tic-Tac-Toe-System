package Server;
import javax.swing.*;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.ConnectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;

/**
 * Author: Yiran Wang <yirwang10@student.unimelb.edu.au>
 * Student ID: 1366272
 */
public class Server {
    private static int port;
    private static String ip;

    public static void main(String[] args)  {
        String[] command = args;//{"localhost","2000"}; //
        if (isValidCommand(command)){
            ip = command[0];
            port = Integer.parseInt(command[1]);

            Registry registry;
            try {
                IRemoteGame remoteGame = new RemoteGame();
                if (ip.equals("localhost") || ip.equals("127.0.0.1")){
                    registry = LocateRegistry.createRegistry(port);
                    System.out.println("Registry created.");

                } else {
                    registry = LocateRegistry.getRegistry(ip, port);
                }
                registry.bind("TicTacToe", remoteGame);

//            registry = LocateRegistry.createRegistry(port);
//            registry.rebind("TicTacToe", remoteGame);
//            System.out.println("Game server ready " + registry.toString());
//            try {
//                //System.setProperty("java.rmi.server.hostname", "YOUR_DESIRED_IP");
//
//                //registry = LocateRegistry.getRegistry(port);
//
//
//            } catch (Exception e) {
//                // Existing registry not found, so create a new one
//                registry = LocateRegistry.createRegistry(port);
//                System.out.println("2nd create");
//            }
                System.out.println("Bind TicTacToe Successfully.");

            }
            catch (ExportException e ){
                JOptionPane.showMessageDialog(null, "Export failed. Port already in use.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            catch (AccessException e){
                JOptionPane.showMessageDialog(null, "RMI Access failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            catch (AlreadyBoundException e){
                JOptionPane.showMessageDialog(null, "RMI registry already bound.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            catch(ConnectException e){
                JOptionPane.showMessageDialog(null, "Connection failed", "Error", JOptionPane.ERROR_MESSAGE);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isTwoArgs(String[] args){
        boolean isValid = false;
        if (args.length != 2){
            JOptionPane.showMessageDialog(null,
                    "Please follow the correct command format: java -jar Server.jar ip port" );
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


    private static boolean isValidCommand(String[] args){
        if(isTwoArgs(args)){
            return portIsValid(args[1]);
        } else {
            return false;
        }
    }
}
