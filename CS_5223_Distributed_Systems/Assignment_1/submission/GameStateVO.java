import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GameStateVO implements Serializable {
    private List<PlayerVO> playerList;
    private MazeVO maze;
    private Integer N;
    private Integer K;
    private Random rand = new Random();

    @Override
    public String toString() {
        return "GameStateVO{" +
                "playerList=" + playerList +
                '}';
    }

    public GameStateVO(Integer n, Integer k, List<PlayerVO> playerList) {
        K = k;
        N = n;
        this.playerList = playerList;
        maze = new MazeVO();
        maze.cells = new CellVO[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                maze.cells[i][j] = new CellVO(i, j);
            }
        }

        for (int i = 0; i < K; i++) {
            placeCells("*");
        }

        placeCells(playerList.get(0).getPlayerId());
    }


    static class MazeVO implements Serializable {
        CellVO[][] cells;

        @Override
        public String toString() {
            return "MazeVO{" +
                    "cells=" + Arrays.toString(cells) +
                    '}';
        }
    }

    static class CellVO implements Serializable {
        int x;
        int y;
        boolean hasTreasure;
        String playerId;

        public CellVO(Integer X, Integer Y) {
            hasTreasure = false;
            playerId = "";
        }

        @Override
        public String toString() {
            return "CellVO{" +
                    "x=" + x +
                    ", y=" + y +
                    ", hasTreasure=" + hasTreasure +
                    ", playerId='" + playerId + '\'' +
                    '}';
        }
    }

    public boolean isTreasure(int x, int y) {
        return maze.cells[x][y].hasTreasure;
    }

    public String isPlayer(int x, int y) {
        return maze.cells[x][y].playerId;
    }

    public Integer getSize() {
        return N;
    }

    public List<PlayerVO> getPlayerList() {
        return playerList;
    }

    public void addPlayer(PlayerVO player) {
        //添加player并随机安排位置
        playerList.add(player);
        placeCells(player.getPlayerId());
    }

    /**
     * Remove the player when the player exits the game.
     */
    public void removePlayer(PlayerVO player) {
        String playerId = player.getPlayerId();
        int x = 0;
        int y = 0;

        // find original position
        for (int i=0; i<N; i++) {
            for (int j=0; j<N; j++) {
                if (maze.cells[i][j].playerId.equalsIgnoreCase(playerId)) {
                    x = i;
                    y = j;
                }
            }
        }

        maze.cells[x][y].playerId = "";
        playerList.remove(player);

        System.out.println("player "+playerId+" removed.");
    }

    public void placeCells(String cell){
        //随机放置treasure或者player
        //To do 用户很多的时候优化放置效率
        while(true){
            int x = rand.nextInt(N);
            int y = rand.nextInt(N);
            if(maze.cells[x][y].hasTreasure==false 
                & maze.cells[x][y].playerId.equalsIgnoreCase("")){
                maze.cells[x][y].playerId=cell;
                if(cell.equalsIgnoreCase("*")){
                    maze.cells[x][y].hasTreasure=true;
                }
                break;
            }
        }
    }


    /**
     *  JH
     *  move player
     *    4
     *   1 3
     *    2
     * @param player
     */
    public boolean movePlayer(PlayerVO player, int move) {
        String playerId = player.getPlayerId();
        int x = 0;
        int y = 0;

        // find original position
        for (int i=0; i<N; i++) {
            for (int j=0; j<N; j++) {
                if (maze.cells[i][j].playerId.equalsIgnoreCase(playerId)) {
                    x = i;
                    y = j;
                }
            }
        }
        int oldX = x;
        int oldY = y;

        // calculate new position
        switch (move){
            case 0:
                break;
            case 4:
                if (x>0) x = x - 1;
                break;
            case 3:
                if (y<N-1) y = y + 1;
                break;
            case 2:
                if (x<N-1) x = x + 1;
                break;
            case 1:
                if (y>0) y = y - 1;
                break;
            default:
                System.out.println("Player " + player.getPlayerId() + ": Unknown move " + move);
                break;
        }
        // Update player's position
        if (move==0) {
            System.out.println("Player " + player.getPlayerId() + ": Refresh");
        } else {
            if (maze.cells[x][y].playerId.length() > 1) {
                System.out.println("Player " + player.getPlayerId() + ": Failed move " + move);
            } else if (maze.cells[x][y].hasTreasure == false) {
                maze.cells[oldX][oldY].playerId = "";
                maze.cells[x][y].playerId = playerId;
            } else if (maze.cells[x][y].hasTreasure == true) {
                maze.cells[oldX][oldY].playerId = "";
                maze.cells[x][y].playerId = playerId;

                // Treasure be eaten
                maze.cells[x][y].hasTreasure = false;
                // Place a new treasure
                placeCells("*");
                System.out.println("New treasure generated");
                return true;
            }
        }

        return false;

    }




}