package src.UIGameEngine;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

import src.Characters.CharacterLoader;
import src.Characters.Characters;
import src.leaderboardstory.Leaderboard;
import src.leaderboardstory.LeaderboardPanel;
import src.leaderboardstory.SaveSystem;

public class MainMenu extends JFrame {

    private JPanel mainMenuPanel;
    private Image mainMenuBackground;
    private JPanel characterSelectPanel;
    private JPanel arenaSelectPanel;
    private JPanel difficultySelectPanel;
    private String selectedCharacter;
    private String selectedArena;

    public MainMenu() {
        setTitle("FOP Tron");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setResizable(false);
        setLocationRelativeTo(null);
        mainMenuBackground = new ImageIcon("src/assets/fop_tron_bg.png.jpg").getImage();

        createMainMenuPanel();
        add(mainMenuPanel);
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
        mainMenuPanel.setLayout(new BorderLayout());
        mainMenuPanel.setOpaque(false);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        buttonPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 0, 10, 0);

        JButton startButton = createStyledButton("START GAME");
        startButton.addActionListener(e -> startGame());
        gbc.gridy = 0;
        buttonPanel.add(startButton, gbc);

        JButton loadButton = createStyledButton("LOAD GAME");
        loadButton.addActionListener(e -> loadGame());
        gbc.gridy = 1;
        buttonPanel.add(loadButton, gbc);

        JButton leaderboardButton = createStyledButton("LEADERBOARD");
        leaderboardButton.addActionListener(e -> showLeaderboard());
        gbc.gridy = 2;
        buttonPanel.add(leaderboardButton, gbc);

        JButton exitButton = createStyledButton("EXIT GAME");
        exitButton.addActionListener(e -> exitGame());
        gbc.gridy = 3;
        buttonPanel.add(exitButton, gbc);

