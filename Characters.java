public abstract class Characters {
    ///note to self: include changes to visuals with level up module
    protected String name;
    protected String color;
    protected int xp;
    protected int level;
    protected double speed;
    protected double stability;
    protected double handling;
    protected int disc_slot;
    protected int discs_owned;
    protected double lives;
    protected Position position;
    protected Direction direction;
    protected boolean alive;

    //Constructor
    public Characters(String name, String color, int xp, int level, double speed, 
        double stability, double handling, int disc_slot, int discs_owned, double lives,
        int start_row, int start_column, int alive){

        this.name = name;
        this.color = color;
        this.xp = xp;
        this.level = level;
        this.speed = speed;
        this.stability = stability;
        this.handling = handling;
        this.disc_slot = disc_slot;
        this.discs_owned = discs_owned;
        this.lives = lives;
        this.position = new Position(start_row,start_column);
        this.direction = Direction.UP;
        if (alive == 1){
            this.alive = true;
        }
        else{
            this.alive = false;
        }
    }

    // getters and setters
    public String getName() {return name;}
    public String getColor() {return color;}
    public int getXp() {return xp;}
    public int getLevel() {return level;}
    public boolean isAlive() {return alive;}
    public Position getPosition() {return position;}

    public void setSpeed(double value){speed += value;}
    public void setStability(double value){stability += value;}
    public void setHandling(double value){handling += value;}
    public void setDiscsOwned(int value){
        discs_owned = Math.min(this.disc_slot, discs_owned + value);
    }

    // method to update lives, note value can be +ve or -ve
    public void updateLives(double value){lives += value;}

    // Helper method to move in a direction (note copied from Enemy.java)
    protected void move(Direction dir) {
        this.direction = dir;

        switch (dir) {
            case UP -> position.row--;
            case DOWN -> position.row++;
            case LEFT -> position.col--;
            case RIGHT -> position.col++;
        }
    }

    //gain xp method
    public void gainXp(int value){
        xp += value;
    }

    // stat increase method (note that stats increase differ for each character. 
    // this method is overriden in Tron.java and Kevin.java respectively)
    protected abstract void applyStatIncrease();

    // level up method (each 100 xp increments level before level 10, and subsequently, each 1000 xp increments level)
    public void levelUp(){
        boolean leveled_up = false;
        if (level <10){
            if (xp % 100 == 0){
                level += 1;
                leveled_up = true;
            }
        }
        else{
            if ((xp-1000) % 200 == 0){
                if (level < 99){
                    level += 1;
                    leveled_up = true;
                }
            }
        }
        if (leveled_up){
            // +1 life every 10 levels
            if (level % 10 == 0){
                updateLives(1.0);;
            }
            // Additional disc slot every 15 levels
            if (level % 15 ==0){
                disc_slot += 1;
            }
            applyStatIncrease();
        }
    }
}
