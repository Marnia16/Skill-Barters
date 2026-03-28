package com.skillbarter.ui;

import com.skillbarter.model.BarterRequest;
import com.skillbarter.model.Notification;
import com.skillbarter.model.Skill;
import com.skillbarter.model.User;
import com.skillbarter.service.BarterService;
import com.skillbarter.service.NotificationService;
import com.skillbarter.service.SkillService;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * CONCEPT 7, 8, 9, 10: Full dark espresso Dashboard.
 *
 * Tabs:
 *   MY SKILLS     — view/add/delete own skills
 *   SEARCH        — Harry Potter style skill search with category pills
 *   REQUESTS      — incoming/outgoing barter requests + Jitsi links
 *   PROFILE       — name, gender, teaching languages
 *   COMPLAINTS    — query/complaint submission box
 */
public class DashboardPanel extends JPanel {

    // ── Colours ────────────────────────────────────────────────
    static final Color BG_DEEP    = new Color(0x0E0602);
    static final Color BG_CARD    = new Color(0x1A0A02);
    static final Color BG_FIELD   = new Color(0x140803);
    static final Color BG_ROW_ALT = new Color(0x1E0D04);
    static final Color GOLD       = new Color(0xC49A6C);
    static final Color GOLD_LIGHT = new Color(0xEDD9C0);
    static final Color GOLD_MUTED = new Color(0xA0714F);
    static final Color BORDER_DIM = new Color(0x6B4226);
    static final Color BORDER_GLOW= new Color(0xC49A6C);
    static final Color GREEN_YES  = new Color(0x97C459);
    static final Color RED_NO     = new Color(0xF09595);
    static final Color TEAL       = new Color(0x5DCAA5);
    static final Color PURPLE     = new Color(0xAFA9EC);

    // ── Categories ─────────────────────────────────────────────
    static final String[] DEFAULT_CATEGORIES = {
        "Coding", "Music", "Art", "Editing", "Martial Arts",
        "Hacking", "Photography", "Languages", "Cooking", "Dance",
        "Design", "Science", "Sports", "Writing", "Business"
    };

    // ── Languages ──────────────────────────────────────────────
    static final String[] LANGUAGES = {
        "Tamil", "English", "Hindi", "Malayalam", "Telugu",
        "Kannada", "Bengali", "Marathi", "Gujarati", "Punjabi",
        "Urdu", "Sanskrit", "French", "German", "Spanish",
        "Japanese", "Mandarin", "Arabic", "Portuguese", "Russian"
    };

    // ── Genders ────────────────────────────────────────────────
    static final String[] GENDERS = {
        "Man", "Woman", "Non-binary", "Genderfluid", "Genderqueer",
        "Agender", "Bigender", "Two-Spirit", "Transgender Man",
        "Transgender Woman", "Intersex", "Pangender",
        "Androgynous", "Neutrois", "Prefer not to say"
    };

    // ── Services ───────────────────────────────────────────────
    private final SkillService        skillService;
    private final BarterService       barterService;
    private final NotificationService notifService;
    private final MainApp             mainApp;
    private User currentUser;

    // ── UI refs ────────────────────────────────────────────────
    private JLabel          welcomeLabel;
    private JLabel          notifBadge;
    private JPanel          contentArea;
    private CardLayout      contentCards;
    private DefaultTableModel skillTableModel;

    // ── Particle system ────────────────────────────────────────
    private static final int PN = 50;
    private final float[] px = new float[PN], py = new float[PN];
    private final float[] pvx= new float[PN], pvy= new float[PN];
    private final float[] pa = new float[PN], pdecay=new float[PN], pr=new float[PN];
    private Timer particleTimer;

    // ── Search state ───────────────────────────────────────────
    private String selectedCategory = null;
    private final List<String> customCategories = new ArrayList<>();

    // ── Connect tracking (userId → skillId they want) ──────────
    private final Map<Integer, Integer> connectRequests = new HashMap<>();

    public DashboardPanel(SkillService skillService, BarterService barterService,
                          NotificationService notifService, MainApp mainApp) {
        this.skillService  = skillService;
        this.barterService = barterService;
        this.notifService  = notifService;
        this.mainApp       = mainApp;
        setLayout(new BorderLayout());
        setBackground(BG_DEEP);
        initParticles();
        buildUI();
    }

