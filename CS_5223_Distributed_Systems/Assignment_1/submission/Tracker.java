import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

/**
 * tracker:
 * - getGameInfo by providing gameRemoteObj
 * - removePlayer
 * -- LX - done
 */
public class Tracker implements TrackerRemote {

    /**
     * The rmi-registry should be used only for registering and locating the tracker.
     */
    private static final String REMOTE_REF = "tracker";
    private static int port;
    private static int N;
    private static int K;
    private static List<PlayerVO> players = new ArrayList<>();

    public static void main(String[] args) {
        readArgs(args);

        try {
            Tracker tracker = new Tracker();
            TrackerRemote stub = (TrackerRemote) UnicastRemoteObject.exportObject(tracker, port);
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(REMOTE_REF, stub);

            System.out.println("tracker ready... port: " + port + ", N: " + N + ", K: " + K + ".");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public GameInfoResDTO getGameInfo(GameInfoReqDTO request) throws RemoteException {
        System.out.println("Tracker getGameInfo - playerId: " + request.getPlayerId() + "\n");
        boolean isValid = addPlayer(request);
        GameInfoResDTO response = new GameInfoResDTO(N, K, players, isValid);
        System.out.println("Tracker getGameInfo - END - response: " + response + "\n");
        return response;
    }

    private boolean addPlayer(GameInfoReqDTO request) {
        synchronized (players) {
            // validate number of players
            int vacancy = N * N - K - players.size();
            if (vacancy == 0) {
                System.out.println("no vacancy..");
                return false;
            }
            PlayerVO newPlayer = new PlayerVO(request);
            players.add(newPlayer);
        }
        return true;
    }

    @Override
    public void removePlayer(PlayerVO playerVO) throws RemoteException {
        System.out.println("removePlayer - playerId:" + playerVO.getPlayerId() + "\n");
        synchronized (players) {
            players.removeIf(player -> player.getPlayerId().equalsIgnoreCase(playerVO.getPlayerId()));
        }
    }

    /**
     * java Tracker [port-number] [N] [K]
     * - port-number is the port over which the Tracker is listening
     * - The (implicit) IP address will be the local machine’s IP
     */
    private static void readArgs(String[] args) {
        if (args != null && args.length == 3) {
            try {
                port = Integer.parseInt(args[0]);
                N = Integer.parseInt(args[1]);
                K = Integer.parseInt(args[2]);
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
}
