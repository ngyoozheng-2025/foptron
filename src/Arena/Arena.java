package src.Arena;

import src.Enemy.Position;
import java.util.Random;

public class Arena implements ArenaView {

    public static final int SIZE = 40;

    public enum ArenaType {
        CLASSICGRID,
        NEONMAZE,
        OPENFRONTIER,
        PROCEDURAL
    }

    private final int[][] grid;
    private final boolean openArena;
    private final ArenaType type;
    private final Long seed;
    private final String name;
    private String difficulty;

    // Player/enemy positions
    private Position playerSpawn;
    private Position playerPosition;

    public Arena(String name) {
        this(name, null);
    }

    public Arena(String name, Long seed) {
        this.name = name == null ? "Procedural" : name;
        this.seed = seed;
        this.grid = new int[SIZE][SIZE];

        // decide type
        this.type = decideType(this.name);

        // generate according to chosen type
        switch (this.type) {
            case CLASSICGRID -> {
                openArena = false;
                generateClassicGrid();
            }
            case NEONMAZE -> {
                openArena = false;
                generateNeonMaze();
            }
            case OPENFRONTIER -> {
                openArena = true;
                generateOpenFrontier();
            }
            case PROCEDURAL -> {
                openArena = false;
                generateProcedural(seed);
            }
            default -> {
                openArena = false;
                generateProcedural(seed);
            }
        }

        // Determine player spawn and set current playerPosition
        playerSpawn = determinePlayerSpawn();
        playerPosition = new Position(playerSpawn.row, playerSpawn.col);
    }

    private ArenaType decideType(String rawName) {
        if (rawName == null)
            return ArenaType.PROCEDURAL;
        String n = rawName.trim().toUpperCase();
        return switch (n) {
            case "CLASSICGRID", "ARENA 1" -> ArenaType.CLASSICGRID;
            case "NEONMAZE", "ARENA 2" -> ArenaType.NEONMAZE;
            case "OPENFRONTIER", "ARENA 3" -> ArenaType.OPENFRONTIER;
            case "RANDOM", "PROCEDURAL" -> ArenaType.PROCEDURAL;
            default -> {
                // fallback by checking if contains keywords
                if (n.contains("CLASSIC"))
                    yield ArenaType.CLASSICGRID;
                if (n.contains("NEON"))
                    yield ArenaType.NEONMAZE;
                if (n.contains("OPEN"))
                    yield ArenaType.OPENFRONTIER;
                yield ArenaType.PROCEDURAL;
            }
        };
    }

    /* ================= ARENA GENERATORS ================= */

    private void generateClassicGrid() {
        // Outer boundary walls
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c] = (r == 0 || c == 0 || r == SIZE - 1 || c == SIZE - 1) ? 1 : 0;
            }
        }

        // Add a couple of interior walls to create corridors
        for (int i = 5; i < SIZE - 5; i += 10) {
            int r1 = 10;
            int r2 = SIZE - 11; // 29 for SIZE=40
            if (r1 >= 1 && r1 < SIZE - 1 && i >= 1 && i < SIZE - 1)
                grid[r1][i] = 1;
            if (r2 >= 1 && r2 < SIZE - 1 && i >= 1 && i < SIZE - 1)
                grid[r2][i] = 1;
        }
    }

    private void generateNeonMaze() {
        // start fully walled
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                grid[r][c] = 1;

        // carve horizontal corridors
        for (int r = 3; r < SIZE - 3; r += 6) {
            for (int c = 1; c < SIZE - 1; c++) {
                grid[r][c] = 0;
                if (r + 1 < SIZE - 1)
                    grid[r + 1][c] = 0;
            }
        }

        // carve vertical corridors
        for (int c = 4; c < SIZE - 4; c += 8) {
            for (int r = 1; r < SIZE - 1; r++) {
                grid[r][c] = 0;
                if (c + 1 < SIZE - 1)
                    grid[r][c + 1] = 0;
            }
        }

        // carve rooms in corners
        carveRoom(6, 6, 6, 6);
        carveRoom(SIZE - 12, 6, 6, 6);
        carveRoom(6, SIZE - 12, 6, 6);
        carveRoom(SIZE - 12, SIZE - 12, 6, 6);

        // ensure borders are walls
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
        for (int r = Math.max(1, sr); r < Math.min(SIZE - 1, sr + h); r++)
            for (int c = Math.max(1, sc); c < Math.min(SIZE - 1, sc + w); c++)
                grid[r][c] = 0;
    }

    private void generateOpenFrontier() {
        // mostly empty space (no interior walls)
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                grid[r][c] = 0;

        // border walls to keep arena bounded
        for (int r = 0; r < SIZE; r++) {
            grid[r][0] = 1;
            grid[r][SIZE - 1] = 1;
        }
        for (int c = 0; c < SIZE; c++) {
            grid[0][c] = 1;
            grid[SIZE - 1][c] = 1;
        }
    }

    private void generateProcedural(Long seed) {
        Random rnd = (seed == null) ? new Random() : new Random(seed);
        // border walls + some random interior walls
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                boolean border = (r == 0 || c == 0 || r == SIZE - 1 || c == SIZE - 1);
                grid[r][c] = border || rnd.nextDouble() < 0.12 ? 1 : 0;
            }
        }

        // keep a center corridor clear
        int mid = SIZE / 2;
        for (int c = 1; c < SIZE - 1; c++)
            grid[mid][c] = 0;
    }

    /* ================= SPAWN LOGIC ================= */

    protected Position determinePlayerSpawn() {
        // Non-procedural arenas: pick safe center area
        if (type != ArenaType.PROCEDURAL) {
            for (int r = SIZE / 2 - 1; r <= SIZE / 2 + 1; r++) {
                for (int c = SIZE / 2 - 1; c <= SIZE / 2 + 1; c++) {
                    if (inBounds(r, c) && grid[r][c] == 0)
                        return new Position(r, c);
                }
            }
        }

        // Procedural: try random empty tiles (seeded if provided)
        Random rnd = (seed == null) ? new Random() : new Random(seed);
        for (int attempt = 0; attempt < 1000; attempt++) {
            int r = rnd.nextInt(SIZE);
            int c = rnd.nextInt(SIZE);
            if (grid[r][c] == 0)
                return new Position(r, c);
        }

        // fallback center
        return new Position(SIZE / 2, SIZE / 2);
    }

    public Position getPlayerSpawn() {
        return playerSpawn;
    }

    public Position getPlayerPosition() {
        return playerPosition != null ? new Position(playerPosition.row, playerPosition.col)
                : new Position(playerSpawn.row, playerSpawn.col);
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setPlayerPosition(Position pos) {
        if (pos == null)
            return;
        this.playerPosition = new Position(pos.row, pos.col);
    }

    public Position getRandomEmptyPosition(Random rnd) {
        for (int attempt = 0; attempt < 1000; attempt++) {
            int r = rnd.nextInt(SIZE);
            int c = rnd.nextInt(SIZE);
            if (grid[r][c] == 0 && (playerSpawn == null || !(r == playerSpawn.row && c == playerSpawn.col)))
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

    public void placeJetwall(int row, int col) {
        if (inBounds(row, col) && grid[row][col] == 0) {
            grid[row][col] = 2;
        }
    }

    public int[][] getGridCopy() {
        int[][] copy = new int[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++)
            System.arraycopy(grid[r], 0, copy[r], 0, SIZE);
        return copy;
    }

    /** returns machine-friendly type name */
    public String getTypeName() {
        return type.name();
    }

    /** returns enum type */
    public ArenaType getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
