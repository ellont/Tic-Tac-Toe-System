package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
/**
 * Author: Yiran Wang <yirwang10@student.unimelb.edu.au>
 * Student ID: 1366272
 */
public class ClientGUI {
    private RemoteClient remoteClient;
    private JLabel statusLabel;
    private JPanel boardPanel;
    private JButton[][] gameBoard;
    private JButton quitButton;
    private JLabel timerLabel;
    private JTextArea chatBox;
    private JTextField chatInput;
    private JPanel chatPanel;
    public static final String ACTIVE = "ACTIVE";
    public static final String INACTIVE = "INACTIVE";
    public static final String PAUSED = "PAUSED";


    protected ClientGUI(RemoteClient remoteClient) {
        this.remoteClient = remoteClient;
        this.boardPanel = new JPanel(new GridLayout(3, 3));
        this.gameBoard = new JButton[3][3];
        this.timerLabel = new JLabel();
        this.statusLabel = new JLabel("Finding Player");
        this.chatBox = new JTextArea();
        chatBox.setEditable(false);
        this.chatInput = new JTextField();
        this.quitButton = new JButton("Quit");
        this.chatPanel = new JPanel(new BorderLayout());
        checkGameStatus();
    }

    protected void startGUI(){
        // Initialize GUI
        JFrame frame = new JFrame("Distributed Tic-Tac-Toe -- "+remoteClient.getUsername());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 450);


        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel leftPanel = new JPanel(new GridLayout(4,1));

        // Left Section
        JLabel timerHeader = new JLabel("Timer");
        timerHeader.setHorizontalAlignment(JLabel.CENTER);
        timerHeader.setVerticalAlignment(JLabel.CENTER);

        timerLabel.setFont(new Font("Arial", Font.BOLD, 22));
        timerLabel.setHorizontalAlignment(JLabel.CENTER);
        timerLabel.setVerticalAlignment(JLabel.CENTER);

        quitButton.addActionListener(e -> remoteClient.closeEverything());
        leftPanel.add(timerHeader);
        leftPanel.add(timerLabel);
        leftPanel.add(new JLabel("Distributed Tic-Tac-Toe"));
        leftPanel.add(quitButton);

        // Middle Section
        JPanel middlePanel = new JPanel(new BorderLayout());
        //statusLabel = new JLabel("Finding Player");
        //JPanel boardPanel = new JPanel(new GridLayout(3, 3));
        //gameBoard = new JButton[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                gameBoard[i][j] = new JButton("");
                gameBoard[i][j].setFont(new Font("Arial", Font.PLAIN, 26));
                gameBoard[i][j].setFocusPainted(false);
                gameBoard[i][j].setEnabled(true);
                final int finalI = i;
                final int finalJ = j;

                boardPanel.add(gameBoard[i][j]);
                gameBoard[i][j].addActionListener(e -> makeMove(finalI, finalJ));
            }
        }

        middlePanel.add(statusLabel, BorderLayout.NORTH);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        timerLabel.setHorizontalAlignment(JLabel.CENTER);
        middlePanel.add(boardPanel, BorderLayout.CENTER);

        // Right Section
        JPanel rightPanel = new JPanel(new BorderLayout());

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendClick());


        chatBox.setColumns(20);
        chatBox.setLineWrap(true);
        chatBox.setWrapStyleWord(true);
        chatPanel.add(chatInput, BorderLayout.CENTER);
        chatPanel.add(sendButton, BorderLayout.SOUTH);

        rightPanel.add(new JLabel("Player Chat"), BorderLayout.NORTH);
        rightPanel.add(chatBox, BorderLayout.CENTER);
        rightPanel.add(chatPanel, BorderLayout.SOUTH);

        // Assemble Main Panel
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(middlePanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Perform the same action as your quit button here
                quitButton.doClick();
            }
        });

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    protected void disableButtons(){
        SwingUtilities.invokeLater(() -> {
            boardPanel.setVisible(false);
            chatPanel.setVisible(false);

        });
    }

