package it.polimi.ingsw.controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.constants.Messages;
import it.polimi.ingsw.exceptions.GameEndedException;
import it.polimi.ingsw.messages.Message;
import it.polimi.ingsw.messages.action.ClientActionMessage;
import it.polimi.ingsw.messages.action.ServerActionMessage;
import it.polimi.ingsw.messages.login.ClientLoginMessage;
import it.polimi.ingsw.messages.login.GameLobby;
import it.polimi.ingsw.messages.login.ServerLoginMessage;
import it.polimi.ingsw.model.Game;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.server.Communicable;
import it.polimi.ingsw.server.PlayerClient;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

public class Controller {
    private final Gson gson;

    private final ArrayList<PlayerClient> loggedUsers;
    private final Object boundLock = new Object();
    private int desiredNumberOfPlayers;
    private GameController gameController;
    private int pongCount;
    private boolean isExpert;
    private int bound = 0;

    public Controller() {
        loggedUsers = new ArrayList<>();
        gson = new Gson();
        gameController = null;
        desiredNumberOfPlayers = -1;
    }

    /**
     * Sends an error message to the client
     *
     * @param ch           The {@code Communicable} interface of the client which caused the error
     * @param status       "LOGIN" or "ACTION"
     * @param errorMessage the string which will be shown to the user
     * @param errorCode    an integer representing the error which occurred
     */
    public static void sendErrorMessage(Communicable ch, String status, String errorMessage, int errorCode) {
        if (status.equals("LOGIN")) {
            ServerLoginMessage message = new ServerLoginMessage();
            message.setError(errorCode);
            message.setDisplayText("[ERROR] " + errorMessage);
            ch.sendMessageToClient(message.toJson());
        } else if (status.equals("ACTION")) {
            ServerActionMessage message = new ServerActionMessage();
            message.setError(errorCode);
            message.setDisplayText("[ERROR] " + errorMessage);
            ch.sendMessageToClient(message.toJson());
        }

    }

    /**
     * Handles a message received from a Client and sends the appropriate response
     *
     * @param jsonMessage the message received from the client
     * @param ch          the {@code Communicable} interface of the client which sent the message
     */
    public synchronized void handleMessage(String jsonMessage, Communicable ch) throws GameEndedException {
        switch (getMessageStatus(jsonMessage)) {
            case "LOGIN" -> handleLoginMessage(jsonMessage, ch);
            case "ACTION" -> handleActionMessage(jsonMessage, ch);
            case "PONG" -> handlePong();
            default -> sendErrorMessage(ch, "LOGIN", "Unrecognised type", 3);
        }
    }

    /**
     * Return the status field of the given json message
     *
     * @param json a JSON string containing the message to get the status of
     * @return the status field of the given json message
     */
    private String getMessageStatus(String json) {
        Type type = new TypeToken<Message>() {
        }.getType();
        Message msg = gson.fromJson(json, type);
        return msg.getStatus();
    }

    /**
     * Handles a pong message
     */
    private void handlePong() {
        synchronized (boundLock) {
            pongCount++;
        }
    }

    /**
     * Deserializes and handles a login message
     *
     * @param jsonMessage Json string which contains a login message
     * @param ch          the {@code Communicable} interface of the client who sent the message
     */
    private void handleLoginMessage(String jsonMessage, Communicable ch) {
        try {
            ClientLoginMessage loginMessage = ClientLoginMessage.fromJSON(jsonMessage);

            if (loginMessage.getAction() == null) {
                sendErrorMessage(ch, "LOGIN", "Bad request", 3);
                return;
            }

            switch (loginMessage.getAction()) {
                case "SET_USERNAME" -> addUser(ch, loginMessage.getUsername());
                case "CREATE_GAME" -> setGameParameters(ch, loginMessage.getNumPlayers(), loginMessage.isExpert());
                default -> sendErrorMessage(ch, "LOGIN", "Bad request", 3);

            }
        } catch (JsonSyntaxException e) {
            sendErrorMessage(ch, "LOGIN", Messages.INVALID_FORMAT_NUM_PLAYER, 3);
        }
    }

