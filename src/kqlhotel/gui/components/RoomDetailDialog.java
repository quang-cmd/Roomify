package kqlhotel.gui.components;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import net.miginfocom.swing.MigLayout;
import kqlhotel.gui.theme.ThemeColors;
import kqlhotel.entity.Service;
import kqlhotel.entity.ServiceDetail;
import kqlhotel.entity.Room;
import kqlhotel.entity.Invoice;
import kqlhotel.entity.Customer;
import kqlhotel.dao.service.ServiceDAO;
import kqlhotel.dao.invoice.ServiceDetailDAO;

public class RoomDetailDialog extends JDialog {

    private final JPanel contentCardPanel = new JPanel(new CardLayout());
    private final CardLayout cardLayout = (CardLayout) contentCardPanel.getLayout();
    private String currentTab = "GUEST";
    private String roomId;
    private String roomType;
    private String roomPrice;
    private Invoice invoice;
    private Customer customer;
    private JPanel serviceListPanel;
    private JLabel lblTotalServices;
    private JLabel lblVat;
    private JLabel lblGrandTotal;
    private JPanel addBtnRow;
    private JPanel addFormPanel;
    private Runnable onCheckout;

    private JPanel tabGuestCont;
    private JPanel tabInvoiceCont;
    private JButton btnFooterLeft;

    public RoomDetailDialog(Window owner, Room room, Invoice invoice, Customer customer) {
        this(owner, room, invoice, customer, null);
    }

