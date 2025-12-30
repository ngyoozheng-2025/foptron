import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class StoryManager {
    private final HashMap<String, String> storyChapters;

    public StoryManager(String filename) {
        storyChapters = new HashMap<>();
        loadStory(filename);
    }

    private void loadStory(String filename) {
        try (Scanner sc = new Scanner(new File(filename))) {
            String currentChapter = "";
            StringBuilder content = new StringBuilder();

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                if (line.startsWith("[") && line.endsWith("]")) {
                    if (!currentChapter.isEmpty()) {
                        storyChapters.put(currentChapter, content.toString());
                    }
                    currentChapter = line.substring(1, line.length() - 1);
                    content = new StringBuilder();
                } else {
                    content.append(line).append("\n");
                }
            }
            // Put the last chapter
            if (!currentChapter.isEmpty()) {
                storyChapters.put(currentChapter, content.toString());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error: story.txt not found.");
        }
    }

    public void playCutscene(String chapterKey) {
        String content = storyChapters.get(chapterKey);
        if (content != null) {
            System.out.println("\n--- TRON NARRATIVE SEQUENCE ---");
            System.out.println(content);
            System.out.println("-------------------------------\n");
            // Here you could add a delay or a "Press Enter to continue" prompt
        }
    }
    
    public void showAchievement(String message) {
        System.out.println(">>> ACHIEVEMENT UNLOCKED: " + message + " <<<");
    }
}