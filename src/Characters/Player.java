package src.Characters;

import java.awt.Graphics2D;

// import java.awt.Graphics2D;
// import java.awt.image.BufferedImage;
// import java.io.IOException;

// import javax.imageio.ImageIO;

import src.TEMP_Main.TEMP_GamePanel;
import src.TEMP_Main.TEMP_KeyHandler;

public class Player{
    TEMP_GamePanel gp;
    TEMP_KeyHandler keyH;
    private Characters activeCharacter;

    public Player(TEMP_GamePanel gp, TEMP_KeyHandler KeyH, Characters activeCharacter){
        this.gp = gp;
        this.keyH = KeyH;
        this.activeCharacter = activeCharacter;

    }

    public void setChar(Characters newCharacter){
        this.activeCharacter = newCharacter;
    }

    private double currentAngle = 0;
    private double targetAngle = 0;
    private double velocity = 0;

    public void update(){
        if (activeCharacter == null){ return; }

        if (keyH.upPressed) targetAngle = -Math.PI/2;    
        else if (keyH.downPressed) targetAngle = Math.PI/2;  
        else if (keyH.leftPressed) targetAngle = Math.PI;      
        else if (keyH.rightPressed) targetAngle = 0;

        double rotationSpeed = 0.1 + (activeCharacter.handling * 0.2);

        double diff = targetAngle - currentAngle;
        while (diff < -Math.PI) diff += Math.PI * 2;
        while (diff > Math.PI) diff -= Math.PI * 2;
        currentAngle += diff * rotationSpeed;

        boolean isMoving = keyH.upPressed || keyH.downPressed || keyH.leftPressed || keyH.rightPressed;

        if (isMoving) {
        velocity += 0.2 + (activeCharacter.speed * 0.05);
        } else {
            velocity *= (0.8 + (activeCharacter.stability * 0.15)); 
        }

        if (velocity > activeCharacter.speed) velocity = activeCharacter.speed;
        if (velocity < 0.1) velocity = 0;

        activeCharacter.position.col += Math.cos(currentAngle) * velocity;
        activeCharacter.position.row += Math.sin(currentAngle) * velocity;
    }

    // private double currentAngle = 0;

    // public double updateRotation() {
    //     double targetAngle = Math.atan2(velocityY, velocityX);
        
    //     // Simple interpolation: Move currentAngle towards targetAngle
    //     // Higher handling = faster rotation
    //     double rotationSpeed = 0.1 + (activeCharacter.handling * 0.2);
        
    //     // This handles the math for smooth turning
    //     currentAngle += (targetAngle - currentAngle) * rotationSpeed;

    //     return currentAngle;
    // }

    // private void updateDirection() {
    //     if (Math.abs(velocityX) > Math.abs(velocityY)) {
    //         activeCharacter.direction = (velocityX > 0) ? Direction.RIGHT : Direction.LEFT;
    //     } else if (Math.abs(velocityY) > 0.1) {
    //         activeCharacter.direction = (velocityY > 0) ? Direction.DOWN : Direction.UP;
    //     }
    // }

    public void draw(Graphics2D g2){
        // activeCharacter.loadPlayerImage();
        if (activeCharacter != null){
            // double radians = updateRotation();
            activeCharacter.draw(g2, gp, currentAngle);
        }
    }

}