        JPanel bottomWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomWrapper.setOpaque(false);
        bottomWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 110));
        bottomWrapper.add(buttonPanel);

        mainMenuPanel.add(bottomWrapper, BorderLayout.SOUTH);
    }

    private void startGame() {
        showCharacterSelection();
    }

    private void showCharacterSelection() {
        characterSelectPanel = new JPanel();
        characterSelectPanel.setLayout(new GridBagLayout());
        characterSelectPanel.setBackground(Color.BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(20, 20, 20, 20);

        JLabel selectLabel = new JLabel("SELECT YOUR CHARACTER");
        selectLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
        selectLabel.setForeground(new Color(0, 200, 255));
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        characterSelectPanel.add(selectLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;

        ArrayList<Characters> characters = CharacterLoader
                .loadCharacters("Users/USER/Documents/VS code/copy/foptron/src/Characters/Characters.txt");
        if (characters != null) {
            int col = 0, row = 1;
            for (Characters character : characters) {
                String charName = character.getName();
                String description = character.getDescription();
                JButton charButton = createCharacterButton(charName, description);

                charButton.addActionListener(e -> {
                    selectedCharacter = charName;
                    showArenaSelection();
                });

                gbc.gridx = col;
                gbc.gridy = row;
                characterSelectPanel.add(charButton, gbc);

                col++;
                if (col > 1) {
                    col = 0;
                    row++;
                }
            }

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

        JLabel titleLabel = new JLabel("SELECT ARENA");
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 200, 255));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        arenaSelectPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;

        JButton arena1Button = createArenaButton("ARENA 1", "Classic Grid");
        arena1Button.addActionListener(e -> {
            selectedArena = "Arena 1";
            showDifficultySelection();
        });
        gbc.gridx = 0;
        arenaSelectPanel.add(arena1Button, gbc);

        JButton arena2Button = createArenaButton("ARENA 2", "Speed Ramps");
        arena2Button.addActionListener(e -> {
            selectedArena = "Arena 2";
            showDifficultySelection();
        });
        gbc.gridx = 1;
        arenaSelectPanel.add(arena2Button, gbc);

        gbc.gridy = 2;

        JButton arena3Button = createArenaButton("ARENA 3", "Obstacle Course");
        arena3Button.addActionListener(e -> {
            selectedArena = "Arena 3";
            showDifficultySelection();
        });
        gbc.gridx = 0;
        arenaSelectPanel.add(arena3Button, gbc);

        JButton randomButton = createArenaButton("RANDOM", "Procedural Generation");
        randomButton.addActionListener(e -> {
            selectedArena = "Random";
            showDifficultySelection();
        });
        gbc.gridx = 1;
        arenaSelectPanel.add(randomButton, gbc);

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

        getContentPane().removeAll();
        add(arenaSelectPanel);
        revalidate();
        repaint();
    }

    private void showDifficultySelection() {
        difficultySelectPanel = new JPanel();
        difficultySelectPanel.setLayout(new GridBagLayout());
        difficultySelectPanel.setBackground(Color.BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        JLabel titleLabel = new JLabel("SELECT DIFFICULTY");
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 200, 255));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        difficultySelectPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;

        JButton easyButton = createDifficultyButton("EASY", "More Koura and Sark (easier mix)");
        easyButton.addActionListener(e -> launchGame("EASY"));
        gbc.gridx = 0;
        difficultySelectPanel.add(easyButton, gbc);

        JButton mediumButton = createDifficultyButton("MEDIUM", "More Rinzler and Sark (balanced)");
        mediumButton.addActionListener(e -> launchGame("MEDIUM"));
        gbc.gridx = 1;
        difficultySelectPanel.add(mediumButton, gbc);

        JButton hardButton = createDifficultyButton("HARD", "More Clu and Rinzler (harder mix)");
        hardButton.addActionListener(e -> launchGame("HARD"));
        gbc.gridx = 2;
        difficultySelectPanel.add(hardButton, gbc);

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

        getContentPane().removeAll();
        add(difficultySelectPanel);
        revalidate();
        repaint();
    }

    private void loadGame() {
        // Show available save slots
        String[] slots = SaveSystem.listSaveSlots();
        if (slots.length == 0) {
            JOptionPane.showMessageDialog(this, "No save slots found.", "Load Game", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String chosen = (String) JOptionPane.showInputDialog(
                this,
                "Choose a save slot:",
                "Load Game",
                JOptionPane.PLAIN_MESSAGE,
                null,
                slots,
                slots[0]);

        if (chosen == null)
            return; // user cancelled

        String[] data = SaveSystem.loadProgressFromSlot(chosen);
        if (data == null) {
            JOptionPane.showMessageDialog(this, "Failed to load save.", "Load Game", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // data layout: [playerName, xp, level, lives, discsOwned, arena, difficulty,
        // roundNumber, totalScore]
        String name = data[0];
        int xp = parseIntOrZero(data[1]);
        int level = parseIntOrZero(data[2]);
        double lives = parseDoubleOrZero(data[3]);
        int discs = parseIntOrZero(data[4]);
        String arenaName = data[5] == null || data[5].isEmpty() ? "Arena 1" : data[5];
        String difficulty = data[6] == null || data[6].isEmpty() ? "EASY" : data[6];

        // find the character template from your CharacterLoader list
        ArrayList<Characters> characters = CharacterLoader
                .loadCharacters("Users/USER/Documents/VS code/copy/foptron/src/Characters/Characters.txt");
        Characters loadedCharacter = null;
        for (Characters c : characters) {
            if (c.getName().equalsIgnoreCase(name)) {
                loadedCharacter = c;
                break;
            }
        }

        if (loadedCharacter == null) {
            JOptionPane.showMessageDialog(this, "Saved character not found in Characters.txt", "Load Game",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // apply saved stats onto the character object
        loadedCharacter.gainXp(xp); // xp may be relative; adjust if your Characters.gainXp logic expects absolute
                                    // vs delta
        loadedCharacter.setLevel(level);
        // set lives to the saved value (Characters doesn't have setLives; if not
        // present use updateLives difference or add a setter)
        double currentLives = loadedCharacter.getLives();
        loadedCharacter.updateLives(lives - currentLives);
        loadedCharacter.setDiscsOwned(discs - loadedCharacter.getDiscsOwned());

        JOptionPane.showMessageDialog(this,
                "Loaded: " + loadedCharacter.getName() + " | Level " + loadedCharacter.getLevel(),
                "Load Game", JOptionPane.INFORMATION_MESSAGE);

        // launch engine with loaded character
        new GameEngine(loadedCharacter, arenaName, difficulty);
        dispose();
    }

    private int parseIntOrZero(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    private double parseDoubleOrZero(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private void showLeaderboard() {
        // Ensure file/entries are loaded
        Leaderboard.loadFromFile();

        // Use modal dialog so it looks like part of interface
        JDialog d = LeaderboardPanel.createDialogFor(this);
        d.setVisible(true);
    }

    private void exitGame() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?", "Exit Game",
                JOptionPane.YES_NO_CANCEL_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void launchGame(String difficulty) {
        ArrayList<Characters> characters = CharacterLoader
                .loadCharacters("Users/USER/Documents/VS code/copy/foptron/src/Characters/Characters.txt");
        Characters characterObject = null;
        for (Characters c : characters) {
            if (c.getName().equalsIgnoreCase(selectedCharacter)) {
                characterObject = c;
                break;
            }
        }
        if (characterObject != null) {
            new GameEngine(characterObject, selectedArena, difficulty);
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

    private JButton createCharacterButton(String name, String description) {
        JButton button = new JButton(
                "<html><center>" + name + "<br><small>" + description + "</small></center></html>");
        button.setPreferredSize(new Dimension(300, 120));
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainMenu());
    }
}
