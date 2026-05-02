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
import javax.swing.border.EmptyBorder;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.gui.theme.ThemeColors;
import net.miginfocom.swing.MigLayout;

public class CancelRoomPanel extends JPanel {
    private static final Color PAGE_BG = new Color(245, 248, 252);
    
    private final JPanel leftCardPanel = new JPanel(new CardLayout());
    private final JPanel rightCardPanel = new JPanel(new CardLayout());
    private JLabel step1;
    private JLabel step2;
    private JLabel arrow;

    private final JTextField txtMaDatPhong = new JTextField();
    private final JTextField txtTenKhach = new JTextField();
    private final JTextField txtSdt = new JTextField();
    private final JTextField txtNgayNhan = new JTextField();

    private String selectedRoomId = "BK001";
    private String selectedRoomType = "Room 201 - Deluxe";
    private String selectedFloor = "Floor 2";
    private String selectedCusName = "John Doe";

    public CancelRoomPanel() {
        setOpaque(false);
        setBackground(PAGE_BG);
        setLayout(new MigLayout("insets 24,gap 20,wrap 1", "[grow,fill]", "[][grow,fill]"));

        // ===== 1. Header =====
        JPanel header = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        header.setOpaque(false);

        JPanel titlePanel = new JPanel(new MigLayout("insets 0,wrap 1,gap 2", "[]", "[]"));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("Hủy đặt phòng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(15, 23, 42));
        titlePanel.add(title);

        // Stepper
        JPanel stepper = new JPanel(new MigLayout("insets 6 16,gap 10", "[][][]", "[]"));
        stepper.setOpaque(false);
        stepper.setBackground(Color.WHITE);
        stepper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 235, 245), 1),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        
        step1 = new JLabel("1   Tìm đặt phòng");
        step1.setFont(new Font("Segoe UI", Font.BOLD, 12));
        step1.setForeground(new Color(15, 23, 42));
        
        arrow = new JLabel(" > ");
        arrow.setForeground(new Color(200, 210, 230));

