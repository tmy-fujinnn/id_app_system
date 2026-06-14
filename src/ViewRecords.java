import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * VIEW RECORDS INTERFACE
 * Features:
 * - Three tabs: Applicants, Parent Info, Emergency Contacts
 * - Live search/filter bar per tab
 * - Row count label ("Showing N of N records")
 * - CSV export button
 * - Column sorting (click header = ASC, click again = DESC, click again =
 * unsorted)
 * - Filter bar on Applicants tab: Sex, Civil Status, Age range
 */

public class ViewRecords {

    private ViewRecords() {
    }

    // ── Column header mappings ────────────────────────────────────────────────
    private static final String[][] APPLICANT_COLS = {
            { "applicant_id", "Applicant ID" },
            { "name", "Full Name" },
            { "maiden_name", "Maiden Name" },
            { "date_of_birth", "Date of Birth" },
            { "place_of_birth", "Place of Birth" },
            { "present_address", "Present Address" },
            { "permanent_address", "Permanent Address" },
            { "sex", "Sex" },
            { "civil_status", "Civil Status" },
            { "blood_type", "Blood Type" },
            { "religion", "Religion" },
            { "nationality", "Nationality" },
            { "pwd", "PWD" },
            { "solo_parent", "Solo Parent" },
            { "occupation", "Occupation" },
            { "personal_mobile", "Personal No." },
            { "personal_email", "Personal Email" },
            { "relative_contact_no", "Relative Contact" },
            { "relative_email", "Relative Email" },
            { "height_cm", "Height (cm)" },
            { "weight_kg", "Weight (kg)" },
            { "hair_color", "Hair Color" },
            { "eye_color", "Eye Color" },
            { "other_marks", "Other Marks" },
            { "status", "Status" },
    };

    private static final String[][] PARENT_COLS = {
            { "parent_id", "Parent ID" },
            { "applicant_id", "Applicant ID" },
            { "parent_type", "Type" },
            { "is_unknown", "Unknown/Deceased" },
            { "name", "Full Name" },
            { "married_ln", "Married Last Name" },
            { "contact_no", "Contact No." },
            { "address", "Address" },
    };

    private static final String[][] EMERGENCY_COLS = {
            { "applicant_id", "Applicant ID" },
            { "ec_name", "Full Name" },
            { "ec_contact_no", "Contact No." },
            { "ec_relationship", "Relationship" },
            { "ec_address", "Address" },
    };

