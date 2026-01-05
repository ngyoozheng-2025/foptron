import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import src.Characters.Characters;

public class GameEngine extends JFrame implements KeyListener {

    //Game state
    private enum GameState {
        PLAYING,
        PAUSED,
        ROUND_WIN,
        GAME_OVER,
        CUTSCENE
    }

    //Core component
    private Arena arena;
    private Characters player;
    private double playerPixelX, playerPixelY; // Smooth pixel position
    private Direction playerDirection;

    private ArrayList<Enemy> enemies;
    private ArrayList<Disc> activeDiscs;

    //Smooth movement
    private double currentAngle = 0;
    private double targetAngle = 0;
    private double velocity = 0;
    private List<TrailPoint> playerTrail = new ArrayList<>();

    private static class TrailPoint {
        double x, y, angle, velocity;
        float life = 1.0f;
    }

    //UI
    private GamePanel gamePanel;
    private HUDPanel hudPanel;

    //Game state
    private GameState gameState;
    private Thread gameThread;
    private volatile boolean running = false;
    private int roundNumber;
    private int totalScore;
    private String difficulty;

    //Timing constant
    private static final int FPS = 60;
    private static final double ns_per_frame = 1_000_000_000.0 / FPS;
    private static final int enemy_update_frames = 30; //Update enemies every 0.5 seconds
    private static final int disc_cooldown_frames = 300; //5 seconds at 60 FPS
    private static final int jetwall_place_frame = 10; //Place jetwall every few frames

    //Movement tracking
    private int enemyUpdateCounter = 0;
    private int discCooldownCounter = 0;
    private int jetwallPlaceCounter = 0;
    private boolean[] keysPressed = new boolean[256];

    //Tile and screen
    private static final int tile_size = 15;
    private static final int arena_pixel_size = Arena.SIZE * tile_size;

    //Story
    private StoryManager storyManager;


    //initialize game with selected character and arena
    public GameEngine(Characters character, String arenaName) {
        this.player = character;
        this.roundNumber = 1;
        this.totalScore = 0;
        this.difficulty = "EASY"; //default difficulty
        this.activeDiscs = new ArrayList<>();
        this.playerDirection = Direction.RIGHT;
        this.targetAngle = 0; //facing right

        //initialize story system
        this.storyManager = new StoryManager("story.txt");
        
        //setup window
        setTitle("FOP Tron - " + player.getName());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());
        
        //handle window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });

        //initialize arena based on selection
        initializeArena(arenaName);

        //create game panel 
        gamePanel = new GamePanel();
        gamePanel.setPreferredSize(new Dimension(arena_pixel_size,arena_pixel_size));
        add(gamePanel, BorderLayout.CENTER);
        // Create HUD panel
        hudPanel = new HUDPanel();
        add(hudPanel, BorderLayout.EAST);
        
