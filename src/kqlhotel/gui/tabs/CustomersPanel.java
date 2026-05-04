package kqlhotel.gui.tabs;

import kqlhotel.bus.customer.CustomerBUS;
import kqlhotel.entity.Customer;
import kqlhotel.entity.CustomerBookingHistory;
import kqlhotel.gui.components.ThemeColors;
import kqlhotel.gui.components.ModernTable;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;

public class CustomersPanel extends JPanel {
    private final CustomerBUS bus = new CustomerBUS();
    private List<Customer> customers;
    private Customer selectedCustomer;
    
    private JPanel listContainer;
    private JPanel detailContainer;
    private JLabel totalCountLabel;
    private JLabel activeCountLabel;
    private SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    private kqlhotel.gui.AppFrame appFrame;

    public CustomersPanel(kqlhotel.gui.AppFrame appFrame) {
        this.appFrame = appFrame;
        setLayout(new BorderLayout());
        setBackground(ThemeColors.BACKGROUND);
        initComponents();
        loadCustomers();
    }

    public CustomersPanel() {
        this(null);
    }

    private void initComponents() {
        // Main split layout
        JPanel mainContent = new JPanel(new MigLayout("fill, insets 20", "[350!]20[fill]", "fill"));
        mainContent.setOpaque(false);

        // Left sidebar (List)
        JPanel sidebar = createSidebar();
        mainContent.add(sidebar, "growy");

        // Right content (Details)
        detailContainer = new JPanel(new BorderLayout());
        detailContainer.setOpaque(false);
        mainContent.add(detailContainer, "grow");

        add(mainContent, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 15));
        sidebar.setOpaque(false);

        // Stats Header
        JPanel statsHeader = createStatsHeader();
        sidebar.add(statsHeader, BorderLayout.NORTH);

        // List Scroll
        listContainer = new JPanel(new MigLayout("wrap, fillx, insets 0", "[fill]", ""));
        listContainer.setOpaque(false);
        JScrollPane scroll = new JScrollPane(listContainer);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        sidebar.add(scroll, BorderLayout.CENTER);

