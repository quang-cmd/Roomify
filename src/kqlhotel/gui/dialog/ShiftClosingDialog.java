package kqlhotel.gui.dialog;

import kqlhotel.bus.shift.ShiftBUS;
import kqlhotel.dao.shift.ShiftDAO.ShiftInfo;
import kqlhotel.gui.components.PrimaryButton;
import kqlhotel.gui.theme.ThemeColors;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

public class ShiftClosingDialog extends JDialog {
    private final ShiftInfo shiftInfo;
    private final Runnable onClosed;
    private final JTextField closingMoneyField = new JTextField();
    private final NumberFormat moneyFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

    public ShiftClosingDialog(Window parent, ShiftInfo shiftInfo, Runnable onClosed) {
        super(parent, "Kết ca", ModalityType.APPLICATION_MODAL);
        this.shiftInfo = shiftInfo;
        this.onClosed = onClosed;

        setSize(480, 420);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        add(createBody(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createBody() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 18, 28));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel titleWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        titleWrap.setOpaque(false);
        titleWrap.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleWrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel title = new JLabel("KẾT CA LÀM VIỆC");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setForeground(new Color(24, 40, 66));

        titleWrap.add(title);
        panel.add(titleWrap);
        panel.add(Box.createVerticalStrut(18));

        panel.add(row("Mã phân công", shiftInfo.maPC));
        panel.add(row("Nhân viên", shiftInfo.hoTenNV));
        panel.add(row("Loại ca", shiftInfo.loaiCa));
        panel.add(row("Giờ ca", shiftInfo.gioBatDau + " - " + shiftInfo.gioKetThuc));
        panel.add(row("Tiền mở ca", formatMoney(shiftInfo.tienMoCa)));
        panel.add(row("Doanh thu trong ca", formatMoney(shiftInfo.doanhThu)));
        panel.add(row("Số giao dịch", String.valueOf(shiftInfo.soGiaoDich)));

        panel.add(Box.createVerticalStrut(14));

        double expectedCash = shiftInfo.tienMoCa + shiftInfo.doanhThu;

        JLabel expected = new JLabel("Tiền dự kiến trong két: " + formatMoney(expectedCash));
        expected.setAlignmentX(Component.LEFT_ALIGNMENT);
        expected.setFont(expected.getFont().deriveFont(Font.BOLD, 14f));
        expected.setForeground(new Color(30, 120, 80));
        panel.add(expected);

        panel.add(Box.createVerticalStrut(12));

        JLabel inputLabel = new JLabel("Nhập tiền kết ca thực tế");
        inputLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputLabel.setForeground(new Color(80, 95, 115));
        inputLabel.setFont(inputLabel.getFont().deriveFont(Font.BOLD, 13f));
        panel.add(inputLabel);

        closingMoneyField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        closingMoneyField.setFont(closingMoneyField.getFont().deriveFont(Font.BOLD, 16f));
        closingMoneyField.setText(String.valueOf(Math.round(expectedCash)));
        panel.add(Box.createVerticalStrut(6));
        panel.add(closingMoneyField);

        JLabel hint = new JLabel("Ví dụ: 1500000 hoặc 1.500.000");
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        hint.setForeground(new Color(120, 130, 145));
        hint.setFont(hint.getFont().deriveFont(12f));
        panel.add(Box.createVerticalStrut(6));
        panel.add(hint);

        return panel;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 235, 245)));

        Dimension cancelSize = new Dimension(120, 42);
        Dimension confirmSize = new Dimension(200, 42);

        JButton cancelBtn = new JButton("Hủy");
        cancelBtn.setPreferredSize(cancelSize);
        cancelBtn.setMinimumSize(cancelSize);
        cancelBtn.setMaximumSize(cancelSize);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setFont(cancelBtn.getFont().deriveFont(Font.PLAIN, 14f));
        cancelBtn.addActionListener(e -> dispose());

        PrimaryButton closeBtn = new PrimaryButton("Xác nhận kết ca");
        closeBtn.setPreferredSize(confirmSize);
        closeBtn.setMinimumSize(confirmSize);
        closeBtn.setMaximumSize(confirmSize);
        closeBtn.setBackground(ThemeColors.PREMIUM_PRIMARY);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setFont(closeBtn.getFont().deriveFont(Font.BOLD, 14f));
        closeBtn.addActionListener(e -> confirmCloseShift());

        footer.add(cancelBtn);
        footer.add(closeBtn);

        return footer;
    }

    private JPanel row(String left, String right) {
        JPanel row = new JPanel(new GridLayout(1, 2));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel l = new JLabel(left);
        l.setForeground(new Color(80, 95, 115));
        l.setFont(l.getFont().deriveFont(13f));

        JLabel r = new JLabel(right == null ? "" : right);
        r.setHorizontalAlignment(SwingConstants.RIGHT);
        r.setForeground(new Color(24, 40, 66));
        r.setFont(r.getFont().deriveFont(Font.BOLD, 13f));

        row.add(l);
        row.add(r);

        return row;
    }

    private void confirmCloseShift() {
        long actualMoney = parseMoney(closingMoneyField.getText());

        if (actualMoney < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Tiền kết ca không hợp lệ.",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        double expected = shiftInfo.tienMoCa + shiftInfo.doanhThu;
        double diff = actualMoney - expected;

        if (Math.abs(diff) > 0) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Tiền thực tế lệch so với dự kiến: " + formatMoney(diff) + "\n"
                            + "Bạn vẫn muốn kết ca?",
                    "Xác nhận lệch tiền",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        boolean success = new ShiftBUS().closeShift(shiftInfo.maPC, actualMoney);

        if (!success) {
            JOptionPane.showMessageDialog(
                    this,
                    "Kết ca thất bại. Có thể ca này đã được kết hoặc không còn tồn tại.",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "Kết ca thành công!",
                "Thông báo",
                JOptionPane.INFORMATION_MESSAGE
        );

        dispose();

        if (onClosed != null) {
            onClosed.run();
        }
    }

    private long parseMoney(String text) {
        if (text == null) {
            return -1;
        }

        String cleaned = text.replaceAll("[^0-9]", "");

        if (cleaned.isBlank()) {
            return -1;
        }

        try {
            return Long.parseLong(cleaned);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String formatMoney(double amount) {
        return moneyFormat.format(Math.round(amount)) + " đ";
    }
}