        //create control panel at bottom
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.SOUTH);
        
        //keyboard input
        addKeyListener(this);
        gamePanel.addKeyListener(this);
        setFocusable(true);
        gamePanel.setFocusable(true);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        
        //start new round
        startNewRound();
        
        //start game loop thread
        startGameLoop();

    }    

    //initialize the arena based on selection
    private void initializeArena(String arenaName) {
        switch(arenaName) {
            case "Arena 1" -> arena = new Arena("ClassicGrid");
            case "Arena 2" -> arena = new Arena("NeonMaze");
            case "Arena 3" -> arena = new Arena("OpenFrontier");
            case "Random" -> arena = new Arena("Procedural", System.currentTimeMillis());
            default -> arena = new Arena("ClassicGrid");
        }
    }

    //create the bottom control panel
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.BLACK);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 5));
        
        JLabel controlsLabel = new JLabel("WASD: Move | SPACE: Throw Disc | P: Pause | ESC: Menu");
        controlsLabel.setForeground(new Color(0, 200, 255));
        controlsLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));
        panel.add(controlsLabel);
        
        return panel;
    }

    //start a new round (spawn player and enemies)
    private void startNewRound() {
        gameState = GameState.PLAYING;
        
        //spawn player at arena spawn point (convert grid to pixel coordinates)
        Position spawn = arena.getPlayerSpawn();
        playerPixelX = spawn.col * tile_size + tile_size / 2.0;
        playerPixelY = spawn.row * tile_size + tile_size / 2.0;
        arena.setPlayerPosition(spawn);
        
        //reset movement
        velocity = 0;
        currentAngle = 0;
        targetAngle = 0;
        playerTrail.clear();
        
        //update difficulty based on round 
        updateDifficulty();
        
        //load and spawn enemies
        enemies = EnemyLoader.loadEnemies("enemies.txt", difficulty, arena, Arena.SIZE, Arena.SIZE);
        
        //clear active dics
        activeDiscs.clear();
        discCooldownCounter = 0;
        
        //update hud
        updateHUD();
        hudPanel.clearEvents();
        hudPanel.addEvent("Round " + roundNumber + " Start!");
        hudPanel.addEvent("Difficulty: " + difficulty);
        hudPanel.addEvent("Enemies: " + enemies.size());
        
        //show round one cutscene
        if (roundNumber == 1) {
            storyManager.playCutscene("INTRO");
        } else if (roundNumber % 5 == 0) {
            storyManager.playCutscene("ROUND_" + roundNumber);
        }
        
        gamePanel.repaint();
    }

    //update difficulty
    private void updateDifficulty() {
        if (roundNumber <= 3) {
            difficulty = "EASY";
        } else if (roundNumber <= 7) {
            difficulty = "MEDIUM";
        } else if (roundNumber <= 12) {
            difficulty = "HARD";
        } else {
            difficulty = "IMPOSSIBLE";
        }
    }

    //Main game loop thread (60fps)
    private void startGameLoop() {
        running = true;
        gameThread = new Thread(() -> {
            long lastTime = System.nanoTime();
            double delta = 0;
            
            while (running) {
                long now = System.nanoTime();
                delta += (now - lastTime) / ns_per_frame;
                lastTime = now;
                
                if (delta >= 1) {
                    gameUpdate();
                    gamePanel.repaint();
                    delta--;
                }
                
                //prevent cpu hogging
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        gameThread.start();
    }

    //game update
    private void gameUpdate() {
        if (gameState != GameState.PLAYING) return;
        
        processPlayerMovement();
        
        updateTrail();
        
        //enemy movement (slower than player)
        enemyUpdateCounter++;
        if (enemyUpdateCounter >= enemy_update_frames) {
            processEnemyMovement();
            enemyUpdateCounter = 0;
        }
        //disc movement
        processDiscs();
        
        //decrease disc cooldown
        if (discCooldownCounter > 0) {
            discCooldownCounter--;
        }
        
        checkGameConditions();
        
        if (enemyUpdateCounter % 10 == 0) {
            updateHUD();
        }
    }
    
    //process smooth player movement with velocity and rotation
    private void processPlayerMovement() {
        if (keysPressed[KeyEvent.VK_W] || keysPressed[KeyEvent.VK_UP]) {
            targetAngle = -Math.PI / 2; //Up
        } else if (keysPressed[KeyEvent.VK_S] || keysPressed[KeyEvent.VK_DOWN]) {
            targetAngle = Math.PI / 2; //Down
        } else if (keysPressed[KeyEvent.VK_A] || keysPressed[KeyEvent.VK_LEFT]) {
            targetAngle = Math.PI; //Left
        } else if (keysPressed[KeyEvent.VK_D] || keysPressed[KeyEvent.VK_RIGHT]) {
            targetAngle = 0; //Right
        }
        
        //smooth rotation based on character handling
        double handling = player.getSpeed() > 0 ? 0.15 : 0.1; //use speed as proxy
        double diff = targetAngle - currentAngle;
        while (diff < -Math.PI) diff += Math.PI * 2;
        while (diff > Math.PI) diff -= Math.PI * 2;
        currentAngle += diff * handling;
        
        boolean isMoving = keysPressed[KeyEvent.VK_W] || keysPressed[KeyEvent.VK_UP] ||
                          keysPressed[KeyEvent.VK_S] || keysPressed[KeyEvent.VK_DOWN] ||
                          keysPressed[KeyEvent.VK_A] || keysPressed[KeyEvent.VK_LEFT] ||
                          keysPressed[KeyEvent.VK_D] || keysPressed[KeyEvent.VK_RIGHT];
        
        //calculate velocity
        double maxSpeed = 2.0 + player.getSpeed() * 0.3;
        if (isMoving) {
            velocity += 0.15;
            if (velocity > maxSpeed) velocity = maxSpeed;
        } else {
            velocity *= 0.92; // Deceleration
            if (velocity < 0.1) velocity = 0;
        }
        
        //calculate new position
        double newX = playerPixelX + Math.cos(currentAngle) * velocity;
        double newY = playerPixelY + Math.sin(currentAngle) * velocity;
        
        //convert to grid position for collision check
        int gridRow = (int) (newY / tile_size);
        int gridCol = (int) (newX / tile_size);
        
        //check boundary collision
        if (!arena.inBounds(gridRow, gridCol)) {
            handlePlayerFallOff();
            return;
        }
        
        //check wall collision
        if (arena.isWall(gridRow, gridCol)) {
            handlePlayerWallCollision();
            velocity = 0;
            return;
        }
        
        //check jetwall collision
        if (arena.isJetwall(gridRow, gridCol)) {
            handlePlayerJetwallCollision();
            velocity = 0;
            return;
        }
        
        //move player
        playerPixelX = newX;
        playerPixelY = newY;
        
        //update grid position for arena tracking
        Position gridPos = new Position(gridRow, gridCol);
        arena.setPlayerPosition(gridPos);
        
        //update player direction for disc throwing
        updatePlayerDirection();
        
        //place jetwall periodically while moving
        if (velocity > 0.5) {
            jetwallPlaceCounter++;
            if (jetwallPlaceCounter >= jetwall_place_frame) {
                int trailRow = (int) ((playerPixelY - Math.sin(currentAngle) * tile_size) / tile_size);
                int trailCol = (int) ((playerPixelX - Math.cos(currentAngle) * tile_size) / tile_size);
                if (arena.inBounds(trailRow, trailCol)) {
                    arena.placeJetwall(trailRow, trailCol);
                }
                jetwallPlaceCounter = 0;
            }
            
            //add trail point for visual effect
            if (velocity > 1.0) {
                TrailPoint tp = new TrailPoint();
                tp.x = playerPixelX;
                tp.y = playerPixelY;
                tp.angle = currentAngle;
                tp.velocity = velocity;
                playerTrail.add(tp);
            }
        }
        
        //check collision with enemies
        checkPlayerEnemyCollision();
    }
    
    /**
     * Update the visual trail (fade out effect)
     */
    private void updateTrail() {
        Iterator<TrailPoint> it = playerTrail.iterator();
        while (it.hasNext()) {
            TrailPoint tp = it.next();
            tp.life -= 0.03f;
            if (tp.life <= 0) {
                it.remove();
            }
        }
    }
    
    /**
     * Update player direction based on current angle
     */
    private void updatePlayerDirection() {
        double angle = currentAngle;
        while (angle < 0) angle += Math.PI * 2;
        while (angle >= Math.PI * 2) angle -= Math.PI * 2;
        
        if (angle < Math.PI / 4 || angle >= 7 * Math.PI / 4) {
            playerDirection = Direction.RIGHT;
        } else if (angle < 3 * Math.PI / 4) {
            playerDirection = Direction.DOWN;
        } else if (angle < 5 * Math.PI / 4) {
            playerDirection = Direction.LEFT;
        } else {
            playerDirection = Direction.UP;
        }
    }
    
    /**
     * Get player grid position from pixel position
     */
    private Position getPlayerGridPos() {
        return new Position((int) (playerPixelY / tile_size), (int) (playerPixelX / tile_size));
    }
    
    /**
     * Process enemy movement and AI decisions
     */
    private void processEnemyMovement() {
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;
            
            Position oldPos = new Position(enemy.getPosition().row, enemy.getPosition().col);
            
            // Let AI decide next move
            enemy.decideNextMove(arena);
            
            Position newPos = enemy.getPosition();
            
            // Check enemy collision with walls/jetwalls
            if (arena.isWall(newPos.row, newPos.col) || arena.isJetwall(newPos.row, newPos.col)) {
                enemy.hitJetwall();
                // Reset position
                enemy.getPosition().row = oldPos.row;
                enemy.getPosition().col = oldPos.col;
                
                if (!enemy.isAlive()) {
                    hudPanel.addEvent(enemy.getName() + " derezzed by wall!");
                    awardXP(enemy.getXpReward() / 2);
                }
            }
            
            // Place enemy jetwall trail
            arena.placeJetwall(oldPos.row, oldPos.col);
            
            // Random chance for enemy to throw disc
            if (Math.random() < 0.05) { // 5% chance per tick
                throwEnemyDisc(enemy);
            }
        }
    }
    
    /**
     * Process disc movement and collisions
     */
    private void processDiscs() {
        Iterator<Disc> it = activeDiscs.iterator();
        while (it.hasNext()) {
            Disc disc = it.next();
            
            // Update all discs (glow timer updates even for inactive discs)
            disc.update(arena, player, enemies);
            
            // Remove inactive discs only if they don't have collision glow
            if (!disc.isActive() && !disc.hasCollisionGlow()) {
                it.remove();
            }
        }
        
        // Check if player was hit
        if (!player.isAlive()) {
            handlePlayerDeath();
        }
        
        // Check for enemy deaths from disc hits
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) {
                hudPanel.addEvent(enemy.getName() + " derezzed! +" + enemy.getXpReward() + " XP");
                awardXP(enemy.getXpReward());
            }
        }
        
        // Remove dead enemies
        enemies.removeIf(e -> !e.isAlive());
    }
    
    /**
     * Player throws a disc
     */
    private void throwPlayerDisc() {
        if (player.getDiscsOwned() <= 0) {
            hudPanel.addEvent("No discs available!");
            return;
        }
        
        if (discCooldownCounter > 0) {
            hudPanel.addEvent("Disc on cooldown!");
            return;
        }
        
        player.useDisc();
        discCooldownCounter = disc_cooldown_frames;
        
        // Create disc at player's grid position
        Position gridPos = getPlayerGridPos();
        Disc disc = new Disc(new Position(gridPos.row, gridPos.col), playerDirection, null);
        activeDiscs.add(disc);
        
        hudPanel.addEvent("Disc thrown!");
    }
    
    /**
     * Enemy throws a disc
     */
    private void throwEnemyDisc(Enemy enemy) {
        Disc disc = new Disc(
            new Position(enemy.getPosition().row, enemy.getPosition().col),
            enemy.direction,
            enemy
        );
        activeDiscs.add(disc);
    }
    
    /**
     * Handle player collision with wall
     */
    private void handlePlayerWallCollision() {
        player.updateLives(-0.5);
        hudPanel.addEvent("Hit wall! -0.5 lives");
        
        if (player.getLives() <= 0) {
            handlePlayerDeath();
        }
    }
    
    /**
     * Handle player collision with jetwall
     */
    private void handlePlayerJetwallCollision() {
        player.updateLives(-0.5);
        hudPanel.addEvent("Hit jetwall! -0.5 lives");
        
        if (player.getLives() <= 0) {
            handlePlayerDeath();
        }
    }
    
    /**
     * Handle player falling off open arena
     */
    private void handlePlayerFallOff() {
        player.updateLives(-player.getLives()); // Lose all lives
        hudPanel.addEvent("Fell off the Grid!");
        handlePlayerDeath();
    }
    
    /**
     * Check collision between player and enemies
     */
    private void checkPlayerEnemyCollision() {
        Position gridPos = getPlayerGridPos();
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;
            
            Position ePos = enemy.getPosition();
            if (gridPos.row == ePos.row && gridPos.col == ePos.col) {
                // Both take damage
                player.updateLives(-0.5);
                enemy.hitJetwall();
                hudPanel.addEvent("Collision with " + enemy.getName() + "!");
                
                if (!enemy.isAlive()) {
                    hudPanel.addEvent(enemy.getName() + " derezzed!");
                    awardXP(enemy.getXpReward());
                }
            }
        }
    }
    
    /**
     * Award XP to player and check for level up
     */
    private void awardXP(int xp) {
        int oldLevel = player.getLevel();
        player.gainXp(xp);
        player.levelUp();
        totalScore += xp;
        
        if (player.getLevel() > oldLevel) {
            hudPanel.addEvent("LEVEL UP! Now level " + player.getLevel());
            storyManager.showAchievement("Reached Level " + player.getLevel());
            
            // Check for character/story unlocks
            if (player.getLevel() == 10) {
                storyManager.playCutscene("LEVEL_10");
            } else if (player.getLevel() == 25) {
                storyManager.playCutscene("LEVEL_25");
            }
        }
    }
    
    /**
     * Handle player death
     */
    private void handlePlayerDeath() {
        gameState = GameState.GAME_OVER;
        running = false;
        
        hudPanel.addEvent("DEREZZED!");
        
        // Add to leaderboard
        Leaderboard.addEntry(player.getName(), player.getLevel(), totalScore);
        
        // Show game over dialog
        SwingUtilities.invokeLater(() -> {
            int choice = JOptionPane.showOptionDialog(
                this,
                "GAME OVER\n\nFinal Score: " + totalScore + "\nLevel Reached: " + player.getLevel() + "\nRounds Survived: " + (roundNumber - 1),
                "Derezzed",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"Main Menu", "Exit"},
                "Main Menu"
            );
            
            if (choice == 0) {
                returnToMainMenu();
            } else {
                System.exit(0);
            }
        });
    }
    
    /**
     * Check win/lose conditions
     */
    private void checkGameConditions() {
        // Check if all enemies are dead
        boolean allEnemiesDead = enemies.stream().noneMatch(Enemy::isAlive);
        
        if (allEnemiesDead) {
            handleRoundWin();
        }
        
        // Check player death
        if (player.getLives() <= 0) {
            handlePlayerDeath();
        }
    }
    
    /**
     * Handle round win
     */
    private void handleRoundWin() {
        gameState = GameState.ROUND_WIN;
        running = false;
        
        int roundBonus = roundNumber * 100;
        awardXP(roundBonus);
        
        hudPanel.addEvent("ROUND " + roundNumber + " COMPLETE!");
        hudPanel.addEvent("Bonus: +" + roundBonus + " XP");
        
        // Show round complete dialog
        SwingUtilities.invokeLater(() -> {
            int choice = JOptionPane.showOptionDialog(
                this,
                "ROUND " + roundNumber + " COMPLETE!\n\nBonus XP: " + roundBonus + "\nTotal Score: " + totalScore,
                "Victory",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"Next Round", "Save & Quit", "Main Menu"},
                "Next Round"
            );
            
            if (choice == 0) {
                // Next round
                roundNumber++;
                initializeArena(getNextArenaType());
                startNewRound();
                running = true;
                startGameLoop();
            } else if (choice == 1) {
                // Save and quit
                saveGame();
                returnToMainMenu();
            } else {
                returnToMainMenu();
            }
        });
    }
    
    /**
     * Get next arena type based on round
     */
    private String getNextArenaType() {
        return switch (roundNumber % 4) {
            case 0 -> "Random";
            case 1 -> "Arena 1";
            case 2 -> "Arena 2";
            case 3 -> "Arena 3";
            default -> "Arena 1";
        };
    }
    
    /**
     * Update HUD with current stats
     */
    private void updateHUD() {
        hudPanel.updateStats(
            (int) player.getLives(),
            player.getXp(),
            player.getLevel(),
            player.getDiscsOwned()
        );
    }
    
    /**
     * Toggle pause state
     */
    private void togglePause() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
            hudPanel.addEvent("PAUSED");
            
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "Game Paused\nPress OK to resume", "Paused", JOptionPane.INFORMATION_MESSAGE);
                gameState = GameState.PLAYING;
            });
        }
    }
    
    /**
     * Handle exit request
     */
    private void handleExit() {
        running = false;
        
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Do you want to save before exiting?",
            "Exit Game",
            JOptionPane.YES_NO_CANCEL_OPTION
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            saveGame();
            returnToMainMenu();
        } else if (choice == JOptionPane.NO_OPTION) {
            returnToMainMenu();
        } else {
            // Cancel - resume game
            running = true;
            startGameLoop();
        }
    }
    
    /**
     * Save current game progress
     */
    private void saveGame() {
        SaveSystem.saveProgress(
            player.getName(),
            player.getXp(),
            player.getLevel(),
            (int) player.getLives(),
            player.getDiscsOwned()
        );
        JOptionPane.showMessageDialog(this, "Game saved successfully!", "Save", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Return to main menu
     */
    private void returnToMainMenu() {
        running = false;
        dispose();
        new MainMenu();
    }
    
    // === KeyListener Implementation ===
    
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (key < keysPressed.length) {
            keysPressed[key] = true;
        }
        
        // Handle special keys
        switch (key) {
            case KeyEvent.VK_SPACE -> {
                if (gameState == GameState.PLAYING) {
                    throwPlayerDisc();
                }
            }
            case KeyEvent.VK_P -> togglePause();
            case KeyEvent.VK_ESCAPE -> handleExit();
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key < keysPressed.length) {
            keysPressed[key] = false;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }
    
    // === Inner Class: Game Panel for Rendering ===
    
    private class GamePanel extends JPanel {
        
        public GamePanel() {
            setBackground(Color.BLACK);
            setDoubleBuffered(true);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw arena grid
            drawArena(g2d);
            
            // Draw player trail (behind player)
            drawPlayerTrail(g2d);
            
            // Draw enemies
            drawEnemies(g2d);
            
            // Draw player
            drawPlayer(g2d);
            
            // Draw discs
            drawDiscs(g2d);
            
            // Draw game state overlay
            drawStateOverlay(g2d);
        }
        
        private void drawArena(Graphics2D g2d) {
            int[][] grid = arena.getGridCopy();
            
            for (int r = 0; r < Arena.SIZE; r++) {
                for (int c = 0; c < Arena.SIZE; c++) {
                    int x = c * tile_size;
                    int y = r * tile_size;
                    
                    // Cell color based on type
                    if (grid[r][c] == 1) {
                        // Wall - dark with border
                        g2d.setColor(new Color(40, 40, 60));
                        g2d.fillRect(x, y, tile_size, tile_size);
                        g2d.setColor(new Color(60, 60, 80));
                        g2d.drawRect(x, y, tile_size, tile_size);
                    } else if (grid[r][c] == 2) {
                        // Jetwall - neon magenta glow effect
                        g2d.setColor(new Color(200, 0, 200));
                        g2d.fillRect(x, y, tile_size, tile_size);
                        g2d.setColor(new Color(255, 100, 255));
                        g2d.drawRect(x, y, tile_size, tile_size);
                    } else {
                        // Empty - dark with subtle grid
                        g2d.setColor(new Color(10, 10, 20));
                        g2d.fillRect(x, y, tile_size, tile_size);
                        g2d.setColor(new Color(30, 30, 50));
                        g2d.drawRect(x, y, tile_size, tile_size);
                    }
                }
            }
        }
        
        private void drawPlayerTrail(Graphics2D g2d) {
            // Get player color
            Color trailColor;
            if (player.getName().equalsIgnoreCase("Tron")) {
                trailColor = new Color(49, 213, 247); // Cyan for Tron
            } else if (player.getName().equalsIgnoreCase("Kevin")) {
                trailColor = new Color(255, 255, 255); // White for Kevin
            } else {
                trailColor = new Color(0, 255, 200); // Default teal
            }
            
            // Draw each trail point with fading alpha
            for (TrailPoint tp : playerTrail) {
                AffineTransform old = g2d.getTransform();
                
                // Set alpha based on life
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, tp.life * 0.8f));
                g2d.setColor(trailColor);
                
                // Calculate trail segment size based on velocity
                int thickness = (int) (tile_size * 0.4 * tp.life);
                int length = (int) (tp.velocity * 4);
                
                // Transform to trail position and rotation
                g2d.translate(tp.x, tp.y);
                g2d.rotate(tp.angle);
                
                // Draw rounded trail segment
                g2d.fillRoundRect(-length, -thickness / 2, length, thickness, thickness, thickness);
                
                g2d.setTransform(old);
            }
            
            // Reset composite
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        
        private void drawPlayer(Graphics2D g2d) {
            // Determine player color based on character
            Color playerColor;
            if (player.getName().equalsIgnoreCase("Tron")) {
                playerColor = new Color(0, 200, 255); // Cyan for Tron
            } else if (player.getName().equalsIgnoreCase("Kevin")) {
                playerColor = Color.WHITE; // White for Kevin
            } else {
                playerColor = new Color(0, 255, 200); // Default teal
            }
            
            // Save transform
            AffineTransform old = g2d.getTransform();
            
            // Draw glow effect
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            g2d.setColor(playerColor);
            g2d.fillOval((int) playerPixelX - tile_size, (int) playerPixelY - tile_size, 
                         tile_size * 2, tile_size * 2);
            
            // Draw player cycle (rotated based on direction)
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2d.translate(playerPixelX, playerPixelY);
            g2d.rotate(currentAngle);
            
            // Draw light cycle shape (elongated oval)
            g2d.setColor(playerColor);
            g2d.fillOval(-tile_size / 2, -tile_size / 3, tile_size, tile_size * 2 / 3);
            
            // Draw inner highlight
            g2d.setColor(Color.WHITE);
            g2d.fillOval(-tile_size / 4, -tile_size / 6, tile_size / 2, tile_size / 3);
            
            // Draw direction indicator (front of cycle)
            g2d.setColor(playerColor.brighter());
            g2d.fillOval(tile_size / 4, -3, 6, 6);
            
            g2d.setTransform(old);
        }
        
        private void drawEnemies(Graphics2D g2d) {
            if (enemies == null) return;
            
            for (Enemy enemy : enemies) {
                if (!enemy.isAlive()) continue;
                
                Position pos = enemy.getPosition();
                int x = pos.col * tile_size + tile_size / 2;
                int y = pos.row * tile_size + tile_size / 2;
                
                // Color based on enemy type
                Color enemyColor = getEnemyColor(enemy.getColor());
                
                // Draw glow
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                g2d.setColor(enemyColor);
                g2d.fillOval(x - tile_size, y - tile_size, tile_size * 2, tile_size * 2);
                
                // Draw enemy
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                g2d.setColor(enemyColor);
                g2d.fillOval(x - tile_size / 2 + 1, y - tile_size / 2 + 1, tile_size - 2, tile_size - 2);
                
                // Draw name initial
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Monospaced", Font.BOLD, 10));
                FontMetrics fm = g2d.getFontMetrics();
                String initial = enemy.getName().substring(0, 1);
                g2d.drawString(initial, x - fm.stringWidth(initial) / 2, y + fm.getAscent() / 2 - 2);
            }
        }
        
        private Color getEnemyColor(String colorName) {
            return switch (colorName.toUpperCase()) {
                case "GOLD" -> new Color(255, 215, 0);    // Clu
                case "RED" -> new Color(255, 50, 50);     // Rinzler
                case "YELLOW" -> new Color(255, 255, 0);  // Sark
                case "GREEN" -> new Color(50, 255, 50);   // Koura
                default -> Color.RED;
            };
        }
        
        private void drawDiscs(Graphics2D g2d) {
            if (activeDiscs == null) return;
            
            for (Disc disc : activeDiscs) {
                if (!disc.isActive()) {
                    // Draw collision glow even for inactive discs
                    if (disc.hasCollisionGlow()) {
                        drawCollisionGlow(g2d, disc);
                    }
                    continue;
                }
                
                // Use smooth position for animation across tiles
                double smoothCol = disc.getSmoothCol();
                double smoothRow = disc.getSmoothRow();
                double x = smoothCol * tile_size + tile_size / 2.0;
                double y = smoothRow * tile_size + tile_size / 2.0;
                
                // Disc color (player disc is cyan, enemy disc is red)
                Color discColor = disc.getOwner() == null ? new Color(0, 255, 255) : new Color(255, 100, 100);
                
                // Outer glow effect
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                g2d.setColor(discColor);
                g2d.fillOval((int)(x - 8), (int)(y - 8), 16, 16);
                
                // Disc core
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                g2d.setColor(discColor);
                g2d.fillOval((int)(x - 5), (int)(y - 5), 10, 10);
                
                // Inner ring (spinning effect)
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawOval((int)(x - 4), (int)(y - 4), 8, 8);
            }
        }
        
        /**
         * Draw collision glow effect at disc collision position
         */
        private void drawCollisionGlow(Graphics2D g2d, Disc disc) {
            Position collisionPos = disc.getLastCollisionPos();
            int x = collisionPos.col * tile_size + tile_size / 2;
            int y = collisionPos.row * tile_size + tile_size / 2;
            
            float glowIntensity = disc.getCollisionGlowIntensity();
            Color discColor = disc.getOwner() == null ? new Color(0, 255, 255) : new Color(255, 100, 100);
            
            // Multiple glow layers for dramatic effect
            int maxGlowSize = tile_size * 3;
            int minGlowSize = tile_size;
            
            // Outer glow (largest, most transparent)
            float outerAlpha = glowIntensity * 0.3f;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, outerAlpha));
            g2d.setColor(discColor);
            int outerSize = (int)(minGlowSize + (maxGlowSize - minGlowSize) * glowIntensity);
            g2d.fillOval(x - outerSize / 2, y - outerSize / 2, outerSize, outerSize);
            
            // Middle glow
            float middleAlpha = glowIntensity * 0.5f;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, middleAlpha));
            int middleSize = (int)(minGlowSize + (maxGlowSize * 0.6f - minGlowSize) * glowIntensity);
            g2d.fillOval(x - middleSize / 2, y - middleSize / 2, middleSize, middleSize);
            
            // Inner bright core
            float coreAlpha = glowIntensity * 0.8f;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, coreAlpha));
            int coreSize = (int)(tile_size * 0.5f + (tile_size * 1.5f - tile_size * 0.5f) * glowIntensity);
            g2d.fillOval(x - coreSize / 2, y - coreSize / 2, coreSize, coreSize);
            
            // Reset composite
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        
        private void drawStateOverlay(Graphics2D g2d) {
            if (gameState == GameState.PAUSED) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                g2d.setColor(new Color(0, 0, 0));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                g2d.setColor(new Color(0, 200, 255));
                g2d.setFont(new Font("Monospaced", Font.BOLD, 30));
                String text = "PAUSED";
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2);
            }
        }
    }
}
