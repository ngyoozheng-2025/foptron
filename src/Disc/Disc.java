package src.Disc;

import java.util.ArrayList;
import src.Arena.ArenaView;
import src.Arena.Arena;
import src.Characters.Characters;
import src.Enemy.Direction;
import src.Enemy.Enemy;
import src.Enemy.Position;

public class Disc {
    private Position position;
    private Direction direction;

    private int maxRange = 3;
    private int distanceTravelled = 0;
    private boolean active = true;

    private Enemy owner;

    // New flags
    private boolean hitEnemy = false; // true if disc hit an enemy (consumed)
    private boolean collected = false; // true if player reclaimed disc (consumed)

    // Smooth animation across tiles
    private double smoothRow; // Sub-tile row position
    private double smoothCol; // Sub-tile col position
    private static final double MOVE_SPEED = 1.0 / 8.0; // Move one tile over 8 frames

    // Collision glow effect
    private int collisionGlowTimer = 0;
    private static final int COLLISION_GLOW_DURATION = 20; // Glow for 20 frames (~0.33s at 60fps)
    private Position lastCollisionPos;

    public Disc(Position startPos, Direction direction, Enemy owner) {
        this.position = new Position(startPos.row, startPos.col);
        this.smoothRow = startPos.row;
        this.smoothCol = startPos.col;
        this.direction = direction;
        this.owner = owner;
    }

    /**
     * Main update called each frame by GameEngine.
     * Note: this method no longer auto-collects discs for the thrower.
     * Player reclaim is handled by GameEngine checking resting/inactive discs.
     */
    public void update(Arena arena, Characters player, ArrayList<Enemy> enemies) {
        // Decrement glow timer regardless
        if (collisionGlowTimer > 0) {
            collisionGlowTimer--;
        }

        // If disc already consumed (hit enemy or collected) we still let glow run down,
        // but don't move further.
        if (!active)
            return;

        // Move disc smoothly across tiles
        switch (direction) {
            case UP -> smoothRow -= MOVE_SPEED;
            case DOWN -> smoothRow += MOVE_SPEED;
            case LEFT -> smoothCol -= MOVE_SPEED;
            case RIGHT -> smoothCol += MOVE_SPEED;
        }

        // Convert smooth coords to grid coords
        int newRow = (int) Math.round(smoothRow);
        int newCol = (int) Math.round(smoothCol);

        boolean movedToNewTile = (newRow != position.row || newCol != position.col);

        if (movedToNewTile) {
            position.row = newRow;
            position.col = newCol;
            distanceTravelled++;
        }

        // 1) Wall or jetwall collision -> stop and rest (not consumed)
        if (arena.isWall(position.row, position.col) || arena.isJetwall(position.row, position.col)) {
            triggerCollisionGlow();
            // mark inactive (resting). Do NOT mark hitEnemy.
            active = false;
            return;
        }

        // 2) Collision with PLAYER - only if thrown by enemy (owner != null)
        if (owner != null) {
            src.Characters.Position charPos = player.getPosition();
            int playerRow = (int) charPos.row;
            int playerCol = (int) charPos.col;
            if (position.row == playerRow && position.col == playerCol) {
                triggerCollisionGlow();
                active = false;
                // enemy disc damages player
                player.hitByDisc((Object) owner);
                return;
            }
        }
        // NOTE: if owner == null (player disc), we DO NOT collide with player here.
        // Player reclaim will be handled externally when disc is resting (active ==
        // false).

        // 3) Collision with ENEMIES
        for (Enemy e : enemies) {
            if (e == owner)
                continue; // cannot hit itself
            Position ep = e.getPosition();
            if (position.row == ep.row && position.col == ep.col) {
                triggerCollisionGlow();
                active = false;
                hitEnemy = true; // mark as consumed by enemy
                e.hitByDisc();
                return;
            }
        }

        // 4) Out of range -> stop and rest (inactive)
        if (movedToNewTile && distanceTravelled >= maxRange) {
            active = false;
        }
    }

    /** Trigger collision glow and record last collision tile (for visual). */
    private void triggerCollisionGlow() {
        collisionGlowTimer = COLLISION_GLOW_DURATION;
        lastCollisionPos = new Position(position.row, position.col);
    }

    /** Called by GameEngine when player picks up this resting disc. */
    public void collect() {
        if (!hitEnemy && !collected) {
            collected = true;
            active = false; // ensure it's no longer moving
            // keep collision glow if you want visual feedback (optional)
            // collisionGlowTimer = COLLISION_GLOW_DURATION;
        }
    }

    // --- Getters ---

    public Position getPosition() {
        return position;
    }

    public double getSmoothRow() {
        return smoothRow;
    }

    public double getSmoothCol() {
        return smoothCol;
    }

    public boolean hasCollisionGlow() {
        return collisionGlowTimer > 0;
    }

    public float getCollisionGlowIntensity() {
        if (collisionGlowTimer <= 0)
            return 0.0f;
        return (float) collisionGlowTimer / COLLISION_GLOW_DURATION;
    }

    public Position getLastCollisionPos() {
        return lastCollisionPos != null ? lastCollisionPos : position;
    }

    public boolean isActive() {
        return active;
    } // moving == active

    public Enemy getOwner() {
        return owner;
    }

    public boolean hasHitEnemy() {
        return hitEnemy;
    } // consumed by enemy

    public boolean isCollected() {
        return collected;
    } // reclaimed by player
}
