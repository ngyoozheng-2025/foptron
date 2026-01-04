import java.util.ArrayList;
import java.awt.Graphics2D;
import src.Characters.Characters;
import src.TEMP_Main.TEMP_GamePanel;

public class TestDisc {
    
    // Test character class that extends Characters for testing
    static class TestCharacter extends Characters {
        private boolean wasHit = false;
        private Enemy lastHitBy = null;
        
        public TestCharacter(String name, String color, int xp, int level, double speed, 
                double stability, double handling, int disc_slot, int discs_owned, double lives,
                int start_row, int start_column, int alive) {
            super(name, color, xp, level, speed, stability, handling, disc_slot, discs_owned, lives,
                  start_row, start_column, alive);
        }
        
        @Override
        public void hitByDisc(Object owner) {
            wasHit = true;
            if (owner instanceof Enemy) {
                lastHitBy = (Enemy) owner;
                updateLives(-1.0); // Lose a life when hit
                System.out.println("    -> Character hit by disc from " + ((Enemy)owner).getName() + "! Lives: " + getLives());
            } else {
                updateLives(-1.0);
                System.out.println("    -> Character hit by disc! Lives: " + getLives());
            }
        }
        
        public boolean wasHit() { return wasHit; }
        public Enemy getLastHitBy() { return lastHitBy; }
        
        @Override
        protected void applyStatIncrease() {
            // Empty implementation for testing
        }
        
        @Override
        public String getBaseImagePath() {
            return "/res/Tron/Tron_base.png";
        }
        
        @Override
        public String getOverlayImagePath() {
            return "/res/Tron/Tron_stripe.png";
        }
        
        @Override
        public void update() {
            // Empty implementation for testing
        }
        
        @Override
        public void draw(Graphics2D g2, TEMP_GamePanel gp, double radians, double velocity) {
            // Empty implementation for testing
        }
    }
    
