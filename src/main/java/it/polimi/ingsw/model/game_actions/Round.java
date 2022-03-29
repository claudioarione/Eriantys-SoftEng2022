package it.polimi.ingsw.model.game_actions;

import it.polimi.ingsw.exceptions.EmptyBagException;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.game_actions.action_phase.PlayerActionPhase;
import it.polimi.ingsw.model.game_actions.action_phase.PlayerActionPhaseFactory;
import it.polimi.ingsw.model.game_objects.Assistant;
import it.polimi.ingsw.model.game_objects.Cloud;

import java.util.ArrayList;
import java.util.Comparator;

public class Round {
    private final int firstPlayerIndex;
    private int currentPlayerIndex = -1;
    private int currentAssistantIndex = -1;
    private final Game game;
    private PlanningPhase planningPhase;
    private ArrayList<Assistant> playedAssistants;
    private PlayerActionPhase currentPlayerActionPhase;
    private boolean isLastRound;

    public Round(int firstPlayerIndex, Game game) {
        this.firstPlayerIndex = firstPlayerIndex;
        this.game = game;
        this.isLastRound = false;
    }

    public Round(int firstPlayerIndex, Game game, boolean isLastRound) {
        this.firstPlayerIndex = firstPlayerIndex;
        this.game = game;
        this.isLastRound = isLastRound;
    }

    public void startPlanningPhase() {
        try {
            fillClouds();
        } catch (EmptyBagException e) {
            isLastRound = true;
        }

        planningPhase = new PlanningPhase(createPlayersArray(), this);
    }

    public void endPlanningPhase(ArrayList<Assistant> playedAssistants) {
        this.playedAssistants = playedAssistants;

        // Sort assistants based on the "value" attribute of each card
        this.playedAssistants = (ArrayList<Assistant>) playedAssistants.stream()
                .sorted(Comparator.comparingInt(Assistant::getValue))
                .toList();

        nextPlayerActionPhase();
    }

    public void nextPlayerActionPhase() {
        currentAssistantIndex++;

        if (currentAssistantIndex == game.getPlayers().size()) {
            Player nextFirstPlayer = playedAssistants.get(0).getPlayer();
            game.nextRound(game.getPlayers().indexOf(nextFirstPlayer));
        } else {
            Player currentPlayer = playedAssistants.get(currentAssistantIndex).getPlayer();
            currentPlayerIndex = game.getPlayers().indexOf(currentPlayer);
            currentPlayerActionPhase = PlayerActionPhaseFactory.createPlayerActionPhase(
                    playedAssistants.get(currentAssistantIndex), game.getGameBoard(), game.isExpert()
            );
        }
    }

    public boolean isLastRound() {
        return isLastRound;
    }


    private void fillClouds() throws EmptyBagException {
        for (Cloud cloud : game.getGameBoard().getClouds()) {
            cloud.fillFromBag(game.getGameBoard().getBag());
        }
    }

    private ArrayList<Player> createPlayersArray() {
        ArrayList<Player> playersInOrder = new ArrayList<>();
        int numPlayers = game.getPlayers().size();
        for (int i = 0; i < numPlayers; i++) {
            playersInOrder.add(game.getPlayers().get((i + firstPlayerIndex) % numPlayers));
        }
        return playersInOrder;
    }

    public PlanningPhase getPlanningPhase() {
        return planningPhase;
    }

    public PlayerActionPhase getCurrentPlayerActionPhase() {
        return currentPlayerActionPhase;
    }

    public int getFirstPlayerIndex() {
        return firstPlayerIndex;
    }
}
