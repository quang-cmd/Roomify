package kqlhotel.gui.tabs;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import kqlhotel.gui.components.LoginBackgroundPanel;
import kqlhotel.gui.components.PrimaryButton;
import kqlhotel.gui.components.RoundedPanel;
import net.miginfocom.swing.MigLayout;

public class ShiftOpeningPanel extends LoginBackgroundPanel {
    private static final Color OVERLAY = new Color(32, 41, 58, 205); // #20293A + alpha
    private static final Color GROUP_BG = new Color(45, 55, 72, 220); // #2D3748 + alpha
    private static final Color PRIMARY_BUTTON = new Color(255, 138, 61); // #FF8A3D
    private static final Color TEXT_MAIN = new Color(255, 255, 255);
    private static final Color TEXT_MUTED = new Color(160, 174, 192); // #A0AEC0

    private final Map<Integer, JSpinner> denominationSpinners = new LinkedHashMap<>();
    private final JLabel totalLabel = new JLabel("0đ");
    private final NumberFormat moneyFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
    private final Runnable onShiftConfirmed;
    private final GlowPrimaryButton confirmButton;

    public ShiftOpeningPanel(Runnable onShiftConfirmed) {
        this.onShiftConfirmed = onShiftConfirmed;
        setOpaque(true);
        setLayout(new MigLayout("insets 24,gap 16", "[grow,fill][360!,fill]", "[]"));

        RoundedPanel left = new RoundedPanel(24, OVERLAY, new Color(91, 107, 131, 120), 1f);
        left.setLayout(new MigLayout("wrap 2,insets 20,gap 16", "[grow,fill][grow,fill]", "[]"));

        JLabel title = new JLabel("Kiểm kê tiền đầu ca");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        title.setForeground(TEXT_MAIN);
        JLabel hint = new JLabel("Nhập số tờ đồng cho từng mệnh giá để xác nhận tiền đầu ca.");
        hint.setForeground(TEXT_MUTED);

        JPanel titleWrap = new JPanel(new MigLayout("insets 0,wrap 1", "[grow,fill]", "[]"));
        titleWrap.setOpaque(false);
        titleWrap.add(title);
        titleWrap.add(hint);

        left.add(titleWrap, "span 2,gapy 0 10");

        addDenomination(left, 10_000);
        addDenomination(left, 20_000);
        addDenomination(left, 50_000);
        addDenomination(left, 100_000);
        addDenomination(left, 200_000);
        addDenomination(left, 500_000);

        RoundedPanel right = new RoundedPanel(24, OVERLAY, new Color(91, 107, 131, 120), 1f);
        right.setLayout(new MigLayout("wrap 1,insets 20,gap 14", "[grow,fill]", "[]"));

        JLabel totalCaption = new JLabel("Tổng tiền đầu ca");
        totalCaption.setForeground(TEXT_MUTED);
        totalLabel.setForeground(TEXT_MAIN);
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 40f));
        totalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        totalLabel.setText("<html>999.999.999<span style='font-size:20px;'>đ</span></html>");

        confirmButton = new GlowPrimaryButton("Xác nhận & Vào ca");
        confirmButton.setBackground(PRIMARY_BUTTON);
        confirmButton.setForeground(TEXT_MAIN);
        confirmButton.addActionListener(e -> confirmShift());

        right.add(totalCaption);
        right.add(totalLabel);
        JLabel detail = new JLabel("Chi tiết mệnh giá sẽ cập nhật theo số lượng.");
        detail.setForeground(TEXT_MUTED);
        right.add(detail);

        JTextArea noteArea = new JTextArea("Ghi chú (tùy chọn)...");
        noteArea.setBackground(GROUP_BG);
        noteArea.setForeground(TEXT_MAIN);
        noteArea.setCaretColor(TEXT_MAIN);
        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JScrollPane noteScroll = new JScrollPane(noteArea);
        noteScroll.setBorder(BorderFactory.createLineBorder(new Color(90, 107, 132, 140), 1));
        noteScroll.getViewport().setOpaque(false);
        noteScroll.setOpaque(false);
        right.add(noteScroll, "h 96");
        right.add(confirmButton, "h 44");

        add(left, "grow");
        add(right, "growy,w 360!");

        recalculateTotal();
    }

    private void addDenomination(JPanel parent, int value) {
        RoundedPanel row = new RoundedPanel(14, tagBackgroundFor(value), new Color(255, 255, 255, 80), 1f);
        row.setLayout(new MigLayout("insets 12,gap 10", "[right,90!][grow,fill]", "[]"));

        JLabel money = new JLabel(moneyFormat.format(value) + "d");
        money.setForeground(new Color(56, 66, 87));
        money.setFont(money.getFont().deriveFont(Font.BOLD));
        money.setHorizontalAlignment(SwingConstants.RIGHT);

        JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 10_000, 1));
        spinner.putClientProperty("FlatLaf.style", "arc:14;background:#2D3748;foreground:#FFFFFF;borderColor:#4A5568;buttonBackground:#334155;buttonArrowColor:#E2E8F0");
        JFormattedTextField spinnerField = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
        spinnerField.setBackground(new Color(45, 55, 72));
        spinnerField.setForeground(TEXT_MAIN);
        spinnerField.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        spinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                recalculateTotal();
            }
        });

        denominationSpinners.put(value, spinner);
        row.add(money, "growx");
        row.add(spinner, "w 100!");

        parent.add(row, "growx");
    }

    private void recalculateTotal() {
        long total = 0L;
        for (Map.Entry<Integer, JSpinner> item : denominationSpinners.entrySet()) {
            total += (long) item.getKey() * (int) item.getValue().getValue();
        }
        totalLabel.setText("<html>" + moneyFormat.format(total) + "<span style='font-size:20px;'>đ</span></html>");
        confirmButton.setEnabled(total > 0);
    }

    private void confirmShift() {
        long total = 0L;
        for (Map.Entry<Integer, JSpinner> item : denominationSpinners.entrySet()) {
            total += (long) item.getKey() * (int) item.getValue().getValue();
        }

        if (total <= 0) {
            JOptionPane.showMessageDialog(
                this,
                "Vui lòng nhập ít nhất một mệnh giá trước khi xác nhận ca.",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (this.onShiftConfirmed != null) {
            this.onShiftConfirmed.run();
        }
    }

    private Color tagBackgroundFor(int value) {
        if (value == 10_000) {
            return new Color(253, 226, 226); // #FDE2E2
        }
        if (value == 20_000) {
            return new Color(225, 239, 254); // #E1EFFE
        }
        if (value == 50_000) {
            return new Color(253, 247, 236); // #FDF7EC
        }
        if (value == 100_000) {
            return new Color(254, 243, 199); // #FEF3C7
        }
        if (value == 200_000) {
            return new Color(255, 237, 213); // #FFEDD5
        }
        if (value == 500_000) {
            return new Color(252, 232, 243); // #FCE8F3
        }
        return new Color(255, 255, 255);
    }

    private static final class GlowPrimaryButton extends PrimaryButton {
        private GlowPrimaryButton(String text) {
            super(text);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 138, 61, 70));
            g2.fillRoundRect(4, 8, getWidth() - 8, getHeight() - 10, 16, 16);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
