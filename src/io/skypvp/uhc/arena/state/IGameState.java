package io.skypvp.uhc.arena.state;

public interface IGameState {
    
    /**
     * The code that shall be executed while inside this state.
     */
    
    public void run();
    
    /**
     * Checks if the state may exit and move on to the next one.
     * @return boolean if the state should exit or not.
     */
    
    public boolean canMoveOn();
    
    /**
     * Checks if the state may continue.
     * If both this method and {@link #canMoveOn()} fails, the state exits
     * and the state behind in the StateManager is entered.
     * @return boolean if the state should continue or not.
     */
    
    public boolean canContinue();
    
    /**
     * The code that shall be executed when the game is entering this state.
     */
    
    public void onEnter();
    
    /**
     * The code that shall be executed when this game state is transitioning to another.
     */
    
    public void onExit();
}
