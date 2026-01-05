import java.util.Random;

public class Arena implements ArenaView {

    public static final int SIZE = 40;
    protected final int[][] grid;
    protected final boolean openArena;
    protected final String name;
    protected final Long seed;

    // Player/enemy positions
    protected Position playerSpawn;
    protected Position playerPosition;

    public Arena(String name) {
        this(name, null);
    }

    public Arena(String name, Long seed) {
        this.name = name;
        this.seed = seed;
        this.grid = new int[SIZE][SIZE];

        // Generate arena
        switch (name.toUpperCase()) {
            case "CLASSICGRID" -> {
                openArena = false;
                generateClassicGrid();
            }
            case "NEONMAZE" -> {
                openArena = false;
                generateNeonMaze();
            }
            case "OPENFRONTIER" -> {
                openArena = true;
                generateOpenFrontier();
            }
            default -> {
                openArena = false;
                generateProcedural(seed);
            }
        }

        // Determine player spawn
        playerSpawn = determinePlayerSpawn();
        playerPosition = new Position(playerSpawn.row, playerSpawn.col);
    }

    /* ================= ARENA GENERATORS ================= */

    private void generateClassicGrid() {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                grid[r][c] = (r == 0 || c == 0 || r == SIZE - 1 || c == SIZE - 1) ? 1 : 0;

        for (int i = 5; i < 35; i += 10) {
            grid[10][i] = 1;
            grid[29][i] = 1;
        }
    }

    private void generateNeonMaze() {
        // Fixed pattern for Neon Maze
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                grid[r][c] = 1;

        // Horizontal corridors
        for (int r = 3; r < SIZE - 3; r += 6)
            for (int c = 1; c < SIZE - 1; c++) {
                grid[r][c] = 0;
                if (r + 1 < SIZE - 1)
                    grid[r + 1][c] = 0;
            }

        // Vertical corridors
        for (int c = 4; c < SIZE - 4; c += 8)
            for (int r = 1; r < SIZE - 1; r++) {
                grid[r][c] = 0;
                if (c + 1 < SIZE - 1)
                    grid[r][c + 1] = 0;
            }

        // Rooms in corners
        carveRoom(6, 6, 6, 6);
        carveRoom(SIZE - 12, 6, 6, 6);
        carveRoom(6, SIZE - 12, 6, 6);
        carveRoom(SIZE - 12, SIZE - 12, 6, 6);

        // Borders
        for (int r = 0; r < SIZE; r++) {
            grid[r][0] = 1;
            grid[r][SIZE - 1] = 1;
        }
        for (int c = 0; c < SIZE; c++) {
            grid[0][c] = 1;
            grid[SIZE - 1][c] = 1;
        }
    }

    private void carveRoom(int sr, int sc, int h, int w) {
        for (int r = sr; r < sr + h && r < SIZE - 1; r++)
            for (int c = sc; c < sc + w && c < SIZE - 1; c++)
                grid[r][c] = 0;
    }

    private void generateOpenFrontier() {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                grid[r][c] = 0;
    }

    private void generateProcedural(Long seed) {
        Random rnd = (seed == null) ? new Random() : new Random(seed);
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                grid[r][c] = (r == 0 || c == 0 || r == SIZE - 1 || c == SIZE - 1 || rnd.nextDouble() < 0.12) ? 1 : 0;

        int mid = SIZE / 2;
        for (int c = 1; c < SIZE - 1; c++)
            grid[mid][c] = 0;
    }

    /* ================= SPAWN LOGIC ================= */

    protected Position determinePlayerSpawn() {
        // Non-random arenas: fixed safe spawn
        if (!name.equalsIgnoreCase("Procedural")) {
            for (int r = SIZE / 2 - 1; r <= SIZE / 2 + 1; r++)
                for (int c = SIZE / 2 - 1; c <= SIZE / 2 + 1; c++)
                    if (grid[r][c] == 0)
                        return new Position(r, c);
        }

        // Procedural: randomized spawn
        Random rnd = (seed == null) ? new Random() : new Random(seed);
        for (int attempt = 0; attempt < 1000; attempt++) {
            int r = rnd.nextInt(SIZE);
            int c = rnd.nextInt(SIZE);
            if (grid[r][c] == 0)
                return new Position(r, c);
        }

        return new Position(SIZE / 2, SIZE / 2); // fallback
    }

    public Position getPlayerSpawn() {
        return playerSpawn;
    }

    public Position getRandomEmptyPosition(Random rnd) {
        for (int attempt = 0; attempt < 1000; attempt++) {
            int r = rnd.nextInt(SIZE);
            int c = rnd.nextInt(SIZE);
            if (grid[r][c] == 0 && (playerSpawn == null || (r != playerSpawn.row && c != playerSpawn.col)))
                return new Position(r, c);
        }
        return new Position(SIZE / 2, SIZE / 2);
    }

    /* ================= UTILS ================= */

    public boolean inBounds(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    public boolean isWall(int row, int col) {
        return inBounds(row, col) && grid[row][col] == 1;
    }

    public boolean isEmpty(int row, int col) {
        return inBounds(row, col) && grid[row][col] == 0;
    }

    public boolean isJetwall(int row, int col) {
        return inBounds(row, col) && grid[row][col] == 2;
    }

    public void setPlayerPosition(Position pos) {
        this.playerPosition = new Position(pos.row, pos.col);
    }

    public Position getPlayerPosition() {
        return playerPosition != null ? new Position(playerPosition.row, playerPosition.col) : playerSpawn;
    }

    public void placeJetwall(int row, int col) {
        if (inBounds(row, col) && grid[row][col] == 0) {
            grid[row][col] = 2;
        }
    }

    public int[][] getGridCopy() {
        int[][] copy = new int[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            System.arraycopy(grid[r], 0, copy[r], 0, SIZE);
        }
        return copy;
    }
}