        return sidebar;
    }

    private JPanel createStatsHeader() {
        JPanel header = new JPanel(new MigLayout("wrap 2, fillx, insets 0", "[fill][fill]", ""));
        header.setOpaque(false);

        totalCountLabel = createStatLabel("0", "Khách hàng");
        activeCountLabel = createStatLabel("0", "Đang hoạt động");

        header.add(totalCountLabel);
        header.add(activeCountLabel);
        return header;
    }

    private JLabel createStatLabel(String value, String label) {
        JLabel l = new JLabel("<html><div style='text-align: center;'><span style='font-size: 18px; font-weight: bold; color: #1a1a1a;'>" + value + "</span><br/><span style='color: #666666;'>" + label + "</span></div></html>");
        l.setOpaque(true);
        l.setBackground(Color.WHITE);
        l.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 0, 0, 0.05f), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return l;
    }

    private void loadCustomers() {
        customers = bus.getAllWithStats();
        updateCounts();
        renderCustomerList(customers);
        if (!customers.isEmpty()) {
            showCustomerDetails(customers.get(0));
        }
    }

    private void updateCounts() {
        long active = customers.stream().filter(Customer::isDangHoatDong).count();
        totalCountLabel.setText("<html><div style='text-align: center;'><span style='font-size: 18px; font-weight: bold;'>" + customers.size() + "</span><br/>Khách hàng</div></html>");
        activeCountLabel.setText("<html><div style='text-align: center;'><span style='font-size: 18px; font-weight: bold; color: #2ecc71;'>" + active + "</span><br/>Đang hoạt động</div></html>");
    }

    private void renderCustomerList(List<Customer> list) {
        listContainer.removeAll();
        for (Customer c : list) {
            listContainer.add(createCustomerCard(c), "growx");
        }
        listContainer.revalidate();
        listContainer.repaint();
    }

    private JPanel createCustomerCard(Customer customer) {
        JPanel card = new JPanel(new MigLayout("insets 12", "[40!]12[fill]", ""));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0, 0.05f)));

        // Avatar placeholder
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(245, 245, 250));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(ThemeColors.PRIMARY);
                g2.setFont(new Font("Inter", Font.BOLD, 14));
                String initial = customer.getHoTenKH().substring(0, 1);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial, (getWidth() - fm.stringWidth(initial)) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(40, 40));

        JPanel text = new JPanel(new MigLayout("wrap, insets 0, gap 2", "[fill]", ""));
        text.setOpaque(false);
        
        JLabel name = new JLabel(customer.getHoTenKH());
        name.setFont(new Font("Inter", Font.BOLD, 13));
        
        JPanel meta = new JPanel();
        meta.setLayout(new BoxLayout(meta, BoxLayout.X_AXIS));
        meta.setOpaque(false);
        meta.add(createMutedLabel(customer.getMaKH()));
        meta.add(Box.createHorizontalStrut(8));
        meta.add(createStatusBadge(customer.isDangHoatDong() ? "Hoạt động" : "Không hoạt động", customer.isDangHoatDong()));
        meta.add(Box.createHorizontalStrut(8));
        meta.add(createMutedLabel("Lần cuối: " + formatDate(customer.getNgayDatGanNhatDate())));

        text.add(name);
        text.add(meta);

        card.add(avatar);
        card.add(text);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showCustomerDetails(customer);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(250, 250, 252));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
            }
        });

        return card;
    }

    private void showCustomerDetails(Customer customer) {
        this.selectedCustomer = customer;
        detailContainer.removeAll();
        
        JPanel content = new JPanel(new MigLayout("wrap, fillx, insets 0", "[fill]", ""));
        content.setOpaque(false);

        // Header Info
        content.add(createDetailHeader(customer), "h 180!");
        
        // Stats Row
        content.add(createDetailStats(customer), "h 100!, pady 20");
        
        // Main Tabs/Details
        content.add(createDetailTabs(customer), "growy");

        detailContainer.add(content, BorderLayout.CENTER);
        detailContainer.revalidate();
        detailContainer.repaint();
    }

    private JPanel createDetailHeader(Customer customer) {
        JPanel header = new JPanel(new MigLayout("insets 30, gap 25", "[100!]25[fill]", "fill"));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0, 0.05f)));

        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(245, 245, 250));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(ThemeColors.PRIMARY);
                g2.setFont(new Font("Inter", Font.BOLD, 32));
                String initial = customer.getHoTenKH().substring(0, 1);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial, (getWidth() - fm.stringWidth(initial)) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatar.setOpaque(false);

        JPanel info = new JPanel(new MigLayout("wrap, insets 0, gap 5", "[fill]", ""));
        info.setOpaque(false);
        
        JLabel name = new JLabel(customer.getHoTenKH());
        name.setFont(new Font("Inter", Font.BOLD, 24));
        
        JLabel sub = new JLabel(customer.getMaKH() + " • Hạng: " + mapRank(customer.getHangKH()));
        sub.setForeground(new Color(102, 102, 102));

        info.add(name);
        info.add(sub);
        info.add(createStatusBadge(customer.isDangHoatDong() ? "Đang hoạt động" : "Ngừng hoạt động", customer.isDangHoatDong()), "w 120!");

        header.add(avatar, "grow");
        header.add(info);
        return header;
    }

    private JPanel createDetailStats(Customer customer) {
        JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
        row.setOpaque(false);
        row.add(createStatCard("Tổng đặt phòng", String.valueOf(customer.getTongDatPhong())));
        row.add(createStatCard("Đã chi tiêu", formatCurrency(customer.getTongChiTieu())));
        row.add(createStatCard("Đặt phòng gần nhất", formatDate(customer.getNgayDatGanNhatDate())));
        return row;
    }

    private JPanel createDetailTabs(Customer customer) {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Inter", Font.BOLD, 13));
        
        // Basic Info Tab
        tabs.addTab("Thông tin cá nhân", createBasicInfoTab(customer));
        
        // History Tab
        tabs.addTab("Lịch sử đặt phòng", createHistoryTab(customer));

        return (JPanel) tabs.getComponent(0); // For demo, return first tab directly wrapped
    }

    private JPanel createBasicInfoTab(Customer customer) {
        JPanel grid = new JPanel(new MigLayout("wrap 2, fillx, insets 30, gap 20 20", "[fill][fill]", ""));
        grid.setBackground(Color.WHITE);
        grid.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0, 0.05f)));

        grid.add(createInfoCard("Số điện thoại", safe(customer.getSdt()), "telephone.png"));
        grid.add(createInfoCard("Địa chỉ email", safe(customer.getEmail()), "email.png"));
        grid.add(createInfoCard("Địa chỉ cư trú", safe(customer.getDiaChi()), "location.png"));
        grid.add(createInfoCard("Ngày sinh", formatDate(customer.getNgaySinhDate()), "calendar.png"));
        grid.add(createInfoCard("CCCD / Hộ chiếu", safe(customer.getCCCD()), "client.png"));
        grid.add(createInfoCard("Quốc tịch", safe(customer.getQuocTich()), "location.png"));
        return grid;
    }

    private JPanel createHistoryTab(Customer customer) {
        List<CustomerBookingHistory> history = bus.getBookingHistory(customer.getMaKH());
        String[] columns = {"Mã HD", "Ngày lập", "Tổng tiền", "Tình trạng"};
        Object[][] data = new Object[history.size()][4];
        for (int i = 0; i < history.size(); i++) {
            CustomerBookingHistory h = history.get(i);
            data[i][0] = h.getMaHD();
            data[i][1] = formatDate(h.getNgayLapHDDate());
            data[i][2] = formatCurrency(h.getTongTien());
            data[i][3] = h.getTinhTrang();
        }
        
        ModernTable table = new ModernTable(data, columns);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        
        JPanel p = new JPanel(new BorderLayout());
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private JPanel createStatCard(String title, String value) {
        JPanel p = new JPanel(new MigLayout("wrap, insets 15", "[fill]", ""));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0, 0.05f)));
        
        JLabel t = new JLabel(title);
        t.setForeground(new Color(102, 102, 102));
        t.setFont(new Font("Inter", Font.PLAIN, 12));
        
        JLabel v = new JLabel(value);
        v.setFont(new Font("Inter", Font.BOLD, 18));
        
        p.add(t);
        p.add(v);
        return p;
    }

    private JPanel createInfoCard(String title, String value, String iconPath) {
        JPanel p = new JPanel(new MigLayout("insets 0", "[20!]10[fill]", ""));
        p.setOpaque(false);
        
        JLabel t = new JLabel(title);
        t.setForeground(new Color(153, 153, 153));
        t.setFont(new Font("Inter", Font.PLAIN, 11));
        
        JLabel v = new JLabel(value);
        v.setFont(new Font("Inter", Font.BOLD, 13));
        
        JPanel text = new JPanel(new MigLayout("wrap, insets 0, gap 0", "[fill]", ""));
        text.setOpaque(false);
        text.add(t);
        text.add(v);
        
        p.add(new JLabel(), "growy"); // Icon placeholder
        p.add(text);
        return p;
    }

    private JLabel createMutedLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(153, 153, 153));
        l.setFont(new Font("Inter", Font.PLAIN, 11));
        return l;
    }

    private JPanel createStatusBadge(String text, boolean isActive) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Inter", Font.BOLD, 10));
        l.setForeground(isActive ? new Color(46, 204, 113) : new Color(149, 165, 166));
        
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 2));
        p.setBackground(isActive ? new Color(235, 251, 241) : new Color(242, 244, 244));
        p.add(l);
        return p;
    }

    private String formatDate(java.util.Date date) {
        return date == null ? "--" : DATE_FORMAT.format(date);
    }
    
    private String formatDate(java.time.LocalDateTime ldt) {
        if (ldt == null) return "--";
        java.util.Date date = java.util.Date.from(ldt.atZone(java.time.ZoneId.systemDefault()).toInstant());
        return DATE_FORMAT.format(date);
    }

    private String formatCurrency(double amount) {
        return java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("vi", "VN")).format(amount);
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "Chưa cập nhật" : s;
    }

    private String mapRank(String rank) {
        if ("Bac".equalsIgnoreCase(rank)) return "Bạc";
        if ("Vang".equalsIgnoreCase(rank)) return "Vàng";
        if ("KimCuong".equalsIgnoreCase(rank)) return "Kim Cương";
        return "Thường";
    }
}
