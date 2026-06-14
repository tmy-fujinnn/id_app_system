import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * MAIN HOME SCREEN
 * 	- The main dashboard shown after a successful login.
 * 	- Displays a styled nav bar, welcome header, four action cards, and a stat bar.
 */

public class HomeScreen {

    public static void show() {
        JFrame frame = new JFrame("QEasy \u2014 ID Application Portal");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 700);
        frame.setMinimumSize(new Dimension(860, 540));
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Constants.C_BG);

        root.add(buildNav(frame),     BorderLayout.NORTH);
        root.add(buildContent(frame), BorderLayout.CENTER);

        frame.add(root);
        frame.setVisible(true);
    }

    // ── NAV BAR ───────────────────────────────────────────────────────────────

    private static JPanel buildNav(JFrame frame) {
        // Gradient nav panel
        JPanel nav = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(20, 47, 93),
                    getWidth(), 0, new Color(28, 63, 120));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        nav.setOpaque(false);
        nav.setPreferredSize(new Dimension(0, 64));
        nav.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 28));

        // ── Left: logo ────────────────────────────────────────────────────────
        ImageIcon navLogoIcon = UIFactory.loadIcon("logo.png", 42);
        JLabel logoMark;
        if (navLogoIcon != null) {
            logoMark = new JLabel(navLogoIcon);
        } else {
            logoMark = new JLabel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(255, 255, 255, 40));
                    g2.fillOval(0, 0, 38, 38);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString("Q",
                        (38 - fm.stringWidth("Q")) / 2,
                        (38 + fm.getAscent() - fm.getDescent()) / 2);
                }
            };
            logoMark.setPreferredSize(new Dimension(38, 38));
        }

        JPanel logoText = new JPanel();
        logoText.setOpaque(false);
        logoText.setLayout(new BoxLayout(logoText, BoxLayout.Y_AXIS));
        JLabel logoName = new JLabel("QEasy");
        logoName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logoName.setForeground(Color.WHITE);
        JLabel logoSub = new JLabel("ID APPLICATION SYSTEM");
        logoSub.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        logoSub.setForeground(new Color(255, 255, 255, 150));
        logoText.add(logoName);
        logoText.add(logoSub);

        JPanel navLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        navLeft.setOpaque(false);
        navLeft.add(logoMark);
        navLeft.add(logoText);

        JPanel navLeftWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 12));
        navLeftWrapper.setOpaque(false);
        navLeftWrapper.add(navLeft);

        // ── Right: role badge + logout ────────────────────────────────────────
        // Role badge pill
        JLabel roleBadge = new JLabel("Administrator") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 25));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                super.paintComponent(g);
            }
        };
        roleBadge.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        roleBadge.setForeground(new Color(255, 255, 255, 200));
        roleBadge.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        roleBadge.setOpaque(false);
        
        // disposes home screen then goes to login page
        JButton logoutBtn = UIFactory.createOutlineButton("Logout");
        logoutBtn.addActionListener(e -> {
            frame.dispose();
            LoginDialog login = new LoginDialog();
            if (login.wasSuccessful()) {
                HomeScreen.show();
            } else {
                System.exit(0);
            }
        });

        JPanel navRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        navRight.setOpaque(false);
        navRight.add(roleBadge);
        navRight.add(logoutBtn);

        nav.add(navLeftWrapper, BorderLayout.WEST);
        nav.add(navRight,       BorderLayout.EAST);

        // Bottom separator line
        JPanel navWrapper = new JPanel(new BorderLayout());
        navWrapper.setOpaque(false);
        navWrapper.add(nav, BorderLayout.CENTER);
        JPanel sep = new JPanel();
        sep.setBackground(new Color(255, 255, 255, 30));
        sep.setPreferredSize(new Dimension(0, 1));
        navWrapper.add(sep, BorderLayout.SOUTH);
        return navWrapper;
    }

    // ── CONTENT ───────────────────────────────────────────────────────────────

    private static JScrollPane buildContent(JFrame frame) {
        // Gradient background panel
        JPanel content = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(248, 250, 252),
                    0, getHeight(), new Color(237, 242, 248));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(36, 48, 36, 48));

        // ── Welcome header ────────────────────────────────────────────────────
        content.add(buildWelcomeHeader());
        content.add(Box.createVerticalStrut(28));

        // ── Section label ──────────────────────────────────────────────────────
        JLabel sectionLbl = new JLabel("Quick Actions");
        sectionLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        sectionLbl.setForeground(Constants.C_TEXT_MUTED);
        sectionLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(sectionLbl);
        content.add(Box.createVerticalStrut(12));

        // ── 2x2 card grid ─────────────────────────────────────────────────────
        JPanel grid = new JPanel(new GridLayout(2, 2, 16, 16));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));

        // statBarHolder wraps the stat bar so we can swap it out on refresh
        JPanel statBarHolder = new JPanel(new BorderLayout());
        statBarHolder.setOpaque(false);
        statBarHolder.setAlignmentX(Component.LEFT_ALIGNMENT);
        statBarHolder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        statBarHolder.add(buildStatBar(), BorderLayout.CENTER);

        // Refreshes the stat bar by rebuilding it from fresh DB counts
        Runnable refreshStats = () -> {
            statBarHolder.removeAll();
            statBarHolder.add(buildStatBar(), BorderLayout.CENTER);
            statBarHolder.revalidate();
            statBarHolder.repaint();
        };

        Object[][] cards = {
            { "Apply for ID",      "Submit a new application", "apply.png",  false, Constants.C_ACCENT },
            { "View applications", "Browse all records",       "view.png",   false, new Color(16, 124, 88) },
            { "Update record",     "Edit applicant details",   "update.png", false, new Color(100, 60, 180) },
            { "Delete record",     "Remove an application",    "delete.png", true,  Constants.C_DANGER },
        };

        for (Object[] cd : cards) {
            boolean danger  = (Boolean) cd[3];
            Color   accent  = (Color)   cd[4];
            JPanel card = buildActionCard(
                (String) cd[0], (String) cd[1], (String) cd[2], danger, accent);

            card.addMouseListener(new MouseAdapter() {
                Color normalBg = Constants.C_SURFACE;
                public void mouseClicked(MouseEvent e) {
                    switch ((String) cd[0]) {
                        case "Apply for ID"      -> { ApplicationForm.open(frame); refreshStats.run(); }
                        case "View applications" -> ViewRecords.open(frame);
                        case "Delete record"     -> { DeleteRecord.open(frame);    refreshStats.run(); }
                        case "Update record"     -> { UpdateRecord.open(frame);    refreshStats.run(); }
                        default -> JOptionPane.showMessageDialog(
                            frame, cd[0] + " \u2014 coming soon.");
                    }
                }
                public void mouseEntered(MouseEvent e) {
                    card.setBackground(danger
                        ? new Color(255, 245, 245)
                        : new Color(245, 248, 255));
                    card.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    card.repaint();
                }
                public void mouseExited(MouseEvent e) {
                    card.setBackground(normalBg);
                    card.repaint();
                }
            });
            grid.add(card);
        }
        content.add(grid);

        // ── Stat bar ──────────────────────────────────────────────────────────
        content.add(Box.createVerticalStrut(32));
        content.add(statBarHolder);

        // Wrap in a gradient-bg scroll pane
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(248, 250, 252),
                    0, getHeight(), new Color(237, 242, 248));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        wrapper.setOpaque(false);
        wrapper.add(content, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(wrapper);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    // ── WELCOME HEADER ────────────────────────────────────────────────────────

    private static JPanel buildWelcomeHeader() {
        // Card with left accent bar
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                // White card bg
                g2.setColor(Constants.C_SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                // Left accent bar
                g2.setColor(Constants.C_ACCENT);
                g2.fillRoundRect(0, 0, 5, getHeight(), 4, 4);
                // Subtle shadow line at bottom
                g2.setColor(new Color(0, 0, 0, 12));
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Left: greeting text
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel greetLbl = new JLabel("Welcome back, Admin");
        greetLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        greetLbl.setForeground(Constants.C_TEXT_PRIMARY);

        JLabel subLbl = new JLabel("Quezon City ID Application Portal \u2014 manage applicant records below.");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(Constants.C_TEXT_MUTED);

        textPanel.add(greetLbl);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subLbl);

        // Right: date/time
        String dateStr = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"));
        JLabel dateLbl = new JLabel(dateStr);
        dateLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        dateLbl.setForeground(Constants.C_TEXT_LIGHT);
        dateLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        card.add(textPanel, BorderLayout.CENTER);
        card.add(dateLbl,   BorderLayout.EAST);
        return card;
    }

    // ── ACTION CARD ───────────────────────────────────────────────────────────

    private static JPanel buildActionCard(String title, String subtitle,
                                          String iconFile, boolean danger,
                                          Color accent) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                // Card background
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                // Left accent stripe
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                // Bottom shadow line
                g2.setColor(new Color(0, 0, 0, 10));
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(danger ? Constants.C_DANGER_BORDER : Constants.C_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            }
        };
        card.setLayout(new BorderLayout());
        card.setBackground(Constants.C_SURFACE);
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 20));

        // ── Left: icon in tinted pill ──────────────────────────────────────────
        Color iconBg = new Color(accent.getRed(), accent.getGreen(),
                                 accent.getBlue(), 20);
        JPanel iconWrap = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        };
        iconWrap.setOpaque(false);
        iconWrap.setPreferredSize(new Dimension(60, 60));
        iconWrap.setLayout(new GridBagLayout());

        ImageIcon cardIcon = UIFactory.loadIcon(iconFile, 32);
        JLabel iconLbl = (cardIcon != null) ? new JLabel(cardIcon) : new JLabel("?");
        iconLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        iconLbl.setForeground(accent);
        iconWrap.add(iconLbl);

        // ── Center: title + subtitle ───────────────────────────────────────────
        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(danger ? Constants.C_DANGER : Constants.C_TEXT_PRIMARY);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(Constants.C_TEXT_MUTED);

        // "Click to open" hint
        JLabel hintLbl = new JLabel("Click to open \u2192");
        hintLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        hintLbl.setForeground(accent);

        textPanel.add(titleLbl);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subLbl);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(hintLbl);

        card.add(iconWrap,  BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    // ── STAT BAR ──────────────────────────────────────────────────────────────

    private static JPanel buildStatBar() {
        // Fetch live counts from DB
        int totalApplicants = 0, totalToday = 0;
        String mostRecentName = "—";
        try (Connection conn = DBUtil.getConnection()) {
            try (Statement st = conn.createStatement()) { // No. of Applicants
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM applicant_t");
                if (rs.next()) totalApplicants = rs.getInt(1);
            }
            try (Statement st = conn.createStatement()) { // No. of Applicants TODAY
                ResultSet rs = st.executeQuery(
                    "SELECT COUNT(*) FROM applicant_t " +
                    "WHERE DATE(created_at) = CURDATE()");
                if (rs.next()) totalToday = rs.getInt(1);
            }
            try (Statement st = conn.createStatement()) {
                ResultSet rs = st.executeQuery(
                    "SELECT name FROM applicant_t ORDER BY applicant_id DESC LIMIT 1"); // Name of recent applicant
                if (rs.next()) mostRecentName = rs.getString(1);
            }
        } catch (SQLException ex) {
            // DB columns may not exist yet — silently ignore, show 0s
        }

        JPanel bar = new JPanel(new GridLayout(1, 3, 16, 0));
        bar.setOpaque(false);
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        bar.add(buildStatCard("Total Applicants", String.valueOf(totalApplicants),
            "All registered records", Constants.C_ACCENT));
        bar.add(buildStatCard("Applied Today", String.valueOf(totalToday),
            "New submissions today", new Color(16, 124, 88)));
        bar.add(buildStatCard("Most Recent", mostRecentName,
            "Latest applicant on record", new Color(100, 60, 180)));

        return bar;
    }

    private static JPanel buildStatCard(String label, String value,
                                        String sub, Color accent) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.C_SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                // Top accent bar
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
                g2.setColor(new Color(0, 0, 0, 10));
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Constants.C_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(0, 90));
        card.setMinimumSize(new Dimension(0, 90));
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        boolean isNumeric = value.matches("\\d+");

        // For numeric values: large bold number
        // For name values: use an HTML label so long names wrap inside the card
        JLabel valueLbl;
        if (isNumeric) {
            valueLbl = new JLabel(value);
            valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        } else {
            valueLbl = new JLabel("<html><div style='width:140px;'>" + value + "</div></html>");
            valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        }
        valueLbl.setForeground(accent);
        valueLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel labelLbl = new JLabel(label);
        labelLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelLbl.setForeground(Constants.C_TEXT_PRIMARY);
        labelLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLbl = new JLabel(sub);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        subLbl.setForeground(Constants.C_TEXT_LIGHT);
        subLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Push content to the bottom so all cards align their labels at the same vertical position
        card.add(Box.createVerticalGlue());
        card.add(valueLbl);
        card.add(Box.createVerticalStrut(2));
        card.add(labelLbl);
        card.add(subLbl);
        return card;
    }
}