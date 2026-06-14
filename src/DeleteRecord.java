import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;


/**
 * DELETE FILE:
 * 		- Once "Delete Record" it redirects to this class
 * 		- Lets the admin search for an applicant by ID and permanently delete all records
 */

public class DeleteRecord {
	
    private DeleteRecord() {}
    
    // MAIN INTERFACE OF DELETE RECORD
    public static void open(JFrame parentFrame) {
        JDialog dialog = new JDialog(parentFrame, "Delete Application Record", true);
        dialog.setSize(980, 640);
        dialog.setMinimumSize(new Dimension(720, 480));
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Constants.C_BG);

        // NAV BAR - set to red to indicate "Danger"
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(Constants.C_DANGER);
        nav.setPreferredSize(new Dimension(0, 56));
        nav.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 28));
        
        // NAV BAR TITLE
        JLabel navTitle = new JLabel("Delete Application Record");
        navTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        navTitle.setForeground(Color.WHITE);
        
        // NAV BAR BACK BUTTON
        JButton backBtn = new JButton("← Back");
        backBtn.setFont(Constants.F_LABEL);
        backBtn.setForeground(Color.WHITE);
        backBtn.setOpaque(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1, true),
            BorderFactory.createEmptyBorder(6, 16, 6, 16)));
        backBtn.addActionListener(e -> dialog.dispose());

        JPanel navLeft  = new JPanel(new FlowLayout(FlowLayout.LEFT,  0, 16)); navLeft.setOpaque(false);  navLeft.add(navTitle);
        JPanel navRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 14)); navRight.setOpaque(false); navRight.add(backBtn);
        nav.add(navLeft,  BorderLayout.WEST);
        nav.add(navRight, BorderLayout.EAST);

        // WARNING LABEL
        JPanel banner = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        banner.setBackground(Constants.C_DANGER_BG);
        banner.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Constants.C_DANGER_BORDER));
        
        JLabel warnText = new JLabel(
            "<html><b>Danger zone.</b>  " +
            "Deleting a record permanently removes the applicant and all linked " +
            "parent / emergency-contact rows.</html>");
        warnText.setFont(Constants.F_SMALL);
        warnText.setForeground(Constants.C_DANGER);

        banner.add(warnText);

        // SEARCH FOR APPLICANT ID TO DELETE
        JTextField idField = new JTextField();
        idField.setFont(Constants.F_BODY);
        idField.setBackground(Constants.C_SURFACE);
        idField.setForeground(Constants.C_TEXT_PRIMARY);
        idField.setCaretColor(Constants.C_TEXT_PRIMARY);
        idField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Constants.C_BORDER, 1, true),
            BorderFactory.createEmptyBorder(7, 11, 7, 11)));
        idField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                idField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Constants.C_DANGER, 1, true),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)));
            }
            public void focusLost(FocusEvent e) {
                idField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Constants.C_BORDER, 1, true),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)));
            }
        });
        
        // DELETE BUTTON 
        JButton deleteBtn = new JButton("Delete Record");
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deleteBtn.setBackground(Constants.C_DANGER);
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setOpaque(true);
        deleteBtn.setContentAreaFilled(true);
        deleteBtn.setBorderPainted(false);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.setBorder(BorderFactory.createEmptyBorder(9, 22, 9, 22));
        deleteBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { deleteBtn.setBackground(new Color(185, 28, 28)); }
            public void mouseExited (MouseEvent e) { deleteBtn.setBackground(Constants.C_DANGER); }
        });
        
        // INPUT BAR
        JLabel idLabel = new JLabel("Applicant ID to delete:");
        idLabel.setFont(Constants.F_LABEL);
        idLabel.setForeground(Constants.C_TEXT_MUTED);

        JPanel inputRow = new JPanel(new BorderLayout(10, 0));
        inputRow.setOpaque(false);
        inputRow.add(idLabel, BorderLayout.WEST);
        inputRow.add(idField, BorderLayout.CENTER);
        inputRow.add(deleteBtn,BorderLayout.EAST);

        JLabel hint = new JLabel("Find the Applicant ID in the table below, then type it above.");
        hint.setFont(Constants.F_CAPTION);
        hint.setForeground(Constants.C_TEXT_LIGHT);

        JPanel searchPanel = new JPanel(new BorderLayout(12, 0));
        searchPanel.setBackground(Constants.C_SURFACE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Constants.C_BORDER),
            BorderFactory.createEmptyBorder(14, 24, 14, 24)));
        searchPanel.add(inputRow, BorderLayout.CENTER);
        searchPanel.add(hint,     BorderLayout.SOUTH);

        // RECORDS TABLE FOR REFERENCE
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; } // matching the method of isCellEditable 
        };																			// from the other Class

        JTable table = new JTable(tableModel);
        table.setFont(Constants.F_BODY);
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(255, 220, 220));
        table.setSelectionForeground(Constants.C_TEXT_PRIMARY);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setBackground(Constants.C_SURFACE);
        table.setForeground(Constants.C_TEXT_PRIMARY);

        // HIGHLIGHT ROW OF MATCHING ID
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                String typed = idField.getText().trim();
                Object rowId = t.getValueAt(row, 0);
                boolean match = !typed.isEmpty() && rowId != null
                                && rowId.toString().equalsIgnoreCase(typed);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Constants.C_BORDER),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)));
                if (sel) {
                    setBackground(new Color(255, 200, 200));
                    setForeground(Constants.C_TEXT_PRIMARY);
                } else if (match) {
                    setBackground(new Color(255, 241, 241));
                    setForeground(Constants.C_DANGER);
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    setBackground(row % 2 == 0 ? Constants.C_SURFACE : Constants.C_BG);
                    setForeground(Constants.C_TEXT_PRIMARY);
                    setFont(Constants.F_BODY);
                }
                return this;
            }
        });

        UIFactory.styleTableHeader(table, Constants.C_DANGER);

        // When row is clicked, it auto fills the search bar
        table.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                Object val = tableModel.getValueAt(table.getSelectedRow(), 0);
                if (val != null) idField.setText(val.toString());
                table.repaint();
            }
        });

        idField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { table.repaint(); }
        });

        // LOAD TABLE DATA FROM DBUtil Connection - refers to the database
        Runnable loadTable = () -> {
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);
            try (Connection conn = DBUtil.getConnection(); // in the DBUtil Class
                 Statement  st   = conn.createStatement();
                 ResultSet  rs   = st.executeQuery(
                     "SELECT applicant_id, name, date_of_birth, sex, occupation, " +
                     "present_address FROM applicant_t ORDER BY applicant_id")) { // 
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                for (int i = 1; i <= cols; i++) tableModel.addColumn(meta.getColumnLabel(i));
                while (rs.next()) {
                    Object[] row = new Object[cols];
                    for (int i = 1; i <= cols; i++) row[i - 1] = rs.getObject(i);
                    tableModel.addRow(row);
                }
                UIFactory.autoSizeColumns(table, tableModel, cols, 900);
            } catch (SQLException ex) { ex.printStackTrace(); }
        };
        loadTable.run();

        JScrollPane tableScroll = new JScrollPane(table,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScroll.setBorder(BorderFactory.createLineBorder(Constants.C_BORDER, 1));
        tableScroll.getViewport().setBackground(Constants.C_SURFACE);

        JPanel tableHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        tableHeader.setBackground(Constants.C_BG);
        tableHeader.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));
        JLabel tableLabel = new JLabel(
            "Existing applicant records  —  click a row to select, or type the ID above");
        tableLabel.setFont(Constants.F_SMALL);
        tableLabel.setForeground(Constants.C_TEXT_MUTED);
        tableHeader.add(tableLabel);

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Constants.C_BG);
        tableWrapper.setBorder(BorderFactory.createEmptyBorder(0, 24, 20, 24));
        tableWrapper.add(tableHeader, BorderLayout.NORTH);
        tableWrapper.add(tableScroll, BorderLayout.CENTER);

        // DELETING
        // Checks if a record is selected
        Runnable doDelete = () -> {
            String targetId = idField.getText().trim();
            if (targetId.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "Please enter an Applicant ID.", "No ID entered",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // selects the applicant 
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                     "SELECT name FROM applicant_t WHERE applicant_id = ?")) {
                ps.setString(1, targetId);
                ResultSet rs = ps.executeQuery();

                if (!rs.next()) {
                    JOptionPane.showMessageDialog(dialog,
                        "<html>No record found with Applicant ID <b>" + targetId + "</b>.<br>" +
                        "Please verify the ID and try again.</html>",
                        "Record Not Found", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String applicantName = rs.getString("name");

                // Confirmation panel
                JPanel confirmPanel = new JPanel();
                confirmPanel.setLayout(new BoxLayout(confirmPanel, BoxLayout.Y_AXIS));
                confirmPanel.setBackground(Constants.C_SURFACE);
                confirmPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));
                
                // VALIDATION BEFORE DELETING
                JLabel iconLbl = new JLabel("!", JLabel.CENTER);
                iconLbl.setFont(new Font("Segoe UI", Font.PLAIN, 36));
                iconLbl.setForeground(Constants.C_DANGER);
                iconLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel headLbl = new JLabel("Delete this record?", JLabel.CENTER);
                headLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
                headLbl.setForeground(Constants.C_TEXT_PRIMARY);
                headLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
                
                // html for placement 
                JLabel detailLbl = new JLabel(
                    "<html><div style='text-align:center;width:320px;'>" +
                    "You are about to permanently delete:<br><br>" +
                    "<b>" + applicantName + "</b><br>" +
                    "<span style='color:#64748b;font-size:11px;'>Applicant ID: " + targetId + "</span><br><br>" +
                    "This will also remove all linked<br>" +
                    "<b>parent records</b> and <b>emergency contacts</b>.<br><br>" +
                    "<span style='color:#dc2626;'>This action cannot be undone.</span>" +
                    "</div></html>", JLabel.CENTER);
                detailLbl.setFont(Constants.F_BODY);
                detailLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

                confirmPanel.add(iconLbl);
                confirmPanel.add(Box.createVerticalStrut(8));
                confirmPanel.add(headLbl);
                confirmPanel.add(Box.createVerticalStrut(10));
                confirmPanel.add(detailLbl);

                Object[] options = { "Yes, delete permanently", "No, cancel" };
                int choice = JOptionPane.showOptionDialog(dialog, confirmPanel,
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, options, options[1]);

                if (choice != JOptionPane.YES_OPTION) return;

                // Execute deletion in a transaction
                try (Connection delConn = DBUtil.getConnection()) { // connection to database
                    delConn.setAutoCommit(false);
                    try {
                        try (PreparedStatement d = delConn.prepareStatement(
                                "DELETE FROM emergency_t WHERE applicant_id = ?")) { // query to delete emergency information
                            d.setString(1, targetId); d.executeUpdate();
                        }
                        try (PreparedStatement d = delConn.prepareStatement(
                                "DELETE FROM parent_t WHERE applicant_id = ?")) { // query to delete parent information
                            d.setString(1, targetId); d.executeUpdate();
                        }
                        try (PreparedStatement d = delConn.prepareStatement(
                                "DELETE FROM applicant_t WHERE applicant_id = ?")) { // delete applicant information
                            d.setString(1, targetId); d.executeUpdate();
                        }
                        delConn.commit();
                        
                        // Deletion Successful
                        JOptionPane.showMessageDialog(dialog,
                            "<html><b>" + applicantName + "</b> (ID: " + targetId + ")<br>" +
                            "has been permanently deleted along with all linked records.</html>",
                            "Record Deleted", JOptionPane.INFORMATION_MESSAGE);

                        idField.setText("");
                        loadTable.run();
                        table.clearSelection();

                    } catch (SQLException ex) {
                        delConn.rollback();
                        throw ex;
                    }
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog,
                    "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        };

        deleteBtn.addActionListener(e -> doDelete.run());
        idField.addActionListener(e -> doDelete.run());

        // LAYOUT ASSEMBLING
        JPanel topStrip = new JPanel(new BorderLayout());
        topStrip.setBackground(Constants.C_BG);
        topStrip.add(banner,      BorderLayout.NORTH);
        topStrip.add(searchPanel, BorderLayout.CENTER);

        JPanel centerStack = new JPanel(new BorderLayout());
        centerStack.setBackground(Constants.C_BG);
        centerStack.add(topStrip,     BorderLayout.NORTH);
        centerStack.add(tableWrapper, BorderLayout.CENTER);

        root.add(nav,         BorderLayout.NORTH);
        root.add(centerStack, BorderLayout.CENTER);

        dialog.add(root);
        dialog.setVisible(true);
    }
}