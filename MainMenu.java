import javax.swing.*; //use components like jframe,jbutton...

import src.Characters.CharacterLoader;
import src.Characters.Characters;

import java.awt.*; //color,font...
import java.awt.event.*; //all events classes like actionevent etc
import java.io.*;
import java.util.ArrayList;

public class MainMenu extends JFrame {

    private JPanel mainMenuPanel;
    private Image mainMenuBackground;
    private JPanel characterSelectPanel;
    private JPanel arenaSelectPanel;
    private JPanel difficultySelectPanel;
    private String selectedCharacter;
    private String selectedArena;

    public MainMenu() { // Window
        setTitle("FOP Tron");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setResizable(false);
        setLocationRelativeTo(null); // center the window on screen
        mainMenuBackground = new ImageIcon("assets/fop_tron_bg.png.jpg").getImage(); // background pic from assets

        createMainMenuPanel(); // call the method

        add(mainMenuPanel); // add main menu to window

        setVisible(true);
    }

    private void createMainMenuPanel() {
        mainMenuPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (mainMenuBackground != null) {
                    g.drawImage(mainMenuBackground, 0, 0, getWidth(), getHeight(), this);
                }

            }
        };
        mainMenuPanel.setLayout(new BorderLayout()); // use to arrange component(south north..)
        mainMenuPanel.setOpaque(false);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout()); // row column
        buttonPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; // column
        gbc.insets = new Insets(10, 0, 10, 0); // spacing between buttons

        // Start game button
        JButton startButton = createStyledButton("START GAME");
        startButton.addActionListener(e -> startGame());
        gbc.gridy = 0; // row
        buttonPanel.add(startButton, gbc);

        // Load game button
        JButton loadButton = createStyledButton("LOAD GAME");
        loadButton.addActionListener(e -> loadGame());
        gbc.gridy = 1;
        buttonPanel.add(loadButton, gbc);

        // Leaderboard button
        JButton leaderboardButton = createStyledButton("LEADERBOARD");
        leaderboardButton.addActionListener(e -> showLeaderboard());
        gbc.gridy = 2;
        buttonPanel.add(leaderboardButton, gbc);

        // Exit game button
        JButton exitButton = createStyledButton("EXIT GAME");
        exitButton.addActionListener(e -> exitGame());
        gbc.gridy = 3;
        buttonPanel.add(exitButton, gbc);

        // Add button panel to the bottom & wrapper to shift right
        JPanel bottomWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomWrapper.setOpaque(false);
        bottomWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 110)); // shift right
        bottomWrapper.add(buttonPanel);

        mainMenuPanel.add(bottomWrapper, BorderLayout.SOUTH);

    }

    private void startGame() {
        showCharacterSelection();
    }

    private void showCharacterSelection() {
        // Create character selection panel
        characterSelectPanel = new JPanel();
        characterSelectPanel.setLayout(new GridBagLayout());
        characterSelectPanel.setBackground(Color.BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; // column
        gbc.insets = new Insets(20, 20, 20, 20);

        // Title
        JLabel selectLabel = new JLabel("SELECT YOUR CHARACTER");
        selectLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
        selectLabel.setForeground(new Color(0, 200, 255));
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        characterSelectPanel.add(selectLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;

        // Load characters dynamically
        ArrayList<Characters> characters = CharacterLoader.loadCharacters("Characters.txt");
        if (characters != null) {
            int col = 0;
            int row = 1;
            for (Characters character : characters) {
                String charName = character.getName();
                String description = character.getDescription(); // make sure this exists
                JButton charButton = createCharacterButton(charName, description);

                charButton.addActionListener(e -> {
                    selectedCharacter = charName;
                    showArenaSelection();
                });

                gbc.gridx = col;
                gbc.gridy = row;
                characterSelectPanel.add(charButton, gbc);

                col++;
                if (col > 1) { // wrap to next row after 2 columns
                    col = 0;
                    row++;
                }
            }

            // Back Button below characters
            JButton backButton = createStyledButton("BACK");
            backButton.addActionListener(e -> {
                getContentPane().removeAll();
                add(mainMenuPanel);
                revalidate();
                repaint();
            });

            gbc.gridx = 0;
            gbc.gridy = row + 1;
            gbc.gridwidth = 2;
            characterSelectPanel.add(backButton, gbc);
        }

        getContentPane().removeAll();
        add(characterSelectPanel);
        revalidate();
        repaint();
    }

    private void showArenaSelection() {
        arenaSelectPanel = new JPanel();
        arenaSelectPanel.setLayout(new GridBagLayout());
        arenaSelectPanel.setBackground(Color.BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        // Title
        JLabel titleLabel = new JLabel("SELECT ARENA");
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 200, 255));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        arenaSelectPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;

        // Arena hardcode need to change
        // Arena 1 Button
        JButton arena1Button = createArenaButton("ARENA 1", "Classic Grid");
        arena1Button.addActionListener(e -> {
            selectedArena = "Arena 1";
            showDifficultySelection();
        });
        gbc.gridx = 0;
        arenaSelectPanel.add(arena1Button, gbc);

        // Arena 2 Button
        JButton arena2Button = createArenaButton("ARENA 2", "Speed Ramps");
        arena2Button.addActionListener(e -> {
            selectedArena = "Arena 2";
            showDifficultySelection();
        });
        gbc.gridx = 1;
        arenaSelectPanel.add(arena2Button, gbc);

        gbc.gridy = 2;

        // Arena 3 Button
        JButton arena3Button = createArenaButton("ARENA 3", "Obstacle Course");
        arena3Button.addActionListener(e -> {
            selectedArena = "Arena 3";
            showDifficultySelection();
        });
        gbc.gridx = 0;
        arenaSelectPanel.add(arena3Button, gbc);

        // Random Arena Button
        JButton randomButton = createArenaButton("RANDOM", "Procedural Generation");
        randomButton.addActionListener(e -> {
            selectedArena = "Random";
            showDifficultySelection();
        });
        gbc.gridx = 1;
        arenaSelectPanel.add(randomButton, gbc);

        // Back Button
        JButton backButton = createStyledButton("BACK");
        backButton.addActionListener(e -> {
            getContentPane().removeAll();
            add(characterSelectPanel);
            revalidate();
            repaint();
        });
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        arenaSelectPanel.add(backButton, gbc);

        // Switch to arena selection
        getContentPane().removeAll();
        add(arenaSelectPanel);
        revalidate();
        repaint();
    }

    private void loadGame() {
        String[] data = SaveSystem.loadProgress();

        if (data == null || data.length < 5) {
            JOptionPane.showMessageDialog(
                    this, "No save file found",
                    "Load Game", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ArrayList<Characters> characters = CharacterLoader.loadCharacters("Characters.txt");

        Characters loadedCharacter = null;
        for (Characters c : characters) {
            if (c.getName().equalsIgnoreCase(data[0])) {
                loadedCharacter = c;
                break;
            }
        }

        if (loadedCharacter == null) {
            JOptionPane.showMessageDialog(
                    this, "Saved character not found",
                    "Load Game", JOptionPane.ERROR_MESSAGE);
            return;
        }

        loadedCharacter.gainXp(Integer.parseInt(data[1]));
        loadedCharacter.setLevel(Integer.parseInt(data[2]));
        loadedCharacter.updateLives(
                Double.parseDouble(data[3]) - loadedCharacter.getLives());
        loadedCharacter.setDiscsOwned(
                Integer.parseInt(data[4]) - loadedCharacter.getDiscsOwned());

        JOptionPane.showMessageDialog(
                this,
                "Loaded: " + loadedCharacter.getName() +
                        " | Level " + loadedCharacter.getLevel(),
                "Load Game", JOptionPane.INFORMATION_MESSAGE);

        new GameEngine(loadedCharacter, "Arena 1");
        dispose();
    }

    private void showLeaderboard() {
        JFrame leaderboardFrame = new JFrame("Leaderboard - Top 10");
        leaderboardFrame.setSize(500, 400);
        leaderboardFrame.setLocationRelativeTo(this);

        JTextArea textArea = new JTextArea();
        textArea.setText("LEADERBOARD - TOP 10\n\n");
        textArea.append(String.format("%-5s %-15s %-10s %-10s %-20s\n", "#", "Player", "Level", "Score", "Date"));
        textArea.append("-------------------------------------------------------------\n");

        int rank = 1;
        try (BufferedReader reader = new BufferedReader(new FileReader("leaderboard.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length >= 4) {
                    textArea.append(String.format("%-5d %-15s %-10s %-10s %-20s\n", rank++, p[0], p[1], p[2], p[3]));
                }
            }
            if (rank == 1) {
                textArea.append("No entries yet.\n");
            }
        } catch (IOException e) {
            textArea.append("No leaderboard file found.\n");
        }

        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        leaderboardFrame.add(scrollPane);
        leaderboardFrame.setVisible(true);

    }

    private void exitGame() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?", "Exit Game",
                JOptionPane.YES_NO_CANCEL_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    /**
     * Show difficulty selection screen after arena is chosen
     */
    private void showDifficultySelection() {
        difficultySelectPanel = new JPanel();
        difficultySelectPanel.setLayout(new GridBagLayout());
        difficultySelectPanel.setBackground(Color.BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        // Title
        JLabel titleLabel = new JLabel("SELECT DIFFICULTY");
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 200, 255));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        difficultySelectPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;

        // Easy Button
        JButton easyButton = createDifficultyButton("EASY", "More Koura and Sark (easier mix)");
        easyButton.addActionListener(e -> launchGame("EASY"));
        gbc.gridx = 0;
        difficultySelectPanel.add(easyButton, gbc);

        // Medium Button
        JButton mediumButton = createDifficultyButton("MEDIUM", "More Rinzler and Sark (balanced)");
        mediumButton.addActionListener(e -> launchGame("MEDIUM"));
        gbc.gridx = 1;
        difficultySelectPanel.add(mediumButton, gbc);

        // Hard Button
        JButton hardButton = createDifficultyButton("HARD", "More Clu and Rinzler (harder mix)");
        hardButton.addActionListener(e -> launchGame("HARD"));
        gbc.gridx = 2;
        difficultySelectPanel.add(hardButton, gbc);

        // Back Button
        JButton backButton = createStyledButton("BACK");
        backButton.addActionListener(e -> {
            getContentPane().removeAll();
            add(arenaSelectPanel);
            revalidate();
            repaint();
        });
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        difficultySelectPanel.add(backButton, gbc);

        // Switch to difficulty selection
        getContentPane().removeAll();
        add(difficultySelectPanel);
        revalidate();
        repaint();
    }

    private JButton createDifficultyButton(String name, String description) {
        JButton button = new JButton(
                "<html><center>" + name + "<br><small>" + description + "</small></center></html>");
        button.setPreferredSize(new Dimension(220, 100));
        button.setFont(new Font("Monospaced", Font.BOLD, 16));
        button.setForeground(new Color(0, 200, 255));
        button.setBackground(Color.BLACK);
        button.setBorder(BorderFactory.createLineBorder(new Color(0, 200, 255), 2));
        button.setFocusPainted(false);

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(0, 100, 150));
            }

            public void mouseExited(MouseEvent evt) {
                button.setBackground(Color.BLACK);
            }
        });

        return button;
    }

    /**
     * Launch the actual game using the selected arena and difficulty
     */
    private void launchGame(String difficulty) {
        ArrayList<Characters> characters = CharacterLoader.loadCharacters("Characters.txt");
        Characters characterObject = null;
        for (Characters c : characters) {
            if (c.getName().equalsIgnoreCase(selectedCharacter)) {
                characterObject = c;
                break;
            }
        }
        if (characterObject != null) {
            new GameEngine(characterObject, selectedArena, difficulty); // open with the character, arena and difficulty
        }
        dispose();
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(200, 50));
        button.setFont(new Font("Monospaced", Font.BOLD, 18));
        button.setForeground(new Color(0, 200, 255));
        button.setBackground(Color.BLACK);
        button.setBorder(BorderFactory.createLineBorder(new Color(0, 200, 255), 2));
        button.setFocusPainted(false); // look pretty

        // Hover effect
        button.addMouseListener(new MouseAdapter() { // Mouseadapter override the methods i need
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(0, 100, 150));
            }

            public void mouseExited(MouseEvent evt) {
                button.setBackground(Color.BLACK);
            }
        });

        return button;

    }

    private JButton createCharacterButton(String name, String description) {
        JButton button = new JButton(
                "<html><center>" + name + "<br><small>" + description + "</small></center></html>");
        button.setPreferredSize(new Dimension(300, 120));
        button.setFont(new Font("Monospaced", Font.BOLD, 16));
        button.setForeground(new Color(0, 200, 255));
        button.setBackground(Color.BLACK);
        button.setBorder(BorderFactory.createLineBorder(new Color(0, 200, 255), 2));
        button.setFocusPainted(false);

        // Hover effect
        button.addMouseListener(new MouseAdapter() { // Mouseadapter override the methods i need
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(0, 100, 150));
            }

            public void mouseExited(MouseEvent evt) {
                button.setBackground(Color.BLACK);
            }
        });

        return button;
    }

    private JButton createArenaButton(String name, String description) {
        JButton button = new JButton(
                "<html><center>" + name + "<br><small>" + description + "</small></center></html>");
        button.setPreferredSize(new Dimension(250, 100));
        button.setFont(new Font("Monospaced", Font.BOLD, 14));
        button.setForeground(new Color(0, 200, 255));
        button.setBackground(Color.BLACK);
        button.setBorder(BorderFactory.createLineBorder(new Color(0, 200, 255), 2));
        button.setFocusPainted(false);

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(new Color(0, 100, 150));
            }

            public void mouseExited(MouseEvent evt) {
                button.setBackground(Color.BLACK);
            }
        });

        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainMenu());

    }
}
