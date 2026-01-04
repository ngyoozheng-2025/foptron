import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import src.Characters.*;
import src.TEMP_Main.TEMP_GamePanel;
import src.TEMP_Main.TEMP_KeyHandler;

public class GameEngine extends JFrame {
    private enum GameState {
        MENU,
        START_ROUND,
        PLAYING,
        POST_ROUND,
        GAME_OVER
    }
    private GameState gameState = GameState.START_ROUND;
    
    private Characters activeCharacter;
    private Player player;

    private final String selectedArena;

    private HUDPanel hud;
    
    private TEMP_GamePanel gamePanel;
    private TEMP_KeyHandler keyHandler;

    private final StoryManager storyManager = new StoryManager("Member 5/story.txt");

    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<Disc> activeDiscs = new ArrayList<>();
    private ArenaView arena;
    private final int mapRows = 20;  // Adjust based on arena dimensions
    private final int mapCols = 20;  // Adjust based on arena dimensions
    
    private boolean isRunning = true;
    private int round = 1;

    public GameEngine(Characters character, String arena) {
        this.activeCharacter = character;
        this.selectedArena = arena;
        activeCharacter.loadPlayerImage();
        initUI();
        startGameLoop();
    }

    private void initUI() {
        setTitle("FOP Tron - " + activeCharacter.getName() + " | " + selectedArena);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1040, 740);
        setResizable(false);
        setLocationRelativeTo(null);

        // Key handler
        keyHandler = new TEMP_KeyHandler();

        //Create HUD Panel (right side)
        hud = new HUDPanel();
        hud.setPreferredSize(new Dimension(220, 740));

        //Create Game Panel (center - arena + player + enemies)
         gamePanel = new TEMP_GamePanel();
         gamePanel.setPreferredSize(new Dimension(820, 740));
         gamePanel.setFocusable(true);
         gamePanel.addKeyListener(keyHandler);

        // Player controller
        player = new Player(gamePanel, keyHandler, activeCharacter);

        //Layout: Game center, HUD right
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(gamePanel, BorderLayout.CENTER);
        mainPanel.add(hud, BorderLayout.EAST);

        add(mainPanel);
        setVisible(true);

