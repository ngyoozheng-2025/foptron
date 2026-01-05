import java.util.ArrayList;
import src.Characters.Characters;

public class Disc {
    private Position position;
    private Direction direction;

    private int maxRange = 3;
    private int distanceTravelled = 0;
    private boolean active = true;

    private Enemy owner;

    // Smooth animation across tiles
    private double smoothRow; // Sub-tile row position
    private double smoothCol; // Sub-tile col position
    private static final double MOVE_SPEED = 1.0 / 8.0; // Move one tile over 8 frames

    // Collision glow effect
    private int collisionGlowTimer = 0;
    private static final int COLLISION_GLOW_DURATION = 20; // Glow for 20 frames (~0.33 seconds at 60fps)
    private Position lastCollisionPos;

    public Disc(Position startPos, Direction direction, Enemy owner) {
        this.position = new Position(startPos.row, startPos.col);
        this.smoothRow = startPos.row;
        this.smoothCol = startPos.col;
        this.direction = direction;
        this.owner = owner;
    }

    public void update(ArenaView arena, Characters player, ArrayList<Enemy> enemies) {
        // Always update collision glow timer (even for inactive discs)
        if (collisionGlowTimer > 0) {
            collisionGlowTimer--;
        }

        if (!active)
            return;

        // Move disc smoothly across tiles
        switch (direction) {
            case UP -> smoothRow -= MOVE_SPEED;
            case DOWN -> smoothRow += MOVE_SPEED;
            case LEFT -> smoothCol -= MOVE_SPEED;
            case RIGHT -> smoothCol += MOVE_SPEED;
        }

        // Update grid position when we've crossed into a new tile
        int newRow = (int) Math.round(smoothRow);
        int newCol = (int) Math.round(smoothCol);

        // Check if we've moved to a new tile
        boolean movedToNewTile = (newRow != position.row || newCol != position.col);

        if (movedToNewTile) {
            position.row = newRow;
            position.col = newCol;
            distanceTravelled++;
        }

        // Check collisions at current position (both when moving to new tile and at
        // current position)
        // This ensures we catch collisions even if disc starts at collision position

        // 1. Wall or jetwall collision
        if (arena.isWall(position.row, position.col) ||
                arena.isJetwall(position.row, position.col)) {
            triggerCollisionGlow();
            active = false;
            return;
        }

        // 2. Collision with PLAYER
        src.Characters.Position charPos = player.getPosition();
        // Convert Characters.Position (double) to Position (int) for comparison
        int playerRow = (int) charPos.row;
        int playerCol = (int) charPos.col;
        if (position.row == playerRow && position.col == playerCol) {
            triggerCollisionGlow();
            active = false;
            player.hitByDisc((Object) owner);
            return;
        }

        // 3. Collision with ENEMIES
        for (Enemy e : enemies) {
            if (e == owner)
                continue; // cannot hit itself
            Position ep = e.getPosition();
            if (position.row == ep.row && position.col == ep.col) {
                triggerCollisionGlow();
                active = false;
                e.hitByDisc();
                return;
            }
        }

        // 4. Out of range (only check when we've moved to a new tile to avoid premature
        // deactivation)
        if (movedToNewTile && distanceTravelled >= maxRange) {
            active = false;
        }
    }

    /**
     * Trigger collision glow effect
     */
    private void triggerCollisionGlow() {
        collisionGlowTimer = COLLISION_GLOW_DURATION;
        lastCollisionPos = new Position(position.row, position.col);
    }

    public Position getPosition() {
        return position;
    }

    /**
     * Get smooth sub-tile position for rendering
     */
    public double getSmoothRow() {
        return smoothRow;
    }

    public double getSmoothCol() {
        return smoothCol;
    }

    /**
     * Check if collision glow is active
     */
    public boolean hasCollisionGlow() {
        return collisionGlowTimer > 0;
    }

    /**
     * Get collision glow intensity (0.0 to 1.0)
     */
    public float getCollisionGlowIntensity() {
        if (collisionGlowTimer <= 0)
            return 0.0f;
        return (float) collisionGlowTimer / COLLISION_GLOW_DURATION;
    }

    /**
     * Get last collision position for glow rendering
     */
    public Position getLastCollisionPos() {
        return lastCollisionPos != null ? lastCollisionPos : position;
    }

    public boolean isActive() {
        return active;
    }

    public Enemy getOwner() {
        return owner;
    }
}
