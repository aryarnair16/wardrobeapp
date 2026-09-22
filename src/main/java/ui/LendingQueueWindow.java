package ui;

import db.RequestDAO;
import model.BorrowRequest;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LendingQueueWindow extends JFrame {

    private int currentUserId;
    private JTable table;
    private DefaultTableModel tableModel;

    public LendingQueueWindow() {
        this(1);
    }

    public LendingQueueWindow(int currentUserId) {
        this.currentUserId = currentUserId;
        initComponents();
        loadRequests();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setTitle("Who Wants to Borrow My Clothes?");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("Pending Borrow Requests", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Request ID", "Item Name", "Requested By"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout());
        JButton approveBtn = new JButton("Approve");
        approveBtn.setBackground(new Color(46, 125, 50));
        approveBtn.setForeground(Color.WHITE);
        approveBtn.addActionListener(e -> approveRequest());

        btnPanel.add(approveBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadRequests() {
        tableModel.setRowCount(0);
        List<BorrowRequest> pending = RequestDAO.getPendingForOwner(currentUserId);
        if (pending != null) {
            for (BorrowRequest r : pending) {
                tableModel.addRow(new Object[]{
                        r.getRequestId(),
                        r.getItemName(),
                        r.getRequesterName()
                });
            }
        }
    }

    private void approveRequest() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a request to approve.");
            return;
        }

        int requestId = (int) tableModel.getValueAt(selectedRow, 0);
        boolean success = RequestDAO.approve(requestId);
        if (success) {
            JOptionPane.showMessageDialog(this, "Request Approved!");
            loadRequests();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to approve request.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LendingQueueWindow(1).setVisible(true));
    }
}