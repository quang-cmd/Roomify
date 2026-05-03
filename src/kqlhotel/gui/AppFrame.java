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
    private final Map<String, String> pageSubtitles = new LinkedHashMap<>();
    private String currentRoute = "booking";
    private String pendingCardName;
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

        JLabel brand = new JLabel("KQL HOTEL");
        brand.setForeground(ThemeColors.PREMIUM_TEXT_PRIMARY);
        brand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sidebar.add(brand, "gapy 4 14");

        JLabel menuLabel = new JLabel("MENU CHÍNH");
        menuLabel.setForeground(ThemeColors.PREMIUM_SIDEBAR_TEXT_MUTED);
        menuLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        sidebar.add(menuLabel, "gapy 4 2");

        registerPage("booking", "Đặt phòng", "Tìm kiếm và đặt phòng");
        registerPage("check-in", "Nhận phòng", "Xác nhận khách đến");
        registerPage("checkout", "Trả phòng", "Xử lý quy trình trả phòng");
        registerPage("swap-room", "Đổi phòng", "Xử lý quy trình đổi phòng");
        registerPage("cancel-room", "Hủy phòng", "Xử lý yêu cầu hủy phòng");
        registerPage("room-management", "Quản lý phòng", "Cập nhật trạng thái phòng");
        registerPage("staff", "Nhân sự", "Quản lý hồ sơ nhân viên");
        registerPage("customers", "Khách hàng", "Quản lý dữ liệu khách hàng");
        registerPage("services", "Dịch vụ", "Quản lý dịch vụ bổ sung");
        registerPage("promotions", "Khuyến mãi", "Quản lý ưu đãi");
        registerPage("invoices", "Hóa đơn", "Theo dõi hóa đơn");
        registerPage("statistics", "Thống kê", "Tổng quan doanh thu");

        sidebar.add(sidebarItem("▢", new Color(49, 130, 206), "Đặt phòng", "booking"));
        sidebar.add(sidebarItem("⤓", new Color(217, 119, 6),  "Nhận phòng", "check-in"));
        sidebar.add(sidebarItem("↩", new Color(56, 161, 105), "Trả phòng", "checkout"));
        sidebar.add(sidebarItem("↔", new Color(100, 100, 220), "Đổi phòng", "swap-room"));
        sidebar.add(sidebarItem("✕", new Color(200, 80, 80), "Hủy phòng", "cancel-room"));
        sidebar.add(sidebarItem("≡", new Color(80, 160, 200), "Quản lý phòng", "room-management"));
        sidebar.add(sidebarItem("●", new Color(143, 97, 255), "Nhân sự", "staff"));
        sidebar.add(sidebarItem("◎", new Color(56, 180, 140), "Khách hàng", "customers"));
        sidebar.add(sidebarItem("★", new Color(200, 130, 40), "Dịch vụ", "services"));
        sidebar.add(sidebarItem("◆", new Color(190, 70, 180), "Khuyến mãi", "promotions"));
        sidebar.add(sidebarItem("☰", new Color(60, 130, 60), "Hóa đơn", "invoices"));
        sidebar.add(sidebarItem("▲", new Color(180, 100, 40), "Thống kê", "statistics"));

        return sidebar;
    }

    private JPanel sidebarItem(String iconChar, Color iconColor, String text, String route) {
        SidebarMenuItem item = new SidebarMenuItem();
        item.setLayout(new BorderLayout(10, 0));
        item.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 12));
        item.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

        JLabel iconLbl = new JLabel(iconChar, SwingConstants.CENTER);
        iconLbl.setForeground(iconColor);
        iconLbl.setFont(new Font("Segoe UI", Font.PLAIN, 16));

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
        JPanel panel = new JPanel(new MigLayout("insets 0,gap 12", "[][][]", "[]"));
        panel.setOpaque(false);
        JButton logoutBtn = new JButton("Đăng xuất");
        logoutBtn.addActionListener(e -> logout());
        panel.add(new JLabel("Admin"));
        panel.add(logoutBtn);
        return panel;
    }

    public void navigateTo(String route) { activateRoute(route); }

<<<<<<< HEAD
    public void refreshRoomManagementData() { if (roomManagementPanel != null) roomManagementPanel.reloadData(); }

    public void navigateToCheckoutWithRoom(String roomID) {
        if (checkoutPanel != null) {
            checkoutPanel.prefillAndSearchRoom(roomID);
            activateRoute("checkout");
=======
    public void navigateToCheckoutWithRoom(String roomId) {
        activateRoute("checkout");
        // We might need to tell CheckoutPanel to load this room
    }

    public void refreshRoomManagementData() {
        if (roomManagementPanel != null) {
            roomManagementPanel.reloadData();
>>>>>>> 67e24f5 (Standardize GUI logic to English keys and keep Vietnamese UI labels (RoomManagement & RoomDetail))
        }
    }

    private void logout() { rootCards.show(rootPanel, "auth"); }

    private void showShiftTransition() { showTransition("shift", "Đăng nhập thành công", "Đang mở ca làm việc..."); }

    private void showAppTransition() { showTransition("app", "Mở ca thành công", "Đang vào hệ thống..."); }

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
        JLabel l = new JLabel("Đang tải...", SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(l);
        return panel;
    }

    private void registerPage(String route, String title, String subtitle) {
        pageSubtitles.put(route, subtitle);
    }

    private void activateRoute(String route) {
        currentRoute = route;
        screenCards.show(screenPanel, route);

        if (route.equals("room-management")) {
            refreshRoomManagementData();
        }

        Map<String, String> vnTitles = new java.util.HashMap<>();
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
        
        for (Map.Entry<String, JPanel> entry : menuItems.entrySet()) {
            if (entry.getValue() instanceof SidebarMenuItem) {
                ((SidebarMenuItem) entry.getValue()).setActive(entry.getKey().equals(route));
            }
        }
    }
}