//    private void disableBoard(){
//        SwingUtilities.invokeLater(() -> {
//            boardPanel.setVisible(false);
//        });
//    }
//
//    private void disableChat(){
//        SwingUtilities.invokeLater(() -> {
//            chatPanel.setVisible(false);
//        });
//    }

    protected void enableButtons(){
        SwingUtilities.invokeLater(() -> {
            boardPanel.setVisible(true);
            chatPanel.setVisible(true);

        });
    }

    protected void resetButtons(){
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    gameBoard[i][j].setEnabled(true);
                    gameBoard[i][j].setText("");
                }
            }
        });
    }

    protected void showBoard(Map<String, String> board){
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    String key = i + "," + j;
                    String value = board.getOrDefault(key, "");
                    gameBoard[i][j].setText(value);

                    if ("X".equals(value) || "O".equals(value)) {
                        gameBoard[i][j].setEnabled(false);
                    } else {
                        gameBoard[i][j].setEnabled(true);
                    }
                }
            }
        });
    }


    protected boolean playNextGame(){
        final boolean[] result = new boolean[1]; // For storing the result
        final CountDownLatch latch = new CountDownLatch(1);
        Runnable showDialog = new Runnable() {
            public void run() {
                Object[] options = {"Yes", "Quit"};
                int n = JOptionPane.showOptionDialog(null,
                        "Fine a new game?",
                        "New Game?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);

                if (n == JOptionPane.YES_OPTION) {
                    resetButtons();
                    //disableButtons();
                    timerLabel.setText("");
                    setStatusLabel("Finding Player");
                    chatBox.setText("");//clear textbox
                    result[0] = true;
                } else {
                    quitButton.doClick();
                    //remoteClient.closeEverything();
                    result[0] = false;
                }
                latch.countDown();
            }
        };

        SwingUtilities.invokeLater(showDialog);
        try {
            latch.await();
        } catch (InterruptedException e) {
            popupMsg("ClientGUI - PlayNextGame - CountDownLatch");
        }

        return result[0];
    }

    protected void makeMove(int x, int y) {
        remoteClient.makeMove(x,y);
    }

    protected void makeRandomMove(){
        ArrayList<int[]> emptyCells = new ArrayList<>();
        // Identify all empty cells
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (gameBoard[i][j].getText().equals("")) {
                    emptyCells.add(new int[]{i, j});
                }
            }
        }
        if (!emptyCells.isEmpty()) {
            Random rand = new Random();
            int[] randomCell = emptyCells.get(rand.nextInt(emptyCells.size()));
            SwingUtilities.invokeLater(() -> gameBoard[randomCell[0]][randomCell[1]].doClick());
        }
    }



    protected void setStatusLabel (String msg){
        SwingUtilities.invokeLater(() ->
                statusLabel.setText(msg));
    }

    private void checkGameStatus() {
        new Thread(() -> {
            while (remoteClient != null) {
                try {
                    if (remoteClient.hasBoard()){ //check if the game is ready
                        String gameStatus = remoteClient.getGameStatus();
                        switch (gameStatus) {
                            case ACTIVE:
                                enableButtons();
                                break;
                            case PAUSED:
                                //System.out.println("Game is paused");
                                disableButtons();
                                showInternalMsg("SERVER: Waiting for the other player to re-join.");
                                break;
                            case INACTIVE:
                                boolean play = playNextGame();
                                remoteClient.playNextGame(play);
                                if (!play){
                                    break;
                                }
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        popupMsg("RemoteGame - checkGameStatus: Thread Interrupted");
                    }
                }
                catch (RemoteException e) {
                    remoteClient.gracefulShutdown();
                }

            }
        }).start();
    }

    protected void popupMsg(String msg){
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE));
    }


    protected void setTimerLabel(int time){
        SwingUtilities.invokeLater(() ->
                timerLabel.setText(String.valueOf(time)));
    }

    protected void setChatBox(List<String> chatHistory){
        SwingUtilities.invokeLater(() -> {
                    chatBox.setText(String.join("\n", chatHistory));
                    chatBox.append("\n");
                }
        );
    }

    protected JButton[][] getGameBoard (){
        return gameBoard;
    }


    private void sendClick() {
        String message = chatInput.getText();
        try {
            remoteClient.sendMsg(message);
            chatInput.setText("");
        } catch (RemoteException e){
            remoteClient.gracefulShutdown();
            //popupMsg(RemoteClient.SERVER_UNAVAILABLE);
        }
    }

    protected void showInternalMsg(String msg){
        SwingUtilities.invokeLater(()->{
            chatBox.append(msg);
            chatBox.append("\n");
            chatBox.setCaretPosition(chatBox.getDocument().getLength());
        });
    }

}

