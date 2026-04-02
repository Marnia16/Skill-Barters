package com.skillbarter.ui;

import com.skillbarter.model.User;
import com.skillbarter.service.EmailOtpService;
import com.skillbarter.service.UserService;

import javax.swing.*;
import javax.swing.Timer;
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
        b.addActionListener(e -> showForgotPasswordStep1());
        p.add(b);
        return p;
    }

    // ── STEP 1: Ask for email ──────────────────────────────────
    private void showForgotPasswordStep1() {
        JDialog dialog = new JDialog((Frame) null, "Reset Password", true);
        dialog.setSize(420, 280);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = darkDialogPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;

        c.gridy = 0; c.insets = new Insets(0, 0, 6, 0);
        JLabel title = new JLabel("Reset Your Password", SwingConstants.CENTER);
        title.setFont(new Font("Georgia", Font.BOLD, 16));
        title.setForeground(GOLD_LIGHT);
        panel.add(title, c);

        c.gridy = 1; c.insets = new Insets(0, 0, 16, 0);
        JLabel sub = new JLabel("Enter your registered email to receive an OTP", SwingConstants.CENTER);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sub.setForeground(GOLD_MUTED);
        panel.add(sub, c);

        c.gridy = 2; c.insets = new Insets(0, 0, 6, 0);
        JLabel lbl = new JLabel("EMAIL ADDRESS");
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(GOLD_MUTED);
        panel.add(lbl, c);

        c.gridy = 3; c.insets = new Insets(0, 0, 14, 0);
        JTextField emailF = buildDialogField("your@email.com");
        panel.add(emailF, c);

        c.gridy = 4; c.insets = new Insets(0, 0, 8, 0);
        JLabel msgLbl = new JLabel(" ", SwingConstants.CENTER);
        msgLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        msgLbl.setForeground(ERROR_RED);
        panel.add(msgLbl, c);

        c.gridy = 5; c.insets = new Insets(0, 0, 0, 0);
        JButton sendBtn = buildDialogPrimaryButton("SEND OTP  →");
        sendBtn.addActionListener(ev -> {
            String email = emailF.getText().trim();
            if (!email.contains("@")) {
                msgLbl.setText("Enter a valid email address.");
                return;
            }
            // Check user exists
            Optional<User> user = userService.findByEmail(email);
            if (user.isEmpty()) {
                msgLbl.setText("No account found with this email.");
                return;
            }
            sendBtn.setEnabled(false);
            msgLbl.setForeground(GOLD_MUTED);
            msgLbl.setText("Sending OTP...");
            EmailOtpService.sendOtp(email,
                () -> {
                    dialog.dispose();
                    showForgotPasswordStep2(email);
                },
                err -> {
                    sendBtn.setEnabled(true);
                    msgLbl.setForeground(ERROR_RED);
                    msgLbl.setText("Failed: " + err);
                }
            );
        });
        panel.add(sendBtn, c);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    // ── STEP 2: Enter OTP ─────────────────────────────────────
    private void showForgotPasswordStep2(String email) {
        JDialog dialog = new JDialog((Frame) null, "Enter OTP", true);
        dialog.setSize(420, 340);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = darkDialogPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;

        c.gridy = 0; c.insets = new Insets(0, 0, 4, 0);
        JLabel title = new JLabel("Check Your Email", SwingConstants.CENTER);
        title.setFont(new Font("Georgia", Font.BOLD, 16));
        title.setForeground(GOLD_LIGHT);
        panel.add(title, c);

        c.gridy = 1; c.insets = new Insets(0, 0, 4, 0);
        JLabel sub = new JLabel("A 6-digit code was sent to:", SwingConstants.CENTER);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sub.setForeground(GOLD_MUTED);
        panel.add(sub, c);

        c.gridy = 2; c.insets = new Insets(0, 0, 16, 0);
        JLabel emailLbl = new JLabel(email, SwingConstants.CENTER);
        emailLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        emailLbl.setForeground(GOLD);
        emailLbl.setOpaque(true);
        emailLbl.setBackground(new Color(0x2C1A0E));
        emailLbl.setBorder(new CompoundBorder(
            new LineBorder(BORDER_DIM, 1, true),
            new EmptyBorder(5, 14, 5, 14)));
        panel.add(emailLbl, c);

        // 6 OTP boxes
        c.gridy = 3; c.insets = new Insets(0, 0, 10, 0);
        JTextField[] boxes = new JTextField[6];
        JPanel boxRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        boxRow.setOpaque(false);
        for (int i = 0; i < 6; i++) {
            boxes[i] = buildOtpBox();
            boxRow.add(boxes[i]);
        }
        // Auto-advance between boxes
        for (int i = 0; i < 6; i++) {
            final int idx = i;
            boxes[i].getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    String t = boxes[idx].getText();
                    if (t.length() > 1) boxes[idx].setText(t.substring(t.length() - 1));
                    if (boxes[idx].getText().length() == 1 && idx < 5)
                        boxes[idx + 1].requestFocus();
                }
                public void removeUpdate(javax.swing.event.DocumentEvent e) {}
                public void changedUpdate(javax.swing.event.DocumentEvent e) {}
            });
            boxes[i].addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE
                            && boxes[idx].getText().isEmpty() && idx > 0)
                        boxes[idx - 1].requestFocus();
                }
            });
        }
        panel.add(boxRow, c);

        // Countdown timer
        c.gridy = 4; c.insets = new Insets(0, 0, 6, 0);
        JLabel countdown = new JLabel("Code expires in  02:00", SwingConstants.CENTER);
        countdown.setFont(new Font("SansSerif", Font.PLAIN, 11));
        countdown.setForeground(GOLD_MUTED);
        panel.add(countdown, c);
        int[] secs = {120};
        Timer timer = new Timer(1000, null);
        timer.addActionListener(ev -> {
            secs[0]--;
            int m = secs[0] / 60, s = secs[0] % 60;
            countdown.setText(String.format("Code expires in  %02d:%02d", m, s));
            if (secs[0] <= 0) { timer.stop(); countdown.setText("Code expired. Close and try again."); }
        });
        timer.start();
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) { timer.stop(); }
        });

        c.gridy = 5; c.insets = new Insets(0, 0, 8, 0);
        JLabel otpMsg = new JLabel(" ", SwingConstants.CENTER);
        otpMsg.setFont(new Font("SansSerif", Font.PLAIN, 11));
        otpMsg.setForeground(ERROR_RED);
        panel.add(otpMsg, c);

        c.gridy = 6; c.insets = new Insets(0, 0, 0, 0);
        JButton verifyBtn = buildDialogPrimaryButton("VERIFY CODE  →");
        verifyBtn.addActionListener(ev -> {
            StringBuilder sb = new StringBuilder();
            for (JTextField box : boxes) sb.append(box.getText().trim());
            String entered = sb.toString();
            if (entered.length() < 6) { otpMsg.setText("Please enter all 6 digits."); return; }

            switch (EmailOtpService.verify(email, entered)) {
                case SUCCESS -> {
                    timer.stop();
                    dialog.dispose();
                    showForgotPasswordStep3(email);
                }
                case WRONG   -> otpMsg.setText("Incorrect code. Try again.");
                case EXPIRED -> { otpMsg.setText("Code expired. Close and try again."); timer.stop(); }
                case NOT_FOUND -> otpMsg.setText("No OTP found. Close and try again.");
            }
        });
        panel.add(verifyBtn, c);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    // ── STEP 3: Set new password ───────────────────────────────
    private void showForgotPasswordStep3(String email) {
        JDialog dialog = new JDialog((Frame) null, "Set New Password", true);
        dialog.setSize(420, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = darkDialogPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;

        c.gridy = 0; c.insets = new Insets(0, 0, 6, 0);
        JLabel title = new JLabel("Set a New Password", SwingConstants.CENTER);
        title.setFont(new Font("Georgia", Font.BOLD, 16));
        title.setForeground(GOLD_LIGHT);
        panel.add(title, c);

        c.gridy = 1; c.insets = new Insets(0, 0, 18, 0);
        JLabel sub = new JLabel("Email verified  ✓  " + email, SwingConstants.CENTER);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sub.setForeground(new Color(0x97C459));
        panel.add(sub, c);

        c.gridy = 2; c.insets = new Insets(0, 0, 5, 0);
        JLabel lbl1 = new JLabel("NEW PASSWORD");
        lbl1.setFont(new Font("SansSerif", Font.BOLD, 10)); lbl1.setForeground(GOLD_MUTED);
        panel.add(lbl1, c);

        c.gridy = 3; c.insets = new Insets(0, 0, 12, 0);
        JPasswordField newPass = buildDialogPasswordField("Min 6 characters");
        panel.add(newPass, c);

        c.gridy = 4; c.insets = new Insets(0, 0, 5, 0);
        JLabel lbl2 = new JLabel("CONFIRM NEW PASSWORD");
        lbl2.setFont(new Font("SansSerif", Font.BOLD, 10)); lbl2.setForeground(GOLD_MUTED);
        panel.add(lbl2, c);

        c.gridy = 5; c.insets = new Insets(0, 0, 10, 0);
        JPasswordField confirmPass = buildDialogPasswordField("Re-enter password");
        panel.add(confirmPass, c);

        c.gridy = 6; c.insets = new Insets(0, 0, 8, 0);
        JLabel msg = new JLabel(" ", SwingConstants.CENTER);
        msg.setFont(new Font("SansSerif", Font.PLAIN, 11));
        msg.setForeground(ERROR_RED);
        panel.add(msg, c);

        c.gridy = 7; c.insets = new Insets(0, 0, 0, 0);
        JButton saveBtn = buildDialogPrimaryButton("SAVE NEW PASSWORD  →");
        saveBtn.addActionListener(ev -> {
            String p1 = new String(newPass.getPassword());
            String p2 = new String(confirmPass.getPassword());
            if (p1.length() < 6) { msg.setText("Password must be at least 6 characters."); return; }
            if (!p1.equals(p2))  { msg.setText("Passwords do not match."); return; }

            // Update password in DB via UserService
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override protected Void doInBackground() {
                    userService.resetPassword(email, p1);
                    return null;
                }
                @Override protected void done() {
                    try {
                        get();
                        dialog.dispose();
                        // Show success then pre-fill email in login
                        setMsg("Password reset successfully! Please login.", true);
                        emailField.setText(email);
                        passField.setText("");
                        passField.requestFocus();
                    } catch (Exception ex) {
                        msg.setForeground(ERROR_RED);
                        msg.setText("Error: " + ex.getMessage());
                    }
                }
            };
            worker.execute();
        });
        panel.add(saveBtn, c);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    // ── Dialog UI helpers ──────────────────────────────────────
    private JPanel darkDialogPanel() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(0x1A0A02));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        p.setBorder(new EmptyBorder(24, 28, 24, 28));
        return p;
    }

    private JTextField buildDialogField(String placeholder) {
        JTextField f = new JTextField();
        f.setBackground(BG_FIELD);
        f.setForeground(GOLD_LIGHT);
        f.setCaretColor(GOLD);
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setOpaque(true);
        f.setToolTipText(placeholder);
        f.setBorder(new CompoundBorder(
            new LineBorder(BORDER_DIM, 1, true),
            new EmptyBorder(9, 12, 9, 12)));
        f.setPreferredSize(new Dimension(0, 42));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBorder(new CompoundBorder(new LineBorder(GOLD, 1, true), new EmptyBorder(9, 12, 9, 12)));
            }
            public void focusLost(FocusEvent e) {
                f.setBorder(new CompoundBorder(new LineBorder(BORDER_DIM, 1, true), new EmptyBorder(9, 12, 9, 12)));
            }
        });
        return f;
    }

    private JPasswordField buildDialogPasswordField(String placeholder) {
        JPasswordField f = new JPasswordField();
        f.setBackground(BG_FIELD);
        f.setForeground(GOLD_LIGHT);
        f.setCaretColor(GOLD);
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setOpaque(true);
        f.setToolTipText(placeholder);
        f.setBorder(new CompoundBorder(
            new LineBorder(BORDER_DIM, 1, true),
            new EmptyBorder(9, 12, 9, 12)));
        f.setPreferredSize(new Dimension(0, 42));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBorder(new CompoundBorder(new LineBorder(GOLD, 1, true), new EmptyBorder(9, 12, 9, 12)));
            }
            public void focusLost(FocusEvent e) {
                f.setBorder(new CompoundBorder(new LineBorder(BORDER_DIM, 1, true), new EmptyBorder(9, 12, 9, 12)));
            }
        });
        return f;
    }

    private JTextField buildOtpBox() {
        JTextField f = new JTextField(1);
        f.setBackground(BG_FIELD);
        f.setForeground(GOLD);
        f.setCaretColor(GOLD);
        f.setFont(new Font("Georgia", Font.BOLD, 22));
        f.setHorizontalAlignment(JTextField.CENTER);
        f.setOpaque(true);
        f.setPreferredSize(new Dimension(46, 54));
        f.setBorder(new CompoundBorder(
            new LineBorder(BORDER_DIM, 1, true),
            new EmptyBorder(4, 4, 4, 4)));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBorder(new CompoundBorder(new LineBorder(GOLD, 1, true), new EmptyBorder(4, 4, 4, 4)));
            }
            public void focusLost(FocusEvent e) {
                f.setBorder(new CompoundBorder(new LineBorder(BORDER_DIM, 1, true), new EmptyBorder(4, 4, 4, 4)));
            }
        });
        return f;
    }

    private JButton buildDialogPrimaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setForeground(new Color(0x0E0602));
        b.setBackground(GOLD);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(0, 44));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(GOLD_LIGHT); }
            public void mouseExited(MouseEvent e)  { b.setBackground(GOLD); }
        });
        return b;
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