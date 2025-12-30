import java.io.*;
import java.util.*;

public class SaveSystem {
    private static final String SAVE_FILE = "player_stats.csv";

    // Save player data: Name, Score, Wins
    public static void savePlayerData(String name, int score, int wins) {
        try (PrintWriter out = new PrintWriter(new FileWriter(SAVE_FILE, true))) {
            out.println(name + "," + score + "," + wins);
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    // Load all data for leaderboard
    public static List<String[]> loadAllData() {
        List<String[]> data = new ArrayList<>();
        File file = new File(SAVE_FILE);
        if (!file.exists()) return data;

        try (BufferedReader br = new BufferedReader(new FileReader(SAVE_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                data.add(line.split(","));
            }
        } catch (IOException e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
        return data;
    }
}