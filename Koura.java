public class Koura extends Enemy {
    public Koura(double speed, double handling, double aggression, int xp, Position pos) {
        super("Koura", "GREEN", speed, handling, aggression, xp, pos);
    }

    @Override
    public void decideNextMove(ArenaView arena) {
        move(AIController.decideMove(this, arena));
    }
}
