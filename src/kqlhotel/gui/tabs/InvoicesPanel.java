package kqlhotel.gui.tabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import kqlhotel.gui.components.PrimaryButton;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.gui.theme.ThemeColors;
import net.miginfocom.swing.MigLayout;

public class InvoicesPanel extends JPanel {
    private static final Color PAGE_BG = new Color(245, 248, 252);
    private final JPanel listPanel = new JPanel(new MigLayout("wrap 1,insets 0,gap 8", "[grow,fill]", "[]"));
    private final RoundedPanel detailPanel = new RoundedPanel(16, Color.WHITE, new Color(225, 231, 245), 1f);

    private final List<InvoiceData> mockInvoices = Arrays.asList(
        new InvoiceData("KQL-2026-0125", "Lê Văn Dũng", "28.061.000đ", "Đặt cọc", new Color(230, 154, 30)),
        new InvoiceData("KQL-2026-0124", "Hoàng Minh Tú", "7.590.000đ", "Đã thanh toán", new Color(30, 180, 120)),
        new InvoiceData("KQL-2026-0123", "Đặng Quốc Bảo", "10.461.000đ", "Đã thanh toán", new Color(30, 180, 120)),
        new InvoiceData("KQL-2026-0122", "Trần Minh Khoa", "5.324.000đ", "Chưa thanh toán", new Color(240, 60, 60)),
        new InvoiceData("KQL-2026-0121", "Nguyễn Thị Hoa", "17.270.000đ", "Chưa thanh toán", new Color(240, 60, 60))
    );

    public InvoicesPanel() {
        setOpaque(false);
        setBackground(PAGE_BG);
        setLayout(new BorderLayout());

        JPanel header = createHeader();
        
        JPanel content = new JPanel(new MigLayout("insets 20 24 24 24, gap 20", "[350!][grow,fill]", "[grow,fill]"));
        content.setOpaque(false);
        
        JPanel leftSide = createLeftSide();
        createRightSide(); // Initializes detailPanel
        
        content.add(leftSide, "growy");
        content.add(detailPanel, "grow");

        add(header, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);

        renderList(mockInvoices);
        showDetail(mockInvoices.get(0)); // Show default
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new MigLayout("insets 20 24 0 24,gap 0", "[grow]", "[]"));
        panel.setOpaque(false);
        
        JPanel titleBox = new JPanel(new MigLayout("insets 0, wrap 1", "[]", "[]"));
        titleBox.setOpaque(false);
        JLabel title = new JLabel("Hóa đơn");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setForeground(new Color(24, 40, 66));
        JLabel subtitle = new JLabel("5 hóa đơn \u00B7 2 chưa thanh toán \u00B7 1 đặt cọc");
        subtitle.setForeground(new Color(119, 137, 168));
        subtitle.setFont(subtitle.getFont().deriveFont(13f));
        titleBox.add(title);
        titleBox.add(subtitle);

