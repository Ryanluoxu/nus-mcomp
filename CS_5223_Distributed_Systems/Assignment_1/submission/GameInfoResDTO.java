import java.io.Serializable;
import java.util.List;

public class GameInfoResDTO implements Serializable {
    private Integer N;
    private Integer K;
    private List<PlayerVO> playerList;
    private boolean isValid;

    public boolean isValid() {
        return isValid;
    }

    public Integer getN() {
        return N;
    }

    public Integer getK() {
        return K;
    }

    public List<PlayerVO> getPlayerList() {
        return playerList;
    }

    public GameInfoResDTO(Integer n, Integer k, List<PlayerVO> players, boolean isValid) {
        N = n;
        K = k;
        this.playerList = players;
        this.isValid = isValid;
    }

    @Override
    public String toString() {
        return "GameInfoResDTO{" +
                "N=" + N +
                ", K=" + K +
                ", players=" + playerList +
                ", isValid=" + isValid +
                '}';
    }
}
