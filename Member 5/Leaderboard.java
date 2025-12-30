import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Leaderboard extends JPanel {
    public Leaderboard(CardLayout layout, JPanel container) {
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        JLabel title = new JLabel("TOP TRON PILOTS", SwingConstants.CENTER);
        title.setFont(new Font("Courier New", Font.ITALIC, 32));
        title.setForeground(Color.GREEN);
        add(title, BorderLayout.NORTH);

        String[] columns = {"Player", "Score", "Wins"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        table.setBackground(Color.BLACK);
        table.setForeground(Color.WHITE);
        table.setFont(new Font("Monospaced", Font.PLAIN, 16));

        // Load and sort data
        List<String[]> stats = SaveSystem.loadAllData();
        stats.sort((a, b) -> Integer.compare(Integer.parseInt(b[1]), Integer.parseInt(a[1])));

        for (String[] row : stats) {
            model.addRow(row);
        }

        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton backBtn = new JButton("BACK TO MENU");
        backBtn.addActionListener(e -> layout.show(container, "Menu"));
        add(backBtn, BorderLayout.SOUTH);
    }
    
    // Achievement Popup helper
    public static void showAchievement(String message) {
        JOptionPane.showMessageDialog(null, "🏆 ACHIEVEMENT UNLOCKED: " + message, 
            "Milestone!", JOptionPane.INFORMATION_MESSAGE);
    }
}