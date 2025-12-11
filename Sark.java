public class Sark extends Enemy {
    public Sark(double speed, double handling, double aggression, int xp, Position pos) {
        super("Sark", "YELLOW", speed, handling, aggression, xp, pos);
    }

    @Override
    public void decideNextMove(ArenaView arena) {
        move(AIController.patternMove(this, arena));
    }
}