    // ── Called after login ──────────────────────────────────────
    public void setUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getName() + "!");
        loadSkills();
        notifService.startPolling(user.getId(), this::onNotifications);
        startParticles();
    }

    // ══════════════════════════════════════════════════════════
    //  PARTICLE SYSTEM
    // ══════════════════════════════════════════════════════════
    private void initParticles() {
        Random rng = new Random();
        for (int i = 0; i < PN; i++) resetP(i, rng, true);
    }

    private void resetP(int i, Random rng, boolean anywhere) {
        px[i]     = rng.nextFloat() * 1200;
        py[i]     = anywhere ? rng.nextFloat() * 700 : 710f;
        pvx[i]    = (rng.nextFloat() - .5f) * .4f;
        pvy[i]    = -rng.nextFloat() * .5f - .1f;
        pa[i]     = rng.nextFloat() * .5f + .1f;
        pdecay[i] = rng.nextFloat() * .002f + .001f;
        pr[i]     = rng.nextFloat() * 1.8f + .4f;
    }

    private void startParticles() {
        Random rng = new Random();
        particleTimer = new Timer(30, e -> {
            for (int i = 0; i < PN; i++) {
                px[i] += pvx[i]; py[i] += pvy[i]; pa[i] -= pdecay[i];
                if (pa[i] <= 0 || py[i] < -5) resetP(i, rng, false);
            }
            repaint();
        });
        particleTimer.start();
    }

    // ── Background paint ───────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int W = getWidth(), H = getHeight();
        g2.setColor(BG_DEEP); g2.fillRect(0, 0, W, H);

        // Ambient glow blobs
        drawGlow(g2, -80, -80, 400, new Color(139, 94, 60, 35));
        drawGlow(g2, W - 200, H - 200, 350, new Color(196, 154, 108, 20));
        drawGlow(g2, W / 2 - 150, H / 2 - 100, 300, new Color(83, 74, 183, 12));

        // Particles
        for (int i = 0; i < PN; i++) {
            float al = pa[i]; if (al <= 0) continue;
            g2.setColor(new Color(GOLD.getRed()/255f, GOLD.getGreen()/255f, GOLD.getBlue()/255f, al));
            g2.fill(new Ellipse2D.Float(px[i]-pr[i], py[i]-pr[i], pr[i]*2, pr[i]*2));
        }
        g2.dispose();
    }

    private void drawGlow(Graphics2D g2, int x, int y, int sz, Color c) {
        g2.setPaint(new RadialGradientPaint(
            new Point2D.Float(x + sz/2f, y + sz/2f), sz/2f,
            new float[]{0f, 1f}, new Color[]{c, new Color(0, 0, 0, 0)}));
        g2.fillOval(x, y, sz, sz);
    }

    // ══════════════════════════════════════════════════════════
    //  BUILD MAIN UI
    // ══════════════════════════════════════════════════════════
    private void buildUI() {
        add(buildTopBar(),   BorderLayout.NORTH);
        add(buildSideNav(),  BorderLayout.WEST);
        contentArea  = new JPanel();
        contentCards = new CardLayout();
        contentArea.setLayout(contentCards);
        contentArea.setOpaque(false);
        contentArea.add(buildMySkillsTab(),   "SKILLS");
        contentArea.add(buildSearchTab(),     "SEARCH");
        contentArea.add(buildRequestsTab(),   "REQUESTS");
        contentArea.add(buildProfileTab(),    "PROFILE");
        contentArea.add(buildComplaintsTab(), "COMPLAINTS");
        add(contentArea, BorderLayout.CENTER);
        contentCards.show(contentArea, "SKILLS");
    }

    // ── Top bar ────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0x1A0A02));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(196, 154, 108, 40));
                g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 52));
        bar.setBorder(new EmptyBorder(0, 18, 0, 18));

        // Left — logo
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        JPanel sb = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x2C1A0E)); g2.fillOval(0,0,34,34);
                g2.setColor(new Color(196,154,108,100));
                g2.setStroke(new BasicStroke(.8f)); g2.drawOval(0,0,33,33);
                g2.setColor(GOLD);
                g2.setFont(new Font("Georgia",Font.BOLD,12));
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString("SB",17-fm.stringWidth("SB")/2,17+fm.getAscent()/2-2);
                g2.dispose();
            }
        };
        sb.setOpaque(false); sb.setPreferredSize(new Dimension(34,34));
        JLabel brand = new JLabel("Skill Barters");
        brand.setFont(new Font("Georgia",Font.BOLD,16));
        brand.setForeground(GOLD_LIGHT);
        left.add(sb); left.add(brand);

        // Center — welcome
        welcomeLabel = new JLabel("", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("SansSerif",Font.PLAIN,12));
        welcomeLabel.setForeground(GOLD_MUTED);

        // Right — notif + logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        notifBadge = new JLabel("🔔  0");
        notifBadge.setFont(new Font("SansSerif",Font.BOLD,11));
        notifBadge.setForeground(GOLD_MUTED);
        notifBadge.setOpaque(true);
        notifBadge.setBackground(new Color(0x2C1A0E));
        notifBadge.setBorder(new CompoundBorder(
            new LineBorder(BORDER_DIM,1,true), new EmptyBorder(4,10,4,10)));

        JButton logout = new JButton("Logout");
        logout.setBackground(GOLD); logout.setForeground(BG_DEEP);
        logout.setFont(new Font("SansSerif",Font.BOLD,11));
        logout.setBorderPainted(false); logout.setFocusPainted(false);
        logout.setOpaque(true);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> {
            notifService.stopPolling();
            if (particleTimer != null) particleTimer.stop();
            mainApp.showLoginPanel();
        });
        right.add(notifBadge); right.add(logout);

        bar.add(left,         BorderLayout.WEST);
        bar.add(welcomeLabel, BorderLayout.CENTER);
        bar.add(right,        BorderLayout.EAST);
        return bar;
    }

    // ── Side navigation ────────────────────────────────────────
    private JPanel buildSideNav() {
        JPanel nav = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setColor(new Color(0x130701));
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(196,154,108,25));
                g2.drawLine(getWidth()-1,0,getWidth()-1,getHeight());
                g2.dispose();
            }
        };
        nav.setLayout(new BoxLayout(nav,BoxLayout.Y_AXIS));
        nav.setOpaque(false);
        nav.setPreferredSize(new Dimension(150,0));
        nav.setBorder(new EmptyBorder(16,0,16,0));

        String[][] items = {
            {"◈","MY SKILLS","SKILLS"},
            {"◎","SEARCH","SEARCH"},
            {"⇄","REQUESTS","REQUESTS"},
            {"◉","PROFILE","PROFILE"},
            {"✉","COMPLAINTS","COMPLAINTS"}
        };

        for (String[] item : items) {
            JButton btn = buildNavButton(item[0], item[1], item[2]);
            nav.add(btn);
            nav.add(Box.createVerticalStrut(2));
        }
        return nav;
    }

    private JButton buildNavButton(String icon, String label, String card) {
        JButton b = new JButton() {
            boolean hovered = false;
            {
                addMouseListener(new MouseAdapter(){
                    public void mouseEntered(MouseEvent e){hovered=true;repaint();}
                    public void mouseExited(MouseEvent e){hovered=false;repaint();}
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                if(hovered||getModel().isPressed()){
                    g2.setColor(new Color(196,154,108,18));
                    g2.fillRect(0,0,getWidth(),getHeight());
                    g2.setColor(GOLD);
                    g2.fillRect(0,0,3,getHeight());
                }
                g2.setFont(new Font("SansSerif",Font.PLAIN,14));
                g2.setColor(hovered?GOLD:GOLD_MUTED);
                g2.drawString(icon,14,(getHeight()+g2.getFontMetrics().getAscent())/2-2);
                g2.setFont(new Font("SansSerif",Font.BOLD,9));
                g2.setColor(hovered?GOLD_LIGHT:GOLD_MUTED);
                FontMetrics fm=g2.getFontMetrics();
                g2.drawString(label,34,(getHeight()+fm.getAscent())/2-2);
                g2.dispose();
            }
        };
        b.setOpaque(false); b.setContentAreaFilled(false);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(150,44));
        b.setMaximumSize(new Dimension(150,44));
        b.setMinimumSize(new Dimension(150,44));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> contentCards.show(contentArea, card));
        return b;
    }

    // ══════════════════════════════════════════════════════════
    //  TAB 1 — MY SKILLS
    // ══════════════════════════════════════════════════════════
    private JPanel buildMySkillsTab() {
        JPanel p = new JPanel(new BorderLayout(0,12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(18,18,18,18));

        // Header
        JLabel h = sectionHeader("◈  My Skills");
        p.add(h, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID","Title","Category","Level","Available"};
        skillTableModel = new DefaultTableModel(cols,0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        JTable table = buildDarkTable(skillTableModel);
        JScrollPane scroll = darkScroll(table);
        p.add(scroll, BorderLayout.CENTER);

        // Add skill form
        p.add(buildAddSkillForm(), BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildAddSkillForm() {
        JPanel outer = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setColor(new Color(0x1A0A02));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(new Color(196,154,108,40));
                g2.setStroke(new BasicStroke(.8f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                g2.dispose();
            }
        };
        outer.setOpaque(false);
        outer.setLayout(new FlowLayout(FlowLayout.LEFT,12,10));
        outer.setBorder(new EmptyBorder(4,4,4,4));

        // Title field
        outer.add(fieldLbl("SKILL TITLE"));
        JTextField titleF = darkField(160);
        outer.add(titleF);

        // Category dropdown (predefined + custom)
        outer.add(fieldLbl("CATEGORY"));
        List<String> allCats = new ArrayList<>(Arrays.asList(DEFAULT_CATEGORIES));
        allCats.addAll(customCategories);
        allCats.add("+ New Category...");
        JComboBox<String> catBox = darkCombo(allCats.toArray(new String[0]));
        catBox.addActionListener(e -> {
            if ("+ New Category...".equals(catBox.getSelectedItem())) {
                String newCat = JOptionPane.showInputDialog(this,
                    "Enter new category name:", "New Category",
                    JOptionPane.PLAIN_MESSAGE);
                if (newCat != null && !newCat.trim().isEmpty()) {
                    customCategories.add(newCat.trim());
                    catBox.insertItemAt(newCat.trim(), catBox.getItemCount()-1);
                    catBox.setSelectedItem(newCat.trim());
                } else {
                    catBox.setSelectedIndex(0);
                }
            }
        });
        outer.add(catBox);

        // Level
        outer.add(fieldLbl("LEVEL"));
        JComboBox<String> levelBox = darkCombo(new String[]{
            "BEGINNER","INTERMEDIATE","EXPERT"});
        outer.add(levelBox);

        // Add button
        JButton addBtn = goldButton("+ Add Skill");
        addBtn.addActionListener(e -> {
            String title = titleF.getText().trim();
            String cat   = catBox.getSelectedItem() == null ? "" : catBox.getSelectedItem().toString();
            if (cat.equals("+ New Category...")) { JOptionPane.showMessageDialog(this,"Please select a valid category."); return; }
            if (title.isEmpty()) { JOptionPane.showMessageDialog(this,"Please enter a skill title."); return; }
            Skill.SkillLevel level = Skill.SkillLevel.valueOf(levelBox.getSelectedItem().toString());
            try {
                skillService.addSkill(currentUser.getId(), title, "", cat, level);
                titleF.setText("");
                loadSkills();
                JOptionPane.showMessageDialog(this,"Skill added successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            }
        });
        outer.add(addBtn);
        return outer;
    }

    private void loadSkills() {
        if (currentUser == null || skillTableModel == null) return;
        skillTableModel.setRowCount(0);
        List<Skill> skills = skillService.getSkillsByUser(currentUser.getId());
        for (Skill s : skills) {
            skillTableModel.addRow(new Object[]{
                s.getId(), s.getTitle(), s.getCategory(),
                s.getSkillLevel(), s.isAvailable() ? "Yes" : "No"
            });
        }
    }

    // ══════════════════════════════════════════════════════════
    //  TAB 2 — SEARCH (Harry Potter style)
    // ══════════════════════════════════════════════════════════
    private JPanel buildSearchTab() {
        JPanel p = new JPanel(new BorderLayout(0,12));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(18,18,18,18));

        // Header — HP style title
        JLabel h = new JLabel("✦  Seek the Skill You Wish to Learn  ✦", SwingConstants.CENTER);
        h.setFont(new Font("Georgia",Font.BOLD|Font.ITALIC,16));
        h.setForeground(GOLD_LIGHT);
        p.add(h, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0,10));
        body.setOpaque(false);

        // Search bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.CENTER,10,6));
        searchBar.setOpaque(false);
        JTextField searchField = darkField(320);
        searchField.setFont(new Font("Georgia",Font.ITALIC,13));
        JButton seekBtn = new JButton("SEEK  ✦") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()?GOLD_LIGHT:GOLD);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        seekBtn.setFont(new Font("Georgia",Font.BOLD,12));
        seekBtn.setForeground(BG_DEEP);
        seekBtn.setOpaque(false); seekBtn.setContentAreaFilled(false);
        seekBtn.setBorderPainted(false); seekBtn.setFocusPainted(false);
        seekBtn.setPreferredSize(new Dimension(100,38));
        seekBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchBar.add(searchField); searchBar.add(seekBtn);

        // Category pills panel
        JPanel pillPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,6,4));
        pillPanel.setOpaque(false);
        rebuildCategoryPills(pillPanel, searchField);

        JPanel topSearch = new JPanel(new BorderLayout(0,8));
        topSearch.setOpaque(false);
        topSearch.add(searchBar, BorderLayout.NORTH);
        topSearch.add(pillPanel, BorderLayout.CENTER);
        body.add(topSearch, BorderLayout.NORTH);

        // Results panel
        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel,BoxLayout.Y_AXIS));
        resultsPanel.setOpaque(false);
        JScrollPane resultsScroll = darkScroll(resultsPanel);
        body.add(resultsScroll, BorderLayout.CENTER);

        // Search action
        Runnable doSearch = () -> {
            String kw = searchField.getText().trim();
            resultsPanel.removeAll();
            List<Skill> results = selectedCategory != null
                ? skillService.getSkillsByCategory(selectedCategory)
                : (kw.isEmpty() ? new ArrayList<>() : skillService.searchSkills(kw));
            if (results.isEmpty()) {
                JLabel none = new JLabel("No skills found for your search.", SwingConstants.CENTER);
                none.setFont(new Font("Georgia",Font.ITALIC,13));
                none.setForeground(GOLD_MUTED);
                none.setAlignmentX(CENTER_ALIGNMENT);
                resultsPanel.add(Box.createVerticalStrut(20));
                resultsPanel.add(none);
            } else {
                for (Skill s : results) {
                    if (s.getUser().getId() == currentUser.getId()) continue;
                    resultsPanel.add(buildSkillResultCard(s));
                    resultsPanel.add(Box.createVerticalStrut(8));
                }
            }
            resultsPanel.revalidate(); resultsPanel.repaint();
        };

        seekBtn.addActionListener(e -> doSearch.run());
        searchField.addKeyListener(new KeyAdapter(){
            public void keyPressed(KeyEvent e){if(e.getKeyCode()==KeyEvent.VK_ENTER) doSearch.run();}
        });

        p.add(body, BorderLayout.CENTER);
        return p;
    }

    private void rebuildCategoryPills(JPanel pillPanel, JTextField searchField) {
        pillPanel.removeAll();
        List<String> all = new ArrayList<>(Arrays.asList(DEFAULT_CATEGORIES));
        all.addAll(customCategories);
        all.add("+ New");

        for (String cat : all) {
            JButton pill = new JButton(cat) {
                boolean hov=false;
                {addMouseListener(new MouseAdapter(){
                    public void mouseEntered(MouseEvent e){hov=true;repaint();}
                    public void mouseExited(MouseEvent e){hov=false;repaint();}
                });}
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    boolean sel = cat.equals(selectedCategory);
                    g2.setColor(sel?new Color(196,154,108,50):hov?new Color(196,154,108,20):new Color(196,154,108,8));
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);
                    g2.setColor(sel?GOLD:hov?new Color(196,154,108,180):BORDER_DIM);
                    g2.setStroke(new BasicStroke(sel?1.2f:.8f));
                    g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,20,20);
                    g2.dispose(); super.paintComponent(g);
                }
            };
            boolean isDashed = cat.equals("+ New");
            pill.setOpaque(false); pill.setContentAreaFilled(false);
            pill.setBorderPainted(false); pill.setFocusPainted(false);
            pill.setFont(new Font(isDashed?"SansSerif":"Georgia",Font.PLAIN,10));
            pill.setForeground(isDashed?new Color(0x7D6040):
                cat.equals(selectedCategory)?GOLD:GOLD_MUTED);
            pill.setMargin(new Insets(3,10,3,10));
            pill.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            pill.addActionListener(e -> {
                if (cat.equals("+ New")) {
                    String newCat = JOptionPane.showInputDialog(this,
                        "Enter new category:","New Category",JOptionPane.PLAIN_MESSAGE);
                    if (newCat != null && !newCat.trim().isEmpty()) {
                        customCategories.add(newCat.trim());
                        rebuildCategoryPills(pillPanel, searchField);
                        pillPanel.revalidate(); pillPanel.repaint();
                    }
                } else {
                    selectedCategory = cat.equals(selectedCategory) ? null : cat;
                    pillPanel.repaint();
                    // trigger search
                    searchField.setText(cat.equals(selectedCategory)?cat:"");
                }
            });
            pillPanel.add(pill);
        }
        pillPanel.revalidate(); pillPanel.repaint();
    }

    private JPanel buildSkillResultCard(Skill skill) {
        JPanel card = new JPanel(new BorderLayout(12,0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x1E0D04));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                // Left gold accent bar
                g2.setColor(GOLD);
                g2.fillRoundRect(0,0,3,getHeight(),2,2);
                g2.setColor(new Color(196,154,108,35));
                g2.setStroke(new BasicStroke(.8f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10,14,10,14));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE,80));

        // Info
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel name = new JLabel("User #" + skill.getUser().getId());
        name.setFont(new Font("Georgia",Font.BOLD,13));
        name.setForeground(GOLD_LIGHT);

        JLabel skillTitle = new JLabel(skill.getTitle());
        skillTitle.setFont(new Font("Georgia",Font.ITALIC,12));
        skillTitle.setForeground(GOLD);

        JPanel tags = new JPanel(new FlowLayout(FlowLayout.LEFT,4,2));
        tags.setOpaque(false);
        tags.add(buildTag(skill.getSkillLevel().name(), GOLD, new Color(196,154,108,40)));
        tags.add(buildTag(skill.getCategory(), PURPLE, new Color(83,74,183,40)));

        info.add(name);
        info.add(Box.createVerticalStrut(2));
        info.add(skillTitle);
        info.add(tags);

        // Connect button
        JButton connectBtn = new JButton("CONNECT  ✦") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                boolean connected = connectRequests.containsKey(currentUser.getId())
                    && connectRequests.get(currentUser.getId())==skill.getId();
                if(connected){
                    g2.setColor(new Color(59,109,17,60));
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                    g2.setColor(GREEN_YES);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                } else if(getModel().isRollover()){
                    g2.setColor(GOLD);
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                } else {
                    g2.setColor(new Color(196,154,108,0));
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8);
                    g2.setColor(GOLD);
                    g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8);
                }
                g2.dispose(); super.paintComponent(g);
            }
        };
        connectBtn.setOpaque(false); connectBtn.setContentAreaFilled(false);
        connectBtn.setBorderPainted(false); connectBtn.setFocusPainted(false);
        connectBtn.setFont(new Font("Georgia",Font.BOLD,10));
        connectBtn.setForeground(GOLD);
        connectBtn.setPreferredSize(new Dimension(130,38));
        connectBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        connectBtn.addActionListener(e -> handleConnect(skill, connectBtn));

        card.add(info, BorderLayout.CENTER);
        card.add(connectBtn, BorderLayout.EAST);
        return card;
    }

    private void handleConnect(Skill skill, JButton btn) {
        int myId    = currentUser.getId();
        int theirId = skill.getUser().getId();

        // Record this user's connect intent
        connectRequests.put(myId, skill.getId());
        btn.setForeground(GREEN_YES);
        btn.repaint();

        // Check if the other person also wants to connect
        boolean mutualConnect = connectRequests.containsKey(theirId)
                && connectRequests.get(theirId) != null;

        if (mutualConnect) {
            // Generate Jitsi meeting link
            String meetingRoom = "SkillBarters-" + sanitize(currentUser.getName())
                    + "-" + sanitize("User" + theirId)
                    + "-" + Integer.toHexString(new Random().nextInt(0xFFFF));
            String jitsiUrl = "https://meet.jit.si/" + meetingRoom;

            showJitsiDialog(jitsiUrl, skill);
            connectRequests.remove(myId);
            connectRequests.remove(theirId);
        } else {
            JOptionPane.showMessageDialog(this,
                "<html><body style='font-family:Georgia;color:#C49A6C;background:#1A0A02'>" +
                "<b>Connect request sent!</b><br><br>" +
                "Waiting for <i>" + skill.getTitle() + "</i> teacher to also connect.<br>" +
                "A Jitsi meeting link will be generated when both of you connect." +
                "</body></html>",
                "Connect Sent", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showJitsiDialog(String url, Skill skill) {
        JPanel panel = new JPanel(new BorderLayout(0,12)) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(0x1A0A02)); g.fillRect(0,0,getWidth(),getHeight());
            }
        };
        panel.setBorder(new EmptyBorder(20,24,20,24));
        panel.setPreferredSize(new Dimension(440,220));

        JLabel title = new JLabel("✦  Meeting Room Ready  ✦", SwingConstants.CENTER);
        title.setFont(new Font("Georgia",Font.BOLD,16));
        title.setForeground(GREEN_YES);

        JLabel sub = new JLabel("Both parties have connected! Your meeting is ready.", SwingConstants.CENTER);
        sub.setFont(new Font("Georgia",Font.ITALIC,12));
        sub.setForeground(GOLD_MUTED);

        JTextField linkField = new JTextField(url);
        linkField.setEditable(false);
        linkField.setBackground(new Color(0x0E0602));
        linkField.setForeground(TEAL);
        linkField.setFont(new Font("Monospaced",Font.PLAIN,11));
        linkField.setBorder(new CompoundBorder(
            new LineBorder(new Color(29,158,117,100),1,true),
            new EmptyBorder(8,10,8,10)));
        linkField.setHorizontalAlignment(JTextField.CENTER);
        linkField.selectAll();

        JButton joinBtn = new JButton("JOIN MEETING  ▶");
        joinBtn.setBackground(new Color(0x3B6D11));
        joinBtn.setForeground(new Color(0xEAF3DE));
        joinBtn.setFont(new Font("Georgia",Font.BOLD,12));
        joinBtn.setBorderPainted(false); joinBtn.setFocusPainted(false);
        joinBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        joinBtn.addActionListener(e2 -> {
            try { Desktop.getDesktop().browse(new java.net.URI(url)); }
            catch (Exception ex) { JOptionPane.showMessageDialog(panel,"Could not open browser: "+ex.getMessage()); }
        });

        panel.add(title, BorderLayout.NORTH);
        panel.add(sub,   BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout(0,8));
        bottom.setOpaque(false);
        bottom.add(linkField, BorderLayout.CENTER);
        bottom.add(joinBtn,   BorderLayout.SOUTH);
        panel.add(bottom, BorderLayout.SOUTH);

        JDialog dialog = new JDialog((Frame)null, "Jitsi Meet — Skill Barters", true);
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private String sanitize(String s) {
        return s.replaceAll("[^a-zA-Z0-9]","");
    }

    // ══════════════════════════════════════════════════════════
    //  TAB 3 — BARTER REQUESTS
    // ══════════════════════════════════════════════════════════
    private JPanel buildRequestsTab() {
        JPanel p = new JPanel(new BorderLayout(0,14));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(18,18,18,18));
        p.add(sectionHeader("⇄  Barter Requests"), BorderLayout.NORTH);

        JPanel body = new JPanel(new GridLayout(1,2,16,0));
        body.setOpaque(false);

        // Incoming requests
        JPanel incoming = buildRequestSection("Incoming Requests", true);
        // Outgoing requests
        JPanel outgoing = buildRequestSection("Sent Requests", false);

        body.add(incoming);
        body.add(outgoing);
        p.add(body, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildRequestSection(String title, boolean incoming) {
        JPanel p = new JPanel(new BorderLayout(0,8)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setColor(new Color(0x1A0A02));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),10,10);
                g2.setColor(new Color(196,154,108,35));
                g2.setStroke(new BasicStroke(.8f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,10,10);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(14,14,14,14));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Georgia",Font.BOLD,13));
        lbl.setForeground(GOLD_LIGHT);
        p.add(lbl, BorderLayout.NORTH);

        String[] cols = {"ID","From/To","Offered","Wanted","Status"};
        DefaultTableModel model = new DefaultTableModel(cols,0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };

        // Load data
        if (currentUser != null) {
            List<BarterRequest> reqs = incoming
                ? barterService.getRequestsForUser(currentUser.getId())
                : barterService.getSentRequests(currentUser.getId());
            for (BarterRequest br : reqs) {
                model.addRow(new Object[]{
                    br.getId(),
                    incoming ? "User #"+br.getRequester().getId() : "User #"+br.getProvider().getId(),
                    "Skill #"+br.getOfferedSkill().getId(),
                    "Skill #"+br.getWantedSkill().getId(),
                    br.getStatus()
                });
            }
        }

        JTable table = buildDarkTable(model);
        p.add(darkScroll(table), BorderLayout.CENTER);

        if (incoming) {
            JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT,8,4));
            btns.setOpaque(false);
            JButton acc = goldButton("Accept");
            JButton rej = new JButton("Reject");
            rej.setBackground(new Color(0x3D1010));
            rej.setForeground(RED_NO);
            rej.setFont(new Font("SansSerif",Font.BOLD,11));
            rej.setBorderPainted(false); rej.setFocusPainted(false);
            rej.setOpaque(true);
            acc.addActionListener(e -> {
                int row = table.getSelectedRow();
                if(row<0){JOptionPane.showMessageDialog(this,"Select a request first.");return;}
                int id = (int)model.getValueAt(row,0);
                barterService.acceptRequest(id);
                model.setValueAt("ACCEPTED",row,4);
            });
            rej.addActionListener(e -> {
                int row = table.getSelectedRow();
                if(row<0){JOptionPane.showMessageDialog(this,"Select a request first.");return;}
                int id = (int)model.getValueAt(row,0);
                barterService.rejectRequest(id);
                model.setValueAt("REJECTED",row,4);
            });
            btns.add(acc); btns.add(rej);
            p.add(btns, BorderLayout.SOUTH);
        }
        return p;
    }

    // ══════════════════════════════════════════════════════════
    //  TAB 4 — PROFILE
    // ══════════════════════════════════════════════════════════
    private JPanel buildProfileTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(18,18,18,18));

        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x1A0A02));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(new Color(196,154,108,50));
                g2.setStroke(new BasicStroke(.8f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(520,480));
        card.setBorder(new EmptyBorder(28,32,28,32));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx=0; c.gridy=0; c.gridwidth=2;
        c.fill=GridBagConstraints.HORIZONTAL;
        c.insets=new Insets(0,0,20,0);

        JLabel h = new JLabel("◉  Your Profile", SwingConstants.CENTER);
        h.setFont(new Font("Georgia",Font.BOLD,18)); h.setForeground(GOLD_LIGHT);
        card.add(h,c);

        // Helper to add a label-field pair
        int[] row = {1};
        java.util.function.BiConsumer<String,JComponent> addRow = (lbl,comp) -> {
            c.gridy=row[0]; c.gridwidth=1; c.insets=new Insets(0,0,12,12); c.gridx=0;
            JLabel l=new JLabel(lbl); l.setFont(new Font("SansSerif",Font.BOLD,10));
            l.setForeground(GOLD_MUTED);
            card.add(l,c);
            c.gridx=1; c.insets=new Insets(0,0,12,0); c.weightx=1;
            card.add(comp,c); c.weightx=0;
            row[0]++;
        };

        // Name (display only)
        JLabel nameVal = new JLabel(currentUser!=null?currentUser.getName():"");
        nameVal.setFont(new Font("SansSerif",Font.PLAIN,13));
        nameVal.setForeground(GOLD_LIGHT);
        addRow.accept("NAME", nameVal);

        // Email (display only)
        JLabel emailVal = new JLabel(currentUser!=null?currentUser.getEmail():"");
        emailVal.setFont(new Font("SansSerif",Font.PLAIN,13));
        emailVal.setForeground(GOLD_LIGHT);
        addRow.accept("EMAIL", emailVal);

        // Gender
        JComboBox<String> genderBox = darkCombo(GENDERS);
        addRow.accept("GENDER", genderBox);

        // Bio
        JTextField bioField = darkField(280);
        bioField.setToolTipText("Tell others about yourself");
        addRow.accept("BIO", bioField);

        // 1st teaching language
        JComboBox<String> lang1 = darkCombo(LANGUAGES);
        addRow.accept("1ST LANGUAGE", lang1);

        // 2nd teaching language
        JComboBox<String> lang2 = darkCombo(LANGUAGES);
        lang2.setSelectedIndex(1);
        addRow.accept("2ND LANGUAGE", lang2);

        // Save button
        c.gridy=row[0]; c.gridwidth=2; c.gridx=0; c.insets=new Insets(12,0,0,0);
        JButton saveBtn = goldButton("Save Profile  →");
        saveBtn.addActionListener(e -> {
            String bio = bioField.getText().trim();
            try {
                userService_updateBio(currentUser.getId(), bio);
                JOptionPane.showMessageDialog(this,"Profile saved successfully!");
            } catch(Exception ex) {
                JOptionPane.showMessageDialog(this,"Error: "+ex.getMessage());
            }
        });
        card.add(saveBtn,c);

        p.add(card);
        return p;
    }

    private void userService_updateBio(int id, String bio) {
        // Calls through to UserService.updateProfile
        // We pass current name to avoid overwriting it
        if (currentUser != null)
            mainApp.getUserService().updateProfile(id, currentUser.getName(), bio);
    }

    // ══════════════════════════════════════════════════════════
    //  TAB 5 — COMPLAINTS / QUERIES
    // ══════════════════════════════════════════════════════════
    private JPanel buildComplaintsTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(18,18,18,18));

        JPanel card = new JPanel(new BorderLayout(0,14)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x1A0A02));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),14,14);
                g2.setColor(new Color(196,154,108,50));
                g2.setStroke(new BasicStroke(.8f));
                g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,14,14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(520,420));
        card.setBorder(new EmptyBorder(28,32,28,32));

        JLabel h = new JLabel("✉  Complaints & Queries", SwingConstants.CENTER);
        h.setFont(new Font("Georgia",Font.BOLD,18)); h.setForeground(GOLD_LIGHT);
        card.add(h, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill=GridBagConstraints.HORIZONTAL; c.weightx=1;

        // Type selector
        c.gridx=0; c.gridy=0; c.insets=new Insets(0,0,5,0);
        JLabel typeL=new JLabel("TYPE"); typeL.setFont(new Font("SansSerif",Font.BOLD,10));
        typeL.setForeground(GOLD_MUTED); form.add(typeL,c);
        c.gridy=1; c.insets=new Insets(0,0,14,0);
        JComboBox<String> typeBox = darkCombo(new String[]{
            "General Query","Complaint Against User","Technical Issue",
            "Payment/Barter Issue","Feature Request","Other"});
        form.add(typeBox,c);

        // Subject
        c.gridy=2; c.insets=new Insets(0,0,5,0);
        JLabel subL=new JLabel("SUBJECT"); subL.setFont(new Font("SansSerif",Font.BOLD,10));
        subL.setForeground(GOLD_MUTED); form.add(subL,c);
        c.gridy=3; c.insets=new Insets(0,0,14,0);
        JTextField subField = darkField(0); subField.setToolTipText("Brief subject");
        form.add(subField,c);

        // Message area
        c.gridy=4; c.insets=new Insets(0,0,5,0);
        JLabel msgL=new JLabel("YOUR MESSAGE"); msgL.setFont(new Font("SansSerif",Font.BOLD,10));
        msgL.setForeground(GOLD_MUTED); form.add(msgL,c);
        c.gridy=5; c.insets=new Insets(0,0,16,0); c.weighty=1;
        c.fill=GridBagConstraints.BOTH;
        JTextArea msgArea = new JTextArea(6,0);
        msgArea.setBackground(BG_FIELD);
        msgArea.setForeground(GOLD_LIGHT);
        msgArea.setCaretColor(GOLD);
        msgArea.setFont(new Font("SansSerif",Font.PLAIN,12));
        msgArea.setLineWrap(true); msgArea.setWrapStyleWord(true);
        JScrollPane msgScroll = new JScrollPane(msgArea);
        msgScroll.setBorder(new CompoundBorder(
            new LineBorder(BORDER_DIM,1,true),
            new EmptyBorder(4,4,4,4)));
        msgScroll.setBackground(BG_FIELD);
        msgScroll.getViewport().setBackground(BG_FIELD);
        form.add(msgScroll,c);

        // Submit
        c.gridy=6; c.insets=new Insets(0,0,0,0); c.weighty=0;
        c.fill=GridBagConstraints.HORIZONTAL;
        JButton submit = goldButton("SUBMIT COMPLAINT  →");
        submit.addActionListener(e -> {
            String subject = subField.getText().trim();
            String message = msgArea.getText().trim();
            if (subject.isEmpty()||message.isEmpty()) {
                JOptionPane.showMessageDialog(this,"Please fill in subject and message."); return;
            }
            // In a real app, save to DB complaints table
            // For now, show confirmation
            JOptionPane.showMessageDialog(this,
                "<html><body style='font-family:Georgia'>" +
                "<b>Complaint submitted successfully!</b><br><br>" +
                "Type: " + typeBox.getSelectedItem() + "<br>" +
                "Subject: " + subject + "<br><br>" +
                "We will review and respond soon." +
                "</body></html>",
                "Submitted", JOptionPane.INFORMATION_MESSAGE);
            subField.setText(""); msgArea.setText("");
        });
        form.add(submit,c);

        card.add(form, BorderLayout.CENTER);
        p.add(card);
        return p;
    }

    // ── Notifications callback (concept 7) ─────────────────────
    private void onNotifications(List<Notification> notifs) {
        notifBadge.setText("🔔  " + notifs.size());
        notifBadge.setForeground(GOLD);
        StringBuilder sb = new StringBuilder("<html><body style='font-family:Georgia;color:#EDD9C0'>");
        sb.append("<b>").append(notifs.size()).append(" New Notification(s)</b><br><br>");
        notifs.forEach(n -> sb.append("• ").append(n.getMessage()).append("<br>"));
        sb.append("</body></html>");
        JOptionPane.showMessageDialog(this, sb.toString(), "Notifications", JOptionPane.INFORMATION_MESSAGE);
        notifBadge.setText("🔔  0");
        notifBadge.setForeground(GOLD_MUTED);
    }

    // ══════════════════════════════════════════════════════════
    //  SHARED UI HELPERS
    // ══════════════════════════════════════════════════════════
    private JLabel sectionHeader(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Georgia",Font.BOLD,16));
        l.setForeground(GOLD_LIGHT);
        l.setBorder(new EmptyBorder(0,0,6,0));
        return l;
    }

    private JLabel fieldLbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif",Font.BOLD,9));
        l.setForeground(GOLD_MUTED);
        return l;
    }

    private JTextField darkField(int width) {
        JTextField f = new JTextField();
        f.setBackground(BG_FIELD);
        f.setForeground(GOLD_LIGHT);
        f.setCaretColor(GOLD);
        f.setFont(new Font("SansSerif",Font.PLAIN,12));
        f.setOpaque(true);
        f.setBorder(new CompoundBorder(
            new LineBorder(BORDER_DIM,1,true),
            new EmptyBorder(7,10,7,10)));
        if (width > 0) f.setPreferredSize(new Dimension(width,36));
        f.addFocusListener(new FocusAdapter(){
            public void focusGained(FocusEvent e){
                f.setBorder(new CompoundBorder(new LineBorder(GOLD,1,true),new EmptyBorder(7,10,7,10)));
            }
            public void focusLost(FocusEvent e){
                f.setBorder(new CompoundBorder(new LineBorder(BORDER_DIM,1,true),new EmptyBorder(7,10,7,10)));
            }
        });
        return f;
    }

    private JComboBox<String> darkCombo(String[] items) {
        JComboBox<String> b = new JComboBox<>(items);
        b.setBackground(BG_FIELD);
        b.setForeground(GOLD_LIGHT);
        b.setFont(new Font("SansSerif",Font.PLAIN,12));
        b.setBorder(new LineBorder(BORDER_DIM,1,true));
        b.setRenderer(new DefaultListCellRenderer(){
            @Override public Component getListCellRendererComponent(JList<?> l,Object v,int i,boolean sel,boolean foc){
                super.getListCellRendererComponent(l,v,i,sel,foc);
                setBackground(sel?new Color(0x2C1A0E):BG_FIELD);
                setForeground(sel?GOLD_LIGHT:GOLD_MUTED);
                setFont(new Font("SansSerif",Font.PLAIN,12));
                setBorder(new EmptyBorder(4,8,4,8));
                return this;
            }
        });
        return b;
    }

    private JButton goldButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(GOLD); b.setForeground(BG_DEEP);
        b.setFont(new Font("SansSerif",Font.BOLD,12));
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){b.setBackground(GOLD_LIGHT);}
            public void mouseExited(MouseEvent e){b.setBackground(GOLD);}
        });
        return b;
    }

    private JTable buildDarkTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setBackground(BG_DEEP);
        t.setForeground(GOLD_LIGHT);
        t.setSelectionBackground(new Color(0x3D2010));
        t.setSelectionForeground(GOLD_LIGHT);
        t.setGridColor(new Color(196,154,108,30));
        t.setRowHeight(28);
        t.setFont(new Font("SansSerif",Font.PLAIN,12));
        t.setShowGrid(true);
        t.getTableHeader().setBackground(new Color(0x1A0A02));
        t.getTableHeader().setForeground(GOLD_MUTED);
        t.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,10));
        t.getTableHeader().setBorder(new MatteBorder(0,0,1,0,BORDER_DIM));
        // Alternating row colours
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(
                    JTable tbl,Object v,boolean sel,boolean foc,int r,int col){
                super.getTableCellRendererComponent(tbl,v,sel,foc,r,col);
                if(!sel){
                    setBackground(r%2==0?BG_DEEP:BG_ROW_ALT);
                    setForeground(GOLD_LIGHT);
                } else {
                    setBackground(new Color(0x3D2010));
                    setForeground(GOLD_LIGHT);
                }
                setBorder(new EmptyBorder(0,10,0,10));
                setFont(new Font("SansSerif",Font.PLAIN,12));
                return this;
            }
        });
        return t;
    }

    private JScrollPane darkScroll(JComponent comp) {
        JScrollPane s = new JScrollPane(comp);
        s.setOpaque(false);
        s.getViewport().setOpaque(false);
        s.setBorder(new LineBorder(BORDER_DIM,1,true));
        s.getVerticalScrollBar().setBackground(BG_CARD);
        s.getHorizontalScrollBar().setBackground(BG_CARD);
        return s;
    }

    private JPanel buildTag(String text, Color fg, Color bg) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif",Font.BOLD,9));
        l.setForeground(fg);
        l.setOpaque(true); l.setBackground(bg);
        l.setBorder(new CompoundBorder(
            new LineBorder(new Color(fg.getRed(),fg.getGreen(),fg.getBlue(),80),1,true),
            new EmptyBorder(2,7,2,7)));
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        p.setOpaque(false); p.add(l);
        return p;
    }
}