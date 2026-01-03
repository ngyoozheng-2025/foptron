package src.Characters;

public class Tron extends Characters{
    public Tron(String name, String color, int xp, int level, double speed, 
        double stability, double handling, int disc_slot, int discs_owned, double lives, 
        int start_row, int start_column,int alive){
            super(name, color, xp, level, speed, stability, handling, disc_slot, discs_owned, lives, start_row, start_column, alive);
    }

    @Override
    public void applyStatIncrease(){
        this.setSpeed(0.3);
        this.setStability(0.3);
    }

    @Override
    public String getImagePath(){
        return "/res/Tron/Tron.png";
    }

    @Override
    public void update() {
    }
    

}
