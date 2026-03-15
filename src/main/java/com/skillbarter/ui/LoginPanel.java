package com.skillbarter.ui;

import com.skillbarter.model.User;
import com.skillbarter.service.UserService;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Optional;

/**
 * FIXED LoginPanel — dark espresso theme.
 * Root cause of previous issues:
 *   - BoxLayout with setMaximumSize was causing components to stretch outside card
 *   - Fields had no visible border in Swing's default L&F
 *   - Card was not using a proper fixed-size centered panel
 *
 * Fix: Use GridBagLayout inside the card for precise control of every row.
 * Fields use a custom border with visible gold stroke.
 */
public class LoginPanel extends JPanel {

    // ── Colours ────────────────────────────────────────────────
    static final Color BG_DEEP    = new Color(0x0E0602);
    static final Color BG_CARD    = new Color(0x1A0A02);
    static final Color BG_FIELD   = new Color(0x140803);
    static final Color GOLD       = new Color(0xC49A6C);
    static final Color GOLD_LIGHT = new Color(0xEDD9C0);
    static final Color GOLD_MUTED = new Color(0xA0714F);
    static final Color BORDER_DIM = new Color(0x6B4226);
    static final Color ERROR_RED  = new Color(0xF09595);

    private final UserService userService;
    private final MainApp     mainApp;

    private JTextField     emailField;
    private JPasswordField passField;
    private JLabel         statusMsg;
    private JButton        loginBtn;

    public LoginPanel(UserService userService, MainApp mainApp) {
        this.userService = userService;
        this.mainApp     = mainApp;
        setLayout(new GridBagLayout()); // centres the card
        setBackground(BG_DEEP);
        add(buildCard());
    }

