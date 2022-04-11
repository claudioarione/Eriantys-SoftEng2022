package it.polimi.ingsw.model.characters;

import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.model.game_actions.PlayerActionPhase;
import it.polimi.ingsw.model.game_objects.Color;
import it.polimi.ingsw.model.game_objects.gameboard_objects.Island;
import it.polimi.ingsw.model.game_objects.Student;

import java.util.ArrayList;

public abstract class Character {

    private final int cost;
    private final CharacterName characterName;
    private boolean hasCoin;

    public Character(CharacterName characterName) {
        this.characterName = characterName;
        this.cost = characterName.getInitialCost();
        this.hasCoin = false;
    }

    public int getCost() {
        return hasCoin ? cost + 1 : cost;
    }

    public CharacterName getCardName() {
        return characterName;
    }

    /**
     * Adds the coin on the {@code Character} after its first use
     */
    public void addCoinAfterFirstUse() {
        hasCoin = true;
    }

    /**
     * Uses the effect of the {@code Character}
     *
     * @param currentPlayerActionPhase the {@code PlayerActionPhase} which the effect is used in
     * @param island                   the {@code Island} which the {@code Character} affects
     * @param color                    the {@code Color} which the {@code Character} affects
     * @param srcStudents              the students to be moved to the destination
     * @param dstStudents              the students to be moved to the source (only if the effect is a "swap" effect)
     * @throws InvalidCharacterException       if
     * @throws CharacterAlreadyPlayedException if the current {@code Player} has already played a {@code Character}
     * @throws StudentNotOnTheCardException    if the {@code Character} has not the selected {@code Student} on it
     * @throws InvalidActionException          if the action is not valid
     * @throws InvalidStudentException         if the {@code Student} is not valid
     */
    public abstract void useEffect(
            PlayerActionPhase currentPlayerActionPhase, Island island, Color color,
            ArrayList<Student> srcStudents, ArrayList<Student> dstStudents
    ) throws InvalidCharacterException, CharacterAlreadyPlayedException, StudentNotOnTheCardException, InvalidActionException, InvalidStudentException;
}
