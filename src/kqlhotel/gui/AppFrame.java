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

import kqlhotel.bus.shift.ShiftBUS;
import kqlhotel.gui.dialog.ShiftClosingDialog;
import kqlhotel.gui.components.BackgroundPanel;
import kqlhotel.gui.components.LoginBackgroundPanel;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.gui.utils.IconLoader;
import kqlhotel.gui.theme.ThemeColors;
import kqlhotel.gui.tabs.DashboardPanel;
import kqlhotel.gui.tabs.BookingPanel;
import kqlhotel.gui.tabs.CheckInPanel;
import kqlhotel.gui.tabs.LoginPanel;
import kqlhotel.gui.tabs.ShiftOpeningPanel;
import kqlhotel.gui.tabs.StatisticsPanel;
import kqlhotel.gui.tabs.CancelRoomPanel;
import kqlhotel.gui.tabs.RoomManagementPanel;
import kqlhotel.gui.tabs.StaffPanel;
import kqlhotel.gui.tabs.CheckoutPanel;
import kqlhotel.gui.tabs.PromotionsPanel;
import kqlhotel.gui.tabs.InvoicesPanel;
import kqlhotel.gui.tabs.SwapRoomPanel;
import kqlhotel.gui.tabs.CustomersPanel;
import kqlhotel.gui.tabs.ServicesPanel;
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
    private JLabel userNameLabel;
    private JLabel userRoleLabel;
    private JButton closeShiftBtn;
    private final Timer transitionTimer;
    private final Map<String, JPanel> menuItems = new LinkedHashMap<>();
    private final Map<String, JLabel> menuTextLabels = new LinkedHashMap<>();
    private final Map<String, String> pageTitles = new LinkedHashMap<>();
    private final Map<String, String> pageSubtitles = new LinkedHashMap<>();
    private String currentRoute = "booking";
    private String pendingCardName;
    private RoomManagementPanel roomManagementPanel;
    private BookingPanel bookingPanel;
    private CheckoutPanel checkoutPanel;
    private ShiftOpeningPanel shiftPanel;
    private LoginPanel loginPanel;

    public AppFrame() {
        setTitle("KQL Hotel - UI Demo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1366, 768));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        rootPanel.setOpaque(false);

        shiftPanel = new ShiftOpeningPanel(this::showAppTransition);

        loginPanel = new LoginPanel(this::showShiftTransition);
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
        DashboardPanel dashboardPanel = new DashboardPanel();
        screenPanel.add(dashboardPanel, "dashboard");
        bookingPanel = new BookingPanel();
        screenPanel.add(bookingPanel, "booking");
        screenPanel.add(new CheckInPanel(), "check-in");
        StatisticsPanel statisticsPanel = new StatisticsPanel();
        JScrollPane statisticsScroll = new JScrollPane(statisticsPanel);
        statisticsScroll.setBorder(BorderFactory.createEmptyBorder());
        statisticsScroll.getVerticalScrollBar().setUnitIncrement(16);
        statisticsScroll.getHorizontalScrollBar().setUnitIncrement(16);
        statisticsScroll.getViewport().setOpaque(false);
        statisticsScroll.setOpaque(false);
        screenPanel.add(statisticsScroll, "statistics");

        checkoutPanel = new CheckoutPanel();
        screenPanel.add(checkoutPanel, "checkout");

        screenPanel.add(new CancelRoomPanel(), "cancel-room");

        roomManagementPanel = new RoomManagementPanel();
        screenPanel.add(roomManagementPanel, "room-management");

        screenPanel.add(new StaffPanel(), "staff");

        screenPanel.add(new PromotionsPanel(), "promotions");
        screenPanel.add(new InvoicesPanel(), "invoices");
        screenPanel.add(new SwapRoomPanel(), "swap-room");
        screenPanel.add(new CustomersPanel(this), "customers");
        screenPanel.add(new ServicesPanel(), "services");
        activateRoute(currentRoute);

        contentWrap.add(screenPanel, BorderLayout.CENTER);
        appRoot.add(contentWrap, BorderLayout.CENTER);

        rootPanel.add(appRoot, "app");

        setContentPane(rootPanel);
        rootCards.show(rootPanel, "auth");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new MigLayout("wrap 1,insets 16,gap 4,hidemode 3", "[grow,fill]", "[]"));
        sidebar.setPreferredSize(new Dimension(240, 1));
        sidebar.setBackground(ThemeColors.PREMIUM_SIDEBAR_BG);
        // Right edge separator (gives the light sidebar a clean delimiter)
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
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 18f));

        JLabel brandSub = new JLabel("Management System");
        brandSub.setForeground(ThemeColors.PREMIUM_SIDEBAR_TEXT_MUTED);
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

        JLabel dashLabel = new JLabel("DASHBOARD");
        dashLabel.setForeground(ThemeColors.PREMIUM_SIDEBAR_TEXT_MUTED);
        dashLabel.setFont(dashLabel.getFont().deriveFont(Font.BOLD, 11f));
        sidebar.add(dashLabel, "gapy 4 2");
        registerPage("dashboard", "Dashboard", "Theo d\u00f5i ho\u1ea1t \u0111\u1ed9ng ca l\u00e0m vi\u1ec7c");
        sidebar.add(sidebarItem("\u25D0", new Color(30, 58, 138), "Dashboard", "dashboard"));

        JLabel menuLabel = new JLabel("MENU CH\u00cdNH");
        menuLabel.setForeground(ThemeColors.PREMIUM_SIDEBAR_TEXT_MUTED);
        menuLabel.setFont(menuLabel.getFont().deriveFont(Font.BOLD, 11f));
        sidebar.add(menuLabel, "gapy 4 2");

        registerPage("booking", "\u0110\u1eb7t ph\u00f2ng", "T\u00ecm ki\u1ebfm v\u00e0 \u0111\u1eb7t ph\u00f2ng cho kh\u00e1ch h\u00e0ng");
        registerPage("check-in", "Nh\u1eadn ph\u00f2ng", "X\u00e1c nh\u1eadn kh\u00e1ch \u0111\u1ebfn nh\u1eadn ph\u00f2ng v\u00e0 k\u00edch ho\u1ea1t h\u00f3a \u0111\u01a1n");
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
        sidebar.add(sidebarItem("\u2935", new Color(217, 119, 6),  "Nh\u1eadn ph\u00f2ng", "check-in"));
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

        // (Sidebar profile removed: user info + logout now live in the topbar)

        return sidebar;
    }

    private JPanel sidebarItem(String iconChar, Color iconColor, String text, String route) {
        SidebarMenuItem item = new SidebarMenuItem();
        item.setLayout(new BorderLayout(10, 0));
        item.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 12));
        item.setPreferredSize(new Dimension(0, 40));
        item.setMinimumSize(new Dimension(0, 40));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        item.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

        // Icon container - flat, no chip background
        JPanel iconBox = new JPanel(new BorderLayout());
        iconBox.setOpaque(false);
        iconBox.setPreferredSize(new Dimension(20, 20));

        // Thử load PNG icon, fallback về Unicode
        String iconFilename = getMenuIconFilename(route);
        ImageIcon pngIcon = loadMenuIcon(iconFilename, 18, 18);
        if (pngIcon != null) {
            JLabel iconLbl = new JLabel(pngIcon, SwingConstants.CENTER);
            iconBox.add(iconLbl, BorderLayout.CENTER);
        } else {
            JLabel iconLbl = new JLabel(iconChar, SwingConstants.CENTER);
            iconLbl.setForeground(iconColor);
            iconLbl.setFont(iconLbl.getFont().deriveFont(13f));
            iconBox.add(iconLbl, BorderLayout.CENTER);
        }

        JLabel textLbl = new JLabel(text);
        textLbl.setForeground(ThemeColors.PREMIUM_SIDEBAR_TEXT);
        textLbl.setFont(textLbl.getFont().deriveFont(Font.BOLD, 13.5f));
        textLbl.setHorizontalAlignment(SwingConstants.LEFT);
        textLbl.setVerticalAlignment(SwingConstants.CENTER);

        item.add(iconBox, BorderLayout.WEST);
        item.add(textLbl, BorderLayout.CENTER);

        item.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!Permission.canAccess(route)) {
                    JOptionPane.showMessageDialog(
                            AppFrame.this,
                            "Bạn không có quyền truy cập chức năng này!",
                            "Không có quyền",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                activateRoute(route);
            }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!route.equals(currentRoute)) {
                    item.setHovered(true);
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!route.equals(currentRoute)) {
                    item.setHovered(false);
                }
            }
        });

        menuItems.put(route, item);
        menuTextLabels.put(route, textLbl);
        return item;
    }

    /**
     * Sidebar menu item - flat by default, with hover/active states drawn manually.
     * Active: solid bg + 3px amber bar at left.
     */
    private static class SidebarMenuItem extends JPanel {
        private boolean active;
        private boolean hovered;

        SidebarMenuItem() {
            setOpaque(false);
        }

        void setActive(boolean active) {
            this.active = active;
            repaint();
        }

        void setHovered(boolean hovered) {
            this.hovered = hovered;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (active) {
                // Solid navy pill on white sidebar — text + icon flip to white
                g2.setColor(ThemeColors.PREMIUM_SIDEBAR_ACTIVE_BG);
                g2.fillRoundRect(0, 0, w, h, 10, 10);
            } else if (hovered) {
                g2.setColor(ThemeColors.PREMIUM_SIDEBAR_HOVER);
                g2.fillRoundRect(0, 0, w, h, 10, 10);
            }

            g2.dispose();
            super.paintComponent(g);
        }
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
            case "check-in": return "check-in.png";
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
        topbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BORDER_SOFT));

        JPanel titleWrap = new JPanel(new MigLayout("insets 0,wrap 1,gap 2", "[grow,fill]", "[]"));
        titleWrap.setOpaque(false);

        pageTitleLabel.setForeground(ThemeColors.TEXT_PRIMARY);
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
        JPanel panel = new JPanel(new MigLayout("insets 0,gap 12", "[][][][][]", "[]"));
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
                g2.setColor(ThemeColors.SURFACE_HOVER);
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
            bellLbl.setForeground(ThemeColors.TEXT_SECONDARY);
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
                g2.setColor(ThemeColors.DANGER);
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
                g.setColor(ThemeColors.BORDER_SOFT);
                g.drawLine(0, 4, 0, getHeight() - 4);
            }
        };
        sep.setOpaque(false);
        sep.setPreferredSize(new Dimension(1, 30));

        // User info
        JPanel userArea = new JPanel(new MigLayout("insets 0,gap 8", "[][grow,fill]", "[]"));
        userArea.setOpaque(false);

        JPanel avatar = createCircleAvatar(ThemeColors.ACCENT, "ND", 13f);

        JPanel userText = new JPanel(new MigLayout("insets 0,wrap 1,gap 1", "[grow,fill]", "[]"));
        userText.setOpaque(false);

        userNameLabel = new JLabel("Người dùng");
        userNameLabel.setForeground(ThemeColors.TEXT_PRIMARY);
        userNameLabel.setFont(userNameLabel.getFont().deriveFont(Font.BOLD, 13f));

        userRoleLabel = new JLabel("Nhân viên");
        userRoleLabel.setForeground(ThemeColors.TEXT_MUTED);
        userRoleLabel.setFont(userRoleLabel.getFont().deriveFont(11f));

        userText.add(userNameLabel);
        userText.add(userRoleLabel);

        userArea.add(avatar, "w 36!,h 36!,aligny center");
        userArea.add(userText, "aligny center");

        // Logout button: outline style, subtle red — less aggressive than
        // the previous solid red badge, harmonises with light topbar.
        JButton logoutBtn = new JButton("\u0110\u0103ng xu\u1ea5t");
        closeShiftBtn = new JButton("Kết ca");
        closeShiftBtn.setFont(closeShiftBtn.getFont().deriveFont(Font.BOLD, 12f));
        closeShiftBtn.setForeground(new Color(25, 135, 84));
        closeShiftBtn.setBackground(ThemeColors.PREMIUM_SURFACE);
        closeShiftBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(25, 135, 84), 1, true),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)
        ));
        closeShiftBtn.setFocusPainted(false);
        closeShiftBtn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        closeShiftBtn.setToolTipText("Kết ca làm việc hiện tại");
        closeShiftBtn.addActionListener(e -> showShiftClosingDialog());
        closeShiftBtn.setVisible(false);
        logoutBtn.setFont(logoutBtn.getFont().deriveFont(Font.BOLD, 12f));
        logoutBtn.setForeground(ThemeColors.DANGER);
        logoutBtn.setBackground(ThemeColors.PREMIUM_SURFACE);
        logoutBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.withAlpha(ThemeColors.DANGER, 90), 1, true),
            BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        logoutBtn.setToolTipText("\u0110\u0103ng xu\u1ea5t kh\u1ecfi h\u1ec7 th\u1ed1ng");
        logoutBtn.addActionListener(e -> logout());

        panel.add(bellWrap, "w 36!,h 36!,aligny center");
        panel.add(sep, "aligny center");
        panel.add(userArea, "aligny center");
        panel.add(closeShiftBtn, "aligny center,gapleft 8");
        panel.add(logoutBtn, "aligny center,gapleft 4");
        return panel;
    }

    public void navigateTo(String route) {
        activateRoute(route);
    }

    public void refreshRoomManagementData() {
        if (roomManagementPanel != null) {
            roomManagementPanel.reloadData();
        }
    }

    public void navigateToCheckoutWithRoom(String roomID) {
        if (!Permission.canAccess("checkout")) {
            JOptionPane.showMessageDialog(
                    this,
                    "Bạn không có quyền truy cập chức năng Trả phòng!",
                    "Không có quyền",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (checkoutPanel != null) {
            checkoutPanel.prefillAndSearchRoom(roomID);
            activateRoute("checkout");
        }
    }

    public BookingPanel getBookingPanel() {
        return bookingPanel;
    }

    private void showMainApp() {
        prepareAppForCurrentUser();
        rootCards.show(rootPanel, "app");
    }

    private void showShiftClosingDialog() {
        if (Permission.isQuanLy()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Tài khoản quản lý không cần kết ca.",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        String maNV = Session.currentStaff != null
                ? Session.currentStaff.getMaNV()
                : null;

        if (maNV == null || maNV.isBlank()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không xác định được nhân viên đang đăng nhập.",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        ShiftBUS shiftBUS = new ShiftBUS();
        kqlhotel.dao.shift.ShiftDAO.ShiftInfo shiftInfo = shiftBUS.getOpenShiftByStaff(maNV);

        if (shiftInfo == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Nhân viên hiện không có ca đang mở.",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        ShiftClosingDialog dialog = new ShiftClosingDialog(
                this,
                shiftInfo,
                () -> {
                    Session.clear();
                    showCleanLoginScreen();
                }
        );

        dialog.setVisible(true);
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

        Session.clear();
        showCleanLoginScreen();
    }

    private void showShiftTransition() {
        prepareAppForCurrentUser();

        if (Permission.isQuanLy()) {
            showTransition("app", "Đăng nhập thành công", "Đang vào giao diện chính...");
            return;
        }

        String maNV = Session.currentStaff != null
                ? Session.currentStaff.getMaNV()
                : null;

        if (maNV == null || maNV.isBlank()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không xác định được nhân viên đang đăng nhập.",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            Session.clear();
            showCleanLoginScreen();
            return;
        }

        ShiftBUS shiftBUS = new ShiftBUS();
        String activeMaNV = shiftBUS.getLatestOpenShiftStaffId();

        if (activeMaNV == null || activeMaNV.isBlank()) {
            showTransition("shift", "Đăng nhập thành công", "Đang mở màn hình kiểm kê tiền đầu ca...");
            return;
        }

        if (activeMaNV.equals(maNV)) {
            showTransition("app", "Đăng nhập thành công", "Đang quay lại ca làm việc hiện tại...");
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "Hiện đang có ca làm việc đang mở bởi nhân viên " + activeMaNV + ".\n"
                        + "Vui lòng kết ca hiện tại trước khi nhân viên khác đăng nhập.",
                "Ca làm việc đang mở",
                JOptionPane.WARNING_MESSAGE
        );

        Session.clear();
        showCleanLoginScreen();
    }

    private void showAppTransition() {
        prepareAppForCurrentUser();
        showTransition("app", "Xác nhận ca thành công", "Đang vào giao diện chính...");
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
        // Lúc AppFrame mới khởi tạo, user chưa đăng nhập nên Session.currentAccount = null.
        // Chỉ kiểm tra quyền sau khi đã đăng nhập.
        if (Session.currentAccount != null && !Permission.canAccess(route)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Bạn không có quyền truy cập chức năng này!",
                    "Không có quyền",
                    JOptionPane.WARNING_MESSAGE
            );

            String fallbackRoute = Permission.getDefaultRoute();
            if (!route.equals(fallbackRoute)) {
                activateRoute(fallbackRoute);
            }
            return;
        }

        currentRoute = route;
        screenCards.show(screenPanel, route);

        // Auto-refresh room management data when entering the tab
        if (route.equals("room-management")) {
            refreshRoomManagementData();
        }

        // Update page title with Vietnamese text
        Map<String, String> vnTitles = new java.util.HashMap<>();
        vnTitles.put("dashboard", "Dashboard");
        vnTitles.put("booking", "Đặt phòng");
        vnTitles.put("check-in", "Nhận phòng");
        vnTitles.put("checkout", "Trả phòng");
        vnTitles.put("swap-room", "Đổi phòng");
        vnTitles.put("cancel-room", "Hủy phòng");
        vnTitles.put("room-management", "Quản lý phòng");
        vnTitles.put("staff", "Nhân sự");
        vnTitles.put("customers", "Khách hàng");
        vnTitles.put("services", "Dịch vụ");
        vnTitles.put("promotions", "Khuyến mãi");
        vnTitles.put("invoices", "Hóa đơn");
        vnTitles.put("statistics", "Thống kê");

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
                lbl.setForeground(active
                        ? ThemeColors.PREMIUM_SIDEBAR_ACTIVE_TEXT
                        : ThemeColors.PREMIUM_SIDEBAR_TEXT);
                lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, active ? 14f : 13.5f));
            }
        }

        revalidate();
        repaint();
    }
    private String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "?";

        String[] parts = fullName.trim().split("\\s+");

        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }

        return (parts[0].substring(0, 1)
                + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private void updateCurrentUserInfo() {
        String fullName = kqlhotel.gui.Session.currentStaff != null
                ? kqlhotel.gui.Session.currentStaff.getFullName()
                : "Người dùng";

        String role = kqlhotel.gui.Session.currentAccount != null
                ? kqlhotel.gui.Session.currentAccount.getRole()
                : "";

        String roleText = "QuanLy".equalsIgnoreCase(role) ? "Quản lý" : "Nhân viên";

        if (userNameLabel != null) {
            userNameLabel.setText(fullName);
        }

        if (userRoleLabel != null) {
            userRoleLabel.setText(roleText);
        }

        if (closeShiftBtn != null) {
            closeShiftBtn.setVisible("NhanVien".equalsIgnoreCase(role));
        }
    }

    private void applyMenuPermissions() {
        for (Map.Entry<String, JPanel> entry : menuItems.entrySet()) {
            String route = entry.getKey();
            JPanel item = entry.getValue();

            boolean allowed = Permission.canAccess(route);
            item.setVisible(allowed);
        }

        revalidate();
        repaint();
    }

    private void prepareAppForCurrentUser() {
        updateCurrentUserInfo();
        applyMenuPermissions();

        currentRoute = Permission.getDefaultRoute();
        activateRoute(currentRoute);
    }

    private void showCleanLoginScreen() {
        if (loginPanel != null) {
            loginPanel.resetForm();
        }

        if (shiftPanel != null) {
            shiftPanel.resetOpeningForm();
        }

        currentRoute = "booking";
        pendingCardName = null;

        rootCards.show(rootPanel, "auth");
    }
}