    // ── Entry point ───────────────────────────────────────────────────────────
    public static void open(JFrame parentFrame) {
        JFrame viewFrame = new JFrame("All Applications");
        viewFrame.setSize(1160, 700);
        viewFrame.setMinimumSize(new Dimension(860, 500));
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
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(20, 47, 93),
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

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 16));
        left.setOpaque(false);
        left.add(title);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 14));
        right.setOpaque(false);
        right.add(back);

        nav.add(left, BorderLayout.WEST);
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
                buildApplicantTab(frame));

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

    // ── Applicant tab (has extra filter bar) ──────────────────────────────────
    private static JPanel buildApplicantTab(JFrame frame) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Constants.C_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        // ── Load data ─────────────────────────────────────────────────────────
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        List<String> actualCols = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(
                        "SELECT applicant_id, name, maiden_name, date_of_birth, " +
                                "place_of_birth, sex, civil_status, blood_type, religion, " +
                                "nationality, pwd, solo_parent, occupation, " +
                                "personal_mobile, personal_email, relative_contact_no, " +
                                "relative_email, height_cm, weight_kg, hair_color, " +
                                "eye_color, other_marks, present_address, permanent_address " +
                                "FROM applicant_t ORDER BY applicant_id")) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            for (int i = 1; i <= colCount; i++) {
                String dbName = meta.getColumnLabel(i).toLowerCase();
                String display = dbName;
                for (String[] m : APPLICANT_COLS)
                    if (m[0].equalsIgnoreCase(dbName)) {
                        display = m[1];
                        break;
                    }
                actualCols.add(display);
                model.addColumn(display);
            }
            while (rs.next()) {
                Object[] row = new Object[colCount];
                for (int i = 1; i <= colCount; i++)
                    row[i - 1] = rs.getObject(i);
                model.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            model.addColumn("Error");
            model.addRow(new Object[] { ex.getMessage() });
        }

        // Find column indexes needed for filtering
        int sexColIdx = -1, csColIdx = -1, dobColIdx = -1, statusColIdx = -1;
        for (int i = 0; i < model.getColumnCount(); i++) {
            String n = model.getColumnName(i);
            if (n.equals("Sex"))
                sexColIdx = i;
            if (n.equals("Civil Status"))
                csColIdx = i;
            if (n.equals("Date of Birth"))
                dobColIdx = i;
            if (n.equals("Status"))
                statusColIdx = i;
        }
        final int sexCol = sexColIdx, csCol = csColIdx,
                dobCol = dobColIdx, statusCol = statusColIdx;

        // ── Sorter ────────────────────────────────────────────────────────────
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);

        // ── Table ─────────────────────────────────────────────────────────────
        JTable table = buildStyledTable(model, sorter, true, statusCol);
        UIFactory.styleTableHeader(table, Constants.C_HEADER_BG);
        UIFactory.autoSizeColumns(table, model, model.getColumnCount(), 1100);

        JScrollPane tableScroll = new JScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScroll.setBorder(BorderFactory.createLineBorder(Constants.C_BORDER, 1));
        tableScroll.getViewport().setBackground(Constants.C_SURFACE);

        // ── Record count ──────────────────────────────────────────────────────
        JLabel countLbl = new JLabel("Showing " + model.getRowCount() +
                " of " + model.getRowCount() + " records");
        countLbl.setFont(Constants.F_SMALL);
        countLbl.setForeground(Constants.C_TEXT_MUTED);

        // ── Filter combos ─────────────────────────────────────────────────────
        JComboBox<String> cbSex = filterCombo("All Sexes", "Male (M)", "Female (F)");
        JComboBox<String> cbCS = filterCombo("All Civil Statuses",
                "Single (S)", "Married (M)",
                "Widowed (W)", "Divorced (D)");
        JComboBox<String> cbAgeFrom = filterCombo(ageOptions("Min Age"));
        JComboBox<String> cbAgeTo = filterCombo(ageOptions("Max Age"));

        // ── Search field ──────────────────────────────────────────────────────
        JTextField searchField = styledSearchField();

        // ── Apply filter runnable ─────────────────────────────────────────────
        Runnable applyFilter = () -> {
            String q = searchField.getText().trim();
            String sexSel = (String) cbSex.getSelectedItem();
            String csSel = (String) cbCS.getSelectedItem();
            String ageFrom = (String) cbAgeFrom.getSelectedItem();
            String ageTo = (String) cbAgeTo.getSelectedItem();

            // Parse age range
            int minAge = ageFrom.equals("Min Age") ? 0 : Integer.parseInt(ageFrom);
            int maxAge = ageTo.equals("Max Age") ? 150 : Integer.parseInt(ageTo);

            // Sex filter value (M or F)
            String sexFilter = sexSel.equals("All Sexes") ? null
                    : sexSel.startsWith("Male") ? "M" : "F";

            // Civil status filter (S, M, W, D)
            String csFilter = csSel.equals("All Civil Statuses") ? null
                    : String.valueOf(csSel.charAt(csSel.indexOf('(') + 1));

            sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    // Text search across all columns
                    if (!q.isEmpty()) {
                        boolean found = false;
                        for (int c = 0; c < entry.getValueCount(); c++) {
                            Object v = entry.getValue(c);
                            if (v != null && v.toString().toLowerCase()
                                    .contains(q.toLowerCase())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found)
                            return false;
                    }

                    // Sex filter
                    if (sexFilter != null && sexCol >= 0) {
                        Object v = entry.getValue(sexCol);
                        if (v == null || !v.toString().equalsIgnoreCase(sexFilter))
                            return false;
                    }

                    // Civil status filter
                    if (csFilter != null && csCol >= 0) {
                        Object v = entry.getValue(csCol);
                        if (v == null || !v.toString().equalsIgnoreCase(csFilter))
                            return false;
                    }

                    // Age range filter (calculated from date_of_birth)
                    if (dobCol >= 0 && (minAge > 0 || maxAge < 150)) {
                        Object dob = entry.getValue(dobCol);
                        if (dob != null) {
                            try {
                                java.time.LocalDate birth = java.time.LocalDate.parse(
                                        dob.toString().substring(0, 10));
                                int age = java.time.Period.between(
                                        birth, java.time.LocalDate.now()).getYears();
                                if (age < minAge || age > maxAge)
                                    return false;
                            } catch (Exception ignored) {
                            }
                        }
                    }

                    return true;
                }
            });

            countLbl.setText("Showing " + table.getRowCount() +
                    " of " + model.getRowCount() + " records");
        };

        // Wire all filter inputs
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                applyFilter.run();
            }

            public void removeUpdate(DocumentEvent e) {
                applyFilter.run();
            }

            public void changedUpdate(DocumentEvent e) {
                applyFilter.run();
            }
        });
        cbSex.addActionListener(e -> applyFilter.run());
        cbCS.addActionListener(e -> applyFilter.run());
        cbAgeFrom.addActionListener(e -> applyFilter.run());
        cbAgeTo.addActionListener(e -> applyFilter.run());

        // Reset button
        JButton resetBtn = new JButton("Reset Filters");
        resetBtn.setFont(Constants.F_CAPTION.deriveFont(Font.BOLD));
        resetBtn.setBackground(Constants.C_BG);
        resetBtn.setForeground(Constants.C_TEXT_MUTED);
        resetBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Constants.C_BORDER, 1, true),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        resetBtn.setFocusPainted(false);
        resetBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            cbSex.setSelectedIndex(0);
            cbCS.setSelectedIndex(0);
            cbAgeFrom.setSelectedIndex(0);
            cbAgeTo.setSelectedIndex(0);
            sorter.setRowFilter(null);
            sorter.setSortKeys(null);
            countLbl.setText("Showing " + model.getRowCount() +
                    " of " + model.getRowCount() + " records");
        });

        // Export button
        JButton exportBtn = UIFactory.createPrimaryButton("Export CSV");
        exportBtn.setFont(Constants.F_SMALL.deriveFont(Font.BOLD));
        exportBtn.setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));
        exportBtn.addActionListener(e -> exportCSV(table, model, frame));

        // ── Toolbar row 1: search + export ────────────────────────────────────
        JPanel row1 = new JPanel(new BorderLayout(8, 0));
        row1.setBackground(Constants.C_BG);

        JPanel row1Left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row1Left.setBackground(Constants.C_BG);
        row1Left.add(new JLabel("\uD83D\uDD0D "));
        row1Left.add(searchField);
        row1Left.add(countLbl);

        JPanel row1Right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        row1Right.setBackground(Constants.C_BG);
        row1Right.add(exportBtn);

        row1.add(row1Left, BorderLayout.WEST);
        row1.add(row1Right, BorderLayout.EAST);

        // ── Toolbar row 2: filters ─────────────────────────────────────────────
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        row2.setBackground(new Color(241, 245, 249));
        row2.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Constants.C_BORDER, 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)));

        JLabel filterIcon = new JLabel("\u25BC  Filters:");
        filterIcon.setFont(new Font("Segoe UI", Font.BOLD, 11));
        filterIcon.setForeground(Constants.C_TEXT_MUTED);

        row2.add(filterIcon);
        row2.add(makeFilterLabel("Sex:"));
        row2.add(cbSex);
        row2.add(makeFilterLabel("Civil Status:"));
        row2.add(cbCS);
        row2.add(makeFilterLabel("Age from:"));
        row2.add(cbAgeFrom);
        row2.add(makeFilterLabel("to:"));
        row2.add(cbAgeTo);
        row2.add(Box.createHorizontalStrut(8));
        row2.add(resetBtn);

        // ── Sort indicator label ──────────────────────────────────────────────
        JLabel sortLbl = new JLabel("Click any column header to sort  \u2191\u2193");
        sortLbl.setFont(Constants.F_CAPTION);
        sortLbl.setForeground(Constants.C_TEXT_LIGHT);

        JPanel sortRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        sortRow.setBackground(Constants.C_BG);
        sortRow.add(sortLbl);

        // ── Toolbar stack ─────────────────────────────────────────────────────
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
        toolbar.setBackground(Constants.C_BG);
        toolbar.add(row1);
        toolbar.add(Box.createVerticalStrut(6));
        toolbar.add(row2);
        toolbar.add(Box.createVerticalStrut(4));
        toolbar.add(sortRow);

        // ── Detail panel ──────────────────────────────────────────────────────
        JPanel detailPanel = buildDetailPanel(table, model, true);
        table.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                refreshDetailPanel(detailPanel, table, model, modelRow,
                        actualCols, true, statusCol);
                detailPanel.setVisible(true);
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                tableScroll, detailPanel);
        split.setResizeWeight(0.65);
        split.setBorder(null);
        split.setDividerSize(6);
        detailPanel.setVisible(false);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // ── Generic tab (Parent Info, Emergency Contacts) ─────────────────────────
    private static JPanel buildTab(JFrame frame, String sql,
            String[][] colMap, boolean isApplicant) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Constants.C_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        List<String> actualCols = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            for (int i = 1; i <= colCount; i++) {
                String dbName = meta.getColumnLabel(i).toLowerCase();
                String display = dbName;
                for (String[] m : colMap)
                    if (m[0].equalsIgnoreCase(dbName)) {
                        display = m[1];
                        break;
                    }
                actualCols.add(display);
                model.addColumn(display);
            }
            while (rs.next()) {
                Object[] row = new Object[colCount];
                for (int i = 1; i <= colCount; i++)
                    row[i - 1] = rs.getObject(i);
                model.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            model.addColumn("Error");
            model.addRow(new Object[] { ex.getMessage() });
        }

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        JTable table = buildStyledTable(model, sorter, false, -1);
        UIFactory.styleTableHeader(table, Constants.C_HEADER_BG);
        UIFactory.autoSizeColumns(table, model, model.getColumnCount(), 1100);

        JScrollPane tableScroll = new JScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScroll.setBorder(BorderFactory.createLineBorder(Constants.C_BORDER, 1));
        tableScroll.getViewport().setBackground(Constants.C_SURFACE);

        JLabel countLbl = new JLabel("Showing " + model.getRowCount() +
                " of " + model.getRowCount() + " records");
        countLbl.setFont(Constants.F_SMALL);
        countLbl.setForeground(Constants.C_TEXT_MUTED);

        JTextField searchField = styledSearchField();
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            public void changedUpdate(DocumentEvent e) {
                filter();
            }

            void filter() {
                String q = searchField.getText().trim();
                sorter.setRowFilter(
                        q.isEmpty() ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q)));
                countLbl.setText("Showing " + table.getRowCount() +
                        " of " + model.getRowCount() + " records");
            }
        });

        JButton exportBtn = UIFactory.createPrimaryButton("Export CSV");
        exportBtn.setFont(Constants.F_SMALL.deriveFont(Font.BOLD));
        exportBtn.setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));
        exportBtn.addActionListener(e -> exportCSV(table, model, frame));

        JLabel sortLbl = new JLabel("Click any column header to sort  \u2191\u2193");
        sortLbl.setFont(Constants.F_CAPTION);
        sortLbl.setForeground(Constants.C_TEXT_LIGHT);

        JPanel searchLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchLeft.setBackground(Constants.C_BG);
        searchLeft.add(new JLabel("\uD83D\uDD0D "));
        searchLeft.add(searchField);
        searchLeft.add(countLbl);

        JPanel searchRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchRight.setBackground(Constants.C_BG);
        searchRight.add(exportBtn);

        JPanel row1 = new JPanel(new BorderLayout(8, 0));
        row1.setBackground(Constants.C_BG);
        row1.add(searchLeft, BorderLayout.WEST);
        row1.add(searchRight, BorderLayout.EAST);

        JPanel sortRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        sortRow.setBackground(Constants.C_BG);
        sortRow.add(sortLbl);

        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
        toolbar.setBackground(Constants.C_BG);
        toolbar.add(row1);
        toolbar.add(Box.createVerticalStrut(4));
        toolbar.add(sortRow);

        JPanel detailPanel = buildDetailPanel(table, model, false);
        table.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                refreshDetailPanel(detailPanel, table, model, modelRow,
                        actualCols, false, -1);
                detailPanel.setVisible(true);
            }
        });

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                tableScroll, detailPanel);
        split.setResizeWeight(0.65);
        split.setBorder(null);
        split.setDividerSize(6);
        detailPanel.setVisible(false);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // ── Shared styled table builder ───────────────────────────────────────────
    private static JTable buildStyledTable(DefaultTableModel model,
            TableRowSorter<DefaultTableModel> sorter,
            boolean isApplicant, int statusCol) {
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

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(241, 245, 249)),
                        BorderFactory.createEmptyBorder(0, 14, 0, 14)));
                setBackground(sel ? new Color(219, 234, 254)
                        : (row % 2 == 0 ? Constants.C_SURFACE : new Color(250, 252, 255)));
                setForeground(Constants.C_TEXT_PRIMARY);
                setFont(Constants.F_BODY);

                int modelCol = t.convertColumnIndexToModel(col);
                if (isApplicant && modelCol == statusCol && val != null) {
                    switch (val.toString()) {
                        case "Approved" -> setForeground(new Color(22, 163, 74));
                        case "Rejected" -> setForeground(Constants.C_DANGER);
                        default -> setForeground(new Color(180, 120, 0));
                    }
                    setFont(Constants.F_BODY.deriveFont(Font.BOLD));
                    setText("● " + val);
                }
                return this;
            }
        });
        return table;
    }

    // ── Detail panel ──────────────────────────────────────────────────────────
    private static JPanel buildDetailPanel(JTable table, DefaultTableModel model,
            boolean isApplicant) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Constants.C_SURFACE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Constants.C_BORDER));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(248, 250, 252));
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Constants.C_BORDER),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)));

        JLabel headerLbl = new JLabel("Record Details");
        headerLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        headerLbl.setForeground(Constants.C_PRIMARY);

        JButton closeBtn = new JButton("\u2715");
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
        header.add(closeBtn, BorderLayout.EAST);

        JPanel fieldsArea = new JPanel();
        fieldsArea.setBackground(Constants.C_SURFACE);
        fieldsArea.setLayout(new WrapLayout(FlowLayout.LEFT, 16, 10));
        fieldsArea.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JScrollPane fieldsScroll = new JScrollPane(fieldsArea,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        fieldsScroll.setBorder(null);
        fieldsScroll.getViewport().setBackground(Constants.C_SURFACE);

        panel.add(header, BorderLayout.NORTH);
        panel.add(fieldsScroll, BorderLayout.CENTER);
        return panel;
    }

    private static void refreshDetailPanel(JPanel detailPanel, JTable table,
            DefaultTableModel model, int modelRow,
            List<String> colNames,
            boolean isApplicant, int statusCol) {
        JScrollPane scroll = (JScrollPane) detailPanel.getComponent(1);
        JPanel fieldsArea = (JPanel) scroll.getViewport().getView();
        fieldsArea.removeAll();

        for (int col = 0; col < model.getColumnCount(); col++) {
            Object val = model.getValueAt(modelRow, col);
            String display = (val != null && !val.toString().trim().isEmpty())
                    ? val.toString()
                    : "\u2014";

            JPanel chip = new JPanel();
            chip.setLayout(new BoxLayout(chip, BoxLayout.Y_AXIS));
            chip.setBackground(new Color(248, 250, 252));
            chip.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Constants.C_BORDER, 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)));

            JLabel keyLbl = new JLabel(model.getColumnName(col).toUpperCase());
            keyLbl.setFont(new Font("Segoe UI", Font.BOLD, 9));
            keyLbl.setForeground(Constants.C_TEXT_LIGHT);

            JLabel valLbl = new JLabel(display);
            valLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            if (isApplicant && col == statusCol && val != null) {
                switch (val.toString()) {
                    case "Approved" -> valLbl.setForeground(new Color(22, 163, 74));
                    case "Rejected" -> valLbl.setForeground(Constants.C_DANGER);
                    default -> valLbl.setForeground(new Color(180, 120, 0));
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

    // ── CSV Export ────────────────────────────────────────────────────────────
    private static void exportCSV(JTable table, DefaultTableModel model, JFrame frame) {
        StringBuilder sb = new StringBuilder();
        for (int col = 0; col < model.getColumnCount(); col++) {
            sb.append('"').append(model.getColumnName(col)).append('"');
            if (col < model.getColumnCount() - 1)
                sb.append(',');
        }
        sb.append('\n');
        for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            for (int col = 0; col < model.getColumnCount(); col++) {
                Object val = model.getValueAt(modelRow, col);
                String cell = val != null ? val.toString().replace("\"", "\"\"") : "";
                sb.append('"').append(cell).append('"');
                if (col < model.getColumnCount() - 1)
                    sb.append(',');
            }
            sb.append('\n');
        }
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(sb.toString()), null);

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save CSV");
        fc.setSelectedFile(new java.io.File("applicants_export.csv"));
        if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try (java.io.FileWriter fw = new java.io.FileWriter(fc.getSelectedFile())) {
                fw.write(sb.toString());
                JOptionPane.showMessageDialog(frame,
                        "CSV saved to:\n" + fc.getSelectedFile().getAbsolutePath(),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (java.io.IOException ex) {
                JOptionPane.showMessageDialog(frame,
                        "Failed to save: " + ex.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── UI helpers ────────────────────────────────────────────────────────────
    private static JTextField styledSearchField() {
        JTextField f = new JTextField();
        f.setFont(Constants.F_BODY);
        f.setBackground(Constants.C_SURFACE);
        f.setForeground(Constants.C_TEXT_PRIMARY);
        f.setCaretColor(Constants.C_TEXT_PRIMARY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Constants.C_BORDER, 1, true),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)));
        f.setPreferredSize(new Dimension(260, 34));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Constants.C_ACCENT, 1, true),
                        BorderFactory.createEmptyBorder(7, 12, 7, 12)));
            }

            public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Constants.C_BORDER, 1, true),
                        BorderFactory.createEmptyBorder(7, 12, 7, 12)));
            }
        });
        return f;
    }

    private static JComboBox<String> filterCombo(String... items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(Constants.F_SMALL);
        cb.setBackground(Constants.C_SURFACE);
        cb.setForeground(Constants.C_TEXT_PRIMARY);
        cb.setPreferredSize(new Dimension(150, 28));
        return cb;
    }

    private static JLabel makeFilterLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(Constants.C_TEXT_MUTED);
        return l;
    }

    private static String[] ageOptions(String placeholder) {
        String[] opts = new String[101];
        opts[0] = placeholder;
        for (int i = 1; i <= 100; i++)
            opts[i] = String.valueOf(i);
        return opts;
    }

    // ── WrapLayout ────────────────────────────────────────────────────────────
    static class WrapLayout extends FlowLayout {
        public WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            return layoutSize(target, false);
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getSize().width;
                if (targetWidth == 0)
                    targetWidth = Integer.MAX_VALUE;
                int hgap = getHgap(), vgap = getVgap();
                Insets insets = target.getInsets();
                int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);
                int x = 0, y = insets.top + vgap, rowHeight = 0;
                for (Component m : target.getComponents()) {
                    if (!m.isVisible())
                        continue;
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