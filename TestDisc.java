import java.util.ArrayList;

public class TestDisc {
    public static void main(String[] args) {

        // Dummy Arena (no walls, no jetwalls)
        ArenaView arena = new ArenaView() {
            @Override public boolean isWall(int r, int c) { return false; }
            @Override public boolean isJetwall(int r, int c) { return false; }
            @Override public boolean isEmpty(int r, int c) { return true; }
            @Override public Position getPlayerPosition() { return new Position(10, 10); }
        };

        // Dummy player (Character)
        Character dummyPlayer = new CharacterTester(new Position(10, 10));

        // Dummy enemy list
        ArrayList<Enemy> enemies = new ArrayList<>();
        Enemy dummyEnemy = new Clu(3, 3, 1, 100, new Position(12, 10));
        enemies.add(dummyEnemy);

        // Disc thrown by dummyEnemy
        Disc disc = new Disc(dummyEnemy.getPosition(), Direction.UP, dummyEnemy);

        System.out.println("Disc starting at: " + disc.getPosition().row + ", " + disc.getPosition().col);

        // Move disc for 5 steps
        for (int i = 0; i < 5; i++) {
            disc.update(arena, dummyPlayer, enemies);
            System.out.println("Step " + i + ": " + disc.getPosition().row + ", " + disc.getPosition().col
                + " active=" + disc.isActive());
        }
    }
}
