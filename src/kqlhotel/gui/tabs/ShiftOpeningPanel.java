package kqlhotel.gui.tabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import kqlhotel.bus.shift.ShiftBUS;
import kqlhotel.gui.Session;
import kqlhotel.gui.components.BackgroundPanel;
import kqlhotel.gui.components.PrimaryButton;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.gui.theme.ThemeColors;
import net.miginfocom.swing.MigLayout;

public class ShiftOpeningPanel extends BackgroundPanel {
    private static final int[] DENOMINATIONS = {10_000, 20_000, 50_000, 100_000, 200_000, 500_000};

    private final Map<Integer, JSpinner> denominationSpinners = new LinkedHashMap<>();
    private final Map<Integer, JLabel> denominationSubtotals = new LinkedHashMap<>();
    private final Map<Integer, JLabel> breakdownLabels = new LinkedHashMap<>();
    private final JLabel totalLabel = new JLabel();
    private final NumberFormat moneyFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
    private final Runnable onShiftConfirmed;
    private final PrimaryButton confirmButton = new PrimaryButton("Xác nhận & Vào ca");
    private final JTextArea noteArea = new JTextArea(3, 20);

    public ShiftOpeningPanel(Runnable onShiftConfirmed) {
        this.onShiftConfirmed = onShiftConfirmed;
        setOpaque(true);
        setLayout(new MigLayout("insets 32 40,gap 24", "[grow,fill][380!,fill]", "[grow,fill]"));

        add(buildLeftCard(), "grow");
        add(buildRightCard(), "growy,w 380!");

        resetOpeningForm();
    }

    private RoundedPanel buildLeftCard() {
        RoundedPanel left = new RoundedPanel(20, ThemeColors.SURFACE, ThemeColors.BORDER, 1f,
            ThemeColors.withAlpha(new Color(0x0F172A), 18), 4);
        left.setLayout(new MigLayout("wrap 1,insets 28 28 24 28,gap 12", "[grow,fill]", "[]"));

        JLabel title = new JLabel("Kiểm kê tiền đầu ca");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        title.setForeground(ThemeColors.TEXT_PRIMARY);

        JLabel hint = new JLabel("Nhập số tờ cho từng mệnh giá. Tổng sẽ tự cập nhật theo thời gian thực.");
        hint.setForeground(ThemeColors.TEXT_MUTED);
        hint.setFont(hint.getFont().deriveFont(13f));

        left.add(title);
        left.add(hint, "gapy 0 12");

        for (int value : DENOMINATIONS) {
            left.add(buildDenominationRow(value), "growx,h 64!");
        }
        return left;
    }

