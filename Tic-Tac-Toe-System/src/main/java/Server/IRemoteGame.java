package Server;
import Client.IRemoteClient;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * Author: Yiran Wang <yirwang10@student.unimelb.edu.au>
 * Student ID: 1366272
 */
public interface IRemoteGame extends Remote {
    void setupPlayer(String username, IRemoteClient iClient)  throws RemoteException;
    void play(String username, int x, int y) throws RemoteException;
    String getGameStatus(String username) throws RemoteException;
    void playerLeave(String username) throws RemoteException;
    void broadcastMsg(String username, String msg)  throws RemoteException;
    void sendHeartbeat(String username) throws RemoteException;
    boolean isGameReady(String username) throws RemoteException;
    boolean isCurrentPlayer(String username) throws RemoteException;
    void playNextGame(String username, boolean play) throws RemoteException;
    Map<String, String> getBoard(String username) throws RemoteException;
    boolean hasBoard(String username) throws RemoteException;
    void makeOtherWin(String username) throws RemoteException;





}
