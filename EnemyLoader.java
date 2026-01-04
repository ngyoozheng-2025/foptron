import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Random;

public class EnemyLoader {

    public static ArrayList<Enemy> loadEnemies(String filename, String difficulty, ArenaView arena, int mapRows, int mapCols) {
        ArrayList<Enemy> activeEnemies = new ArrayList<>();
        ArrayList<Enemy> enemyTemplates = new ArrayList<>();

        // 1. READ FILE to create "Templates" (Blueprints for stats)
        try {
            Scanner sc = new Scanner(new File(filename));
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(",");
                if (parts.length != 7) continue;

                String name = parts[0].trim();
                String color = parts[1].trim(); // stored but unused in logic below
                // difficulty in file (parts[2]) is ignored in favor of game difficulty setting
                int xpReward= Integer.parseInt(parts[3].trim());
                double speed = Double.parseDouble(parts[4].trim());
                double handling = Double.parseDouble(parts[5].trim());
                double aggression = Double.parseDouble(parts[6].trim());
                
                // Create a temporary enemy to hold these stats
                // Position doesn't matter here, it's just a template
                Enemy template = createEnemyFromName(name, color, speed, handling, aggression, xpReward, new Position(0,0));
                if (template != null) {
                    enemyTemplates.add(template);
                }
            }
            sc.close();
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: enemies.txt not found!");
            return activeEnemies;
        }

        // 2. SPAWN LOOP (Runs 7 times using RNG and Difficulty)
        Random random = new Random();
        
        for (int i = 0; i < 7; i++) {
            int roll = random.nextInt(100); // 0 to 99
            String nameToSpawn = "";

            if (difficulty.equalsIgnoreCase("EASY")) {
                if (roll < 70) nameToSpawn = "Koura";
                else nameToSpawn = "Sark";
            } 
            else if (difficulty.equalsIgnoreCase("HARD")) {
                if (roll < 10) nameToSpawn = "Koura";      // Rare free kill
                else if (roll < 30) nameToSpawn = "Sark";
                else if (roll < 70) nameToSpawn = "Rinzler";
                else nameToSpawn = "Clu";                  // High chance of boss
            }
            else { 
                // Default / Medium
                if (roll < 50) nameToSpawn = "Sark";
                else nameToSpawn = "Rinzler";
            }

            // Find the template stats from the file data
            Enemy template = findTemplate(enemyTemplates, nameToSpawn);
            
            if (template != null) {
                Position startPos = getRandomSpawn(arena, mapRows, mapCols);
                
                // Create the actual active enemy using template stats + new position
                Enemy newEnemy = createEnemyFromName(
                    nameToSpawn, "Red", // Default color or from template
                    template.speed, template.handling, template.aggression, 
                    template.xpReward, startPos
                );
                
                if (newEnemy != null) {
                    activeEnemies.add(newEnemy);
                }
            } else {
                System.out.println("Warning: Could not find stats for " + nameToSpawn + " in enemies.txt");
            }
        }

        return activeEnemies;
    }

    // Helper to find a specific enemy type in our loaded templates
    private static Enemy findTemplate(ArrayList<Enemy> templates, String name) {
        for (Enemy e : templates) {
            // Check class name or specific ID if you have one. 
            // Assuming simple class matching logic here:
            if (e.getClass().getSimpleName().equalsIgnoreCase(name)) {
                return e;
            }
        }
        return null;
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
                return null;
        }
    }

    // Generate a random safe position on the map
    private static Position getRandomSpawn(ArenaView arena, int mapRows, int mapCols) {
        Random rand = new Random();
        while (true) {
            int r = rand.nextInt(mapRows);
            int c = rand.nextInt(mapCols);
            if (!arena.isWall(r, c) && !arena.isJetwall(r, c) && arena.isEmpty(r, c)) {
                return new Position(r, c);
            }
        }
    }
}
