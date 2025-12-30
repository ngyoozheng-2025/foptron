import java.io.*;

public class SaveSystem {
    private static final String SAVE_FILE = "savegame.csv";

    public static void saveProgress(Characters player) {
        // Requirements check: Prompt before save could be handled in GameLoop 
        try (PrintWriter writer = new PrintWriter(new FileWriter(SAVE_FILE))) {
            // Format matching your Characters.java constructor order
            // Note: Direct access to player.lives/discs_owned requires getters if private/protected
            writer.println(player.getName() + "," + 
                           player.getXp() + "," + 
                           player.getLevel() + "," + 
                           player.updateLives(0) + "," + // Using method to get current lives
                           player.getDiscsOwned()); // Ensure you add getDiscsOwned() to Characters.java
            System.out.println("Progress saved to " + SAVE_FILE);
        } catch (IOException e) {
            System.out.println("Error saving game: " + e.getMessage() );
        }
    }

    public static String[] loadProgress() {
        try (BufferedReader reader = new BufferedReader(new FileReader(SAVE_FILE))) {
            String line = reader.readLine();
            if (line != null) return line.split(",");
        } catch (IOException e) {
            System.out.println("No save file found. Starting fresh.");
        }
        return null;
    }
}