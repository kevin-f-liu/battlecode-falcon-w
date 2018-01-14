import bc.*;


public class Player {
	public static void main(String[] args) {
        // Literally do nothing.

        // Connect to the manager, starting the game
        GameController gc = new GameController();

        // Direction is a normal java enum.
        Direction[] directions = Direction.values();

        while (true) {
            System.out.println("DUMMY round: "+gc.round());
            // Submit the actions we've done, and wait for our next turn.
            gc.nextTurn();
        }
    }
}