    /**
     * Sets the desired number of players of the game if possible
     *
     * @param ch         the {@code Communicable} interface of the client who sent the message
     * @param numPlayers the value contained in the message received
     */
    private void setGameParameters(Communicable ch, int numPlayers, boolean isExpert) {
        if (loggedUsers.isEmpty() || loggedUsers.get(0).getCommunicable() != ch) {
            sendErrorMessage(ch, "LOGIN", Messages.INVALID_PLAYER_CREATING_GAME, 3);
            return;
        }
        if (numPlayers < 2 || numPlayers > 4) {
            sendErrorMessage(ch, "LOGIN", Messages.INVALID_NUM_PLAYERS, 3);
            return;
        }

        desiredNumberOfPlayers = numPlayers;
        this.isExpert = isExpert;
        System.out.println("GAME CREATED | " + numPlayers + " players | " + (isExpert ? "expert" : "non expert") + " mode");

        if (!isGameReady()) {
            ServerLoginMessage message = getServerLoginMessage(Messages.GAME_CREATED);
            for (PlayerClient player : loggedUsers) {
                player.getCommunicable().sendMessageToClient(message.toJson());
            }
        }
    }

    /**
     * Adds the user who sent the message to the loggedUsers {@code ArrayList} if possible
     *
     * @param ch       the {@code Communicable} interface of the client who sent the message
     * @param username the username of the user to be added
     */
    private void addUser(Communicable ch, String username) {

        if (username == null || username.trim().equals("")) {
            sendErrorMessage(ch, "LOGIN", Messages.INVALID_USERNAME, 3);
        } else if ((desiredNumberOfPlayers != -1 && loggedUsers.size() >= desiredNumberOfPlayers) || loggedUsers.size() >= 4 || gameController != null) {
            sendErrorMessage(ch, "LOGIN", Messages.LOBBY_FULL, 1);
        } else if (username.length() > 32) {
            sendErrorMessage(ch, "LOGIN", Messages.USERNAME_TOO_LONG, 3);
        } else if (loggedUsers.stream().anyMatch(u -> u.getUsername().equals(username))) {
            sendErrorMessage(ch, "LOGIN", Messages.USERNAME_ALREADY_TAKEN, 2);
        } else {
            PlayerClient newUser = new PlayerClient(ch, username);
            loggedUsers.add(newUser);
            System.out.println(Messages.ADDED_PLAYER + newUser.getUsername());

            if (newUser == loggedUsers.get(0)) {
                askDesiredNumberOfPlayers(ch);
                return;
            }
            if (!isGameReady()) {
                sendBroadcastMessage();     // signals everyone that a new player has joined
            }
        }
    }


    /**
     * Sends a message to every logged user containing the usernames of all logged users and the desired number of players
     * of the game, which can be -1 (not specified yet), 2, 3 or 4
     */
    private void sendBroadcastMessage() {

        ServerLoginMessage res = getServerLoginMessage(Messages.NEW_PLAYER_JOINED);

        if (desiredNumberOfPlayers != -1) {
            loggedUsers.get(0).getCommunicable().sendMessageToClient(res.toJson());
        }

        for (int i = 1; i < loggedUsers.size(); i++) {
            loggedUsers.get(i).getCommunicable().sendMessageToClient(res.toJson());
        }
    }

    /**
     * Returns a {@code ServerLoginMessage} with the current {@code GameLobby} and number of players and a custom message
     *
     * @param message the {@code String} to put in the field displayText of the message
     * @return a {@code ServerLoginMessage} object
     */
    private ServerLoginMessage getServerLoginMessage(String message) {
        ServerLoginMessage res = new ServerLoginMessage();
        res.setDisplayText(message);
        Collection<String> playersList = loggedUsers.stream().map(PlayerClient::getUsername).toList();
        String[] playersArray = playersList.toArray(new String[0]);
        res.setGameLobby(new GameLobby(playersArray, desiredNumberOfPlayers, isExpert));
        return res;
    }

