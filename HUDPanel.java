import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;

/**
 * HUD Panel - Displays player stats and event messages during gameplay
 */
public class HUDPanel extends JPanel {
    private int lives;
    private int xp;
    private int level;
    private int activeDiscs;

    private Queue<String> eventLog = new LinkedList<>();
    private static final int MAX_EVENTS = 5;

    public HUDPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(220, 700));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Semi-transparent background
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(10, 10, 200, getHeight() - 20);

        // Neon cyan border
        g2d.setColor(new Color(0, 200, 255));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(10, 10, 200, getHeight() - 20);

        // Display stats
        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2d.setColor(new Color(0, 255, 255));
        int y = 35;
        int lineHeight = 22;

        g2d.drawString("LIVES: " + lives, 20, y);
        y += lineHeight;
        g2d.drawString("LEVEL: " + level, 20, y);
        y += lineHeight;
        g2d.drawString("XP: " + xp + "/100", 20, y);
        y += lineHeight;

        // XP Progress bar
        drawProgressBar(g2d, 20, y, 180, 10, xp, 100);
        y += lineHeight + 5;

        g2d.drawString("DISCS: " + activeDiscs, 20, y);
        y += lineHeight + 10;

        // Event log
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2d.setColor(new Color(255, 200, 100)); // Yellow for events
        g2d.drawString("--- EVENTS ---", 20, y);
        y += lineHeight;

        for (String event : eventLog) {
            if (y + lineHeight > getHeight() - 20)
                break;
            g2d.drawString(event, 20, y);
            y += lineHeight;
        }
    }

    private void drawProgressBar(Graphics2D g2d, int x, int y, int width, int height, int current, int max) {
        g2d.setColor(new Color(40, 40, 60));
        g2d.fillRect(x, y, width, height);

        int filledWidth = (int) (width * ((double) current / max));
        g2d.setColor(new Color(0, 200, 255));
        g2d.fillRect(x, y, filledWidth, height);

        g2d.setColor(new Color(0, 255, 255));
        g2d.drawRect(x, y, width, height);
    }

    /**
     * Update player stats
     */
    public void updateStats(int lives, int xp, int level, int activeDiscs) {
        this.lives = lives;
        this.xp = xp;
        this.level = level;
        this.activeDiscs = activeDiscs;
        repaint();
    }

    /**
     * Add an event message to the log
     */
    public void addEvent(String message) {
        if (eventLog.size() >= MAX_EVENTS) {
            eventLog.poll(); // Remove oldest
        }
        eventLog.offer(message);
        repaint();
    }

    /**
     * Clear all events
     */
    public void clearEvents() {
        eventLog.clear();
        repaint();
    }
}
