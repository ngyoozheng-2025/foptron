package src;

import javax.swing.SwingUtilities;
import src.UIGameEngine.MainMenu;

/**
 * Main Launcher for FOP Tron Game
 * This is the entry point that starts the game
 */
public class Launcher {
    public static void main(String[] args) {
        // Start the game on the Event Dispatch Thread (required for Swing)
        SwingUtilities.invokeLater(() -> {
            try {
                new MainMenu();
            } catch (Exception e) {
                System.err.println("Error starting game: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}
