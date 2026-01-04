package src.Characters;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import src.TEMP_Main.TEMP_GamePanel;

public abstract class Entity {
    protected Position position;
    protected Direction direction = Direction.UP;
    protected double speed;
    protected boolean alive;
    public BufferedImage sprite;

    public Entity(int row, int col, double speed, int alive) {
        this.position = new Position(row, col);
        this.speed = speed;
        if (alive == 1){
            this.alive = true;
        }
        else{
            this.alive = false;
        }
    }

    public abstract void update();
    public abstract void draw(Graphics2D g2, TEMP_GamePanel gp, double radians, double velocity);

    public Position getPosition() { return position; }
    public boolean isAlive() { return alive; }
}

