package src.Characters;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    public void setDiscsOwned(int value){discs_owned = Math.min(this.disc_slot, discs_owned + value);} //change
    public void setLevel(int newLevel) {this.level = newLevel;}

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
    public abstract String getBaseImagePath();
    public abstract String getOverlayImagePath();

    public void loadPlayerImage(){
        try{
            this.sprite = ImageIO.read(getClass().getResourceAsStream(getBaseImagePath()));
            this.spriteOverlay = ImageIO.read(getClass().getResourceAsStream(getOverlayImagePath()));
        }
        catch (IOException |IllegalArgumentException e){
            System.out.println("Error loading sprite for " + name);
            e.printStackTrace();
        }
    }

    List<TrailPoint> trail = new ArrayList<>();

    public void draw(Graphics2D g2, TEMP_GamePanel gp, double radians, double velocity){
        if (this.sprite == null) {
            System.out.println("No sprite image for " + this.name);
            return;
        }
        if (this.spriteOverlay == null) {
            System.out.println("No overlay image for " + this.name);
            return;
        }

        
        if (velocity > 1.0) { // Only leave a trail when moving fast
            TrailPoint p = new TrailPoint();
            p.col = position.col + gp.tileSize/2.0; // Center of sprite
            p.row = position.row + gp.tileSize/2.0;
            p.angle = radians;
            p.velocity = velocity;
            trail.add(p);
        }

        // FADE TRAIL
        for (int i = trail.size() - 1; i >= 0; i--) {
            trail.get(i).life -= 0.035f; // Adjust this to change trail length
            if (trail.get(i).life <= 0) {
                trail.remove(i);
            }
        }
        for (int i = 0; i < trail.size(); i++) {
            TrailPoint p = trail.get(i);
            
            java.awt.geom.AffineTransform oldTrail = g2.getTransform();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, p.life * 0.95f));
            if (this.name.equals("tron")){
                g2.setColor(new Color(49, 213, 247)); 
            }
            else if (this.name.equals("kevin")){
                g2.setColor(new Color(255, 255, 255)); 
            }
            else{
                g2.setColor(new Color(255, 255, 255));
            }
            int thickness = (int) ((gp.tileSize-20)* 0.4 * p.life); 
            int length = (int) (p.velocity * 5.0); 

            g2.translate(p.col, p.row);
            g2.rotate(p.angle);
            g2.fillRoundRect(-length, -thickness / 2, length, thickness, thickness, thickness);
            g2.setTransform(oldTrail);
        }

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        java.awt.geom.AffineTransform old = g2.getTransform();

        int centerX = (int) position.col + gp.tileSize/2;
        int centerY = (int) position.row + gp.tileSize/2;

        g2.rotate(radians, centerX, centerY);
        g2.drawImage(sprite, 
                     (int) position.col, 
                     (int) position.row, 
                     gp.tileSize, gp.tileSize, null);
        float stripeAlpha = Math.min(1.0f, level / 10.0f); 
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, stripeAlpha));
        g2.drawImage(spriteOverlay, (int) position.col, (int) position.row, gp.tileSize, gp.tileSize, null);
        g2.setTransform(old);
    }

    public String getDescription() {
    // Format: Color • Handling/Stability/Speed summary
    return color + " • Handling: " + String.format("%.1f", handling) +
           " • Stability: " + String.format("%.1f", stability) +
           " • Speed: " + String.format("%.1f", speed);

        }
    

    public void useDisc () {
        if (discs_owned > 0) {
            discs_owned--;
        }
    }
    public double getSpeed() { 
        return speed;
    }

    public double getLives() { 
        return lives; 

    }
    
    public int getDiscsOwned() { 
        return discs_owned; 

    }
    public int getDiscSlot() { 
        return disc_slot; 
    }
}
