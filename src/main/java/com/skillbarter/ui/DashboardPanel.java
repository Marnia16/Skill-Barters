package com.skillbarter.ui;

import com.skillbarter.model.Notification;
import com.skillbarter.model.Skill;
import com.skillbarter.model.User;
import com.skillbarter.service.BarterService;
import com.skillbarter.service.NotificationService;
import com.skillbarter.service.SkillService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * CONCEPT 7, 8, 9, 10: Dashboard — tabbed panel showing skills,
 * barter requests, and live notifications via background thread.
 */
public class DashboardPanel extends JPanel {

    private final SkillService        skillService;
    private final BarterService       barterService;
    private final NotificationService notifService;
    private final MainApp             mainApp;
    private User currentUser;

    private JTabbedPane tabs;
    private DefaultTableModel skillTableModel;
    private JLabel notifBadge;
    private JLabel welcomeLabel;

    public DashboardPanel(SkillService skillService, BarterService barterService,
                          NotificationService notifService, MainApp mainApp) {
        this.skillService  = skillService;
        this.barterService = barterService;
        this.notifService  = notifService;
        this.mainApp       = mainApp;
        initUI();
    }

    public void setUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getName() + "!");
        loadSkills();
        // CONCEPT 7: start background notification polling
        notifService.startPolling(user.getId(), this::onNotificationsReceived);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(245, 247, 250));

        // Top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(50, 90, 160));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        welcomeLabel = new JLabel("Welcome!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        welcomeLabel.setForeground(Color.WHITE);

        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBar.setOpaque(false);

        notifBadge = new JLabel("🔔 0");
        notifBadge.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notifBadge.setForeground(Color.WHITE);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> {   // CONCEPT 10: event handling
            notifService.stopPolling();
            mainApp.showLoginPanel();
        });

        rightBar.add(notifBadge);
        rightBar.add(logoutBtn);
        topBar.add(welcomeLabel, BorderLayout.WEST);
        topBar.add(rightBar, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // Tabbed pane
        tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabs.addTab("My Skills",      buildSkillsTab());
        tabs.addTab("Search Skills",  buildSearchTab());
        tabs.addTab("Barter Requests", buildRequestsTab());
        add(tabs, BorderLayout.CENTER);
    }

    // ---- Skills Tab ----
    private JPanel buildSkillsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Title", "Category", "Level", "Available"};
        skillTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(skillTableModel);
        table.setRowHeight(24);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Add skill form
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JTextField titleF    = new JTextField(12);
        JTextField categoryF = new JTextField(10);
        JComboBox<Skill.SkillLevel> levelBox = new JComboBox<>(Skill.SkillLevel.values());
        JButton addBtn = new JButton("Add Skill");
        addBtn.setBackground(new Color(50, 90, 160));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);

        form.add(new JLabel("Title:")); form.add(titleF);
        form.add(new JLabel("Category:")); form.add(categoryF);
        form.add(new JLabel("Level:")); form.add(levelBox);
        form.add(addBtn);
        panel.add(form, BorderLayout.SOUTH);

        // CONCEPT 10: Event handling for add skill button
        addBtn.addActionListener(e -> {
            String title    = titleF.getText().trim();
            String category = categoryF.getText().trim();
            Skill.SkillLevel level = (Skill.SkillLevel) levelBox.getSelectedItem();
            if (title.isEmpty() || category.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title and category are required.");
                return;
            }
            try {
                skillService.addSkill(currentUser.getId(), title, "", category, level);
                titleF.setText(""); categoryF.setText("");
                loadSkills();
                JOptionPane.showMessageDialog(this, "Skill added successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    // ---- Search Tab ----
    private JPanel buildSearchTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JTextField searchField = new JTextField(20);
        JButton searchBtn = new JButton("Search");
        searchBtn.setBackground(new Color(50, 90, 160));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        searchBar.add(new JLabel("Keyword:")); searchBar.add(searchField); searchBar.add(searchBtn);
        panel.add(searchBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Title", "Category", "Level", "Owner"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable resultTable = new JTable(model);
        resultTable.setRowHeight(24);
        panel.add(new JScrollPane(resultTable), BorderLayout.CENTER);

        // CONCEPT 10: search event
        searchBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            if (keyword.isEmpty()) return;
            model.setRowCount(0);
            // CONCEPT 8: iterate over List<Skill>
            List<Skill> results = skillService.searchSkills(keyword);
            for (Skill s : results) {
                model.addRow(new Object[]{
                    s.getId(), s.getTitle(), s.getCategory(),
                    s.getSkillLevel(), "User #" + s.getUser().getId()
                });
            }
            if (results.isEmpty())
                JOptionPane.showMessageDialog(panel, "No skills found for: " + keyword);
        });

        return panel;
    }

    // ---- Requests Tab ----
    private JPanel buildRequestsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel info = new JLabel("Incoming barter requests will appear here. Use Search to send a request.");
        info.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        info.setForeground(Color.GRAY);
        panel.add(info, BorderLayout.NORTH);

        String[] cols = {"ID", "From", "Offered Skill", "Wanted Skill", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(24);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> {
            model.setRowCount(0);
            barterService.getRequestsForUser(currentUser.getId()).forEach(br ->
                model.addRow(new Object[]{
                    br.getId(),
                    "User #" + br.getRequester().getId(),
                    "Skill #" + br.getOfferedSkill().getId(),
                    "Skill #" + br.getWantedSkill().getId(),
                    br.getStatus()
                })
            );
        });
        panel.add(refreshBtn, BorderLayout.SOUTH);
        return panel;
    }

    private void loadSkills() {
        if (currentUser == null) return;
        skillTableModel.setRowCount(0);
        // CONCEPT 8: List<Skill> iteration
        List<Skill> skills = skillService.getSkillsByUser(currentUser.getId());
        for (Skill s : skills) {
            skillTableModel.addRow(new Object[]{
                s.getId(), s.getTitle(), s.getCategory(),
                s.getSkillLevel(), s.isAvailable() ? "Yes" : "No"
            });
        }
    }

    // Called from background notification thread (concept 7)
    private void onNotificationsReceived(List<Notification> notifs) {
        notifBadge.setText("🔔 " + notifs.size());
        notifBadge.setForeground(new Color(255, 220, 0));
        StringBuilder sb = new StringBuilder("New notifications:\n");
        notifs.forEach(n -> sb.append("• ").append(n.getMessage()).append("\n"));
        JOptionPane.showMessageDialog(this, sb.toString(), "Notifications", JOptionPane.INFORMATION_MESSAGE);
        notifBadge.setText("🔔 0");
        notifBadge.setForeground(Color.WHITE);
    }
}
