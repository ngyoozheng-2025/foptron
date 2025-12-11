import java.util.Random;

public class AIController {
    private static final Random rand = new Random();

    // ===== Random Movement (Koura) =====
    public static Direction randomMove(Enemy enemy, ArenaView arena) {
        Direction[] dirs = Direction.values();

        for (int i = 0; i < 4; i++) {
            Direction d = dirs[rand.nextInt(dirs.length)];
            Position next = nextPos(enemy.getPosition(), d);

            if (arena.isEmpty(next.row, next.col))
                return d;
        }
        return enemy.direction; // Keep current direction if stuck
    }

    // ===== Simple Pattern (Sark) =====
    public static Direction patternMove(Enemy enemy, ArenaView arena) {
        Direction[] order = {
            Direction.RIGHT,
            Direction.DOWN,
            Direction.LEFT,
            Direction.UP
        };

        for (Direction d : order) {
            Position next = nextPos(enemy.getPosition(), d);
            if (arena.isEmpty(next.row, next.col))
                return d;
        }
        return enemy.direction;
    }

    // ===== Direct Chase (Rinzler) =====
    public static Direction chase(Enemy enemy, ArenaView arena) {
        Position player = arena.getPlayerPosition();
        Position pos = enemy.getPosition();

        if (Math.abs(player.row - pos.row) > Math.abs(player.col - pos.col)) {
            return player.row < pos.row ? Direction.UP : Direction.DOWN;
        } else {
            return player.col < pos.col ? Direction.LEFT : Direction.RIGHT;
        }
    }

    // ===== Smart Chase + Avoid Jetwalls (Clu) =====
    public static Direction chaseAvoid(Enemy enemy, ArenaView arena) {
        Direction primary = chase(enemy, arena);

        Direction[] options = {
            primary,
            turnLeft(primary),
            turnRight(primary),
            opposite(primary)
        };

        for (Direction d : options) {
            Position next = nextPos(enemy.getPosition(), d);
            if (!arena.isWall(next.row, next.col) && !arena.isJetwall(next.row, next.col))
                return d;
        }
        return primary;
    }

    // ===== Helpers =====
    private static Position nextPos(Position p, Direction d) {
        return switch (d) {
            case UP -> new Position(p.row - 1, p.col);
            case DOWN -> new Position(p.row + 1, p.col);
            case LEFT -> new Position(p.row, p.col - 1);
            case RIGHT -> new Position(p.row, p.col + 1);
        };
    }

    private static Direction turnLeft(Direction d) {
        return switch (d) {
            case UP -> Direction.LEFT;
            case LEFT -> Direction.DOWN;
            case DOWN -> Direction.RIGHT;
            case RIGHT -> Direction.UP;
        };
    }

    private static Direction turnRight(Direction d) {
        return switch (d) {
            case UP -> Direction.RIGHT;
            case RIGHT -> Direction.DOWN;
            case DOWN -> Direction.LEFT;
            case LEFT -> Direction.UP;
        };
    }

    private static Direction opposite(Direction d) {
        return switch (d) {
            case UP -> Direction.DOWN;
            case DOWN -> Direction.UP;
            case LEFT -> Direction.RIGHT;
            case RIGHT -> Direction.LEFT;
        };
    }
}
