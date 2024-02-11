package Server;

import javax.swing.text.Position;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * Author: Yiran Wang <yirwang10@student.unimelb.edu.au>
 * Student ID: 1366272
 */
public class GameBoard {
    private Player player1;
    private Player player2;
    private IRemoteGame remoteGame;
    private Player currentPlayer;
    private ConcurrentHashMap<Position, String> board;
    private ConcurrentLinkedQueue<String> chatHistory;
    private String status;
    private ScheduledExecutorService gameTimer;
    public AtomicInteger move_time;
    public static final String X = "X"; //first turn
    public static final String O = "O";
    public static final int ROW = 3;
    public static final int COL = 3;
    public static final String BOARD_INIT = "";
    public static final int PLAYER1_WIN = 1;
    public static final int PLAYER2_WIN = 2;
    public static final int DRAW = 3;
    public static final int ONGOING = 4;
    public static final int WIN_POINTS = 5;
    public static final int LOSS_POINTS = -5;
    public static final int DRAW_POINTS = 2;
    public static final String ACTIVE = "ACTIVE";
    public static final String INACTIVE = "INACTIVE";
    public static final String PAUSED = "PAUSED";
    public static final int CHAT_HISTORY_SIZE = 10;
    public static final int TIMEOUT_TIME = 20;


    protected GameBoard(Player player1, Player player2, IRemoteGame remoteGame){
        this.remoteGame = remoteGame;
        this.board = new ConcurrentHashMap<>();
        initBoard();
        this.chatHistory = new ConcurrentLinkedQueue<>();
        this.player1 = player1;
        player1.setBoard(this);
        this.player2 = player2;
        player2.setBoard(this);
        this.currentPlayer = setCurrentPlayer(player1,player2);
        assignSymbols(player1,player2);
        this.move_time = new AtomicInteger(TIMEOUT_TIME);
        this.status = ACTIVE;
        this.gameTimer = Executors.newScheduledThreadPool(1);

    }


    protected synchronized Player getCurrentPlayer(){
        return currentPlayer;
    }

    protected ArrayList<Player> getPlayers(){
        ArrayList<Player> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        return players;
    }


    protected boolean updateBoard(Player player, int x, int y){
        Position newPos = new Position(x,y);
        //return false if the position is taken, otherwise, put player symbol then return true
        if (board.replace(newPos,BOARD_INIT, player.getSymbol())){
            return true;
        }
        return false;
    }


    protected synchronized int checkGame(){
        if (isWin(player1)){
            return PLAYER1_WIN;
        }
        if (isWin(player2)){
            return PLAYER2_WIN;
        }
        if (isBoardFilled()){
            return DRAW;
        }
        return ONGOING;
    }

    protected synchronized void updatePlayersPointsAndRank(int gameStatus){
        if (gameStatus == PLAYER1_WIN){
//            System.out.println(player1.getUsername()+" wins.");
            player1.updatePointsAndRank(WIN_POINTS);
            player2.updatePointsAndRank(LOSS_POINTS);
        }
        if (gameStatus == PLAYER2_WIN){
//            System.out.println(player2.getUsername()+" wins.");
            player1.updatePointsAndRank(LOSS_POINTS);
            player2.updatePointsAndRank(WIN_POINTS);
        }
        if (gameStatus == DRAW){
//            System.out.println(player2.getUsername()+" and "+player1.getUsername()+" draw.");
            player1.updatePointsAndRank(DRAW_POINTS);
            player2.updatePointsAndRank(DRAW_POINTS);
        }
//        for (Map.Entry<String, Player> entry : Player.getPlayersList().entrySet()) {
//            String name = entry.getKey();
//            Player player = entry.getValue();
//
//            System.out.println(name+" #"+player.getRank()+": "+player.getPoints()+" pts.");
//        }
    }

