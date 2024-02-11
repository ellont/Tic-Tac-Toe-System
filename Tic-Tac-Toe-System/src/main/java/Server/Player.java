package Server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * Author: Yiran Wang <yirwang10@student.unimelb.edu.au>
 * Student ID: 1366272
 */
public class Player {
    public static final int START_POINT = 0;
    public static final String ACTIVE = "ACTIVE";
    public static final String INACTIVE = "INACTIVE";
    public static final String DISCONNECTED = "DISCONNECTED";
    private String username;
    private AtomicInteger points;
    private AtomicInteger rank;
    private String symbol;
    //private AtomicLong lastHeartbeat;
    private GameBoard board;
    private String status;
    private static ConcurrentHashMap<String, Player> players = new ConcurrentHashMap<>(); //only has method to add and get, no other modifications

    // Constructor for player
    protected Player(String username){
        this.username = username;
        this.points = new AtomicInteger(START_POINT);
        this.rank = new AtomicInteger(-1);
        this.symbol = null;
        //this.lastHeartbeat = new AtomicLong(System.currentTimeMillis());
        players.putIfAbsent(username, this);
        this.board = null;
        this.status = ACTIVE;
    }

    protected String getUsername() {
        return username;
    }
    protected synchronized String getStatus(){
        return status;
    }

    protected synchronized void setStatus(String status){
        this.status = status;
    }

//    protected static synchronized Map<String, Player> getPlayersList(){
//        Map<String, Player> serializedPlayer = new HashMap<>();
//        for (Map.Entry<String, Player> entry : players.entrySet()) {
//            String key = entry.getKey();
//            Player value = entry.getValue();
//            serializedPlayer.put(key, value);
//        }
//        return serializedPlayer;
//    }


    protected synchronized void updatePointsAndRank(int point){
        points.addAndGet(point); //update the points
        updateRank(); // update the rank
    }

    protected String getSymbol() {
        return symbol;
    }


    protected void setSymbol(String symbol){
        this.symbol = symbol;
    }
    protected GameBoard getBoard(){
        return board;
    }
    protected void setBoard(GameBoard board){
        this.board = board;
    }

//    protected int getRank(){
//        return rank.get();
//    }
    protected void setRank(int newRank){
        rank.set(newRank);
    }

    protected void resetForNewGame(){
        this.symbol = null;
        this.board = null;
        this.status = ACTIVE;
        updatePointsAndRank(START_POINT);
    }

    protected void resumeGameSetup(){
        this.status = ACTIVE;//update lasthearbeat

    }

    //check if the player is a new player or not
    protected static boolean isExist(String username){
        return players.containsKey(username);
    }

    protected static Player getPlayer(String username){
        return players.get(username);
    }

    protected String playerRankAndName(){
        return "Rank#"+rank+" "+username;
    }

    private void updateRank(){
        List<Player> playerList = new ArrayList<>(players.values());
        playerList.sort(Comparator.comparingInt(Player::getPoints).reversed());
        for (int i = 0; i < playerList.size(); i++){
            // update the rank for this player instance only
            if (playerList.get(i).getUsername().equals(this.username)){
                this.setRank(i+1);
            }
        }
    }
    protected int getPoints() {
        return points.get();
    }

}
