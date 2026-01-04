import java.util.ArrayList;
import src.Characters.Characters;

public class Disc {
    private Position position;
    private Direction direction;

    private int maxRange = 3;
    private int distanceTravelled = 0;
    private boolean active = true;

    private Enemy owner; 

    public Disc(Position startPos, Direction direction, Enemy owner) {
        this.position = new Position(startPos.row, startPos.col);
        this.direction = direction;
        this.owner = owner;
    }

    public void update(ArenaView arena, Characters player, ArrayList<Enemy> enemies) {
    if (!active) return;

    // Move disc one tile
    switch (direction) {
        case UP -> position.row--;
        case DOWN -> position.row++;
        case LEFT -> position.col--;
        case RIGHT -> position.col++;
    }

    distanceTravelled++;

    // 1. Wall or jetwall collision
    if (arena.isWall(position.row, position.col) ||
        arena.isJetwall(position.row, position.col)) {
        active = false;
        return;
    }

    // 2. Collision with PLAYER
    src.Characters.Position charPos = player.getPosition();
    // Convert Characters.Position (double) to Position (int) for comparison
    int playerRow = (int) charPos.row;
    int playerCol = (int) charPos.col;
    if (position.row == playerRow && position.col == playerCol) {
        active = false;
        player.hitByDisc((Object)owner);
        return;
    }

    // 3. Collision with ENEMIES
    for (Enemy e : enemies) {
        if (e == owner) continue; // cannot hit itself
        Position ep = e.getPosition();
        if (position.row == ep.row && position.col == ep.col) {
            active = false;
            e.hitByDisc();
            return;
        }
    }

    // 4. Out of range
    if (distanceTravelled >= maxRange) {
        active = false;
    }
}



    public Position getPosition() { return position; }
    public boolean isActive() { return active; }
    public Enemy getOwner() { return owner; }
}
