import java.util.Random;

public class Arena implements ArenaView {
    public static final int SIZE = 40;
    private final int[][] grid; // SIZE x SIZE
    private final boolean openArena; // true if open-type (no boundaries)
    private Position playerSpawn; // integer row,col position
    private final String name;

    public Arena(String name) {
        this(name, null);
    }

    public Arena(String name, Long seed) {
        this.name = name;
        this.grid = new int[SIZE][SIZE];
        if (name.equalsIgnoreCase("ClassicGrid")) {
            openArena = false;
            generateClassicGrid();
        } else if (name.equalsIgnoreCase("NeonMaze")) {
            openArena = false;
            generateNeonMaze();
        } else if (name.equalsIgnoreCase("OpenFrontier")) {
            openArena = true;
            generateOpenFrontier();
        } else { // Procedural or fallback
            openArena = false;
            generateProcedural(seed);
        }
        // Ensure a valid player spawn exists
        if (playerSpawn == null)
            playerSpawn = findFirstEmptyOrCenter();
    }

    // --- Arena generators ---

    private void generateClassicGrid() {
        // Boundary walls around the 40x40 grid, interior empty
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (r == 0 || r == SIZE - 1 || c == 0 || c == SIZE - 1)
                    grid[r][c] = 1;
                else
                    grid[r][c] = 0;
            }
        }
        // place a few symmetric obstacles for variety
        for (int i = 5; i < 35; i += 10) {
            grid[10][i] = 1;
            grid[29][i] = 1;
        }
        playerSpawn = new Position(SIZE / 2, SIZE / 4); // left-middle quadrant
    }

    private void generateNeonMaze() {
        // Create a simple maze using a "rooms + corridors" pattern
        // Start with all walls, then carve corridors
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++)
                grid[r][c] = 1;
        }

        // Carve a grid of corridors every 4 tiles
        for (int r = 1; r < SIZE - 1; r++) {
            for (int c = 1; c < SIZE - 1; c++) {
                if (r % 4 == 2 || c % 4 == 2)
                    grid[r][c] = 0;
            }
        }

        // Add some openings and rooms
        for (int r = 6; r < 10; r++)
            for (int c = 6; c < 10; c++)
                grid[r][c] = 0;
        for (int r = 25; r < 30; r++)
            for (int c = 20; c < 28; c++)
                grid[r][c] = 0;

        playerSpawn = new Position(2, 2); // near top-left corridor
    }

    private void generateOpenFrontier() {
        // No boundary walls. Mostly empty with a few scattered obstacles.
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++)
                grid[r][c] = 0;
        }
        // scatter obstacles in pseudo-rings
        for (int r = 5; r < SIZE - 5; r += 6) {
            for (int c = 5; c < SIZE - 5; c++)
                grid[r][c] = 1;
        }
        for (int c = 5; c < SIZE - 5; c += 7) {
            for (int r = 8; r < SIZE - 8; r++)
                grid[r][c] = 1;
        }
        playerSpawn = new Position(SIZE / 2, SIZE / 2); // central spawn
    }

    private void generateProcedural(Long seed) {
        Random rnd = (seed == null) ? new Random() : new Random(seed);
        // Start with boundary walls (procedural arena tends to be closed), plus random
        // interior obstacles
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (r == 0 || r == SIZE - 1 || c == 0 || c == SIZE - 1)
                    grid[r][c] = 1;
                else
                    grid[r][c] = (rnd.nextDouble() < 0.12) ? 1 : 0; // 12% obstacles
            }
        }
        // Ensure connectivity by clearing a simple path from left to right
        int mid = SIZE / 2;
        for (int c = 1; c < SIZE - 1; c++)
            grid[mid][c] = 0;

        // Add a few larger blocks
        for (int b = 0; b < 6; b++) {
            int br = 2 + rnd.nextInt(SIZE - 6);
            int bc = 2 + rnd.nextInt(SIZE - 6);
            int bw = 2 + rnd.nextInt(4);
            int bh = 2 + rnd.nextInt(4);
            for (int r = br; r < Math.min(SIZE - 1, br + bh); r++) {
                for (int c = bc; c < Math.min(SIZE - 1, bc + bw); c++)
                    grid[r][c] = 1;
            }
        }

        playerSpawn = new Position(SIZE / 2, 2 + rnd.nextInt(6));
    }

    // --- Public API / ArenaView implementation ---

    @Override
    public boolean isWall(int row, int col) {
        if (!inBounds(row, col))
            return openArena ? false : true; // out-of-bounds treated as wall for closed arenas
        return grid[row][col] == 1;
    }

    @Override
    public boolean isJetwall(int row, int col) {
        if (!inBounds(row, col))
            return false;
        return grid[row][col] == 2;
    }

    @Override
    public boolean isEmpty(int row, int col) {
        if (!inBounds(row, col))
            return false;
        return grid[row][col] == 0;
    }

   // Current player position (updated by GameEngine)
    private Position currentPlayerPos = null;
    
    /**
     * Set the current player position for AI tracking
     */
    public void setPlayerPosition(Position pos) {
        this.currentPlayerPos = pos;
    }
    
    @Override
    public Position getPlayerPosition() {
        // Return current player position if available, otherwise spawn point
        return currentPlayerPos != null ? currentPlayerPos : playerSpawn;
    }
    
    /**
     * Get the spawn position for new players
     */
    public Position getPlayerSpawn() {
        return playerSpawn;
    }
    // Add a jetwall at integer grid position
    public void placeJetwall(int row, int col) {
        if (!inBounds(row, col))
            return;
        // do not overwrite permanent walls
        if (grid[row][col] == 1)
            return;
        grid[row][col] = 2;
    }

    // Remove a jetwall (if needed)
    public void clearJetwall(int row, int col) {
        if (!inBounds(row, col))
            return;
        if (grid[row][col] == 2)
            grid[row][col] = 0;
    }

    // Find a random empty spawn suitable for enemies/players
    public Position getRandomEmptySpawn(long seed) {
        Random rnd = new Random(seed);
        for (int attempts = 0; attempts < 1000; attempts++) {
            int r = rnd.nextInt(SIZE);
            int c = rnd.nextInt(SIZE);
            if (isEmpty(r, c))
                return new Position(r, c);
        }
        return findFirstEmptyOrCenter();
    }

    // Utility
    public boolean inBounds(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    private Position findFirstEmptyOrCenter() {
        // prefer center, then search outward
        int cr = SIZE / 2, cc = SIZE / 2;
        if (isEmpty(cr, cc))
            return new Position(cr, cc);
        for (int radius = 1; radius < SIZE / 2; radius++) {
            for (int r = Math.max(0, cr - radius); r <= Math.min(SIZE - 1, cr + radius); r++) {
                for (int c = Math.max(0, cc - radius); c <= Math.min(SIZE - 1, cc + radius); c++) {
                    if (isEmpty(r, c))
                        return new Position(r, c);
                }
            }
        }
        return new Position(cr, cc); // fallback
    }

    // Simple ASCII visualization for debugging
    public void printAscii() {
        System.out.println("Arena: " + name + " (Open: " + openArena + ")");
        for (int r = 0; r < SIZE; r++) {
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < SIZE; c++) {
                if (playerSpawn.row == r && playerSpawn.col == c) {
                    sb.append('P'); // player spawn
                } else if (grid[r][c] == 1)
                    sb.append('#'); // wall
                else if (grid[r][c] == 2)
                    sb.append('J'); // jetwall
                else
                    sb.append('.'); // empty
            }
            System.out.println(sb.toString());
        }
    }

    public int getCell(int row, int col) {
        if (!inBounds(row, col))
            return 1; // out-of-bounds = wall
        return grid[row][col];
    }

    //get grid as rendering 
    //copy of the grid where 0=empty, 1=wall, 2=jetwall
    public int[][] getGridCopy() {
        int[][] copy = new int[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            System.arraycopy(grid[r], 0, copy[r], 0, SIZE);
        }
        return copy;
    }
}
