package src.Enemy;

import src.AI.AIController;
import src.Arena.ArenaView;

public class Rinzler extends Enemy {
    public Rinzler(double speed, double handling, double aggression, int xp, Position pos) {
        super("Rinzler", "RED", speed, handling, aggression, xp, pos);
    }

    @Override
    public void decideNextMove(ArenaView arena) {
        move(AIController.decideMove(this, arena));
    }
}
