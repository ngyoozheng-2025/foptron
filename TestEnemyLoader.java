public class TestEnemyLoader {
    public static void main(String[] args) {
        ArenaView arena = new ArenaView() {
            public boolean isWall(int r, int c) { return false; }
            public boolean isJetwall(int r, int c) { return false; }
            public boolean isEmpty(int r, int c) { return true; }
            public Position getPlayerPosition() { return new Position(10,10); }
        };

        var enemies = EnemyLoader.loadEnemies("enemies.txt", arena, 20, 20);

        for (Enemy e : enemies) {
            System.out.println(e.getName() + " -> " + 
                e.getPosition().row + "," + e.getPosition().col +
                " speed=" + e.speed);
        }
    }
}
