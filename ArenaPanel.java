import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class ArenaPanel extends JPanel implements KeyListener {

    public enum ArenaType {
        CLASSIC, NEON, OPEN, PROCEDURAL
    }

    public static final int ROWS = 40;
    public static final int COLS = 40;
    private static final int TILE_SIZE = 16; // bigger grid
    private static final int PADDING = 20;

    private final boolean[][] walls = new boolean[ROWS][COLS];
    private final ArenaType type;
    private Difficulty difficulty; // can be null for preview
    private Random rand;

    private Position playerPos; // can be null for preview
    private final ArrayList<Position> enemies = new ArrayList<>();

    // ================= CONSTRUCTORS =================

    // Full game
    public ArenaPanel(ArenaType type, Difficulty difficulty) {
        this.type = type;
        this.difficulty = difficulty;

        setFocusable(true);
        addKeyListener(this);

        rand = (type == ArenaType.NEON) ? new Random(12345) : new Random();

        setPreferredSize(new Dimension(
                COLS * TILE_SIZE + PADDING * 2,
                ROWS * TILE_SIZE + PADDING * 2));

        generateArena();

        if (difficulty != null) { // only spawn player/enemies if not preview
            spawnPlayer();
            spawnEnemies();
        }
    }

    // ================= ARENA GENERATION =================

    private void generateArena() {
        clearArena();
        switch (type) {
            case CLASSIC -> generateClassic();
            case NEON -> generateNeon();
            case OPEN -> generateOpen();
            case PROCEDURAL -> generateProcedural();
        }
    }

    private void clearArena() {
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                walls[r][c] = false;
    }

    private void generateClassic() {
        for (int r = 0; r < ROWS; r++) {
            walls[r][0] = true;
            walls[r][COLS - 1] = true;
        }
        for (int c = 0; c < COLS; c++) {
            walls[0][c] = true;
            walls[ROWS - 1][c] = true;
        }
    }

    private void generateNeon() {
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                walls[r][c] = true;
        carveMaze(1, 1, rand);

        // borders
        for (int r = 0; r < ROWS; r++) {
            walls[r][0] = true;
            walls[r][COLS - 1] = true;
        }
        for (int c = 0; c < COLS; c++) {
            walls[0][c] = true;
            walls[ROWS - 1][c] = true;
        }
    }

    private void carveMaze(int r, int c, Random fixedRand) {
        walls[r][c] = false;
        ArrayList<int[]> dirs = new ArrayList<>();
        dirs.add(new int[] { -2, 0 });
        dirs.add(new int[] { 2, 0 });
        dirs.add(new int[] { 0, -2 });
        dirs.add(new int[] { 0, 2 });
        Collections.shuffle(dirs, fixedRand);
        for (int[] dir : dirs) {
            int nr = r + dir[0], nc = c + dir[1];
            if (nr > 0 && nr < ROWS - 1 && nc > 0 && nc < COLS - 1 && walls[nr][nc]) {
                walls[r + dir[0] / 2][c + dir[1] / 2] = false;
                carveMaze(nr, nc, fixedRand);
            }
        }
    }

    private void generateOpen() {
        clearArena();
    }

    private void generateProcedural() {
        rand = new Random();
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                walls[r][c] = false;
        for (int i = 0; i < ROWS * COLS / 5; i++)
            walls[rand.nextInt(ROWS)][rand.nextInt(COLS)] = true;
    }

    // ================= SPAWN =================

    private void spawnPlayer() {
        playerPos = getRandomEmptyPosition();
    }

    private void spawnEnemies() {
        enemies.clear();
        for (int i = 0; i < 5; i++) {
            Position pos;
            int attempts = 0;
            do {
                pos = new Position(rand.nextInt(ROWS), rand.nextInt(COLS));
                attempts++;
                if (attempts > 500) {
                    pos = playerPos;
                    break;
                }
            } while (walls[pos.row][pos.col] || pos.equals(playerPos) || enemies.contains(pos));
            enemies.add(pos);
        }
    }

    private Position getRandomEmptyPosition() {
        int r, c, attempts = 0;
        do {
            r = rand.nextInt(ROWS);
            c = rand.nextInt(COLS);
            attempts++;
            if (attempts > 500)
                return new Position(1, 1);
        } while (walls[r][c]);
        return new Position(r, c);
    }

    // ================= PAINT =================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++) {
                int x = PADDING + c * TILE_SIZE;
                int y = PADDING + r * TILE_SIZE;
                if (walls[r][c]) {
                    g.setColor(new Color(0, 200, 255));
                    g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                }
                g.setColor(new Color(50, 50, 50));
                g.drawRect(x, y, TILE_SIZE, TILE_SIZE);
            }

        // draw player only if exists
        if (playerPos != null) {
            g.setColor(Color.RED);
            g.fillOval(PADDING + playerPos.col * TILE_SIZE, PADDING + playerPos.row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        // draw enemies only if exist
        if (!enemies.isEmpty()) {
            g.setColor(Color.YELLOW);
            for (Position e : enemies) {
                g.fillOval(PADDING + e.col * TILE_SIZE, PADDING + e.row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (playerPos == null)
            return; // preview mode, skip
        int r = playerPos.row, c = playerPos.col;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> r--;
            case KeyEvent.VK_S -> r++;
            case KeyEvent.VK_A -> c--;
            case KeyEvent.VK_D -> c++;
        }
        if (r >= 0 && r < ROWS && c >= 0 && c < COLS && !walls[r][c])
            playerPos = new Position(r, c);
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    // ================= PREVIEW UTILITY =================
    public void removePlayerAndEnemies() {
        playerPos = null;
        enemies.clear();
    }

    // ================= POSITION CLASS =================
    public static class Position {
        public int row, col;

        public Position(int r, int c) {
            row = r;
            col = c;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Position))
                return false;
            Position o = (Position) obj;
            return this.row == o.row && this.col == o.col;
        }
    }
}
