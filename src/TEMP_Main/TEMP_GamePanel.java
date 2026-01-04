package src.TEMP_Main;

import javax.swing.JPanel;

import src.Characters.CharacterLoader;
import src.Characters.Player;
import src.Characters.Characters;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.Graphics;

public class TEMP_GamePanel extends JPanel implements Runnable{
    final int originalTilesize = 16;
    final int scale = 3;

    public final int tileSize = originalTilesize * scale;
    final int maxScreenCol = 16;
    final int maxScreenRow = 12;
    final int screenWidth = maxScreenCol * tileSize;
    final int screenHeight = maxScreenRow * tileSize;

    int FPS = 60;

    TEMP_KeyHandler keyH = new TEMP_KeyHandler();
    Thread gameThread;

    ArrayList<Characters> characterList;
    Player player;


    public TEMP_GamePanel(){

        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);
        characterList = CharacterLoader.loadCharacters("src/Characters/Characters.txt");
        player = new Player(this, keyH, characterList.get(0));

    }

    public void startGameThread(){
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run(){
        double drawInterval = 1000000000/FPS;
        double delta =0;
        long lastTime = System.nanoTime();
        long currentTime;

        while(gameThread != null){
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta>= 1){
                update();
                repaint();
                delta --;
            }
        }    
    }

    public void update(){
        player.update();
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        player.draw(g2);
        g2.dispose();
    }
}
