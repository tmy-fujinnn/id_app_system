import java.awt.*;

/**
 * CONSTANTS FILES
 * 		- Used for setting the colors and theme for the application
 * 		- Called on different parts of the application to set the colors
 * 		- Contains all usernames & passwords for Log In and Database Connection
 */
public final class Constants {

    private Constants() {}   // utility class — no instances

    // PALLETTE USED FOR APPLICATION (hue colors)
    public static final Color C_PRIMARY        = new Color(28,  63, 120);
    public static final Color C_PRIMARY_DARK   = new Color(20,  47,  93);
    public static final Color C_ACCENT         = new Color(37,  99, 235);
    public static final Color C_DANGER         = new Color(220, 38,  38);
    public static final Color C_DANGER_BG      = new Color(255, 241, 241);
    public static final Color C_DANGER_BORDER  = new Color(252, 165, 165);
    public static final Color C_SUCCESS        = new Color(22, 163,  74);
    public static final Color C_BG             = new Color(248, 250, 252);
    public static final Color C_SURFACE        = Color.WHITE;
    public static final Color C_BORDER         = new Color(226, 232, 240);
    public static final Color C_TEXT_PRIMARY   = new Color(15,  23,  42);
    public static final Color C_TEXT_MUTED     = new Color(100, 116, 139);
    public static final Color C_TEXT_LIGHT     = new Color(148, 163, 184);
    public static final Color C_HEADER_BG      = new Color(28,  63, 120);
    public static final Color C_INPUT_FOCUS    = new Color(219, 234, 254);

    // FONT USED 
    public static final Font F_DISPLAY = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font F_TITLE   = new Font("Segoe UI", Font.BOLD,  15);
    public static final Font F_LABEL   = new Font("Segoe UI", Font.BOLD,  11);
    public static final Font F_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font F_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font F_CAPTION = new Font("Segoe UI", Font.PLAIN, 10);

    // FOR LOGIN INFORMATION OF ADMIN
    public static final String CORRECT_USERNAME = "admin";
    public static final String CORRECT_PASSWORD = "password123";

    // FOR DATABASE ESTABLISHMENT AND CONNECTION
    public static final String DB_URL  = "jdbc:mysql://localhost:3306/im_db?serverTimezone=UTC";
    public static final String DB_USER = "root";
    public static final String DB_PASS = "Boybatchoy@0726";
}