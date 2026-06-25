package ui;

import model.ResultRecord;
import model.User;
import service.ResultService;
import service.ServiceException;
import ui.MainFrame;
import java.awt.Color;
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

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(15, 23, 42));

        JLabel title = new JLabel(
                "Previous Results - " + user.getUsername(),
                SwingConstants.CENTER
        );
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        root.add(title, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Date", "Topic", "Score"}, 0
        );

        JTable table = new JTable(model);
        table.setRowHeight(25);

        JScrollPane scroll = new JScrollPane(table);
        root.add(scroll, BorderLayout.CENTER);

        JButton back = new JButton("Back");
            back.addActionListener(e -> {
                MainFrame.get().showCard(MainFrame.DASHBOARD);
                dispose();
            });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(back);

        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);

        loadResults(model);

        setVisible(true);
    }

    private void loadResults(DefaultTableModel model) {

        try {
            List<ResultRecord> list =
                    resultService.getPreviousResults(user.getUsername());

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            for (ResultRecord r : list) {
                model.addRow(new Object[]{
                        r.getTestDate() == null ? "" : df.format(r.getTestDate()),
                        r.getTopic(),
                        r.getScorePercent()
                });
            }

        } catch (SQLException | ServiceException e) {
            JOptionPane.showMessageDialog(this, "Error loading results");
        }
    }
}
