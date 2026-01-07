package src.AI;

import java.util.Random;
import src.Arena.ArenaView;
import src.Enemy.Direction;
import src.Enemy.Enemy;
import src.Enemy.Position;

public class AIController {
    private static final Random rand = new Random();

    // ===== Main difficulty-based AI router =====
    public static Direction decideMove(Enemy enemy, ArenaView arena) {
        String difficulty = getDifficultyFromName(enemy.getName());

        return switch (difficulty) {
            case "Easy" -> easyBehavior(enemy, arena);
            case "Medium" -> mediumBehavior(enemy, arena);
            case "Hard" -> hardBehavior(enemy, arena);
            case "Impossible" -> impossibleBehavior(enemy, arena);
            default -> randomMove(enemy, arena); // Fallback
        };
    }

    // ===== Get difficulty from enemy name (based on enemies.txt) =====
    private static String getDifficultyFromName(String name) {
        return switch (name.toLowerCase()) {
            case "koura" -> "Easy";
            case "sark" -> "Medium";
            case "rinzler" -> "Hard";
            case "clu" -> "Impossible";
            default -> "Easy";
        };
    }

    // ===== Easy: Predictable Random Movement (Koura) =====
    public static Direction randomMove(Enemy enemy, ArenaView arena) {
        Direction[] dirs = Direction.values();

        for (int i = 0; i < 4; i++) {
            Direction d = dirs[rand.nextInt(dirs.length)];
            Position next = nextPos(enemy.getPosition(), d);

            if (arena.isEmpty(next.row, next.col))
                return d;
        }
        return enemy.getDirection(); // Keep current direction if stuck
    }

    private static Direction easyBehavior(Enemy enemy, ArenaView arena) {
        // Simple predictable random movement
        return randomMove(enemy, arena);
    }

