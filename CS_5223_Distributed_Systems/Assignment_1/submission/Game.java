import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Game implements GameRemote {

    private static final String REMOTE_REF_TRACKER = "tracker";
    private static String tracker_ip;
    private static int tracker_port;
    private static String playerId;
    private static GameStateVO gameState;
    private static TrackerRemote trackerRemoteObj;
    private static PlayerVO player;

    public static void main(String[] args) throws Exception {

        readArgs(args);
        trackerRemoteObj = getTrackerRemoteObj();
        LocalScanner scanner = new LocalScanner();
        Maze maze = new Maze(playerId);

        try {
            Game game = new Game();
            GameRemote gameRemoteObj = (GameRemote) UnicastRemoteObject.exportObject(game, 0);
            GameInfoReqDTO gameInfoReq = new GameInfoReqDTO(gameRemoteObj, playerId);
            GameInfoResDTO gameInfoRes = trackerRemoteObj.getGameInfo(gameInfoReq);
            System.out.println("get gameInfoRes: " + gameInfoRes);
            if (!gameInfoRes.isValid()) {
                System.out.println("playerId already exists or no vacancy..");
                System.exit(0);
            }
            if (gameInfoRes.getPlayerList().size() == 1) {  // 1st player -> pServer: init game
                System.out.println("1st player->pServer: init the game");
                initGame(gameInfoRes.getN(), gameInfoRes.getK(), gameInfoRes.getPlayerList());
                maze.refreshBoard(gameState);
            } else {    // joinGame
                // call joinGame one by one - LX
                player = new PlayerVO(gameRemoteObj, playerId, 0);
                joinGame(gameInfoRes.getPlayerList(), player, gameInfoRes.getN(), gameInfoRes.getK());
                maze.refreshBoard(gameState);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        doScheduledPing();

        String[] inst = {"0", "1", "2", "3", "4"};
        while (true) {
            String token = scanner.nextToken();
            if (token.equalsIgnoreCase("9")) {
                MoveReqDTO moveReqDTO = new MoveReqDTO(playerId, Integer.parseInt(token));
                sendMoveRequest(gameState.getPlayerList(), moveReqDTO);
                trackerRemoteObj.removePlayer(player);
                maze.dispose();
                break;
            } else if (Arrays.asList(inst).contains(token)) {
                MoveReqDTO moveReqDTO = new MoveReqDTO(playerId, Integer.parseInt(token));
                sendMoveRequest(gameState.getPlayerList(), moveReqDTO);
            } else {
                continue;
            }
            maze.refreshBoard(gameState);
        }
    }

    private static void sendMoveRequest(List<PlayerVO> playerList, MoveReqDTO moveReqDTO) {
        for (int i = 0; i < playerList.size(); i++) {
            try {
                gameState = playerList.get(i).getGameRemoteObj().move(moveReqDTO);
                break;
            } catch (Exception ex) {
                System.out.println("crash player: " + playerList.get(i).getPlayerId());
                continue;
            }
        }
    }

    /**
     * set gameState
     */
    private static void joinGame(List<PlayerVO> playerList, PlayerVO player, int N, int K) {
        List<PlayerVO> crashPlayers = new ArrayList<>();
        while (true) {
            PlayerVO pServer = playerList.get(0);
            if (pServer.getPlayerId().equals(playerId)) {
                System.out.println("pServer: init the game");
                initGame(N, K, playerList);
                for (PlayerVO playerVO : crashPlayers) {
                    try {
                        trackerRemoteObj.removePlayer(playerVO);
                    } catch (RemoteException e) {
                        System.err.println("trackerRemoteObj.removePlayer() fail..");
                    }
                }
                break;
            }
            try {
                gameState = pServer.getGameRemoteObj().joinGame(player);
                System.out.println("join game - gameState: " + gameState);
                break;
            } catch (Exception ex) {
                playerList.remove(0);
                System.out.println("crashPlayer: " + pServer.getPlayerId());
                crashPlayers.add(pServer);
            }
        }
    }

    private static void doScheduledPing() {
        Runnable pingTask = new Runnable() {
            @Override
            public void run() {
                try {
                    schedulePing();
                } catch (RemoteException e) {
                    System.out.println(e.getMessage());
                }
            }
        };
        ScheduledExecutorService scheduledService = Executors.newSingleThreadScheduledExecutor();
        scheduledService.scheduleAtFixedRate(pingTask, 500, 500, TimeUnit.MILLISECONDS);
    }

    /**
     * primary ping all players，backup ping primary
     * -- LX - done
     */
    private static void schedulePing() throws RemoteException {
        if (isPrimaryServer()) {
//            System.out.println(new Date() + ": scheduled ping....");
            for (int i = 1; i < gameState.getPlayerList().size(); i++) {
                PlayerVO player = gameState.getPlayerList().get(i);
                try {
                    player.getGameRemoteObj().ping();
                } catch (Exception ex) {
                    // for both bServer and normal player
                    gameState.removePlayer(player);
                    if (gameState.getPlayerList().size() > 1) {
                        gameState.getPlayerList().get(1).getGameRemoteObj().updateGameState(gameState);
                    }
                    trackerRemoteObj.removePlayer(player);
                }
            }
        } else if (isBackupServer()) {
            PlayerVO server = gameState.getPlayerList().get(0);
            try {
                server.getGameRemoteObj().ping();
            } catch (Exception ex) {
                gameState.removePlayer(server);
                if (gameState.getPlayerList().size() > 1) {
                    gameState.getPlayerList().get(1).getGameRemoteObj().updateGameState(gameState);
                }
                trackerRemoteObj.removePlayer(server);
            }
        }
    }

    private static TrackerRemote getTrackerRemoteObj() {
        TrackerRemote trackerRemoteObj = null;
        try {
            Registry registry = LocateRegistry.getRegistry(tracker_ip, 0);
            trackerRemoteObj = (TrackerRemote) registry.lookup(REMOTE_REF_TRACKER);
            System.out.println("get trackerRemoteObj...");
        } catch (RemoteException | NotBoundException ex) {
            System.err.println("readArgs error: " + ex.getMessage());
            System.exit(0);
        }
        return trackerRemoteObj;
    }

    /**
     * java Game [IP-address] [port-number] [player-id]
     * - IP-address is the well-known IP address of the Tracker
     * - port-number is the port over which the Tracker is listening
     * - player-id is the two-character name of the player
     */
    private static void readArgs(String[] args) {
        if (args != null && args.length == 3) {
            try {
                tracker_ip = args[0];
                tracker_port = Integer.parseInt(args[1]);
                playerId = args[2];
                if (playerId.length() != 2) {
                    exitGame(args);
                }
            } catch (Exception ex) {
                System.err.println("readArgs error: " + ex.getMessage());
                exitGame(args);
            }
        } else {
            exitGame(args);
        }
    }

    private static void exitGame(String[] args) {
        System.err.println("invalid args: " + args);
        System.exit(0);
    }

    public static void initGame(int N, int K, List<PlayerVO> playerList) {
        gameState = new GameStateVO(N, K, playerList);
    }

    /**
     * LW
     */
    @Override
    public GameStateVO joinGame(PlayerVO playerVO) throws RemoteException {
        validatePServer();
        gameState.addPlayer(playerVO);
        // update bServer
        if (gameState.getPlayerList().size() > 1) {
            while (true) {
                PlayerVO bServer = gameState.getPlayerList().get(1);
                try {
                    bServer.getGameRemoteObj().updateGameState(gameState);
                    break;
                } catch (Exception ex) {
                    System.out.println("bServer crash: " + bServer.getPlayerId());
                    gameState.removePlayer(bServer);
                    trackerRemoteObj.removePlayer(bServer);
                }
            }
        }
        return gameState;
    }

    private void validatePServer() {
        if (isBackupServer()) {
            System.out.println("bServer receive join/move request..");
            PlayerVO server = gameState.getPlayerList().get(0);
            try {
                gameState.removePlayer(server);
                if (gameState.getPlayerList().size() > 1) {
                    gameState.getPlayerList().get(1).getGameRemoteObj().updateGameState(gameState);
                }
                trackerRemoteObj.removePlayer(server);
            } catch (Exception ex) {
                System.out.println("");
            }
        }
    }

    /**
     * JH
     *
     * @param moveRequest
     * @return server's game state
     * <p>
     * The player moves and refreshes its local state.
     * If it is primary server, inform backup server latest game state.
     * return latest game state.
     */
    @Override
    public GameStateVO move(MoveReqDTO moveRequest) throws RemoteException {
        System.out.println("move request: " + moveRequest);
        validatePServer();

        Integer move = moveRequest.getKeyboardInput();
        String playerId = moveRequest.getPlayerId();

        // Update server's game state
        synchronized (gameState) { // sync game state

            // the player exits the game on its own initiative
            if (move == 9) {
//                System.out.println("Player " + playerId + " quit the game.");
                for (PlayerVO player : gameState.getPlayerList()) {
                    if (player.getPlayerId().equalsIgnoreCase(playerId)) {
                        gameState.movePlayer(player, move);
                        gameState.removePlayer(player);
                    }
                }
            }

            // the player moves S/N/E/W or remain its position, then refresh its local state
            else if (move == 0 || move == 1 || move == 2 || move == 3 || move == 4) {
                for (PlayerVO player : gameState.getPlayerList()) {
                    if (player.getPlayerId().equalsIgnoreCase(playerId)) {
                        boolean score = gameState.movePlayer(player, move);
                        if (score == true) { // eat the treasure and get 1 score
                            player.setScore(player.getScore() + 1);
                        }
                    }
                }
            }

            // Unknown move
            else {
//                System.err.println("Player " + playerId + " unknown move " + move);
                return gameState;
            }
        }

        // If player is the primary server, it should inform the backup server update to the latest game state
        if (gameState.getPlayerList().size() > 1) {
            while (true) {
                try {
                    //inform bServer latest game state
                    gameState.getPlayerList().get(1).getGameRemoteObj().updateGameState(gameState);
                    break;
                } catch (RemoteException e) {
                    // pServer fails to call bServer.getGameRemoteObj
                    // since bServer crashed
                    // remove bServer and update to next player (new bServer)
                    System.err.println("pServer fails to call bServer.getGameRemoteObj().");
                    PlayerVO bServer = gameState.getPlayerList().get(1);
                    gameState.removePlayer(bServer);
                }
            }
        }
        // return latest game state to Game
        return gameState;
    }

    /**
     * LX
     */
    @Override
    public void ping() throws RemoteException {
//        System.out.println("successful ping -> player:" + playerId);
    }

    @Override
    public void updateGameState(GameStateVO gameState) throws RemoteException {
        System.out.println("bServer receive updateGameState: " + gameState);
        this.gameState = gameState;
    }

    /**
     * compare playerList with playerId, if this is pServer
     */
    private static boolean isPrimaryServer() {
        synchronized (gameState) {
            if (gameState.getPlayerList().size() > 0
                    && gameState.getPlayerList().get(0).getPlayerId().equalsIgnoreCase(playerId)) {
                return true;
            }
            return false;
        }
    }

    /**
     * compare playerList with playerId, if this is bServer
     */
    private static boolean isBackupServer() {
        synchronized (gameState) {
            if (gameState.getPlayerList().size() > 1
                    && gameState.getPlayerList().get(1).getPlayerId().equalsIgnoreCase(playerId)) {
                return true;
            }
            return false;
        }
    }

}