    /**
     * Sends a message asking for the desired number of players and the mode of the game
     *
     * @param ch the {@code Communicable} interface of the player to send the message to
     */
    private void askDesiredNumberOfPlayers(Communicable ch) {
        ServerLoginMessage res = new ServerLoginMessage();
        res.setAction("CREATE_GAME");
        res.setDisplayText(Messages.SET_GAME_PARAMETERS);

        ch.sendMessageToClient(res.toJson());

    }

    /**
     * Checks if a game can be started; if so, every client in the lobby is notified
     */
    private boolean isGameReady() {
        if (desiredNumberOfPlayers == -1 || loggedUsers.size() < desiredNumberOfPlayers) return false;

        while (desiredNumberOfPlayers < loggedUsers.size()) {
            // Alert player that game is full and removes him
            PlayerClient toRemove = loggedUsers.get(desiredNumberOfPlayers);
            String errorMessage = "A new game for " + desiredNumberOfPlayers + " players is starting. Your connection will be closed";
            sendErrorMessage(toRemove.getCommunicable(), "LOGIN", errorMessage, 1);
            loggedUsers.remove(toRemove);
        }
        String message = Messages.GAME_STARTING;
        if (desiredNumberOfPlayers == 4) {
            message += ". The teams are: " + loggedUsers.get(0).getUsername() + " and " + loggedUsers.get(2).getUsername() +
                    " [WHITE team]  VS  " + loggedUsers.get(1).getUsername() + " and " + loggedUsers.get(3).getUsername() + " [BLACK team]";
        }
        ServerLoginMessage toSend = getServerLoginMessage(message);

        for (PlayerClient playerClient : loggedUsers) {
            // Alert player that game is starting
            playerClient.getCommunicable().sendMessageToClient(toSend.toJson());
            playerClient.setPlayer(new Player(playerClient.getUsername(), desiredNumberOfPlayers % 2 == 0 ? 8 : 6));
        }

        //Start a new Game
        gameController = new GameController(loggedUsers, isExpert);
        gameController.start();


        return true;
    }

    /**
     * Deserializes and handles an action message
     *
     * @param jsonMessage Json string which contains an action message
     * @param ch          the {@code Communicable} interface of the client who sent the message
     */
    private void handleActionMessage(String jsonMessage, Communicable ch) throws GameEndedException {
        if (gameController == null) {
            sendErrorMessage(ch, "ACTION", Messages.GAME_NOT_STARTED, 1);
            return;
        }

        try {
            ClientActionMessage actionMessage = ClientActionMessage.fromJSON(jsonMessage);
            gameController.handleActionMessage(actionMessage, ch);
        } catch (JsonSyntaxException e) {
            sendErrorMessage(ch, "ACTION", "Bad request (syntax error)", 3);
        }
    }

    /**
     * Periodically sends a "ping" message to every client and awaits for a "pong" response.
     * This is done on a parallel thread
     */
    public void startPingPong() {
        Timer timer = new Timer("PING THREAD");
        bound = sendPingAndReturnBound();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                synchronized (boundLock) {
                    if (pongCount < bound) {
                        for (PlayerClient user : loggedUsers) {
                            sendErrorMessage(user.getCommunicable(), "LOGIN", "Connection with one client lost", 3);
                        }
                        loggedUsers.clear();
                        gameController = null;
                        desiredNumberOfPlayers = -1;
                        System.out.println("Connection with one client lost, clearing the game...");
                    }
                }

                bound = sendPingAndReturnBound();
            }
        };

        timer.schedule(task, 0, 2000);
    }

    private int sendPingAndReturnBound() {
        synchronized (boundLock) {
            pongCount = 0;
        }
        Message ping = new Message();
        ping.setStatus("PING");
        int bound = 0;
        for (PlayerClient user : loggedUsers) {
            user.getCommunicable().sendMessageToClient(ping.toJson());
            bound++;
        }
        return bound;
    }

    public int getPongCount() {
        return pongCount;
    }

    public ArrayList<PlayerClient> getLoggedUsers() {
        return new ArrayList<>(loggedUsers);
    }

    public Game getGame() {
        if (gameController == null) return null;
        return gameController.getGame();
    }
}