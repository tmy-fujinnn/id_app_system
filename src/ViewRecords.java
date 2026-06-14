import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.sql.*;

/**
 * VIEW RECORDS INTERFACE
 * Features:
 *  - Three tabs: Applicants, Parent Info, Emergency Contacts
 *  - Live search/filter bar per tab
 *  - Row count label ("Showing N of N records")
 *  - CSV export button
 */

public class ViewRecords {

    private ViewRecords() {}

    // ── Column header mappings ─────────────────────────────────────────────────
    private static final String[][] APPLICANT_COLS = {
        {"applicant_id",       "Applicant ID"}, // DB name → display name
        {"name",               "Full Name"},
        {"maiden_name",        "Maiden Name"},
        {"date_of_birth",      "Date of Birth"},
        {"place_of_birth",     "Place of Birth"},
        {"present_address",    "Present Address"},
        {"permanent_address",  "Permanent Address"},
        {"sex",                "Sex"},
        {"civil_status",       "Civil Status"},
        {"blood_type",         "Blood Type"},
        {"religion",           "Religion"},
        {"nationality",        "Nationality"},
        {"pwd",                "PWD"},
        {"solo_parent",        "Solo Parent"},
        {"occupation",         "Occupation"},
        {"personal_mobile",    "Personal No."},
        {"personal_email",     "Personal Email"},
        {"relative_contact_no","Relative Contact"},
        {"relative_email",     "Relative Email"},
        {"height_cm",          "Height (cm)"},
        {"weight_kg",          "Weight (kg)"},
        {"hair_color",         "Hair Color"},
        {"eye_color",          "Eye Color"},
        {"other_marks",        "Other Marks"},
        {"status",             "Status"},
    };

    private static final String[][] PARENT_COLS = {
        {"parent_id",    "Parent ID"}, // DB name → display name
        {"applicant_id", "Applicant ID"},
        {"parent_type",  "Type"},
        {"is_unknown",   "Unknown/Deceased"},
        {"name",         "Full Name"},
        {"married_ln",   "Married Last Name"},
        {"contact_no",   "Contact No."},
        {"address",      "Address"},
    };

    private static final String[][] EMERGENCY_COLS = {
        {"applicant_id",    "Applicant ID"}, // DB name → display name
        {"ec_name",         "Full Name"},
        {"ec_contact_no",   "Contact No."},
        {"ec_relationship", "Relationship"},
        {"ec_address",      "Address"},
    };

    // ── Entry point ───────────────────────────────────────────────────────────
    public static void open(JFrame parentFrame) {
        JFrame viewFrame = new JFrame("All Applications");
        viewFrame.setSize(1160, 680);
        viewFrame.setMinimumSize(new Dimension(860, 480));
        viewFrame.setLocationRelativeTo(parentFrame);
        viewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Constants.C_BG);

        root.add(buildNav(viewFrame), BorderLayout.NORTH);
        root.add(buildBody(viewFrame), BorderLayout.CENTER);

