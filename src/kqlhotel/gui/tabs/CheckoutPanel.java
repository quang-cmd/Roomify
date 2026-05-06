package kqlhotel.gui.tabs;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import kqlhotel.bus.checkout.CheckoutBUS;
import kqlhotel.entity.Invoice;
import kqlhotel.entity.Promotion;
import kqlhotel.gui.components.PrimaryButton;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.gui.theme.ThemeColors;
import kqlhotel.utils.CurrencyUtils;
import kqlhotel.utils.PDFInvoiceGenerator;
import net.miginfocom.swing.MigLayout;

public class CheckoutPanel extends JPanel {
    private static final Color PAGE_BG = new Color(245, 248, 252);

    private final CheckoutBUS checkoutBUS = new CheckoutBUS();
    private final CardLayout mainCards = new CardLayout();
    private final JPanel contentPanel = new JPanel(mainCards);

    private final JLabel step1Label = new JLabel();
    private final JLabel step2Label = new JLabel();

    private final JTextField roomCodeField = new JTextField();
    private final JTextField customerIdField = new JTextField();
    private final JTextField customerNameField = new JTextField();
    private final JLabel detailDepositLabel = new JLabel();
    private final JLabel detailPenaltyLabel = new JLabel();

    private final JPanel roomListPanel = new JPanel(
            new MigLayout("wrap 2,insets 0,gap 12", "[grow,fill][grow,fill]", "[]")
    );

    private final JLabel detailNameLabel = new JLabel();
    private final JLabel detailRoomLabel = new JLabel();
    private final JLabel detailRoomPriceLabel = new JLabel();
    private final JLabel detailDateInLabel = new JLabel();
    private final JLabel detailDateOutLabel = new JLabel();

    private final JLabel detailTotalRoomLabel = new JLabel();
    private final JLabel detailTotalServiceLabel = new JLabel();
    private final JLabel detailSurchargeLabel = new JLabel();
    private final JLabel detailTaxLabel = new JLabel();
    private final JLabel detailDiscountLabel = new JLabel();
    private final JLabel detailTotalFinalLabel = new JLabel();

    private final JLabel kName = new JLabel();
    private final JLabel kRoom = new JLabel();
    private final JLabel kDateIn = new JLabel();
    private final JLabel kDateOut = new JLabel();
    private final JLabel kCID = new JLabel();

    private final JComboBox<String> promotionCombo = new JComboBox<>();
    private final Map<String, String> promotionDisplayToCode = new LinkedHashMap<>();
    private String selectedPromotionCode = null;

    private List<CheckoutData> selectedRooms = new java.util.ArrayList<>();
    private final PrimaryButton checkoutMultiBtn = new PrimaryButton("Thanh toán các phòng đã chọn (0)");
    private List<String> currentRoomCodes = new java.util.ArrayList<>();

    private String nextRoomStatus = "Trong";
    private boolean isSaveInvoice = true;
    private boolean isPrintInvoice = false;
    private Invoice currentHoaDon;

    public CheckoutPanel() {
        setOpaque(false);
        setBackground(PAGE_BG);
        setLayout(new BorderLayout());

        //JPanel header = createHeader();

        contentPanel.setOpaque(false);
        contentPanel.add(createStep1View(), "step1");
        contentPanel.add(createStep2View(), "step2");

        //add(header, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        setStep(1);
        renderRooms(checkoutBUS.getRoomsDueToday());
    }

    public void prefillAndSearchRoom(String roomCode) {
        setStep(1);
        mainCards.show(contentPanel, "step1");
        
        roomCodeField.setText(roomCode);
        customerIdField.setText("");
        customerNameField.setText("");

        List<CheckoutData> results = checkoutBUS.searchCheckoutData(roomCode, "", "");
        selectedRooms.clear();
        checkoutMultiBtn.setText("Thanh toán các phòng đã chọn (0)");
        renderRooms(results);

        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy lưu trú nào phù hợp!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new MigLayout("insets 20 24 0 24,gap 0", "[grow][]", "[]"));
        panel.setOpaque(false);

        JPanel titleBox = new JPanel(new MigLayout("insets 0, wrap 1", "[]", "[]"));
        titleBox.setOpaque(false);

        JLabel title = new JLabel("Trả phòng");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setForeground(new Color(24, 40, 66));

        JLabel subtitle = new JLabel("Thực hiện thủ tục trả phòng và cập nhật trạng thái phòng");
        subtitle.setForeground(new Color(119, 137, 168));
        subtitle.setFont(subtitle.getFont().deriveFont(13f));

        titleBox.add(title);
        titleBox.add(subtitle);

        JPanel stepper = createStepperPanel();

        panel.add(titleBox, "aligny center");
        panel.add(stepper, "aligny center");

