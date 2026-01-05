import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;

public class ArenaPanel extends JPanel implements KeyListener {

    public enum ArenaType {
        CLASSIC, NEON, OPEN, PROCEDURAL
    }

    public static final int ROWS = 40;
    public static final int COLS = 40;
    private static final int TILE_SIZE = 12;
    private static final int PADDING = 30;

    private final boolean[][] walls = new boolean[ROWS][COLS];
    private final ArenaType type;
    private Random rand;

    // Player
    private int playerRow = ROWS / 2;
    private int playerCol = COLS / 2;

    // Enemies
    private final ArrayList<Position> enemies = new ArrayList<>();

    public ArenaPanel(ArenaType type) {
        this.type = type;
        setFocusable(true);
        addKeyListener(this);

        if (type == ArenaType.NEON) {
            this.rand = new Random(12345); // deterministic
        } else {
            this.rand = new Random(); // random
        }

        setPreferredSize(new Dimension(
                COLS * TILE_SIZE + PADDING * 2,
                ROWS * TILE_SIZE + PADDING * 2));

        generateArena();
        spawnPlayer();
        spawnEnemies(5); // spawn 5 enemies for demo
    }

    /* ================= ARENA GENERATION ================= */

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
        ArrayList<int[]> directions = new ArrayList<>();
        directions.add(new int[] { -2, 0 });
        directions.add(new int[] { 2, 0 });
        directions.add(new int[] { 0, -2 });
        directions.add(new int[] { 0, 2 });
        Collections.shuffle(directions, fixedRand);

        for (int[] dir : directions) {
            int nr = r + dir[0];
            int nc = c + dir[1];

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
        rand = new Random(); // truly random each time
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                walls[r][c] = false;

        for (int i = 0; i < ROWS * COLS / 5; i++)
            walls[rand.nextInt(ROWS)][rand.nextInt(COLS)] = true;
    }

    private void spawnPlayer() {
        playerRow = ROWS / 2;
        playerCol = COLS / 2;
        if (walls[playerRow][playerCol])
            playerRow = 1; // move to empty if blocked
    }

    private void spawnEnemies(int count) {
        enemies.clear();
        for (int i = 0; i < count; i++) {
            int er, ec;
            do {
                er = rand.nextInt(ROWS);
                ec = rand.nextInt(COLS);
            } while (walls[er][ec] || (er == playerRow && ec == playerCol));
            enemies.add(new Position(er, ec));
        }
    }

    /* ================= RENDERING ================= */

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        for (int r = 0; r < ROWS; r++) {
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
        }

        // Draw player
        g.setColor(Color.RED);
        g.fillOval(PADDING + playerCol * TILE_SIZE, PADDING + playerRow * TILE_SIZE, TILE_SIZE, TILE_SIZE);

        // Draw enemies
        g.setColor(Color.YELLOW);
        for (Position e : enemies) {
            g.fillOval(PADDING + e.col * TILE_SIZE, PADDING + e.row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }
    }

    /* ================= PLAYER MOVEMENT ================= */

    @Override
    public void keyPressed(KeyEvent e) {
        int newR = playerRow;
        int newC = playerCol;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> newR--;
            case KeyEvent.VK_S -> newR++;
            case KeyEvent.VK_A -> newC--;
            case KeyEvent.VK_D -> newC++;
        }

        if (inBounds(newR, newC) && !walls[newR][newC]) {
            // leave a jetwall trail
            walls[playerRow][playerCol] = false; // optional: leave true if trail is a wall
            playerRow = newR;
            playerCol = newC;
            // can also mark trail here as "jetwall" for persistent trail
        }

        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    /* ================= UTILS ================= */

    public boolean inBounds(int r, int c) {
        return r >= 0 && r < ROWS && c >= 0 && c < COLS;
    }

    // Enemy helper class
    public static class Position {
        public int row, col;

        public Position(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }
}
