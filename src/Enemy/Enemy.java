package src.Enemy;

import src.Arena.ArenaView;

public abstract class Enemy {
    protected String name;
    protected String color;
    protected double speed;
    protected double handling;
    protected double aggression;
    protected int xpReward;

    protected Position position;
    protected Direction direction;

    protected int lives = 1;
    protected boolean alive = true;

    public Enemy(String name, String color, double speed, double handling,
            double aggression, int xpReward, Position startPos) {

        this.name = name;
        this.color = color;
        this.speed = speed;
        this.handling = handling;
        this.aggression = aggression;
        this.xpReward = xpReward;
        this.position = startPos;
        this.direction = Direction.UP;
    }

    // Each enemy decides its own next move using AIController
    public abstract void decideNextMove(ArenaView arena);

    // Helper method to move in a direction
    protected void move(Direction dir) {
        this.direction = dir;

        switch (dir) {
            case UP -> position.row--;
            case DOWN -> position.row++;
            case LEFT -> position.col--;
            case RIGHT -> position.col++;
        }
    }

    public Position getPosition() {
        return position;
    }

    public double getSpeed() {
        return speed;
    }

    public boolean isAlive() {
        return alive;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public int getXpReward() {
        return xpReward;
    }

    public void hitByDisc() {
        lives--;
        if (lives <= 0)
            alive = false;
    }

    public void hitJetwall() {
        lives--;
        if (lives <= 0)
            alive = false;
    }

    public Direction getDirection() {
        return direction;
    }
}
