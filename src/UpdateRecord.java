import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.Year;

public class UpdateRecord {

    private UpdateRecord() {}

    private static boolean isValidName(String value) {
        if (value == null || value.trim().isEmpty()) return false;
        return value.matches("[\\p{L}\\s'.\\-]+") && value.matches(".*\\p{L}.*");
    }

    private static String findInvalidNameField(String[][] pairs) {
        for (String[] pair : pairs) {
            String val = pair[1];
            if (!val.isEmpty() && !isValidName(val)) return pair[0];
        }
        return null;
    }

    private static boolean isValidPhone(String value) {
        return value.matches("[\\d\\s\\+\\-\\(\\)]+")
            && value.replaceAll("[^\\d]", "").length() >= 7;
    }

    private static boolean isValidEmail(String value) {
        return value.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    }

    private static boolean isValidHeight(String value) {
        try { double h = Double.parseDouble(value); return h >= 50 && h <= 300; }
        catch (NumberFormatException e) { return false; }
    }

    private static boolean isValidWeight(String value) {
        try { double w = Double.parseDouble(value); return w >= 1 && w <= 500; }
        catch (NumberFormatException e) { return false; }
    }

    public static void open(JFrame parentFrame) {
        JDialog dialog = new JDialog(parentFrame, "Update Application Record", true);
        dialog.setSize(980, 660);
        dialog.setMinimumSize(new Dimension(720, 500));
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Constants.C_BG);

