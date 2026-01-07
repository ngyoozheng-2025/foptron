package src.Characters;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;
import javax.imageio.ImageIO;
import src.TEMP_Main.TEMP_GamePanel;

public abstract class Characters extends Entity {
    /// note to self: include changes to visuals with level up module
    protected String name;
    protected String color;
    protected int xp;
    protected int level;
    protected double stability;
    protected double handling;
    protected int disc_slot;
    protected int discs_owned;
    protected double lives;

    // Saved initial values for reset
    private final int initXp;
    private final int initLevel;
    private final int initDiscsOwned;
    private final double initLives;
    private boolean canUpdateLives = true;
    private Timer lifeCooldownTimer;

    // level-up glow timer
    private int glowTimer = 0;
    private final int GLOW_DURATION = 60;

    public Characters(String name, String color, int xp, int level, double speed,
            double stability, double handling, int disc_slot, int discs_owned, double lives,
            int start_row, int start_column, int alive) {
        super(start_row, start_column, speed, alive);
        this.name = name;
        this.color = color;
        this.xp = xp;
        this.level = level;
        this.stability = stability;
        this.handling = handling;
        this.disc_slot = disc_slot;
        this.discs_owned = discs_owned;
        this.lives = lives;

        // record initial values for reset() later
        this.initXp = xp;
        this.initLevel = level;
        this.initDiscsOwned = discs_owned;
        this.initLives = lives;
    }

    // getters and setters
    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public int getXp() {
        return xp;
    }

    public int getLevel() {
        return level;
    }

    public void setSpeed(double value) {
        speed += value;
    }

    public void setStability(double value) {
        stability += value;
    }

    public void setHandling(double value) {
        handling += value;
    }

    public void setDiscsOwned(int value) {
        discs_owned = Math.min(this.disc_slot, discs_owned + value);
    } // change

    public void setLevel(int newLevel) {
        this.level = newLevel;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public void setLives(double lives) {
        this.lives = lives;
    }

    // method to update lives, note value can be +ve or -ve, also determines if a
    // character is dead
    public boolean updateLives(double value) {
        if (!canUpdateLives) {
            return false; // cooldown active → no damage applied
        }

        canUpdateLives = false;

        lives += value;
        if (lives <= 0) {
            alive = false;
        }

        if (lifeCooldownTimer != null && lifeCooldownTimer.isRunning()) {
            lifeCooldownTimer.stop();
        }

        lifeCooldownTimer = new Timer(500, e -> canUpdateLives = true);
        lifeCooldownTimer.setRepeats(false);
        lifeCooldownTimer.start();

        return true;
    }

    // stat increase method (note that stats increase differ for each character.
    // this method is overridden in Tron.java and Kevin.java respectively)
    protected abstract void applyStatIncrease();

    // for image loading
    public abstract String getBaseImagePath();

    public abstract String getOverlayImagePath();

    public void loadPlayerImage() {
        try {
            sprite = ImageIO.read(getClass().getResourceAsStream(getBaseImagePath()));
            spriteOverlay = ImageIO.read(getClass().getResourceAsStream(getOverlayImagePath()));
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("Error loading sprite for " + name);
            e.printStackTrace();
        }
    }

    public void startGlow() {
        this.glowTimer = GLOW_DURATION;
    }

    public void updateGlow() {
        if (glowTimer > 0) {
            glowTimer--;
        }
    }

    List<TrailPoint> trail = new ArrayList<>();

    public void draw(Graphics2D g2, TEMP_GamePanel gp, double radians, double velocity) {
        if (sprite == null) {
            System.out.println("No sprite image for " + this.name);
            return;
        }
        if (spriteOverlay == null) {
            System.out.println("No overlay image for " + this.name);
            return;
        }

        // determines colour for each sprite
        Color charColor = new Color(255, 0, 0); // defaulted as red to indicate error
        if (this.name.equals("Tron")) {
            charColor = new Color(49, 213, 247);

        } else if (this.name.equals("Kevin")) {
            charColor = new Color(255, 255, 255);
        } else {
            // red means name is neither Tron nor Kevin
            System.out.println(this.name);
        }

        if (velocity > 1.0) { // Only leave a trail when moving fast
            TrailPoint p = new TrailPoint();
            p.col = position.col + gp.tileSize / 2.0; // Center of sprite
            p.row = position.row + gp.tileSize / 2.0;
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
            g2.setColor(charColor);
            int thickness = (int) ((gp.tileSize - 20) * 0.4 * p.life);
            int length = (int) (p.velocity * 5.0);

            g2.translate(p.col, p.row);
            g2.rotate(p.angle);
            g2.fillRoundRect(-length, -thickness / 2, length, thickness, thickness, thickness);
            g2.setTransform(oldTrail);
        }

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        java.awt.geom.AffineTransform old = g2.getTransform();

        int centerX = (int) position.col + gp.tileSize / 2;
        int centerY = (int) position.row + gp.tileSize / 2;

        // glow on level up
        if (glowTimer > 0) {
            updateGlow();
            float glowAlpha = (float) glowTimer / GLOW_DURATION;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glowAlpha * 0.7f));
            g2.setColor(charColor);
            int padding = 0;
            g2.fillOval((int) position.col - padding / 2, (int) position.row - padding / 2, gp.tileSize + padding,
                    gp.tileSize + padding);
        }

        // draw sprite
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2.rotate(radians, centerX, centerY);
        g2.drawImage(sprite, (int) position.col, (int) position.row, gp.tileSize, gp.tileSize, null);
        // draw sprite overlay
        float stripeAlpha = Math.min(1.0f, 0.4f + (level / 100.0f) * 0.6f);
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

    public void useDisc() {
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

    // Method called when character is hit by a disc
    // Note: Using Object to avoid package import issues (Enemy is in default
    // package)
    public void hitByDisc(Object owner) {
        updateLives(-1.0); // Lose a life when hit by disc
    }

    public void collectDisc() {
        discs_owned++;
    }

    /**
     * Restore the character to the initial state loaded from file.
     * Called when returning to main menu / restarting a round.
     */
    public void reset() {
        this.alive = true;
        this.xp = initXp;
        this.level = initLevel;
        this.discs_owned = initDiscsOwned;
        this.lives = initLives;
        // stop any level-up glow
        this.glowTimer = 0;
    }

    /**
     * Adds XP to the character and handles level ups automatically.
     * Extra XP carries over to next level.
     */
    public void gainXp(int amount) {
        if (amount <= 0)
            return;

        xp += amount;

        // Level up as long as XP exceeds current level threshold
        while (xp >= xpToLevelUp()) {
            xp -= xpToLevelUp(); // subtract the threshold (reset XP bar)
            level++; // increment level
            // level-up side effects:
            if (level % 10 == 0) {
                updateLives(1.0);
            }
            if (level % 15 == 0) {
                disc_slot += 1;
            }
            startGlow();
            applyStatIncrease();
            onLevelUp();
        }
    }

    /**
     * Returns the XP required to reach the next level
     */
    public int xpToLevelUp() {
        // Matches your earlier comment:
        // - Levels 1..9 require 100 XP each
        // - From level 10 onwards the requirement rises (we use 1000 + 200*(level-10))
        if (level < 10) {
            return 100;
        } else {
            return 1000 + (level - 10) * 200;
        }
    }

    /**
     * Optional: called whenever the player levels up (hook for logging, sounds)
     */
    private void onLevelUp() {
        System.out.println(getName() + " leveled up! Now level " + level);
    }
}
