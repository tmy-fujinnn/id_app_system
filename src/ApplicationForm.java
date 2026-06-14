import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.Year;

/**
 * MAIN APPLICATION FORM
 *  - Gradient nav bar with title and Close button
 *  - Styled primary Submit button (not generic OK/Cancel)
 *  - Parent sections with Unknown/Deceased checkbox
 *  - Name, phone, email, height, weight validation
 */

public class ApplicationForm {

    private ApplicationForm() {}

    // NAME VALIDATION
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

    // PHONE VALIDATION
    private static boolean isValidPhone(String value) {
        return value.matches("[\\d\\s\\+\\-\\(\\)]+")
            && value.replaceAll("[^\\d]", "").length() >= 7;
    }

    // EMAIL VALIDATION
    private static boolean isValidEmail(String value) {
        return value.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    }

    // HEIGHT VALIDATION
    private static boolean isValidHeight(String value) {
        try {
            double h = Double.parseDouble(value);
            return h >= 50 && h <= 300;
        } catch (NumberFormatException e) { return false; }
    }

    // WEIGHT VALIDATION
    private static boolean isValidWeight(String value) {
        try {
            double w = Double.parseDouble(value);
            return w >= 1 && w <= 500;
        } catch (NumberFormatException e) { return false; }
    }

    // ENTRY POINT
    public static void open(JFrame parentFrame) {

        JDialog dialog = new JDialog(parentFrame, "New ID Application", true);
        dialog.setSize(860, 720);
        dialog.setMinimumSize(new Dimension(700, 560));
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Constants.C_BG);

        // NAV BAR
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

        JPanel navLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        navLeft.setOpaque(false);

        JPanel navTitlePanel = new JPanel();
        navTitlePanel.setOpaque(false);
        navTitlePanel.setLayout(new BoxLayout(navTitlePanel, BoxLayout.Y_AXIS));
        JLabel navTitle = new JLabel("New ID Application");
        navTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        navTitle.setForeground(Color.WHITE);
        JLabel navSub = new JLabel("Fill in all required fields marked with *");
        navSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        navSub.setForeground(new Color(255, 255, 255, 160));
        navTitlePanel.add(navTitle);
        navTitlePanel.add(navSub);

        navLeft.add(navTitlePanel);

