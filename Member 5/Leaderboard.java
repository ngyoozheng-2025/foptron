import java.io.*;
import java.util.*;

public class Leaderboard {
    private static final String LEADERBOARD_FILE = "leaderboard.csv";
    private static final int TOP_LIMIT = 10;

    public static void addEntry(String name, int level, int score) {
        List<String> entries = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(LEADERBOARD_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                entries.add(line);
            }
        } catch (IOException ignored) {}

        entries.add(name + "," + level + "," + score + "," + new java.util.Date());
        
        // Sort by score (descending)
        entries.sort((a, b) -> {
            int scoreA = Integer.parseInt(a.split(",")[2]);
            int scoreB = Integer.parseInt(b.split(",")[2]);
            return Integer.compare(scoreB, scoreA);
        });

        // Write back top 10
        try (PrintWriter writer = new PrintWriter(new FileWriter(LEADERBOARD_FILE))) {
            for (int i = 0; i < Math.min(entries.size(), TOP_LIMIT); i++) {
                writer.println(entries.get(i));
            }
        } catch (IOException e) {
            System.out.println("Leaderboard Update Error.");
        }
    }

    public static void display() {
        System.out.println("\n=== THE GRID: TOP 10 USERS ===");
        System.out.printf("%-15s %-10s %-10s %-20s\n", "NAME", "LEVEL", "SCORE", "DATE");
        try (BufferedReader reader = new BufferedReader(new FileReader(LEADERBOARD_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.split(",");
                System.out.printf("%-15s %-10s %-10s %-20s\n", p[0], p[1], p[2], p[3]);
            }
        } catch (IOException e) {
            System.out.println("Leaderboard is empty.");
        }
    }
}