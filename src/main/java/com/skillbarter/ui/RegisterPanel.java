package com.skillbarter.ui;

import com.skillbarter.service.EmailOtpService;
import com.skillbarter.service.UserService;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * FIXED RegisterPanel — dark espresso theme.
 * Same fix as LoginPanel: GridBagLayout + explicit LineBorder on all fields.
 *
 * 3 internal screens via CardLayout:
 *   FORM    → fill name/email/password → SEND OTP
 *   OTP     → 6-digit boxes + countdown → VERIFY
 *   SUCCESS → account created → go to login
 */
public class RegisterPanel extends JPanel {

    static final Color BG_DEEP    = new Color(0x0E0602);
    static final Color BG_CARD    = new Color(0x1A0A02);
    static final Color BG_FIELD   = new Color(0x140803);
    static final Color GOLD       = new Color(0xC49A6C);
    static final Color GOLD_LIGHT = new Color(0xEDD9C0);
    static final Color GOLD_MUTED = new Color(0xA0714F);
    static final Color BORDER_DIM = new Color(0x6B4226);
    static final Color ERROR_RED  = new Color(0xF09595);
    static final Color SUCCESS_GR = new Color(0x97C459);

    private static final String CARD_FORM    = "FORM";
    private static final String CARD_OTP     = "OTP";
    private static final String CARD_SUCCESS = "SUCCESS";

    private final CardLayout cards    = new CardLayout();
    private final JPanel     cardWrap = new JPanel(cards);

    private final UserService userService;
    private final MainApp     mainApp;

    // Form fields
    private JTextField     nameField;
    private JTextField     emailField;
    private JPasswordField passField;
    private JPasswordField confirmField;
    private JLabel         formMsg;
    private JButton        createBtn;

    // OTP fields
    private JTextField[] otpBoxes = new JTextField[6];
    private JLabel       otpEmailLabel;
    private JLabel       otpTimerLabel;
    private JLabel       otpMsg;
    private JButton      verifyBtn;
    private JButton      resendBtn;
    private Timer        countdownTimer;
    private int          secondsLeft = 120;

    // Pending data
    private String pendingName;
    private String pendingEmail;
    private String pendingPassword;

    public RegisterPanel(UserService userService, MainApp mainApp) {
        this.userService = userService;
        this.mainApp     = mainApp;
        setLayout(new GridBagLayout());
        setBackground(BG_DEEP);
        cardWrap.setOpaque(false);
        cardWrap.add(buildFormCard(),    CARD_FORM);
        cardWrap.add(buildOtpCard(),     CARD_OTP);
        cardWrap.add(buildSuccessCard(), CARD_SUCCESS);
        add(cardWrap);
        cards.show(cardWrap, CARD_FORM);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(BG_DEEP); g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setPaint(new RadialGradientPaint(new Point2D.Float(0, 0), 400,
            new float[]{0f, 1f},
            new Color[]{new Color(139, 94, 60, 45), new Color(0, 0, 0, 0)}));
        g2.fillOval(-200, -200, 600, 600);
        g2.setPaint(new RadialGradientPaint(new Point2D.Float(getWidth(), getHeight()), 350,
            new float[]{0f, 1f},
            new Color[]{new Color(139, 94, 60, 30), new Color(0, 0, 0, 0)}));
        g2.fillOval(getWidth() - 350, getHeight() - 350, 700, 700);
        g2.dispose();
    }

