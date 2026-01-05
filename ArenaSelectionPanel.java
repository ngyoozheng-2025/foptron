import javax.swing.*;
import java.awt.*;

public class ArenaSelectionPanel extends JPanel {

    private ArenaPanel.ArenaType selectedArenaType;
    private JPanel previewPanel;

    public ArenaSelectionPanel(JFrame frame) {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        // ===== Title =====
        JLabel title = new JLabel("SELECT ARENA", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 28));
        title.setForeground(new Color(0, 200, 255));
        add(title, BorderLayout.NORTH);

        // ===== Preview Panel =====
        previewPanel = new JPanel();
        previewPanel.setBackground(Color.BLACK);
        add(previewPanel, BorderLayout.CENTER);

        // ===== Arena Buttons =====
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        buttonPanel.setBackground(Color.BLACK);

        JButton classicBtn = createArenaButton("ClassicGrid", ArenaPanel.ArenaType.CLASSIC);
        JButton neonBtn = createArenaButton("NeonMaze", ArenaPanel.ArenaType.NEON);
        JButton openBtn = createArenaButton("OpenFrontier", ArenaPanel.ArenaType.OPEN);
        JButton procBtn = createArenaButton("Procedural", ArenaPanel.ArenaType.PROCEDURAL);

        buttonPanel.add(classicBtn);
        buttonPanel.add(neonBtn);
        buttonPanel.add(openBtn);
        buttonPanel.add(procBtn);

        add(buttonPanel, BorderLayout.NORTH);

        // ===== Confirm Button =====
        JButton confirmBtn = new JButton("CONFIRM");
        confirmBtn.setFont(new Font("Arial", Font.BOLD, 20));
        confirmBtn.setBackground(new Color(0, 200, 255)); // blue box
        confirmBtn.setForeground(Color.BLACK); // black text
        confirmBtn.setFocusPainted(false);
        confirmBtn.setPreferredSize(new Dimension(150, 50)); // smaller
        confirmBtn.addActionListener(e -> {
            if (selectedArenaType != null) {
                frame.getContentPane().removeAll();
                frame.add(new DifficultySelectionPanel(frame, selectedArenaType));
                frame.revalidate();
                frame.repaint();
            } else {
                JOptionPane.showMessageDialog(frame, "Please select an arena first!");
            }
        });

        JPanel confirmPanel = new JPanel();
        confirmPanel.setBackground(Color.BLACK);
        confirmPanel.add(confirmBtn);

        add(confirmPanel, BorderLayout.SOUTH);
    }

    private JButton createArenaButton(String name, ArenaPanel.ArenaType type) {
        JButton btn = new JButton(name);
        btn.setFont(new Font("Monospaced", Font.BOLD, 16));
        btn.setForeground(new Color(0, 200, 255));
        btn.setBackground(Color.BLACK);
        btn.setFocusPainted(false);

        btn.addActionListener(e -> {
            selectedArenaType = type;
            showPreview(type);
        });

        return btn;
    }

    private void showPreview(ArenaPanel.ArenaType type) {
        previewPanel.removeAll();
        // Show ONLY walls (no player or enemies)
        ArenaPanel previewArena = new ArenaPanel(type, null); // pass null for difficulty to skip player/enemies
        previewArena.removePlayerAndEnemies(); // method we will add in ArenaPanel
        previewPanel.add(previewArena);
        previewPanel.revalidate();
        previewPanel.repaint();
    }
}
