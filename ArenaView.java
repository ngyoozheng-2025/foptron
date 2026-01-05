public interface ArenaView {
    boolean isWall(int row, int col);

    boolean isJetwall(int row, int col);

    boolean isEmpty(int row, int col);

    Position getPlayerPosition();
}
