package kqlhotel.gui.tabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import kqlhotel.gui.components.PrimaryButton;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.gui.theme.ThemeColors;
import net.miginfocom.swing.MigLayout;

public class CancelRoomPanel extends JPanel {
    private static final Color PAGE_BG = new Color(245, 248, 252);
    private final JTextField txtMaDatPhong = new JTextField();
    private final JTextField txtTenKhach = new JTextField();
    private final JTextField txtSdt = new JTextField();
    private final JTextField txtNgayNhan = new JTextField();

    public CancelRoomPanel() {
        setOpaque(false);
        setBackground(PAGE_BG);
        setLayout(new MigLayout("insets 24,gap 20,wrap 1", "[grow,fill]", "[][grow,fill]"));

        // ===== 1. Header =====
        JPanel header = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        header.setOpaque(false);

        JPanel titlePanel = new JPanel(new MigLayout("insets 0,wrap 1,gap 2", "[]", "[]"));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("Hủy phòng");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        title.setForeground(new Color(24, 40, 66));
        JLabel subtitle = new JLabel("Tìm kiếm đặt phòng và thực hiện hủy theo chính sách hoàn/trả cọc");
        subtitle.setForeground(new Color(150, 165, 190));
        titlePanel.add(title);
        titlePanel.add(subtitle);

        // Stepper
        JPanel stepper = new JPanel(new MigLayout("insets 6 16,gap 10", "[][][]", "[]"));
        stepper.setOpaque(false);
        stepper.setBackground(Color.WHITE);
        stepper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 235, 245), 1),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        
        JLabel step1 = new JLabel("1   Tìm đặt phòng");
        step1.setFont(step1.getFont().deriveFont(Font.BOLD, 12f));
        step1.setForeground(new Color(24, 40, 66));
        
        JLabel arrow = new JLabel(" > ");
        arrow.setForeground(new Color(200, 210, 230));

        JLabel step2 = new JLabel("2   Xác nhận hủy");
        step2.setFont(step2.getFont().deriveFont(12f));
        step2.setForeground(new Color(150, 165, 190));

        stepper.add(step1);
        stepper.add(arrow);
        stepper.add(step2);

        header.add(titlePanel);
        header.add(stepper, "alignx right");

        // ===== 2. Body Area (Split 2 Columns) =====
        JPanel body = new JPanel(new MigLayout("insets 20 0,gap 40", "[450!][grow,fill]", "[grow,fill]"));
        body.setOpaque(false);

        RoundedPanel formCard = createFormCard();
        JPanel emptyResultCard = createEmptyResultCard();

        body.add(formCard, "aligny top");
        body.add(emptyResultCard, "aligny top");

        // ===== Assemble =====
        add(header);
        add(body, "grow");
    }

    private RoundedPanel createFormCard() {
        RoundedPanel card = new RoundedPanel(16, Color.WHITE, new Color(225, 231, 245), 1f);
        card.setLayout(new MigLayout("wrap 1,insets 24,gap 12", "[grow,fill]", "[]"));

        // Header Form
        JPanel hForm = new JPanel(new MigLayout("insets 0,gap 10", "[][]", "[]"));
        hForm.setOpaque(false);
        
        JPanel iconSearch = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 235, 235));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
            }
        };
        iconSearch.setOpaque(false);
        JLabel sIco = new JLabel("🔍", SwingConstants.CENTER);
        sIco.setForeground(new Color(220, 53, 69));
        iconSearch.add(sIco);

        JPanel ht = new JPanel(new MigLayout("insets 0,wrap 1,gap 2", "[]", "[]"));
        ht.setOpaque(false);
        JLabel fTitle = new JLabel("Tìm đặt phòng cần hủy");
        fTitle.setFont(fTitle.getFont().deriveFont(Font.BOLD, 16f));
        fTitle.setForeground(new Color(24, 40, 66));
        JLabel fSub = new JLabel("Nhập mã đặt phòng hoặc thông tin khách để tra cứu");
        fSub.setFont(fSub.getFont().deriveFont(11f));
        fSub.setForeground(new Color(150, 165, 190));
        ht.add(fTitle);
        ht.add(fSub);

        hForm.add(iconSearch, "w 36!,h 36!");
        hForm.add(ht);

        card.add(hForm, "gapy 0 16");

        // Form Fields
        card.add(createLabel("Mã đặt phòng"));
        txtMaDatPhong.putClientProperty("JTextField.placeholderText", "Ví dụ: DP001");
        card.add(createFieldEnclosure("🏷", txtMaDatPhong), "h 42!");

        card.add(createLabel("Tên khách hàng"));
        txtTenKhach.putClientProperty("JTextField.placeholderText", "Nhập tên khách");
        card.add(createFieldEnclosure("👤", txtTenKhach), "h 42!");

        card.add(createLabel("Số điện thoại"));
        txtSdt.putClientProperty("JTextField.placeholderText", "0912 345 678");
        card.add(createFieldEnclosure("📞", txtSdt), "h 42!");

        card.add(createLabel("Ngày nhận phòng"));
        txtNgayNhan.putClientProperty("JTextField.placeholderText", "dd/mm/yyyy");
        card.add(createFieldEnclosure("📅", txtNgayNhan), "h 42!");

        PrimaryButton btnSearch = new PrimaryButton("🔍 Tìm đặt phòng");
        btnSearch.setBackground(new Color(17, 24, 39));
        btnSearch.setForeground(Color.WHITE);
        card.add(btnSearch, "h 44!,gapy 16 0");

        return card;
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(80, 100, 130));
        lbl.setFont(lbl.getFont().deriveFont(12f));
        return lbl;
    }

    private JPanel createFieldEnclosure(String icon, JTextField field) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(Color.WHITE);
        wrap.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 230, 245), 1),
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));

        JLabel ico = new JLabel(icon + "  ");
        ico.setForeground(new Color(180, 195, 215));
        
        field.setBorder(BorderFactory.createEmptyBorder());
        field.setOpaque(false);
        field.setForeground(new Color(30, 50, 80));

        wrap.add(ico, BorderLayout.WEST);
        wrap.add(field, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel createEmptyResultCard() {
        JPanel wrap = new JPanel(new MigLayout("wrap 1,insets 40 20", "[fill]", "[]"));
        wrap.setOpaque(false);

        // Illustration Center
        JPanel illust = new JPanel(new MigLayout("wrap 1,gap 10", "[center]", "[]"));
        illust.setOpaque(false);
        
        JPanel bigIcon = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 235, 235));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        bigIcon.setOpaque(false);
        JLabel xMark = new JLabel("✕", SwingConstants.CENTER);
        xMark.setForeground(new Color(220, 53, 69));
        xMark.setFont(xMark.getFont().deriveFont(Font.BOLD, 24f));
        bigIcon.add(xMark);

        JLabel resTitle = new JLabel("Chưa có kết quả tra cứu");
        resTitle.setFont(resTitle.getFont().deriveFont(Font.BOLD, 18f));
        resTitle.setForeground(new Color(24, 40, 66));

        JLabel resSub = new JLabel("<html><center>Nhập mã đặt phòng, tên khách, số điện thoại<br>hoặc ngày nhận phòng rồi nhấn <b>Tìm đặt phòng</b></center></html>", SwingConstants.CENTER);
        resSub.setForeground(new Color(130, 145, 170));

        illust.add(bigIcon, "w 64!,h 64!,gapy 0 10");
        illust.add(resTitle);
        illust.add(resSub);

        // List Guide Below
        JPanel guides = new JPanel(new MigLayout("wrap 1,insets 0,gap 10", "[400!]", "[]"));
        guides.setOpaque(false);
        guides.add(createGuideItem("🔍", "Tìm đặt phòng theo mã hoặc thông tin khách"));
        guides.add(createGuideItem("📋", "Chọn đúng đặt phòng cần hủy"));
        guides.add(createGuideItem("⏱", "Nhập thời điểm yêu cầu hủy"));
        guides.add(createGuideItem("🏢", "Hệ thống tính hoàn/trả cọc tự động"));

        wrap.add(illust, "alignx center,gapy 0 40");
        wrap.add(guides, "alignx center");

        return wrap;
    }

    private RoundedPanel createGuideItem(String icon, String text) {
        RoundedPanel p = new RoundedPanel(12, Color.WHITE, new Color(240, 244, 250), 1f);
        p.setLayout(new MigLayout("insets 12 16", "[][]", "[]"));
        JLabel ico = new JLabel(icon);
        JLabel txt = new JLabel(text);
        txt.setForeground(new Color(120, 135, 160));
        p.add(ico);
        p.add(txt);
        return p;
    }
}
