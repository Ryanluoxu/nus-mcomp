import java.io.Serializable;

public class GameInfoReqDTO implements Serializable {

    private GameRemote gameRemoteObj;
    private String playerId;

    public GameInfoReqDTO(GameRemote gameRemoteObj, String playerId) {
        this.gameRemoteObj = gameRemoteObj;
        this.playerId = playerId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public GameRemote getGameRemoteObj() {
        return gameRemoteObj;
    }

    @Override
    public String toString() {
        return "GameInfoReqDTO{" +
                "gameRemoteObj=" + gameRemoteObj +
                ", playerId='" + playerId + '\'' +
                '}';
    }
}