    // ── Background glow ────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(BG_DEEP);
        g2.fillRect(0, 0, getWidth(), getHeight());
        // top-left glow
        g2.setPaint(new RadialGradientPaint(
            new Point2D.Float(0, 0), 400,
            new float[]{0f, 1f},
            new Color[]{new Color(0x8B5E3C, false) {
                { /* alpha 30 */}
                @Override public int getAlpha() { return 45; }
            }, new Color(0, 0, 0, 0)}));
        g2.fillOval(-200, -200, 600, 600);
        // bottom-right glow
        g2.setPaint(new RadialGradientPaint(
            new Point2D.Float(getWidth(), getHeight()), 350,
            new float[]{0f, 1f},
            new Color[]{new Color(139, 94, 60, 30), new Color(0, 0, 0, 0)}));
        g2.fillOval(getWidth() - 350, getHeight() - 350, 700, 700);
        g2.dispose();
    }

    // ── Card ───────────────────────────────────────────────────
    private JPanel buildCard() {
        // Outer card panel with custom paint
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Card background
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                // Card border
                g2.setColor(new Color(196, 154, 108, 60));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(400, 500));
        card.setBorder(new EmptyBorder(32, 36, 32, 36));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 0, 0);

        // ── Logo row ────────────────────────────────────────
        card.add(buildLogoRow(), c);

        // ── Tab bar ─────────────────────────────────────────
        c.gridy++; c.insets = new Insets(18, 0, 0, 0);
        card.add(buildTabBar(), c);

        // ── Title ───────────────────────────────────────────
        c.gridy++; c.insets = new Insets(22, 0, 4, 0);
        JLabel title = new JLabel("Welcome Back", SwingConstants.CENTER);
        title.setFont(new Font("Georgia", Font.BOLD, 22));
        title.setForeground(GOLD_LIGHT);
        card.add(title, c);

        // ── Subtitle ────────────────────────────────────────
        c.gridy++; c.insets = new Insets(0, 0, 24, 0);
        JLabel sub = new JLabel("Sign in to continue trading skills", SwingConstants.CENTER);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(GOLD_MUTED);
        card.add(sub, c);

        // ── Email label ──────────────────────────────────────
        c.gridy++; c.insets = new Insets(0, 0, 5, 0);
        card.add(buildFieldLabel("EMAIL ADDRESS"), c);

        // ── Email field ──────────────────────────────────────
        c.gridy++; c.insets = new Insets(0, 0, 14, 0);
        emailField = buildTextField("you@example.com");
        card.add(emailField, c);

        // ── Password label ───────────────────────────────────
        c.gridy++; c.insets = new Insets(0, 0, 5, 0);
        card.add(buildFieldLabel("PASSWORD"), c);

        // ── Password field ───────────────────────────────────
        c.gridy++; c.insets = new Insets(0, 0, 6, 0);
        passField = buildPasswordField("Enter your password");
        card.add(passField, c);

        // ── Forgot password (right-aligned) ──────────────────
        c.gridy++; c.insets = new Insets(0, 0, 14, 0);
        card.add(buildForgotRow(), c);

        // ── Status message ───────────────────────────────────
        c.gridy++; c.insets = new Insets(0, 0, 8, 0);
        statusMsg = new JLabel(" ", SwingConstants.CENTER);
        statusMsg.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusMsg.setForeground(ERROR_RED);
        card.add(statusMsg, c);

        // ── Login button ─────────────────────────────────────
        c.gridy++; c.insets = new Insets(0, 0, 16, 0);
        loginBtn = buildPrimaryButton("LOGIN  →");
        loginBtn.addActionListener(e -> handleLogin());
        card.add(loginBtn, c);

        // ── Divider ──────────────────────────────────────────
        c.gridy++; c.insets = new Insets(0, 0, 12, 0);
        card.add(buildDivider("NEW TO SKILL BARTERS?"), c);

        // ── Ghost register button ────────────────────────────
        c.gridy++; c.insets = new Insets(0, 0, 0, 0);
        JButton regBtn = buildGhostButton("Create a free account  →");
        regBtn.addActionListener(e -> mainApp.showRegisterPanel());
        card.add(regBtn, c);

        // Enter key
        passField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) handleLogin();
            }
        });

        return card;
    }

    // ── Login logic ────────────────────────────────────────────
    private void handleLogin() {
        String email    = emailField.getText().trim();
        String password = new String(passField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            setMsg("Please fill in all fields.", false); return;
        }
        if (!email.contains("@")) {
            setMsg("Enter a valid email address.", false); return;
        }
        loginBtn.setEnabled(false);
        setMsg("Signing you in...", true);

        new SwingWorker<Optional<User>, Void>() {
            @Override protected Optional<User> doInBackground() {
                return userService.login(email, password);
            }
            @Override protected void done() {
                try {
                    Optional<User> result = get();
                    if (result.isPresent()) {
                        setMsg("Login successful!", true);
                        mainApp.onLoginSuccess(result.get());
                    } else {
                        setMsg("Invalid email or password.", false);
                    }
                } catch (Exception ex) {
                    setMsg("Error: " + ex.getMessage(), false);
                } finally {
                    loginBtn.setEnabled(true);
                }
            }
        }.execute();
    }

    public void clearFields() {
        if (emailField != null) emailField.setText("");
        if (passField  != null) passField.setText("");
        if (statusMsg  != null) statusMsg.setText(" ");
        if (loginBtn   != null) loginBtn.setEnabled(true);
    }

    private void setMsg(String text, boolean info) {
        statusMsg.setText(text.isEmpty() ? " " : text);
        statusMsg.setForeground(info ? GOLD_MUTED : ERROR_RED);
    }

    // ══════════════════════════════════════════════════════════
    //  COMPONENT BUILDERS
    // ══════════════════════════════════════════════════════════

    private JPanel buildLogoRow() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setOpaque(false);

        JPanel emblem = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x2C1A0E));
                g2.fillOval(0, 0, 38, 38);
                g2.setColor(new Color(196, 154, 108, 100));
                g2.setStroke(new BasicStroke(1f));
                g2.drawOval(0, 0, 37, 37);
                g2.setColor(GOLD);
                g2.setFont(new Font("Georgia", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("SB", 19 - fm.stringWidth("SB") / 2, 19 + fm.getAscent() / 2 - 2);
                g2.dispose();
            }
        };
        emblem.setOpaque(false);
        emblem.setPreferredSize(new Dimension(38, 38));
        emblem.setMinimumSize(new Dimension(38, 38));
        emblem.setMaximumSize(new Dimension(38, 38));
        emblem.setAlignmentY(Component.CENTER_ALIGNMENT);

        JLabel brand = new JLabel("Skill Barters");
        brand.setFont(new Font("Georgia", Font.BOLD, 20));
        brand.setForeground(GOLD_LIGHT);
        brand.setAlignmentY(Component.CENTER_ALIGNMENT);

        p.add(Box.createHorizontalGlue());
        p.add(emblem);
        p.add(Box.createHorizontalStrut(10));
        p.add(brand);
        p.add(Box.createHorizontalGlue());
        return p;
    }

    private JPanel buildTabBar() {
        JPanel p = new JPanel(new GridLayout(1, 2, 0, 0));
        p.setOpaque(true);
        p.setBackground(new Color(0x0A0401));
        p.setBorder(new LineBorder(BORDER_DIM, 1, true));
        p.setPreferredSize(new Dimension(328, 38));

        JButton loginTab = new JButton("Login");
        styleTabButton(loginTab, true);

        JButton regTab = new JButton("Create Account");
        styleTabButton(regTab, false);
        regTab.addActionListener(e -> mainApp.showRegisterPanel());

        p.add(loginTab);
        p.add(regTab);
        return p;
    }

    private void styleTabButton(JButton b, boolean active) {
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        if (active) {
            b.setBackground(GOLD);
            b.setForeground(new Color(0x0E0602));
        } else {
            b.setBackground(new Color(0x0A0401));
            b.setForeground(GOLD_MUTED);
        }
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /**
     * KEY FIX: Fields use a visible compound border:
     *   outer = LineBorder with gold colour (visible against dark bg)
     *   inner = EmptyBorder for padding
     * Background set explicitly to BG_FIELD so it contrasts against the card.
     */
    private JTextField buildTextField(String placeholder) {
        JTextField f = new JTextField(20);
        f.setBackground(BG_FIELD);
        f.setForeground(GOLD_LIGHT);
        f.setCaretColor(GOLD);
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setOpaque(true);
        // Visible border — this is the main fix
        f.setBorder(new CompoundBorder(
            new LineBorder(BORDER_DIM, 1, true),
            new EmptyBorder(9, 12, 9, 12)
        ));
        f.setPreferredSize(new Dimension(328, 42));

        // Gold focus border
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                f.setBorder(new CompoundBorder(
                    new LineBorder(GOLD, 1, true),
                    new EmptyBorder(9, 12, 9, 12)));
            }
            @Override public void focusLost(FocusEvent e) {
                f.setBorder(new CompoundBorder(
                    new LineBorder(BORDER_DIM, 1, true),
                    new EmptyBorder(9, 12, 9, 12)));
            }
        });
        return f;
    }

    private JPasswordField buildPasswordField(String placeholder) {
        JPasswordField f = new JPasswordField(20);
        f.setBackground(BG_FIELD);
        f.setForeground(GOLD_LIGHT);
        f.setCaretColor(GOLD);
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setOpaque(true);
        f.setBorder(new CompoundBorder(
            new LineBorder(BORDER_DIM, 1, true),
            new EmptyBorder(9, 12, 9, 12)
        ));
        f.setPreferredSize(new Dimension(328, 42));

        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                f.setBorder(new CompoundBorder(
                    new LineBorder(GOLD, 1, true),
                    new EmptyBorder(9, 12, 9, 12)));
            }
            @Override public void focusLost(FocusEvent e) {
                f.setBorder(new CompoundBorder(
                    new LineBorder(BORDER_DIM, 1, true),
                    new EmptyBorder(9, 12, 9, 12)));
            }
        });
        return f;
    }

    private JLabel buildFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(GOLD_MUTED);
        return l;
    }

    private JPanel buildForgotRow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        p.setOpaque(false);
        JButton b = new JButton("Forgot password?");
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.PLAIN, 11));
        b.setForeground(GOLD);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "Enter your registered email to receive a reset OTP.",
            "Forgot Password", JOptionPane.INFORMATION_MESSAGE));
        p.add(b);
        return p;
    }

    private JButton buildPrimaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setForeground(new Color(0x0E0602));
        b.setBackground(GOLD);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(328, 44));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(0, 0, 0, 0));
        // Rounded via UIManager trick — wrap in rounded panel instead
        b.putClientProperty("JButton.buttonType", "roundRect");

        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                b.setBackground(GOLD_LIGHT);
            }
            @Override public void mouseExited(MouseEvent e) {
                b.setBackground(GOLD);
            }
        });
        return b;
    }

    private JButton buildGhostButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        b.setForeground(GOLD);
        b.setBackground(BG_CARD);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(328, 42));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new CompoundBorder(
            new LineBorder(BORDER_DIM, 1, true),
            new EmptyBorder(0, 0, 0, 0)
        ));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                b.setBorder(new CompoundBorder(
                    new LineBorder(GOLD, 1, true),
                    new EmptyBorder(0, 0, 0, 0)));
            }
            @Override public void mouseExited(MouseEvent e) {
                b.setBorder(new CompoundBorder(
                    new LineBorder(BORDER_DIM, 1, true),
                    new EmptyBorder(0, 0, 0, 0)));
            }
        });
        return b;
    }

    private JPanel buildDivider(String text) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(328, 20));

        JSeparator left  = new JSeparator(); left.setForeground(BORDER_DIM); left.setBackground(BORDER_DIM);
        JSeparator right = new JSeparator(); right.setForeground(BORDER_DIM); right.setBackground(BORDER_DIM);
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lbl.setForeground(new Color(0x6B4226));
        lbl.setBorder(new EmptyBorder(0, 8, 0, 8));

        p.add(left,  BorderLayout.WEST);
        p.add(lbl,   BorderLayout.CENTER);
        p.add(right, BorderLayout.EAST);
        return p;
    }
}