        JPanel navLeftWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 14));
        navLeftWrapper.setOpaque(false);
        navLeftWrapper.add(navLeft);

        JButton closeBtn = UIFactory.createOutlineButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel navRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 14));
        navRight.setOpaque(false);
        navRight.add(closeBtn);

        nav.add(navLeftWrapper, BorderLayout.WEST);
        nav.add(navRight,       BorderLayout.EAST);

        // FIELDS FROM UIFactory
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

        // DOB combos
        int currentYear = Year.now().getValue();
        String[] months = {"January","February","March","April","May","June",
                           "July","August","September","October","November","December"};
        String[] days  = new String[31];  for (int i = 0; i < 31; i++)  days[i]  = String.valueOf(i + 1);
        String[] years = new String[100]; for (int i = 0; i < 100; i++) years[i] = String.valueOf(currentYear - i);

        JComboBox<String> cbDobMonth = UIFactory.styledCombo(months);
        JComboBox<String> cbDobDay   = UIFactory.styledCombo(days);
        JComboBox<String> cbDobYear  = UIFactory.styledCombo(years);

        cbDobMonth.setPreferredSize(new Dimension(130, 32));
        cbDobDay.setPreferredSize(new Dimension(65, 32));
        cbDobYear.setPreferredSize(new Dimension(85, 32));

        JPanel dobPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        dobPanel.setBackground(Constants.C_SURFACE);
        dobPanel.add(cbDobMonth);
        dobPanel.add(cbDobDay);
        dobPanel.add(cbDobYear);

        JComboBox<String> cbSex         = UIFactory.styledCombo("Male", "Female");
        JComboBox<String> cbCivilStatus = UIFactory.styledCombo("Single","Married","Widowed","Divorced/Annulled");
        JComboBox<String> cbPwd         = UIFactory.styledCombo("No", "Yes");
        JComboBox<String> cbSoloParent  = UIFactory.styledCombo("No", "Yes");
        JComboBox<String> cbBloodType   = UIFactory.styledCombo("A+","A-","B+","B-","AB+","AB-","O+","O-");

        for (JComboBox<String> cb : new JComboBox[]{cbSex, cbCivilStatus, cbPwd, cbSoloParent, cbBloodType})
            cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        // Parent fields
        JTextField txtFatherName        = UIFactory.styledField();
        JTextField txtFatherContact     = UIFactory.styledField();
        JTextField txtFatherAddress     = UIFactory.styledField();
        JTextField txtMotherName        = UIFactory.styledField();
        JTextField txtMotherMarriedLast = UIFactory.styledField();
        JTextField txtMotherContact     = UIFactory.styledField();
        JTextField txtMotherAddress     = UIFactory.styledField();

        // Emergency fields
        JTextField txtEmergName         = UIFactory.styledField();
        JTextField txtEmergContact      = UIFactory.styledField();
        JTextField txtEmergRelationship = UIFactory.styledField();
        JTextField txtEmergAddress      = UIFactory.styledField();

        // Unknown checkboxes
        JCheckBox chkFatherUnknown = styledCheckbox("Unknown / Deceased");
        JCheckBox chkMotherUnknown = styledCheckbox("Unknown / Deceased");

        JTextField[] fatherFields = { txtFatherName, txtFatherContact, txtFatherAddress };
        JTextField[] motherFields = { txtMotherName, txtMotherMarriedLast,
                                      txtMotherContact, txtMotherAddress };

        chkFatherUnknown.addActionListener(e -> toggleFields(chkFatherUnknown.isSelected(), fatherFields));
        chkMotherUnknown.addActionListener(e -> toggleFields(chkMotherUnknown.isSelected(), motherFields));

        JTextField[] required = {
            txtName, txtPlaceOfBirth, txtPresentAddress, txtPermanentAddress,
            txtNationality, txtReligion, txtOccupation, txtRelativeNo,
            txtRelativeEmail, txtPersonalEmail, txtHeight, txtWeight,
            txtHairColor, txtEyeColor,
            txtEmergName, txtEmergContact, txtEmergRelationship, txtEmergAddress
        };

        // ── Form sections ─────────────────────────────────────────────────────
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBackground(Constants.C_BG);
        formContainer.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        formContainer.add(buildSection("Personal Information", Constants.C_PRIMARY, new Object[][]{
            {"Full Name *",           txtName,             "Maiden Name",           txtMaidenName},
            {"Date of Birth *",       dobPanel,            "Place of Birth *",      txtPlaceOfBirth},
            {"Present Address *",     txtPresentAddress,   "Permanent Address *",   txtPermanentAddress},
            {"Sex *",                 cbSex,               "Civil Status *",        cbCivilStatus},
            {"Blood Type *",          cbBloodType,         "Religion *",            txtReligion},
            {"Nationality *",         txtNationality,      "Occupation *",          txtOccupation},
            {"PWD *",                 cbPwd,               "Solo Parent *",         cbSoloParent},
            {"Personal No",           txtPersonalNo,       "Personal Email *",      txtPersonalEmail},
            {"Relative Contact No *", txtRelativeNo,       "Relative Email *",      txtRelativeEmail},
            {"Height (cm) *",         txtHeight,           "Weight (kg) *",         txtWeight},
            {"Hair Color *",          txtHairColor,        "Eye Color *",           txtEyeColor},
            {"Other Marks",           txtOtherMarks,       null,                    null},
        }));

        formContainer.add(Box.createVerticalStrut(14));
        formContainer.add(buildParentSection("Father Information", chkFatherUnknown, new Object[][]{
            {"Father's Name",    txtFatherName,    "Father's Contact No", txtFatherContact},
            {"Father's Address", txtFatherAddress, null,                  null},
        }));

        formContainer.add(Box.createVerticalStrut(14));
        formContainer.add(buildParentSection("Mother Information", chkMotherUnknown, new Object[][]{
            {"Mother's Name",       txtMotherName,    "Mother's Married Last Name", txtMotherMarriedLast},
            {"Mother's Contact No", txtMotherContact, "Mother's Address",           txtMotherAddress},
        }));

        formContainer.add(Box.createVerticalStrut(14));
        formContainer.add(buildSection("Emergency Contact", new Color(16, 124, 88), new Object[][]{
            {"Name *",         txtEmergName,         "Contact No *",   txtEmergContact},
            {"Relationship *", txtEmergRelationship, "Address *",      txtEmergAddress},
        }));

        JScrollPane scrollPane = new JScrollPane(formContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getViewport().setBackground(Constants.C_BG);

        // ACTION BAR
        JPanel actionBar = new JPanel(new BorderLayout());
        actionBar.setBackground(Constants.C_SURFACE);
        actionBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Constants.C_BORDER),
            BorderFactory.createEmptyBorder(14, 24, 14, 24)));

        JLabel requiredNote = new JLabel("* Required fields must be filled in before submitting.");
        requiredNote.setFont(Constants.F_CAPTION);
        requiredNote.setForeground(Constants.C_TEXT_LIGHT);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        
        // BUTTONS FOR SUBMISSION AND CANCEL
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(Constants.F_LABEL);
        cancelBtn.setBackground(Constants.C_BG);
        cancelBtn.setForeground(Constants.C_TEXT_MUTED);
        cancelBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Constants.C_BORDER, 1, true),
            BorderFactory.createEmptyBorder(9, 22, 9, 22)));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton submitBtn = new JButton("Submit Application");
        submitBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        submitBtn.setBackground(Constants.C_PRIMARY);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setOpaque(true);
        submitBtn.setContentAreaFilled(true);
        submitBtn.setBorderPainted(false);
        submitBtn.setFocusPainted(false);
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.setBorder(BorderFactory.createEmptyBorder(10, 28, 10, 28));
        submitBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { submitBtn.setBackground(Constants.C_PRIMARY_DARK); }
            public void mouseExited (MouseEvent e) { submitBtn.setBackground(Constants.C_PRIMARY); }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(submitBtn);
        actionBar.add(requiredNote, BorderLayout.WEST);
        actionBar.add(btnPanel,     BorderLayout.EAST);

        root.add(nav,        BorderLayout.NORTH);
        root.add(scrollPane, BorderLayout.CENTER);
        root.add(actionBar,  BorderLayout.SOUTH);
        dialog.add(root);

        // SUBMIT ACTION
        Runnable doSubmit = () -> {

            // Step 1: required fields
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
                    "If a parent is unknown or deceased, tick the<br>" +
                    "checkbox in that parent's section.</html>",
                    "Required Fields Missing", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Step 2: name validation
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
                return;
            }

            // Step 3: phone validation
            String[][] phoneChecks = {
                {"Relative Contact No", txtRelativeNo.getText().trim()},
                {"Emergency Contact No", txtEmergContact.getText().trim()},
            };
            for (String[] pair : phoneChecks) {
                if (!pair[1].isEmpty() && !isValidPhone(pair[1])) {
                    JOptionPane.showMessageDialog(dialog,
                        "<html><b>" + pair[0] + "</b> is not a valid phone number.<br>" +
                        "Use digits, spaces, hyphens, or a leading + only.</html>",
                        "Invalid Phone Number", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            if (!txtPersonalNo.getText().trim().isEmpty()
                    && !isValidPhone(txtPersonalNo.getText().trim())) {
                JOptionPane.showMessageDialog(dialog,
                    "<html><b>Personal No</b> is not a valid phone number.<br>" +
                    "Use digits, spaces, hyphens, or a leading + only.</html>",
                    "Invalid Phone Number", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!chkFatherUnknown.isSelected() && !txtFatherContact.getText().trim().isEmpty()
                    && !isValidPhone(txtFatherContact.getText().trim())) {
                JOptionPane.showMessageDialog(dialog,
                    "<html><b>Father's Contact No</b> is not a valid phone number.<br>" +
                    "Use digits, spaces, hyphens, or a leading + only.</html>",
                    "Invalid Phone Number", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!chkMotherUnknown.isSelected() && !txtMotherContact.getText().trim().isEmpty()
                    && !isValidPhone(txtMotherContact.getText().trim())) {
                JOptionPane.showMessageDialog(dialog,
                    "<html><b>Mother's Contact No</b> is not a valid phone number.<br>" +
                    "Use digits, spaces, hyphens, or a leading + only.</html>",
                    "Invalid Phone Number", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Step 4: email validation
            String[][] emailChecks = {
                {"Personal Email",  txtPersonalEmail.getText().trim()},
                {"Relative Email",  txtRelativeEmail.getText().trim()},
            };
            for (String[] pair : emailChecks) {
                if (!pair[1].isEmpty() && !isValidEmail(pair[1])) {
                    JOptionPane.showMessageDialog(dialog,
                        "<html><b>" + pair[0] + "</b> is not a valid email address.<br>" +
                        "Example: juan@email.com</html>",
                        "Invalid Email", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            // Step 5: height and weight
            if (!isValidHeight(txtHeight.getText().trim())) {
                JOptionPane.showMessageDialog(dialog,
                    "<html><b>Height</b> must be a number between 50 and 300 cm.</html>",
                    "Invalid Height", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!isValidWeight(txtWeight.getText().trim())) {
                JOptionPane.showMessageDialog(dialog,
                    "<html><b>Weight</b> must be a number between 1 and 500 kg.</html>",
                    "Invalid Weight", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Step 6: persist to DB
            try (Connection conn = DBUtil.getConnection()) {
                conn.setAutoCommit(false);

                String dob = String.format("%04d-%02d-%02d",
                    Integer.parseInt((String) cbDobYear.getSelectedItem()),
                    cbDobMonth.getSelectedIndex() + 1,
                    Integer.parseInt((String) cbDobDay.getSelectedItem()));

                String applicantId = DBUtil.generateApplicantId(conn);

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO applicant_t " +
                        "(applicant_id,name,maiden_name,date_of_birth,place_of_birth," +
                        "present_address,permanent_address,sex,civil_status,blood_type," +
                        "religion,nationality,pwd,solo_parent,occupation,personal_mobile," +
                        "personal_email,relative_contact_no,relative_email,height_cm," +
                        "weight_kg,hair_color,eye_color,other_marks) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {
                    ps.setString(1,  applicantId);
                    ps.setString(2,  txtName.getText().trim());
                    ps.setString(3,  UIFactory.nullIfEmpty(txtMaidenName));
                    ps.setString(4,  dob);
                    ps.setString(5,  txtPlaceOfBirth.getText().trim());
                    ps.setString(6,  txtPresentAddress.getText().trim());
                    ps.setString(7,  txtPermanentAddress.getText().trim());
                    ps.setString(8,  cbSex.getSelectedIndex() == 0 ? "M" : "F");
                    ps.setString(9,  new String[]{"S","M","W","D"}[cbCivilStatus.getSelectedIndex()]);
                    ps.setString(10, (String) cbBloodType.getSelectedItem());
                    ps.setString(11, txtReligion.getText().trim());
                    ps.setString(12, txtNationality.getText().trim());
                    ps.setString(13, cbPwd.getSelectedIndex() == 0 ? "NO" : "YES");
                    ps.setString(14, cbSoloParent.getSelectedIndex() == 0 ? "NO" : "YES");
                    ps.setString(15, txtOccupation.getText().trim());
                    ps.setString(16, UIFactory.nullIfEmpty(txtPersonalNo));
                    ps.setString(17, UIFactory.nullIfEmpty(txtPersonalEmail));
                    ps.setString(18, txtRelativeNo.getText().trim());
                    ps.setString(19, txtRelativeEmail.getText().trim());
                    ps.setString(20, txtHeight.getText().trim());
                    ps.setString(21, txtWeight.getText().trim());
                    ps.setString(22, txtHairColor.getText().trim());
                    ps.setString(23, txtEyeColor.getText().trim());
                    ps.setString(24, UIFactory.nullIfEmpty(txtOtherMarks));
                    ps.executeUpdate();
                }

                String parentSql =
                    "INSERT INTO parent_t (parent_id,applicant_id,parent_type,is_unknown," +
                    "name,married_ln,contact_no,address) VALUES (?,?,?,?,?,?,?,?)";

                String fatherId   = DBUtil.generateParentId(conn);
                boolean fatherUnk = chkFatherUnknown.isSelected();
                try (PreparedStatement ps = conn.prepareStatement(parentSql)) {
                    ps.setString(1, fatherId);  ps.setString(2, applicantId);
                    ps.setString(3, "Father");  ps.setString(4, fatherUnk ? "YES" : "NO");
                    ps.setString(5, fatherUnk ? null : txtFatherName.getText().trim());
                    ps.setNull  (6, Types.VARCHAR);
                    ps.setString(7, fatherUnk ? null : txtFatherContact.getText().trim());
                    ps.setString(8, fatherUnk ? null : txtFatherAddress.getText().trim());
                    ps.executeUpdate();
                }

                String motherId   = DBUtil.generateParentId(conn);
                boolean motherUnk = chkMotherUnknown.isSelected();
                try (PreparedStatement ps = conn.prepareStatement(parentSql)) {
                    ps.setString(1, motherId);  ps.setString(2, applicantId);
                    ps.setString(3, "Mother");  ps.setString(4, motherUnk ? "YES" : "NO");
                    ps.setString(5, motherUnk ? null : txtMotherName.getText().trim());
                    ps.setString(6, motherUnk ? null : txtMotherMarriedLast.getText().trim());
                    ps.setString(7, motherUnk ? null : txtMotherContact.getText().trim());
                    ps.setString(8, motherUnk ? null : txtMotherAddress.getText().trim());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO emergency_t " +
                        "(applicant_id,ec_name,ec_contact_no,ec_relationship,ec_address) " +
                        "VALUES (?,?,?,?,?)")) {
                    ps.setString(1, applicantId);
                    ps.setString(2, txtEmergName.getText().trim());
                    ps.setString(3, txtEmergContact.getText().trim());
                    ps.setString(4, txtEmergRelationship.getText().trim());
                    ps.setString(5, txtEmergAddress.getText().trim());
                    ps.executeUpdate();
                }

                conn.commit();

                // Success dialog
                JPanel successPanel = new JPanel();
                successPanel.setLayout(new BoxLayout(successPanel, BoxLayout.Y_AXIS));
                successPanel.setBackground(Constants.C_SURFACE);
                successPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

                JLabel successTitle = new JLabel("Application Submitted!", JLabel.CENTER);
                successTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
                successTitle.setForeground(Constants.C_TEXT_PRIMARY);
                successTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel successDetails = new JLabel(
                    "<html><div style='text-align:center;width:280px;'>" +
                    "Your application has been recorded.<br><br>" +
                    "<b>Applicant ID:</b> " + applicantId + "<br>" +
                    "<b>Father ID:</b> " + (fatherUnk ? "Unknown/Deceased" : fatherId) + "<br>" +
                    "<b>Mother ID:</b> " + (motherUnk ? "Unknown/Deceased" : motherId) +
                    "</div></html>", JLabel.CENTER);
                successDetails.setFont(Constants.F_BODY);
                successDetails.setForeground(Constants.C_TEXT_MUTED);
                successDetails.setAlignmentX(Component.CENTER_ALIGNMENT);

                successPanel.add(Box.createVerticalStrut(8));
                successPanel.add(successTitle);
                successPanel.add(Box.createVerticalStrut(10));
                successPanel.add(successDetails);

                JOptionPane.showMessageDialog(dialog, successPanel,
                    "Success", JOptionPane.PLAIN_MESSAGE);
                dialog.dispose();

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog,
                    "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        };

        submitBtn.addActionListener(e -> doSubmit.run());
        dialog.setVisible(true);
    }

    // ── Toggle parent fields ──────────────────────────────────────────────────
    private static void toggleFields(boolean unknown, JTextField[] fields) {
        for (JTextField f : fields) {
            f.setEnabled(!unknown);
            f.setText("");
            f.setBackground(unknown ? Constants.C_BG : Constants.C_SURFACE);
        }
    }

    // ── Styled checkbox ───────────────────────────────────────────────────────
    private static JCheckBox styledCheckbox(String label) {
        JCheckBox cb = new JCheckBox(label);
        cb.setFont(Constants.F_SMALL);
        cb.setForeground(Constants.C_DANGER);
        cb.setBackground(Constants.C_SURFACE);
        cb.setFocusPainted(false);
        cb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return cb;
    }

    // ── 2-column section builder ──────────────────────────────────────────────
    private static JPanel buildSection(String title, Color accent, Object[][] rows) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.C_SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.C_BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(18, 24, 20, 24));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(accent);
        header.add(titleLbl, BorderLayout.WEST);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        card.add(header);

        JPanel divider = new JPanel();
        divider.setBackground(new Color(accent.getRed(), accent.getGreen(),
                                        accent.getBlue(), 40));
        divider.setPreferredSize(new Dimension(0, 1));
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(divider);
        card.add(Box.createVerticalStrut(14));

        JPanel grid = new JPanel(new GridLayout(rows.length, 2, 18, 10));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (Object[] row : rows) {
            grid.add(buildFieldCell((String) row[0], (Component) row[1]));
            if (row[2] != null)
                grid.add(buildFieldCell((String) row[2], (Component) row[3]));
            else
                grid.add(new JPanel() {{ setOpaque(false); }});
        }
        card.add(grid);
        return card;
    }

    // ── Parent section builder ────────────────────────────────────────────────
    private static JPanel buildParentSection(String title, JCheckBox unknownChk,
                                             Object[][] rows) {
        Color accent = new Color(100, 60, 180);

        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.C_SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.C_BORDER);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(18, 24, 20, 24));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(accent);

        JPanel chkPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        chkPanel.setOpaque(false);
        JLabel optTag = new JLabel("optional");
        optTag.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        optTag.setForeground(Constants.C_TEXT_LIGHT);
        chkPanel.add(optTag);
        chkPanel.add(unknownChk);

        header.add(titleLbl, BorderLayout.WEST);
        header.add(chkPanel, BorderLayout.EAST);
        card.add(header);

        JPanel divider = new JPanel();
        divider.setBackground(new Color(accent.getRed(), accent.getGreen(),
                                        accent.getBlue(), 40));
        divider.setPreferredSize(new Dimension(0, 1));
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(divider);
        card.add(Box.createVerticalStrut(10));

        JPanel notice = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        notice.setBackground(Constants.C_DANGER_BG);
        notice.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Constants.C_DANGER_BORDER, 1, true),
            BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        notice.setAlignmentX(Component.LEFT_ALIGNMENT);
        notice.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JLabel noticeIcon = new JLabel("\u24d8");
        noticeIcon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        noticeIcon.setForeground(Constants.C_DANGER);
        JLabel noticeText = new JLabel("This parent will be recorded as Unknown / Deceased.");
        noticeText.setFont(Constants.F_SMALL);
        noticeText.setForeground(Constants.C_DANGER);
        notice.add(noticeIcon);
        notice.add(noticeText);
        notice.setVisible(false);
        card.add(notice);
        card.add(Box.createVerticalStrut(10));

        JPanel grid = new JPanel(new GridLayout(rows.length, 2, 18, 10));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (Object[] row : rows) {
            grid.add(buildFieldCell((String) row[0], (Component) row[1]));
            if (row[2] != null)
                grid.add(buildFieldCell((String) row[2], (Component) row[3]));
            else
                grid.add(new JPanel() {{ setOpaque(false); }});
        }
        card.add(grid);

        unknownChk.addActionListener(e -> {
            boolean unknown = unknownChk.isSelected();
            notice.setVisible(unknown);
            for (Component child : grid.getComponents()) {
                child.setEnabled(!unknown);
                if (child instanceof JPanel)
                    for (Component inner : ((JPanel) child).getComponents())
                        inner.setEnabled(!unknown);
            }
            card.revalidate();
            card.repaint();
        });

        return card;
    }

    // ── Single field cell ─────────────────────────────────────────────────────
    private static JPanel buildFieldCell(String labelText, Component field) {
        JPanel cell = new JPanel();
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        cell.setOpaque(false);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(Constants.C_TEXT_MUTED);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setMaximumSize(new Dimension(Integer.MAX_VALUE,
            field.getPreferredSize().height + 2));
        if (field instanceof JComponent)
            ((JComponent) field).setAlignmentX(Component.LEFT_ALIGNMENT);

        cell.add(lbl);
        cell.add(field);
        return cell;
    }
}