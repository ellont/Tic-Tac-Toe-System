package Client;

import Server.IRemoteGame;

import javax.swing.*;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * Author: Yiran Wang <yirwang10@student.unimelb.edu.au>
 * Student ID: 1366272
 */
public class RemoteClient extends UnicastRemoteObject implements IRemoteClient {
    private String username;
    private String ip;
    private int port;
    private IRemoteGame remoteGame;
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);
    private ClientGUI gui;
    public static final String SERVER_UNAVAILABLE = "Server unavailable.";


    protected RemoteClient(String username, String ip, int port) throws RemoteException{
        this.username = username;
        this.ip = ip;
        this.port = port;
        this.gui = new ClientGUI(this);
    }

    @Override
    public void JoinGame() throws RemoteException {
        try {
            //Connect to the rmiregistry that is running on localhost
            Registry registry = LocateRegistry.getRegistry(port);
            String[] boundNames = registry.list();
            for (String name : boundNames){
                System.out.println(name);
            }
            //Retrieve the stub/proxy for the remote math object from the registry
            this.remoteGame = (IRemoteGame) registry.lookup("TicTacToe");
            gui.startGUI();
            //gui.disableButtons();
            gui.setStatusLabel("Connecting to Server");
            remoteGame.setupPlayer(username, this);

            sendHeartbeat(); // a separate thread to send heartbeat periodically
            //checkGameStatus(); // a separate thread to check
            //gui.setStatusLabel("Finding Player");
        }
        catch(RemoteException e){
            gracefulShutdown();
        }
        catch(Exception e) {
            gracefulShutdown();
        }

    }

    @Override
    public String getGameStatus() throws RemoteException {
        return remoteGame.getGameStatus(username);
    }


    protected void makeMove(int x, int y){
        try {
            if(remoteGame!=null){
                remoteGame.play(username, x, y);
            } else {
                gui.popupMsg("Connecting to the server.");
            }
        } catch (RemoteException e) {
            gracefulShutdown();
        }
    }

    @Override
    public void promptRandonMove()throws RemoteException{
        gui.makeRandomMove();
    }
    @Override
    public void updateBoard(int x, int y, String symbol) throws RemoteException{
        gui.getGameBoard()[x][y].setEnabled(false);
        gui.getGameBoard()[x][y].setText(symbol);
    }


    public void showBoard() throws RemoteException{
        try {
            Map<String, String> board = remoteGame.getBoard(username);
            if (board != null){
                gui.showBoard(board);
            }
        } catch (RemoteException e) {
            gracefulShutdown();
        }
    }


    @Override
    public void displayResult(String result) throws RemoteException {
        gui.setStatusLabel(result);
    }

    @Override
    public void getServerMsg(String msg) throws RemoteException {
        gui.showInternalMsg(msg);
        //JOptionPane.showMessageDialog(null, msg);
    }

    @Override
    public void sendMsg(String msg) throws RemoteException {
        remoteGame.broadcastMsg(username, msg);
    }

    @Override
    public void displayChat(List<String> chatHistory) throws RemoteException {
        gui.setChatBox(chatHistory);
    }

    @Override
    public void popUpServerMessage(String msg) throws RemoteException {
        gui.popupMsg(msg);

    }

    @Override
    public void setCountdown(int time) throws RemoteException{
        gui.setTimerLabel(time);
    }


    protected void closeEverything(){
        try{
            System.out.println("player exit");
            remoteGame.makeOtherWin(username);
            remoteGame.playerLeave(username);
            UnicastRemoteObject.unexportObject(this, true);
            System.exit(0);
        }
        catch(RemoteException e){
            gracefulShutdown();
        }
    }

    private void sendHeartbeat() {
        AtomicInteger counter = new AtomicInteger(0);
        scheduler.scheduleAtFixedRate(() -> {
            try{
                remoteGame.sendHeartbeat(username);
                //System.out.println(counter.get()+" heartbeat sent..");
                counter.incrementAndGet();
            } catch(RemoteException e){
                gracefulShutdown();
                //
            }
        },0,1000, TimeUnit.MILLISECONDS);
    }

    protected void playNextGame(boolean play) throws RemoteException {
        remoteGame.playNextGame(username, play);
    }

    protected boolean hasBoard() throws RemoteException {
        //boolean result = false; // Default to false
        if(remoteGame == null){
            return false;
        }
        return remoteGame.hasBoard(username);
    }


    protected String getUsername(){
        return username;
    }


    protected void gracefulShutdown() {
        int delay = 5000;
        try {
            gui.popupMsg(SERVER_UNAVAILABLE);
            UnicastRemoteObject.unexportObject(this, true);
            Thread.sleep(delay);
            System.exit(0);
//            Timer timer = new Timer(false);
//            timer.schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    System.exit(0);
//                }
//            }, delay);
        } catch (NoSuchObjectException e){
            gui.popupMsg("NoSuchObjectException");
            System.exit(0);
        } catch (InterruptedException e) {
            gui.popupMsg("Thread Interrupted");
            System.exit(0);
        }
    }

}
