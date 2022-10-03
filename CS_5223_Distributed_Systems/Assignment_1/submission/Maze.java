import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import javax.management.monitor.StringMonitor;
import javax.swing.*;
import javax.swing.plaf.synth.SynthEditorPaneUI;

import java.util.concurrent.TimeUnit;
@SuppressWarnings("serial")
public class Maze extends JFrame{
    private JPanel panel;
    private JPanel leftPanel;
    private JPanel centerPanel;
    private String[][] mazeGrid;
    private Map<String, Integer> playerScores;
    private Map<String, String> server;
    private LocalScanner lScanner;
    private String localPlayerId;

    public Maze(String Id) throws Exception{
        localPlayerId = Id;
        init();
        this.setTitle(localPlayerId);
        this.add(panel);
        this.setPreferredSize(new Dimension(450, 400));
        this.pack(); // 不加pack就只剩标题栏了
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 用户单击窗口的关闭按钮时程序执行的操作
        this.setVisible(true);
    }

    public void init() throws Exception{
        panel = new JPanel();
        leftPanel = new JPanel();
        centerPanel = new JPanel();
        panel.setLayout(new BorderLayout());
        lScanner = new LocalScanner();
    }

    //刷新全局信息
    public void refreshBoard(GameStateVO gameState){
        clearPanel();
        updateGameState(gameState);
        displayBoard();
        paintPanel();
        this.setVisible(true);
    }

    //更新全局信息，包括maze, player和server
    public void updateGameState(GameStateVO gameState){
        List<PlayerVO> playerList = gameState.getPlayerList();
        updatePlayerStatus(playerList);
        updateMazeStatus(gameState);
        updateServerStatus(playerList);
    }

    public void updatePlayerStatus(List<PlayerVO> playerList){
        Map<String, Integer> player = new HashMap<String, Integer>();
        for(int i=0; i<playerList.size(); i++){
            player.put(playerList.get(i).getPlayerId(),playerList.get(i).getScore());
        }
        this.playerScores=player;
    }

    public void updateMazeStatus(GameStateVO gameState){
        int N=gameState.getSize();
        String mazeGrid[][] = new String[N][N];
        for(int i=0; i<mazeGrid.length; i++){
            for(int j=0; j<mazeGrid[i].length; j++){
                String playerId=gameState.isPlayer(i, j);
                if(gameState.isTreasure(i,j)){
                    mazeGrid[i][j]=new String("*");
                }
                else if(playerId.equals("")){
                    mazeGrid[i][j] = new String(" ");
                }
                else{
                    mazeGrid[i][j] = playerId;
                }
            }
        }
        this.mazeGrid=mazeGrid;
    }

    public void updateServerStatus(List<PlayerVO> playerList){
        Map<String, String> server = new HashMap<String, String>();
        server.put("Primary Server:", playerList.get(0).getPlayerId());
        if(playerList.size()>1){
            server.put("Backup Server:", playerList.get(1).getPlayerId());
        }
        this.server=server;
    }

    //展示全局信息
    public void displayBoard(){
        displayLeftBoard();
        displayMazeBoard();
    }

    public void displayLeftBoard(){
        leftPanel.setLayout(new BoxLayout(leftPanel,BoxLayout.Y_AXIS));
        leftPanel.setBackground(new Color(255, 255, 255));
        displayScoreBoard();
        displayServerBoard();
        panel.add(leftPanel, BorderLayout.WEST);
    }

    public void displayScoreBoard(){
        for (Map.Entry<String, Integer> entry : this.playerScores.entrySet()) {
            StringBuilder label =new StringBuilder();
            label.append(entry.getKey());
            label.append(":");
            label.append(String.valueOf(entry.getValue()));
            leftPanel.add(new JLabel(label.toString(),JLabel.CENTER));
        }
    }

    public void displayServerBoard(){
        for (Map.Entry<String, String> entry : this.server.entrySet()) {
            StringBuilder label =new StringBuilder();
            label.append(entry.getKey());
            label.append(":");
            label.append(entry.getValue());
            leftPanel.add(new JLabel(label.toString(),JLabel.CENTER));
        }
    }

    public void displayMazeBoard(){
        centerPanel.setLayout(new GridLayout(this.mazeGrid.length, this.mazeGrid[0].length, 1, 1));
        centerPanel.setBackground(new Color(255, 255, 255));
        for(int row=0; row<this.mazeGrid.length; row++){
            for(int col=0; col<this.mazeGrid[row].length; col++){
                JLabel gridCell= new JLabel(this.mazeGrid[row][col],JLabel.CENTER);
                gridCell.setOpaque(true);
                //highlight the local player
                if(this.mazeGrid[row][col].equalsIgnoreCase(localPlayerId))
                    gridCell.setBackground(Color.RED);
                else
                    gridCell.setBackground(Color.GREEN);
                centerPanel.add(gridCell);
            }
        }
        panel.add(centerPanel, BorderLayout.CENTER);
    }

    public void clearPanel(){
        centerPanel.removeAll();
        leftPanel.removeAll();
    }

    public void paintPanel(){
        leftPanel.repaint();
        leftPanel.revalidate();
        centerPanel.repaint();
        centerPanel.revalidate();
    }

    public static void main(String args[]) throws Exception{
        Maze maze = new Maze("uu");
        List<PlayerVO> playerList = new ArrayList<>();
        LocalScanner scanner= new LocalScanner();
        playerList.add(new PlayerVO("uu", 3));
        playerList.add(new PlayerVO("kk", 2));
        playerList.add(new PlayerVO("mm", 1));
        GameStateVO gameState = new GameStateVO(15,10, playerList);
        gameState.placeCells("uu");
        gameState.placeCells("kk");
        gameState.placeCells("mm");
        maze.refreshBoard(gameState);
    }
}
