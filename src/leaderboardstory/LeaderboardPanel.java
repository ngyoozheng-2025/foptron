package src.leaderboardstory;

import javax.swing.*;
import javax.swing.table.*;

import java.awt.*;
import java.util.List;

/**
 * Neat, aligned leaderboard panel (non-scrollable).
 * Expects a Leaderboard class with:
 * - static void loadFromFile()
 * - static List<Leaderboard.LeaderboardEntry> getEntries()
 *
 * Each LeaderboardEntry should expose: name, level, score, date (all public or
 * via getters).
 */
public class LeaderboardPanel extends JPanel {

    private final String[] columnNames = { "#", "Player", "Level", "Score", "Date" };
    private final JTable table;

    public LeaderboardPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // ask Leaderboard to reload data from disk first (caller may do it too)
        try {
            Leaderboard.loadFromFile();
        } catch (Exception ignored) {
        }

        List<Leaderboard.LeaderboardEntry> entries = Leaderboard.getEntries();

        // Prepare table data (top 10). If fewer than 10, remaining rows left blank.
        Object[][] data = new Object[10][columnNames.length];
        for (int i = 0; i < 10; i++) {
            if (i < entries.size()) {
                Leaderboard.LeaderboardEntry e = entries.get(i);
                data[i][0] = i + 1; // rank
                data[i][1] = e.name; // player
                data[i][2] = e.level; // level
                data[i][3] = e.score; // score
                data[i][4] = e.date; // date (expected "dd/MM/yyyy")
            } else {
                // empty row for visual consistency
                data[i][0] = "";
                data[i][1] = "";
                data[i][2] = "";
                data[i][3] = "";
                data[i][4] = "";
            }
        }

        // Non-editable table model
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        styleTable();

        // Add header and table without a scrollpane (so it is single page, fixed)
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(0, 34)); // taller header for style
        add(header, BorderLayout.NORTH);
        add(table, BorderLayout.CENTER);
    }

    private void styleTable() {
        // General table style
        table.setShowGrid(false);
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);
        table.setOpaque(false);
        table.setBackground(new Color(10, 10, 20));
        table.setForeground(Color.WHITE);
        table.setFont(new Font("Monospaced", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(0, 120, 160));
        table.setSelectionForeground(Color.WHITE);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFocusable(false);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(0, 200, 255)); // neon cyan
        header.setForeground(Color.BLACK);
        header.setFont(new Font("Monospaced", Font.BOLD, 14));
        header.setReorderingAllowed(false);
        header.setResizingAllowed(false);

        // Column widths & alignment
        TableColumnModel cm = table.getColumnModel();

        // Small rank column
        cm.getColumn(0).setPreferredWidth(48);
        // Player wide
        cm.getColumn(1).setPreferredWidth(220);
        // Level small
        cm.getColumn(2).setPreferredWidth(70);
        // Score medium
        cm.getColumn(3).setPreferredWidth(110);
        // Date medium
        cm.getColumn(4).setPreferredWidth(140);

        // Renderers for alignment and color
        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        leftRenderer.setForeground(Color.WHITE);
        leftRenderer.setBackground(new Color(10, 10, 20));
        leftRenderer.setFont(new Font("Monospaced", Font.PLAIN, 14));
        leftRenderer.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setForeground(Color.WHITE);
        centerRenderer.setBackground(new Color(10, 10, 20));
        centerRenderer.setFont(new Font("Monospaced", Font.PLAIN, 14));
        centerRenderer.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        rightRenderer.setForeground(Color.WHITE);
        rightRenderer.setBackground(new Color(10, 10, 20));
        rightRenderer.setFont(new Font("Monospaced", Font.PLAIN, 14));
        rightRenderer.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        cm.getColumn(0).setCellRenderer(centerRenderer); // #
        cm.getColumn(1).setCellRenderer(leftRenderer); // player
        cm.getColumn(2).setCellRenderer(centerRenderer); // level
        cm.getColumn(3).setCellRenderer(rightRenderer); // score
        cm.getColumn(4).setCellRenderer(centerRenderer); // date

        // Optional subtle row striping (keeps theme)
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private final Color DARK = new Color(10, 10, 20);
            private final Color ALT = new Color(14, 14, 26);

            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                c.setFont(new Font("Monospaced", Font.PLAIN, 14));
                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                } else {
                    c.setBackground((row % 2 == 0) ? DARK : ALT);
                    c.setForeground(Color.WHITE);
                }
                // keep alignment set earlier
                // (we reapply alignment per column)
                int modelCol = t.convertColumnIndexToModel(column);
                if (modelCol == 0 || modelCol == 2 || modelCol == 4) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else if (modelCol == 3) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        });
    }

    // For integration convenience: handy static factory that produces a framed
    // dialog for MainMenu
    public static JDialog createDialogFor(JFrame parent) {
        JDialog dlg = new JDialog(parent, "Leaderboard - Top 10", true);
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        LeaderboardPanel panel = new LeaderboardPanel();
        panel.setPreferredSize(new Dimension(640, 360)); // adjust to taste
        dlg.getContentPane().add(panel);
        dlg.pack();
        dlg.setResizable(false);
        dlg.setLocationRelativeTo(parent);
        return dlg;
    }
}
