package com.skillbarter.ui;

import com.skillbarter.model.User;
import com.skillbarter.service.*;
import com.skillbarter.util.HibernateUtil;

import javax.swing.*;
import java.awt.*;

/**
 * CONCEPT 9 + 10: Main JFrame — uses CardLayout to switch between panels.
 * Entry point of the application.
 */
public class MainApp extends JFrame {

    private static final String CARD_LOGIN     = "LOGIN";
    private static final String CARD_REGISTER  = "REGISTER";
    private static final String CARD_DASHBOARD = "DASHBOARD";

    private final CardLayout  cardLayout;
    private final JPanel      cardPanel;

    private final UserService         userService;
    private final SkillService        skillService;
    private final BarterService       barterService;
    private final NotificationService notifService;

    private LoginPanel    loginPanel;
    private RegisterPanel registerPanel;
    private DashboardPanel dashboardPanel;

    public MainApp() {
        super("Skill Barter System");

        // Instantiate services
        userService   = new UserService();
        skillService  = new SkillService();
        barterService = new BarterService();
        notifService  = new NotificationService();

        // Setup frame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 620);
        setLocationRelativeTo(null);
        setResizable(true);

        // CardLayout for panel navigation
        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);

        loginPanel     = new LoginPanel(userService, this);
        registerPanel  = new RegisterPanel(userService, this);
        dashboardPanel = new DashboardPanel(skillService, barterService, notifService, this);

        cardPanel.add(loginPanel,     CARD_LOGIN);
        cardPanel.add(registerPanel,  CARD_REGISTER);
        cardPanel.add(dashboardPanel, CARD_DASHBOARD);

        add(cardPanel);
        showLoginPanel();

        // Cleanup on close
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                notifService.stopPolling();
                HibernateUtil.shutdown();
            }
        });
    }

    public void showLoginPanel() {
        loginPanel.clearFields();
        cardLayout.show(cardPanel, CARD_LOGIN);
    }

    public void showRegisterPanel() {
        cardLayout.show(cardPanel, CARD_REGISTER);
    }

    public void onLoginSuccess(User user) {
        dashboardPanel.setUser(user);
        cardLayout.show(cardPanel, CARD_DASHBOARD);
    }

    public static void main(String[] args) {
        // CONCEPT 9: launch Swing UI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            System.out.println("Starting Skill Barter System...");
            //new MainApp().setVisible(true);
            SplashScreen.show(() -> new MainApp().setVisible(true));
        });
    }
}
