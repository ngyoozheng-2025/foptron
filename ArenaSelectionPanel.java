import javax.swing.*;
import java.awt.*;

public class ArenaSelectionPanel extends JPanel {

    private ArenaPanel selectedArena;
    private JPanel previewPanel;

    public ArenaSelectionPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        JLabel title = new JLabel("SELECT ARENA");
        title.setFont(new Font("Monospaced", Font.BOLD, 24));
        title.setForeground(new Color(0, 200, 255));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);

        previewPanel = new JPanel();
        previewPanel.setBackground(Color.BLACK);
        add(previewPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        buttonPanel.setBackground(Color.BLACK);

        JButton classic = createArenaButton("ClassicGrid", ArenaPanel.ArenaType.CLASSIC);
        JButton neon = createArenaButton("NeonMaze", ArenaPanel.ArenaType.NEON);
        JButton open = createArenaButton("OpenFrontier", ArenaPanel.ArenaType.OPEN);
        JButton proc = createArenaButton("Procedural", ArenaPanel.ArenaType.PROCEDURAL);

        buttonPanel.add(classic);
        buttonPanel.add(neon);
        buttonPanel.add(open);
        buttonPanel.add(proc);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JButton createArenaButton(String name, ArenaPanel.ArenaType type) {
        JButton btn = new JButton(name);
        btn.setFont(new Font("Monospaced", Font.BOLD, 14));
        btn.setForeground(new Color(0, 200, 255));
        btn.setBackground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> showPreview(type));
        return btn;
    }

    private void showPreview(ArenaPanel.ArenaType type) {
        previewPanel.removeAll();
        selectedArena = new ArenaPanel(type);
        previewPanel.add(selectedArena);
        previewPanel.revalidate();
        previewPanel.repaint();
    }

    public ArenaPanel getSelectedArena() {
        return selectedArena;
    }
}