        return panel;
    }

    private JPanel createStepperPanel() {
        JPanel stepper = new JPanel(new MigLayout("insets 6 10,gap 0", "[grow,fill][grow,fill]", "[]"));
        stepper.setOpaque(false);
        stepper.setBorder(BorderFactory.createLineBorder(new Color(225, 231, 245), 1));
        stepper.setBackground(Color.WHITE);

        step1Label.setHorizontalAlignment(SwingConstants.CENTER);
        step2Label.setHorizontalAlignment(SwingConstants.CENTER);
        step1Label.setFont(step1Label.getFont().deriveFont(Font.BOLD, 12f));
        step2Label.setFont(step2Label.getFont().deriveFont(Font.BOLD, 12f));

        JPanel s1Wrap = new JPanel(new BorderLayout());
        s1Wrap.setOpaque(false);
        s1Wrap.add(step1Label);

        JLabel arrow = new JLabel(" \u203A ", SwingConstants.CENTER);
        arrow.setForeground(new Color(180, 190, 210));

        JPanel s2Wrap = new JPanel(new BorderLayout());
        s2Wrap.setOpaque(false);
        s2Wrap.add(step2Label);

        stepper.add(s1Wrap, "h 30");
        stepper.add(arrow);
        stepper.add(s2Wrap, "h 30");

        return stepper;
    }

    private JPanel createStep1View() {
        JPanel panel = new JPanel(new MigLayout(
                "insets 5 24 10 24, gap 12, fill",
                "[300!][grow,fill]",
                "[grow,fill]"
        ));
        panel.setOpaque(false);

        RoundedPanel filterCard = new RoundedPanel(20, Color.WHITE, new Color(225, 231, 245), 1.5f);
        filterCard.setLayout(new MigLayout("wrap 1,insets 8,gap 4", "[grow,fill]", "[]"));

        JLabel title = new JLabel("Tìm phòng cần trả");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setForeground(new Color(24, 40, 66));

        JLabel desc = new JLabel("<html><span style='color:#7789A8'>Nhập ít nhất 1 thông tin để tra cứu lưu trú cần trả phòng</span></html>");

        filterCard.add(title);
        filterCard.add(desc, "gapy 0 10");

        filterCard.add(makeFilterLabel("Mã phòng"));
        roomCodeField.putClientProperty("JTextField.placeholderText", "Ví dụ: 101");
        filterCard.add(makeTextInput(roomCodeField), "h 32!");

        filterCard.add(makeFilterLabel("Mã khách"), "gapy 4 0");
        customerIdField.putClientProperty("JTextField.placeholderText", "Ví dụ: KH001");
        filterCard.add(makeTextInput(customerIdField), "h 32!");

        filterCard.add(makeFilterLabel("Họ tên khách"), "gapy 4 0");
        customerNameField.putClientProperty("JTextField.placeholderText", "Ví dụ: Nguyễn Văn A");
        filterCard.add(makeTextInput(customerNameField), "h 32!");

        PrimaryButton searchBtn = new PrimaryButton("Tìm lưu trú");
        searchBtn.setBackground(new Color(24, 34, 52));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.addActionListener(e -> {
            String rC = roomCodeField.getText().trim();
            String cI = customerIdField.getText().trim();
            String cN = customerNameField.getText().trim();

            if (rC.isEmpty() && cI.isEmpty() && cN.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập ít nhất 1 thông tin để tìm kiếm!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            List<CheckoutData> results = checkoutBUS.searchCheckoutData(rC, cI, cN);
            selectedRooms.clear();
            checkoutMultiBtn.setText("Thanh toán các phòng đã chọn (0)");
            renderRooms(results);

            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy lưu trú nào phù hợp!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        filterCard.add(searchBtn, "h 40!, gapy 10 0");

        PrimaryButton refreshBtn = new PrimaryButton("Làm mới");
        refreshBtn.setBackground(new Color(24, 34, 52));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.addActionListener(e -> {
            roomCodeField.setText("");
            customerIdField.setText("");
            customerNameField.setText("");
            selectedRooms.clear();
            checkoutMultiBtn.setText("Thanh toán các phòng đã chọn (0)");
            renderRooms(checkoutBUS.getRoomsDueToday());
        });
        filterCard.add(refreshBtn, "h 40!, gapy 6 0");

        JPanel rightSide = new JPanel(new MigLayout(
                "wrap 1,insets 0, fill",
                "[grow,fill]",
                "[][][grow,fill][]"
        ));
        rightSide.setOpaque(false);

        JLabel rTitle = new JLabel("Phòng dự kiến trả trong ngày");
        rTitle.setFont(rTitle.getFont().deriveFont(Font.BOLD, 14f));
        rTitle.setForeground(new Color(24, 40, 66));

        JLabel rDesc = new JLabel("Gợi ý các phòng có lịch trả hôm nay để lễ tân xử lý nhanh");
        rDesc.setForeground(new Color(119, 137, 168));

        rightSide.add(rTitle);
        rightSide.add(rDesc, "gapy 0 10");

        roomListPanel.setOpaque(false);

        JScrollPane roomScroll = new JScrollPane(roomListPanel);
        roomScroll.setBorder(BorderFactory.createEmptyBorder());
        roomScroll.setOpaque(false);
        roomScroll.getViewport().setOpaque(false);
        roomScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        roomScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        roomScroll.getVerticalScrollBar().setUnitIncrement(16);

        rightSide.add(roomScroll, "grow, push");

        checkoutMultiBtn.setBackground(new Color(40, 167, 69));
        checkoutMultiBtn.setForeground(Color.WHITE);
        checkoutMultiBtn.addActionListener(evt -> {
            if (selectedRooms.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất 1 phòng để thanh toán!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String firstInvoiceId = selectedRooms.get(0).id;
            for (CheckoutData cd : selectedRooms) {
                if (!cd.id.equals(firstInvoiceId)) {
                    JOptionPane.showMessageDialog(this, "Chỉ có thể thanh toán nhiều phòng thuộc cùng 1 hóa đơn (cùng Mã HD)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            proceedCheckoutMultiRooms();
        });
        rightSide.add(checkoutMultiBtn, "h 44!, growx, gapy 10 0");

        panel.add(filterCard, "growy, top");
        panel.add(rightSide, "grow, push");

        return panel;
    }

    private JLabel makeFilterLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(50, 65, 85));
        l.setFont(l.getFont().deriveFont(12f));
        return l;
    }

    private JPanel makeTextInput(JTextField field) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(250, 251, 252));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 231, 245), 1),
                BorderFactory.createEmptyBorder(0, 10, 0, 10)
        ));
        field.setOpaque(false);
        field.setBorder(BorderFactory.createEmptyBorder());
        field.setForeground(new Color(30, 45, 65));
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void renderRooms(List<CheckoutData> list) {
        roomListPanel.removeAll();

        for (CheckoutData data : list) {
            roomListPanel.add(createRoomCard(data), "growx");
        }

        roomListPanel.revalidate();
        roomListPanel.repaint();
    }

    private JPanel createRoomCard(CheckoutData data) {
        RoundedPanel card = new RoundedPanel(16, Color.WHITE, new Color(230, 235, 245), 1.5f);
        card.setLayout(new MigLayout("wrap 1,insets 16,gap 8", "[grow,fill]", "[]"));

        JPanel header = new JPanel(new MigLayout("insets 0", "[grow,fill][]", "[]"));
        header.setOpaque(false);

        JPanel tBox = new JPanel(new MigLayout("insets 0,wrap 1", "[]", "[][]"));
        tBox.setOpaque(false);

        JLabel dId = new JLabel(data.id);
        dId.setFont(dId.getFont().deriveFont(Font.BOLD, 12f));
        dId.setForeground(new Color(24, 40, 66));

        JLabel dRoom = new JLabel(data.roomName);
        dRoom.setForeground(new Color(100, 115, 135));
        dRoom.setFont(dRoom.getFont().deriveFont(12f));

        tBox.add(dId);
        tBox.add(dRoom);

        JPanel badge = createStatusBadge(data.statusText, data.statusColor);

        header.add(tBox);
        header.add(badge, "aligny top");

        JPanel divider = new JPanel();
        divider.setBackground(new Color(240, 243, 248));

        JLabel rName = new JLabel(data.customerName);
        rName.setFont(rName.getFont().deriveFont(Font.BOLD, 13f));
        rName.setForeground(new Color(24, 40, 66));

        JLabel rPhone = new JLabel(data.phone);
        rPhone.setForeground(new Color(110, 125, 145));
        rPhone.setFont(rPhone.getFont().deriveFont(12f));

        JLabel rDate = new JLabel(data.expectedIn + " \u2013 " + data.expectedOut);
        rDate.setForeground(new Color(110, 125, 145));
        rDate.setFont(rDate.getFont().deriveFont(12f));

        JPanel footer = new JPanel(new MigLayout("insets 0", "[grow,fill][]", "[]"));
        footer.setOpaque(false);

        JLabel price = new JLabel(data.price);
        price.setFont(price.getFont().deriveFont(Font.BOLD, 15f));
        price.setForeground(new Color(24, 40, 66));

        javax.swing.JCheckBox chkBox = new javax.swing.JCheckBox("Chọn phòng");
        chkBox.setOpaque(false);
        chkBox.setFont(chkBox.getFont().deriveFont(Font.BOLD, 13f));
        chkBox.setForeground(new Color(49, 106, 210));
        chkBox.setSelected(selectedRooms.contains(data));

        boolean daTra = "Đã trả".equalsIgnoreCase(data.statusText);
        chkBox.setEnabled(!daTra);

        chkBox.addActionListener(evt -> {
            if (chkBox.isSelected()) {
                if (!selectedRooms.contains(data)) {
                    selectedRooms.add(data);
                }
            } else {
                selectedRooms.remove(data);
            }
            checkoutMultiBtn.setText("Thanh toán các phòng đã chọn (" + selectedRooms.size() + ")");
        });

        footer.add(price, "aligny center");
        footer.add(chkBox, "h 36!");

        card.add(header, "growx");
        card.add(divider, "h 1!, growx, gapy 6 6");
        card.add(rName);
        card.add(rPhone);
        card.add(rDate);
        card.add(footer, "growx, gapy 12 0");

        return card;
    }

    private JPanel createStatusBadge(String text, Color bg) {
        JPanel p = new RoundedPanel(12, bg, bg, 1f);
        p.setLayout(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 11f));

        p.add(l, BorderLayout.CENTER);
        return p;
    }

    private JPanel createStep2View() {
        JPanel panel = new JPanel(new MigLayout(
                "insets 16 24 20 24, gap 20, fill",
                "[360!][grow,fill]",
                "[][grow,fill]"
        ));
        panel.setOpaque(false);

        PrimaryButton backBtn = new PrimaryButton("\u2190 Quay lại danh sách lưu trú");
        backBtn.setBackground(new Color(245, 248, 252));
        backBtn.setForeground(new Color(60, 80, 110));
        backBtn.setBorder(BorderFactory.createEmptyBorder());
        backBtn.setHorizontalAlignment(SwingConstants.LEFT);
        backBtn.addActionListener(e -> {
            setStep(1);
            mainCards.show(contentPanel, "step1");
        });

        panel.add(backBtn, "span 2, wrap");

        RoundedPanel leftPanel = new RoundedPanel(16, Color.WHITE, new Color(225, 231, 245), 1f);
        leftPanel.setLayout(new MigLayout("wrap 1,insets 16", "[grow,fill]", "[]"));

        JLabel t1 = new JLabel("Thanh toán & trả phòng");
        t1.setFont(t1.getFont().deriveFont(Font.BOLD, 16f));
        t1.setForeground(new Color(49, 106, 210));
        leftPanel.add(t1, "gapy 0 12");

        RoundedPanel pBox = new RoundedPanel(12, new Color(246, 249, 255), new Color(225, 235, 255), 1f);
        pBox.setLayout(new MigLayout("wrap 2,insets 14", "[grow,fill][grow,fill]", "[]"));

        detailNameLabel.setFont(detailNameLabel.getFont().deriveFont(Font.BOLD, 14f));
        pBox.add(detailNameLabel, "span 2");
        pBox.add(makeSmallLabel("Phòng"), "gapy 6 0");
        pBox.add(makeSmallLabel("Giá phòng"), "gapy 6 0");
        pBox.add(detailRoomLabel);
        pBox.add(detailRoomPriceLabel);
        pBox.add(makeSmallLabel("Ngày nhận dự kiến"), "gapy 6 0");
        pBox.add(makeSmallLabel("Ngày trả dự kiến"), "gapy 6 0");
        pBox.add(detailDateInLabel);
        pBox.add(detailDateOutLabel);
        leftPanel.add(pBox);

        JLabel kmTitle = new JLabel("Mã khuyến mãi");
        kmTitle.setForeground(new Color(100, 115, 135));
        leftPanel.add(kmTitle, "gapy 10 6");

        promotionCombo.setBackground(Color.WHITE);
        promotionCombo.setFocusable(false);
        promotionCombo.addActionListener(e -> {
            if (promotionCombo.getSelectedItem() == null) {
                selectedPromotionCode = null;
            } else {
                String display = promotionCombo.getSelectedItem().toString();
                selectedPromotionCode = promotionDisplayToCode.get(display);
            }
            refreshInvoicePreview();
        });
        leftPanel.add(promotionCombo, "h 36!");

        JLabel tCost = new JLabel("Tổng thanh toán");
        tCost.setForeground(new Color(100, 115, 135));
        leftPanel.add(tCost, "gapy 10 6");

        RoundedPanel costBox = new RoundedPanel(12, Color.WHITE, new Color(225, 231, 245), 1f);
        costBox.setLayout(new MigLayout("wrap 2,insets 14", "[grow,fill][]", "[]"));

        JLabel lTotal = new JLabel("Tổng chi phí");
        lTotal.setFont(lTotal.getFont().deriveFont(Font.BOLD, 14f));
        lTotal.setForeground(new Color(24, 100, 210));
        costBox.add(lTotal, "span 2, gapy 0 6");

        costBox.add(new JLabel("Tiền phòng"));

        detailTotalRoomLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        detailTotalRoomLabel.setFont(detailTotalRoomLabel.getFont().deriveFont(Font.BOLD, 13f));
        costBox.add(detailTotalRoomLabel);

        JLabel svc = new JLabel("Dịch vụ phát sinh");
        svc.setForeground(new Color(110, 125, 145));
        costBox.add(svc, "gapy 4 0");

        detailTotalServiceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        detailTotalServiceLabel.setForeground(new Color(110, 125, 145));
        costBox.add(detailTotalServiceLabel, "gapy 4 0");

        JLabel surcharge = new JLabel("Phụ thu");
        surcharge.setForeground(new Color(110, 125, 145));
        costBox.add(surcharge, "gapy 4 0");

        detailSurchargeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        detailSurchargeLabel.setForeground(new Color(220, 38, 38));
        detailSurchargeLabel.setFont(detailSurchargeLabel.getFont().deriveFont(Font.BOLD, 13f));
        costBox.add(detailSurchargeLabel, "gapy 4 0");

        JLabel vat = new JLabel("Thuế VAT (10%)");
        vat.setForeground(new Color(110, 125, 145));
        costBox.add(vat, "gapy 4 0");

        detailTaxLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        detailTaxLabel.setForeground(new Color(110, 125, 145));
        costBox.add(detailTaxLabel, "gapy 4 0");

        JLabel km = new JLabel("Khuyến mãi");
        km.setForeground(new Color(110, 125, 145));
        costBox.add(km, "gapy 4 0");

        detailDiscountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        detailDiscountLabel.setForeground(new Color(40, 167, 69));
        detailDiscountLabel.setFont(detailDiscountLabel.getFont().deriveFont(Font.BOLD, 13f));
        costBox.add(detailDiscountLabel, "gapy 4 0");

        JLabel penalty = new JLabel("Tiền phạt trả trễ");
        penalty.setForeground(new Color(110, 125, 145));
        costBox.add(penalty, "gapy 4 0");

        detailPenaltyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        detailPenaltyLabel.setForeground(new Color(220, 38, 38));
        detailPenaltyLabel.setFont(detailPenaltyLabel.getFont().deriveFont(Font.BOLD, 13f));
        costBox.add(detailPenaltyLabel, "gapy 4 0");

        JLabel depositTitle = new JLabel("Tiền cọc còn lại");
        depositTitle.setForeground(new Color(110, 125, 145));
        costBox.add(depositTitle, "gapy 4 0");

        detailDepositLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        detailDepositLabel.setForeground(new Color(40, 167, 69));
        detailDepositLabel.setFont(detailDepositLabel.getFont().deriveFont(Font.BOLD, 13f));
        costBox.add(detailDepositLabel, "gapy 4 0");

        JPanel divider = new JPanel();
        divider.setBackground(new Color(230, 235, 245));
        costBox.add(divider, "span 2, growx, h 1!, gapy 8 8");

        JLabel fnTitle = new JLabel("Tổng thanh toán");
        fnTitle.setFont(fnTitle.getFont().deriveFont(Font.BOLD, 14f));
        costBox.add(fnTitle);

        detailTotalFinalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        detailTotalFinalLabel.setFont(detailTotalFinalLabel.getFont().deriveFont(Font.BOLD, 16f));
        detailTotalFinalLabel.setForeground(new Color(220, 38, 38));
        costBox.add(detailTotalFinalLabel);

        leftPanel.add(costBox);

        JLabel ts = new JLabel("Trạng thái phòng sau khi trả");
        ts.setForeground(new Color(100, 115, 135));
        leftPanel.add(ts, "gapy 10 6");

        JPanel sBox = new JPanel(new MigLayout("insets 0,gap 10", "[grow,fill][grow,fill]", "[]"));
        sBox.setOpaque(false);

        PrimaryButton sEmpty = new PrimaryButton("Trống");
        PrimaryButton sMaint = new PrimaryButton("Bảo trì");

        sEmpty.addActionListener(e -> {
            nextRoomStatus = "Trong";
            sEmpty.setBackground(new Color(40, 167, 69));
            sEmpty.setForeground(Color.WHITE);
            sEmpty.setBorder(BorderFactory.createEmptyBorder());

            sMaint.setBackground(Color.WHITE);
            sMaint.setForeground(new Color(110, 125, 145));
            sMaint.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 235), 2));
        });

        sMaint.addActionListener(e -> {
            nextRoomStatus = "Bảo trì";
            sMaint.setBackground(new Color(40, 167, 69));
            sMaint.setForeground(Color.WHITE);
            sMaint.setBorder(BorderFactory.createEmptyBorder());

            sEmpty.setBackground(Color.WHITE);
            sEmpty.setForeground(new Color(110, 125, 145));
            sEmpty.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 235), 2));
        });

        sEmpty.doClick();

        sBox.add(sEmpty, "h 38!");
        sBox.add(sMaint, "h 38!");
        leftPanel.add(sBox);

        JPanel actBox = new JPanel(new MigLayout("insets 0,gap 10", "[grow,fill][grow,fill]", "[]"));
        actBox.setOpaque(false);

        PrimaryButton saveBtn = new PrimaryButton("Lưu hóa đơn");
        PrimaryButton printBtn = new PrimaryButton("In hóa đơn");

        printBtn.addActionListener(e -> {
            isPrintInvoice = !isPrintInvoice;
            if (isPrintInvoice) {
                printBtn.setBackground(new Color(40, 167, 69));
                printBtn.setForeground(Color.WHITE);
                printBtn.setBorder(BorderFactory.createEmptyBorder());
            } else {
                printBtn.setBackground(new Color(245, 248, 252));
                printBtn.setForeground(new Color(50, 70, 90));
                printBtn.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 225), 2));
            }
        });

        saveBtn.setBackground(new Color(40, 167, 69));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBorder(BorderFactory.createEmptyBorder());
        isSaveInvoice = true;

        printBtn.setBackground(new Color(245, 248, 252));
        printBtn.setForeground(new Color(50, 70, 90));
        printBtn.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 225), 2));
        isPrintInvoice = false;

        actBox.add(saveBtn, "h 40!");
        actBox.add(printBtn, "h 40!");

        PrimaryButton submitBtn = new PrimaryButton("Xác nhận thanh toán & trả phòng");
        submitBtn.setBackground(ThemeColors.SUCCESS);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.addActionListener(e -> {
            CheckoutBUS.CheckoutTotals totals =
                    checkoutBUS.previewTotals(currentHoaDon, currentRoomCodes, selectedPromotionCode);

            double remainingDeposit = checkoutBUS.getRemainingDepositForCheckout(
                    currentHoaDon,
                    currentRoomCodes
            );

            double amountToPay = Math.max(0, totals.total - remainingDeposit);

            String[] options = {"Tiền mặt", "QR Code", "Hủy"};

            int choice = JOptionPane.showOptionDialog(
                    this,
                    "Số tiền cần thanh toán: " + CurrencyUtils.formatVND(amountToPay)
                            + "\n\nChọn phương thức thanh toán:",
                    "Phương thức thanh toán",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (choice == 2 || choice == JOptionPane.CLOSED_OPTION) {
                return;
            }

            if (choice == 1) {
                showQrPayment(currentHoaDon.getMaHD(), amountToPay);

                int confirmQr = JOptionPane.showConfirmDialog(
                        this,
                        "Khách đã chuyển khoản thành công chưa?",
                        "Xác nhận thanh toán QR",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (confirmQr != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            String paymentMethod = (choice == 1) ? "ChuyenKhoan" : "TienMat";

            boolean paymentSuccess = checkoutBUS.createCheckoutPayment(
                    currentHoaDon,
                    amountToPay,
                    paymentMethod,
                    currentHoaDon.getMaNhanVien()
            );

            boolean success = paymentSuccess && checkoutBUS.completeCheckout(
                    currentHoaDon,
                    currentRoomCodes,
                    nextRoomStatus,
                    selectedPromotionCode
            );

            if (success) {
                refreshInvoicePreview();

                if (isPrintInvoice) {
                    PDFInvoiceGenerator.exportInvoice(currentHoaDon, new java.util.ArrayList<>(), new java.util.ArrayList<>());
                }

                JOptionPane.showMessageDialog(this, "Trả phòng thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                
                // Refresh RoomManagementPanel data
                java.awt.Window win = javax.swing.SwingUtilities.getWindowAncestor(this);
                if (win instanceof kqlhotel.gui.AppFrame) {
                    ((kqlhotel.gui.AppFrame) win).refreshRoomManagementData();
                }

                roomCodeField.setText("");
                customerIdField.setText("");
                customerNameField.setText("");
                selectedRooms.clear();
                checkoutMultiBtn.setText("Thanh toán các phòng đã chọn (0)");
                renderRooms(checkoutBUS.getRoomsDueToday());

                setStep(1);
                mainCards.show(contentPanel, "step1");
            } else {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi cập nhật DB!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        leftPanel.add(actBox, "gapy 10 0");
        leftPanel.add(submitBtn, "h 42!, gapy 8 0");

        RoundedPanel rightPanel = new RoundedPanel(0, PAGE_BG, PAGE_BG, 0f);
        rightPanel.setLayout(new MigLayout("wrap 1,insets 0", "[grow,fill]", "[]"));

        JLabel iTitle = new JLabel("Thông tin lưu trú");
        iTitle.setFont(iTitle.getFont().deriveFont(Font.BOLD, 16f));
        iTitle.setForeground(new Color(24, 40, 66));
        rightPanel.add(iTitle, "gapy 8 16");

        RoundedPanel kvBox = new RoundedPanel(12, Color.WHITE, new Color(225, 231, 245), 1.5f);
        kvBox.setLayout(new MigLayout("wrap 2,insets 20, gap 15", "[grow,fill][grow,fill]", "[]"));

        kvBox.add(makeSmallLabel("Khách hàng"));
        kvBox.add(makeSmallLabel("Phòng hiện tại"));

        kName.setFont(kName.getFont().deriveFont(Font.BOLD, 14f));
        kRoom.setFont(kRoom.getFont().deriveFont(Font.BOLD, 14f));
        kvBox.add(kName);
        kvBox.add(kRoom);

        kvBox.add(makeSmallLabel("Ngày nhận phòng thực tế"), "gapy 10 0");
        kvBox.add(makeSmallLabel("Ngày trả phòng thực tế"), "gapy 10 0");

        kDateIn.setFont(kDateIn.getFont().deriveFont(Font.BOLD, 14f));
        kDateOut.setFont(kDateOut.getFont().deriveFont(Font.BOLD, 14f));

        kvBox.add(kDateIn);
        kvBox.add(kDateOut);

        kvBox.add(makeSmallLabel("Mã khách hàng (CCCD/ID)"), "gapy 10 0");
        kvBox.add(makeSmallLabel("Trạng thái"), "gapy 10 0");

        kCID.setFont(kCID.getFont().deriveFont(Font.BOLD, 14f));

        JLabel kStatusLabel = new JLabel("Đang lưu trú");
        kStatusLabel.setFont(kStatusLabel.getFont().deriveFont(Font.BOLD, 14f));
        kStatusLabel.setForeground(ThemeColors.SUCCESS);

        kvBox.add(kCID);
        kvBox.add(kStatusLabel);

        rightPanel.add(kvBox);

        JScrollPane leftScroll = new JScrollPane(leftPanel);
        leftScroll.setBorder(BorderFactory.createEmptyBorder());
        leftScroll.setOpaque(false);
        leftScroll.getViewport().setOpaque(false);
        leftScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        leftScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        leftScroll.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(leftScroll, "grow, push");
        panel.add(rightPanel, "grow, aligny top");

        return panel;
    }

    private void loadPromotionOptions() {
        promotionDisplayToCode.clear();
        promotionCombo.removeAllItems();

        String noneText = "Không áp dụng khuyến mãi";
        promotionCombo.addItem(noneText);
        promotionDisplayToCode.put(noneText, null);

        CheckoutBUS.CheckoutTotals totals =
                checkoutBUS.previewTotals(currentHoaDon, currentRoomCodes, null);

        double amountBeforeDiscount =
                totals.roomFee + totals.serviceFee + totals.surcharge + totals.tax + totals.earlyCheckoutPenalty;

        List<Promotion> promotions = checkoutBUS.getAvailablePromotions();

        for (Promotion km : promotions) {
            double minAmount = km.getDieuKienApDung();

            if (amountBeforeDiscount <= minAmount) {
                continue;
            }

            String valueText;

            if ("TheoPhanTram".equals(km.getLoaiKM())) {
                valueText = removeDecimalZero(km.getTienKhuyenMai()) + "%";
            } else {
                valueText = CurrencyUtils.formatVND(km.getTienKhuyenMai());
            }

            String display = km.getMaKM() + " - " + km.getTenKM() + " (-" + valueText + ")";
            promotionCombo.addItem(display);
            promotionDisplayToCode.put(display, km.getMaKM());
        }

        String bestDisplay = findBestPromotionDisplay();
        if (bestDisplay != null) {
            promotionCombo.setSelectedItem(bestDisplay);
            selectedPromotionCode = promotionDisplayToCode.get(bestDisplay);
        } else {
            promotionCombo.setSelectedIndex(0);
            selectedPromotionCode = null;
        }
    }

    private void refreshInvoicePreview() {
        if (currentHoaDon == null) {
            return;
        }

        CheckoutBUS.CheckoutTotals totals =
                checkoutBUS.previewTotals(currentHoaDon, currentRoomCodes, selectedPromotionCode);

        double deposit = checkoutBUS.getRemainingDepositForCheckout(
                currentHoaDon,
                currentRoomCodes
        );

        double finalPay = Math.max(0, totals.total - deposit);

        detailTotalRoomLabel.setText(CurrencyUtils.formatVND(totals.roomFee));
        detailTotalServiceLabel.setText(CurrencyUtils.formatVND(totals.serviceFee));
        detailSurchargeLabel.setText(CurrencyUtils.formatVND(totals.surcharge));
        detailTaxLabel.setText(CurrencyUtils.formatVND(totals.tax));
        detailDiscountLabel.setText("-" + CurrencyUtils.formatVND(totals.discount));

        // dòng mới
        detailPenaltyLabel.setText(CurrencyUtils.formatVND(totals.earlyCheckoutPenalty));
        detailDepositLabel.setText("-" + CurrencyUtils.formatVND(deposit));

        // tổng cuối đã trừ cọc
        detailTotalFinalLabel.setText(CurrencyUtils.formatVND(finalPay));

        if (totals.surcharge > 0) {
            detailSurchargeLabel.setForeground(new Color(220, 38, 38));
        } else {
            detailSurchargeLabel.setForeground(new Color(110, 125, 145));
        }

        if (totals.earlyCheckoutPenalty > 0) {
            detailPenaltyLabel.setForeground(new Color(220, 38, 38));
        } else {
            detailPenaltyLabel.setForeground(new Color(110, 125, 145));
        }

        if (deposit > 0) {
            detailDepositLabel.setForeground(new Color(40, 167, 69));
        } else {
            detailDepositLabel.setForeground(new Color(110, 125, 145));
        }

        if (totals.discount > 0) {
            detailDiscountLabel.setForeground(new Color(40, 167, 69));
        } else {
            detailDiscountLabel.setForeground(new Color(110, 125, 145));
        }
    }

    private JLabel makeSmallLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(110, 125, 145));
        l.setFont(l.getFont().deriveFont(12f));
        return l;
    }

    private void proceedCheckoutMultiRooms() {
        if (selectedRooms.isEmpty()) {
            return;
        }

        CheckoutData firstData = selectedRooms.get(0);

        String roomCode = firstData.roomName.split(" · ")[0].replace("Phòng ", "").trim();
        this.currentHoaDon = checkoutBUS.getInvoiceForCheckout(roomCode);

        if (currentHoaDon == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn chưa thanh toán cho các phòng này!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentRoomCodes.clear();

        List<String> roomNames = new java.util.ArrayList<>();
        for (CheckoutData cd : selectedRooms) {
            String rc = cd.roomName.split(" · ")[0].replace("Phòng ", "").trim();
            currentRoomCodes.add(rc);
            roomNames.add(cd.roomName);
        }

        detailNameLabel.setText(firstData.customerName);
        detailRoomLabel.setText(String.join(", ", currentRoomCodes));
        detailRoomLabel.setFont(detailRoomLabel.getFont().deriveFont(Font.BOLD, 13f));

        detailRoomPriceLabel.setText("Theo hóa đơn (" + selectedRooms.size() + " phòng)");
        detailRoomPriceLabel.setFont(detailRoomPriceLabel.getFont().deriveFont(Font.BOLD, 13f));

        detailDateInLabel.setText(firstData.expectedIn);
        detailDateInLabel.setFont(detailDateInLabel.getFont().deriveFont(Font.BOLD, 13f));

        detailDateOutLabel.setText(firstData.expectedOut);
        detailDateOutLabel.setFont(detailDateOutLabel.getFont().deriveFont(Font.BOLD, 13f));

        kName.setText(firstData.customerName + " (" + firstData.phone + ")");
        kRoom.setText(String.join(" | ", roomNames));
        kDateIn.setText(firstData.actualIn);
        kDateOut.setText(java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        ));
        kCID.setText(firstData.id);

        loadPromotionOptions();
        refreshInvoicePreview();

        setStep(2);
        mainCards.show(contentPanel, "step2");
    }

    private void setStep(int s) {
        if (step1Label.getParent() == null || step2Label.getParent() == null) {
            return;
        }

        if (s == 1) {
            step1Label.setText(" 1    Tìm lưu trú ");
            step1Label.setForeground(Color.WHITE);
            step1Label.setIcon(null);
            step1Label.getParent().setBackground(new Color(18, 35, 67));
            ((JPanel) step1Label.getParent()).setOpaque(true);

            step2Label.setText(" 2    Thanh toán & trả phòng ");
            step2Label.setForeground(new Color(150, 160, 175));
            step2Label.setIcon(null);
            step2Label.getParent().setBackground(Color.WHITE);
            ((JPanel) step2Label.getParent()).setOpaque(false);
        } else {
            step1Label.setText(" 1    Tìm lưu trú ");
            step1Label.setForeground(new Color(24, 40, 66));
            step1Label.setIcon(loadIcon("check-circle.png", 18, 18));
            step1Label.getParent().setBackground(Color.WHITE);
            ((JPanel) step1Label.getParent()).setOpaque(false);

            step2Label.setText(" 2    Thanh toán & trả phòng ");
            step2Label.setForeground(Color.WHITE);
            step2Label.setIcon(null);
            step2Label.getParent().setBackground(ThemeColors.PRIMARY);
            ((JPanel) step2Label.getParent()).setOpaque(true);
        }
    }

    private ImageIcon loadIcon(String filename, int w, int h) {
        try {
            URL resource = getClass().getResource("/kqlhotel/resources/icons/" + filename);
            if (resource == null) {
                java.io.File file = new java.io.File("src/kqlhotel/resources/icons/" + filename);
                if (file.exists()) {
                    resource = file.toURI().toURL();
                }
            }

            if (resource != null) {
                ImageIcon icon = new ImageIcon(resource);
                return new ImageIcon(icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String findBestPromotionDisplay() {
        if (currentHoaDon == null) {
            return null;
        }

        CheckoutBUS.CheckoutTotals totals =
                checkoutBUS.previewTotals(currentHoaDon, currentRoomCodes, null);

        double amountBeforeDiscount =
                totals.roomFee + totals.serviceFee + totals.surcharge + totals.tax;

        String bestDisplay = null;
        double bestEffectiveDiscount = 0;

        List<Promotion> promotions = checkoutBUS.getAvailablePromotions();

        for (Promotion km : promotions) {
            if (km == null) continue;

            double minAmount = km.getDieuKienApDung();

            if (amountBeforeDiscount <= minAmount) {
                continue;
            }

            double effectiveDiscount;

            if ("TheoPhanTram".equals(km.getLoaiKM())) {
                effectiveDiscount = amountBeforeDiscount * km.getTienKhuyenMai() / 100.0;

                if (km.getGiaTriToiDa() > 0) {
                    effectiveDiscount = Math.min(effectiveDiscount, km.getGiaTriToiDa());
                }
            } else {
                effectiveDiscount = km.getTienKhuyenMai();
            }

            effectiveDiscount = Math.min(effectiveDiscount, amountBeforeDiscount);

            String valueText;
            if ("TheoPhanTram".equals(km.getLoaiKM())) {
                valueText = removeDecimalZero(km.getTienKhuyenMai()) + "%";
            } else {
                valueText = CurrencyUtils.formatVND(km.getTienKhuyenMai());
            }

            String display = km.getMaKM() + " - " + km.getTenKM() + " (-" + valueText + ")";

            if (effectiveDiscount > bestEffectiveDiscount) {
                bestEffectiveDiscount = effectiveDiscount;
                bestDisplay = display;
            }
        }

        return bestDisplay;
    }

    public static class CheckoutData {
        public String id, roomName, customerName, phone;

        public String expectedIn, expectedOut;
        public String actualIn, actualOut;

        public String price, statusText;
        public Color statusColor;

        public CheckoutData(String id, String roomName, String name, String phone,
                            String expectedIn, String expectedOut,
                            String actualIn, String actualOut,
                            String price, String st, Color col) {
            this.id = id;
            this.roomName = roomName;
            this.customerName = name;
            this.phone = phone;
            this.expectedIn = expectedIn;
            this.expectedOut = expectedOut;
            this.actualIn = actualIn;
            this.actualOut = actualOut;
            this.price = price;
            this.statusText = st;
            this.statusColor = col;
        }
    }
    private String removeDecimalZero(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
    private void showQrPayment(String maHD, double amount) {
        try {
            String bankId = "970422";        // MB Bank
            String accountNo = "0868465911";
            String accountName = "NGUYEN KHA LUAN";

            String amountStr = String.valueOf((long) amount);

            String qrUrl = "https://img.vietqr.io/image/"
                    + bankId + "-" + accountNo + "-compact2.png"
                    + "?amount=" + amountStr
                    + "&addInfo=" + java.net.URLEncoder.encode(maHD, "UTF-8")
                    + "&accountName=" + java.net.URLEncoder.encode(accountName, "UTF-8");

            java.net.URL url = new java.net.URL(qrUrl);
            ImageIcon icon = new ImageIcon(url);

            JLabel label = new JLabel(icon);

            JOptionPane.showMessageDialog(
                    this,
                    label,
                    "Quét mã QR thanh toán: " + CurrencyUtils.formatVND(amount),
                    JOptionPane.PLAIN_MESSAGE
            );

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tạo QR thanh toán!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private double parseMinimumAmount(String condition) {
        if (condition == null || condition.isBlank()) {
            return 0;
        }

        try {
            return Double.parseDouble(condition.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}