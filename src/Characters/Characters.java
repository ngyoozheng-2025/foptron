package src.Characters;

import java.awt.Graphics2D;
import java.io.IOException;

import javax.imageio.ImageIO;

import src.TEMP_Main.TEMP_GamePanel;

public abstract class Characters extends Entity{
    ///note to self: include changes to visuals with level up module
    protected String name;
    protected String color;
    protected int xp;
    protected int level;
    protected double stability;
    protected double handling;
    protected int disc_slot;
    protected int discs_owned;
    protected double lives;

    //Constructor
    public Characters(String name, String color, int xp, int level, double speed, 
        double stability, double handling, int disc_slot, int discs_owned, double lives,
        int start_row, int start_column, int alive){
        super(start_row,start_column,speed,alive);
        this.name = name;
        this.color = color;
        this.xp = xp;
        this.level = level;
        this.stability = stability;
        this.handling = handling;
        this.disc_slot = disc_slot;
        this.discs_owned = discs_owned;
        this.lives = lives;
    }

    // getters and setters
    public String getName() {return name;}
    public String getColor() {return color;}
    public int getXp() {return xp;}
    public int getLevel() {return level;}

    public void setSpeed(double value){speed += value;}
    public void setStability(double value){stability += value;}
    public void setHandling(double value){handling += value;}
    public void setDiscsOwned(int value){
        discs_owned = Math.min(this.disc_slot, discs_owned + value);
    }

    // method to update lives, note value can be +ve or -ve, also determines if a character is dead
    public void updateLives(double value){
        lives += value;
        if(lives<=0){
            this.alive = false;
        }
    }

    //gain xp method
    public void gainXp(int value){
        xp += value;
    }

    // stat increase method (note that stats increase differ for each character. 
    // this method is overriden in Tron.java and Kevin.java respectively)
    protected abstract void applyStatIncrease();

    // level up method (each 100 xp increments level before level 10, and subsequently, each 1000 xp increments level)
    public void levelUp(){
        boolean leveled_up = false;
        if (level <10){
            if (xp % 100 == 0){
                level += 1;
                leveled_up = true;
            }
        }
        else{
            if ((xp-1000) % 200 == 0){
                if (level < 99){
                    level += 1;
                    leveled_up = true;
                }
            }
        }
        if (leveled_up){
            // +1 life every 10 levels
            if (level % 10 == 0){
                updateLives(1.0);;
            }
            // Additional disc slot every 15 levels
            if (level % 15 ==0){
                disc_slot += 1;
            }
            applyStatIncrease();
        }
    }

    //for image loading
    public abstract String getImagePath();

    public void loadPlayerImage(){
        try{
            this.sprite = ImageIO.read(getClass().getResourceAsStream(getImagePath()));
        }
        catch (IOException |IllegalArgumentException e){
            System.out.println("Error loading sprite for " + name);
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2, TEMP_GamePanel gp){
        if (this.sprite == null) return;

        java.awt.geom.AffineTransform old = g2.getTransform();

        int centerX = position.col + gp.tileSize/2;
        int centerY = position.row + gp.tileSize/2;

        double radians = 0;
        switch (direction) {
            case UP -> radians = 0;
            case DOWN -> radians = Math.PI;
            case LEFT -> radians = Math.PI * 1.5;
            case RIGHT -> radians = Math.PI * 0.5;
        }

        g2.rotate(radians, centerX, centerY);
        g2.drawImage(sprite, 
                     position.col, 
                     position.row, 
                     gp.tileSize, gp.tileSize, null);
        g2.setTransform(old);
    }
}
