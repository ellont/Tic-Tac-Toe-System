package Client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
/**
 * Author: Yiran Wang <yirwang10@student.unimelb.edu.au>
 * Student ID: 1366272
 */
public interface IRemoteClient extends Remote {

    void JoinGame() throws RemoteException;
    void sendMsg(String msg)  throws RemoteException;
    void displayChat(List<String> chatHistory) throws RemoteException;
    String getGameStatus() throws RemoteException;

    void displayResult(String result) throws RemoteException;
    void updateBoard(int x, int y, String symbol) throws RemoteException;
    public void promptRandonMove()throws RemoteException;
    void setCountdown(int time) throws RemoteException;
    void popUpServerMessage(String msg)  throws RemoteException;
    void getServerMsg(String msg) throws RemoteException;
    void showBoard() throws RemoteException;


}
