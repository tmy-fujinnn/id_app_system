import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Modal login dialog.  Call {@link #wasSuccessful()} after construction to
 * determine whether the user authenticated successfully.
 */
public class LoginDialog extends JDialog {

    private final JTextField     usernameField;
    private final JPasswordField passwordField;
    private boolean successful;

    public LoginDialog() {
        super((Frame) null, "QEasy — Sign in", true);
        setSize(420, 640);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Constants.C_PRIMARY);

        // ── Branding strip ──────────────────────────────────────────────────
        JPanel brand = new JPanel();
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBackground(Constants.C_PRIMARY);
        brand.setBorder(BorderFactory.createEmptyBorder(28, 32, 24, 32));

        // Load logo — fills the full dialog width minus padding.
        // Falls back to plain "QEasy" text if logo.png is missing.
        ImageIcon logoIcon = UIFactory.loadIcon("QC_logo.png", 50);
        if (logoIcon != null) {
            JLabel logoLbl = new JLabel(logoIcon);
            logoLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            brand.add(logoLbl);
        } else {
            JLabel appName = new JLabel("QEasy");
            appName.setFont(new Font("Segoe UI", Font.BOLD, 28));
            appName.setForeground(Color.WHITE);
            appName.setAlignmentX(Component.CENTER_ALIGNMENT);
            brand.add(appName);
        }

        // Thin divider between logo and title text
        brand.add(Box.createVerticalStrut(14));

        // "ID Application System" title under the logo
        JLabel sysTitle = new JLabel("ID Application System", JLabel.CENTER);
        sysTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        sysTitle.setForeground(Color.WHITE);
        sysTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        brand.add(sysTitle);

        // Subtitle / portal name
        JLabel sysSubtitle = new JLabel("Quezon City e-Services Portal", JLabel.CENTER);
        sysSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        sysSubtitle.setForeground(new Color(255, 255, 255, 150));
        sysSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        brand.add(Box.createVerticalStrut(3));
        brand.add(sysSubtitle);

        // ── White card form ──────────────────────────────────────────────────
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Constants.C_SURFACE);
        card.setBorder(BorderFactory.createEmptyBorder(32, 36, 36, 36));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx   = 0;
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        JLabel signInLbl = new JLabel("Sign in to manage your ID application", JLabel.CENTER);
        signInLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        signInLbl.setForeground(Constants.C_TEXT_MUTED);
        gc.gridy = 0; gc.insets = new Insets(0, 0, 24, 0);
        card.add(signInLbl, gc);

        gc.gridy = 1; gc.insets = new Insets(0, 0, 6, 0);
        card.add(makeLabel("Username *"), gc);

        usernameField = new JTextField();
        styleField(usernameField);
        gc.gridy = 2; gc.insets = new Insets(0, 0, 16, 0);
        card.add(usernameField, gc);

        gc.gridy = 3; gc.insets = new Insets(0, 0, 6, 0);
        card.add(makeLabel("Password *"), gc);

        passwordField = new JPasswordField();
        styleField(passwordField);
        gc.gridy = 4; gc.insets = new Insets(0, 0, 24, 0);
        card.add(passwordField, gc);

        JButton loginBtn = UIFactory.createPrimaryButton("Sign in");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gc.gridy = 5; gc.insets = new Insets(0, 0, 20, 0);
        card.add(loginBtn, gc);

        JLabel secured = new JLabel("Secured by QC e-Services", JLabel.CENTER);
        secured.setFont(Constants.F_CAPTION);
        secured.setForeground(Constants.C_TEXT_LIGHT);
        gc.gridy = 6; gc.insets = new Insets(0, 0, 0, 0);
        card.add(secured, gc);

        // ── Footer ───────────────────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 10));
        footer.setBackground(Constants.C_HEADER_BG);
        for (String f : new String[]{
                "Apply in under 10 minutes",
                "Admin-Use Prototype",
                "Real-time status updates" }) {
            JLabel l = new JLabel(f);
            l.setFont(Constants.F_CAPTION);
            l.setForeground(new Color(255, 255, 255, 180));
            footer.add(l);
        }

        loginBtn.addActionListener(e -> validateCredentials());
        passwordField.addActionListener(e -> validateCredentials());

        root.add(brand,  BorderLayout.NORTH);
        root.add(card,   BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
        add(root);
        setVisible(true);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(Constants.C_ACCENT);
        return l;
    }

    private static void styleField(JTextField f) {
        f.setFont(Constants.F_BODY);
        f.setBackground(Constants.C_SURFACE);
        f.setForeground(Constants.C_TEXT_PRIMARY);
        f.setCaretColor(Constants.C_TEXT_PRIMARY);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
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
    }

    private void validateCredentials() {
        String u = usernameField.getText().trim();
        String p = new String(passwordField.getPassword());
        if (u.equals(Constants.CORRECT_USERNAME) && p.equals(Constants.CORRECT_PASSWORD)) {
            successful = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                "Invalid username or password. Please try again.",
                "Sign in failed", JOptionPane.WARNING_MESSAGE);
            passwordField.setText("");
            usernameField.requestFocus();
        }
    }

    /** @return {@code true} if the user signed in successfully. */
    public boolean wasSuccessful() { return successful; }
}