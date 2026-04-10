package kqlhotel.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.net.URL;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import kqlhotel.gui.components.BackgroundPanel;
import kqlhotel.gui.components.LoginBackgroundPanel;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.gui.theme.ThemeColors;
import kqlhotel.gui.tabs.BookingPanel;
import kqlhotel.gui.tabs.LoginPanel;
import kqlhotel.gui.tabs.ShiftOpeningPanel;
import kqlhotel.gui.tabs.StatisticsPanel;
import kqlhotel.gui.tabs.UnderDevelopmentPanel;
import kqlhotel.gui.tabs.CheckoutPanel;
import kqlhotel.gui.tabs.SwapRoomPanel;
import kqlhotel.gui.tabs.CancelRoomPanel;
import kqlhotel.gui.tabs.RoomManagementPanel;
import kqlhotel.gui.tabs.StaffPanel;
import kqlhotel.gui.tabs.CustomersPanel;
import kqlhotel.gui.tabs.ServicesPanel;
import kqlhotel.gui.tabs.PromotionsPanel;
import kqlhotel.gui.tabs.InvoicesPanel;
import net.miginfocom.swing.MigLayout;

public class AppFrame extends JFrame {
    private final CardLayout rootCards = new CardLayout();
    private final JPanel rootPanel = new JPanel(rootCards);
    private final CardLayout screenCards = new CardLayout();
    private final JPanel screenPanel = new JPanel(screenCards);
    private final JLabel pageTitleLabel = new JLabel();
    private final JLabel pageSubtitleLabel = new JLabel();
    private final JLabel transitionTitle = new JLabel();
    private final JLabel transitionMessage = new JLabel();
    private final Timer transitionTimer;
    private final Map<String, JPanel> menuItems = new LinkedHashMap<>();
    private final Map<String, JLabel> menuTextLabels = new LinkedHashMap<>();
    private final Map<String, String> pageTitles = new LinkedHashMap<>();
    private final Map<String, String> pageSubtitles = new LinkedHashMap<>();
    private String currentRoute = "booking";
    private String pendingCardName;

