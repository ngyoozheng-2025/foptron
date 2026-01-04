public class Clu extends Enemy {
    public Clu(double speed, double handling, double aggression, int xp, Position pos) {
        super("Clu", "GOLD", speed, handling, aggression, xp, pos);
    }

    @Override
    public void decideNextMove(ArenaView arena) {
        move(AIController.decideMove(this, arena));
    }
}
