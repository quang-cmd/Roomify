package kqlhotel.gui.tabs;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DoiPhongPanel extends JPanel {

    public DoiPhongPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        JPanel main = new JPanel(new MigLayout(
                "insets 30,gap 30",
                "[350!][grow]",
                "[grow]"
        ));
        main.setOpaque(false);

        main.add(createSearchCard(), "growy");
        main.add(createEmptyPanel(), "grow");

        add(main, BorderLayout.CENTER);
    }

    // ===== LEFT CARD =====
    private JPanel createSearchCard() {
        JPanel card = new JPanel(new MigLayout(
                "wrap 1,insets 20,gap 12",
                "[grow,fill]",
                "[]"
        ));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(10, 10, 10, 10)
        ));

        // ===== HEADER =====
        JPanel header = new JPanel(new MigLayout("insets 0,gap 10", "[][]", "[]"));
        header.setOpaque(false);

        JLabel icon = new JLabel("🔍");
        icon.setOpaque(true);
        icon.setBackground(new Color(230, 240, 255));
        icon.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Tìm khách cần đổi phòng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));

        header.add(icon);
        header.add(title);

        JLabel desc = new JLabel("Nhập ít nhất 1 thông tin để tra cứu khách cần đổi phòng");
        desc.setForeground(Color.GRAY);
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JSeparator line = new JSeparator();

        // ===== FORM =====
        JTextField ma = createInput("Ví dụ: DP001");
        JTextField ten = createInput("Ví dụ: Nguyễn Văn A");
        JTextField sdt = createInput("Ví dụ: 0912345678");
        JTextField phong = createInput("Ví dụ: 101");

        JButton btn = new JButton("Tìm khách hàng");
        btn.setBackground(new Color(20, 30, 60));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(new EmptyBorder(12, 0, 12, 0));

        // ===== ADD =====
        card.add(header);
        card.add(desc);
        card.add(line, "growx");

        card.add(new JLabel("Mã đặt phòng"));
        card.add(ma);

        card.add(new JLabel("Tên khách"));
        card.add(ten);

        card.add(new JLabel("Số điện thoại"));
        card.add(sdt);

        card.add(new JLabel("Số phòng"));
        card.add(phong);

        card.add(btn, "gapy 10");

        return card;
    }

    private JTextField createInput(String hint) {
        JTextField txt = new JTextField();
        txt.setPreferredSize(new Dimension(200, 36));
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                new EmptyBorder(5, 10, 5, 10)
        ));
        txt.setToolTipText(hint);
        return txt;
    }

    // ===== RIGHT EMPTY =====
    private JPanel createEmptyPanel() {
        JPanel panel = new JPanel(new MigLayout(
                "wrap 1,align center center",
                "[center]",
                "[]20[]"
        ));
        panel.setOpaque(false);

        JLabel icon = new JLabel("🔄");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        icon.setForeground(new Color(200, 200, 200));

        JLabel title = new JLabel("Chưa có dữ liệu đổi phòng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(70, 70, 70));

        JLabel desc = new JLabel("Tìm khách hàng đang có phòng hợp lệ để thực hiện đổi phòng");
        desc.setForeground(Color.GRAY);

        panel.add(icon);
        panel.add(title);
        panel.add(desc);

        return panel;
    }
}