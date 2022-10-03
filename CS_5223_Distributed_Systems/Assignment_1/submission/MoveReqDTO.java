import java.io.Serializable;
import java.util.HashMap;
import java.io.Serializable;
public class MoveReqDTO implements Serializable{
    private String playerId;
    private Integer keyboardInput;

    public MoveReqDTO(String playerId, Integer keyboardInput){
        this.playerId=playerId;
        this.keyboardInput=keyboardInput;   
    }

    public String getPlayerId() {
        return playerId;
    }

    public Integer getKeyboardInput() {
        return keyboardInput;
    }

    public String toString() {
        HashMap<Integer, String> moveDir = new HashMap<Integer, String>();
        moveDir.put(0,"Unmoved");
        moveDir.put(1,"West");
        moveDir.put(2,"South");
        moveDir.put(3,"East");
        moveDir.put(4,"North");
        moveDir.put(9,"Quit");

        return "MoveReqDTO{" +
                "playerId='" + playerId +
                ", move=" + moveDir.get(keyboardInput) +
                '}';
    }
}
