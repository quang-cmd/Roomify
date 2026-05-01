package kqlhotel.gui.tabs;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.awt.Image;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import kqlhotel.gui.components.PrimaryButton;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.gui.theme.ThemeColors;
import net.miginfocom.swing.MigLayout;

public class CancelRoomPanel extends JPanel {
    private static final Color PAGE_BG = new Color(245, 248, 252);
    
    // Status / State
    private final JPanel leftCardPanel = new JPanel(new CardLayout());
    private final JPanel rightCardPanel = new JPanel(new CardLayout());
    private JLabel step1;
    private JLabel step2;
    private JLabel arrow;

    private final JTextField txtMaDatPhong = new JTextField();
    private final JTextField txtTenKhach = new JTextField();
    private final JTextField txtSdt = new JTextField();
    private final JTextField txtNgayNhan = new JTextField();

    private String selectedRoomId = "DP001";
    private String selectedRoomType = "Phòng 201 - Deluxe";
    private String selectedFloor = "Tầng 2";
    private String selectedCusName = "Nguyễn Văn A";

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
//        JLabel subtitle = new JLabel("Tìm kiếm đặt phòng và thực hiện hủy theo chính sách hoàn/trừ cọc");
//        subtitle.setForeground(new Color(150, 165, 190));
        // titlePanel.add(title); // Bỏ title bị trùng
//        titlePanel.add(subtitle);

