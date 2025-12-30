import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class StoryManager extends JPanel {
    private final List<String[]> scenes = new ArrayList<>();
    private int currentScene = 0;
    private final JLabel imageLabel;
    private final JTextArea textArea;
    private final Runnable onFinish;

    public StoryManager(Runnable onFinish) {
        this.onFinish = onFinish;
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        imageLabel = new JLabel("", SwingConstants.CENTER);
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.BOLD, 18));
        textArea.setForeground(Color.CYAN);
        textArea.setBackground(Color.BLACK);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        add(imageLabel, BorderLayout.CENTER);
        add(textArea, BorderLayout.SOUTH);

        loadStory("story.txt");
        displayNextScene();

        // Click to advance
        this.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                displayNextScene();
            }
        });
    }

    private void loadStory(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                scenes.add(line.split("\\|"));
            }
        } catch (IOException e) {
            scenes.add(new String[]{"null", "Welcome to Tron. Click to Start."});
        }
    }

    private void displayNextScene() {
        if (currentScene < scenes.size()) {
            String[] scene = scenes.get(currentScene);
            // Load Image
            if (!scene[1].equals("null")) {
                imageLabel.setIcon(new ImageIcon(scene[1]));
            } else {
                imageLabel.setIcon(null);
            }
            // Set Text
            textArea.setText(scene[2]);
            currentScene++;
        } else {
            onFinish.run(); // End of story, start game
        }
    }
}