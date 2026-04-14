package kqlhotel.gui.tabs;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class KhachHangPanel extends JPanel {

    public KhachHangPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        JPanel main = new JPanel(new MigLayout(
                "insets 20,gap 20",
                "[320!][grow]",
                "[grow]"
        ));
        main.setOpaque(false);

        main.add(leftPanel(), "growy");
        main.add(rightPanel(), "grow");

        add(main, BorderLayout.CENTER);
    }

    // ================= LEFT =================
    private JPanel leftPanel() {
        JPanel panel = new JPanel(new MigLayout("wrap 1,insets 15,gap 10", "[grow,fill]", "[]"));
        panel.setBackground(Color.WHITE);

        JTextField search = new JTextField("Tìm khách hàng...");
        search.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(search);

        panel.add(item("MK", "Trần Minh Khoa", "0912 345 678", true));
        panel.add(item("TH", "Nguyễn Thị Hoa", "0987 654 321", true));
        panel.add(item("VD", "Lê Văn Dũng", "0901 234 567", true));
        panel.add(item("TL", "Phạm Thị Lan", "0934 567 890", true));
        panel.add(item("MT", "Hoàng Minh Tú", "0965 432 109", true));
        panel.add(item("TM", "Vũ Thị Mai", "0978 901 234", true));
        panel.add(item("QB", "Đặng Quốc Bảo", "0923 456 789", true));
        panel.add(item("TN", "Bùi Thị Ngọc", "0956 789 012", false));

        return panel;
    }

    private JPanel item(String avatar, String name, String phone, boolean active) {
        JPanel p = new JPanel(new MigLayout("insets 10,gap 10", "[][grow][]", "[]"));
        p.setBackground(new Color(248, 250, 252));
        p.setBorder(BorderFactory.createLineBorder(new Color(230,230,230)));

        JLabel ava = new JLabel(avatar, SwingConstants.CENTER);
        ava.setOpaque(true);
        ava.setBackground(new Color(16, 185, 129));
        ava.setForeground(Color.WHITE);
        ava.setPreferredSize(new Dimension(40,40));

        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JLabel phoneLbl = new JLabel(phone);
        phoneLbl.setForeground(Color.GRAY);

        JPanel text = new JPanel(new GridLayout(2,1));
        text.setOpaque(false);
        text.add(nameLbl);
        text.add(phoneLbl);

        JLabel status = new JLabel(active ? "Active" : "Inactive");
        status.setForeground(active ? new Color(34,197,94) : Color.GRAY);

        p.add(ava);
        p.add(text, "growx");
        p.add(status);

        return p;
    }

    // ================= RIGHT =================
    private JPanel rightPanel() {
        JPanel panel = new JPanel(new MigLayout(
                "wrap 1,insets 20,gap 20",
                "[grow,fill]",
                "[]"
        ));
        panel.setOpaque(false);

        panel.add(header());
        panel.add(statistics());
        panel.add(info());
        panel.add(history());

        return panel;
    }

    private JPanel header() {
        JPanel p = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        p.setOpaque(false);

        JLabel name = new JLabel("Trần Minh Khoa");
        name.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JButton btn = new JButton("+ Đặt phòng mới");
        btn.setBackground(new Color(59,130,246));
        btn.setForeground(Color.WHITE);

        p.add(name);
        p.add(btn);

        return p;
    }

    private JPanel statistics() {
        JPanel p = new JPanel(new GridLayout(1,3,15,0));
        p.setOpaque(false);

        p.add(box("Tổng đặt phòng", "5"));
        p.add(box("Tổng chi tiêu", "24.500.000đ"));
        p.add(box("Lần gần nhất", "20/03/2026"));

        return p;
    }

    private JPanel box(String title, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(15,15,15,15));

        JLabel t = new JLabel(title);
        t.setForeground(Color.GRAY);

        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 16));

        p.add(t, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);

        return p;
    }

    private JPanel info() {
        JPanel p = new JPanel(new GridLayout(2,2,15,15));
        p.setOpaque(false);

        p.add(box("SĐT", "0912 345 678"));
        p.add(box("Email", "khoa@gmail.com"));
        p.add(box("Địa chỉ", "Hà Nội"));
        p.add(box("CCCD", "001090012345"));

        return p;
    }

    private JPanel history() {
        JPanel p = new JPanel(new MigLayout("wrap 1", "[grow,fill]", "[]"));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(15,15,15,15));

        p.add(new JLabel("Lịch sử đặt phòng"));

        p.add(historyItem("Phòng 401 - Suite", "11.000.000đ"));
        p.add(historyItem("Phòng 301 - Premium", "9.600.000đ"));

        return p;
    }

    private JPanel historyItem(String room, String price) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(10,0,10,0));

        JLabel r = new JLabel(room);
        JLabel pr = new JLabel(price);

        p.add(r, BorderLayout.WEST);
        p.add(pr, BorderLayout.EAST);

        return p;
    }
}