        //Request focus for key input
        gamePanel.requestFocusInWindow();
    }

    //Inner class for game rendering
    class GamePanel extends JPanel {
        private String character;
        private String arena;

        public GamePanel(String character, String arena) {
            this.character = character;
            this.arena = arena;
            setBackground(new Color(20, 20, 40)); // Dark blue background
            setPreferredSize(new Dimension(820, 740));
            setFocusable(true); //allow keyboard input
            
            //Add key listener for input
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    handleInput(e.getKeyCode());
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) { //draw thing in jpanel
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // TODO: Member 1 - Draw arena grid and obstacles
            // TODO: Member 2 - Draw player and jetwall trail
            // TODO: Member 3 - Draw enemies and discs

            // Placeholder info
            g2d.setColor(Color.CYAN);
            g2d.setFont(new Font("Monospaced", Font.BOLD, 16));
            g2d.drawString("Arena: " + arena, 20, 30);
            g2d.drawString("Character: " + character, 20, 55);
            g2d.drawString("Press WASD to move | SPACE to throw disc | P to pause", 20, 700);
        }

        private void handleInput(int keyCode) {
            // TODO: Member 2 - Handle WASD movement
            switch (keyCode) {
                case KeyEvent.VK_W: // Move up
                    System.out.println("Move Up");
                    break;
                case KeyEvent.VK_A: // Move left
                    System.out.println("Move Left");
                    break;
                case KeyEvent.VK_S: // Move down
                    System.out.println("Move Down");
                    break;
                case KeyEvent.VK_D: // Move right
                    System.out.println("Move Right");
                    break;
                case KeyEvent.VK_SPACE: // Throw disc
                    System.out.println("Throw Disc");
                    break;
                case KeyEvent.VK_P: // Pause
                    System.out.println("Pause");
                    break;
            }
            repaint();
        }
    }

    /**
     * Main game loop running at 60 FPS
     */
    private void startGameLoop() {
        Thread gameThread = new Thread(() -> {
            long lastTime = System.nanoTime();
            double fps = 60.0;
            double ns = 1_000_000_000.0 / fps;
            double delta = 0;

            while (isRunning) {
                long now = System.nanoTime();
                delta += (now - lastTime) / ns;
                lastTime = now;
 
                if (delta >= 1) {
                    update();
                    gamePanel.repaint();
                    delta--;
                }
            }
        });
        gameThread.setDaemon(true);
        gameThread.start();
    }

    /**
     * Update game logic each frame
     */
    private void update() {
        switch (gameState) {
            case START_ROUND:
                startNewRound();
                gameState = GameState.PLAYING;
                break;
                
                case PLAYING:
                    player.update();
                    
                    // Update all active enemies
                    for (Enemy enemy : enemies) {
                        if (enemy.isAlive()) {
                            enemy.decideNextMove(arena);  // Let enemy AI decide movement
                        }
                    }
                    
                    // Update all active discs and remove inactive ones
                    for (int i = activeDiscs.size() - 1; i >= 0; i--) {
                        Disc disc = activeDiscs.get(i);
                        disc.update(arena, activeCharacter, enemies);
                        
                        // Remove disc if it's no longer active
                        if (!disc.isActive()) {
                            activeDiscs.remove(i);
                        }
                    }
                    
                    // Check if all enemies are defeated
                    boolean allEnemiesDefeated = enemies.stream().allMatch(e -> !e.isAlive());
                    if (allEnemiesDefeated) {
                        gameState = GameState.POST_ROUND;
                    }
                    
                    // REMARK: Member 2 – update player
                    // REMARK: Member 1 – collision detection
                    hud.updateStats(
                        (int) activeCharacter.getLives(),
                        activeCharacter.getXp(),
                        activeCharacter.getLevel(),
                        activeCharacter.getDiscsOwned()
                );

                if (!activeCharacter.isAlive()) {
                    gameState = GameState.GAME_OVER;
                }
                    break;
                    
                case POST_ROUND:
                    handlePostRound();
                    break;
                        
                case GAME_OVER:
                    isRunning = false;
                    break;
        }
    }

    //Round Start
    private void startNewRound() {
        hud.addEvent("Round " + round + " Started");
        if (round == 1 ) {
            storyManager.playCutscene("Intro");
        }
        startRound();
        // REMARK: Member 1 – reset arena
        // REMARK: Member 3 – spawn enemies
    }    
    
    private void startRound() {
        // Load enemies from enemies.txt file
        enemies = EnemyLoader.loadEnemies("enemies.txt", arena, mapRows, mapCols);
        hud.addEvent("Spawned " + enemies.size() + " enemies");
    }
    
    // Called when player throws a disc
    public void throwDisc(Position pos, Direction dir) {
        if (activeCharacter.getDiscsOwned() > 0) {
            Disc newDisc = new Disc(pos, dir, null);
            activeDiscs.add(newDisc);
            activeCharacter.useDisc();  // Decrease disc count
            hud.addEvent("Disc thrown! Remaining: " + activeCharacter.getDiscsOwned());
        }
    }
    private void handlePostRound() {
        round++;
        hud.addEvent("Round Cleared!");

        activeCharacter.gainXp(100);
        activeCharacter.levelUp();

        gameState = GameState.START_ROUND;
    }

    //Game End
    private void gameOver() {
        isRunning = false;

        JOptionPane.showMessageDialog(
                this,
                "Game Over!\nRound: " + round +
                "\nLevel: " + activeCharacter.getLevel(),
                "Game Over",
                JOptionPane.INFORMATION_MESSAGE
        );

        Leaderboard.addEntry(
                activeCharacter.getName(),
                activeCharacter.getLevel(),
                activeCharacter.getXp()
        );

        SaveSystem.saveProgress(activeCharacter);

        dispose();
        new MainMenu();
    }
}