    // ══════════════════════════════════════════════════════════
    //  CARD 1 — Registration form
    // ══════════════════════════════════════════════════════════
    private JPanel buildFormCard() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);

        JPanel card = makeCard(400, 580);
        GridBagConstraints c = gbc();

        // Logo
        c.insets = new Insets(0, 0, 18, 0);
        card.add(buildLogoRow(), c);

        // Tab bar
        c.gridy++; c.insets = new Insets(0, 0, 22, 0);
        card.add(buildTabBar(false), c);

        // Title
        c.gridy++; c.insets = new Insets(0, 0, 4, 0);
        card.add(centeredLabel("Create Your Account", 22, Font.BOLD, GOLD_LIGHT), c);

        // Subtitle
        c.gridy++; c.insets = new Insets(0, 0, 22, 0);
        card.add(centeredLabel("Join the community — it's free", 12, Font.PLAIN, GOLD_MUTED), c);

        // Name
        c.gridy++; c.insets = new Insets(0, 0, 5, 0);
        card.add(leftLabel("FULL NAME"), c);
        c.gridy++; c.insets = new Insets(0, 0, 14, 0);
        nameField = buildTextField("Your full name");
        card.add(nameField, c);

        // Email
        c.gridy++; c.insets = new Insets(0, 0, 5, 0);
        card.add(leftLabel("EMAIL ADDRESS"), c);
        c.gridy++; c.insets = new Insets(0, 0, 4, 0);
        emailField = buildTextField("you@example.com");
        card.add(emailField, c);

        // Email hint
        c.gridy++; c.insets = new Insets(0, 2, 14, 0);
        JLabel hint = new JLabel("  A 6-digit code will be sent here to verify you");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 10));
        hint.setForeground(new Color(0x7D6040));
        card.add(hint, c);

        // Password
        c.gridy++; c.insets = new Insets(0, 0, 5, 0);
        card.add(leftLabel("PASSWORD"), c);
        c.gridy++; c.insets = new Insets(0, 0, 14, 0);
        passField = buildPasswordField("Min 6 characters");
        card.add(passField, c);

        // Confirm password
        c.gridy++; c.insets = new Insets(0, 0, 5, 0);
        card.add(leftLabel("CONFIRM PASSWORD"), c);
        c.gridy++; c.insets = new Insets(0, 0, 10, 0);
        confirmField = buildPasswordField("Re-enter password");
        card.add(confirmField, c);

        // Message
        c.gridy++; c.insets = new Insets(0, 0, 8, 0);
        formMsg = msgLabel();
        card.add(formMsg, c);

        // Create button
        c.gridy++; c.insets = new Insets(0, 0, 16, 0);
        createBtn = buildPrimaryButton("SEND OTP TO EMAIL  →");
        createBtn.addActionListener(e -> handleCreateAccount());
        card.add(createBtn, c);

        // Divider
        c.gridy++; c.insets = new Insets(0, 0, 12, 0);
        card.add(buildDivider("ALREADY A MEMBER?"), c);

        // Back to login
        c.gridy++; c.insets = new Insets(0, 0, 0, 0);
        JButton loginBtn = buildGhostButton("Back to login");
        loginBtn.addActionListener(e -> mainApp.showLoginPanel());
        card.add(loginBtn, c);

        outer.add(card);
        return outer;
    }

    // ══════════════════════════════════════════════════════════
    //  CARD 2 — OTP verification
    // ══════════════════════════════════════════════════════════
    private JPanel buildOtpCard() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);

        JPanel card = makeCard(400, 480);
        GridBagConstraints c = gbc();

        // Logo
        c.insets = new Insets(0, 0, 14, 0);
        card.add(buildLogoRow(), c);

        // Step dots
        c.gridy++; c.insets = new Insets(0, 0, 18, 0);
        card.add(buildStepDots(), c);

        // Title
        c.gridy++; c.insets = new Insets(0, 0, 4, 0);
        card.add(centeredLabel("Verify Your Email", 20, Font.BOLD, GOLD_LIGHT), c);

        // Sub
        c.gridy++; c.insets = new Insets(0, 0, 8, 0);
        card.add(centeredLabel("We sent a 6-digit code to:", 12, Font.PLAIN, GOLD_MUTED), c);

        // Email pill
        c.gridy++; c.insets = new Insets(0, 0, 22, 0);
        otpEmailLabel = new JLabel("", SwingConstants.CENTER);
        otpEmailLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        otpEmailLabel.setForeground(GOLD);
        otpEmailLabel.setOpaque(true);
        otpEmailLabel.setBackground(new Color(0x2C1A0E));
        otpEmailLabel.setBorder(new CompoundBorder(
            new LineBorder(BORDER_DIM, 1, true),
            new EmptyBorder(6, 20, 6, 20)));
        card.add(otpEmailLabel, c);

        // OTP boxes
        c.gridy++; c.insets = new Insets(0, 0, 16, 0);
        card.add(buildOtpBoxRow(), c);

        // Timer
        c.gridy++; c.insets = new Insets(0, 0, 6, 0);
        otpTimerLabel = new JLabel("Code expires in  02:00", SwingConstants.CENTER);
        otpTimerLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        otpTimerLabel.setForeground(GOLD_MUTED);
        card.add(otpTimerLabel, c);

        // Resend
        c.gridy++; c.insets = new Insets(0, 0, 10, 0);
        resendBtn = new JButton("Resend code");
        resendBtn.setOpaque(false); resendBtn.setContentAreaFilled(false);
        resendBtn.setBorderPainted(false); resendBtn.setFocusPainted(false);
        resendBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        resendBtn.setForeground(GOLD);
        resendBtn.setEnabled(false);
        resendBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        resendBtn.addActionListener(e -> resendOtp());
        card.add(resendBtn, c);

        // OTP msg
        c.gridy++; c.insets = new Insets(0, 0, 10, 0);
        otpMsg = msgLabel();
        card.add(otpMsg, c);

        // Verify button
        c.gridy++; c.insets = new Insets(0, 0, 12, 0);
        verifyBtn = buildPrimaryButton("VERIFY & CREATE ACCOUNT  →");
        verifyBtn.addActionListener(e -> handleOtpVerify());
        card.add(verifyBtn, c);

        // Back link
        c.gridy++; c.insets = new Insets(0, 0, 0, 0);
        JButton back = new JButton("← Change email / go back");
        back.setOpaque(false); back.setContentAreaFilled(false);
        back.setBorderPainted(false); back.setFocusPainted(false);
        back.setFont(new Font("SansSerif", Font.PLAIN, 11));
        back.setForeground(GOLD_MUTED);
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.addActionListener(e -> { stopTimer(); cards.show(cardWrap, CARD_FORM); });
        card.add(back, c);

        outer.add(card);
        return outer;
    }

    // ══════════════════════════════════════════════════════════
    //  CARD 3 — Success
    // ══════════════════════════════════════════════════════════
    private JPanel buildSuccessCard() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);

        JPanel card = makeCard(400, 340);
        GridBagConstraints c = gbc();

        // Check icon
        c.insets = new Insets(10, 0, 20, 0);
        JLabel check = new JLabel("✓", SwingConstants.CENTER);
        check.setFont(new Font("SansSerif", Font.BOLD, 36));
        check.setForeground(SUCCESS_GR);
        check.setOpaque(true);
        check.setBackground(new Color(0x1E3010));
        check.setPreferredSize(new Dimension(72, 72));
        check.setBorder(new CompoundBorder(
            new LineBorder(new Color(59, 109, 17, 80), 1, true),
            new EmptyBorder(0, 0, 0, 0)));
        card.add(check, c);

        c.gridy++; c.insets = new Insets(0, 0, 8, 0);
        card.add(centeredLabel("Account Created!", 22, Font.BOLD, GOLD_LIGHT), c);

        c.gridy++; c.insets = new Insets(0, 0, 4, 0);
        card.add(centeredLabel("Your email has been verified.", 12, Font.PLAIN, GOLD_MUTED), c);

        c.gridy++; c.insets = new Insets(0, 0, 28, 0);
        card.add(centeredLabel("Welcome to Skill Barters.", 12, Font.PLAIN, GOLD_MUTED), c);

        c.gridy++; c.insets = new Insets(0, 0, 0, 0);
        JButton go = buildPrimaryButton("GO TO LOGIN  →");
        go.addActionListener(e -> { cards.show(cardWrap, CARD_FORM); mainApp.showLoginPanel(); });
        card.add(go, c);

        outer.add(card);
        return outer;
    }

    // ══════════════════════════════════════════════════════════
    //  LOGIC
    // ══════════════════════════════════════════════════════════
    private void handleCreateAccount() {
        String name     = nameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = new String(passField.getPassword());
        String confirm  = new String(confirmField.getPassword());

        if (name.isEmpty())            { setMsg(formMsg, "Please enter your full name.", false); return; }
        if (!email.contains("@"))      { setMsg(formMsg, "Enter a valid email address.", false); return; }
        if (password.length() < 6)     { setMsg(formMsg, "Password must be at least 6 characters.", false); return; }
        if (!password.equals(confirm)) { setMsg(formMsg, "Passwords do not match.", false); return; }

        pendingName     = name;
        pendingEmail    = email;
        pendingPassword = password;

        createBtn.setEnabled(false);
        setMsg(formMsg, "Sending OTP to " + email + "...", true);

        EmailOtpService.sendOtp(email,
            () -> {
                createBtn.setEnabled(true);
                setMsg(formMsg, "", true);
                otpEmailLabel.setText(email);
                clearOtpBoxes();
                startCountdown();
                cards.show(cardWrap, CARD_OTP);
            },
            err -> {
                createBtn.setEnabled(true);
                setMsg(formMsg, err, false);
            }
        );
    }

    private void handleOtpVerify() {
        StringBuilder sb = new StringBuilder();
        for (JTextField b : otpBoxes) sb.append(b.getText().trim());
        String entered = sb.toString();

        if (entered.length() < 6) { setMsg(otpMsg, "Please enter all 6 digits.", false); return; }

        switch (EmailOtpService.verify(pendingEmail, entered)) {
            case SUCCESS -> {
                stopTimer();
                new SwingWorker<Void, Void>() {
                    @Override protected Void doInBackground() {
                        userService.register(pendingName, pendingEmail, pendingPassword);
                        return null;
                    }
                    @Override protected void done() {
                        try { get(); cards.show(cardWrap, CARD_SUCCESS); }
                        catch (Exception ex) { setMsg(otpMsg, "Error: " + ex.getMessage(), false); }
                    }
                }.execute();
            }
            case WRONG    -> setMsg(otpMsg, "Incorrect code. Try again.", false);
            case EXPIRED  -> { setMsg(otpMsg, "Code expired. Request a new one.", false); resendBtn.setEnabled(true); }
            case NOT_FOUND-> setMsg(otpMsg, "No OTP found. Click Resend.", false);
        }
    }

    private void resendOtp() {
        if (pendingEmail == null) return;
        setMsg(otpMsg, "Sending new code...", true);
        EmailOtpService.sendOtp(pendingEmail,
            () -> { clearOtpBoxes(); startCountdown(); setMsg(otpMsg, "New code sent!", true); },
            err -> setMsg(otpMsg, err, false));
    }

    private void startCountdown() {
        stopTimer();
        secondsLeft = 120;
        resendBtn.setEnabled(false);
        countdownTimer = new Timer(1000, e -> {
            secondsLeft--;
            int m = secondsLeft / 60, s = secondsLeft % 60;
            otpTimerLabel.setText(String.format("Code expires in  %02d:%02d", m, s));
            if (secondsLeft <= 0) {
                stopTimer();
                otpTimerLabel.setText("Code expired. Click Resend.");
                resendBtn.setEnabled(true);
            }
        });
        countdownTimer.start();
    }

    private void stopTimer() {
        if (countdownTimer != null && countdownTimer.isRunning()) countdownTimer.stop();
    }

    private void clearOtpBoxes() {
        for (JTextField b : otpBoxes) b.setText("");
        if (otpBoxes[0] != null) otpBoxes[0].requestFocus();
        if (otpMsg != null) otpMsg.setText(" ");
    }

    // ══════════════════════════════════════════════════════════
    //  UI HELPERS
    // ══════════════════════════════════════════════════════════

    /** Shared card factory — custom painted dark rounded panel */
    private JPanel makeCard(int w, int h) {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(196, 154, 108, 60));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 18, 18);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(w, h));
        card.setBorder(new EmptyBorder(32, 36, 32, 36));
        return card;
    }

    /** Standard GBC for full-width single-column layout */
    private GridBagConstraints gbc() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(0, 0, 0, 0);
        return c;
    }

    private JPanel buildLogoRow() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.setOpaque(false);
        JPanel emblem = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x2C1A0E)); g2.fillOval(0,0,38,38);
                g2.setColor(new Color(196,154,108,100));
                g2.setStroke(new BasicStroke(1f)); g2.drawOval(0,0,37,37);
                g2.setColor(GOLD);
                g2.setFont(new Font("Georgia",Font.BOLD,14));
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString("SB",19-fm.stringWidth("SB")/2,19+fm.getAscent()/2-2);
                g2.dispose();
            }
        };
        emblem.setOpaque(false);
        emblem.setPreferredSize(new Dimension(38,38));
        emblem.setMinimumSize(new Dimension(38,38));
        emblem.setMaximumSize(new Dimension(38,38));
        emblem.setAlignmentY(Component.CENTER_ALIGNMENT);
        JLabel brand = new JLabel("Skill Barters");
        brand.setFont(new Font("Georgia",Font.BOLD,20));
        brand.setForeground(GOLD_LIGHT);
        brand.setAlignmentY(Component.CENTER_ALIGNMENT);
        p.add(Box.createHorizontalGlue());
        p.add(emblem);
        p.add(Box.createHorizontalStrut(10));
        p.add(brand);
        p.add(Box.createHorizontalGlue());
        return p;
    }

    private JPanel buildTabBar(boolean loginActive) {
        JPanel p = new JPanel(new GridLayout(1, 2, 0, 0));
        p.setOpaque(true);
        p.setBackground(new Color(0x0A0401));
        p.setBorder(new LineBorder(BORDER_DIM, 1, true));
        p.setPreferredSize(new Dimension(328, 38));

        JButton loginTab = new JButton("Login");
        styleTab(loginTab, loginActive);
        loginTab.addActionListener(e -> mainApp.showLoginPanel());

        JButton regTab = new JButton("Create Account");
        styleTab(regTab, !loginActive);

        p.add(loginTab); p.add(regTab);
        return p;
    }

    private void styleTab(JButton b, boolean active) {
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setFocusPainted(false); b.setBorderPainted(false); b.setOpaque(true);
        b.setBackground(active ? GOLD : new Color(0x0A0401));
        b.setForeground(active ? new Color(0x0E0602) : GOLD_MUTED);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private JTextField buildTextField(String placeholder) {
        JTextField f = new JTextField(20);
        f.setBackground(BG_FIELD);
        f.setForeground(GOLD_LIGHT);
        f.setCaretColor(GOLD);
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setOpaque(true);
        f.setBorder(new CompoundBorder(
            new LineBorder(BORDER_DIM, 1, true),
            new EmptyBorder(9, 12, 9, 12)));
        f.setPreferredSize(new Dimension(328, 42));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBorder(new CompoundBorder(new LineBorder(GOLD,1,true), new EmptyBorder(9,12,9,12)));
            }
            public void focusLost(FocusEvent e) {
                f.setBorder(new CompoundBorder(new LineBorder(BORDER_DIM,1,true), new EmptyBorder(9,12,9,12)));
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
            new EmptyBorder(9, 12, 9, 12)));
        f.setPreferredSize(new Dimension(328, 42));
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                f.setBorder(new CompoundBorder(new LineBorder(GOLD,1,true), new EmptyBorder(9,12,9,12)));
            }
            public void focusLost(FocusEvent e) {
                f.setBorder(new CompoundBorder(new LineBorder(BORDER_DIM,1,true), new EmptyBorder(9,12,9,12)));
            }
        });
        return f;
    }

    private JPanel buildOtpBoxRow() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        p.setOpaque(false);
        for (int i = 0; i < 6; i++) {
            otpBoxes[i] = buildOtpBox(i);
            p.add(otpBoxes[i]);
        }
        return p;
    }

    private JTextField buildOtpBox(int index) {
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
                f.setBorder(new CompoundBorder(new LineBorder(GOLD,1,true), new EmptyBorder(4,4,4,4)));
            }
            public void focusLost(FocusEvent e) {
                f.setBorder(new CompoundBorder(new LineBorder(BORDER_DIM,1,true), new EmptyBorder(4,4,4,4)));
            }
        });

        f.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                String t = f.getText();
                if (t.length() > 1) f.setText(t.substring(t.length() - 1));
                if (f.getText().length() == 1 && index < 5)
                    otpBoxes[index + 1].requestFocus();
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {}
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        f.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && f.getText().isEmpty() && index > 0)
                    otpBoxes[index - 1].requestFocus();
                if (e.getKeyCode() == KeyEvent.VK_ENTER) handleOtpVerify();
            }
        });
        return f;
    }

    private JPanel buildStepDots() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        p.setOpaque(false);
        Color[] colors = {GOLD, GOLD_LIGHT, BORDER_DIM};
        for (int i = 0; i < 3; i++) {
            final Color col = colors[i];
            JPanel dot = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(col); g2.fillOval(0,0,10,10); g2.dispose();
                }
            };
            dot.setOpaque(false); dot.setPreferredSize(new Dimension(10,10));
            p.add(dot);
            if (i < 2) {
                JSeparator line = new JSeparator(JSeparator.HORIZONTAL);
                line.setForeground(i == 0 ? GOLD : BORDER_DIM);
                line.setPreferredSize(new Dimension(40, 1));
                p.add(line);
            }
        }
        return p;
    }

    private JButton buildPrimaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setForeground(new Color(0x0E0602));
        b.setBackground(GOLD);
        b.setOpaque(true); b.setBorderPainted(false); b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(328, 44));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(GOLD_LIGHT); }
            public void mouseExited(MouseEvent e)  { b.setBackground(GOLD); }
        });
        return b;
    }

    private JButton buildGhostButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        b.setForeground(GOLD); b.setBackground(BG_CARD);
        b.setOpaque(true); b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(328, 42));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new CompoundBorder(
            new LineBorder(BORDER_DIM, 1, true),
            new EmptyBorder(0, 0, 0, 0)));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setBorder(new CompoundBorder(new LineBorder(GOLD,1,true), new EmptyBorder(0,0,0,0)));
            }
            public void mouseExited(MouseEvent e) {
                b.setBorder(new CompoundBorder(new LineBorder(BORDER_DIM,1,true), new EmptyBorder(0,0,0,0)));
            }
        });
        return b;
    }

    private JPanel buildDivider(String text) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(328, 20));
        JSeparator l = new JSeparator(); l.setForeground(BORDER_DIM);
        JSeparator r = new JSeparator(); r.setForeground(BORDER_DIM);
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lbl.setForeground(new Color(0x6B4226));
        lbl.setBorder(new EmptyBorder(0, 8, 0, 8));
        p.add(l, BorderLayout.WEST);
        p.add(lbl, BorderLayout.CENTER);
        p.add(r, BorderLayout.EAST);
        return p;
    }

    private JLabel centeredLabel(String text, int size, int style, Color color) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font(style == Font.BOLD ? "Georgia" : "SansSerif", style, size));
        l.setForeground(color);
        return l;
    }

    private JLabel leftLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(GOLD_MUTED);
        return l;
    }

    private JLabel msgLabel() {
        JLabel l = new JLabel(" ", SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setForeground(ERROR_RED);
        return l;
    }

    private void setMsg(JLabel lbl, String text, boolean info) {
        lbl.setText(text.isEmpty() ? " " : text);
        lbl.setForeground(info ? GOLD_MUTED : ERROR_RED);
    }
}