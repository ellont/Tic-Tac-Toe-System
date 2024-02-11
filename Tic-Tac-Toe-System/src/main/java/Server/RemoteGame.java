package Server;
import Client.IRemoteClient;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Author: Yiran Wang <yirwang10@student.unimelb.edu.au>
 * Student ID: 1366272
 */
public class RemoteGame extends UnicastRemoteObject implements IRemoteGame {
    public static final String ONGOING = "ONGOING";
    public static final String ACTIVE = "ACTIVE";
    public static final String INACTIVE = "INACTIVE";
    public static final String PAUSED = "PAUSED";

    private final ConcurrentHashMap<Player, IRemoteClient> playerList; // store Player object, IRemoteClient object
    private final ConcurrentHashMap<Player, AtomicInteger> playersHeartbeat;
    //private final ConcurrentHashMap<GameBoard, ScheduledExecutorService> gameTimers;
    private ConcurrentLinkedQueue<Player> waitingUsers;


    protected RemoteGame() throws RemoteException {
        this.playerList = new ConcurrentHashMap<>();
        this.waitingUsers = new ConcurrentLinkedQueue<>();
        this.playersHeartbeat = new ConcurrentHashMap<>();
        //this.gameTimers = new ConcurrentHashMap<>();

        matchingUsers();
        checkHeartbeat();
    }
    @Override //Initial set up for the player
    public void setupPlayer(String username, IRemoteClient iClient) throws RemoteException {
        System.out.println(username+" has connected.");
        iClient.displayResult("Connected to server.");

        Player player = null;
        if(Player.isExist(username)){ //player exist
            //Existing player
            //System.out.println(username+" exists");
            player = Player.getPlayer(username);
            player.setStatus(Player.ACTIVE);
            // Update remote client object in playerList
            if (playerList.containsKey(player)){
                playerList.replace(player, iClient);
            } else {
                playerList.putIfAbsent(player, iClient);
            }

            if (player.getBoard() != null) {
                String boardStatus = player.getBoard().getStatus();
                switch (boardStatus){
                    case GameBoard.ACTIVE:
                        System.out.println("Error:"+player.getUsername()+" board is active");
                        break;
                    case GameBoard.PAUSED:
//                        System.out.println("crashed planer");
//                        System.out.println(username+" is "+player.getStatus());
                        player.resumeGameSetup();
//                        System.out.println(player.getBoard()+" was "+player.getBoard().getStatus());
                        player.getBoard().setStatus();
//                        System.out.println(player.getBoard()+" is "+player.getBoard().getStatus());
                        playerList.get(player).showBoard();
                        updateMstForPlayers(player.getBoard(), currentPlayerMsg(player.getUsername()));
                        //enableBoard(player);
                        redisplayChat(player.getUsername());
                        //deploy the board
                        break;
                    case GameBoard.INACTIVE:
                        System.out.println(username+" exists, game inactive");
                        player.resetForNewGame();
                        waitingUsers.offer(player);
                        playerList.get(player).displayResult("Finding player...");
                }
            }else { // no
                player.resetForNewGame();
                waitingUsers.offer(player);
                playerList.get(player).displayResult("Finding player...");
            }

        } else { //player doesn't exist
            //System.out.println(username+" is a new player");
            player = new Player(username);
            playerList.putIfAbsent(player, iClient); //add player to the player list
            waitingUsers.offer(player); //add player to the waiting list
            playerList.get(player).displayResult("Finding player...");
        }

        System.out.println(waitingUsers.size()+" waiting players");
        //return true;
    }