        step2 = new JLabel("2   Xác nhận hủy");
        step2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        step2.setForeground(new Color(100, 116, 139));

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
            step1.setForeground(new Color(15, 23, 42));
            step1.setFont(step1.getFont().deriveFont(Font.BOLD));
            step2.setForeground(new Color(100, 116, 139));
            step2.setFont(step2.getFont().deriveFont(Font.PLAIN));
            arrow.setForeground(new Color(200, 210, 230));
        } else if (state.equals("RESULT")) {
            ((CardLayout)leftCardPanel.getLayout()).show(leftCardPanel, "SEARCH");
            ((CardLayout)rightCardPanel.getLayout()).show(rightCardPanel, "RESULT");
            step1.setForeground(new Color(15, 23, 42));
            step1.setFont(step1.getFont().deriveFont(Font.BOLD));
            step2.setForeground(new Color(100, 116, 139));
            step2.setFont(step2.getFont().deriveFont(Font.PLAIN));
            arrow.setForeground(new Color(200, 210, 230));
        } else if (state.equals("CONFIRM")) {
            leftCardPanel.add(createConfirmFormCard(), "CONFIRM");
            ((CardLayout)leftCardPanel.getLayout()).show(leftCardPanel, "CONFIRM");
            rightCardPanel.add(createSearchResultCard(true), "CONFIRMING");
            ((CardLayout)rightCardPanel.getLayout()).show(rightCardPanel, "CONFIRMING");
            
            step1.setForeground(new Color(100, 116, 139));
            step1.setFont(step1.getFont().deriveFont(Font.PLAIN));
            step2.setForeground(new Color(220, 38, 38));
            step2.setFont(step2.getFont().deriveFont(Font.BOLD));
            arrow.setForeground(new Color(100, 116, 139));
        }
    }

    private RoundedPanel createFormCard() {
        RoundedPanel card = new RoundedPanel(16, Color.WHITE, new Color(226, 232, 240), 1f);
        card.setLayout(new MigLayout("wrap 1,insets 24,gap 12", "[grow,fill]", "[]"));

        JPanel hForm = new JPanel(new MigLayout("insets 0,gap 10", "[grow]", "[]"));
        hForm.setOpaque(false);
        
        JLabel fTitle = new JLabel("Tìm đặt phòng cần hủy");
        fTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        fTitle.setForeground(new Color(15, 23, 42));
        JLabel fSub = new JLabel("Nhập mã đặt phòng hoặc thông tin khách để tìm kiếm.");
        fSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fSub.setForeground(new Color(100, 116, 139));
        hForm.add(fTitle, "wrap");
        hForm.add(fSub);

        card.add(hForm, "gapy 0 16");

        card.add(createLabel("Mã đặt phòng"));
        txtMaDatPhong.putClientProperty("JTextField.placeholderText", "VD: BK001");
        card.add(createFieldEnclosure("", txtMaDatPhong), "h 42!");

        card.add(createLabel("Tên khách"));
        txtTenKhach.putClientProperty("JTextField.placeholderText", "VD: Nguyễn Văn A");
        card.add(createFieldEnclosure("", txtTenKhach), "h 42!");

        card.add(createLabel("Số điện thoại"));
        txtSdt.putClientProperty("JTextField.placeholderText", "VD: 0987654321");
        card.add(createFieldEnclosure("", txtSdt), "h 42!");

        card.add(createLabel("Ngày nhận phòng"));
        txtNgayNhan.putClientProperty("JTextField.placeholderText", "dd/mm/yyyy");
        card.add(createFieldEnclosure("", txtNgayNhan), "h 42!");

        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.setBackground(new Color(15, 23, 42));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(e -> {
            updateSearchResults();
            setState("RESULT");
        });

        JButton btnReset = new JButton("Làm mới");
        btnReset.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnReset.setForeground(new Color(100, 116, 139));
        btnReset.setBackground(new Color(248, 250, 252));
        btnReset.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
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

        JButton btnBack = new JButton("← Quay lại kết quả");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnBack.setForeground(new Color(100, 116, 139));
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setMargin(new java.awt.Insets(0,0,0,0));
        btnBack.setHorizontalAlignment(SwingConstants.LEFT);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> setState("RESULT"));
        wrap.add(btnBack, "gapy 0 4");

        JLabel title = new JLabel("Xác nhận hủy đặt phòng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(220, 38, 38));
        wrap.add(title);

        RoundedPanel blueBox = new RoundedPanel(10, new Color(239, 246, 255), new Color(191, 219, 254), 1);
        blueBox.setLayout(new MigLayout("insets 10, gap 10", "[][grow]", "[]"));
        JPanel blueText = new JPanel(new MigLayout("insets 0, wrap 1", "[]", "[]"));
        blueText.setOpaque(false);
        JLabel dpRoom = new JLabel(selectedRoomId + " - " + selectedRoomType);
        dpRoom.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel dpCust = new JLabel(selectedCusName + " - 10/04/2026 14:00");
        dpCust.setForeground(new Color(100, 116, 139));
        blueText.add(dpRoom); blueText.add(dpCust);
        blueBox.add(new JLabel(""), "w 20!"); blueBox.add(blueText);
        wrap.add(blueBox);

        wrap.add(createLabel("Thời gian yêu cầu hủy *"));
        JTextField txtTime = new JTextField("11/04/2026 10:06 SA");
        wrap.add(createFieldEnclosure("", txtTime), "h 36!");

        wrap.add(createLabel("Lý do hủy (Tùy chọn)"));
        JTextArea txtReason = new JTextArea(2, 20);
        txtReason.setLineWrap(true);
        txtReason.setWrapStyleWord(true);
        txtReason.setForeground(new Color(15, 23, 42));
        
        RoundedPanel wrapReason = new RoundedPanel(8, Color.WHITE, new Color(226, 232, 240), 1);
        wrapReason.setLayout(new BorderLayout());
        wrapReason.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        txtReason.setBorder(BorderFactory.createEmptyBorder());
        wrapReason.add(txtReason, BorderLayout.CENTER);
        wrap.add(wrapReason, "h 50!");

        RoundedPanel policyBox = new RoundedPanel(10, new Color(254, 242, 242), new Color(254, 202, 202), 1);
        policyBox.setLayout(new MigLayout("insets 10, wrap 1, gap 6", "[grow,fill]", "[]"));
        JLabel polTitle = new JLabel("Áp dụng chính sách");
        polTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        polTitle.setForeground(new Color(185, 28, 28));
        JLabel polSub = new JLabel("Sau giờ nhận phòng: Không hoàn cọ");
        polSub.setForeground(new Color(185, 28, 28));
        
        JPanel polGrid = new JPanel(new MigLayout("insets 0, gap 6", "[grow,fill][grow,fill]", "[]4[]"));
        polGrid.setOpaque(false);
        polGrid.add(createMoneyBox("Tiền cọ đã nhận", "500.000đ"));
        polGrid.add(createMoneyBox("Phí phạt", "500.000đ"), "wrap");
        polGrid.add(createMoneyBox("Số tiền hoàn lại", "0đ"));
        polGrid.add(createMoneyBox("Trạng thái phòng sau", "Trống"));
        
        JLabel polEnd = new JLabel("Quá giờ nhận phòng - không hoàn tiền theo chính sách.");
        polEnd.setForeground(new Color(220, 38, 38));
        polEnd.setFont(new Font("Segoe UI", Font.PLAIN, 10));

        policyBox.add(polTitle);
        policyBox.add(polSub);
        policyBox.add(polGrid, "gapy 4 4");
        policyBox.add(polEnd);
        wrap.add(policyBox);

        JButton btnConfirm = new JButton("Xác nhận hủy");
        btnConfirm.setBackground(new Color(220, 38, 38));
        btnConfirm.setForeground(Color.WHITE);
        btnConfirm.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnConfirm.setFocusPainted(false);
        btnConfirm.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Đã hủy đặt phòng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
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
        JLabel title = new JLabel("Đặt phòng phù hợp");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(15, 23, 42));
        
        titleRow.add(title);
        
        JLabel sub = new JLabel("Chọn một đặt phòng để xem chi tiết và thực hiện hủy.");
        sub.setForeground(new Color(100, 116, 139));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        wrap.add(titleRow, "gapy 0 4");
        wrap.add(sub, "gapy 0 16");

        JPanel listPnl = new ScrollablePanel(new MigLayout("insets 0, wrap 2, gap 16", "[grow,fill][grow,fill]", "[]"));
        listPnl.setOpaque(false);

        if (isConfirming) {
            listPnl.add(createSingleRoomCard(selectedRoomId, selectedRoomType, selectedFloor, selectedCusName, isConfirming));
        } else {
            listPnl.add(createSingleRoomCard("BK001", "Room 201 - Deluxe", "Floor 2", "John Doe", isConfirming));
            listPnl.add(createSingleRoomCard("BK002", "Room 305 - Suite", "Floor 3", "Alice Smith", isConfirming));
            listPnl.add(createSingleRoomCard("BK003", "Room 502 - Standard", "Floor 5", "Bob Wilson", isConfirming));
        }
        
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

    private JPanel createSingleRoomCard(String id, String type, String floor, String cusName, boolean isConfirming) {
        RoundedPanel card = new RoundedPanel(16, Color.WHITE, new Color(226, 232, 240), 1.5f);
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

        JPanel hRow = new JPanel(new BorderLayout());
        hRow.setOpaque(false);
        JLabel dpId = new JLabel(id);
        dpId.setFont(new Font("Segoe UI", Font.BOLD, 18));
        dpId.setForeground(new Color(15, 23, 42));
        hRow.add(dpId, BorderLayout.WEST);

        JLabel rType = new JLabel(type + " · " + floor);
        rType.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rType.setForeground(new Color(100, 116, 139));

        card.add(hRow);
        card.add(rType);
        
        RoundedPanel infoBox = new RoundedPanel(8, new Color(248, 250, 252), new Color(226, 232, 240), 1);
        infoBox.setLayout(new MigLayout("insets 12, wrap 1, gap 4", "[]", "[]"));
        infoBox.add(new JLabel("Guest: " + cusName));
        infoBox.add(new JLabel("Phone: 0912 345 678"));
        infoBox.add(new JLabel("Date: 10/04/2026 14:00"));
        card.add(infoBox, "gapy 8 8");

        JPanel botRow = new JPanel(new BorderLayout());
        botRow.setOpaque(false);
        
        if (isConfirming) {
            JLabel lblConfirming = new JLabel("Selected", SwingConstants.CENTER);
            lblConfirming.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblConfirming.setForeground(new Color(220, 38, 38));
            botRow.add(lblConfirming, BorderLayout.EAST);
        } else {
            JButton btnCancel = new JButton("Hủy đặt phòng");
            btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnCancel.setForeground(new Color(220, 38, 38));
            btnCancel.setBackground(new Color(254, 242, 242));
            btnCancel.setFocusPainted(false);
            btnCancel.addActionListener(e -> {
                selectedRoomId = id;
                selectedRoomType = type;
                selectedFloor = floor;
                selectedCusName = cusName;
                setState("CONFIRM");
            });
            botRow.add(btnCancel, BorderLayout.EAST);
        }

        card.add(botRow, "gapy 8 0");
        return card;
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(71, 85, 105));
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return lbl;
    }

    private JPanel createFieldEnclosure(String icon, JTextField field) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(Color.WHITE);
        wrap.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        
        field.setBorder(BorderFactory.createEmptyBorder());
        field.setOpaque(false);
        field.setForeground(new Color(15, 23, 42));

        wrap.add(field, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel createMoneyBox(String title, String val) {
        RoundedPanel p = new RoundedPanel(8, Color.WHITE, new Color(226, 232, 240), 1);
        p.setLayout(new MigLayout("insets 8, wrap 1, gap 2", "[]", "[]"));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        t.setForeground(new Color(100, 116, 139));
        JLabel v = new JLabel(val);
        v.setFont(new Font("Segoe UI", Font.BOLD, 13));
        v.setForeground(new Color(15, 23, 42));
        p.add(t); p.add(v);
        return p;
    }

    private JPanel createEmptyResultCard() {
        JPanel wrap = new JPanel(new MigLayout("wrap 1,insets 40 20", "[fill]", "[]"));
        wrap.setOpaque(false);

        JLabel resTitle = new JLabel("Chưa có kết quả tìm kiếm", SwingConstants.CENTER);
        resTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        resTitle.setForeground(new Color(15, 23, 42));

        JLabel resSub = new JLabel("<html><center>Nhập mã đặt phòng hoặc thông tin khách rồi nhấn <b>Tìm kiếm</b></center></html>", SwingConstants.CENTER);
        resSub.setForeground(new Color(100, 116, 139));

        wrap.add(resTitle, "alignx center");
        wrap.add(resSub, "alignx center, gapy 10 0");

        return wrap;
    }

    private static class ScrollablePanel extends JPanel implements Scrollable {
        public ScrollablePanel(java.awt.LayoutManager layout) {
            super(layout);
        }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) { return 64; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }
}