        // Stepper
        JPanel stepper = new JPanel(new MigLayout("insets 6 16,gap 10", "[][][]", "[]"));
        stepper.setOpaque(false);
        stepper.setBackground(Color.WHITE);
        stepper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 235, 245), 1),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        
        step1 = new JLabel("1   Tìm đặt phòng");
        step1.setFont(step1.getFont().deriveFont(Font.BOLD, 12f));
        step1.setForeground(new Color(24, 40, 66));
        
        arrow = new JLabel(" > ");
        arrow.setForeground(new Color(200, 210, 230));

        step2 = new JLabel("2   Xác nhận hủy");
        step2.setFont(step2.getFont().deriveFont(12f));
        step2.setForeground(new Color(150, 165, 190));

        stepper.add(step1);
        stepper.add(arrow);
        stepper.add(step2);

        header.add(titlePanel);
        header.add(stepper, "alignx right");

        // ===== 2. Body Area (Split 2 Columns) =====
        JPanel body = new JPanel(new MigLayout("insets 20 0,gap 20", "[310!][grow,fill]", "[grow,fill]"));
        body.setOpaque(false);

        leftCardPanel.setOpaque(false);
        rightCardPanel.setOpaque(false);

        leftCardPanel.add(createFormCard(), "SEARCH");
        leftCardPanel.add(createConfirmFormCard(), "CONFIRM");

        updateSearchResults();
        body.add(leftCardPanel, "aligny top");
        body.add(rightCardPanel, "aligny top, grow");

        // ===== Assemble =====
        add(header);
        add(body, "grow");
        
        setState("RESULT");
    }

    private void updateSearchResults() {
        rightCardPanel.removeAll();
        rightCardPanel.add(createEmptyResultCard(), "EMPTY");
        rightCardPanel.add(createSearchResultCard(false), "RESULT");
        rightCardPanel.add(createSearchResultCard(true), "CONFIRMING");
        rightCardPanel.revalidate();
        rightCardPanel.repaint();
    }

    private void setState(String state) {
        if (state.equals("SEARCH")) {
            ((CardLayout)leftCardPanel.getLayout()).show(leftCardPanel, "SEARCH");
            ((CardLayout)rightCardPanel.getLayout()).show(rightCardPanel, "EMPTY");
            step1.setForeground(new Color(24, 40, 66));
            step1.setFont(step1.getFont().deriveFont(Font.BOLD));
            step2.setForeground(new Color(150, 165, 190));
            step2.setFont(step2.getFont().deriveFont(Font.PLAIN));
            arrow.setForeground(new Color(200, 210, 230));
        } else if (state.equals("RESULT")) {
            ((CardLayout)leftCardPanel.getLayout()).show(leftCardPanel, "SEARCH");
            ((CardLayout)rightCardPanel.getLayout()).show(rightCardPanel, "RESULT");
            step1.setForeground(new Color(24, 40, 66));
            step1.setFont(step1.getFont().deriveFont(Font.BOLD));
            step2.setForeground(new Color(150, 165, 190));
            step2.setFont(step2.getFont().deriveFont(Font.PLAIN));
            arrow.setForeground(new Color(200, 210, 230));
        } else if (state.equals("CONFIRM")) {
            // Before showing confirm, rebuild the left form with selected room details
            leftCardPanel.add(createConfirmFormCard(), "CONFIRM");
            ((CardLayout)leftCardPanel.getLayout()).show(leftCardPanel, "CONFIRM");
            
            // Rebuild right side confirming state
            rightCardPanel.add(createSearchResultCard(true), "CONFIRMING");
            ((CardLayout)rightCardPanel.getLayout()).show(rightCardPanel, "CONFIRMING");
            
            step1.setForeground(new Color(150, 165, 190));
            step1.setFont(step1.getFont().deriveFont(Font.PLAIN));
            step2.setForeground(new Color(220, 50, 60));
            step2.setFont(step2.getFont().deriveFont(Font.BOLD));
            arrow.setForeground(new Color(150, 165, 190));
        }
    }

    private RoundedPanel createFormCard() {
        RoundedPanel card = new RoundedPanel(16, Color.WHITE, new Color(225, 231, 245), 1f);
        card.setLayout(new MigLayout("wrap 1,insets 24,gap 12", "[grow,fill]", "[]"));

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
        JLabel sIco = new JLabel("", SwingConstants.CENTER);
        java.net.URL searchURL = getClass().getResource("/kqlhotel/resources/icons/search.png");
        if (searchURL != null) {
            ImageIcon rawIcon = new ImageIcon(searchURL);
            Image scaled = rawIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            sIco.setIcon(new ImageIcon(scaled));
        }
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

        card.add(createLabel("Mã đặt phòng"));
        txtMaDatPhong.putClientProperty("JTextField.placeholderText", "Ví dụ: DP001");
        card.add(createFieldEnclosure("", txtMaDatPhong), "h 42!");

        card.add(createLabel("Tên khách hàng"));
        txtTenKhach.putClientProperty("JTextField.placeholderText", "Nhập tên khách");
        card.add(createFieldEnclosure("", txtTenKhach), "h 42!");

        card.add(createLabel("Số điện thoại"));
        txtSdt.putClientProperty("JTextField.placeholderText", "0912 345 678");
        card.add(createFieldEnclosure("", txtSdt), "h 42!");

        card.add(createLabel("Ngày nhận phòng"));
        txtNgayNhan.putClientProperty("JTextField.placeholderText", "dd/mm/yyyy");
        card.add(createFieldEnclosure("", txtNgayNhan), "h 42!");

        PrimaryButton btnSearch = new PrimaryButton("Tìm đặt phòng");
        btnSearch.setBackground(new Color(17, 24, 39));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.addActionListener(e -> {
            updateSearchResults();
            setState("RESULT");
        });

        JButton btnReset = new JButton("Làm mới");
        btnReset.setFont(btnReset.getFont().deriveFont(Font.BOLD, 12f));
        btnReset.setForeground(new Color(100, 115, 140));
        btnReset.setBackground(new Color(245, 248, 252));
        btnReset.setBorder(BorderFactory.createLineBorder(new Color(220, 230, 245), 1));
        btnReset.setFocusPainted(false);
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.addActionListener(e -> {
            txtMaDatPhong.setText("");
            txtTenKhach.setText("");
            txtSdt.setText("");
            txtNgayNhan.setText("");
            updateSearchResults();
            setState("RESULT");
        });

        JPanel btnRow = new JPanel(new MigLayout("insets 0, gap 10", "[grow,fill][grow,fill]", "[]"));
        btnRow.setOpaque(false);
        btnRow.add(btnReset, "h 42!");
        btnRow.add(btnSearch, "h 42!");

        card.add(btnRow, "gapy 16 0");

        return card;
    }

    private JPanel createConfirmFormCard() {
        JPanel wrap = new JPanel(new MigLayout("insets 0, wrap 1, gap 8", "[grow,fill]", "top"));
        wrap.setOpaque(false);

        JButton btnBack = new JButton("← Quay lại danh sách tìm kiếm");
        btnBack.setFont(btnBack.getFont().deriveFont(12f));
        btnBack.setForeground(new Color(100, 115, 140));
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setMargin(new java.awt.Insets(0,0,0,0));
        btnBack.setHorizontalAlignment(SwingConstants.LEFT);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> setState("RESULT"));
        wrap.add(btnBack, "gapy 0 4");

        JLabel title = new JLabel("Xác nhận hủy phòng");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setForeground(new Color(220, 50, 60));
        wrap.add(title);

        RoundedPanel blueBox = new RoundedPanel(10, new Color(235, 245, 255), new Color(180, 200, 240), 1);
        blueBox.setLayout(new MigLayout("insets 10, gap 10", "[][grow]", "[]"));
        JLabel blueIcon = new JLabel();
        blueIcon.setFont(blueIcon.getFont().deriveFont(18f));
        blueIcon.setForeground(ThemeColors.PRIMARY);
        JPanel blueText = new JPanel(new MigLayout("insets 0, wrap 1", "[]", "[]"));
        blueText.setOpaque(false);
        JLabel dpRoom = new JLabel(selectedRoomId + " - " + selectedRoomType);
        dpRoom.setFont(dpRoom.getFont().deriveFont(Font.BOLD, 13f));
        JLabel dpCust = new JLabel(selectedCusName + " - 10/04/2026 14:00");
        dpCust.setForeground(new Color(100, 115, 140));
        blueText.add(dpRoom); blueText.add(dpCust);
        blueBox.add(blueIcon); blueBox.add(blueText);
        wrap.add(blueBox);

        wrap.add(createLabel("Thời điểm yêu cầu hủy *"));
        JTextField txtTime = new JTextField("11/04/2026 10:06 CH");
        wrap.add(createFieldEnclosure("", txtTime), "h 36!");

        wrap.add(createLabel("Lý do hủy (tùy chọn)"));
        JTextArea txtReason = new JTextArea(2, 20);
        txtReason.setLineWrap(true);
        txtReason.setWrapStyleWord(true);
        txtReason.setForeground(new Color(30, 50, 80));
        
        RoundedPanel wrapReason = new RoundedPanel(8, Color.WHITE, new Color(220, 230, 245), 1);
        wrapReason.setLayout(new BorderLayout());
        wrapReason.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        txtReason.setBorder(BorderFactory.createEmptyBorder());
        wrapReason.add(txtReason, BorderLayout.CENTER);
        wrap.add(wrapReason, "h 50!");

        RoundedPanel policyBox = new RoundedPanel(10, new Color(255, 245, 245), new Color(250, 200, 200), 1);
        policyBox.setLayout(new MigLayout("insets 10, wrap 1, gap 6", "[grow,fill]", "[]"));
        JLabel polTitle = new JLabel("Chính sách áp dụng");
        polTitle.setFont(polTitle.getFont().deriveFont(Font.BOLD, 13f));
        polTitle.setForeground(new Color(220, 50, 60));
        JLabel polSub = new JLabel("Đã quá giờ nhận phòng: không hoàn lại tiền cọc");
        polSub.setForeground(new Color(220, 50, 60));
        
        JPanel polGrid = new JPanel(new MigLayout("insets 0, gap 6", "[grow,fill][grow,fill]", "[]4[]"));
        polGrid.setOpaque(false);
        polGrid.add(createMoneyBox("Tiền cọc đã nhận", "500.000đ"));
        polGrid.add(createMoneyBox("Tiền bị trừ", "500.000đ"), "wrap");
        polGrid.add(createMoneyBox("Tiền hoàn lại", "0đ"));
        polGrid.add(createMoneyBox("Trạng thái phòng sau hủy", "Trống"));
        
        JLabel polEnd = new JLabel("Đã quá giờ nhận phòng - hệ thống không hoàn lại tiền theo chính sách.");
        polEnd.setForeground(new Color(220, 100, 100));
        polEnd.setFont(polEnd.getFont().deriveFont(10f));

        policyBox.add(polTitle);
        policyBox.add(polSub);
        policyBox.add(polGrid, "gapy 4 4");
        policyBox.add(polEnd);
        wrap.add(policyBox);

        PrimaryButton btnConfirm = new PrimaryButton("Xác nhận hủy phòng");
        btnConfirm.setBackground(new Color(220, 50, 60));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Hủy phòng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            setState("SEARCH");
        });
        wrap.add(btnConfirm, "h 40!, gapy 4 0");

        return wrap;
    }

    private JPanel createSearchResultCard(boolean isConfirming) {
        JPanel wrap = new JPanel(new MigLayout("wrap 1,insets 0 0 0 0", "[fill]", "[][][grow,fill]"));
        wrap.setOpaque(false);
        
        JPanel titleRow = new JPanel(new MigLayout("insets 0", "[]", "[]"));
        titleRow.setOpaque(false);
        JLabel title = new JLabel("Danh sách đặt phòng phù hợp");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        title.setForeground(new Color(24, 40, 66));
        
        JPanel badge1 = new RoundedPanel(16, ThemeColors.PRIMARY, null, 0);
        badge1.setLayout(new BorderLayout());
        JLabel l1 = new JLabel("0", SwingConstants.CENTER); // Will update dynamically
        l1.setForeground(Color.WHITE); l1.setFont(l1.getFont().deriveFont(Font.BOLD, 10f));
        badge1.add(l1); badge1.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        titleRow.add(title); titleRow.add(badge1);
        
        JLabel sub = new JLabel("Chọn một đặt phòng để xem chi tiết và thực hiện hủy");
        sub.setForeground(new Color(130, 145, 170));
        sub.setFont(sub.getFont().deriveFont(12f));

        wrap.add(titleRow, "gapy 0 4");
        wrap.add(sub, "gapy 0 16");

        // Scrollable panel: ép chiều rộng theo viewport (không bể), cuộn dọc khi nhiều phòng
        JPanel listPnl = new ScrollablePanel(new MigLayout("insets 0, wrap 2, gap 16", "[grow,fill][grow,fill]", "[]"));
        listPnl.setOpaque(false);

        int count = 0;

        if (isConfirming) {
            listPnl.add(createSingleRoomCard(selectedRoomId, selectedRoomType, selectedFloor, selectedCusName, isConfirming));
            count = 1;
        } else {
            if (checkMatch("DP001", "Nguyễn Văn A", "0912345678")) {
                listPnl.add(createSingleRoomCard("DP001", "Phòng 201 - Deluxe", "Tầng 2", "Nguyễn Văn A", isConfirming));
                count++;
            }
            if (checkMatch("DP002", "Trần Thị B", "0987654321")) {
                listPnl.add(createSingleRoomCard("DP002", "Phòng 305 - Suite", "Tầng 3", "Trần Thị B", isConfirming));
                count++;
            }
            if (checkMatch("DP003", "Lê Văn C", "0123456789")) {
                listPnl.add(createSingleRoomCard("DP003", "Phòng 502 - Standard", "Tầng 5", "Lê Văn C", isConfirming));
                count++;
            }
            if (checkMatch("DP004", "Phạm Văn D", "0999888777")) {
                listPnl.add(createSingleRoomCard("DP004", "Phòng 101 - Basic", "Tầng 1", "Phạm Văn D", isConfirming));
                count++;
            }
        }
        
        l1.setText(String.valueOf(count));

        JScrollPane sp = new JScrollPane(listPnl);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        wrap.add(sp, "grow");
        
        return wrap;
    }

    private boolean checkMatch(String code, String name, String phone) {
        String m = txtMaDatPhong.getText().trim().toLowerCase();
        String t = txtTenKhach.getText().trim().toLowerCase();
        String s = txtSdt.getText().trim().toLowerCase();
        
        if (m.isEmpty() && t.isEmpty() && s.isEmpty()) return true;
        
        boolean match = true;
        if (!m.isEmpty() && !code.toLowerCase().contains(m) && !code.toLowerCase().contains(m.replace("dp", ""))) match = false;
        if (!t.isEmpty() && !name.toLowerCase().contains(t)) match = false;
        if (!s.isEmpty() && !phone.contains(s)) match = false;
        return match;
    }

    private JPanel createSingleRoomCard(String id, String type, String floor, String cusName, boolean isConfirming) {
        RoundedPanel card = new RoundedPanel(16, Color.WHITE, new Color(180, 200, 240), 1.5f);
        card.setLayout(new MigLayout("insets 16 20, wrap 1, gap 8", "[grow,fill]", "[]"));
        
        if (!isConfirming) {
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedRoomId = id;
                    selectedRoomType = type;
                    selectedFloor = floor;
                    selectedCusName = cusName;
                    setState("CONFIRM");
                }
            });
        }

        JPanel hRow = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        hRow.setOpaque(false);
        JLabel dpId = new JLabel(id);
        dpId.setFont(dpId.getFont().deriveFont(Font.BOLD, 16f));
        dpId.setForeground(new Color(24, 40, 66));
        JPanel bedgeT2 = new RoundedPanel(16, new Color(230, 240, 255), null, 0);
        bedgeT2.setBorder(BorderFactory.createEmptyBorder(2,8,2,8));
        bedgeT2.setLayout(new BorderLayout());
        JLabel t2 = new JLabel(floor); t2.setForeground(ThemeColors.PRIMARY); t2.setFont(t2.getFont().deriveFont(Font.BOLD, 11f));
        bedgeT2.add(t2);
        hRow.add(dpId); hRow.add(bedgeT2);

        JLabel rType = new JLabel(type);
        rType.setFont(rType.getFont().deriveFont(12f));
        rType.setForeground(new Color(130, 145, 170));

        card.add(hRow);
        card.add(rType);
        
        RoundedPanel infoBox = new RoundedPanel(8, new Color(248, 250, 253), new Color(230, 235, 245), 1);
        infoBox.setLayout(new MigLayout("insets 12, wrap 1, gap 4", "[]", "[]"));
        infoBox.add(createIconText("", cusName, true));
        infoBox.add(createIconText("", "0912345678", false));
        infoBox.add(createIconText("", "10/04/2026 14:00", false));
        infoBox.add(createIconText("", "2 khách", false));
        card.add(infoBox, "gapy 8 8");

        JPanel moneyRow = new JPanel(new MigLayout("insets 0, gap 12", "[grow,fill][grow,fill]", "[]"));
        moneyRow.setOpaque(false);
        moneyRow.add(createMoneyBox("Tiền cọc", "500.000đ"));
        moneyRow.add(createMoneyBox("Đã thanh toán", "500.000đ"));
        card.add(moneyRow);

        JPanel botRow = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        botRow.setOpaque(false);
        JLabel stat = new JLabel("<html>Trạng thái<br><b>Chờ nhận phòng</b></html>");
        stat.setForeground(new Color(100, 115, 140));
        botRow.add(stat, "aligny center");

        if (isConfirming) {
            JPanel btnConfirming = new RoundedPanel(20, new Color(255, 235, 235), null, 0);
            btnConfirming.setLayout(new BorderLayout());
            btnConfirming.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
            JLabel lblConfirming = new JLabel("Đã chọn", SwingConstants.CENTER);
            lblConfirming.setFont(lblConfirming.getFont().deriveFont(Font.BOLD, 12f));
            lblConfirming.setForeground(new Color(220, 50, 60));
            btnConfirming.add(lblConfirming);
            botRow.add(btnConfirming, "aligny center, alignx right");
        } else {
            JButton btnCancel = new JButton("Hủy phòng này");
            btnCancel.setFont(btnCancel.getFont().deriveFont(Font.BOLD, 12f));
            btnCancel.setForeground(new Color(220, 50, 60));
            btnCancel.setBackground(new Color(255, 235, 235));
            btnCancel.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
            btnCancel.setFocusPainted(false);
            btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnCancel.addActionListener(e -> {
                selectedRoomId = id;
                selectedRoomType = type;
                selectedFloor = floor;
                selectedCusName = cusName;
                setState("CONFIRM");
            });
            botRow.add(btnCancel, "aligny center, alignx right");
        }

        card.add(botRow, "gapy 8 0");
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

    private JPanel createIconText(String icon, String text, boolean bold) {
        JPanel p = new JPanel(new MigLayout("insets 0, gap 8", "[][]", "[]"));
        p.setOpaque(false);
        JLabel lIcon = new JLabel(icon);
        lIcon.setForeground(new Color(150, 165, 190));
        JLabel lText = new JLabel(text);
        lText.setForeground(new Color(50, 70, 100));
        if (bold) lText.setFont(lText.getFont().deriveFont(Font.BOLD));
        p.add(lIcon); p.add(lText);
        return p;
    }

    private JPanel createMoneyBox(String title, String val) {
        RoundedPanel p = new RoundedPanel(8, Color.WHITE, new Color(230, 235, 245), 1);
        p.setLayout(new MigLayout("insets 8, wrap 1, gap 2", "[]", "[]"));
        JLabel t = new JLabel(title);
        t.setFont(t.getFont().deriveFont(10f));
        t.setForeground(new Color(130, 145, 170));
        JLabel v = new JLabel(val);
        v.setFont(v.getFont().deriveFont(Font.BOLD, 13f));
        v.setForeground(new Color(24, 40, 66));
        p.add(t); p.add(v);
        return p;
    }

    private JPanel createEmptyResultCard() {
        JPanel wrap = new JPanel(new MigLayout("wrap 1,insets 40 20", "[fill]", "[]"));
        wrap.setOpaque(false);

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

        JPanel guides = new JPanel(new MigLayout("wrap 1,insets 0,gap 10", "[400!]", "[]"));
        guides.setOpaque(false);
        guides.add(createGuideItem("", "Tìm đặt phòng theo mã hoặc thông tin khách"));
        guides.add(createGuideItem("", "Chọn đúng đặt phòng cần hủy"));
        guides.add(createGuideItem("", "Nhập thời điểm yêu cầu hủy"));
        guides.add(createGuideItem("", "Hệ thống tính hoàn/trả cọc tự động"));

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
    /**
     * JPanel that implements Scrollable to track viewport width (no horizontal overflow)
     * while allowing vertical scrolling when content exceeds the visible area.
     */
    private static class ScrollablePanel extends JPanel implements Scrollable {
        public ScrollablePanel(java.awt.LayoutManager layout) {
            super(layout);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 64;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true; // Ép chiều rộng theo viewport → không bể layout
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false; // Cho phép cuộn dọc khi nội dung dài hơn viewport
        }
    }
}
