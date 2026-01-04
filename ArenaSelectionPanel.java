import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ArenaSelectionPanel extends JPanel {
    public interface ArenaSelectListener {
        void onArenaSelected(String arenaName);
    }

    public ArenaSelectionPanel(ArenaSelectListener listener) {
        setLayout(new GridBagLayout());
        setBackground(Color.BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);

        JLabel title = new JLabel("SELECT ARENA");
        title.setFont(new Font("Monospaced", Font.BOLD, 20));
        title.setForeground(new Color(0, 200, 255));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;

        addArenaButton("ClassicGrid", "Standard square arena with solid walls", listener, gbc, 0, 1);
        addArenaButton("NeonMaze", "Maze-like structure with narrow corridors", listener, gbc, 1, 1);
        addArenaButton("OpenFrontier", "No boundaries — fall to derez", listener, gbc, 0, 2);
        addArenaButton("Procedural", "Randomly generated layout using seed", listener, gbc, 1, 2);
    }

    private void addArenaButton(String name, String description, ArenaSelectListener listener, GridBagConstraints gbc,
            int x, int y) {
        JButton button = new JButton(
                "<html><center>" + name + "<br><small>" + description + "</small></center></html>");
        button.setPreferredSize(new Dimension(250, 100));
        button.setFont(new Font("Monospaced", Font.BOLD, 14));
        button.setForeground(new Color(0, 200, 255));
        button.setBackground(Color.BLACK);
        button.setBorder(BorderFactory.createLineBorder(new Color(0, 200, 255), 2));
        button.setFocusPainted(false);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0, 100, 150));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.BLACK);
            }
        });

        button.addActionListener(e -> listener.onArenaSelected(name));

        gbc.gridx = x;
        gbc.gridy = y;
        add(button, gbc);
    }
}
