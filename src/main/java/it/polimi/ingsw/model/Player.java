package it.polimi.ingsw.model;

import java.util.ArrayList;

public class Player {
    private final ArrayList<Assistant> hand;
    private final Dashboard dashboard;
    private final String name;
    private final Game game;
    private int numCoins;
    private final Wizard wizard = null;
    private final int initialTowers;

    public Player(String name, Game game, int initialTowers) {
        this.name = name;
        this.game = game;
        this.initialTowers = initialTowers;
        this.numCoins = 1;
        this.dashboard = new Dashboard();
        this.hand = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            hand.add(new Assistant(i%2 != 0 ? i/2 + 1 : i/2, i, this));
        }
    }

    /**
     * Returns the remaining towers in the player dashboard
     * @return the remaining towers in the player dashboard
     */
    public int getNumberOfTowers(){
        int res = initialTowers;
        for (Island island : game.getGameBoard().getIslands()) {
            if(island.getOwner() == this){
                res -= island.getNumOfTowers();
            }
        }
        return res;
    }

    public ArrayList<Assistant> getHand() {
        return new ArrayList<>(hand);
    }

    public Dashboard getDashboard() {
        return dashboard;
    }

    public String getName() {
        return name;
    }

    public Game getGame() {
        return game;
    }

    public int getNumCoins() {
        return numCoins;
    }

    public Wizard getWizard() {
        return wizard;
    }

    public void moveStudent(Place from, Place to, Student student){

    }

    public void playAssistant(Assistant assistant){

    }

    public void useCharacter(Character character){
        // chiama charachter.useEffect()
    }

    public void pickWizard(Wizard wizard){

    }

    private void checkProfessors(Color color){
        // Steal professor if possible
    }

    public void fillFromCloud(Cloud cloud){

    }
}