    public static void main(String[] args) {
        System.out.println("=== Testing Disc ===\n");
        
        // Create a mock arena
        ArenaView arena = new ArenaView() {
            @Override public boolean isWall(int r, int c) { return false; }
            @Override public boolean isJetwall(int r, int c) { return false; }
            @Override public boolean isEmpty(int r, int c) { return true; }
            @Override public Position getPlayerPosition() { return new Position(10, 10); }
        };

        // Create test player at position (10, 10)
        TestCharacter player = new TestCharacter("Tron", "BLUE", 0, 1, 2.0, 1.0, 1.0, 3, 3, 3.0, 10, 10, 1);

        // Create enemy list
        ArrayList<Enemy> enemies = new ArrayList<>();
        Enemy enemy1 = new Clu(3.0, 3.0, 1.0, 1000, new Position(12, 10));
        Enemy enemy2 = new Sark(2.0, 2.0, 0.5, 100, new Position(15, 15));
        enemies.add(enemy1);
        enemies.add(enemy2);

        System.out.println("--- Test 1: Disc movement and range limit ---");
        Disc disc1 = new Disc(enemy1.getPosition(), Direction.DOWN, enemy1);
        System.out.println("Disc starting at: (" + disc1.getPosition().row + ", " + disc1.getPosition().col + ")");
        System.out.println("Max range: 3 tiles");
        System.out.println("Direction: DOWN");
        
        for (int i = 0; i < 5; i++) {
            disc1.update(arena, player, enemies);
            System.out.println("Step " + (i + 1) + ": Position (" + disc1.getPosition().row + ", " + disc1.getPosition().col
                + ") | Active: " + disc1.isActive());
            if (!disc1.isActive()) {
                if (player.wasHit()) {
                    System.out.println("    -> Disc deactivated (hit player)");
                } else {
                    System.out.println("    -> Disc deactivated (reached max range or hit obstacle)");
                }
                break;
            }
        }
        
        System.out.println("\n--- Test 2: Disc hitting player ---");
        // Create new player for clean test
        TestCharacter player2 = new TestCharacter("Tron", "BLUE", 0, 1, 2.0, 1.0, 1.0, 3, 3, 3.0, 10, 10, 1);
        Disc disc2 = new Disc(new Position(10, 9), Direction.RIGHT, enemy1);
        System.out.println("Disc starting at: (" + disc2.getPosition().row + ", " + disc2.getPosition().col + ")");
        System.out.println("Player at: (10, 10)");
        System.out.println("Disc direction: RIGHT (will hit player on next step)");
        
        disc2.update(arena, player2, enemies);
        System.out.println("After update: Position (" + disc2.getPosition().row + ", " + disc2.getPosition().col
            + ") | Active: " + disc2.isActive());
        System.out.println("Player was hit: " + player2.wasHit());
        System.out.println("Player lives remaining: " + player2.getLives());
        
        System.out.println("\n--- Test 3: Disc hitting another enemy ---");
        TestCharacter player3 = new TestCharacter("Tron", "BLUE", 0, 1, 2.0, 1.0, 1.0, 3, 3, 3.0, 20, 20, 1);
        // Reset enemy2
        Enemy enemy3 = new Sark(2.0, 2.0, 0.5, 100, new Position(15, 15));
        ArrayList<Enemy> enemies2 = new ArrayList<>();
        enemies2.add(enemy1);
        enemies2.add(enemy3);
        
        Disc disc3 = new Disc(new Position(15, 14), Direction.RIGHT, enemy1);
        System.out.println("Disc starting at: (" + disc3.getPosition().row + ", " + disc3.getPosition().col + ")");
        System.out.println("Enemy3 (Sark) at: (" + enemy3.getPosition().row + ", " + enemy3.getPosition().col + ")");
        System.out.println("Disc direction: RIGHT (will hit enemy3 on next step)");
        System.out.println("Enemy3 alive before: " + enemy3.isAlive());
        
        disc3.update(arena, player3, enemies2);
        System.out.println("After update: Position (" + disc3.getPosition().row + ", " + disc3.getPosition().col
            + ") | Active: " + disc3.isActive());
        System.out.println("Enemy3 alive after: " + enemy3.isAlive());
        
        System.out.println("\n--- Test 4: Disc hitting wall ---");
        ArenaView arenaWithWall = new ArenaView() {
            @Override public boolean isWall(int r, int c) { return (r == 5 && c == 10); }
            @Override public boolean isJetwall(int r, int c) { return false; }
            @Override public boolean isEmpty(int r, int c) { return true; }
            @Override public Position getPlayerPosition() { return new Position(10, 10); }
        };
        
        Disc disc4 = new Disc(new Position(6, 10), Direction.UP, enemy1);
        System.out.println("Disc starting at: (" + disc4.getPosition().row + ", " + disc4.getPosition().col + ")");
        System.out.println("Wall at: (5, 10)");
        System.out.println("Disc direction: UP (will hit wall on next step)");
        
        disc4.update(arenaWithWall, player3, enemies2);
        System.out.println("After update: Position (" + disc4.getPosition().row + ", " + disc4.getPosition().col
            + ") | Active: " + disc4.isActive());
        
        System.out.println("\n--- Test 5: Disc owner cannot hit itself ---");
        Disc disc5 = new Disc(new Position(12, 9), Direction.RIGHT, enemy1);
        System.out.println("Disc starting at: (" + disc5.getPosition().row + ", " + disc5.getPosition().col + ")");
        System.out.println("Owner (enemy1) at: (" + enemy1.getPosition().row + ", " + enemy1.getPosition().col + ")");
        System.out.println("Disc direction: RIGHT (will reach owner position on next step)");
        System.out.println("Enemy1 alive before: " + enemy1.isAlive());
        
        disc5.update(arena, player3, enemies2);
        System.out.println("After update: Position (" + disc5.getPosition().row + ", " + disc5.getPosition().col
            + ") | Active: " + disc5.isActive());
        System.out.println("Enemy1 alive after: " + enemy1.isAlive() + " (should still be alive - disc cannot hit owner)");
        
        System.out.println("\n=== Test Complete ===");
    }
}
