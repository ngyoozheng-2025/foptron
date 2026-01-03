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

    public void update(){
        if (activeCharacter == null){ return; }

        // if(keyH == null){return; }

        if(keyH.upPressed == true){
            activeCharacter.direction = Direction.UP;
            activeCharacter.position.row -= activeCharacter.speed;
        }
        else if(keyH.downPressed == true){
            activeCharacter.direction = Direction.DOWN;
            activeCharacter.position.row += activeCharacter.speed;
        }
        else if(keyH.leftPressed == true){
            activeCharacter.direction = Direction.LEFT;
            activeCharacter.position.col -= activeCharacter.speed;
        }
        else if(keyH.rightPressed == true){
            activeCharacter.direction = Direction.RIGHT;
            activeCharacter.position.col += activeCharacter.speed;
        }
    }
    public void draw(Graphics2D g2){
        // activeCharacter.loadPlayerImage();
        if (activeCharacter != null){
            activeCharacter.draw(g2, gp);
        }
    }
    //     if (activeCharacter == null || activeCharacter.sprite == null) return;
    //     int centerX = activeCharacter.position.col + gp.tileSize/2;
    //     int centerY = activeCharacter.position.row + gp.tileSize/2;

    //     double radians = 0;
    //     switch (activeCharacter.direction) {
    //         case UP -> radians = 0;
    //         case DOWN -> radians = Math.PI;
    //         case LEFT -> radians = Math.PI * 1.5;
    //         case RIGHT -> radians = Math.PI * 0.5;
    //     }

    //     g2.rotate(radians, centerX, centerY);
    //     g2.drawImage(activeCharacter.sprite, 
    //                  activeCharacter.position.col, 
    //                  activeCharacter.position.row, 
    //                  gp.tileSize, gp.tileSize, null);
    // }
}