    // ===== Medium: Simple Pattern Movement (Sark) =====
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
        return enemy.getDirection();
    }

    private static Direction mediumBehavior(Enemy enemy, ArenaView arena) {
        // Predictable pattern movement with occasional chase
        if (rand.nextDouble() < 0.3) {
            // 30% chance to try basic chase
            Direction chaseDir = basicChase(enemy, arena);
            Position next = nextPos(enemy.getPosition(), chaseDir);
            if (arena.isEmpty(next.row, next.col))
                return chaseDir;
        }
        return patternMove(enemy, arena);
    }

    // ===== Hard: Anticipate Player Direction + Flanking (Rinzler) =====
    public static Direction chase(Enemy enemy, ArenaView arena) {
        Position player = arena.getPlayerPosition();
        Position pos = enemy.getPosition();

        if (Math.abs(player.row - pos.row) > Math.abs(player.col - pos.col)) {
            return player.row < pos.row ? Direction.UP : Direction.DOWN;
        } else {
            return player.col < pos.col ? Direction.LEFT : Direction.RIGHT;
        }
    }

    private static Direction hardBehavior(Enemy enemy, ArenaView arena) {
        Position player = arena.getPlayerPosition();
        Position pos = enemy.getPosition();

        // Anticipate player movement by predicting where they'll be
        Position predictedPlayer = anticipatePlayerPosition(player, pos);

        // Try to intercept predicted position
        Direction interceptDir = getDirectionToTarget(pos, predictedPlayer);
        Position next = nextPos(pos, interceptDir);

        if (arena.isEmpty(next.row, next.col) && !arena.isJetwall(next.row, next.col)) {
            return interceptDir;
        }

        // If interception blocked, try flanking
        Direction flankDir = attemptFlank(enemy, arena, player);
        if (flankDir != null) {
            return flankDir;
        }

        // Fallback to direct chase with obstacle avoidance
        return chaseWithAvoidance(enemy, arena);
    }

    // ===== Impossible: Advanced Strategies + Anticipation + Flanking + Team
    // Coordination (Clu) =====
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

    private static Direction impossibleBehavior(Enemy enemy, ArenaView arena) {
        Position player = arena.getPlayerPosition();
        Position pos = enemy.getPosition();

        // Advanced anticipation: predict multiple steps ahead
        Position predictedPlayer = anticipatePlayerPositionAdvanced(player, pos);

        // Try strategic positioning (cut off escape routes)
        Direction strategicDir = strategicPositioning(enemy, arena, predictedPlayer);
        if (strategicDir != null) {
            Position next = nextPos(pos, strategicDir);
            if (arena.isEmpty(next.row, next.col) && !arena.isJetwall(next.row, next.col)) {
                return strategicDir;
            }
        }

        // Attempt flanking from multiple angles
        Direction flankDir = attemptAdvancedFlank(enemy, arena, player);
        if (flankDir != null) {
            return flankDir;
        }

        // Advanced chase with obstacle avoidance and jetwall awareness
        return advancedChaseAvoid(enemy, arena);
    }

    // ===== Helper Methods for Advanced Behaviors =====

    private static Direction basicChase(Enemy enemy, ArenaView arena) {
        Position player = arena.getPlayerPosition();
        Position pos = enemy.getPosition();

        if (Math.abs(player.row - pos.row) > Math.abs(player.col - pos.col)) {
            return player.row < pos.row ? Direction.UP : Direction.DOWN;
        } else {
            return player.col < pos.col ? Direction.LEFT : Direction.RIGHT;
        }
    }

    // Anticipate where player will be based on current position
    private static Position anticipatePlayerPosition(Position player, Position enemy) {
        // Simple prediction: assume player continues in direction of movement
        // Since we don't have player direction, predict based on relative position
        int rowDiff = player.row - enemy.row;
        int colDiff = player.col - enemy.col;

        // Predict player moves 1-2 steps toward or away from enemy
        int predictRow = player.row;
        int predictCol = player.col;

        if (Math.abs(rowDiff) > Math.abs(colDiff)) {
            // Player likely moving vertically
            predictRow += rowDiff > 0 ? 1 : -1;
        } else {
            // Player likely moving horizontally
            predictCol += colDiff > 0 ? 1 : -1;
        }

        return new Position(predictRow, predictCol);
    }

    // Advanced anticipation for Impossible difficulty
    private static Position anticipatePlayerPositionAdvanced(Position player, Position enemy) {
        // More sophisticated prediction: consider multiple possible moves
        int rowDiff = player.row - enemy.row;
        int colDiff = player.col - enemy.col;

        // Predict 2 steps ahead with strategic thinking
        int predictRow = player.row;
        int predictCol = player.col;

        // If player is far, predict they'll move closer; if close, predict escape
        int distance = Math.abs(rowDiff) + Math.abs(colDiff);

        if (distance > 5) {
            // Far away: predict player moves toward center or enemy
            if (Math.abs(rowDiff) > Math.abs(colDiff)) {
                predictRow += rowDiff > 0 ? -1 : 1; // Move toward enemy
            } else {
                predictCol += colDiff > 0 ? -1 : 1;
            }
        } else {
            // Close: predict player tries to escape
            if (Math.abs(rowDiff) > Math.abs(colDiff)) {
                predictRow += rowDiff > 0 ? 1 : -1; // Move away
            } else {
                predictCol += colDiff > 0 ? 1 : -1;
            }
        }

        return new Position(predictRow, predictCol);
    }

    // Get direction to move toward a target position
    private static Direction getDirectionToTarget(Position from, Position to) {
        int rowDiff = to.row - from.row;
        int colDiff = to.col - from.col;

        if (Math.abs(rowDiff) > Math.abs(colDiff)) {
            return rowDiff > 0 ? Direction.DOWN : Direction.UP;
        } else {
            return colDiff > 0 ? Direction.RIGHT : Direction.LEFT;
        }
    }

    // Attempt to flank player from the side
    private static Direction attemptFlank(Enemy enemy, ArenaView arena, Position player) {
        Position pos = enemy.getPosition();

        // Calculate relative position
        int rowDiff = player.row - pos.row;
        int colDiff = player.col - pos.col;

        // Try to approach from perpendicular direction
        Direction[] flankOptions;

        if (Math.abs(rowDiff) > Math.abs(colDiff)) {
            // Player is more vertical, flank horizontally
            flankOptions = new Direction[] {
                    colDiff > 0 ? Direction.RIGHT : Direction.LEFT,
                    colDiff > 0 ? Direction.LEFT : Direction.RIGHT
            };
        } else {
            // Player is more horizontal, flank vertically
            flankOptions = new Direction[] {
                    rowDiff > 0 ? Direction.DOWN : Direction.UP,
                    rowDiff > 0 ? Direction.UP : Direction.DOWN
            };
        }

        // Try flanking directions
        for (Direction d : flankOptions) {
            Position next = nextPos(pos, d);
            if (arena.isEmpty(next.row, next.col) && !arena.isJetwall(next.row, next.col)) {
                return d;
            }
        }

        return null; // Flanking not possible
    }

    // Advanced flanking for Impossible difficulty
    private static Direction attemptAdvancedFlank(Enemy enemy, ArenaView arena, Position player) {
        Position pos = enemy.getPosition();

        // Try multiple flanking strategies
        Direction[] strategies = {
                attemptFlank(enemy, arena, player), // Basic flank
                attemptDiagonalApproach(enemy, arena, player), // Diagonal approach
                attemptCutoff(enemy, arena, player) // Cut off escape route
        };

        for (Direction d : strategies) {
            if (d != null) {
                Position next = nextPos(pos, d);
                if (arena.isEmpty(next.row, next.col) && !arena.isJetwall(next.row, next.col)) {
                    return d;
                }
            }
        }

        return null;
    }

    // Diagonal approach strategy
    private static Direction attemptDiagonalApproach(Enemy enemy, ArenaView arena, Position player) {
        Position pos = enemy.getPosition();
        int rowDiff = player.row - pos.row;
        int colDiff = player.col - pos.col;

        // Try to move in a direction that combines both axes
        Direction[] diagonalOptions;

        if (rowDiff > 0 && colDiff > 0) {
            // Player is down-right, try to approach from up-left
            diagonalOptions = new Direction[] { Direction.UP, Direction.LEFT };
        } else if (rowDiff > 0 && colDiff < 0) {
            // Player is down-left, try to approach from up-right
            diagonalOptions = new Direction[] { Direction.UP, Direction.RIGHT };
        } else if (rowDiff < 0 && colDiff > 0) {
            // Player is up-right, try to approach from down-left
            diagonalOptions = new Direction[] { Direction.DOWN, Direction.LEFT };
        } else {
            // Player is up-left, try to approach from down-right
            diagonalOptions = new Direction[] { Direction.DOWN, Direction.RIGHT };
        }

        for (Direction d : diagonalOptions) {
            Position next = nextPos(pos, d);
            if (arena.isEmpty(next.row, next.col) && !arena.isJetwall(next.row, next.col)) {
                return d;
            }
        }

        return null;
    }

    // Cut off escape route
    private static Direction attemptCutoff(Enemy enemy, ArenaView arena, Position player) {
        Position pos = enemy.getPosition();

        // Try to position between player and likely escape direction
        // Check which directions player can escape to
        Direction[] escapeDirections = {
                Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT
        };

        for (Direction escapeDir : escapeDirections) {
            Position escapePos = nextPos(player, escapeDir);
            if (arena.isEmpty(escapePos.row, escapePos.col)) {
                // Player can escape this way, try to cut them off
                Direction cutoffDir = getDirectionToTarget(pos, escapePos);
                Position next = nextPos(pos, cutoffDir);
                if (arena.isEmpty(next.row, next.col) && !arena.isJetwall(next.row, next.col)) {
                    return cutoffDir;
                }
            }
        }

        return null;
    }

    // Strategic positioning to control the battlefield
    private static Direction strategicPositioning(Enemy enemy, ArenaView arena, Position target) {
        Position pos = enemy.getPosition();

        // Try to position optimally relative to target
        // Prefer positions that limit player's movement options

        Direction[] strategicOptions = {
                getDirectionToTarget(pos, target),
                turnLeft(getDirectionToTarget(pos, target)),
                turnRight(getDirectionToTarget(pos, target))
        };

        for (Direction d : strategicOptions) {
            Position next = nextPos(pos, d);
            if (arena.isEmpty(next.row, next.col) && !arena.isJetwall(next.row, next.col)) {
                // Check if this position is strategically better
                int optionsFromNext = countValidMoves(next, arena);
                int optionsFromCurrent = countValidMoves(pos, arena);

                if (optionsFromNext >= optionsFromCurrent) {
                    return d;
                }
            }
        }

        return null;
    }

    // Count valid moves from a position
    private static int countValidMoves(Position pos, ArenaView arena) {
        int count = 0;
        for (Direction d : Direction.values()) {
            Position next = nextPos(pos, d);
            if (arena.isEmpty(next.row, next.col) && !arena.isJetwall(next.row, next.col)) {
                count++;
            }
        }
        return count;
    }

    // Chase with obstacle avoidance
    private static Direction chaseWithAvoidance(Enemy enemy, ArenaView arena) {
        Direction primary = chase(enemy, arena);

        Direction[] options = {
                primary,
                turnLeft(primary),
                turnRight(primary),
                opposite(primary)
        };

        for (Direction d : options) {
            Position next = nextPos(enemy.getPosition(), d);
            if (arena.isEmpty(next.row, next.col) && !arena.isJetwall(next.row, next.col)) {
                return d;
            }
        }

        return enemy.getDirection(); // Stay in place if completely blocked
    }

    // Advanced chase with comprehensive avoidance
    private static Direction advancedChaseAvoid(Enemy enemy, ArenaView arena) {
        Position player = arena.getPlayerPosition();
        Position pos = enemy.getPosition();

        // Calculate best approach considering obstacles
        Direction bestDir = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Direction d : Direction.values()) {
            Position next = nextPos(pos, d);

            if (!arena.isEmpty(next.row, next.col) || arena.isJetwall(next.row, next.col)) {
                continue; // Skip blocked directions
            }

            // Score based on distance to player and safety
            double distanceToPlayer = Math.sqrt(
                    Math.pow(next.row - player.row, 2) +
                            Math.pow(next.col - player.col, 2));

            // Prefer closer to player, but avoid dangerous positions
            double score = -distanceToPlayer;

            // Penalize positions near jetwalls
            for (Direction checkDir : Direction.values()) {
                Position checkPos = nextPos(next, checkDir);
                if (arena.isJetwall(checkPos.row, checkPos.col)) {
                    score -= 2.0; // Penalty for being near jetwall
                }
            }

            if (score > bestScore) {
                bestScore = score;
                bestDir = d;
            }
        }

        return bestDir != null ? bestDir : enemy.getDirection();
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
