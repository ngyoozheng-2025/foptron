import java.util.Random;

public abstract class Arena implements ArenaView {

    public static final int SIZE = 40;
    private final int[][] grid;
    private final boolean openArena;
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
        } else { // Procedural
            openArena = false;
            generateProcedural(seed);
        }
    }

    // ----------------- GENERATORS -----------------

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
        // Fill with walls
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                grid[r][c] = 1;

        // Horizontal corridors every 6 rows, 2 tiles wide
        for (int r = 3; r < SIZE - 3; r += 6)
            for (int c = 1; c < SIZE - 1; c++) {
                grid[r][c] = 0;
                if (r + 1 < SIZE - 1)
                    grid[r + 1][c] = 0;
            }

        // Vertical corridors every 8 columns, 2 tiles wide
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

        // Ensure outer border walls
        for (int r = 0; r < SIZE; r++) {
            grid[r][0] = 1;
            grid[r][SIZE - 1] = 1;
        }
        for (int c = 0; c < SIZE; c++) {
            grid[0][c] = 1;
            grid[SIZE - 1][c] = 1;
        }
    }

    private void carveRoom(int startR, int startC, int h, int w) {
        for (int r = startR; r < startR + h && r < SIZE - 1; r++)
            for (int c = startC; c < startC + w && c < SIZE - 1; c++)
                grid[r][c] = 0;
    }

    private void generateOpenFrontier() {
        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                grid[r][c] = 0;

        for (int r = 5; r < SIZE - 5; r += 6)
            for (int c = 5; c < SIZE - 5; c++)
                grid[r][c] = 1;

        for (int c = 5; c < SIZE - 5; c += 7)
            for (int r = 8; r < SIZE - 8; r++)
                grid[r][c] = 1;
    }

    private void generateProcedural(Long seed) {
        Random rnd = (seed == null) ? new Random() : new Random(seed);

        for (int r = 0; r < SIZE; r++)
            for (int c = 0; c < SIZE; c++)
                grid[r][c] = (r == 0 || c == 0 || r == SIZE - 1 || c == SIZE - 1 || rnd.nextDouble() < 0.12) ? 1 : 0;

        // clear middle row
        int mid = SIZE / 2;
        for (int c = 1; c < SIZE - 1; c++)
            grid[mid][c] = 0;
    }

    // ----------------- ARENA VIEW -----------------

    @Override
    public boolean isWall(int row, int col) {
        if (!inBounds(row, col))
            return !openArena;
        return grid[row][col] == 1;
    }

    @Override
    public boolean isEmpty(int row, int col) {
        return inBounds(row, col) && grid[row][col] == 0;
    }

<<<<<<< HEAD
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
=======
    // ----------------- UTILS -----------------

>>>>>>> 9cd303b (Arena Layout Fix)
    public boolean inBounds(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    public int getCell(int row, int col) {
        return inBounds(row, col) ? grid[row][col] : 1;
    }

    public void printAscii() {
        System.out.println("Arena: " + name + " (Open: " + openArena + ")");
        for (int r = 0; r < SIZE; r++) {
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < SIZE; c++)
                sb.append(grid[r][c] == 1 ? '#' : '.');
            System.out.println(sb);
        }
    }
<<<<<<< HEAD

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
=======
>>>>>>> 9cd303b (Arena Layout Fix)
}