    public AppFrame() {
        setTitle("KQL Hotel - UI Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1366, 768));
        setLocationRelativeTo(null);

        rootPanel.setOpaque(false);

        ShiftOpeningPanel shiftPanel = new ShiftOpeningPanel(this::showAppTransition);

        LoginPanel loginPanel = new LoginPanel(this::showShiftTransition);
        rootPanel.add(loginPanel, "auth");
        rootPanel.add(shiftPanel, "shift");
        rootPanel.add(createTransitionPanel(), "transition");

        transitionTimer = new Timer(700, e -> {
            if (pendingCardName != null) {
                rootCards.show(rootPanel, pendingCardName);
                pendingCardName = null;
            }
        });
        transitionTimer.setRepeats(false);

        BackgroundPanel appRoot = new BackgroundPanel();
        appRoot.setLayout(new BorderLayout());

        JPanel sidebar = createSidebar();
        appRoot.add(sidebar, BorderLayout.WEST);

        JPanel contentWrap = new JPanel(new BorderLayout());
        contentWrap.setOpaque(false);
        contentWrap.add(createTopbar(), BorderLayout.NORTH);

        screenPanel.setOpaque(false);
        screenPanel.add(new BookingPanel(), "booking");
        screenPanel.add(new StatisticsPanel(), "statistics");
        screenPanel.add(new CheckoutPanel(), "checkout");
        screenPanel.add(new SwapRoomPanel(), "swap-room");
        screenPanel.add(new CancelRoomPanel(), "cancel-room");
        screenPanel.add(new RoomManagementPanel(), "room-management");
        screenPanel.add(new StaffPanel(), "staff");
        screenPanel.add(new CustomersPanel(), "customers");
        screenPanel.add(new ServicesPanel(), "services");
        screenPanel.add(new PromotionsPanel(), "promotions");
        screenPanel.add(new InvoicesPanel(), "invoices");
        activateRoute(currentRoute);

        contentWrap.add(screenPanel, BorderLayout.CENTER);
        appRoot.add(contentWrap, BorderLayout.CENTER);

        rootPanel.add(appRoot, "app");

        setContentPane(rootPanel);
        rootCards.show(rootPanel, "auth");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new MigLayout("wrap 1,insets 16,gap 4", "[grow,fill]", "[]push[]"));
        sidebar.setPreferredSize(new Dimension(240, 1));
        sidebar.setBackground(new Color(26, 32, 44));

        // Brand header with hotel icon
        JPanel hotelIcon = createCircleAvatar(ThemeColors.ACCENT, "KH", 14f);

        JLabel brand = new JLabel("KQL HOTEL");
        brand.setForeground(Color.WHITE);
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 18f));

        JLabel brandSub = new JLabel("Management System");
        brandSub.setForeground(ThemeColors.TEXT_MUTED);
        brandSub.setFont(brandSub.getFont().deriveFont(11f));

        JPanel brandTextWrap = new JPanel(new MigLayout("insets 0,wrap 1,gap 1", "[grow,fill]", "[]"));
        brandTextWrap.setOpaque(false);
        brandTextWrap.add(brand);
        brandTextWrap.add(brandSub);

        JPanel brandWrap = new JPanel(new MigLayout("insets 0,gap 10", "[][grow,fill]", "[]"));
        brandWrap.setOpaque(false);
        brandWrap.add(hotelIcon, "w 40!,h 40!,aligny center");
        brandWrap.add(brandTextWrap, "aligny center");
        sidebar.add(brandWrap, "gapy 4 14");

        JLabel menuLabel = new JLabel("MENU CH\u00cdNH");
        menuLabel.setForeground(new Color(100, 116, 139));
        menuLabel.setFont(menuLabel.getFont().deriveFont(Font.BOLD, 11f));
        sidebar.add(menuLabel, "gapy 4 2");

        registerPage("booking", "\u0110\u1eb7t ph\u00f2ng", "T\u00ecm ki\u1ebfm v\u00e0 \u0111\u1eb7t ph\u00f2ng cho kh\u00e1ch h\u00e0ng");
        registerPage("checkout", "Tr\u1ea3 ph\u00f2ng", "\u0110ang x\u1eed l\u00fd quy tr\u00ecnh tr\u1ea3 ph\u00f2ng");
        registerPage("swap-room", "\u0110\u1ed5i ph\u00f2ng", "\u0110ang x\u1eed l\u00fd quy tr\u00ecnh \u0111\u1ed5i ph\u00f2ng");
        registerPage("cancel-room", "H\u1ee7y ph\u00f2ng", "\u0110ang x\u1eed l\u00fd y\u00eau c\u1ea7u h\u1ee7y ph\u00f2ng");
        registerPage("room-management", "Qu\u1ea3n l\u00fd ph\u00f2ng", "C\u1eadp nh\u1eadt tr\u1ea1ng th\u00e1i v\u00e0 c\u1ea5u h\u00ecnh ph\u00f2ng");
        registerPage("staff", "Nh\u00e2n s\u1ef1", "Qu\u1ea3n l\u00fd h\u1ed3 s\u01a1 v\u00e0 ph\u00e2n c\u00f4ng nh\u00e2n s\u1ef1");
        registerPage("customers", "Kh\u00e1ch h\u00e0ng", "Qu\u1ea3n l\u00fd d\u1eef li\u1ec7u kh\u00e1ch h\u00e0ng");
        registerPage("services", "D\u1ecbch v\u1ee5", "Qu\u1ea3n l\u00fd d\u1ecbch v\u1ee5 b\u1ed5 sung");
        registerPage("promotions", "Khuy\u1ebfn m\u00e3i", "Qu\u1ea3n l\u00fd ch\u01b0\u01a1ng tr\u00ecnh \u01b0u \u0111\u00e3i");
        registerPage("invoices", "H\u00f3a \u0111\u01a1n", "Theo d\u00f5i h\u00f3a \u0111\u01a1n v\u00e0 thanh to\u00e1n");
        registerPage("statistics", "Th\u1ed1ng k\u00ea", "T\u1ed5ng quan doanh thu v\u00e0 c\u00f4ng su\u1ea5t ph\u00f2ng");

        sidebar.add(sidebarItem("\u25A1", new Color(49, 130, 206), "\u0110\u1eb7t ph\u00f2ng", "booking"));
        sidebar.add(sidebarItem("\u21A9", new Color(56, 161, 105), "Tr\u1ea3 ph\u00f2ng", "checkout"));
        sidebar.add(sidebarItem("\u2194", new Color(100, 100, 220), "\u0110\u1ed5i ph\u00f2ng", "swap-room"));
        sidebar.add(sidebarItem("\u2715", new Color(200, 80, 80), "H\u1ee7y ph\u00f2ng", "cancel-room"));
        sidebar.add(sidebarItem("\u2261", new Color(80, 160, 200), "Qu\u1ea3n l\u00fd ph\u00f2ng", "room-management"));
        sidebar.add(sidebarItem("\u25CF", new Color(143, 97, 255), "Nh\u00e2n s\u1ef1", "staff"));
        sidebar.add(sidebarItem("\u25CE", new Color(56, 180, 140), "Kh\u00e1ch h\u00e0ng", "customers"));
        sidebar.add(sidebarItem("\u2605", new Color(200, 130, 40), "D\u1ecbch v\u1ee5", "services"));
        sidebar.add(sidebarItem("\u25C6", new Color(190, 70, 180), "Khuy\u1ebfn m\u00e3i", "promotions"));
        sidebar.add(sidebarItem("\u2630", new Color(60, 130, 60), "H\u00f3a \u0111\u01a1n", "invoices"));
        sidebar.add(sidebarItem("\u25B2", new Color(180, 100, 40), "Th\u1ed1ng k\u00ea", "statistics"));

        // User profile at bottom
        sidebar.add(createSidebarUserProfile(), "gapy 10 0");

        return sidebar;
    }

    private JPanel sidebarItem(String iconChar, Color iconColor, String text, String route) {
        JPanel item = new JPanel(new MigLayout("insets 6 8", "[][6][grow,fill]", "[]"));
        item.setOpaque(true);
        item.setBackground(new Color(31, 41, 57));
        item.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

        // Small icon box
        JPanel iconBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(iconColor.getRed(), iconColor.getGreen(), iconColor.getBlue(), 40));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconBox.setOpaque(false);
        iconBox.setLayout(new BorderLayout());
        
        // Thử load PNG icon, fallback về Unicode
        String iconFilename = getMenuIconFilename(route);
        ImageIcon pngIcon = loadMenuIcon(iconFilename, 18, 18);
        if (pngIcon != null) {
            JLabel iconLbl = new JLabel(pngIcon);
            iconBox.add(iconLbl, BorderLayout.CENTER);
        } else {
            JLabel iconLbl = new JLabel(iconChar, SwingConstants.CENTER);
            iconLbl.setForeground(iconColor);
            iconLbl.setFont(iconLbl.getFont().deriveFont(12f));
            iconBox.add(iconLbl);
        }

        JLabel textLbl = new JLabel(text);
        textLbl.setForeground(new Color(200, 210, 225));
        textLbl.setFont(textLbl.getFont().deriveFont(13f));

        item.add(iconBox, "w 24!,h 24!");
        item.add(textLbl, "skip 1");

        item.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                activateRoute(route);
            }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!route.equals(currentRoute)) {
                    item.setBackground(new Color(40, 52, 70));
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!route.equals(currentRoute)) {
                    item.setBackground(new Color(31, 41, 57));
                }
            }
        });

        menuItems.put(route, item);
        menuTextLabels.put(route, textLbl);
        return item;
    }

    private ImageIcon loadMenuIcon(String filename, int width, int height) {
        try {
            URL resource = getClass().getResource("/kqlhotel/resources/icons/" + filename);
            if (resource == null) {
                String srcPath = "src/kqlhotel/resources/icons/" + filename;
                java.io.File file = new java.io.File(srcPath);
                if (file.exists()) {
                    resource = file.toURI().toURL();
                }
            }
            if (resource != null) {
                ImageIcon icon = new ImageIcon(resource);
                Image scaledImage = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }
        } catch (Exception e) {
            // Menu icon loading failed silently
        }
        return null;
    }

    private String getMenuIconFilename(String route) {
        switch (route) {
            case "booking": return "booking.png";
            case "checkout": return "checkout.png";
            case "swap-room": return "swap-room.png";
            case "cancel-room": return "cancel-room.png";
            case "room-management": return "room-management.png";
            case "staff": return "staff.png";
            case "customers": return "customers.png";
            case "services": return "services.png";
            case "promotions": return "promotions.png";
            case "invoices": return "invoices.png";
            case "statistics": return "statistics.png";
            default: return null;
        }
    }

    private JPanel createSidebarUserProfile() {
        JPanel divider = new JPanel();
        divider.setBackground(new Color(45, 57, 75));
        divider.setPreferredSize(new Dimension(0, 1));

        JPanel profilePanel = new JPanel(new MigLayout("insets 10 8", "[][grow,fill][]", "[]"));
        profilePanel.setOpaque(false);

        JPanel avatar = createCircleAvatar(new Color(49, 130, 206), "NL", 12f);

        JPanel textWrap = new JPanel(new MigLayout("insets 0,wrap 1,gap 1", "[grow,fill]", "[]"));
        textWrap.setOpaque(false);
        JLabel name = new JLabel("Nguy\u1ec5n Kh\u1ea3 Lu\u00e2n");
        name.setForeground(Color.WHITE);
        name.setFont(name.getFont().deriveFont(Font.BOLD, 12f));
        JLabel role = new JLabel("Qu\u1ea3n l\u00fd");
        role.setForeground(ThemeColors.TEXT_MUTED);
        role.setFont(role.getFont().deriveFont(11f));
        textWrap.add(name);
        textWrap.add(role);

        JButton logoutBtn = new JButton("\u2192");
        logoutBtn.setForeground(new Color(150, 168, 195));
        logoutBtn.setFont(logoutBtn.getFont().deriveFont(16f));
        logoutBtn.setContentAreaFilled(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setToolTipText("\u0110\u0103ng xu\u1ea5t");
        logoutBtn.addActionListener(e -> logout());

        profilePanel.add(avatar, "w 34!,h 34!,aligny center");
        profilePanel.add(textWrap, "aligny center");
        profilePanel.add(logoutBtn, "aligny center");

        JPanel wrapper = new JPanel(new MigLayout("wrap 1,insets 0,gap 0", "[grow,fill]", "[][]"));
        wrapper.setOpaque(false);
        wrapper.add(divider, "growx,h 1!");
        wrapper.add(profilePanel);
        return wrapper;
    }

    private JPanel createCircleAvatar(Color bg, String initials, float fontSize) {
        JPanel circle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        circle.setOpaque(false);
        circle.setLayout(new BorderLayout());
        JLabel lbl = new JLabel(initials, SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, fontSize));
        circle.add(lbl);
        return circle;
    }

    private JPanel createTopbar() {
        JPanel topbar = new JPanel(new MigLayout("insets 12 18", "[grow,fill][]", "[]"));
        topbar.setOpaque(false);
        topbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        JPanel titleWrap = new JPanel(new MigLayout("insets 0,wrap 1,gap 2", "[grow,fill]", "[]"));
        titleWrap.setOpaque(false);

        pageTitleLabel.setForeground(new Color(34, 52, 84));
        pageTitleLabel.setFont(pageTitleLabel.getFont().deriveFont(Font.BOLD, 22f));

        // Date below page title
        Calendar cal = Calendar.getInstance();
        String[] days = {"Ch\u1ee7 nh\u1eadt", "Th\u1ee9 hai", "Th\u1ee9 ba", "Th\u1ee9 t\u01b0", "Th\u1ee9 n\u0103m", "Th\u1ee9 s\u00e1u", "Th\u1ee9 b\u1ea3y"};
        String dayName = days[cal.get(Calendar.DAY_OF_WEEK) - 1];
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);
        JLabel dateLbl = new JLabel(dayName + ", " + day + " th\u00e1ng " + month + " " + year);
        dateLbl.setForeground(ThemeColors.TEXT_MUTED);
        dateLbl.setFont(dateLbl.getFont().deriveFont(12f));

        pageSubtitleLabel.setForeground(ThemeColors.TEXT_MUTED);
        titleWrap.add(pageTitleLabel);
        titleWrap.add(dateLbl);

        topbar.add(titleWrap);
        topbar.add(createTopbarRight(), "aligny center");
        return topbar;
    }

    private JPanel createTopbarRight() {
        JPanel panel = new JPanel(new MigLayout("insets 0,gap 12", "[][][]", "[]"));
        panel.setOpaque(false);

        // Notification bell with badge
        JPanel bellWrap = new JPanel(null);
        bellWrap.setOpaque(false);
        bellWrap.setPreferredSize(new Dimension(40, 36));

        JPanel bellCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(240, 244, 250));
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bellCircle.setOpaque(false);
        bellCircle.setLayout(new BorderLayout());
        bellCircle.setBounds(2, 2, 34, 34);
        
        JLabel bellLbl = new JLabel();
        ImageIcon bellPNG = loadMenuIcon("bell.png", 18, 18);
        if (bellPNG != null) {
            bellLbl.setIcon(bellPNG);
        } else {
            bellLbl.setText("\u25CE");
            bellLbl.setForeground(new Color(80, 100, 130));
            bellLbl.setFont(bellLbl.getFont().deriveFont(16f));
            bellLbl.setHorizontalAlignment(SwingConstants.CENTER);
        }
        bellCircle.add(bellLbl);

        // Red badge "2"
        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(220, 53, 69));
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setLayout(null);
        badge.setBounds(24, 0, 16, 16);
        JLabel badgeLbl = new JLabel("2", SwingConstants.CENTER);
        badgeLbl.setForeground(Color.WHITE);
        badgeLbl.setFont(badgeLbl.getFont().deriveFont(Font.BOLD, 9f));
        badgeLbl.setBounds(0, 0, 16, 16);
        badgeLbl.setVerticalAlignment(SwingConstants.CENTER);
        badge.add(badgeLbl);

        bellWrap.add(badge);
        bellWrap.add(bellCircle);
        bellWrap.setComponentZOrder(badge, 0);

        // Separator
        JPanel sep = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(220, 228, 240));
                g.drawLine(0, 4, 0, getHeight() - 4);
            }
        };
        sep.setOpaque(false);
        sep.setPreferredSize(new Dimension(1, 30));

        // User info
        JPanel userArea = new JPanel(new MigLayout("insets 0,gap 8", "[][grow,fill]", "[]"));
        userArea.setOpaque(false);

        JPanel avatar = createCircleAvatar(ThemeColors.ACCENT, "NL", 13f);

        JPanel userText = new JPanel(new MigLayout("insets 0,wrap 1,gap 1", "[grow,fill]", "[]"));
        userText.setOpaque(false);
        JLabel userName = new JLabel("Nguy\u1ec5n Kh\u1ea3 Lu\u00e2n");
        userName.setForeground(new Color(34, 52, 84));
        userName.setFont(userName.getFont().deriveFont(Font.BOLD, 13f));
        JLabel userRole = new JLabel("Qu\u1ea3n l\u00fd");
        userRole.setForeground(ThemeColors.TEXT_MUTED);
        userRole.setFont(userRole.getFont().deriveFont(11f));
        userText.add(userName);
        userText.add(userRole);

        userArea.add(avatar, "w 36!,h 36!,aligny center");
        userArea.add(userText, "aligny center");

        panel.add(bellWrap, "w 36!,h 36!,aligny center");
        panel.add(sep, "aligny center");
        panel.add(userArea, "aligny center");
        return panel;
    }

    private void showMainApp() {
        rootCards.show(rootPanel, "app");
        activateRoute(currentRoute);
    }

    private void logout() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "B\u1ea1n c\u00f3 ch\u1eafc ch\u1eafn mu\u1ed1n \u0111\u0103ng xu\u1ea5t?",
            "X\u00e1c nh\u1eadn",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        currentRoute = "booking";
        pendingCardName = null;
        rootCards.show(rootPanel, "auth");
    }

    private void showShiftTransition() {
        showTransition("shift", "\u0110\u0103ng nh\u1eadp th\u00e0nh c\u00f4ng", "\u0110ang m\u1edf m\u00e0n h\u00ecnh ki\u1ec3m k\u00ea ti\u1ec1n \u0111\u1ea7u ca...");
    }

    private void showAppTransition() {
        showTransition("app", "X\u00e1c nh\u1eadn ca th\u00e0nh c\u00f4ng", "\u0110ang v\u00e0o giao di\u1ec7n ch\u00ednh...");
    }

    private void showTransition(String nextCard, String title, String message) {
        transitionTitle.setText(title);
        transitionMessage.setText(message);
        pendingCardName = nextCard;
        rootCards.show(rootPanel, "transition");
        transitionTimer.restart();
    }

    private JPanel createTransitionPanel() {
        LoginBackgroundPanel panel = new LoginBackgroundPanel();
        panel.setLayout(new BorderLayout());

        JPanel wrapper = new JPanel(new MigLayout("insets 0", "[grow]", "[grow]"));
        wrapper.setOpaque(false);

        RoundedPanel card = new RoundedPanel(
            16,
            ThemeColors.SURFACE,
            ThemeColors.BORDER_SOFT,
            1f,
            new Color(17, 24, 39, 20),
            6
        );
        card.setLayout(new MigLayout("wrap 1,insets 24,gap 10", "[grow,fill]", "[]"));
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel logo = new JLabel("KH", SwingConstants.CENTER);
        logo.setForeground(new Color(255, 138, 44));
        logo.setFont(logo.getFont().deriveFont(28f));

        RoundedPanel logoBox = new RoundedPanel(14, new Color(20, 31, 59), new Color(255, 255, 255, 20), 1f);
        logoBox.setLayout(new BorderLayout());
        logoBox.add(logo, BorderLayout.CENTER);

        transitionTitle.setForeground(ThemeColors.TEXT_PRIMARY);
        transitionTitle.setFont(transitionTitle.getFont().deriveFont(26f));
        transitionTitle.setHorizontalAlignment(SwingConstants.CENTER);

        transitionMessage.setForeground(ThemeColors.TEXT_MUTED);
        transitionMessage.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel footer = new JLabel("Vui l\u00f2ng ch\u1edd gi\u00e2y l\u00e1t...");
        footer.setForeground(ThemeColors.TEXT_MUTED);
        footer.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(logoBox, "w 64!,h 64!,alignx center");
        card.add(transitionTitle);
        card.add(transitionMessage);
        card.add(footer);

        wrapper.add(card, "alignx center,aligny center,w 420!,h 260!");
        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    private void registerPage(String route, String title, String subtitle) {
        pageTitles.put(route, title);
        pageSubtitles.put(route, subtitle);
    }

    private void activateRoute(String route) {
        currentRoute = route;
        screenCards.show(screenPanel, route);

        // Update page title with Vietnamese text
        Map<String, String> vnTitles = new java.util.HashMap<>();
        vnTitles.put("booking", "\u0110\u1eb7t ph\u00f2ng");
        vnTitles.put("checkout", "Tr\u1ea3 ph\u00f2ng");
        vnTitles.put("swap-room", "\u0110\u1ed5i ph\u00f2ng");
        vnTitles.put("cancel-room", "H\u1ee7y ph\u00f2ng");
        vnTitles.put("room-management", "Qu\u1ea3n l\u00fd ph\u00f2ng");
        vnTitles.put("staff", "Nh\u00e2n s\u1ef1");
        vnTitles.put("customers", "Kh\u00e1ch h\u00e0ng");
        vnTitles.put("services", "D\u1ecbch v\u1ee5");
        vnTitles.put("promotions", "Khuy\u1ebfn m\u00e3i");
        vnTitles.put("invoices", "H\u00f3a \u0111\u01a1n");
        vnTitles.put("statistics", "Th\u1ed1ng k\u00ea");
        pageTitleLabel.setText(vnTitles.getOrDefault(route, "KQL HOTEL"));
        pageSubtitleLabel.setText(pageSubtitles.getOrDefault(route, ""));

        for (Map.Entry<String, JPanel> entry : menuItems.entrySet()) {
            boolean active = entry.getKey().equals(route);
            entry.getValue().setBackground(active ? new Color(237, 137, 54, 40) : new Color(31, 41, 57));
            JLabel lbl = menuTextLabels.get(entry.getKey());
            if (lbl != null) {
                lbl.setForeground(active ? ThemeColors.ACCENT : new Color(200, 210, 225));
                lbl.setFont(lbl.getFont().deriveFont(active ? Font.BOLD : Font.PLAIN, 13f));
            }
        }
    }
}