        // NAV BAR
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(Constants.C_HEADER_BG);
        nav.setPreferredSize(new Dimension(0, 56));
        nav.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 28));

        JLabel navTitle = new JLabel("Update Application Record");
        navTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        navTitle.setForeground(Color.WHITE);

        JButton backBtn = UIFactory.createOutlineButton("\u2190 Back");
        backBtn.addActionListener(e -> dialog.dispose());

        JPanel navLeft  = new JPanel(new FlowLayout(FlowLayout.LEFT,  0, 16)); navLeft.setOpaque(false);  navLeft.add(navTitle);
        JPanel navRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 14)); navRight.setOpaque(false); navRight.add(backBtn);
        nav.add(navLeft,  BorderLayout.WEST);
        nav.add(navRight, BorderLayout.EAST);

        // SEARCH BAR
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
                    BorderFactory.createLineBorder(Constants.C_ACCENT, 1, true),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)));
            }
            public void focusLost(FocusEvent e) {
                idField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Constants.C_BORDER, 1, true),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)));
            }
        });

        JButton loadBtn = UIFactory.createPrimaryButton("Load Record");

        JLabel idLabel = new JLabel("Applicant ID to update:");
        idLabel.setFont(Constants.F_LABEL);
        idLabel.setForeground(Constants.C_TEXT_MUTED);

        JPanel inputRow = new JPanel(new BorderLayout(10, 0));
        inputRow.setOpaque(false);
        inputRow.add(idLabel, BorderLayout.WEST);
        inputRow.add(idField, BorderLayout.CENTER);
        inputRow.add(loadBtn, BorderLayout.EAST);

        JLabel hint = new JLabel("Tip: click a row in the table below to auto-fill the ID.");
        hint.setFont(Constants.F_CAPTION);
        hint.setForeground(Constants.C_TEXT_LIGHT);

        JPanel searchPanel = new JPanel(new BorderLayout(12, 0));
        searchPanel.setBackground(Constants.C_SURFACE);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Constants.C_BORDER),
            BorderFactory.createEmptyBorder(14, 24, 14, 24)));
        searchPanel.add(inputRow, BorderLayout.CENTER);
        searchPanel.add(hint,     BorderLayout.SOUTH);

        // PREVIEW TABLE
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel);
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
                    setBackground(new Color(219, 234, 254));
                    setForeground(Constants.C_TEXT_PRIMARY);
                } else if (match) {
                    setBackground(new Color(239, 246, 255));
                    setForeground(Constants.C_ACCENT);
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    setBackground(row % 2 == 0 ? Constants.C_SURFACE : Constants.C_BG);
                    setForeground(Constants.C_TEXT_PRIMARY);
                    setFont(Constants.F_BODY);
                }
                return this;
            }
        });

        UIFactory.styleTableHeader(table, Constants.C_HEADER_BG);

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

        Runnable loadTable = () -> {
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);
            try (Connection conn = DBUtil.getConnection();
                 Statement  st   = conn.createStatement();
                 ResultSet  rs   = st.executeQuery(
                     "SELECT applicant_id, name, date_of_birth, sex, occupation, " +
                     "present_address FROM applicant_t ORDER BY applicant_id")) {
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

        // LOAD + EDIT RECORD
        Runnable doLoad = () -> {
            String targetId = idField.getText().trim();
            if (targetId.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                    "Please enter an Applicant ID.", "No ID entered",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection conn = DBUtil.getConnection()) {

                // ── Fetch applicant ──────────────────────────────────────────
                String name, maidenName, dob, placeOfBirth, presentAddress, permanentAddress,
                       sex, civilStatus, bloodType, religion, nationality, pwd, soloParent,
                       occupation, personalMobile, personalEmail, relativeContact, relativeEmail,
                       heightCm, weightKg, hairColor, eyeColor, otherMarks;

                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT * FROM applicant_t WHERE applicant_id = ?")) {
                    ps.setString(1, targetId);
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next()) {
                        JOptionPane.showMessageDialog(dialog,
                            "<html>No record found with Applicant ID <b>" + targetId + "</b>.</html>",
                            "Record Not Found", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    name             = rs.getString("name");
                    maidenName       = rs.getString("maiden_name");
                    dob              = rs.getString("date_of_birth");
                    placeOfBirth     = rs.getString("place_of_birth");
                    presentAddress   = rs.getString("present_address");
                    permanentAddress = rs.getString("permanent_address");
                    sex              = rs.getString("sex");
                    civilStatus      = rs.getString("civil_status");
                    bloodType        = rs.getString("blood_type");
                    religion         = rs.getString("religion");
                    nationality      = rs.getString("nationality");
                    pwd              = rs.getString("pwd");
                    soloParent       = rs.getString("solo_parent");
                    occupation       = rs.getString("occupation");
                    personalMobile   = rs.getString("personal_mobile");
                    personalEmail    = rs.getString("personal_email");
                    relativeContact  = rs.getString("relative_contact_no");
                    relativeEmail    = rs.getString("relative_email");
                    heightCm         = rs.getString("height_cm");
                    weightKg         = rs.getString("weight_kg");
                    hairColor        = rs.getString("hair_color");
                    eyeColor         = rs.getString("eye_color");
                    otherMarks       = rs.getString("other_marks");
                }

                // ── Fetch father ─────────────────────────────────────────────
                String  fatherName = "", fatherContact = "", fatherAddress = "";
                boolean fatherWasUnknown = false;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT is_unknown, name, contact_no, address FROM parent_t " +
                        "WHERE applicant_id = ? AND parent_type = 'Father'")) {
                    ps.setString(1, targetId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        fatherWasUnknown = "YES".equalsIgnoreCase(rs.getString("is_unknown"));
                        fatherName    = rs.getString("name")       != null ? rs.getString("name")       : "";
                        fatherContact = rs.getString("contact_no") != null ? rs.getString("contact_no") : "";
                        fatherAddress = rs.getString("address")    != null ? rs.getString("address")    : "";
                    }
                }

                // ── Fetch mother ─────────────────────────────────────────────
                String  motherName = "", motherMarriedLn = "", motherContact = "", motherAddress = "";
                boolean motherWasUnknown = false;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT is_unknown, name, married_ln, contact_no, address FROM parent_t " +
                        "WHERE applicant_id = ? AND parent_type = 'Mother'")) {
                    ps.setString(1, targetId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        motherWasUnknown = "YES".equalsIgnoreCase(rs.getString("is_unknown"));
                        motherName      = rs.getString("name")       != null ? rs.getString("name")       : "";
                        motherMarriedLn = rs.getString("married_ln") != null ? rs.getString("married_ln") : "";
                        motherContact   = rs.getString("contact_no") != null ? rs.getString("contact_no") : "";
                        motherAddress   = rs.getString("address")    != null ? rs.getString("address")    : "";
                    }
                }

                // ── Fetch emergency ──────────────────────────────────────────
                String ecName = "", ecContact = "", ecRelationship = "", ecAddress = "";
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT ec_name, ec_contact_no, ec_relationship, ec_address " +
                        "FROM emergency_t WHERE applicant_id = ?")) {
                    ps.setString(1, targetId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        ecName         = rs.getString("ec_name");
                        ecContact      = rs.getString("ec_contact_no");
                        ecRelationship = rs.getString("ec_relationship");
                        ecAddress      = rs.getString("ec_address");
                    }
                }

                // ── Build edit fields ────────────────────────────────────────
                JTextField txtName             = UIFactory.styledField();
                JTextField txtMaidenName       = UIFactory.styledField();
                JTextField txtPlaceOfBirth     = UIFactory.styledField();
                JTextField txtPresentAddress   = UIFactory.styledField();
                JTextField txtPermanentAddress = UIFactory.styledField();
                JTextField txtNationality      = UIFactory.styledField();
                JTextField txtReligion         = UIFactory.styledField();
                JTextField txtOccupation       = UIFactory.styledField();
                JTextField txtPersonalNo       = UIFactory.styledField();
                JTextField txtPersonalEmail    = UIFactory.styledField();
                JTextField txtRelativeNo       = UIFactory.styledField();
                JTextField txtRelativeEmail    = UIFactory.styledField();
                JTextField txtHeight           = UIFactory.styledField();
                JTextField txtWeight           = UIFactory.styledField();
                JTextField txtHairColor        = UIFactory.styledField();
                JTextField txtEyeColor         = UIFactory.styledField();
                JTextField txtOtherMarks       = UIFactory.styledField();

                int currentYear = Year.now().getValue();
                String[] months = {"January","February","March","April","May","June",
                                   "July","August","September","October","November","December"};
                String[] days  = new String[31];  for (int i = 0; i < 31; i++)  days[i]  = String.valueOf(i + 1);
                String[] years = new String[100]; for (int i = 0; i < 100; i++) years[i] = String.valueOf(currentYear - i);

                JComboBox<String> cbDobMonth = UIFactory.styledCombo(months);
                JComboBox<String> cbDobDay   = UIFactory.styledCombo(days);
                JComboBox<String> cbDobYear  = UIFactory.styledCombo(years);
                JPanel dobPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                dobPanel.setBackground(Constants.C_SURFACE);
                dobPanel.add(cbDobMonth); dobPanel.add(cbDobDay); dobPanel.add(cbDobYear);

                JComboBox<String> cbSex         = UIFactory.styledCombo("M", "F");
                JComboBox<String> cbCivilStatus = UIFactory.styledCombo("S", "M", "W", "D");
                JComboBox<String> cbPwd         = UIFactory.styledCombo("NO", "YES");
                JComboBox<String> cbSoloParent  = UIFactory.styledCombo("NO", "YES");
                JComboBox<String> cbBloodType   = UIFactory.styledCombo("A+","A-","B+","B-","AB+","AB-","O+","O-");

                JTextField txtFatherName        = UIFactory.styledField();
                JTextField txtFatherContact     = UIFactory.styledField();
                JTextField txtFatherAddress     = UIFactory.styledField();
                JTextField txtMotherName        = UIFactory.styledField();
                JTextField txtMotherMarriedLast = UIFactory.styledField();
                JTextField txtMotherContact     = UIFactory.styledField();
                JTextField txtMotherAddress     = UIFactory.styledField();
                JTextField txtEmergName         = UIFactory.styledField();
                JTextField txtEmergContact      = UIFactory.styledField();
                JTextField txtEmergRelationship = UIFactory.styledField();
                JTextField txtEmergAddress      = UIFactory.styledField();

                JCheckBox chkFatherUnknown = new JCheckBox("Unknown / Deceased");
                chkFatherUnknown.setFont(Constants.F_SMALL);
                chkFatherUnknown.setForeground(Constants.C_DANGER);
                chkFatherUnknown.setBackground(Constants.C_SURFACE);
                chkFatherUnknown.setFocusPainted(false);

                JCheckBox chkMotherUnknown = new JCheckBox("Unknown / Deceased");
                chkMotherUnknown.setFont(Constants.F_SMALL);
                chkMotherUnknown.setForeground(Constants.C_DANGER);
                chkMotherUnknown.setBackground(Constants.C_SURFACE);
                chkMotherUnknown.setFocusPainted(false);

                JTextField[] fatherFields = { txtFatherName, txtFatherContact, txtFatherAddress };
                JTextField[] motherFields = { txtMotherName, txtMotherMarriedLast,
                                              txtMotherContact, txtMotherAddress };

                chkFatherUnknown.addActionListener(e -> {
                    boolean u = chkFatherUnknown.isSelected();
                    for (JTextField f : fatherFields) {
                        f.setEnabled(!u); f.setText(""); f.setBackground(u ? Constants.C_BG : Constants.C_SURFACE);
                    }
                });
                chkMotherUnknown.addActionListener(e -> {
                    boolean u = chkMotherUnknown.isSelected();
                    for (JTextField f : motherFields) {
                        f.setEnabled(!u); f.setText(""); f.setBackground(u ? Constants.C_BG : Constants.C_SURFACE);
                    }
                });

                // ── Pre-fill ─────────────────────────────────────────────────
                txtName.setText(name != null ? name : "");
                txtMaidenName.setText(maidenName != null ? maidenName : "");
                txtPlaceOfBirth.setText(placeOfBirth != null ? placeOfBirth : "");
                txtPresentAddress.setText(presentAddress != null ? presentAddress : "");
                txtPermanentAddress.setText(permanentAddress != null ? permanentAddress : "");
                txtNationality.setText(nationality != null ? nationality : "");
                txtReligion.setText(religion != null ? religion : "");
                txtOccupation.setText(occupation != null ? occupation : "");
                txtPersonalNo.setText(personalMobile != null ? personalMobile : "");
                txtPersonalEmail.setText(personalEmail != null ? personalEmail : "");
                txtRelativeNo.setText(relativeContact != null ? relativeContact : "");
                txtRelativeEmail.setText(relativeEmail != null ? relativeEmail : "");
                txtHeight.setText(heightCm != null ? heightCm : "");
                txtWeight.setText(weightKg != null ? weightKg : "");
                txtHairColor.setText(hairColor != null ? hairColor : "");
                txtEyeColor.setText(eyeColor != null ? eyeColor : "");
                txtOtherMarks.setText(otherMarks != null ? otherMarks : "");

                if (dob != null && dob.length() == 10) {
                    try {
                        cbDobMonth.setSelectedIndex(Integer.parseInt(dob.substring(5, 7)) - 1);
                        cbDobDay.setSelectedItem(String.valueOf(Integer.parseInt(dob.substring(8, 10))));
                        cbDobYear.setSelectedItem(dob.substring(0, 4));
                    } catch (NumberFormatException ignored) {}
                }
                if (sex != null)         cbSex.setSelectedItem(sex);
                if (civilStatus != null) cbCivilStatus.setSelectedItem(civilStatus);
                if (bloodType != null)   cbBloodType.setSelectedItem(bloodType);
                if (pwd != null)         cbPwd.setSelectedItem(pwd);
                if (soloParent != null)  cbSoloParent.setSelectedItem(soloParent);

                txtFatherName.setText(fatherName);
                txtFatherContact.setText(fatherContact);
                txtFatherAddress.setText(fatherAddress);
                if (fatherWasUnknown) {
                    chkFatherUnknown.setSelected(true);
                    for (JTextField f : fatherFields) {
                        f.setEnabled(false); f.setText(""); f.setBackground(Constants.C_BG);
                    }
                }

                txtMotherName.setText(motherName);
                txtMotherMarriedLast.setText(motherMarriedLn);
                txtMotherContact.setText(motherContact);
                txtMotherAddress.setText(motherAddress);
                if (motherWasUnknown) {
                    chkMotherUnknown.setSelected(true);
                    for (JTextField f : motherFields) {
                        f.setEnabled(false); f.setText(""); f.setBackground(Constants.C_BG);
                    }
                }

                txtEmergName.setText(ecName != null ? ecName : "");
                txtEmergContact.setText(ecContact != null ? ecContact : "");
                txtEmergRelationship.setText(ecRelationship != null ? ecRelationship : "");
                txtEmergAddress.setText(ecAddress != null ? ecAddress : "");

                JTextField[] required = {
                    txtName, txtPlaceOfBirth, txtPresentAddress, txtPermanentAddress,
                    txtNationality, txtReligion, txtOccupation, txtRelativeNo,
                    txtRelativeEmail, txtPersonalEmail, txtHeight, txtWeight,
                    txtHairColor, txtEyeColor,
                    txtEmergName, txtEmergContact, txtEmergRelationship, txtEmergAddress
                };

                JPanel fatherSection = buildParentSection(
                    "Father Information", chkFatherUnknown, fatherFields,
                    new Object[]{
                        "Father's Name",       txtFatherName,
                        "Father's Contact No", txtFatherContact,
                        "Father's Address",    txtFatherAddress
                    });

                JPanel motherSection = buildParentSection(
                    "Mother Information", chkMotherUnknown, motherFields,
                    new Object[]{
                        "Mother's Name",              txtMotherName,
                        "Mother's Married Last Name", txtMotherMarriedLast,
                        "Mother's Contact No",        txtMotherContact,
                        "Mother's Address",           txtMotherAddress
                    });

                JPanel formContainer = new JPanel();
                formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
                formContainer.setBackground(Constants.C_BG);
                formContainer.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

                JPanel infoBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
                infoBanner.setBackground(new Color(239, 246, 255));
                infoBanner.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(191, 219, 254), 1, true),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)));
                JLabel infoText = new JLabel(
                    "<html>Editing record for <b>" + name + "</b>  —  " +
                    "Applicant ID: <b>" + targetId + "</b></html>");
                infoText.setFont(Constants.F_SMALL);
                infoText.setForeground(new Color(29, 78, 216));
                infoBanner.add(infoText);
                infoBanner.setAlignmentX(Component.LEFT_ALIGNMENT);

                formContainer.add(infoBanner);
                formContainer.add(Box.createVerticalStrut(10));
                formContainer.add(UIFactory.buildFormSection("Personal Information", new Object[]{
                    "Full Name *",           txtName,
                    "Maiden Name",           txtMaidenName,
                    "Date of Birth *",       dobPanel,
                    "Place of Birth *",      txtPlaceOfBirth,
                    "Present Address *",     txtPresentAddress,
                    "Permanent Address *",   txtPermanentAddress,
                    "Sex *",                 cbSex,
                    "Civil Status *",        cbCivilStatus,
                    "Blood Type *",          cbBloodType,
                    "Religion *",            txtReligion,
                    "Nationality *",         txtNationality,
                    "PWD *",                 cbPwd,
                    "Solo Parent *",         cbSoloParent,
                    "Occupation *",          txtOccupation,
                    "Personal No",           txtPersonalNo,
                    "Personal Email *",      txtPersonalEmail,
                    "Relative Contact No *", txtRelativeNo,
                    "Relative Email *",      txtRelativeEmail,
                    "Height (cm) *",         txtHeight,
                    "Weight (kg) *",         txtWeight,
                    "Hair Color *",          txtHairColor,
                    "Eye Color *",           txtEyeColor,
                    "Other Marks",           txtOtherMarks
                }));
                formContainer.add(Box.createVerticalStrut(12));
                formContainer.add(fatherSection);
                formContainer.add(Box.createVerticalStrut(12));
                formContainer.add(motherSection);
                formContainer.add(Box.createVerticalStrut(12));
                formContainer.add(UIFactory.buildFormSection("Emergency Contact", new Object[]{
                    "Name *",         txtEmergName,
                    "Contact No *",   txtEmergContact,
                    "Relationship *", txtEmergRelationship,
                    "Address *",      txtEmergAddress
                }));

                JScrollPane editScroll = new JScrollPane(formContainer);
                editScroll.setPreferredSize(new Dimension(640, 560));
                editScroll.setBorder(BorderFactory.createLineBorder(Constants.C_BORDER, 1));
                editScroll.getVerticalScrollBar().setUnitIncrement(18);
                editScroll.getViewport().setBackground(Constants.C_BG);

                // ── Edit / validate loop ──────────────────────────────────────
                // KEY FIX: ALL validation failures use 'continue' not 'return'.
                // 'return' exits doLoad entirely (closes the form).
                // 'continue' re-shows the edit dialog so the user can fix the error.
                while (true) {
                    int option = JOptionPane.showConfirmDialog(dialog, editScroll,
                        "Edit Application — " + targetId,
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                    // User cancelled — close without saving
                    if (option != JOptionPane.OK_OPTION) break;

                    // ── Step 1: required fields ───────────────────────────────
                    boolean allFilled = true;
                    for (JTextField f : required)
                        if (f.getText().trim().isEmpty()) { allFilled = false; break; }
                    if (allFilled && !chkFatherUnknown.isSelected()
                            && txtFatherName.getText().trim().isEmpty()) allFilled = false;
                    if (allFilled && !chkMotherUnknown.isSelected()
                            && txtMotherName.getText().trim().isEmpty()) allFilled = false;

                    if (!allFilled) {
                        JOptionPane.showMessageDialog(dialog,
                            "<html>Please fill in all required fields (*).<br>" +
                            "If a parent is unknown or deceased, tick the checkbox " +
                            "in that parent's section.</html>",
                            "Required fields missing", JOptionPane.WARNING_MESSAGE);
                        continue; // <-- keep loop alive, re-show edit form
                    }

                    // ── Step 2: name validation ───────────────────────────────
                    java.util.List<String[]> nameChecks = new java.util.ArrayList<>();
                    nameChecks.add(new String[]{"Full Name",              txtName.getText().trim()});
                    nameChecks.add(new String[]{"Maiden Name",            txtMaidenName.getText().trim()});
                    nameChecks.add(new String[]{"Emergency Contact Name", txtEmergName.getText().trim()});
                    if (!chkFatherUnknown.isSelected() && !txtFatherName.getText().trim().isEmpty())
                        nameChecks.add(new String[]{"Father's Name", txtFatherName.getText().trim()});
                    if (!chkMotherUnknown.isSelected()) {
                        if (!txtMotherName.getText().trim().isEmpty())
                            nameChecks.add(new String[]{"Mother's Name", txtMotherName.getText().trim()});
                        if (!txtMotherMarriedLast.getText().trim().isEmpty())
                            nameChecks.add(new String[]{"Mother's Married Last Name",
                                                        txtMotherMarriedLast.getText().trim()});
                    }
                    String invalidName = findInvalidNameField(nameChecks.toArray(new String[0][]));
                    if (invalidName != null) {
                        JOptionPane.showMessageDialog(dialog,
                            "<html><b>" + invalidName + "</b> contains invalid characters.<br>" +
                            "Name fields must contain letters only<br>" +
                            "(spaces, hyphens, apostrophes, and dots are allowed).</html>",
                            "Invalid Name", JOptionPane.WARNING_MESSAGE);
                        continue; // <-- was 'return', now 'continue'
                    }

                    // ── Step 3: phone validation ──────────────────────────────
                    boolean phoneError = false;
                    String[][] phoneChecks = {
                        {"Relative Contact No",  txtRelativeNo.getText().trim()},
                        {"Emergency Contact No", txtEmergContact.getText().trim()},
                    };
                    for (String[] pair : phoneChecks) {
                        if (!pair[1].isEmpty() && !isValidPhone(pair[1])) {
                            JOptionPane.showMessageDialog(dialog,
                                "<html><b>" + pair[0] + "</b> is not a valid phone number.<br>" +
                                "Use digits, spaces, hyphens, or a leading + only.</html>",
                                "Invalid Phone Number", JOptionPane.WARNING_MESSAGE);
                            phoneError = true;
                            break;
                        }
                    }
                    if (phoneError) continue; // <-- was 'return', now 'continue'

                    if (!txtPersonalNo.getText().trim().isEmpty()
                            && !isValidPhone(txtPersonalNo.getText().trim())) {
                        JOptionPane.showMessageDialog(dialog,
                            "<html><b>Personal No</b> is not a valid phone number.<br>" +
                            "Use digits, spaces, hyphens, or a leading + only.</html>",
                            "Invalid Phone Number", JOptionPane.WARNING_MESSAGE);
                        continue; // <-- was 'return'
                    }
                    if (!chkFatherUnknown.isSelected() && !txtFatherContact.getText().trim().isEmpty()
                            && !isValidPhone(txtFatherContact.getText().trim())) {
                        JOptionPane.showMessageDialog(dialog,
                            "<html><b>Father's Contact No</b> is not a valid phone number.<br>" +
                            "Use digits, spaces, hyphens, or a leading + only.</html>",
                            "Invalid Phone Number", JOptionPane.WARNING_MESSAGE);
                        continue; // <-- was 'return'
                    }
                    if (!chkMotherUnknown.isSelected() && !txtMotherContact.getText().trim().isEmpty()
                            && !isValidPhone(txtMotherContact.getText().trim())) {
                        JOptionPane.showMessageDialog(dialog,
                            "<html><b>Mother's Contact No</b> is not a valid phone number.<br>" +
                            "Use digits, spaces, hyphens, or a leading + only.</html>",
                            "Invalid Phone Number", JOptionPane.WARNING_MESSAGE);
                        continue; // <-- was 'return'
                    }

                    // ── Step 4: email validation ──────────────────────────────
                    boolean emailError = false;
                    String[][] emailChecks = {
                        {"Personal Email", txtPersonalEmail.getText().trim()},
                        {"Relative Email", txtRelativeEmail.getText().trim()},
                    };
                    for (String[] pair : emailChecks) {
                        if (!pair[1].isEmpty() && !isValidEmail(pair[1])) {
                            JOptionPane.showMessageDialog(dialog,
                                "<html><b>" + pair[0] + "</b> is not a valid email address.<br>" +
                                "Example: juan@email.com</html>",
                                "Invalid Email", JOptionPane.WARNING_MESSAGE);
                            emailError = true;
                            break;
                        }
                    }
                    if (emailError) continue; // <-- was 'return'

                    // ── Step 5: height & weight ───────────────────────────────
                    if (!isValidHeight(txtHeight.getText().trim())) {
                        JOptionPane.showMessageDialog(dialog,
                            "<html><b>Height</b> must be a number between 50 and 300 cm.</html>",
                            "Invalid Height", JOptionPane.WARNING_MESSAGE);
                        continue; // <-- was 'return'
                    }
                    if (!isValidWeight(txtWeight.getText().trim())) {
                        JOptionPane.showMessageDialog(dialog,
                            "<html><b>Weight</b> must be a number between 1 and 500 kg.</html>",
                            "Invalid Weight", JOptionPane.WARNING_MESSAGE);
                        continue; // <-- was 'return'
                    }

                    // ── Step 6: all valid — execute UPDATE ────────────────────
                    try (Connection upConn = DBUtil.getConnection()) {
                        upConn.setAutoCommit(false);
                        try {
                            String newDob = String.format("%04d-%02d-%02d",
                                Integer.parseInt((String) cbDobYear.getSelectedItem()),
                                cbDobMonth.getSelectedIndex() + 1,
                                Integer.parseInt((String) cbDobDay.getSelectedItem()));

                            try (PreparedStatement ps = upConn.prepareStatement(
                                    "UPDATE applicant_t SET " +
                                    "name=?,maiden_name=?,date_of_birth=?,place_of_birth=?," +
                                    "present_address=?,permanent_address=?,sex=?,civil_status=?," +
                                    "blood_type=?,religion=?,nationality=?,pwd=?,solo_parent=?," +
                                    "occupation=?,personal_mobile=?,personal_email=?," +
                                    "relative_contact_no=?,relative_email=?," +
                                    "height_cm=?,weight_kg=?,hair_color=?,eye_color=?,other_marks=? " +
                                    "WHERE applicant_id=?")) {
                                ps.setString(1,  txtName.getText().trim());
                                ps.setString(2,  UIFactory.nullIfEmpty(txtMaidenName));
                                ps.setString(3,  newDob);
                                ps.setString(4,  txtPlaceOfBirth.getText().trim());
                                ps.setString(5,  txtPresentAddress.getText().trim());
                                ps.setString(6,  txtPermanentAddress.getText().trim());
                                ps.setString(7,  (String) cbSex.getSelectedItem());
                                ps.setString(8,  (String) cbCivilStatus.getSelectedItem());
                                ps.setString(9,  (String) cbBloodType.getSelectedItem());
                                ps.setString(10, txtReligion.getText().trim());
                                ps.setString(11, txtNationality.getText().trim());
                                ps.setString(12, (String) cbPwd.getSelectedItem());
                                ps.setString(13, (String) cbSoloParent.getSelectedItem());
                                ps.setString(14, txtOccupation.getText().trim());
                                ps.setString(15, UIFactory.nullIfEmpty(txtPersonalNo));
                                ps.setString(16, UIFactory.nullIfEmpty(txtPersonalEmail));
                                ps.setString(17, txtRelativeNo.getText().trim());
                                ps.setString(18, txtRelativeEmail.getText().trim());
                                ps.setString(19, txtHeight.getText().trim());
                                ps.setString(20, txtWeight.getText().trim());
                                ps.setString(21, txtHairColor.getText().trim());
                                ps.setString(22, txtEyeColor.getText().trim());
                                ps.setString(23, UIFactory.nullIfEmpty(txtOtherMarks));
                                ps.setString(24, targetId);
                                ps.executeUpdate();
                            }

                            boolean fUnknown = chkFatherUnknown.isSelected();
                            try (PreparedStatement ps = upConn.prepareStatement(
                                    "UPDATE parent_t SET is_unknown=?,name=?,contact_no=?,address=? " +
                                    "WHERE applicant_id=? AND parent_type='Father'")) {
                                ps.setString(1, fUnknown ? "YES" : "NO");
                                ps.setString(2, fUnknown ? null : txtFatherName.getText().trim());
                                ps.setString(3, fUnknown ? null : txtFatherContact.getText().trim());
                                ps.setString(4, fUnknown ? null : txtFatherAddress.getText().trim());
                                ps.setString(5, targetId);
                                ps.executeUpdate();
                            }

                            boolean mUnknown = chkMotherUnknown.isSelected();
                            try (PreparedStatement ps = upConn.prepareStatement(
                                    "UPDATE parent_t SET is_unknown=?,name=?,married_ln=?," +
                                    "contact_no=?,address=? " +
                                    "WHERE applicant_id=? AND parent_type='Mother'")) {
                                ps.setString(1, mUnknown ? "YES" : "NO");
                                ps.setString(2, mUnknown ? null : txtMotherName.getText().trim());
                                ps.setString(3, mUnknown ? null : txtMotherMarriedLast.getText().trim());
                                ps.setString(4, mUnknown ? null : txtMotherContact.getText().trim());
                                ps.setString(5, mUnknown ? null : txtMotherAddress.getText().trim());
                                ps.setString(6, targetId);
                                ps.executeUpdate();
                            }

                            try (PreparedStatement ps = upConn.prepareStatement(
                                    "UPDATE emergency_t SET ec_name=?,ec_contact_no=?," +
                                    "ec_relationship=?,ec_address=? WHERE applicant_id=?")) {
                                ps.setString(1, txtEmergName.getText().trim());
                                ps.setString(2, txtEmergContact.getText().trim());
                                ps.setString(3, txtEmergRelationship.getText().trim());
                                ps.setString(4, txtEmergAddress.getText().trim());
                                ps.setString(5, targetId);
                                ps.executeUpdate();
                            }

                            upConn.commit();
                            JOptionPane.showMessageDialog(dialog,
                                "<html>Record for <b>" + txtName.getText().trim() + "</b>" +
                                " (ID: " + targetId + ") updated successfully.</html>",
                                "Record Updated", JOptionPane.INFORMATION_MESSAGE);
                            idField.setText("");
                            loadTable.run();
                            table.clearSelection();
                            break; // success — exit loop

                        } catch (SQLException ex) {
                            upConn.rollback();
                            throw ex;
                        }
                    }
                } // end while(true)

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog,
                    "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }; // end doLoad

        loadBtn.addActionListener(e -> doLoad.run());
        idField.addActionListener(e -> doLoad.run());

        JPanel centerStack = new JPanel(new BorderLayout());
        centerStack.setBackground(Constants.C_BG);
        centerStack.add(searchPanel,  BorderLayout.NORTH);
        centerStack.add(tableWrapper, BorderLayout.CENTER);

        root.add(nav,         BorderLayout.NORTH);
        root.add(centerStack, BorderLayout.CENTER);

        dialog.add(root);
        dialog.setVisible(true);
    }

    private static JPanel buildParentSection(String title,
                                             JCheckBox unknownCheckbox,
                                             JTextField[] groupedFields,
                                             Object[] fieldPairs) {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(Constants.C_SURFACE);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Constants.C_BORDER, 1, true),
            BorderFactory.createEmptyBorder(16, 20, 20, 20)));
        wrapper.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(Constants.C_SURFACE);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(Constants.C_PRIMARY);

        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        checkboxPanel.setBackground(Constants.C_SURFACE);
        JLabel optionalTag = new JLabel("optional");
        optionalTag.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        optionalTag.setForeground(Constants.C_TEXT_LIGHT);
        checkboxPanel.add(optionalTag);
        checkboxPanel.add(unknownCheckbox);

        headerRow.add(titleLbl,      BorderLayout.WEST);
        headerRow.add(checkboxPanel, BorderLayout.EAST);

        JPanel divider = new JPanel();
        divider.setBackground(Constants.C_BORDER);
        divider.setPreferredSize(new Dimension(0, 1));
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        JPanel headerBlock = new JPanel();
        headerBlock.setLayout(new BoxLayout(headerBlock, BoxLayout.Y_AXIS));
        headerBlock.setBackground(Constants.C_SURFACE);
        headerBlock.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerBlock.add(headerRow);
        headerBlock.add(Box.createVerticalStrut(6));
        headerBlock.add(divider);
        headerBlock.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        wrapper.add(headerBlock);

        JPanel unknownNotice = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        unknownNotice.setBackground(Constants.C_DANGER_BG);
        unknownNotice.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Constants.C_DANGER_BORDER, 1, true),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)));
        unknownNotice.setAlignmentX(Component.LEFT_ALIGNMENT);
        unknownNotice.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel noticeText = new JLabel("This parent will be recorded as Unknown / Deceased.");
        noticeText.setFont(Constants.F_SMALL);
        noticeText.setForeground(Constants.C_DANGER);
        unknownNotice.add(noticeText);
        unknownNotice.setVisible(unknownCheckbox.isSelected());
        wrapper.add(unknownNotice);
        wrapper.add(Box.createVerticalStrut(8));

        int pairs = fieldPairs.length / 2;
        JPanel grid = new JPanel(new GridLayout(pairs, 2, 16, 10));
        grid.setBackground(Constants.C_SURFACE);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (int i = 0; i + 1 < fieldPairs.length; i += 2) {
            String    labelText = (String)    fieldPairs[i];
            Component field     = (Component) fieldPairs[i + 1];

            JPanel cell = new JPanel();
            cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
            cell.setBackground(Constants.C_SURFACE);

            JLabel lbl = new JLabel(labelText);
            lbl.setFont(Constants.F_LABEL);
            lbl.setForeground(Constants.C_TEXT_MUTED);
            lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
            lbl.setHorizontalAlignment(SwingConstants.LEFT);
            lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

            field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height));
            if (field instanceof JComponent)
                ((JComponent) field).setAlignmentX(Component.LEFT_ALIGNMENT);

            cell.add(lbl);
            cell.add(field);
            grid.add(cell);
        }
        wrapper.add(grid);

        unknownCheckbox.addActionListener(e -> {
            boolean unknown = unknownCheckbox.isSelected();
            unknownNotice.setVisible(unknown);
            for (Component child : grid.getComponents()) {
                child.setEnabled(!unknown);
                if (child instanceof JPanel)
                    for (Component inner : ((JPanel) child).getComponents())
                        inner.setEnabled(!unknown);
            }
            wrapper.revalidate();
            wrapper.repaint();
        });

        if (unknownCheckbox.isSelected()) {
            for (Component child : grid.getComponents()) {
                child.setEnabled(false);
                if (child instanceof JPanel)
                    for (Component inner : ((JPanel) child).getComponents())
                        inner.setEnabled(false);
            }
        }

        return wrapper;
    }
}