import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class EnemyLoader {

    public static ArrayList<Enemy> loadEnemies(String filename, ArenaView arena, int mapRows, int mapCols)
 {
        ArrayList<Enemy> enemies = new ArrayList<>();

        try {
            Scanner sc = new Scanner(new File(filename));

            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();

                // Skip empty or comment lines
                if (line.isEmpty() || line.startsWith("#"))
                    continue;

                // Split CSV values
                String[] parts = line.split(",");

                if (parts.length != 7) {
                    System.out.println("Invalid enemy line: " + line);
                    continue;
                }

                // Extract values
                String name = parts[0].trim();
                String color = parts[1].trim();
                String difficulty = parts[2].trim(); // not used yet
                int xp = Integer.parseInt(parts[3].trim());
                double speed = Double.parseDouble(parts[4].trim());
                double handling = Double.parseDouble(parts[5].trim());
                double aggression = Double.parseDouble(parts[6].trim());

                Position startPos = getRandomSpawn(arena, mapRows, mapCols);


                Enemy enemy = createEnemyFromName(
                    name, color, speed, handling, aggression, xp, startPos
                );

                if (enemy != null) {
                   applyDifficultyMultiplier(difficulty, enemy);
                   enemies.add(enemy);
                }

            }

            sc.close();

        } catch (FileNotFoundException e) {
            System.out.println("ERROR: enemies.txt not found!");
        }

        return enemies;
    }

    private static void applyDifficultyMultiplier(String difficulty, Enemy e) {
    switch (difficulty.toLowerCase()) {
        case "impossible":
            e.speed *= 1.8;
            e.handling *= 1.8;
            e.aggression *= 1.6;
            break;

        case "hard":
            e.speed *= 1.5;
            e.handling *= 1.4;
            e.aggression *= 1.3;
            break;

        case "medium":
            e.speed *= 1.2;
            e.handling *= 1.1;
            break;

        case "easy":
        default:
            // No changes for easy enemies
            break;
    }
   }
 

    // Factory method to create enemy subclasses
    private static Enemy createEnemyFromName(
        String name, String color, double speed, double handling,
        double aggression, int xp, Position pos
    ) {
        switch (name.toLowerCase()) {
            case "clu":
                return new Clu(speed, handling, aggression, xp, pos);
            case "rinzler":
                return new Rinzler(speed, handling, aggression, xp, pos);
            case "sark":
                return new Sark(speed, handling, aggression, xp, pos);
            case "koura":
                return new Koura(speed, handling, aggression, xp, pos);
            default:
                System.out.println("Unknown enemy: " + name);
                return null;
        }
    }




// Generate a random safe position on the map
private static Position getRandomSpawn(ArenaView arena, int mapRows, int mapCols) {
    java.util.Random rand = new java.util.Random();

    while (true) {
        int r = rand.nextInt(mapRows);
        int c = rand.nextInt(mapCols);

        // Conditions for a valid spawn:
        if (!arena.isWall(r, c) &&
            !arena.isJetwall(r, c) &&
            arena.isEmpty(r, c)) {

            return new Position(r, c);
        }
    }
}

}

