package src.UIGameEngine;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;

/**
 * HUD Panel - Displays player stats, event messages, and a collision glow
 * flash.
 */
public class HUDPanel extends JPanel {
    private int lives;
    private int xp;
    private int level;
    private int activeDiscs;
    private int xpForNextLevel = 100; // dynamic, updated from GameEngine

    private final Queue<String> eventLog = new LinkedList<>();
    private static final int MAX_EVENTS = 5;

    // Collision flash (controlled via Swing Timer)
    private float collisionAlpha = 0f;
    private javax.swing.Timer collisionTimer;
    private static final int COLLISION_DURATION_MS = 300; // total duration
    private static final int COLLISION_TICK_MS = 40; // update rate

    public HUDPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(220, 700));
    }

    /**
     * Trigger a brief collision flash on the HUD.
     */
    public void triggerCollisionGlow() {
        if (collisionTimer != null && collisionTimer.isRunning()) {
            collisionTimer.stop();
        }
        collisionAlpha = 1.0f;
        int steps = Math.max(1, COLLISION_DURATION_MS / COLLISION_TICK_MS);
        collisionTimer = new javax.swing.Timer(COLLISION_TICK_MS, e -> {
            collisionAlpha -= 1.0f / steps;
            if (collisionAlpha <= 0f) {
                collisionAlpha = 0f;
                ((javax.swing.Timer) e.getSource()).stop();
            }
            repaint();
        });
        collisionTimer.setInitialDelay(0);
        collisionTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // HUD background
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(10, 10, 200, getHeight() - 20, 8, 8);

        // Collision overlay (glow)
        if (collisionAlpha > 0f) {
            Color glow = new Color(255, 60, 60, Math.min(200, (int) (collisionAlpha * 200)));
            g2d.setColor(glow);
            g2d.fillRoundRect(10, 10, 200, getHeight() - 20, 8, 8);
        }

        // Border
        g2d.setColor(new Color(0, 200, 255));
        g2d.setStroke(new BasicStroke(2f));
        g2d.drawRoundRect(10, 10, 200, getHeight() - 20, 8, 8);

        // Stats text
        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2d.setColor(new Color(0, 255, 255));
        int y = 38;
        int lineHeight = 22;

        g2d.drawString("LIVES: " + lives, 20, y);
        y += lineHeight;
        g2d.drawString("LEVEL: " + level, 20, y);
        y += lineHeight;
        g2d.drawString("XP: " + xp + " / " + xpForNextLevel, 20, y);
        y += lineHeight;

        // XP bar
        drawProgressBar(g2d, 20, y, 180, 10, xp, xpForNextLevel);
        y += lineHeight + 6;

        g2d.drawString("DISCS: " + activeDiscs, 20, y);
        y += lineHeight + 8;

        // Events
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2d.setColor(new Color(255, 200, 100));
        g2d.drawString("--- EVENTS ---", 20, y);
        y += lineHeight;

        int displayed = 0;
        for (String ev : eventLog) {
            if (y + lineHeight > getHeight() - 20 || displayed >= MAX_EVENTS)
                break;
            g2d.drawString(ev, 20, y);
            y += lineHeight;
            displayed++;
        }

        g2d.dispose();
    }

    private void drawProgressBar(Graphics2D g2d, int x, int y, int width, int height, int current, int max) {
        g2d.setColor(new Color(30, 30, 45));
        g2d.fillRect(x, y, width, height);

        int safeMax = Math.max(1, max);
        int filled = (int) (width * ((double) current / safeMax));
        g2d.setColor(new Color(0, 200, 255));
        g2d.fillRect(x, y, Math.max(0, Math.min(width, filled)), height);

        g2d.setColor(new Color(0, 255, 255));
        g2d.drawRect(x, y, width, height);
    }

    /**
     * Update displayed stats.
     *
     * @param lives          lives (int)
     * @param xp             current xp (int)
     * @param level          level (int)
     * @param activeDiscs    discs owned (int)
     * @param xpForNextLevel xp threshold for next level (int)
     */
    public void updateStats(int lives, int xp, int level, int activeDiscs, int xpForNextLevel) {
        this.lives = lives;
        this.xp = xp;
        this.level = level;
        this.activeDiscs = activeDiscs;
        this.xpForNextLevel = Math.max(1, xpForNextLevel);
        repaint();
    }

    public void addEvent(String message) {
        if (eventLog.size() >= MAX_EVENTS)
            eventLog.poll();
        eventLog.offer(message);
        repaint();
    }

    public void clearEvents() {
        eventLog.clear();
        repaint();
    }
}