    private void matchingUsers() {
        new Thread(() -> {
            while (true) {
                if (waitingUsers.size() >= 2) {
                    // remove the head of the queue
                    Player player1 = waitingUsers.poll();
                    Player player2 = waitingUsers.poll();
                    System.out.println(player1.getUsername()+" and "+player2.getUsername()+" are matched. Setting up the game.");

                    GameBoard board = new GameBoard(player1, player2, this);
                    for (Player player : board.getPlayers()){
                        if (player != null && player.getStatus().equals(Player.ACTIVE)){
                            player.updatePointsAndRank(0);
                            updateMstForPlayers(board, currentPlayerMsg(player.getUsername()));
                            //enableBoard(player);
                        }
                    }
                    startGameTimer(board);
                } else {
                    // Sleep for a while before trying to match again
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println("RemoteGame - matchingUsers: Thread Interrupted");
                    }
                }
            }
        }).start();
    }


    @Override
    public synchronized boolean isGameReady(String username) throws RemoteException {
        Player player = Player.getPlayer(username);
        if (player == null || player.getBoard() == null){
            return false;
        } else {//check if the board is active
            return Objects.equals(player.getStatus(), Player.ACTIVE) &&
                    Objects.equals(player.getBoard().getStatus(), GameBoard.ACTIVE);
        }
    }

    @Override
    public void play(String username, int x, int y) throws RemoteException {
        Player player = Player.getPlayer(username);
        GameBoard board = player.getBoard();
        if (board != null && board.getStatus().equals(GameBoard.ACTIVE)) {
            if (board.getCurrentPlayer().equals(player)) {// if is the current player
                if (!board.updateBoard(player, x, y)) {
                    playerList.get(player).popUpServerMessage("SERVER: Invalid move. Try again.");
                } else { // VALID MOVE
                    // update displaying board for both players
                    for (Player p : board.getPlayers()) {
                        playerList.get(p).updateBoard(x, y, player.getSymbol());
                    }
                    // check game results
                    int result = board.checkGame(); // get the result, points is updated by the result
                    if (result == GameBoard.ONGOING) {
                        board.updateCurrentPlayer();
                        resetTimer(board);
                        updateMstForPlayers(board, currentPlayerMsg(username));
                    } else {// Game is finished
                        stopGameTimer(player.getBoard()); //stop timer
                        player.getBoard().setStatus(); // set board to inactive
                        updateMstForPlayers(board, resultMsg(result, player)); //display winner msg

                        player.getBoard().updatePlayersPointsAndRank(result);
                    }
                }
            } else { // not current player
                playerList.get(player).popUpServerMessage("SERVER: Please wait for your turn.");
            }
        } else {
            if(player!=null){
                playerList.get(player).popUpServerMessage("SERVER: Please wait for the game to start.");
            }
        }
    }


    private void promptForRandomMove(Player player) throws RemoteException{
        playerList.get(player).promptRandonMove();
    }
    private String winMsg(Player player){
        return "Player "+player.playerRankAndName()+" wins!";
    }
    private String drawMsg(){
        return "Match Drawn";
    }
    private String resultMsg(int result, Player player){
        if (result == GameBoard.PLAYER1_WIN){//won the game
            return winMsg(player.getBoard().getPlayer1());
        }
        if (result == GameBoard.PLAYER2_WIN){//player2 win the game
            return winMsg(player.getBoard().getPlayer2());
        }
        if (result == GameBoard.DRAW){//match draw
            //Match Draw
            return drawMsg();
        }
        return ONGOING;
    }

    private String currentPlayerMsg(String username){
        Player player = Player.getPlayer(username);
        return player.getBoard().getCurrentPlayer().playerRankAndName()+"'s turn. ("
                + player.getBoard().getCurrentPlayer().getSymbol()+")";
    }

    private void updateMstForPlayers(GameBoard board, String msg){
        if (board != null){
            for (Player p: board.getPlayers()){
                try {
                    if (playerList.get(p) != null){
                        playerList.get(p).displayResult(msg);
                    }
                } catch (RemoteException e){
                    System.out.println(p.getUsername()+" is offline.");
                }
            }
        }
    }

    private void updateCountdownLabel(GameBoard board, int time) throws RemoteException{
        for (Player p : board.getPlayers()){
            if (p.getStatus().equals(Player.ACTIVE)){
                playerList.get(p).setCountdown(time);
            }
        }
    }

    private void startGameTimer(GameBoard board) {//thread per request
        ScheduledExecutorService gameTimer = board.getGameTimer();

        gameTimer.scheduleAtFixedRate(() -> {
            try{
                switch (board.getStatus()){
                    case ACTIVE:
                        updateCountdownLabel(board, board.getMoveTime());
                        board.decreseTimer();
                        if (board.getMoveTime() < 0) {
                            promptForRandomMove(board.getCurrentPlayer());
                            board.resetTimer();
                        }
                        break;
                    case PAUSED:
                        //do nothing
                        break;
                    case INACTIVE:
                        gameTimer.shutdownNow();
                        break;
                }
            } catch(RemoteException e){
                System.out.println("Players offline.");
                //gameTimer.shutdownNow();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void resetTimer(GameBoard board){
        board.resetTimer();
    }
    private void stopGameTimer(GameBoard board){
        board.getGameTimer().shutdownNow();
//        gameTimers.remove(board);
    }

    @Override
    public boolean isCurrentPlayer(String username) throws RemoteException { // check if game is still active
        return Player.getPlayer(username).getBoard().getCurrentPlayer().getUsername().equals(username);
    }

    @Override
    public Map<String, String> getBoard(String username) throws RemoteException { // check if game is still active
        Player player = Player.getPlayer(username);
        return player.getBoard().getSerializableBoard();
    }

    private void checkHeartbeat() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                synchronized (playersHeartbeat) {
                    if(playersHeartbeat.size() > 0){//when there is active players
                        for (Player player : playersHeartbeat.keySet()) {
                            AtomicInteger currentTime = playersHeartbeat.get(player);
                            if (currentTime.get() < 3){ // player sends heartbeat every second, so long it's < 3secs, its alive
                                player.setStatus(Player.ACTIVE);
                                currentTime.incrementAndGet();
                                playersHeartbeat.put(player, currentTime);
                            }
                            if (currentTime.get() >= 3 && currentTime.get() <= 30) {  // no heartbeat after 3sec, player is disconnected
                                currentTime.incrementAndGet();
                                playersHeartbeat.put(player, currentTime);
                                player.setStatus(Player.DISCONNECTED);
                                System.out.println("Client " + player.getUsername() + " has "+player.getStatus()+" "+currentTime+"sec.");
                                if (player.getBoard() != null && !player.getBoard().getStatus().equals(GameBoard.INACTIVE)){//player has a game yet
                                    player.getBoard().setStatus(); // update board status
                                } else {
                                    player.setStatus(Player.INACTIVE);
                                    player.setBoard(null);
                                    playersHeartbeat.remove(player);
                                    waitingUsers.remove(player);
                                }
                            }
                            if (currentTime.get() > 30){
                                System.out.println("Client " + player.getUsername() + " is inactive.");
                                player.setStatus(Player.INACTIVE);
                                handleInactivePlayerInGame(player);

//                                if (!player.getBoard().getStatus().equals(GameBoard.INACTIVE)){//If the other player has not left yet
//                                    player.getBoard().updatePlayersPointsAndRank(GameBoard.DRAW); //make draw
//                                    updateMstForPlayers(player.getBoard(), drawMsg());
//                                }
                                player.getBoard().setStatus();
                                playersHeartbeat.remove(player);
                            }
                        }
                    }
                }

            }
        }, 0, 1000);
    }

    private void handleInactivePlayerInGame(Player player){
        player.setStatus(Player.INACTIVE);
        GameBoard board = player.getBoard();
        if (player.equals(board.getPlayer1())){
            if (!board.getPlayer2().getStatus().equals(Player.INACTIVE)){
                player.getBoard().updatePlayersPointsAndRank(GameBoard.DRAW); //make draw
                updateMstForPlayers(player.getBoard(), drawMsg());
            }
        } else {
            if (!board.getPlayer1().getStatus().equals(Player.INACTIVE)){
                player.getBoard().updatePlayersPointsAndRank(GameBoard.DRAW); //make draw
                updateMstForPlayers(player.getBoard(), drawMsg());
            }
        }
    }


    @Override
    public void sendHeartbeat (String username) throws RemoteException{
        playersHeartbeat.put(Player.getPlayer(username), new AtomicInteger(0));
        //System.out.println(username+"'s board status: "+Player.getPlayer(username).getBoard().getStatus());
    }

    @Override
    public void playNextGame(String username, boolean play) throws RemoteException {
        if (play){
            Player player = Player.getPlayer(username);
            player.resetForNewGame();
            if(!waitingUsers.contains(player)){
                waitingUsers.offer(player);
            }
        } else {
            playerLeave(username);
        }
    }
    @Override
    public boolean hasBoard(String username) throws RemoteException {
        Player player = Player.getPlayer(username);
        if (player != null && player.getBoard() != null){
            return true;
        }
        return false;
    }



    @Override
    public synchronized String getGameStatus(String username) throws RemoteException {
        return Player.getPlayer(username).getBoard().getStatus();
    }

    @Override
    public synchronized void playerLeave(String username) throws RemoteException { //quit game
        Player player = Player.getPlayer(username);
        GameBoard board = player.getBoard();
        player.setStatus(Player.INACTIVE);
        //inform the other player
        if (board != null && board.getStatus().equals(ACTIVE)){
            for (Player p : player.getBoard().getPlayers()){
                if(!p.equals(player) && p.getStatus().equals(Player.ACTIVE)){
                    playerList.get(p).getServerMsg("SERVER: "+username + " has left the game.");//popUpServerMessage(username + " has left the game.");
                }
            }
            System.out.println(username+" left.");
            player.getBoard().setStatus();
        }
        player.setBoard(null);
        //remove player from all lists
        playersHeartbeat.remove(player);
        playerList.remove(player);
        waitingUsers.remove(player);
    }

    @Override
    public void makeOtherWin(String username) throws RemoteException{
        Player player = Player.getPlayer(username);
        GameBoard board = player.getBoard();
        if (board != null && !board.getStatus().equals(GameBoard.INACTIVE)){
            if(player.equals(board.getPlayer1())){
                updateMstForPlayers(board,winMsg(board.getPlayer2()));
                board.updatePlayersPointsAndRank(GameBoard.PLAYER2_WIN);
            } else {
                updateMstForPlayers(board,winMsg(board.getPlayer1()));
                board.updatePlayersPointsAndRank(GameBoard.PLAYER1_WIN);
            }
        }
    }


    @Override
    public synchronized void broadcastMsg(String username, String msg) throws RemoteException {
        Player player = Player.getPlayer(username);
        GameBoard board = player.getBoard();
        if(board != null && board.getStatus().equals(GameBoard.ACTIVE)){
            board.storeChat(player,msg);
            for (Player p : board.getPlayers()){
                playerList.get(p).displayChat(board.getChatHistory());
            }
        } else {
            playerList.get(player).popUpServerMessage("SERVER: Chat disabled. Please wait for the game to start");
        }
    }

    private void redisplayChat(String username) throws RemoteException {
        Player player = Player.getPlayer(username);
        GameBoard board = player.getBoard();
        playerList.get(player).displayChat(board.getChatHistory());
    }


}
