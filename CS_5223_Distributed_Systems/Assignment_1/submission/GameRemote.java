import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GameRemote extends Remote {

    /**
     * 2. 第二位玩家 tracker.getGameInfo 之后：调用首位玩家的 gameRemoteObj.joinGame 加入游戏，得到 gameState
     * -- LW - done
     */
    GameStateVO joinGame(PlayerVO playerVO) throws RemoteException;

    /**
     * 3. 任何一位玩家开始操作：从 playerList 中顺位调用玩家的 gameRemoteObj.move，得到 gameState
     * -- JH
     */
    GameStateVO move(MoveReqDTO moveRequest) throws RemoteException;

    /**
     * 4. 首位玩家定时ping，从 playerList 中依次调用玩家的 gameRemoteObj.ping，得到 ACK。第二位玩家定时ping首位玩家。
     * -- LX - done
     */
    void ping() throws RemoteException;

    /**
     * 首位玩家发现第二位玩家挂了，向更新后的第二位玩家主动更新 gameState
     * 第二位玩家发现首位玩家挂了，向更新后的第二位玩家主动更新 gameState。
     * -- LX - done
     */
    void updateGameState(GameStateVO gameState) throws RemoteException;

}
