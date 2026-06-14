import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * UIFactory FILE
 * - Factory / helper methods for creating consistently styled Swing components:
 * - text fields, combo boxes, buttons, nav bars, form sections, and data
 * tables.
 */

public final class UIFactory {

    private UIFactory() {
    }

    // TEXT FIELDS

    /** Returns a styled {@link JTextField} with focus-ring behaviour. */
    public static JTextField styledField() {
        JTextField f = new JTextField(20);
        f.setFont(Constants.F_BODY);
        f.setBackground(Constants.C_SURFACE);
        f.setForeground(Constants.C_TEXT_PRIMARY);
        f.setCaretColor(Constants.C_TEXT_PRIMARY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Constants.C_BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Constants.C_ACCENT, 1, true),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            }

            public void focusLost(FocusEvent e) {
                f.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Constants.C_BORDER, 1, true),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            }
        });
        return f;
    }

    // COMBO BOX

    /** Returns a styled {@link JComboBox} populated with {@code items}. */
    public static JComboBox<String> styledCombo(String... items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(Constants.F_BODY);
        cb.setBackground(Constants.C_SURFACE);
        cb.setForeground(Constants.C_TEXT_PRIMARY);
        return cb;
    }

    // BUTTONS

    /** Filled navy primary button. */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Constants.F_LABEL);
        btn.setBackground(Constants.C_PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(Constants.C_PRIMARY_DARK);
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(Constants.C_PRIMARY);
            }
        });
        return btn;
    }

    // White outline button for use on coloured nav bars.
    public static JButton createOutlineButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Constants.F_LABEL);
        btn.setBackground(new Color(255, 255, 255, 0));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1, true),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.WHITE, 1, true),
                        BorderFactory.createEmptyBorder(6, 16, 6, 16)));
            }

            public void mouseExited(MouseEvent e) {
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 1, true),
                        BorderFactory.createEmptyBorder(6, 16, 6, 16)));
            }
        });
        return btn;
    }

    // FOR IMAGE DISPLAYS
    public static ImageIcon loadIcon(String filename, int size) {
        try {
            java.net.URL url = UIFactory.class.getResource("/icons/" + filename);
            if (url == null) {
                System.err.println("Icon not found: " + filename);
                return null;
            }
            Image img = new ImageIcon(url).getImage()
                    .getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // NAV BAR
    // Builds a compact navy nav bar and Back Button
    public static JPanel buildMiniNav(String pageTitle, JFrame frame) {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(Constants.C_HEADER_BG);
        nav.setPreferredSize(new Dimension(0, 56));
        nav.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 28));

        JLabel title = new JLabel(pageTitle);
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(Color.WHITE);

        JButton back = createOutlineButton("← Back");
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

    // FORMS SECTION
    // Builds a card-style form section.
    // @param title section heading
    // @param elements interleaved array of {@code String} label / {@link Component}
    // field pairs, e.g. {@code {"Full Name *", txtName, "DOB", dobPanel, ...}}

    public static JPanel buildFormSection(String title, Object[] elements) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(Constants.C_SURFACE);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Constants.C_BORDER, 1, true),
                BorderFactory.createEmptyBorder(16, 20, 20, 20)));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Section header + divider
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Constants.C_SURFACE);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(Constants.C_PRIMARY);

        JPanel divider = new JPanel();
        divider.setBackground(Constants.C_BORDER);
        divider.setPreferredSize(new Dimension(0, 1));

        header.add(titleLbl, BorderLayout.NORTH);
        header.add(divider, BorderLayout.SOUTH);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        wrapper.add(header);

        // 2-column grid of label + field pairs
        int pairs = elements.length / 2;
        JPanel grid = new JPanel(new GridLayout(pairs, 2, 16, 10));
        grid.setBackground(Constants.C_SURFACE);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int i = 0; i + 1 < elements.length; i += 2) {
            String labelText = (String) elements[i];
            Component field = (Component) elements[i + 1];

            JPanel cell = new JPanel();
            cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
            cell.setBackground(Constants.C_SURFACE);

            JLabel lbl = new JLabel(labelText);
            lbl.setFont(Constants.F_LABEL);
            lbl.setForeground(Constants.C_TEXT_MUTED);
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
            lbl.setHorizontalAlignment(SwingConstants.LEFT);
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

            field.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                    field.getPreferredSize().height));
            if (field instanceof JComponent) {
                ((JComponent) field).setAlignmentX(Component.LEFT_ALIGNMENT);
            }

            cell.add(lbl);
            cell.add(field);
            grid.add(cell);
        }

        wrapper.add(grid);
        return wrapper;
    }

    // DATA TABLE
    // Executes {@code sql} and returns a scrollable, read-only table
    // pre-populated with the result set.

    public static JScrollPane buildTable(String sql) {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel() {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(Constants.F_BODY);
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(Constants.C_TEXT_PRIMARY);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setBackground(Constants.C_SURFACE);
        table.setForeground(Constants.C_TEXT_PRIMARY);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Constants.C_BORDER),
                        BorderFactory.createEmptyBorder(0, 12, 0, 12)));
                if (!sel) {
                    setBackground(row % 2 == 0 ? Constants.C_SURFACE : Constants.C_BG);
                    setForeground(Constants.C_TEXT_PRIMARY);
                }
                return this;
            }
        });

        styleTableHeader(table, Constants.C_HEADER_BG);

        try (Connection conn = DBUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            for (int i = 1; i <= colCount; i++)
                model.addColumn(meta.getColumnLabel(i));
            while (rs.next()) {
                Object[] row = new Object[colCount];
                for (int i = 1; i <= colCount; i++)
                    row[i - 1] = rs.getObject(i);
                model.addRow(row);
            }
            autoSizeColumns(table, model, colCount, 1200);

        } catch (SQLException ex) {
            ex.printStackTrace();
            model.addColumn("Error");
            model.addRow(new Object[] { ex.getMessage() });
        }

        JScrollPane sp = new JScrollPane(table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        sp.setBorder(BorderFactory.createLineBorder(Constants.C_BORDER, 1));
        sp.getViewport().setBackground(Constants.C_SURFACE);
        return sp;
    }

    // PACKAGE PRIVATE HELPERS

    // Applies the standard header renderer to a table, using the given bg colour.
    public static void styleTableHeader(JTable table, Color headerBg) {
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(headerBg);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(Constants.F_LABEL);
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean focus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        t, v, sel, focus, row, col);
                lbl.setBackground(headerBg);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(Constants.F_LABEL);
                lbl.setHorizontalAlignment(JLabel.LEFT);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                lbl.setOpaque(true);

                // --- SORTING ARROWS LOGIC ---
                // Check if the table has an active sorter
                if (t.getRowSorter() != null) {
                    // Fully qualified java.util.List to avoid missing import errors
                    java.util.List<? extends RowSorter.SortKey> sortKeys = t.getRowSorter().getSortKeys();

                    if (!sortKeys.isEmpty()) {
                        RowSorter.SortKey sortKey = sortKeys.get(0);

                        // If this specific column is the one actively being sorted
                        if (sortKey.getColumn() == t.convertColumnIndexToModel(col)) {
                            if (sortKey.getSortOrder() == SortOrder.ASCENDING) {
                                lbl.setText(v.toString() + "  \u25B2"); // Append Up Arrow
                            } else if (sortKey.getSortOrder() == SortOrder.DESCENDING) {
                                lbl.setText(v.toString() + "  \u25BC"); // Append Down Arrow
                            }
                        }
                    }
                }
                // -----------------------------

                return lbl;
            }
        });
    }

    // Auto-sizes table columns to content; scales up if total width < minTotal.
    public static void autoSizeColumns(JTable table,
            javax.swing.table.DefaultTableModel model, int colCount, int minTotal) {
        FontMetrics hFm = table.getTableHeader().getFontMetrics(Constants.F_LABEL);
        FontMetrics cFm = table.getFontMetrics(Constants.F_BODY);
        int total = 0;
        int[] widths = new int[colCount];

        for (int col = 0; col < colCount; col++) {
            // INCREASED BUFFER: 55px ensures enough space for the padding and sorting
            // arrows (▲/▼)
            int w = Math.max(90, hFm.stringWidth(model.getColumnName(col)) + 55);

            for (int row = 0; row < model.getRowCount(); row++) {
                Object val = model.getValueAt(row, col);
                // Slightly bumped cell padding buffer to 32px as well
                if (val != null)
                    w = Math.max(w, cFm.stringWidth(val.toString()) + 32);
            }
            widths[col] = w;
            total += w;
        }

        if (total < minTotal) {
            double scale = (double) minTotal / total;
            for (int col = 0; col < colCount; col++)
                widths[col] = (int) (widths[col] * scale);
        }

        for (int col = 0; col < colCount; col++) {
            table.getColumnModel().getColumn(col).setPreferredWidth(widths[col]);
        }
    }

    /** Returns {@code null} if the field is blank, otherwise its trimmed text. */
    public static String nullIfEmpty(JTextField f) {
        String s = f.getText().trim();
        return s.isEmpty() ? null : s;
    }
}
