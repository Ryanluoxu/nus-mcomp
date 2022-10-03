import java.io.Serializable;

public class PlayerVO implements Serializable {
    private GameRemote gameRemoteObj;
    private String playerId;
    private int score;

    public PlayerVO(GameRemote gameRemoteObj, String playerId, int score) {
        this.gameRemoteObj = gameRemoteObj;
        this.playerId = playerId;
        this.score = score;
    }

    public PlayerVO(GameInfoReqDTO request) {
        this.gameRemoteObj = request.getGameRemoteObj();
        this.playerId = request.getPlayerId();
    }

    public PlayerVO(String playerId, Integer score) {
        this.playerId = playerId;
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public GameRemote getGameRemoteObj() {
        return gameRemoteObj;
    }

    public void setGameRemoteObj(GameRemote gameRemoteObj) {
        this.gameRemoteObj = gameRemoteObj;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    @Override
    public String toString() {
        return "PlayerVO{" +
                "playerId='" + playerId + '\'' +
                ", score=" + score +
                '}';
    }
}