        viewFrame.add(root);
        viewFrame.setVisible(true);
    }

    // ── Nav bar ───────────────────────────────────────────────────────────────
    private static JPanel buildNav(JFrame frame) {
        JPanel nav = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(
                    0, 0, new Color(20, 47, 93),
                    getWidth(), 0, Constants.C_HEADER_BG));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        nav.setOpaque(false);
        nav.setPreferredSize(new Dimension(0, 58));
        nav.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 28));

        JLabel title = new JLabel("All Applications");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(Color.WHITE);

        JButton back = UIFactory.createOutlineButton("\u2190 Back");
        back.addActionListener(e -> frame.dispose());

        JPanel left  = new JPanel(new FlowLayout(FlowLayout.LEFT,  0, 16));
        left.setOpaque(false); left.add(title);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 14));
        right.setOpaque(false); right.add(back);

        nav.add(left,  BorderLayout.WEST);
        nav.add(right, BorderLayout.EAST);
        return nav;
    }

    // ── Body with tabs ────────────────────────────────────────────────────────
    private static JPanel buildBody(JFrame frame) {
        JPanel body = new JPanel(new BorderLayout());
        body.setBackground(Constants.C_BG);
        body.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(Constants.F_LABEL);
        tabs.setBackground(Constants.C_BG);
        tabs.setFocusable(false);

        tabs.addTab("  Applicants  ",
            buildTab(frame,
                "SELECT applicant_id, name, maiden_name, date_of_birth, " +
                "place_of_birth, sex, civil_status, blood_type, religion, " +
                "nationality, pwd, solo_parent, occupation, " +
                "personal_mobile, personal_email, relative_contact_no, " +
                "relative_email, height_cm, weight_kg, hair_color, " +
                "eye_color, other_marks, present_address, permanent_address " +
                "FROM applicant_t ORDER BY applicant_id",
                APPLICANT_COLS, true));

        tabs.addTab("  Parent Info  ",
            buildTab(frame,
                "SELECT parent_id, applicant_id, parent_type, is_unknown, " +
                "name, married_ln, contact_no, address FROM parent_t " +
                "ORDER BY applicant_id",
                PARENT_COLS, false));

        tabs.addTab("  Emergency Contacts  ",
            buildTab(frame,
                "SELECT applicant_id, ec_name, ec_contact_no, " +
                "ec_relationship, ec_address FROM emergency_t " +
                "ORDER BY applicant_id",
                EMERGENCY_COLS, false));

        body.add(tabs, BorderLayout.CENTER);
        return body;
    }

    // ── Tab panel: search bar + table + detail panel ──────────────────────────
    private static JPanel buildTab(JFrame frame, String sql,
                                   String[][] colMap, boolean isApplicant) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Constants.C_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        // ── Table model ────────────────────────────────────────────────────────
        DefaultTableModel model = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        // ── Load data from DB ──────────────────────────────────────────────────
        String[] dbCols   = new String[colMap.length];
        String[] dispCols = new String[colMap.length];
        for (int i = 0; i < colMap.length; i++) {
            dbCols[i]   = colMap[i][0];
            dispCols[i] = colMap[i][1];
        }

        java.util.List<Object[]> allRows = new java.util.ArrayList<>();
        java.util.List<String>   actualCols = new java.util.ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             Statement  st   = conn.createStatement();
             ResultSet  rs   = st.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            // Map DB column names → display names
            for (int i = 1; i <= colCount; i++) {
                String dbName = meta.getColumnLabel(i).toLowerCase();
                String display = dbName; // default: raw name
                for (String[] mapping : colMap)
                    if (mapping[0].equalsIgnoreCase(dbName)) { display = mapping[1]; break; }
                actualCols.add(display);
                model.addColumn(display);
            }

            while (rs.next()) {
                Object[] row = new Object[colCount];
                for (int i = 1; i <= colCount; i++) row[i - 1] = rs.getObject(i);
                allRows.add(row);
                model.addRow(row);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            model.addColumn("Error");
            model.addRow(new Object[]{ ex.getMessage() });
        }

        // Filter model (wraps real model) 
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);

        // Create Table
        JTable table = new JTable(model);
        table.setFont(Constants.F_BODY);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(Constants.C_TEXT_PRIMARY);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setBackground(Constants.C_SURFACE);
        table.setForeground(Constants.C_TEXT_PRIMARY);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Status badge renderer (only for applicant tab)
        int statusColIdx = -1;
        for (int i = 0; i < model.getColumnCount(); i++)
            if (model.getColumnName(i).equalsIgnoreCase("Status"))
                { statusColIdx = i; break; }
        final int statusCol = statusColIdx;

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)),
                    BorderFactory.createEmptyBorder(0, 14, 0, 14)));
                setBackground(sel
                    ? new Color(219, 234, 254)
                    : (row % 2 == 0 ? Constants.C_SURFACE : new Color(250, 252, 255)));
                setForeground(Constants.C_TEXT_PRIMARY);
                setFont(Constants.F_BODY);

                // Status badge
                int modelCol = t.convertColumnIndexToModel(col);
                if (isApplicant && modelCol == statusCol && val != null) {
                    String status = val.toString();
                    switch (status) {
                        case "Approved" -> setForeground(new Color(22, 163, 74));
                        case "Rejected" -> setForeground(Constants.C_DANGER);
                        default         -> setForeground(new Color(180, 120, 0));
                    }
                    setFont(Constants.F_BODY.deriveFont(Font.BOLD));
                    setText("● " + status);
                }
                return this;
            }
        });

        // Header
        UIFactory.styleTableHeader(table, Constants.C_HEADER_BG);

        // Auto-size columns
        int colCount = model.getColumnCount();
        UIFactory.autoSizeColumns(table, model, colCount, 1100);

        JScrollPane tableScroll = new JScrollPane(table,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScroll.setBorder(BorderFactory.createLineBorder(Constants.C_BORDER, 1));
        tableScroll.getViewport().setBackground(Constants.C_SURFACE);

        // Searchbar and toolbar
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setBackground(Constants.C_BG);

        // Search field
        JTextField searchField = new JTextField();
        searchField.setFont(Constants.F_BODY);
        searchField.setBackground(Constants.C_SURFACE);
        searchField.setForeground(Constants.C_TEXT_PRIMARY);
        searchField.setCaretColor(Constants.C_TEXT_PRIMARY);
        searchField.putClientProperty("JTextField.placeholderText",
            "Search by name, ID, or any field...");
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Constants.C_BORDER, 1, true),
            BorderFactory.createEmptyBorder(7, 12, 7, 12)));
        searchField.setPreferredSize(new Dimension(300, 34));
        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                searchField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Constants.C_ACCENT, 1, true),
                    BorderFactory.createEmptyBorder(7, 12, 7, 12)));
            }
            public void focusLost(FocusEvent e) {
                searchField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Constants.C_BORDER, 1, true),
                    BorderFactory.createEmptyBorder(7, 12, 7, 12)));
            }
        });

        // Record count label
        JLabel countLbl = new JLabel("Showing " + model.getRowCount() +
                                     " of " + model.getRowCount() + " records");
        countLbl.setFont(Constants.F_SMALL);
        countLbl.setForeground(Constants.C_TEXT_MUTED);

        // Live filter
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { filter(); }
            public void removeUpdate(DocumentEvent e)  { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }
            void filter() {
                String q = searchField.getText().trim();
                if (q.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter(
                        "(?i)" + java.util.regex.Pattern.quote(q)));
                }
                countLbl.setText("Showing " + table.getRowCount() +
                                 " of " + model.getRowCount() + " records");
            }
        });

        // Export CSV button
        JButton exportBtn = UIFactory.createPrimaryButton("Export CSV");
        exportBtn.setFont(Constants.F_SMALL.deriveFont(Font.BOLD));
        exportBtn.setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));
        exportBtn.addActionListener(e -> exportCSV(table, model, frame));

        JPanel searchLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchLeft.setBackground(Constants.C_BG);
        searchLeft.add(new JLabel("\uD83D\uDD0D "));  // magnifier emoji
        searchLeft.add(searchField);
        searchLeft.add(countLbl);

        JPanel searchRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchRight.setBackground(Constants.C_BG);
        searchRight.add(exportBtn);

        toolbar.add(searchLeft,  BorderLayout.WEST);
        toolbar.add(searchRight, BorderLayout.EAST);

        // Detail Panel
        JPanel detailPanel = buildDetailPanel(table, model, isApplicant);

        table.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                refreshDetailPanel(detailPanel, table, model, modelRow,
                                   actualCols, isApplicant, statusCol);
                detailPanel.setVisible(true);
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            tableScroll, detailPanel);
        split.setResizeWeight(0.65);
        split.setBorder(null);
        split.setDividerSize(6);
        split.setBackground(Constants.C_BG);
        detailPanel.setVisible(false);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(split,   BorderLayout.CENTER);
        return panel;
    }

    // Detail panel 
    private static JPanel buildDetailPanel(JTable table, DefaultTableModel model,
                                           boolean isApplicant) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Constants.C_SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Constants.C_BORDER),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        // Header bar
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(248, 250, 252));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Constants.C_BORDER),
            BorderFactory.createEmptyBorder(10, 18, 10, 18)));

        JLabel headerLbl = new JLabel("Record Details");
        headerLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        headerLbl.setForeground(Constants.C_PRIMARY);

        JButton closeBtn = new JButton("✕");
        closeBtn.setFont(Constants.F_SMALL);
        closeBtn.setForeground(Constants.C_TEXT_MUTED);
        closeBtn.setBackground(Constants.C_BG);
        closeBtn.setBorderPainted(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> {
            panel.setVisible(false);
            table.clearSelection();
        });

        header.add(headerLbl, BorderLayout.WEST);
        header.add(closeBtn,  BorderLayout.EAST);

        // Fields area (will be populated on row click)
        JPanel fieldsArea = new JPanel();
        fieldsArea.setName("fieldsArea");
        fieldsArea.setBackground(Constants.C_SURFACE);
        fieldsArea.setLayout(new WrapLayout(FlowLayout.LEFT, 16, 10));
        fieldsArea.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JScrollPane fieldsScroll = new JScrollPane(fieldsArea,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        fieldsScroll.setBorder(null);
        fieldsScroll.getViewport().setBackground(Constants.C_SURFACE);

        panel.add(header,       BorderLayout.NORTH);
        panel.add(fieldsScroll, BorderLayout.CENTER);
        return panel;
    }

    private static void refreshDetailPanel(JPanel detailPanel, JTable table,
                                           DefaultTableModel model, int modelRow,
                                           java.util.List<String> colNames,
                                           boolean isApplicant, int statusCol) {
        // Find the fields area inside the scroll pane
        JScrollPane scroll = (JScrollPane) detailPanel.getComponent(1);
        JPanel fieldsArea  = (JPanel) scroll.getViewport().getView();
        fieldsArea.removeAll();

        for (int col = 0; col < model.getColumnCount(); col++) {
            Object val = model.getValueAt(modelRow, col);
            String display = (val != null && !val.toString().trim().isEmpty())
                ? val.toString() : "—";
            String colName = model.getColumnName(col);

            JPanel chip = new JPanel();
            chip.setLayout(new BoxLayout(chip, BoxLayout.Y_AXIS));
            chip.setBackground(new Color(248, 250, 252));
            chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Constants.C_BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

            JLabel keyLbl = new JLabel(colName.toUpperCase());
            keyLbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
            keyLbl.setForeground(Constants.C_TEXT_LIGHT);

            JLabel valLbl = new JLabel(display);
            valLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            // Colour status value
            if (isApplicant && col == statusCol && val != null) {
                switch (val.toString()) {
                    case "Approved" -> valLbl.setForeground(new Color(22, 163, 74));
                    case "Rejected" -> valLbl.setForeground(Constants.C_DANGER);
                    default         -> valLbl.setForeground(new Color(180, 120, 0));
                }
                valLbl.setFont(valLbl.getFont().deriveFont(Font.BOLD));
            } else {
                valLbl.setForeground(Constants.C_TEXT_PRIMARY);
            }

            chip.add(keyLbl);
            chip.add(Box.createVerticalStrut(3));
            chip.add(valLbl);
            fieldsArea.add(chip);
        }

        fieldsArea.revalidate();
        fieldsArea.repaint();
    }

    // CSV Export 
    private static void exportCSV(JTable table, DefaultTableModel model,
                                  JFrame frame) {
        StringBuilder sb = new StringBuilder();

        // Headers
        for (int col = 0; col < model.getColumnCount(); col++) {
            sb.append('"').append(model.getColumnName(col)).append('"');
            if (col < model.getColumnCount() - 1) sb.append(',');
        }
        sb.append('\n');

        // Visible rows only (respects current filter)
        for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            for (int col = 0; col < model.getColumnCount(); col++) {
                Object val = model.getValueAt(modelRow, col);
                String cell = val != null ? val.toString().replace("\"", "\"\"") : "";
                sb.append('"').append(cell).append('"');
                if (col < model.getColumnCount() - 1) sb.append(',');
            }
            sb.append('\n');
        }

        // Copy to clipboard
        Toolkit.getDefaultToolkit().getSystemClipboard()
            .setContents(new StringSelection(sb.toString()), null);

        // Ask to save as file too
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save CSV");
        fc.setSelectedFile(new java.io.File("applicants_export.csv"));
        if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try (java.io.FileWriter fw =
                     new java.io.FileWriter(fc.getSelectedFile())) {
                fw.write(sb.toString());
                JOptionPane.showMessageDialog(frame,
                    "CSV saved to:\n" + fc.getSelectedFile().getAbsolutePath(),
                    "Export successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (java.io.IOException ex) {
                JOptionPane.showMessageDialog(frame,
                    "Failed to save: " + ex.getMessage(),
                    "Export error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // WrapLayout helper (FlowLayout that wraps properly in scroll panes)
    static class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }
        @Override public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }
        @Override public Dimension minimumLayoutSize(Container target) {
            return layoutSize(target, false);
        }
        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;
                int hgap = getHgap(), vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);
                int x = 0, y = insets.top + vgap, rowHeight = 0;
                for (Component m : target.getComponents()) {
                    if (!m.isVisible()) continue;
                    Dimension d = preferred ? m.getPreferredSize() : m.getMinimumSize();
                    if (x == 0 || (x + d.width) <= maxWidth) {
                        x += d.width + hgap;
                    } else {
                        y += rowHeight + vgap;
                        x = d.width + hgap;
                        rowHeight = 0;
                    }
                    rowHeight = Math.max(rowHeight, d.height);
                }
                y += rowHeight + vgap + insets.bottom;
                return new Dimension(targetWidth, y);
            }
        }
    }
}