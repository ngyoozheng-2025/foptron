package src.UIGameEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import src.Arena.Arena;
import src.Characters.Characters;
import src.Disc.Disc;
import src.Enemy.Direction;
import src.Enemy.Enemy;
import src.Enemy.EnemyLoader;
import src.Enemy.Position;
import src.leaderboardstory.Leaderboard;
import src.leaderboardstory.SaveSystem;
import src.leaderboardstory.StoryManager;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;

/**
 * GameEngine (trimmed/cleaned) — integrates collision glow + HUD flash + shake.
 *
 * NOTE: depends on your project's other classes:
 * Enemy, EnemyLoader, Disc, Position, Direction, Arena, StoryManager,
 * Leaderboard, SaveSystem, MainMenu
 */
public class GameEngine extends JFrame implements KeyListener {

    private enum GameState {
        PLAYING, PAUSED, ROUND_WIN, GAME_OVER, CUTSCENE
    }

    private Arena arena;
    private Characters player;
    private double playerPixelX, playerPixelY;
    private Direction playerDirection;

    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Disc> activeDiscs = new ArrayList<>();
    private final Map<Enemy, Double> enemyMoveAcc = new HashMap<>();

    // Movement / smoothing
    private double currentAngle = 0;
    private double targetAngle = 0;
    private double velocity = 0;
    private final List<TrailPoint> playerTrail = new ArrayList<>();

    // Track last jetwall grid position to help keep the trail continuous
    private int lastJetwallRow = -1;
    private int lastJetwallCol = -1;

    // Track previous player tile (used to place jetwall on tile left)
    private int prevPlayerGridRow = -1;
    private int prevPlayerGridCol = -1;

    private BufferedImage kevinSprite;
    private BufferedImage tronSprite;
    private BufferedImage enemyClu;
    private BufferedImage enemyRinzler;
    private BufferedImage enemySark;
    private BufferedImage enemyKoura;

    private static class TrailPoint {
        double x, y, angle, velocity;
        float life = 1.0f;
    }

    // Collision visual effect
    private int collisionGlowFrames = 0;
    private static final int COLLISION_GLOW_FRAMES_MAX = 18; // ~0.3s at 60fps
    private double collisionShake = 0.0;
    private static final double COLLISION_SHAKE_MAX = 6.0;
    private Position collisionGridPos = null; // where the last collision happened (grid coords)

    // UI
    private final GamePanel gamePanel;
    private final HUDPanel hudPanel;

    // Game state
    private GameState gameState;
    private Thread gameThread;
    private volatile boolean running = false;
    private int roundNumber = 1;
    private int totalScore = 0;
    private String difficulty;

    // Timing
    private static final int FPS = 60;
    private static final double NS_PER_FRAME = 1_000_000_000.0 / FPS;

    // Controls
    private final boolean[] keysPressed = new boolean[256];
    private boolean persistentMove = false;
    private double persistentTargetAngle = 0.0;

    // Tiles
    private static final int tile_size = 15;
    private static final int arena_pixel_size = Arena.SIZE * tile_size;

    // Other
    private final StoryManager storyManager;

    // Cooldowns (kept but jetwall frame logic removed)
    private int discCooldownCounter = 0;
    private static final int DISC_COOLDOWN_FRAMES = 300;
    private int jetwallPlaceCounter = 0;
    private static final int JETWALL_PLACE_FRAME = 10;

    public GameEngine(Characters character, String arenaName) {
        this(character, arenaName, "EASY");
    }

    public GameEngine(Characters character, String arenaName, String difficulty) {
        try {
            this.kevinSprite = ImageIO.read(new File("res/Kevin/Kevin.png"));
            this.tronSprite = ImageIO.read(new File("res/Tron/Tron.png"));
            this.enemyClu = ImageIO.read(new File("res/Clu/Clu.png"));
            this.enemyRinzler = ImageIO.read(new File("res/Rinzler/Rinzler.png"));
            this.enemySark = ImageIO.read(new File("res/Sark/Sark.png"));
            this.enemyKoura = ImageIO.read(new File("res/Koura/Koura.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.player = character;
        this.difficulty = difficulty == null ? "EASY" : difficulty;
        this.playerDirection = Direction.RIGHT;

        this.storyManager = new StoryManager("leaderboardstory/story.txt");

        setTitle("FOP Tron - " + player.getName());
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleExit();
            }
        });

        initializeArena(arenaName);

        gamePanel = new GamePanel();
        gamePanel.setPreferredSize(new Dimension(arena_pixel_size, arena_pixel_size));
        add(gamePanel, BorderLayout.CENTER);

        hudPanel = new HUDPanel();
        add(hudPanel, BorderLayout.EAST);

        JPanel control = createControlPanel();
        add(control, BorderLayout.SOUTH);

        addKeyListener(this);
        gamePanel.addKeyListener(this);
        setFocusable(true);
        gamePanel.setFocusable(true);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        startNewRound();
        startGameLoop();
    }

