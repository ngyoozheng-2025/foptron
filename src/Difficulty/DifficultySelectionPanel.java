package src.Difficulty;

import javax.swing.*;

import src.Arena.ArenaPanel;

import java.awt.*;

public class DifficultySelectionPanel extends JPanel {

    public DifficultySelectionPanel(JFrame frame, ArenaPanel.ArenaType arenaType) {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        JLabel title = new JLabel("SELECT DIFFICULTY", SwingConstants.CENTER);
        title.setForeground(Color.CYAN);
        title.setFont(new Font("Arial", Font.BOLD, 32));
        add(title, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 20, 20));
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(60, 200, 60, 200));

        // All buttons same style
        JButton easy = createButton(frame, arenaType, "EASY", Difficulty.EASY);
        JButton normal = createButton(frame, arenaType, "NORMAL", Difficulty.NORMAL);
        JButton hard = createButton(frame, arenaType, "HARD", Difficulty.HARD);

        buttonPanel.add(easy);
        buttonPanel.add(normal);
        buttonPanel.add(hard);

        add(buttonPanel, BorderLayout.CENTER);
    }

    private JButton createButton(JFrame frame,
            ArenaPanel.ArenaType arenaType,
            String text,
            Difficulty difficulty) {

        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 20));
        btn.setBackground(Color.CYAN); // blue
        btn.setForeground(Color.BLACK); // black text
        btn.setFocusPainted(false);

        btn.addActionListener(e -> {
            frame.getContentPane().removeAll();
            frame.add(new ArenaPanel(arenaType, difficulty));
            frame.revalidate();
            frame.repaint();
        });

        return btn;
    }
}
