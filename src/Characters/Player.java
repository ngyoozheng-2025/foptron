package src.Characters;

import java.awt.Graphics2D;

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

    public void draw(Graphics2D g2){
        if (activeCharacter != null){
            activeCharacter.draw(g2, gp, currentAngle, velocity);
        }
    }

}
