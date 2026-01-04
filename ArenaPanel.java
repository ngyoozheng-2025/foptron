import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class ArenaPanel extends JPanel {
    private final int rows;
    private final int cols;
    private int tileSize;

    private boolean[][] walls;
    private boolean[][] jetwalls;
    private ArrayList<Position> enemies = new ArrayList<>();
    private Position player;

    public ArenaPanel(String arenaType, int panelWidth, int panelHeight) {
        this.rows = 40;
        this.cols = 40;

        tileSize = Math.min(panelWidth / cols, panelHeight / rows);
        setPreferredSize(new Dimension(cols * tileSize, rows * tileSize));
        setBackground(Color.BLACK);

        walls = new boolean[rows][cols];
        jetwalls = new boolean[rows][cols];

        generateArena(arenaType);
    }

    private void generateArena(String type) {
        switch (type) {
            case "ClassicGrid" -> generateClassicGrid();
            case "NeonMaze" -> generateNeonMaze();
            case "OpenFrontier" -> generateOpenFrontier();
            case "Procedural" -> generateProcedural();
        }
    }

    private void generateClassicGrid() {
        // Outer walls
        for (int r = 0; r < rows; r++)
            walls[r][0] = walls[r][cols - 1] = true;
        for (int c = 0; c < cols; c++)
            walls[0][c] = walls[rows - 1][c] = true;
    }

    private void generateNeonMaze() {
        Random rand = new Random();
        generateClassicGrid(); // Outer walls

        // Simple maze generation: random inner walls
        for (int r = 1; r < rows - 1; r++) {
            for (int c = 1; c < cols - 1; c++) {
                if (rand.nextDouble() < 0.15)
                    walls[r][c] = true;
            }
        }
    }

    private void generateOpenFrontier() {
        // No outer walls
        // Optionally you can leave outer walls but treat outside as hazard
        // We'll just leave all empty
    }

    private void generateProcedural() {
        Random rand = new Random(System.currentTimeMillis());
        generateClassicGrid(); // Outer walls

        for (int r = 1; r < rows - 1; r++) {
            for (int c = 1; c < cols - 1; c++) {
                if (rand.nextDouble() < 0.1)
                    walls[r][c] = true;
                if (rand.nextDouble() < 0.05)
                    jetwalls[r][c] = true;
            }
        }
    }

    public void setPlayer(Position p) {
        this.player = p;
        repaint();
    }

    public void setEnemies(ArrayList<Position> enemies) {
        this.enemies = enemies;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Draw grid
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x = c * tileSize;
                int y = r * tileSize;

                if (walls[r][c])
                    g2d.setColor(Color.DARK_GRAY);
                else if (jetwalls[r][c])
                    g2d.setColor(Color.MAGENTA);
                else
                    g2d.setColor(Color.BLACK);

                g2d.fillRect(x, y, tileSize, tileSize);

                g2d.setColor(Color.GRAY);
                g2d.drawRect(x, y, tileSize, tileSize);
            }
        }

        // Draw player
        if (player != null) {
            g2d.setColor(Color.CYAN);
            g2d.fillOval(player.col * tileSize, player.row * tileSize, tileSize, tileSize);
        }

        // Draw enemies
        if (enemies != null) {
            g2d.setColor(Color.RED);
            for (Position e : enemies) {
                g2d.fillRect(e.col * tileSize, e.row * tileSize, tileSize, tileSize);
            }
        }
    }
}