    private JPanel createControlPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        p.setBackground(Color.BLACK);
        JLabel lbl = new JLabel("WASD: Move | SPACE: Throw Disc | X: Stop | P: Pause | ESC: Menu");
        lbl.setForeground(new Color(0, 200, 255));
        lbl.setFont(new Font("Monospaced", Font.PLAIN, 12));
        p.add(lbl);
        return p;
    }

    private void initializeArena(String arenaName) {
        switch (arenaName) {
            case "Arena 1" -> arena = new Arena("ClassicGrid");
            case "Arena 2" -> arena = new Arena("NeonMaze");
            case "Arena 3" -> arena = new Arena("OpenFrontier");
            case "Random" -> arena = new Arena("Procedural", System.currentTimeMillis());
            default -> arena = new Arena("ClassicGrid");
        }
    }

    private void startNewRound() {
        gameState = GameState.PLAYING;
        Position spawn = arena.getPlayerSpawn();
        playerPixelX = spawn.col * tile_size + tile_size / 2.0;
        playerPixelY = spawn.row * tile_size + tile_size / 2.0;
        arena.setPlayerPosition(spawn);

        // initialize prev player tile so the first move places a jetwall at spawn tile
        prevPlayerGridRow = spawn.row;
        prevPlayerGridCol = spawn.col;

        velocity = 0;
        currentAngle = 0;
        targetAngle = 0;
        playerTrail.clear();

        enemies.clear();
        enemies.addAll(EnemyLoader.loadEnemies("src/Enemy/enemies.txt", difficulty, arena, Arena.SIZE, Arena.SIZE));
        enemyMoveAcc.clear();
        for (Enemy e : enemies)
            enemyMoveAcc.put(e, 0.0);

        activeDiscs.clear();
        discCooldownCounter = 0;

        hudPanel.clearEvents();
        hudPanel.addEvent("Round " + roundNumber + " Start!");
        hudPanel.addEvent("Difficulty: " + difficulty);
        hudPanel.addEvent("Enemies: " + enemies.size());

        updateHUD();

        if (roundNumber == 1)
            storyManager.playCutscene("INTRO");
        else if (roundNumber % 5 == 0)
            storyManager.playCutscene("ROUND_" + roundNumber);

        gamePanel.repaint();
    }

    private void startGameLoop() {
        running = true;
        gameThread = new Thread(() -> {
            long last = System.nanoTime();
            double delta = 0;
            while (running) {
                long now = System.nanoTime();
                delta += (now - last) / NS_PER_FRAME;
                last = now;

                if (delta >= 1.0) {
                    gameUpdate();
                    gamePanel.repaint();
                    delta--;
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        gameThread.start();
    }

    private void gameUpdate() {
        if (gameState != GameState.PLAYING)
            return;

        processPlayerMovement();
        updateTrail();
        processEnemyMovement();
        processDiscs();

        if (discCooldownCounter > 0)
            discCooldownCounter--;

        checkGameConditions();

        // collision effect decay
        if (collisionGlowFrames > 0)
            collisionGlowFrames--;
        if (collisionShake > 0.05)
            collisionShake *= 0.85;
        else
            collisionShake = 0.0;

        updateHUD();
    }

    /**
     * Main player movement + jetwall placement logic (tile-based).
     *
     * Replaced fragile frame-counter approach with per-tile placement:
     * - place a jetwall on the tile the player just left (prevPlayerGridRow/Col)
     * - if player skipped multiple tiles, fill the straight line of tiles between
     * prev and current (excluding player's current tile).
     */
    private void processPlayerMovement() {
        if (persistentMove)
            targetAngle = persistentTargetAngle;
        else {
            if (keysPressed[KeyEvent.VK_W] || keysPressed[KeyEvent.VK_UP])
                targetAngle = -Math.PI / 2;
            else if (keysPressed[KeyEvent.VK_S] || keysPressed[KeyEvent.VK_DOWN])
                targetAngle = Math.PI / 2;
            else if (keysPressed[KeyEvent.VK_A] || keysPressed[KeyEvent.VK_LEFT])
                targetAngle = Math.PI;
            else if (keysPressed[KeyEvent.VK_D] || keysPressed[KeyEvent.VK_RIGHT])
                targetAngle = 0;
        }

        // snap rotation for simpler responsiveness (optional)
        currentAngle = targetAngle;

        boolean isMoving = persistentMove ||
                keysPressed[KeyEvent.VK_W] || keysPressed[KeyEvent.VK_UP] ||
                keysPressed[KeyEvent.VK_S] || keysPressed[KeyEvent.VK_DOWN] ||
                keysPressed[KeyEvent.VK_A] || keysPressed[KeyEvent.VK_LEFT] ||
                keysPressed[KeyEvent.VK_D] || keysPressed[KeyEvent.VK_RIGHT];

        double maxSpeed = 2.0 + player.getSpeed() * 0.3;
        if (isMoving) {
            velocity += 0.15;
            if (velocity > maxSpeed)
                velocity = maxSpeed;
        } else {
            velocity *= 0.92;
            if (velocity < 0.1)
                velocity = 0;
        }

        double newX = playerPixelX + Math.cos(currentAngle) * velocity;
        double newY = playerPixelY + Math.sin(currentAngle) * velocity;

        int gridRow = (int) (newY / tile_size);
        int gridCol = (int) (newX / tile_size);

        if (!arena.inBounds(gridRow, gridCol)) {
            handlePlayerFallOff();
            return;
        }

        if (arena.isWall(gridRow, gridCol)) {
            handlePlayerWallCollision(gridRow, gridCol);
            velocity = 0;
            return;
        }

        if (arena.isJetwall(gridRow, gridCol)) {
            handlePlayerJetwallCollision(gridRow, gridCol);
            velocity = 0;
            return;
        }

        // --- PLACE JETWALL(S) BASED ON TILE TRANSITION ---
        // If previous tile known and player moved into a new tile, place jetwall on
        // tile left.
        if (prevPlayerGridRow != -1 && prevPlayerGridCol != -1) {
            if (gridRow != prevPlayerGridRow || gridCol != prevPlayerGridCol) {
                // place on the tile we left (prev)
                if (arena.inBounds(prevPlayerGridRow, prevPlayerGridCol)) {
                    // avoid placing on the player's current tile
                    if (!(prevPlayerGridRow == gridRow && prevPlayerGridCol == gridCol)) {
                        arena.placeJetwall(prevPlayerGridRow, prevPlayerGridCol);
                        lastJetwallRow = prevPlayerGridRow;
                        lastJetwallCol = prevPlayerGridCol;
                    }
                }

                // fill intermediate tiles if we skipped tiles (line from prev to current),
                // but do NOT overwrite the player's current tile.
                int r = prevPlayerGridRow;
                int c = prevPlayerGridCol;
                while (r != gridRow || c != gridCol) {
                    if (r < gridRow)
                        r++;
                    else if (r > gridRow)
                        r--;
                    if (c < gridCol)
                        c++;
                    else if (c > gridCol)
                        c--;

                    // break if we've reached the player's current tile
                    if (r == gridRow && c == gridCol)
                        break;

                    if (arena.inBounds(r, c)) {
                        arena.placeJetwall(r, c);
                        lastJetwallRow = r;
                        lastJetwallCol = c;
                    } else {
                        break;
                    }
                }
            }
        }

        // Update player pixel position & grid pos AFTER placing jetwalls
        playerPixelX = newX;
        playerPixelY = newY;

        Position gridPos = new Position(gridRow, gridCol);
        arena.setPlayerPosition(gridPos);
        updatePlayerDirection();

        // create visual trail points
        if (velocity > 1.0) {
            TrailPoint tp = new TrailPoint();
            tp.x = playerPixelX;
            tp.y = playerPixelY;
            tp.angle = currentAngle;
            tp.velocity = velocity;
            playerTrail.add(tp);
        }

        // update prev player tile for next frame
        prevPlayerGridRow = gridRow;
        prevPlayerGridCol = gridCol;

        checkPlayerEnemyCollision();
    }

    private void updateTrail() {
        Iterator<TrailPoint> it = playerTrail.iterator();
        while (it.hasNext()) {
            TrailPoint tp = it.next();
            tp.life -= 0.03f;
            if (tp.life <= 0)
                it.remove();
        }
    }

    private void updatePlayerDirection() {
        double angle = currentAngle;
        while (angle < 0)
            angle += Math.PI * 2;
        while (angle >= Math.PI * 2)
            angle -= Math.PI * 2;

        if (angle < Math.PI / 4 || angle >= 7 * Math.PI / 4)
            playerDirection = Direction.RIGHT;
        else if (angle < 3 * Math.PI / 4)
            playerDirection = Direction.DOWN;
        else if (angle < 5 * Math.PI / 4)
            playerDirection = Direction.LEFT;
        else
            playerDirection = Direction.UP;
    }

    private Position getPlayerGridPos() {
        return new Position((int) (playerPixelY / tile_size), (int) (playerPixelX / tile_size));
    }

    private void processEnemyMovement() {
        if (enemies == null || enemies.isEmpty())
            return;
        final double tilesPerFrameFactor = 1.0 / FPS;
        for (Enemy enemy : new ArrayList<>(enemies)) {
            if (!enemy.isAlive()) {
                enemyMoveAcc.remove(enemy);
                continue;
            }
            double acc = enemyMoveAcc.getOrDefault(enemy, 0.0);
            acc += enemy.getSpeed() * tilesPerFrameFactor;

            while (acc >= 1.0) {
                Position old = new Position(enemy.getPosition().row, enemy.getPosition().col);
                enemy.decideNextMove(arena);
                Position newPos = enemy.getPosition();

                if (arena.isWall(newPos.row, newPos.col) || arena.isJetwall(newPos.row, newPos.col)) {
                    enemy.hitJetwall();
                    enemy.getPosition().row = old.row;
                    enemy.getPosition().col = old.col;
                    if (!enemy.isAlive()) {
                        hudPanel.addEvent(enemy.getName() + " derezzed by wall!");
                        awardXP(enemy.getXpReward() / 2);
                    }
                    break;
                } else {
                    arena.placeJetwall(old.row, old.col);
                    if (Math.random() < 0.02 * Math.min(enemy.getSpeed(), 5.0))
                        throwEnemyDisc(enemy);
                }

                acc -= 1.0;
                if (!enemy.isAlive()) {
                    enemyMoveAcc.remove(enemy);
                    break;
                }
            }
            enemyMoveAcc.put(enemy, acc);
        }
        enemyMoveAcc.keySet().removeIf(e -> !enemies.contains(e));
    }

    private void processDiscs() {
        Iterator<Disc> it = activeDiscs.iterator();
        while (it.hasNext()) {
            Disc d = it.next();
            d.update(arena, player, (ArrayList<Enemy>) enemies);

            if (d.hasHitEnemy()) {
                if (!d.hasCollisionGlow())
                    it.remove();
                continue;
            }
            if (d.isActive())
                continue;
            if (d.isCollected()) {
                it.remove();
                continue;
            }

            Position dp = d.getPosition();
            Position pg = getPlayerGridPos();
            if (dp.row == pg.row && dp.col == pg.col) {
                if (d.getOwner() == null) {
                    player.setDiscsOwned(player.getDiscsOwned() + 1);
                    d.collect();
                    hudPanel.addEvent("Disc reclaimed!");
                    it.remove();
                }
            }
        }

        for (Enemy e : new ArrayList<>(enemies)) {
            if (!e.isAlive()) {
                hudPanel.addEvent(e.getName() + " derezzed! +" + e.getXpReward() + " XP");
                awardXP(e.getXpReward());
            }
        }
        enemies.removeIf(e -> !e.isAlive());
    }

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
        discCooldownCounter = DISC_COOLDOWN_FRAMES;
        Position gp = getPlayerGridPos();
        Disc d = new Disc(new Position(gp.row, gp.col), playerDirection, null);
        activeDiscs.add(d);
        hudPanel.addEvent("Disc thrown!");
    }

    private void throwEnemyDisc(Enemy enemy) {
        Disc d = new Disc(new Position(enemy.getPosition().row, enemy.getPosition().col), enemy.getDirection(), enemy);
        activeDiscs.add(d);
    }

    private void handlePlayerWallCollision(int gridRow, int gridCol) {
        // visual + audio hooks
        triggerCollisionEffects(gridRow, gridCol);
        boolean damageApplied = player.updateLives(-0.5);

        if (damageApplied) {
            hudPanel.addEvent("Hit wall! -0.5 lives");
        }
        if (player.getLives() <= 0)
            handlePlayerDeath();
    }

    private void handlePlayerJetwallCollision(int gridRow, int gridCol) {
        triggerCollisionEffects(gridRow, gridCol);
        boolean damageApplied = player.updateLives(-0.5);
        if (damageApplied) {
            hudPanel.addEvent("Hit jetwall! -0.5 lives");
        }
        if (player.getLives() <= 0)
            handlePlayerDeath();
    }

    private void triggerCollisionEffects(int gridRow, int gridCol) {
        collisionGlowFrames = COLLISION_GLOW_FRAMES_MAX;
        collisionShake = COLLISION_SHAKE_MAX;
        collisionGridPos = new Position(gridRow, gridCol);
        // also flash the HUD
        hudPanel.triggerCollisionGlow();
    }

    private void handlePlayerFallOff() {
        player.updateLives(-player.getLives());
        hudPanel.addEvent("Fell off the Grid!");
        handlePlayerDeath();
    }

    private void checkPlayerEnemyCollision() {
        Position gridPos = getPlayerGridPos();
        if (enemies == null)
            return;
        for (Enemy enemy : enemies) {
            if (!enemy.isAlive())
                continue;

            Position ePos = enemy.getPosition();

            if (gridPos.row == ePos.row && gridPos.col == ePos.col) {
                boolean playerHit = player.updateLives(-0.5);

                if (playerHit) {
                    enemy.hitJetwall();
                    hudPanel.addEvent("Collision with " + enemy.getName() + "!");

                    if (!enemy.isAlive()) {
                        hudPanel.addEvent(enemy.getName() + " derezzed!");
                        awardXP(enemy.getXpReward());
                    }
                }
            }
        }
    }

    private void awardXP(int xpAmount) {
        int oldLvl = player.getLevel();
        player.gainXp(xpAmount);
        totalScore += xpAmount;
        updateHUD();
        if (player.getLevel() > oldLvl) {
            hudPanel.addEvent("LEVEL UP! Now level " + player.getLevel());
            storyManager.showAchievement("Reached Level " + player.getLevel());
            if (player.getLevel() == 10)
                storyManager.playCutscene("LEVEL_10");
            else if (player.getLevel() == 25)
                storyManager.playCutscene("LEVEL_25");
        }
    }

    private void handlePlayerDeath() {
        gameState = GameState.GAME_OVER;
        running = false;
        hudPanel.addEvent("DEREZZED!");
        Leaderboard.addEntry(player.getName(), player.getLevel(), totalScore);

        SwingUtilities.invokeLater(() -> {
            String message = "GAME OVER\n\nFinal Score: " + totalScore +
                    "\nLevel Reached: " + player.getLevel() +
                    "\nRounds Survived: " + (roundNumber - 1);

            String[] options = { "Main Menu", "Exit" };
            int choice = showCustomOptionDialog("GAME OVER", message, options);

            if (choice == 1) { // "Exit"
                System.exit(0);
            } else {
                // Main Menu or closed -> main menu
                returnToMainMenu();
            }
        });

    }

    private void checkGameConditions() {
        boolean allDead = enemies == null || enemies.stream().noneMatch(Enemy::isAlive);
        if (allDead)
            handleRoundWin();
        if (player.getLives() <= 0)
            handlePlayerDeath();
    }

    private void handleRoundWin() {
        gameState = GameState.ROUND_WIN;
        running = false;
        int roundBonus = roundNumber * 100;
        awardXP(roundBonus);
        hudPanel.addEvent("ROUND " + roundNumber + " COMPLETE!");
        hudPanel.addEvent("Bonus: +" + roundBonus + " XP");

        SwingUtilities.invokeLater(() -> {
            String message = "ROUND " + roundNumber + " COMPLETE!\n\n" +
                    "Bonus XP: " + roundBonus + "\nTotal Score: " + totalScore;

            String[] options = { "Next Round", "Save & Quit", "Main Menu" };
            int choice = showCustomOptionDialog("ROUND COMPLETE", message, options);

            if (choice == 0) { // Next Round
                roundNumber++;
                initializeArena(getNextArenaType());
                startNewRound();
                running = true;
                startGameLoop();
            } else if (choice == 1) { // Save & Quit
                saveGame();
                returnToMainMenu();
            } else { // Main Menu or closed
                returnToMainMenu();
            }
        });

    }

    private String getNextArenaType() {
        return switch (roundNumber % 4) {
            case 0 -> "Random";
            case 1 -> "Arena 1";
            case 2 -> "Arena 2";
            default -> "Arena 3";
        };
    }

    private void updateHUD() {
        hudPanel.updateStats((int) player.getLives(), player.getXp(), player.getLevel(), player.getDiscsOwned(),
                player.xpToLevelUp());
    }

    private void togglePause() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
            hudPanel.addEvent("PAUSED");
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "Game Paused\nPress OK to resume", "Paused",
                        JOptionPane.INFORMATION_MESSAGE);
                gameState = GameState.PLAYING;
            });
        }
    }

    private void handleExit() {
        running = false;

        String[] options = { "Save & Exit", "Exit without Save", "Cancel" };
        String message = "Do you want to save before exiting?";
        int choice = showCustomOptionDialog("Exit Game", message, options);

        if (choice == 0) { // Save & Exit
            saveGame();
            returnToMainMenu();
        } else if (choice == 1) { // Exit without Save
            returnToMainMenu();
        } else { // Cancel or closed
            // resume
            running = true;
            startGameLoop();
        }

    }

    private void saveGame() {
        // adapt SaveSystem signature in your project
        SaveSystem.saveProgress(
                "slot1",
                player.getName(),
                player.getXp(),
                player.getLevel(),
                player.getLives(),
                player.getDiscsOwned(),
                arena.getName(),
                difficulty,
                roundNumber,
                totalScore);
        Leaderboard.addEntry(player.getName(), player.getLevel(), totalScore);
        JOptionPane.showMessageDialog(this, "Game saved successfully!", "Save", JOptionPane.INFORMATION_MESSAGE);
    }

    private void returnToMainMenu() {
        running = false;
        dispose();
        new MainMenu();
    }

    // === KeyListener ===
    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (k < keysPressed.length)
            keysPressed[k] = true;

        switch (k) {
            case KeyEvent.VK_W, KeyEvent.VK_UP -> {
                persistentTargetAngle = -Math.PI / 2;
                persistentMove = true;
            }
            case KeyEvent.VK_S, KeyEvent.VK_DOWN -> {
                persistentTargetAngle = Math.PI / 2;
                persistentMove = true;
            }
            case KeyEvent.VK_A, KeyEvent.VK_LEFT -> {
                persistentTargetAngle = Math.PI;
                persistentMove = true;
            }
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> {
                persistentTargetAngle = 0;
                persistentMove = true;
            }
            case KeyEvent.VK_X -> persistentMove = false;
        }

        switch (k) {
            case KeyEvent.VK_SPACE -> {
                if (gameState == GameState.PLAYING)
                    throwPlayerDisc();
            }
            case KeyEvent.VK_P -> togglePause();
            case KeyEvent.VK_ESCAPE -> handleExit();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();
        if (k < keysPressed.length)
            keysPressed[k] = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    // === Rendering Panel ===
    private class GamePanel extends JPanel {
        public GamePanel() {
            setBackground(Color.BLACK);
            setDoubleBuffered(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g.create();

            // apply camera shake if active
            if (collisionShake > 0.01) {
                double sx = (Math.random() * 2 - 1) * collisionShake;
                double sy = (Math.random() * 2 - 1) * collisionShake;
                g2d.translate((int) sx, (int) sy);
            }

            // draw arena and elements
            drawArena(g2d);
            drawPlayerTrail(g2d);
            drawEnemies(g2d);
            drawPlayer(g2d);
            drawDiscs(g2d);

            // draw collision glow overlay in world
            if (collisionGlowFrames > 0 && collisionGridPos != null) {
                float t = (float) collisionGlowFrames / (float) COLLISION_GLOW_FRAMES_MAX;
                drawPlayerCollisionGlow(g2d, collisionGridPos, t);
            }

            drawStateOverlay(g2d);
            g2d.dispose();
        }

        private void drawArena(Graphics2D g2d) {
            int[][] grid = arena.getGridCopy();
            for (int r = 0; r < Arena.SIZE; r++) {
                for (int c = 0; c < Arena.SIZE; c++) {
                    int x = c * tile_size, y = r * tile_size;
                    if (grid[r][c] == 1) {
                        g2d.setColor(new Color(40, 40, 60));
                        g2d.fillRect(x, y, tile_size, tile_size);
                        g2d.setColor(new Color(60, 60, 80));
                        g2d.drawRect(x, y, tile_size, tile_size);
                    } else if (grid[r][c] == 2) {
                        g2d.setColor(new Color(200, 0, 200));
                        g2d.fillRect(x, y, tile_size, tile_size);
                        g2d.setColor(new Color(255, 100, 255));
                        g2d.drawRect(x, y, tile_size, tile_size);
                    } else {
                        g2d.setColor(new Color(10, 10, 20));
                        g2d.fillRect(x, y, tile_size, tile_size);
                        g2d.setColor(new Color(30, 30, 50));
                        g2d.drawRect(x, y, tile_size, tile_size);
                    }
                }
            }
        }

        private void drawPlayerTrail(Graphics2D g2d) {
            Color trailColor = player.getName().equalsIgnoreCase("Tron") ? new Color(49, 213, 247)
                    : player.getName().equalsIgnoreCase("Kevin") ? Color.WHITE : new Color(0, 255, 200);

            for (TrailPoint tp : playerTrail) {
                AffineTransform old = g2d.getTransform();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, tp.life * 0.8f));
                g2d.setColor(trailColor);
                int thickness = (int) (tile_size * 0.4 * tp.life);
                int length = (int) (tp.velocity * 4);
                g2d.translate(tp.x, tp.y);
                g2d.rotate(tp.angle);
                g2d.fillRoundRect(-length, -thickness / 2, length, thickness, thickness, thickness);
                g2d.setTransform(old);
            }
            g2d.setComposite(AlphaComposite.SrcOver);
        }

        private BufferedImage getPlayerSprite() {
            if (player.getName().equalsIgnoreCase("Tron")) {
                return tronSprite;
            } else if (player.getName().equalsIgnoreCase("Kevin")) {
                return kevinSprite;
            }
            return tronSprite; // fallback
        }

        private void drawPlayer(Graphics2D g2d) {
            BufferedImage sprite = getPlayerSprite();
            if (sprite == null)
                return;

            AffineTransform old = g2d.getTransform();

            g2d.translate(playerPixelX, playerPixelY);
            g2d.rotate(currentAngle);

            // ===== GLOW EFFECT =====
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
            int glowSize = tile_size * 2;
            g2d.drawImage(
                    sprite,
                    -glowSize / 2,
                    -glowSize / 2,
                    glowSize,
                    glowSize,
                    null);

            // ===== PLAYER SPRITE =====
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2d.drawImage(
                    sprite,
                    -tile_size / 2,
                    -tile_size / 2,
                    tile_size,
                    tile_size,
                    null);

            g2d.setTransform(old); // reset
        }

        private BufferedImage getEnemySprite(String enemyName) {
            return switch (enemyName.toUpperCase()) {
                case "CLU" -> enemyClu;
                case "RINZLER" -> enemyRinzler;
                case "SARK" -> enemySark;
                case "KOURA" -> enemyKoura;
                default -> null; // fallback
            };
        }

        private void drawEnemies(Graphics2D g2d) {
            if (enemies == null)
                return;
            for (Enemy enemy : enemies) {
                if (!enemy.isAlive())
                    continue;
                Position p = enemy.getPosition();
                int x = p.col * tile_size + tile_size / 2, y = p.row * tile_size + tile_size / 2;

                BufferedImage sprite = getEnemySprite(enemy.getName());
                if (sprite != null) {
                    AffineTransform old = g2d.getTransform();

                    // Translate to enemy center and rotate based on its direction
                    g2d.translate(x, y);

                    double angle = 0.0;
                    Direction dir = enemy.getDirection();
                    if (dir == Direction.UP) {
                        angle = -Math.PI / 2;
                    } else if (dir == Direction.DOWN) {
                        angle = Math.PI / 2;
                    } else if (dir == Direction.LEFT) {
                        angle = Math.PI;
                    } else if (dir == Direction.RIGHT) {
                        angle = 0;
                    }
                    g2d.rotate(angle);

                    // Glow
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                    g2d.drawImage(sprite, -tile_size, -tile_size, tile_size * 2, tile_size * 2, null);

                    // Actual enemy sprite
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
                    g2d.drawImage(sprite, -tile_size / 2, -tile_size / 2, tile_size, tile_size, null);

                    g2d.setTransform(old);
                } else {
                    g2d.setColor(Color.RED);
                    g2d.fillOval(x - tile_size / 2, y - tile_size / 2, tile_size, tile_size);
                }

                // Name initial
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Monospaced", Font.BOLD, 10));
                FontMetrics fm = g2d.getFontMetrics();
                String initial = enemy.getName().substring(0, 1);
                g2d.drawString(initial, x - fm.stringWidth(initial) / 2, y + fm.getAscent() / 2 - 2);
            }
            g2d.setComposite(AlphaComposite.SrcOver);
        }

        private void drawDiscs(Graphics2D g2d) {
            if (activeDiscs == null)
                return;
            for (Disc d : activeDiscs) {
                if (d.hasCollisionGlow())
                    drawDiscCollisionGlow(g2d, d);
                if (d.isActive()) {
                    double smoothCol = d.getSmoothCol(), smoothRow = d.getSmoothRow();
                    double x = smoothCol * tile_size + tile_size / 2.0, y = smoothRow * tile_size + tile_size / 2.0;
                    Color color = d.getOwner() == null ? new Color(0, 255, 255) : new Color(255, 100, 100);
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                    g2d.setColor(color);
                    g2d.fillOval((int) (x - 6), (int) (y - 6), 12, 12);
                    g2d.setComposite(AlphaComposite.SrcOver);
                    g2d.setColor(Color.WHITE);
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawOval((int) (x - 5), (int) (y - 5), 10, 10);
                } else {
                    Position p = d.getPosition();
                    double x = p.col * tile_size + tile_size / 2.0, y = p.row * tile_size + tile_size / 2.0;
                    Color base = d.getOwner() == null ? new Color(0, 180, 180) : new Color(200, 80, 80);
                    g2d.setComposite(AlphaComposite.SrcOver);
                    g2d.setColor(base);
                    g2d.fillOval((int) (x - 6), (int) (y - 6), 12, 12);
                    g2d.setColor(Color.WHITE);
                    g2d.drawOval((int) (x - 5), (int) (y - 5), 10, 10);
                }
            }
            g2d.setComposite(AlphaComposite.SrcOver);
        }

        private void drawDiscCollisionGlow(Graphics2D g2d, Disc d) {
            Position cp = d.getLastCollisionPos();
            if (cp == null)
                return;
            int x = cp.col * tile_size + tile_size / 2, y = cp.row * tile_size + tile_size / 2;
            float glow = d.getCollisionGlowIntensity();
            Color color = d.getOwner() == null ? new Color(0, 255, 255) : new Color(255, 100, 100);

            int max = tile_size * 3, min = tile_size;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glow * 0.3f));
            g2d.setColor(color);
            int outer = (int) (min + (max - min) * glow);
            g2d.fillOval(x - outer / 2, y - outer / 2, outer, outer);

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glow * 0.5f));
            int mid = (int) (min + (max * 0.6f - min) * glow);
            g2d.fillOval(x - mid / 2, y - mid / 2, mid, mid);

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glow * 0.8f));
            int core = (int) (tile_size * 0.5 + (tile_size * 1.5 - tile_size * 0.5) * glow);
            g2d.fillOval(x - core / 2, y - core / 2, core, core);
            g2d.setComposite(AlphaComposite.SrcOver);
        }

        private void drawPlayerCollisionGlow(Graphics2D g2d, Position gridPos, float t) {
            if (gridPos == null)
                return;
            int cx = gridPos.col * tile_size + tile_size / 2;
            int cy = gridPos.row * tile_size + tile_size / 2;
            float alpha = Math.min(0.9f, 0.2f + t * 0.8f);

            Color glowColor = new Color(255, 90, 90, (int) (alpha * 200));
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.6f));
            g2d.setColor(glowColor);

            int maxSize = tile_size * 5;
            int size = (int) (tile_size + (maxSize - tile_size) * (1.0f - t)); // shrink while fading
            g2d.fillOval(cx - size / 2, cy - size / 2, size, size);

            // inner bright core
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.setColor(new Color(255, 140, 140, (int) (alpha * 255)));
            int core = (int) (tile_size * (1.0 + 0.6 * (1.0 - t)));
            g2d.fillOval(cx - core / 2, cy - core / 2, core, core);

            g2d.setComposite(AlphaComposite.SrcOver);
        }

        private void drawStateOverlay(Graphics2D g2d) {
            if (gameState == GameState.PAUSED) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setComposite(AlphaComposite.SrcOver);
                g2d.setColor(new Color(0, 200, 255));
                g2d.setFont(new Font("Monospaced", Font.BOLD, 30));
                String text = "PAUSED";
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2);
            }
        }

    }

    // Helper: modal themed option dialog that returns index of selected option (-1
    // if closed)
    private int showCustomOptionDialog(String title, String message, String[] options) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.BLACK);
        panel.setBorder(BorderFactory.createLineBorder(new Color(0, 200, 255), 3));

        JTextArea msgArea = new JTextArea(message);
        msgArea.setEditable(false);
        msgArea.setOpaque(false);
        msgArea.setForeground(new Color(0, 200, 255));
        msgArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        msgArea.setLineWrap(true);
        msgArea.setWrapStyleWord(true);
        msgArea.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        panel.add(msgArea, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        btnPanel.setBackground(Color.BLACK);

        final int[] result = { -1 };
        for (int i = 0; i < options.length; i++) {
            String opt = options[i];
            JButton btn = new JButton(opt);
            btn.setForeground(new Color(0, 200, 255));
            btn.setBackground(new Color(20, 20, 20));
            btn.setFocusPainted(false);
            btn.setFont(new Font("Monospaced", Font.BOLD, 13));
            final int idx = i;
            // optional hover glow
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    btn.setBackground(new Color(0, 40, 60));
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    btn.setBackground(new Color(20, 20, 20));
                }
            });
            btn.addActionListener(ae -> {
                result[0] = idx;
                dialog.setVisible(false);
                dialog.dispose();
            });
            btnPanel.add(btn);
        }

        panel.add(btnPanel, BorderLayout.SOUTH);

        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return result[0];
    }

}