        panel.add(titleBox, "aligny center");
        return panel;
    }

    private JPanel createLeftSide() {
        JPanel left = new JPanel(new MigLayout("wrap 1,insets 0,gap 16", "[grow,fill]", "[][grow,fill]"));
        left.setOpaque(false);

        // Filters
        JPanel filters1 = new JPanel(new MigLayout("insets 0,gap 10", "[][]", "[]"));
        filters1.setOpaque(false);
        filters1.add(createFilterBtn("Tất cả (5)", true));
        filters1.add(createFilterBtn("Đã thanh toán (2)", false));
        
        JPanel filters2 = new JPanel(new MigLayout("insets 0,gap 10", "[][]", "[]"));
        filters2.setOpaque(false);
        filters2.add(createFilterBtn("Chưa thanh toán (2)", false));
        filters2.add(createFilterBtn("Đặt cọc (1)", false));

        listPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        // Tắt bar scroll để đẹp
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        left.add(filters1);
        left.add(filters2);
        left.add(scroll, "grow");
        return left;
    }

    private PrimaryButton createFilterBtn(String text, boolean active) {
        PrimaryButton btn = new PrimaryButton(text);
        if (active) {
            btn.setBackground(new Color(24, 34, 52));
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(100, 115, 135));
            btn.setBorder(BorderFactory.createLineBorder(new Color(225, 231, 245), 1));
        }
        return btn;
    }

    private void renderList(List<InvoiceData> list) {
        listPanel.removeAll();
        for (InvoiceData data : list) {
            listPanel.add(createListItem(data, data == list.get(0)));
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createListItem(InvoiceData data, boolean selected) {
        RoundedPanel item = new RoundedPanel(12, selected ? Color.WHITE : new Color(250, 252, 255), selected ? new Color(49, 106, 210) : new Color(230, 235, 245), selected ? 2f : 1f);
        item.setLayout(new MigLayout("insets 16 12 16 12", "[][grow,fill][][]", "[]"));

        // Icon part
        JLabel icon = new JLabel();
        if (data.status.equals("Đã thanh toán")) {
            icon.setIcon(loadIcon("check-circle.png", 20, 20));
        } else if (data.status.equals("Chưa thanh toán")) {
            icon.setIcon(loadIcon("alert-circle.png", 20, 20));
        } else {
            icon.setIcon(loadIcon("clock-circle.png", 20, 20)); // mock
        }

        // Info part
        JPanel info = new JPanel(new MigLayout("wrap 1,insets 0", "[]", "[][]"));
        info.setOpaque(false);
        JLabel idLabel = new JLabel(data.id);
        idLabel.setFont(idLabel.getFont().deriveFont(Font.BOLD, 14f));
        idLabel.setForeground(new Color(24, 40, 66));
        
        JLabel nameLabel = new JLabel(data.customer);
        nameLabel.setForeground(new Color(110, 125, 145));
        nameLabel.setFont(nameLabel.getFont().deriveFont(12f));
        info.add(idLabel);
        info.add(nameLabel);

        // Price part
        JPanel pricePane = new JPanel(new MigLayout("wrap 1,insets 0", "[]", "[][]"));
        pricePane.setOpaque(false);
        JLabel pLabel = new JLabel(data.price);
        pLabel.setFont(pLabel.getFont().deriveFont(Font.BOLD, 14f));
        pLabel.setForeground(new Color(24, 40, 66));
        pLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        JLabel sLabel = new JLabel(data.status);
        sLabel.setForeground(data.statusColor);
        sLabel.setFont(sLabel.getFont().deriveFont(11f));
        sLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        pricePane.add(pLabel, "alignx right");
        pricePane.add(sLabel, "alignx right");

        JLabel arrow = new JLabel(" \u203A ");
        arrow.setForeground(new Color(180, 190, 210));

        item.add(icon, "aligny center");
        item.add(info, "aligny center");
        item.add(pricePane, "aligny center");
        item.add(arrow, "aligny center");

        return item;
    }

    private void createRightSide() {
        detailPanel.setLayout(new MigLayout("wrap 1,insets 24 32 24 32, gap 20", "[grow,fill]", "[]"));
    }

    private void showDetail(InvoiceData data) {
        detailPanel.removeAll();

        // Top Action row
        JPanel topRow = new JPanel(new MigLayout("insets 0", "[][grow,fill][][][]", "[]"));
        topRow.setOpaque(false);

        JPanel idBox = new JPanel(new MigLayout("wrap 1,insets 0", "[]", "[]"));
        idBox.setOpaque(false);
        
        JPanel titleRow = new JPanel(new MigLayout("insets 0,gap 10", "[][]", "[]"));
        titleRow.setOpaque(false);
        JLabel lId = new JLabel(data.id);
        lId.setFont(lId.getFont().deriveFont(Font.BOLD, 22f));
        lId.setForeground(new Color(24, 40, 66));
        
        JLabel lStatus = new JLabel(" \u2022 " + data.status);
        lStatus.setForeground(data.statusColor);
        lStatus.setFont(lStatus.getFont().deriveFont(Font.BOLD, 12f));
        titleRow.add(lId, "aligny bottom");
        titleRow.add(lStatus, "aligny bottom");

        JLabel lDate = new JLabel("Ngày tạo: 01/04/2026");
        lDate.setForeground(new Color(110, 125, 145));
        idBox.add(titleRow);
        idBox.add(lDate);

        PrimaryButton bConfirm = new PrimaryButton("Xác nhận thanh toán");
        bConfirm.setBackground(ThemeColors.SUCCESS);
        bConfirm.setForeground(Color.WHITE);
        bConfirm.setIcon(loadIcon("check-circle.png", 16, 16));
        
        PrimaryButton bPdf = new PrimaryButton("Xuất PDF");
        bPdf.setBackground(Color.WHITE);
        bPdf.setForeground(new Color(80, 95, 115));
        bPdf.setBorder(BorderFactory.createLineBorder(new Color(225, 231, 245), 1));
        
        JButton bPrint = new JButton();
        bPrint.setIcon(loadIcon("print.png", 18, 18));
        bPrint.setBackground(Color.WHITE);
        bPrint.setBorder(BorderFactory.createLineBorder(new Color(225, 231, 245), 1));
        bPrint.setFocusPainted(false);
        bPrint.setPreferredSize(new Dimension(36, 36));

        topRow.add(idBox);
        topRow.add(new JPanel(){{setOpaque(false);}}, "growx"); // spacer
        topRow.add(bConfirm, "h 36!");
        topRow.add(bPdf, "h 36!");
        topRow.add(bPrint, "h 36!,w 36!");

        // Two boxes: KH, Phòng
        JPanel infoRow = new JPanel(new MigLayout("insets 0,gap 20", "[grow,fill][grow,fill]", "[]"));
        infoRow.setOpaque(false);
        infoRow.add(createBox("KHÁCH HÀNG", data.customer, "Mã KH: C003"));
        infoRow.add(createBox("THÔNG TIN PHÒNG", "Phòng 401 - Suite", "01/04/2026 - 05/04/2026 · 4 đêm"));

        // Table Title
        JLabel tTitle = new JLabel("CHI TIẾT DỊCH VỤ");
        tTitle.setForeground(new Color(130, 145, 165));
        tTitle.setFont(tTitle.getFont().deriveFont(Font.BOLD, 12f));

        // Table
        RoundedPanel tablePanel = new RoundedPanel(12, Color.WHITE, new Color(225, 231, 245), 1f);
        tablePanel.setLayout(new MigLayout("wrap 1,insets 0,gap 0", "[grow,fill]", "[]"));
        
        // Table header
        JPanel tHeader = new JPanel(new MigLayout("insets 16 20 16 20", "[grow,fill][100!][150!][150!]", "[]"));
        tHeader.setBackground(new Color(250, 252, 255));
        // Không set opaque = false cho tHeader vì muốn nó có màu Bg
        tHeader.add(makeTText("Dịch vụ", false));
        tHeader.add(makeTText("Số lượng", true));
        tHeader.add(makeTText("Đơn giá", true));
        tHeader.add(makeTText("Thành tiền", true));
        
        tablePanel.add(tHeader, "growx");
        
        // Rows
        tablePanel.add(createTRow("Tiền phòng Suite (x4 đêm)", "4", "5.500.000đ", "22.000.000đ"));
        tablePanel.add(createTRow("Bữa sáng buffet", "8", "220.000đ", "1.760.000đ"));
        tablePanel.add(createTRow("Spa & Massage", "2", "800.000đ", "1.600.000đ"));
        tablePanel.add(createTRow("Giặt ủi", "3", "50.000đ", "150.000đ"));

        // Footer in Table
        JPanel tFooter = new JPanel(new MigLayout("wrap 2,insets 20 20 20 20", "[grow,fill][200!]", "[]"));
        tFooter.setBackground(Color.WHITE);
        
        tFooter.add(makeTText("Tạm tính", false), "alignx left");
        tFooter.add(makeTText("25.510.000đ", true), "alignx right");
        
        tFooter.add(makeTText("Thuế VAT (10%)", false), "alignx left");
        tFooter.add(makeTText("2.551.000đ", true), "alignx right");
        
        JPanel divider = new JPanel(); divider.setBackground(new Color(230, 235, 245));
        tFooter.add(divider, "span 2, growx, h 1!, gapy 12 12");
        
        JLabel lTotal = new JLabel("Tổng cộng");
        lTotal.setFont(lTotal.getFont().deriveFont(Font.BOLD, 16f));
        lTotal.setForeground(new Color(24, 40, 66));
        
        JLabel valTotal = new JLabel(data.price);
        valTotal.setFont(valTotal.getFont().deriveFont(Font.BOLD, 20f));
        valTotal.setForeground(new Color(24, 40, 66));
        valTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        
        tFooter.add(lTotal, "alignx left");
        tFooter.add(valTotal, "alignx right");

        tablePanel.add(tFooter, "growx");

        // Generated Label
        JLabel lGen = new JLabel("KQL HOTEL - Hóa đơn được tạo bởi hệ thống quản lý tự động", SwingConstants.CENTER);
        lGen.setForeground(new Color(150, 165, 185));
        lGen.setFont(lGen.getFont().deriveFont(11f));

        detailPanel.add(topRow, "growx");
        detailPanel.add(infoRow, "growx");
        detailPanel.add(tTitle, "gapy 10 0");
        detailPanel.add(tablePanel, "growx");
        detailPanel.add(lGen, "growx, gapy 20 0");

        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private JPanel createBox(String title, String val1, String val2) {
        RoundedPanel p = new RoundedPanel(12, new Color(250, 252, 255), new Color(230, 235, 245), 1f);
        p.setLayout(new MigLayout("wrap 1,insets 16,gap 4", "[grow,fill]", "[]"));
        
        JLabel t = new JLabel(title);
        t.setForeground(new Color(130, 145, 165));
        t.setFont(t.getFont().deriveFont(Font.BOLD, 11f));
        
        JLabel v1 = new JLabel(val1);
        v1.setFont(v1.getFont().deriveFont(Font.BOLD, 14f));
        v1.setForeground(new Color(24, 40, 66));
        
        JLabel v2 = new JLabel(val2);
        v2.setForeground(new Color(110, 125, 145));
        
        p.add(t, "gapy 0 8");
        p.add(v1);
        p.add(v2);
        
        return p;
    }

    private JLabel makeTText(String t, boolean right) {
        JLabel l = new JLabel(t);
        l.setForeground(new Color(100, 115, 135));
        if (right) l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }

    private JPanel createTRow(String n, String q, String p, String t) {
        JPanel row = new JPanel(new MigLayout("insets 14 20 14 20", "[grow,fill][100!][150!][150!]", "[]"));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 245, 250)));
        
        JLabel ln = new JLabel(n); ln.setForeground(new Color(50, 65, 80));
        JLabel lq = new JLabel(q); lq.setForeground(new Color(50, 65, 80)); lq.setHorizontalAlignment(SwingConstants.RIGHT);
        JLabel lp = new JLabel(p); lp.setForeground(new Color(50, 65, 80)); lp.setHorizontalAlignment(SwingConstants.RIGHT);
        JLabel lt = new JLabel(t); lt.setForeground(new Color(24, 40, 66)); lt.setFont(lt.getFont().deriveFont(Font.BOLD)); lt.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(ln);
        row.add(lq);
        row.add(lp);
        row.add(lt);
        return row;
    }

    private ImageIcon loadIcon(String filename, int w, int h) {
        try {
            URL resource = getClass().getResource("/kqlhotel/resources/icons/" + filename);
            if (resource == null) {
                java.io.File file = new java.io.File("src/kqlhotel/resources/icons/" + filename);
                if (file.exists()) resource = file.toURI().toURL();
            }
            if (resource != null) {
                ImageIcon icon = new ImageIcon(resource);
                return new ImageIcon(icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static class InvoiceData {
        String id, customer, price, status;
        Color statusColor;
        InvoiceData(String id, String c, String p, String s, Color c2) {
            this.id = id; customer = c; price = p; status = s; statusColor = c2; 
        }
    }
}
