package kqlhotel.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import kqlhotel.gui.components.BackgroundPanel;
import kqlhotel.gui.components.LoginBackgroundPanel;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.gui.utils.IconLoader;
import kqlhotel.gui.theme.ThemeColors;
import kqlhotel.gui.tabs.BookingPanel;
import kqlhotel.gui.tabs.CheckInPanel;
import kqlhotel.gui.tabs.LoginPanel;
import kqlhotel.gui.tabs.ShiftOpeningPanel;
import kqlhotel.gui.tabs.StatisticsPanel;
import kqlhotel.gui.tabs.SwapRoomPanel;
import kqlhotel.gui.tabs.CancelRoomPanel;
import kqlhotel.gui.tabs.RoomManagementPanel;
import kqlhotel.gui.tabs.StaffPanel;
import kqlhotel.gui.tabs.CustomersPanel;
import kqlhotel.gui.tabs.ServicesPanel;
import kqlhotel.gui.tabs.CheckoutPanel;
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

    // Controllers/Panels
    private RoomManagementPanel roomManagementPanel;
    private BookingPanel bookingPanel;
    private CheckoutPanel checkoutPanel;
    private CustomersPanel customersPanel;
    private SwapRoomPanel swapRoomPanel;
    private ServicesPanel servicesPanel;

    public AppFrame() {
        setTitle("KQL Hotel - Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1366, 768));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

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
        
        // Registering Panels
        bookingPanel = new BookingPanel();
        screenPanel.add(bookingPanel, "booking");
        
        screenPanel.add(new CheckInPanel(), "check-in");
        
        checkoutPanel = new CheckoutPanel();
        screenPanel.add(checkoutPanel, "checkout");
        
        swapRoomPanel = new SwapRoomPanel();
        screenPanel.add(swapRoomPanel, "swap-room");
        
        screenPanel.add(new CancelRoomPanel(), "cancel-room");
        
        roomManagementPanel = new RoomManagementPanel();
        screenPanel.add(roomManagementPanel, "room-management");
        
        screenPanel.add(new StaffPanel(), "staff");
        
        customersPanel = new CustomersPanel(this);
        screenPanel.add(customersPanel, "customers");
        
        servicesPanel = new ServicesPanel();
        screenPanel.add(servicesPanel, "services");
        
        screenPanel.add(new PromotionsPanel(), "promotions");
        screenPanel.add(new InvoicesPanel(), "invoices");
        
        StatisticsPanel statisticsPanel = new StatisticsPanel();
        JScrollPane statisticsScroll = new JScrollPane(statisticsPanel);
        statisticsScroll.setBorder(BorderFactory.createEmptyBorder());
        statisticsScroll.getVerticalScrollBar().setUnitIncrement(16);
        statisticsScroll.getHorizontalScrollBar().setUnitIncrement(16);
        statisticsScroll.getViewport().setOpaque(false);
        statisticsScroll.setOpaque(false);
        screenPanel.add(statisticsScroll, "statistics");

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
        sidebar.setBackground(ThemeColors.PREMIUM_SIDEBAR_BG);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ThemeColors.PREMIUM_SIDEBAR_BORDER));

        // Brand header with hotel icon (try image `logo.png`, fallback to initials)
        ImageIcon logoIcon = IconLoader.loadIconKeepRatio("logo.png", 40);
        JPanel hotelIcon;
        if (logoIcon != null) {
            JPanel logoPanel = new JPanel(new BorderLayout());
            logoPanel.setOpaque(false);
            logoPanel.add(new JLabel(logoIcon, SwingConstants.CENTER), BorderLayout.CENTER);
            hotelIcon = logoPanel;
        } else {
            hotelIcon = createCircleAvatar(ThemeColors.PREMIUM_PRIMARY, "KH", 14f);
        }

        JLabel brand = new JLabel("KQL HOTEL");
        brand.setForeground(ThemeColors.PREMIUM_TEXT_PRIMARY);
        brand.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JPanel brandWrapper = new JPanel(new MigLayout("insets 0,gap 12", "[]", "[]"));
        brandWrapper.setOpaque(false);
        brandWrapper.add(hotelIcon, "w 40!,h 40!");
        brandWrapper.add(brand);

        sidebar.add(brandWrapper, "gapy 4 14");

        JLabel menuLabel = new JLabel("MENU CH\u00cdNH");
        menuLabel.setForeground(ThemeColors.PREMIUM_SIDEBAR_TEXT_MUTED);
        menuLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        sidebar.add(menuLabel, "gapy 4 2");

        registerPage("booking", "\u0110\u1eb7t ph\u00f2ng", "T\u00ecm ki\u1ebfm v\u00e0 \u0111\u1eb7t ph\u00f2ng");
        registerPage("check-in", "Nh\u1eadn ph\u00f2ng", "X\u00e1c nh\u1eadn kh\u00e1ch \u0111\u1ebfn");
        registerPage("checkout", "Tr\u1ea3 ph\u00f2ng", "X\1eed l\u00fd quy tr\u00ecnh tr\u1ea3 ph\u00f2ng");
        registerPage("swap-room", "\u0110\u1ed5i ph\u00f2ng", "X\1eed l\u00fd quy tr\u00ecnh \u0111\u1ed5i ph\u00f2ng");
        registerPage("cancel-room", "H\u1ee7y ph\u00f2ng", "X\u1eed l\u00fd y\u00eau c\u1ea7u h\u1ee7y ph\u00f2ng");
        registerPage("room-management", "Qu\u1ea3n l\u00fd ph\u00f2ng", "C\u1eadp nh\u1eadt tr\u1ea1ng th\u00e1i ph\u00f2ng");
        registerPage("staff", "Nh\u00e2n s\u1ef1", "Qu\u1ea3n l\u00fd h\u1ed3 s\u01a1 nh\u00e2n vi\u00ean");
        registerPage("customers", "Kh\u00e1ch h\u00e0ng", "Qu\u1ea3n l\u00fd d\u1eef li\u1ec7u kh\u00e1ch h\u00e0ng");
        registerPage("services", "D\u1ecbch v\u1ee5", "Qu\u1ea3n l\u00fd d\u1ecbch v\u1ee5 b\u1ed5 sung");
        registerPage("promotions", "Khuy\u1ebfn m\u00e3i", "Qu\u1ea3n l\u00fd \u01b0u \u0111\u00e3i");
        registerPage("invoices", "H\u00f3a \u0111\u01a1n", "Theo d\u00f5i h\u00f3a \u0111\u01a1n");
        registerPage("statistics", "Th\u1ed1ng k\u00ea", "T\u1ed5 overview doanh thu");

        sidebar.add(sidebarItem("booking.png", "\u25a2", new Color(49, 130, 206), "\u0110\u1eb7t ph\u00f2ng", "booking"));
        sidebar.add(sidebarItem("check-in.png", "\u2913", new Color(217, 119, 6),  "Nh\u1eadn ph\u00f2ng", "check-in"));
        sidebar.add(sidebarItem("checkout.png", "\u21a9", new Color(56, 161, 105), "Tr\u1ea3 ph\u00f2ng", "checkout"));
        sidebar.add(sidebarItem("swap-room.png", "\u2194", new Color(100, 100, 220), "\u0110\u1ed5i ph\u00f2ng", "swap-room"));
        sidebar.add(sidebarItem("cancel-room.png", "\u2715", new Color(200, 80, 80), "H\u1ee7y ph\u00f2ng", "cancel-room"));
        sidebar.add(sidebarItem("room-management.png", "\u2261", new Color(80, 160, 200), "Qu\u1ea3n l\u00fd ph\u00f2ng", "room-management"));
        sidebar.add(sidebarItem("staff.png", "\u25cf", new Color(143, 97, 255), "Nh\u00e2n s\u1ef1", "staff"));
        sidebar.add(sidebarItem("customers.png", "\u25ce", new Color(56, 180, 140), "Kh\u00e1ch h\u00e0ng", "customers"));
        sidebar.add(sidebarItem("services.png", "\u2605", new Color(200, 130, 40), "D\u1ecbch v\u1ee5", "services"));
        sidebar.add(sidebarItem("promotions.png", "\u25c6", new Color(190, 70, 180), "Khuy\u1ebfn m\u00e3i", "promotions"));
        sidebar.add(sidebarItem("invoices.png", "\u2630", new Color(60, 130, 60), "H\u00f3a \u0111\u01a1n", "invoices"));
        sidebar.add(sidebarItem("statistics.png", "\u25b2", new Color(180, 100, 40), "Th\u1ed1ng k\u00ea", "statistics"));

        return sidebar;
    }

    private JPanel sidebarItem(String iconFile, String fallbackChar, Color iconColor, String text, String route) {
        SidebarMenuItem item = new SidebarMenuItem();
        item.setLayout(new BorderLayout(10, 0));
        item.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 12));
        item.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

        JLabel iconLbl = new JLabel();
        ImageIcon img = IconLoader.loadIcon(iconFile, 20, 20);
        if (img != null) {
            iconLbl.setIcon(img);
        } else {
            iconLbl.setText(fallbackChar);
            iconLbl.setForeground(iconColor);
            iconLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            iconLbl.setHorizontalAlignment(SwingConstants.CENTER);
        }

        JLabel textLbl = new JLabel(text);
        textLbl.setForeground(ThemeColors.PREMIUM_SIDEBAR_TEXT);
        textLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));

        item.add(iconLbl, BorderLayout.WEST);
        item.add(textLbl, BorderLayout.CENTER);

        item.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { activateRoute(route); }
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { if (!route.equals(currentRoute)) item.setHovered(true); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { if (!route.equals(currentRoute)) item.setHovered(false); }
        });

        menuItems.put(route, item);
        menuTextLabels.put(route, textLbl);
        return item;
    }

    private static class SidebarMenuItem extends JPanel {
        private boolean active;
        private boolean hovered;
        SidebarMenuItem() { setOpaque(false); }
        void setActive(boolean active) { this.active = active; repaint(); }
        void setHovered(boolean hovered) { this.hovered = hovered; repaint(); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (active) {
                g2.setColor(ThemeColors.PREMIUM_SIDEBAR_ACTIVE_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            } else if (hovered) {
                g2.setColor(ThemeColors.PREMIUM_SIDEBAR_HOVER);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private JPanel createTopbar() {
        JPanel topbar = new JPanel(new MigLayout("insets 12 18", "[grow,fill][]", "[]"));
        topbar.setOpaque(false);
        topbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BORDER_SOFT));

        JPanel titleWrap = new JPanel(new MigLayout("insets 0,wrap 1,gap 2", "[grow,fill]", "[]"));
        titleWrap.setOpaque(false);
        pageTitleLabel.setForeground(ThemeColors.TEXT_PRIMARY);
        pageTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleWrap.add(pageTitleLabel);
        titleWrap.add(pageSubtitleLabel);

        topbar.add(titleWrap);
        topbar.add(createTopbarRight(), "aligny center");
        return topbar;
    }

    private JPanel createTopbarRight() {
        JPanel panel = new JPanel(new MigLayout("insets 0,gap 18", "[][][grow,fill][]", "[]"));
        panel.setOpaque(false);

        // Notification Bell
        JPanel bellWrap = new JPanel(new BorderLayout());
        bellWrap.setOpaque(false);
        bellWrap.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        JLabel bell = new JLabel("\ud83d\udd14");
        bell.setForeground(ThemeColors.TEXT_MUTED);
        bellWrap.add(bell);

        // Vertical Separator
        JPanel sep = new JPanel();
        sep.setBackground(ThemeColors.BORDER_SOFT);
        sep.setPreferredSize(new Dimension(1, 30));

        // User info
        JPanel userArea = new JPanel(new MigLayout("insets 0,gap 8", "[][grow,fill]", "[]"));
        userArea.setOpaque(false);

        JPanel avatar = createCircleAvatar(ThemeColors.ACCENT, "NL", 13f);

        JPanel userText = new JPanel(new MigLayout("insets 0,wrap 1,gap 1", "[grow,fill]", "[]"));
        userText.setOpaque(false);
        JLabel userName = new JLabel("Nguy\u1ec5n Kh\u1ea3 Lu\u00e2n");
        userName.setForeground(ThemeColors.TEXT_PRIMARY);
        userName.setFont(userName.getFont().deriveFont(Font.BOLD, 13f));
        JLabel userRole = new JLabel("Qu\u1ea3n l\u00fd");
        userRole.setForeground(ThemeColors.TEXT_MUTED);
        userRole.setFont(userRole.getFont().deriveFont(11f));
        userText.add(userName);
        userText.add(userRole);

        userArea.add(avatar, "w 36!,h 36!,aligny center");
        userArea.add(userText, "aligny center");

        // Logout button
        JButton logoutBtn = new JButton("\u0110\u0103ng xu\u1ea5t");
        logoutBtn.setFont(logoutBtn.getFont().deriveFont(Font.BOLD, 12f));
        logoutBtn.setForeground(ThemeColors.DANGER);
        logoutBtn.setBackground(ThemeColors.PREMIUM_SURFACE);
        logoutBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.withAlpha(ThemeColors.DANGER, 90), 1, true),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> logout());

        panel.add(bellWrap, "w 36!,h 36!,aligny center");
        panel.add(sep, "aligny center");
        panel.add(userArea, "aligny center");
        panel.add(logoutBtn, "aligny center,gapleft 8");
        return panel;
    }

    public void navigateTo(String route) { activateRoute(route); }

    public void refreshRoomManagementData() { if (roomManagementPanel != null) roomManagementPanel.reloadData(); }

    public void navigateToCheckoutWithRoom(String roomID) {
        if (checkoutPanel != null) {
            checkoutPanel.prefillAndSearchRoom(roomID);
            activateRoute("checkout");
        }
    }

    public BookingPanel getBookingPanel() { return bookingPanel; }

    private void logout() { rootCards.show(rootPanel, "auth"); }

    private void showShiftTransition() { showTransition("shift", "\u0110\u0103ng nh\u1eadp th\u00e0nh c\u00f4ng", "\u0110ang m\u1edf ca l\u00e0m vi\u1ec7c..."); }

    private void showAppTransition() { showTransition("app", "M\u1edf ca th\u00e0nh c\u00f4ng", "\u0110ang v\u00e0o h\u1ec7 th\u1ed1ng..."); }

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

        ImageIcon logoIcon = IconLoader.loadIconKeepRatio("logo.png", 80);
        JLabel logoLabel;
        if (logoIcon != null) {
            logoLabel = new JLabel(logoIcon, SwingConstants.CENTER);
        } else {
            logoLabel = new JLabel("KH", SwingConstants.CENTER);
            logoLabel.setForeground(ThemeColors.ACCENT);
            logoLabel.setFont(logoLabel.getFont().deriveFont(48f));
        }

        transitionTitle.setForeground(ThemeColors.TEXT_PRIMARY);
        transitionTitle.setFont(transitionTitle.getFont().deriveFont(26f));
        transitionTitle.setHorizontalAlignment(SwingConstants.CENTER);

        transitionMessage.setForeground(ThemeColors.TEXT_MUTED);
        transitionMessage.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel footer = new JLabel("Vui l\u00f2ng ch\u1edd gi\u00e2y l\u00e1t...");
        footer.setForeground(ThemeColors.TEXT_MUTED);
        footer.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(logoLabel, "alignx center,gapy 8 8");
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
        
        // Auto-refresh room management data when entering the tab
        if (route.equals("room-management")) {
            refreshRoomManagementData();
        }

        // Update page title with Vietnamese text
        Map<String, String> vnTitles = new java.util.HashMap<>();
        vnTitles.put("booking", "\u0110\u1eb7t ph\u00f2ng");
        vnTitles.put("check-in", "Nh\u1eadn ph\u00f2ng");
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
            JPanel panel = entry.getValue();
            if (panel instanceof SidebarMenuItem) {
                ((SidebarMenuItem) panel).setActive(active);
                ((SidebarMenuItem) panel).setHovered(false);
            }
            JLabel lbl = menuTextLabels.get(entry.getKey());
            if (lbl != null) {
                lbl.setForeground(active ? ThemeColors.PREMIUM_SIDEBAR_ACTIVE_TEXT : ThemeColors.PREMIUM_SIDEBAR_TEXT);
                lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, active ? 14f : 13.5f));
            }
        }

        revalidate();
        repaint();
    }

    private JPanel createCircleAvatar(Color bgColor, String initials, float fontSize) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillOval(0, 0, getWidth(), getHeight());
                
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, (int) (getHeight() * 0.4)));
                java.awt.FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(initials)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(initials, x, y);
                g2.dispose();
            }
            @Override public void setPreferredSize(Dimension d) { super.setPreferredSize(d); }
        };
    }
}
