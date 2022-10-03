import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TrackerRemote extends Remote {

    /**
     * when player first joins the game
     */
    GameInfoResDTO getGameInfo(GameInfoReqDTO request) throws RemoteException;

    /**
     * for primary server or backup server to remove left or crashed player
     */
    void removePlayer(PlayerVO playerVO) throws RemoteException;

}
