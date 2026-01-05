import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.File;

public class TestEnemyLoader {
    public static void main(String[] args) {
        System.out.println("Starting EnemyLoader test...");
        System.out.flush();

        // Check if enemies.txt exists
        File enemiesFile = new File("enemies.txt");
        if (!enemiesFile.exists()) {
            System.err.println("ERROR: enemies.txt not found in current directory!");
            System.err.println("Current directory: " + System.getProperty("user.dir"));
            return;
        }

        // Create a mock arena for testing
        ArenaView arena = new ArenaView() {
            public boolean isWall(int r, int c) {
                return false;
            }

            public boolean isJetwall(int r, int c) {
                return false;
            }

            public boolean isEmpty(int r, int c) {
                return true;
            }

            public Position getPlayerPosition() {
                return new Position(10, 10);
            }
        };

        // Test all difficulty levels
        String[] difficulties = { "EASY", "MEDIUM", "HARD", "IMPOSSIBLE" };

        System.out.println("=== Testing EnemyLoader ===\n");
        System.out.flush();

        for (String difficulty : difficulties) {
            try {
                System.out.println("--- Testing Difficulty: " + difficulty + " ---");
                System.out.flush();

                ArrayList<Enemy> enemies = EnemyLoader.loadEnemies("enemies.txt", difficulty, arena, 20, 20);

                if (enemies == null || enemies.isEmpty()) {
                    System.out.println("WARNING: No enemies loaded for difficulty " + difficulty);
                    System.out.flush();
                    continue;
                }

                // Count enemy types
                Map<String, Integer> enemyCounts = new HashMap<>();
                for (Enemy e : enemies) {
                    String name = e.getName();
                    enemyCounts.put(name, enemyCounts.getOrDefault(name, 0) + 1);
                }

                // Display results
                System.out.println("Total enemies loaded: " + enemies.size());
                System.out.println("Enemy distribution:");
                for (Map.Entry<String, Integer> entry : enemyCounts.entrySet()) {
                    System.out.println("  " + entry.getKey() + ": " + entry.getValue());
                }

                // Display detailed info for each enemy
                System.out.println("\nDetailed enemy information:");
                for (Enemy e : enemies) {
                    System.out.println("  " + e.getName() +
                            " | Position: (" + e.getPosition().row + "," + e.getPosition().col + ")" +
                            " | Speed: " + e.speed +
                            " | Handling: " + e.handling +
                            " | Aggression: " + e.aggression +
                            " | XP Reward: " + e.getXpReward() +
                            " | Color: " + e.getColor());
                }

                System.out.println();
                System.out.flush();
            } catch (Exception e) {
                System.err.println("ERROR testing difficulty " + difficulty + ": " + e.getMessage());
                e.printStackTrace();
                System.err.flush();
            }
        }

        System.out.println("=== Test Complete ===");
        System.out.flush();
    }
}