    protected synchronized void setStatus(){ //update board status to inactive if either of the player has left, or the game has finished
        if (player1.getStatus().equals(Player.INACTIVE) || player2.getStatus().equals(Player.INACTIVE) || checkGame() != ONGOING){
            this.status = INACTIVE;
        } else if (player1.getStatus().equals(Player.DISCONNECTED) || player2.getStatus().equals(Player.DISCONNECTED)){
            this.status = PAUSED;
        } else {
            this.status = ACTIVE;
        }
    }

    protected synchronized String getStatus(){
        return status;
    }

    private boolean isWin(Player player){
        // Check rows
        String playerSymbol = player.getSymbol();
        for (int i = 0; i < 3; i++) {
            if (playerSymbol.equals(board.get(new Position(i, 0))) &&
                    playerSymbol.equals(board.get(new Position(i, 1))) &&
                    playerSymbol.equals(board.get(new Position(i, 2)))) {
                return true;
            }
        }

        // Check columns
        for (int i = 0; i < 3; i++) {
            if (playerSymbol.equals(board.get(new Position(0, i))) &&
                    playerSymbol.equals(board.get(new Position(1, i))) &&
                    playerSymbol.equals(board.get(new Position(2, i)))) {
                return true;
            }
        }

        // Check diagonals
        if (playerSymbol.equals(board.get(new Position(0, 0))) &&
                playerSymbol.equals(board.get(new Position(1, 1))) &&
                playerSymbol.equals(board.get(new Position(2, 2)))) {
            return true;
        }

        if (playerSymbol.equals(board.get(new Position(0, 2))) &&
                playerSymbol.equals(board.get(new Position(1, 1))) &&
                playerSymbol.equals(board.get(new Position(2, 0)))) {
            return true;
        }

        return false;
    }

    private boolean isBoardFilled(){ //Draw condition
        for (int i = 0; i < ROW; i++){
            for (int j = 0 ; j < COL; j++){
                if(BOARD_INIT.equals(board.get(new Position(i,j)))){
                    return false; //board is not filled
                }
            }
        }
        return true;
    }

    private void assignSymbols(Player player1, Player player2) {
        if (this.currentPlayer.equals(player1)){ //first player always gets X
            player1.setSymbol(X);
            player2.setSymbol(O);
        } else {
            player1.setSymbol(O);
            player2.setSymbol(X);
        }
    }

    private void initBoard(){
        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COL; j++) {
                board.put(new Position(i,j), BOARD_INIT);
            }
        }
    }

    private Player setCurrentPlayer(Player player1, Player player2){
        Random random = new Random();
        int ran = random.nextInt(2);
        return (ran == 0) ? player1 : player2;
    }

    protected void updateCurrentPlayer(){
        currentPlayer = (currentPlayer.equals(player1)) ? player2 : player1;
    }

    protected Player getPlayer1(){
        return player1;
    }
    protected Player getPlayer2(){
        return player2;
    }

    protected int getMoveTime(){
        return move_time.get();
    }
    protected void decreseTimer(){
        move_time.getAndDecrement();
    }
    protected void resetTimer(){
        this.move_time.set(TIMEOUT_TIME);
    }

    protected ScheduledExecutorService getGameTimer(){
        return gameTimer;
    }

    protected synchronized void storeChat(Player player, String msg){
        chatHistory.offer(player.playerRankAndName()+": "+msg);
        while (chatHistory.size() > CHAT_HISTORY_SIZE){
            chatHistory.poll();
        }
    }

    protected synchronized List<String> getChatHistory(){
        return new ArrayList<>(chatHistory);
    }

    protected Map<String, String> getSerializableBoard(){
        Map<String, String> serializableBoard = new HashMap<>();
        for (Map.Entry<Position, String> entry : board.entrySet()) {
            String key = entry.getKey().getRow() + "," + entry.getKey().getCol();
            String value = entry.getValue();
            serializableBoard.put(key, value);
        }
        return serializableBoard;
    }




    // game board coordinates
    final class Position {
        final int row;
        final int col;

        public Position(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != (o.getClass())) return false;
            Position position = (Position) o;
            return this.row==(position.row) && this.col == (position.col);
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }
        public int getRow(){
            return row;
        }
        public int getCol(){
            return col;
        }
    }


}
