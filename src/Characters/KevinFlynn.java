package src.Characters;

public class KevinFlynn extends Characters {
    public KevinFlynn(String name, String color, int xp, int level, double speed,
            double stability, double handling, int disc_slot, int discs_owned, double lives,
            int start_row, int start_column, int alive) {
        super(name, color, xp, level, speed, stability, handling, disc_slot, discs_owned, lives, start_row,
                start_column, alive);
    }

    @Override
    public void applyStatIncrease() {
        this.setHandling(0.3);
        ;
        this.setDiscsOwned(2);
    }

    @Override
    public String getBaseImagePath() {
        return "/res/Kevin/Kevin_base.png";
    }

    @Override
    public void update() {
    }

    @Override
    public String getOverlayImagePath() {
        return "/res/Kevin/Kevin_stripe.png";
    }
}
