public class TestEnemyMovement {

    public static void main(String[] args) {

        // Dummy arena: always empty, no walls
        ArenaView arena = new ArenaView() {
            @Override public boolean isWall(int r, int c) { return false; }
            @Override public boolean isJetwall(int r, int c) { return false; }
            @Override public boolean isEmpty(int r, int c) { return true; }
            @Override public Position getPlayerPosition() { return new Position(10, 10); }
        };

        int rows = 20;
        int cols = 20;

        var enemies = EnemyLoader.loadEnemies("enemies.txt", arena, rows, cols);

        // Print initial spawn
        System.out.println("=== ENEMY SPAWN POSITIONS ===");
        for (Enemy e : enemies) {
            System.out.println(e.getName() + " -> (" + e.getPosition().row + "," + e.getPosition().col + ")");
        }

        System.out.println("\n=== RUNNING MOVEMENT TEST ===");

        // Simulate 10 movement steps
        for (int step = 1; step <= 10; step++) {
            System.out.println("\nStep " + step);

            for (Enemy e : enemies) {
                e.decideNextMove(arena);  // AI movement

                System.out.println(e.getName() + " at (" +
                    e.getPosition().row + "," + e.getPosition().col + ")");
            }
        }
    }
}
