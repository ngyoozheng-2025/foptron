import java.awt.Color;
import java.awt.Point;
import java.util.Random;

public abstract class Enemy {
    protected String name;
    protected Color color;
    protected int speed; 
    protected int Handling; 
    protected int Lives;
    protected int Discs;
    protected int XP;
    protected Point position;

    public Enemy(String name, Color color, int speed, int Handling, int Lives, int Discs, int XP, Point startPos) {
        this.name = name;
        this.color = color;
        this.speed = speed;
        this.Handling = Handling;
        this.speed = speed;
        this.position = startPos;
    }
    public  abstract class Difficulty{
        protected String Difficulty;
    }

    class Easy extends Difficulty{
        public Easy(int speed){
            Random random = new Random();
            String[] directions={"Right","Down","Left","Up"};
            Point startPos=new Point(random.nextInt(40),random.nextInt(40));

            String direction=directions[random.nextInt(directions.length)];
            int steps=0;
            int maxsteps;
            if(direction=="Right"){
                maxsteps=startPos.x;
            }
            else if(direction=="Down"){
                maxsteps=40-startPos.y;
            }
            else if(direction=="Left"){
                maxsteps=40-startPos.x;
            }
            else{
                maxsteps=startPos.y;
            }

            

            steps+=speed;
            if(steps>=maxsteps){
            switch(direction) {
                case "RIGHT":
                 direction = "DOWN"; 
                  break;
                case "DOWN": 
                 direction = "LEFT";
                  break;
                case "LEFT": 
                 direction = "UP";
                  break;
                case "UP":  
                 direction = "RIGHT";
                  break;
            }}
            
        }
    }

    class Medium extends Difficulty{
        public Medium(){
            this.Difficulty="Medium";
        }
    }

    class Hard extends Difficulty{
        public Hard(){
            this.Difficulty="Hard";
        }
    }

    class Impossible extends Difficulty{
        public Impossible(){
           
        }
    }
    public abstract void move(Point playerPos); // define AI behavior

    public void takeDamage(int dmg) {
        Lives -= dmg;
        if (Lives == 0) {
            System.out.println(name + " defeated!");
        }
    }

    public Point getPosition() {
        return position;
    }

    public Color getColor() {
        return color;
    }

    public String getName() {
        return name;
    }
}

class Clu extends Enemy {
    public Clu(Point startPos) {
        super("Clu", new Color(255,215,0)/*(255,215,0 )is gold color*/, 95, 80, 100, 10,1000); 
    }

    @Override
    public void move(Point playerPos) {
        // Simple chase logic
        if (playerPos.x > position.x) position.x += speed;
        else if (playerPos.x < position.x) position.x -= speed;

        if (playerPos.y > position.y) position.y += speed;
        else if (playerPos.y < position.y) position.y -= speed;
    }
}

class Rinzler extends Enemy {
    private Random rand = new Random();
    public Rinzler(Point startPos) {
        super("Rinzler", Color.RED, 85, 70, 20,6,500);
    }

    @Override
    public void move(Point playerPos) {
        // Random movement
        position.x += rand.nextInt(3) - 1;
        position.y += rand.nextInt(3) - 1;
    }
}

class Koura extends Enemy {
    private Random rand = new Random();
    public Koura(Point startPos) {
        super("Koura", Color.GREEN, 40, 30, 1,1,10);
    }

    @Override
    public void move(Point playerPos) {
        // Random movement
        position.x += rand.nextInt(3) - 1;
        position.y += rand.nextInt(3) - 1;
    }
}

class Sark extends Enemy {
    private Random rand = new Random();
    public Sark(Point startPos) {
        super("Sark", Color.YELLOW, 60, 50, 5,2,100);
    }

    @Override
    public void move(Point playerPos) {
        // Random movement
        position.x += rand.nextInt(3) - 1;
        position.y += rand.nextInt(3) - 1;
    }
}


// Similarly define Sark and Koura with unique behaviors
