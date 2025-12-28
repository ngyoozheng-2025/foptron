import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class CharacterLoader {
    public static ArrayList<Characters> loadCharacters(String filename){
        ArrayList<Characters> characters = new ArrayList<>();
        try{
            Scanner read = new Scanner(new File(filename));
            while(read.hasNextLine()){
                String line = read.nextLine().strip();

                //Ignores comment lines or empty lines if any
                if (line.startsWith("#") || line.isEmpty()){continue;}

                String[] attributes = line.split(",");
                if (attributes.length != 13){
                    System.out.println("Invalid character line");
                    continue;
                }

                // character data stored in the format:
                // # name, colour, xp, level, speed, stability, handling, disc_slot, discs_owned, lives, start_row, start_column, alive
                // This section parses all string values from array attributes into their required datatypes.
                String name = attributes[0];
                String color = attributes[1];
                int xp = Integer.parseInt(attributes[2]);
                int level = Integer.parseInt(attributes[3]);
                double speed = Double.parseDouble(attributes[4]);
                double stability = Double.parseDouble(attributes[5]);
                double handling = Double.parseDouble(attributes[6]);
                int disc_slot = Integer.parseInt(attributes[7]);
                int discs_owned = Integer.parseInt(attributes[8]);
                double lives = Double.parseDouble(attributes[9]);
                int start_row = Integer.parseInt(attributes[10]);
                int start_column = Integer.parseInt(attributes[11]);
                int alive = Integer.parseInt(attributes[12]);

                //debugging purposes
                // for (String attr: attributes){
                //     System.out.println(attr);
                // }
                // System.out.println(name.toLowerCase());

                // Create character from name:
                switch (name.toLowerCase()){
                    case "tron":
                        characters.add(new Tron(name,color,xp,level,speed, stability, handling, disc_slot, discs_owned, lives, start_row, start_column, alive));
                        System.out.println("Tron successfully loaded");
                        break;
                    case "kevin":
                        characters.add(new KevinFlynn(name, color, xp, level, speed, stability, handling, disc_slot, discs_owned, lives, start_row, start_column, alive));
                        System.out.println("KevinFlynn successfully loaded");
                        break;
                    default:
                        System.out.println("Unknown character");
                        return null;
                }
            }
            read.close();
            return characters;
        }
        catch(FileNotFoundException e){
            System.out.println("Could not find "+ filename);
            return null;
        }
    }
}
