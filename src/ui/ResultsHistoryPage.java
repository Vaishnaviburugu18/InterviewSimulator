package ui;

import model.ResultRecord;
import model.User;
import service.ResultService;
import service.ServiceException;
import service.ValidationException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class ResultsHistoryPage extends JFrame {
    private final User user;
    private final ResultService resultService = new ResultService();

    public ResultsHistoryPage(User user) {
        this.user = user;

        setTitle("Previous Results");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(new Color(15, 23, 42));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Previous Results for: " + user.getUsername(), SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        root.add(title, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
                new Object[] {"Date", "Topic", "Score %"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 75, 120)));
        root.add(scroll, BorderLayout.CENTER);

        JButton backBtn = new JButton("Back to Dashboard");
        backBtn.setFont(new Font("Arial", Font.BOLD, 14));
        backBtn.addActionListener(e -> {
            new Dashboard(user).setVisible(true);
            dispose();
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(backBtn);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);

        loadResultsInto(model);
        setVisible(true);
    }

    private void loadResultsInto(DefaultTableModel model) {
        try {
            List<ResultRecord> records = resultService.getPreviousResults(user.getUsername());
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (ResultRecord r : records) {
                String dateStr = r.getTestDate() == null ? "" : df.format(r.getTestDate());
                model.addRow(new Object[] {dateStr, r.getTopic(), r.getScorePercent()});
            }
            if (records.isEmpty()) {
                model.addRow(new Object[] {"", "No results yet", ""});
            }
        } catch (ServiceException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