    private RoundedPanel buildRightCard() {
        RoundedPanel right = new RoundedPanel(20, ThemeColors.SURFACE, ThemeColors.BORDER, 1f,
            ThemeColors.withAlpha(new Color(0x0F172A), 18), 4);
        right.setLayout(new MigLayout("wrap 1,insets 24,gap 12", "[grow,fill]", "[][][grow,fill][][]"));

        // Section: total summary
        JLabel totalCaption = new JLabel("TỔNG TIỀN ĐẦU CA");
        totalCaption.setForeground(ThemeColors.TEXT_MUTED);
        totalCaption.setFont(totalCaption.getFont().deriveFont(Font.BOLD, 11f));

        totalLabel.setForeground(ThemeColors.TEXT_PRIMARY);
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 36f));
        totalLabel.setHorizontalAlignment(SwingConstants.LEFT);

        JPanel totalWrap = new JPanel(new MigLayout("insets 0,wrap 1,gap 4", "[grow,fill]", "[]"));
        totalWrap.setOpaque(false);
        totalWrap.add(totalCaption);
        totalWrap.add(totalLabel);

        // Section: breakdown list
        JPanel breakdown = new JPanel(new MigLayout("wrap 1,insets 0,gap 4", "[grow,fill]", "[]"));
        breakdown.setOpaque(false);
        for (int value : DENOMINATIONS) {
            JLabel line = new JLabel();
            line.setForeground(ThemeColors.TEXT_MUTED);
            line.setFont(line.getFont().deriveFont(12f));
            breakdownLabels.put(value, line);
            breakdown.add(line);
        }

        RoundedPanel breakdownCard = new RoundedPanel(14, ThemeColors.SURFACE_LIGHT, ThemeColors.BORDER_SOFT, 1f);
        breakdownCard.setLayout(new MigLayout("insets 12 16,gap 4", "[grow,fill]", "[]"));
        breakdownCard.add(breakdown, "growx");

        // Section: note
        JLabel noteTitle = new JLabel("Ghi chú (tùy chọn)");
        noteTitle.setForeground(ThemeColors.TEXT_SECONDARY);
        noteTitle.setFont(noteTitle.getFont().deriveFont(Font.BOLD, 12f));

        noteArea.setLineWrap(true);
        noteArea.setWrapStyleWord(true);
        noteArea.setMargin(new Insets(10, 12, 10, 12));
        noteArea.setFont(noteArea.getFont().deriveFont(Font.PLAIN, 13f));
        noteArea.setBackground(ThemeColors.SURFACE_LIGHT);
        noteArea.setForeground(ThemeColors.TEXT_PRIMARY);
        noteArea.setCaretColor(ThemeColors.PRIMARY);

        JScrollPane noteScroll = new JScrollPane(noteArea);
        noteScroll.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER, 1, true));
        noteScroll.getViewport().setBackground(ThemeColors.SURFACE_LIGHT);
        noteScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        noteScroll.setPreferredSize(new Dimension(0, 80));

        // Section: CTA - Premium navy primary (replaces amber accent)
        confirmButton.setBackground(ThemeColors.PREMIUM_PRIMARY);
        confirmButton.setForeground(Color.WHITE);
        confirmButton.setFont(confirmButton.getFont().deriveFont(Font.BOLD, 14f));
        confirmButton.addActionListener(e -> confirmShift());

        right.add(totalWrap);
        right.add(breakdownCard, "growx,gapy 4 4");
        right.add(new JPanel() {{ setOpaque(false); }}, "growy"); // spacer
        right.add(noteTitle, "gapy 4 2");
        right.add(noteScroll, "growx,h 84!");
        right.add(confirmButton, "growx,h 48!,gapy 12 0");
        return right;
    }

    private RoundedPanel buildDenominationRow(int value) {
        Color stripeColor = stripeColorFor(value);
        RoundedPanel row = new RoundedPanel(12, ThemeColors.SURFACE_LIGHT, ThemeColors.BORDER_SOFT, 1f);
        row.setLayout(new MigLayout("insets 0,gap 0", "[6!][140!][grow,fill][120!][140!]", "[grow,fill]"));

        // Colored stripe (left)
        JPanel stripe = new JPanel();
        stripe.setBackground(stripeColor);
        stripe.setOpaque(true);

        // Denomination label
        JLabel money = new JLabel(moneyFormat.format(value) + " đ");
        money.setForeground(ThemeColors.TEXT_PRIMARY);
        money.setFont(money.getFont().deriveFont(Font.BOLD, 16f));
        money.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));

        // Spinner with +/- buttons
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 10_000, 1));
        JFormattedTextField spinnerField = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
        spinnerField.setHorizontalAlignment(SwingConstants.CENTER);
        spinnerField.setFont(spinnerField.getFont().deriveFont(Font.BOLD, 14f));
        spinnerField.setBackground(ThemeColors.SURFACE);
        spinnerField.setForeground(ThemeColors.TEXT_PRIMARY);
        spinnerField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeColors.BORDER, 1, true),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        spinnerField.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        if (spinnerField.getDocument() instanceof AbstractDocument) {
            ((AbstractDocument) spinnerField.getDocument()).setDocumentFilter(new DigitOnlyFilter());
        }
        spinnerField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(spinnerField::selectAll);
            }
            @Override
            public void focusLost(FocusEvent e) {
                String text = spinnerField.getText() == null ? "" : spinnerField.getText().trim();
                if (text.isEmpty()) {
                    spinner.setValue(0);
                    recalculateTotal();
                }
            }
        });
        spinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) { recalculateTotal(); }
        });
        spinnerField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { recalculateTotal(); }
            @Override public void removeUpdate(DocumentEvent e)  { recalculateTotal(); }
            @Override public void changedUpdate(DocumentEvent e) { recalculateTotal(); }
        });

        JButton minus = makeStepButton("−", () -> stepSpinner(spinner, -1));
        JButton plus  = makeStepButton("+",   () -> stepSpinner(spinner, +1));

        JPanel spinnerWrap = new JPanel(new MigLayout("insets 0,gap 6", "[36!][grow,fill][36!]", "[grow,fill]"));
        spinnerWrap.setOpaque(false);
        spinnerWrap.add(minus, "growy");
        spinnerWrap.add(spinner, "grow");
        spinnerWrap.add(plus, "growy");

        // Subtotal label (right)
        JLabel subtotal = new JLabel("= 0 đ");
        subtotal.setForeground(ThemeColors.TEXT_SECONDARY);
        subtotal.setFont(subtotal.getFont().deriveFont(Font.BOLD, 13f));
        subtotal.setHorizontalAlignment(SwingConstants.RIGHT);
        subtotal.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));

        // Quantity hint (between stripe and money)
        JLabel qtyHint = new JLabel();
        qtyHint.setForeground(ThemeColors.TEXT_MUTED);
        qtyHint.setFont(qtyHint.getFont().deriveFont(11f));
        qtyHint.setHorizontalAlignment(SwingConstants.RIGHT);
        qtyHint.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

        // Compose money + qty hint vertically
        JPanel moneyWrap = new JPanel(new BorderLayout());
        moneyWrap.setOpaque(false);
        moneyWrap.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 0));
        moneyWrap.add(money, BorderLayout.CENTER);
        moneyWrap.add(qtyHint, BorderLayout.SOUTH);

        denominationSpinners.put(value, spinner);
        denominationSubtotals.put(value, subtotal);

        row.add(stripe, "growy");
        row.add(moneyWrap, "grow");
        row.add(spinnerWrap, "grow,gap 8 8 10 10");
        row.add(new JLabel(), ""); // spacer
        row.add(subtotal, "grow");
        return row;
    }

    private JButton makeStepButton(String label, Runnable action) {
        JButton btn = new JButton(label);
        btn.setFocusPainted(false);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 14f));
        btn.setForeground(ThemeColors.PRIMARY);
        btn.setBackground(ThemeColors.SURFACE);
        btn.setBorder(BorderFactory.createLineBorder(ThemeColors.BORDER, 1, true));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private void stepSpinner(JSpinner spinner, int delta) {
        int current = getSpinnerCount(spinner);
        int next = Math.max(0, Math.min(10_000, current + delta));
        spinner.setValue(next);
    }

    private void recalculateTotal() {
        long total = 0L;
        for (Map.Entry<Integer, JSpinner> item : denominationSpinners.entrySet()) {
            int value = item.getKey();
            int count = getSpinnerCount(item.getValue());
            long subtotal = (long) value * count;
            total += subtotal;

            JLabel sub = denominationSubtotals.get(value);
            if (sub != null) {
                sub.setText("= " + moneyFormat.format(subtotal) + " đ");
                sub.setForeground(count > 0 ? ThemeColors.SUCCESS : ThemeColors.TEXT_MUTED);
            }
            JLabel line = breakdownLabels.get(value);
            if (line != null) {
                line.setText(count + " tờ × " + moneyFormat.format(value) + " đ  =  " + moneyFormat.format(subtotal) + " đ");
                line.setForeground(count > 0 ? ThemeColors.TEXT_PRIMARY : ThemeColors.TEXT_MUTED);
            }
        }
        totalLabel.setText("<html>" + moneyFormat.format(total) + "<span style='font-size:18px;color:#64748B;'> đ</span></html>");

        boolean hasMoney = total > 0;
        confirmButton.setEnabled(hasMoney);
        confirmButton.setBackground(hasMoney ? ThemeColors.ACCENT : ThemeColors.BG_SECONDARY);
        confirmButton.setForeground(hasMoney ? Color.WHITE : ThemeColors.TEXT_PLACEHOLDER);
        confirmButton.setToolTipText(hasMoney ? null : "Vui lòng nhập số tờ cho ít nhất 1 mệnh giá.");
    }

    private void confirmShift() {
        long total = 0L;
        for (Map.Entry<Integer, JSpinner> item : denominationSpinners.entrySet()) {
            total += (long) item.getKey() * getSpinnerCount(item.getValue());
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

        boolean isManager = Session.currentAccount != null
            && "QuanLy".equals(Session.currentAccount.getRole());
        if (!isManager) {
            String maNV = Session.currentStaff != null ? Session.currentStaff.getMaNV() : null;
            if (maNV == null) {
                JOptionPane.showMessageDialog(this, "Không xác định được nhân viên đang đăng nhập.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = new ShiftBUS().openShift(maNV, total);
            if (!success) {
                JOptionPane.showMessageDialog(
                        this,
                        "Mở ca thất bại.",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
        }

        resetOpeningForm();

        if (this.onShiftConfirmed != null) {
            this.onShiftConfirmed.run();
        }
    }

    /** Trả ghi chú hiện tại (caller có thể dùng để lưu vào bảng PhanCongCa). */
    public String getNote() {
        return noteArea.getText();
    }

    /** Trả tổng tiền mở ca đã kiểm kê. */
    public long getTotal() {
        long total = 0L;
        for (Map.Entry<Integer, JSpinner> item : denominationSpinners.entrySet()) {
            total += (long) item.getKey() * getSpinnerCount(item.getValue());
        }
        return total;
    }

    private int getSpinnerCount(JSpinner spinner) {
        if (!(spinner.getEditor() instanceof JSpinner.DefaultEditor)) {
            return (int) spinner.getValue();
        }

        JFormattedTextField field = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
        String text = field.getText() == null ? "" : field.getText().trim();

        if (text.isEmpty()) {
            return 0;
        }
        if (!text.matches("\\d+")) {
            return (int) spinner.getValue();
        }

        try {
            long parsed = Long.parseLong(text);
            if (parsed < 0L) {
                return 0;
            }
            if (parsed > 10_000L) {
                return 10_000;
            }
            return (int) parsed;
        } catch (NumberFormatException ex) {
            return (int) spinner.getValue();
        }
    }

    private static final class DigitOnlyFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            replace(fb, offset, 0, string, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            String incoming = text == null ? "" : text;
            String current = fb.getDocument().getText(0, fb.getDocument().getLength());
            String next = current.substring(0, offset) + incoming + current.substring(offset + length);

            if (next.isEmpty()) {
                super.replace(fb, offset, length, text, attrs);
                return;
            }

            if (!next.matches("\\d+")) {
                return;
            }

            try {
                long value = Long.parseLong(next);
                if (value > 10_000L) {
                    return;
                }
            } catch (NumberFormatException ex) {
                return;
            }

            super.replace(fb, offset, length, text, attrs);
        }
    }

    /**
     * Màu stripe theo mệnh giá — lấy đúng màu chủ đạo của tờ tiền VND
     * thật ngoài đời để nhân viên nhận diện nhanh khi kiểm kê.
     */
    private Color stripeColorFor(int value) {
        switch (value) {
            case 10_000:  return new Color(0xC1A376); // nâu vàng / vàng đất
            case 20_000:  return new Color(0x5885AF); // xanh dương đậm
            case 50_000:  return new Color(0xE29BB1); // hồng tím
            case 100_000: return new Color(0xA5C97A); // xanh lá mạ
            case 200_000: return new Color(0xD17A61); // đỏ cam / nâu hồng
            case 500_000: return new Color(0x82B0D2); // xanh lơ / xanh tím
            default:      return ThemeColors.TEXT_MUTED;
        }
    }

    public void resetOpeningForm() {
        for (JSpinner spinner : denominationSpinners.values()) {
            spinner.setValue(0);
        }
        noteArea.setText("");
        recalculateTotal();
    }
}