    public RoomDetailDialog(Window owner, Room room, Invoice invoice, Customer customer, Runnable onCheckout) {
        super(owner, "Room Detail - " + room.getRoomId(), ModalityType.APPLICATION_MODAL);
        this.roomId = room.getRoomId();
        this.roomType = room.getRoomType().getRoomTypeName();
        this.roomPrice = String.valueOf(room.getRoomType().getPrice());
        this.invoice = invoice;
        this.customer = customer;
        this.onCheckout = onCheckout;
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        RoundedPanel rootPanel = new RoundedPanel(16, Color.WHITE, new Color(226, 232, 240), 1);
        rootPanel.setLayout(new MigLayout("insets 0, wrap 1, gap 0", "[fill, 550!]", "[]"));
        rootPanel.setOpaque(false);
        
        rootPanel.add(createHeader(roomId, roomType, "Floor " + room.getFloor()), "growx");
        rootPanel.add(createTabsRow(), "growx");

        contentCardPanel.setOpaque(false);
        JScrollPane scrollPane = new JScrollPane(contentCardPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        contentCardPanel.add(createGuestTab(), "GUEST");
        contentCardPanel.add(createInvoiceTab(), "INVOICE");
        
        rootPanel.add(scrollPane, "grow, h 550!");
        rootPanel.add(createFooter(), "growx");

        setContentPane(rootPanel);
        pack();
        setLocationRelativeTo(owner);
        
        switchTab("GUEST");
    }

    private JPanel createHeader(String roomId, String roomType, String floor) {
        JPanel header = new JPanel(new MigLayout("insets 16 20 16 20", "[][grow][]", "[]")) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 250, 252));
                g2.fillRoundRect(0, 0, getWidth(), getHeight() + 20, 16, 16);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        JLabel title = new JLabel("Room " + roomId + " — Guest & Invoice");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(15, 23, 42));
        
        JLabel subtitle = new JLabel(roomType + " - " + floor);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(ThemeColors.PRIMARY);

        JPanel textPanel = new JPanel(new MigLayout("insets 0, wrap 1, gap 2"));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(subtitle);

        JButton btnClose = new JButton("×");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 24));
        btnClose.setForeground(new Color(100, 116, 139));
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        header.add(new JLabel("🛏"), "w 32!, h 32!");
        header.add(textPanel, "growx, gapx 10");
        header.add(btnClose, "top");

        return header;
    }

    private JPanel createTabsRow() {
        JPanel tabs = new JPanel(new MigLayout("insets 0 20 0 20, gap 20", "[][]", "[45!]"));
        tabs.setBackground(Color.WHITE);
        tabs.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));

        tabGuestCont = createTabBtn("Guest Info");
        tabInvoiceCont = createTabBtn("Invoice");

        tabGuestCont.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { switchTab("GUEST"); } });
        tabInvoiceCont.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { switchTab("INVOICE"); } });

        tabs.add(tabGuestCont, "growy");
        tabs.add(tabInvoiceCont, "growy");

        return tabs;
    }

    private JPanel createTabBtn(String label) {
        JPanel p = new JPanel(new MigLayout("insets 0 10 0 10", "[]", "[grow]"));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        p.add(lbl, "aligny center");
        p.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return p;
    }

    private void switchTab(String tab) {
        currentTab = tab;
        cardLayout.show(contentCardPanel, tab);
        
        if (tab.equals("GUEST")) {
            tabGuestCont.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ThemeColors.PRIMARY));
            tabInvoiceCont.setBorder(null);
            if(btnFooterLeft != null) btnFooterLeft.setText("View Invoice");
        } else {
            tabInvoiceCont.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ThemeColors.PRIMARY));
            tabGuestCont.setBorder(null);
            if(btnFooterLeft != null) btnFooterLeft.setText("View Guest Info");
        }
    }

    private JPanel createGuestTab() {
        JPanel pnl = new JPanel(new MigLayout("insets 20, wrap 1, gap 16", "[grow,fill]", "[]"));
        pnl.setBackground(Color.WHITE);

        String name = (customer != null) ? customer.getHoTenKH() : "Walk-in Guest";
        String phone = (customer != null) ? customer.getSdt() : "N/A";
        String email = (customer != null) ? customer.getEmail() : "N/A";

        pnl.add(new JLabel("PERSONAL INFORMATION") {{ setFont(new Font("Segoe UI", Font.BOLD, 10)); setForeground(new Color(100, 116, 139)); }});
        pnl.add(createInfoItem("Name", name));
        pnl.add(createInfoItem("Phone", phone));
        pnl.add(createInfoItem("Email", email));

        pnl.add(new JLabel("BOOKING INFORMATION") {{ setFont(new Font("Segoe UI", Font.BOLD, 10)); setForeground(new Color(100, 116, 139)); }}, "gapy 10 0");
        pnl.add(createInfoItem("Check-in", (invoice != null && invoice.getNgayLapHD() != null) ? invoice.getNgayLapHD().toString() : "N/A"));
        pnl.add(createInfoItem("Occupants", (invoice != null) ? String.valueOf(invoice.getSoLuongNguoi()) : "1"));

        return pnl;
    }

    private JPanel createInfoItem(String label, String value) {
        JPanel p = new JPanel(new MigLayout("insets 0, wrap 1, gap 2"));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(new Color(100, 116, 139));
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 13));
        p.add(l);
        p.add(v);
        return p;
    }

    private JPanel createInvoiceTab() {
        JPanel pnl = new JPanel(new MigLayout("insets 20, wrap 1, gap 16", "[grow,fill]", "[]"));
        pnl.setBackground(Color.WHITE);

        serviceListPanel = new JPanel(new MigLayout("insets 0, wrap 1, gap 0", "[grow,fill]", "[]"));
        serviceListPanel.setOpaque(false);
        
        pnl.add(new JLabel("INVOICE DETAILS") {{ setFont(new Font("Segoe UI", Font.BOLD, 10)); setForeground(new Color(100, 116, 139)); }});
        pnl.add(serviceListPanel, "growx");

        JPanel sumRow = new JPanel(new MigLayout("insets 16 0 0 0, wrap 2", "[grow][right]"));
        sumRow.setOpaque(false);
        lblGrandTotal = new JLabel("0.00 USD");
        lblGrandTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblGrandTotal.setForeground(ThemeColors.PRIMARY);
        sumRow.add(new JLabel("Total Amount Due") {{ setFont(new Font("Segoe UI", Font.BOLD, 14)); }});
        sumRow.add(lblGrandTotal);
        pnl.add(sumRow);

        JButton btnCheckout = new JButton("Confirm Checkout");
        btnCheckout.setBackground(new Color(34, 197, 94));
        btnCheckout.setForeground(Color.WHITE);
        btnCheckout.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCheckout.addActionListener(e -> {
            dispose();
            if (onCheckout != null) onCheckout.run();
        });
        pnl.add(btnCheckout, "h 44!, gapy 10 0");

        return pnl;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new MigLayout("insets 16 20 16 20", "[][grow][]"));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        btnFooterLeft = new JButton("View Invoice");
        btnFooterLeft.addActionListener(e -> {
            if (currentTab.equals("GUEST")) switchTab("INVOICE");
            else switchTab("GUEST");
        });

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dispose());

        footer.add(btnFooterLeft);
        footer.add(new JLabel(""), "growx");
        footer.add(btnClose);

        return footer;
    }
}
