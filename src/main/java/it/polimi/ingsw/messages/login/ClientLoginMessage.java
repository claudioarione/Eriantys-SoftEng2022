package it.polimi.ingsw.messages.login;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.messages.Message;

public class ClientLoginMessage extends Message {
    private String username;
    private int numPlayers;
    private String action;
    private boolean expert;

    public ClientLoginMessage() {
        super();
        setStatus("LOGIN");
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public String getAction() {
        return action;
    }

    public void setNumPlayers(int numPlayers) {
        this.numPlayers = numPlayers;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isExpert() {
        return expert;
    }

    public void setExpert(boolean expert) {
        this.expert = expert;
    }

    /**
     * Returns a {@code ClientLoginMessage} object from a Json {@code String}
     *
     * @param json the Json {@code String}
     * @return a {@code ClientLoginMessage} object from a Json {@code String}
     */
    public static ClientLoginMessage fromJSON(String json) throws JsonSyntaxException {
        Gson gson = new Gson();
        return gson.fromJson(json, new TypeToken<ClientLoginMessage>() {
        }.getType());
    }
}