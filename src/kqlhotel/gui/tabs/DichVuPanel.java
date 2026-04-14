package kqlhotel.gui.tabs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DichVuPanel extends JPanel {

    public DichVuPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // ===== HEADER =====
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(20, 20, 10, 20));

        JLabel title = new JLabel("Dịch vụ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));

        JButton btnAdd = new JButton("+ Thêm dịch vụ");
        btnAdd.setBackground(new Color(30, 41, 59));
        btnAdd.setForeground(Color.WHITE);

        header.add(title, BorderLayout.WEST);
        header.add(btnAdd, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ===== GRID =====
        JPanel grid = new JPanel(new GridLayout(0, 3, 20, 20));
        grid.setBorder(new EmptyBorder(20, 20, 20, 20));
        grid.setOpaque(false);

        grid.add(createCard("Dọn phòng", "Miễn phí", "Buồng phòng"));
        grid.add(createCard("Giặt ủi", "50.000đ", "Buồng phòng"));
        grid.add(createCard("Buffet sáng", "220.000đ", "Ăn uống"));
        grid.add(createCard("Spa & Massage", "800.000đ", "Thư giãn"));
        grid.add(createCard("Thuê xe", "350.000đ", "Vận chuyển"));
        grid.add(createCard("Phòng họp", "1.500.000đ", "Tiện ích"));

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setBlockIncrement(50);

        add(scroll, BorderLayout.CENTER);
    }

    private JPanel createCard(String ten, String gia, String loai) {

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // ===== TOP =====
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel status = new JLabel("Hoạt động");
        status.setOpaque(true);
        status.setBackground(new Color(220, 252, 231));
        status.setForeground(new Color(22, 163, 74));
        status.setBorder(new EmptyBorder(3, 8, 3, 8));

        top.add(status, BorderLayout.EAST);

        // ===== CENTER =====
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        JLabel lblTen = new JLabel(ten);
        lblTen.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JTextArea moTa = new JTextArea(getMoTa(ten));
        moTa.setWrapStyleWord(true);
        moTa.setLineWrap(true);
        moTa.setEditable(false);
        moTa.setFocusable(false);
        moTa.setBackground(null);
        moTa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        moTa.setForeground(Color.GRAY);
        moTa.setForeground(Color.GRAY);

        center.add(lblTen);
        center.add(Box.createVerticalStrut(5));
        center.add(moTa);

        // ===== BOTTOM =====
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        JLabel tag = new JLabel(loai);
        tag.setOpaque(true);
        tag.setBorder(new EmptyBorder(3, 8, 3, 8));
        tag.setBackground(getColor(loai));

        JLabel lblGia = new JLabel(gia);
        lblGia.setFont(new Font("Segoe UI", Font.BOLD, 14));

        bottom.add(tag, BorderLayout.WEST);
        bottom.add(lblGia, BorderLayout.EAST);

        // ===== ADD =====
        card.add(top, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }

    private Color getColor(String loai) {
        switch (loai) {
            case "Ăn uống":
                return new Color(255, 237, 213);
            case "Thư giãn":
                return new Color(220, 252, 231);
            case "Vận chuyển":
                return new Color(219, 234, 254);
            default:
                return new Color(237, 233, 254);
        }
    }
    private String getMoTa(String ten) {
        switch (ten) {
            case "Dọn phòng":
                return "Dọn dẹp phòng sạch sẽ mỗi ngày cho khách.";
            case "Giặt ủi":
                return "Dịch vụ giặt và ủi quần áo nhanh chóng.";
            case "Buffet sáng":
                return "Buffet sáng với nhiều món ăn hấp dẫn.";
            case "Spa & Massage":
                return "Thư giãn với các liệu trình massage chuyên nghiệp.";
            case "Thuê xe":
                return "Thuê xe tiện lợi cho việc di chuyển.";
            case "Phòng họp":
                return "Không gian phòng họp hiện đại, đầy đủ tiện nghi.";
            default:
                return "Dịch vụ tiện ích dành cho khách hàng.";
        }
    }

}