package kqlhotel.gui.tabs;

import java.awt.*;
import java.net.URL;
import java.util.List;
import javax.swing.*;
import kqlhotel.bus.promotion.PromotionsBUS;
import kqlhotel.entity.Promotion;
import kqlhotel.gui.components.PrimaryButton;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.gui.theme.ThemeColors;
import kqlhotel.utils.CurrencyUtils;
import kqlhotel.utils.DateUtils;
import net.miginfocom.swing.MigLayout;

public class PromotionsPanel extends JPanel {
    private static final Color PAGE_BG = new Color(245, 248, 252);
    private static final Color NAVY = new Color(24, 34, 52);
    private static final Color GOLD = new Color(196, 142, 45);
    private static final Color SOFT_GOLD = new Color(255, 250, 242);

    private final JPanel listPanel = new JPanel(
            new MigLayout("wrap 3,insets 0,gap 22", "[grow,fill][grow,fill][grow,fill]", "[]")
    );

    private final PromotionsBUS promotionsBUS = new PromotionsBUS();
    private List<Promotion> currentList;
    private final JLabel summaryLabel = new JLabel();
    private final List<PrimaryButton> filterButtons = new java.util.ArrayList<>();

    private Image bgImage;

    public PromotionsPanel() {
        setOpaque(false);
        setBackground(PAGE_BG);
        setLayout(new BorderLayout());

        bgImage = loadRawImage("/kqlhotel/resources/icons/promo_bg.png");

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(createHeroBanner(), BorderLayout.NORTH);
        topContainer.add(createFilterBar(), BorderLayout.SOUTH);

        listPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 24, 16, 24));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(20);

        add(topContainer, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        refreshData();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            g2.setColor(new Color(255, 255, 255, 205));
            g2.fillRect(0, 0, getWidth(), getHeight());
        } else {
            GradientPaint gp = new GradientPaint(
                    0, 0, new Color(255, 250, 242),
                    getWidth(), getHeight(), new Color(238, 244, 252)
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        g2.dispose();
    }

    private void refreshData() {
        this.currentList = promotionsBUS.getAllPromotions();
        updateSummaryLabel();
        updateFilterButtonStyles("Tất cả");
        renderList(currentList);
    }

    private void filterData(String status) {
        this.currentList = promotionsBUS.filterPromotions(status);
        updateFilterButtonStyles(status);
        renderList(currentList);
    }

    private void updateSummaryLabel() {
        int[] counts = promotionsBUS.getPromotionsCount();
        summaryLabel.setText(String.format("%d chương trình · %d đang áp dụng", counts[0], counts[1]));

        if (filterButtons.size() >= 4) {
            filterButtons.get(0).setText("Tất cả (" + counts[0] + ")");
            filterButtons.get(1).setText("Đang áp dụng (" + counts[1] + ")");
            filterButtons.get(2).setText("Sắp diễn ra (" + counts[2] + ")");
            filterButtons.get(3).setText("Đã hết hạn (" + counts[3] + ")");
        }
    }

    private JPanel createHeroBanner() {
        RoundedPanel hero = new RoundedPanel(
                24,
                new Color(24, 34, 52, 235),
                new Color(255, 215, 120, 120),
                1.3f
        );

        hero.setLayout(new MigLayout(
                "insets 22 28 22 28, fill",
                "[grow,fill][]",
                "[]"
        ));

        JPanel textBox = new JPanel(new MigLayout("wrap 1,insets 0,gap 4", "[grow,fill]", "[]"));
        textBox.setOpaque(false);

        JLabel title = new JLabel("Ưu đãi & Khuyến mãi");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(new Color(255, 215, 120));

        summaryLabel.setForeground(new Color(235, 240, 250));
        summaryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel sub = new JLabel("Quản lý các chương trình ưu đãi dành cho khách hàng KQL HOTEL");
        sub.setForeground(new Color(210, 218, 232));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        textBox.add(title);
        textBox.add(summaryLabel);
        textBox.add(sub);

        PrimaryButton btnAdd = new PrimaryButton("+ Tạo khuyến mãi");
        btnAdd.setBackground(new Color(255, 193, 7));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setArc(16);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.addActionListener(e -> {
            new PromotionDialog(
                    SwingUtilities.getWindowAncestor(this),
                    null,
                    this::refreshData
            ).setVisible(true);
        });

        hero.add(textBox, "grow");
        hero.add(btnAdd, "w 170!, h 42!, aligny center");

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(16, 24, 10, 24));
        wrap.add(hero, BorderLayout.CENTER);

        return wrap;
    }

    private JPanel createFilterBar() {
        JPanel bar = new JPanel(new MigLayout(
                "insets 6 24 16 24, gap 10",
                "[][][][][grow][]",
                "[]"
        ));
        bar.setOpaque(false);

        bar.add(createFilterBtn("Tất cả", true, e -> filterData("Tất cả")), "h 40!");
        bar.add(createFilterBtn("Đang áp dụng", false, e -> filterData("Đang áp dụng")), "h 40!");
        bar.add(createFilterBtn("Sắp diễn ra", false, e -> filterData("Sắp diễn ra")), "h 40!");
        bar.add(createFilterBtn("Đã hết hạn", false, e -> filterData("Đã hết hạn")), "h 40!");

        return bar;
    }

    private PrimaryButton createFilterBtn(String text, boolean active, java.awt.event.ActionListener al) {
        PrimaryButton btn = new PrimaryButton(text);
        btn.setArc(20);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (active) {
            btn.setBackground(NAVY);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(100, 115, 135));
            btn.setBorder(BorderFactory.createLineBorder(new Color(225, 231, 245), 1));
        }

        btn.addActionListener(al);
        filterButtons.add(btn);
        return btn;
    }

    private void updateFilterButtonStyles(String activeText) {
        for (PrimaryButton btn : filterButtons) {
            if (btn.getText().startsWith(activeText)) {
                btn.setBackground(NAVY);
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(Color.WHITE);
                btn.setForeground(new Color(100, 115, 135));
                btn.setBorder(BorderFactory.createLineBorder(new Color(225, 231, 245), 1));
            }
        }
    }

    private void renderList(List<Promotion> list) {
        listPanel.removeAll();

        if (list == null || list.isEmpty()) {
            JLabel empty = new JLabel("Không có chương trình khuyến mãi phù hợp", SwingConstants.CENTER);
            empty.setFont(new Font("Segoe UI", Font.BOLD, 16));
            empty.setForeground(new Color(100, 115, 135));
            listPanel.add(empty, "span 3, growx, h 160!");
        } else {
            for (Promotion data : list) {
                listPanel.add(createPromoCard(data), "growx");
            }
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createPromoCard(Promotion km) {
        String statusText;
        Color statusColor;
        Color cardBorder;

        if ("DangHoatDong".equals(km.getTrangThaiKM())) {
            statusText = "Đang áp dụng";
            statusColor = ThemeColors.SUCCESS;
            cardBorder = new Color(215, 170, 90);
        } else if ("SapDienRa".equals(km.getTrangThaiKM())) {
            statusText = "Sắp diễn ra";
            statusColor = new Color(49, 106, 210);
            cardBorder = new Color(120, 165, 235);
        } else {
            statusText = "Đã hết hạn";
            statusColor = new Color(150, 150, 150);
            cardBorder = new Color(210, 210, 210);
        }

        RoundedPanel card = new RoundedPanel(
                22,
                SOFT_GOLD,
                cardBorder,
                1.5f
        );

        card.setHoverEffectEnabled(true);
        card.setLayout(new MigLayout(
                "wrap 1,insets 20,gap 10",
                "[grow,fill]",
                "[]"
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel topBox = new JPanel(new MigLayout("insets 0", "[][grow,fill][]", "[]"));
        topBox.setOpaque(false);

        RoundedPanel iconBox = new RoundedPanel(
                14,
                new Color(255, 245, 220),
                new Color(230, 185, 95),
                1.2f
        );
        iconBox.setLayout(new BorderLayout());
        iconBox.setPreferredSize(new Dimension(48, 48));

        String iconText = "TheoPhanTram".equals(km.getLoaiKM()) ? "%" : "₫";

        JLabel iconL = new JLabel(iconText, SwingConstants.CENTER);
        iconL.setForeground(GOLD);
        iconL.setFont(new Font("Segoe UI", Font.BOLD, 24));
        iconBox.add(iconL, BorderLayout.CENTER);

        JPanel titleBox = new JPanel(new MigLayout("insets 0,wrap 1,gap 2", "[grow,fill]", "[]"));
        titleBox.setOpaque(false);

        JLabel lName = new JLabel(km.getTenKM());
        lName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lName.setForeground(NAVY);

        JPanel badge = createStatusBadge(statusText, statusColor);

        titleBox.add(lName);
        titleBox.add(badge, "w 115!, h 24!");

        JLabel editIcon = new JLabel(loadIcon("edit.png", 16, 16));
        editIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openDialog(km);
            }
        });

        topBox.add(iconBox, "w 48!, h 48!");
        topBox.add(titleBox, "gapx 12");
        topBox.add(editIcon, "aligny top");

        String discountText;
        if ("TheoPhanTram".equals(km.getLoaiKM())) {
            discountText = "-" + removeDecimalZero(km.getTienKhuyenMai()) + "%";
        } else {
            discountText = "-" + CurrencyUtils.formatVND(km.getTienKhuyenMai());
        }

        JLabel lDis = new JLabel(discountText, SwingConstants.CENTER);
        lDis.setFont(new Font("Segoe UI", Font.BOLD, 34));
        lDis.setForeground(GOLD);

        JLabel lMax = new JLabel(
                km.getGiaTriToiDa() > 0
                        ? "Tối đa " + CurrencyUtils.formatVND(km.getGiaTriToiDa())
                        : "Không giới hạn mức giảm",
                SwingConstants.CENTER
        );
        lMax.setForeground(new Color(110, 125, 145));
        lMax.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        RoundedPanel codeBox = new RoundedPanel(
                10,
                Color.WHITE,
                new Color(230, 200, 140),
                1f
        );
        codeBox.setLayout(new BorderLayout());
        codeBox.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        JLabel lTag = new JLabel("Mã: " + km.getMaKM(), SwingConstants.CENTER);
        lTag.setForeground(new Color(180, 120, 30));
        lTag.setFont(new Font("Segoe UI", Font.BOLD, 12));
        codeBox.add(lTag, BorderLayout.CENTER);

        double dk = km.getDieuKienApDung();
        JLabel lDesc = new JLabel(
                dk > 0
                        ? "Áp dụng đơn từ " + CurrencyUtils.formatVND(dk)
                        : "Áp dụng cho mọi hóa đơn",
                SwingConstants.CENTER
        );
        lDesc.setForeground(new Color(80, 95, 115));
        lDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        String strDate =
                DateUtils.format(km.getNgayBatDau()).substring(0, 10)
                        + "  →  "
                        + DateUtils.format(km.getNgayKetThuc()).substring(0, 10);

        JLabel lDate = new JLabel(strDate, SwingConstants.CENTER);
        lDate.setForeground(new Color(110, 125, 145));
        lDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lDate.setIcon(loadIcon("clock-circle.png", 14, 14));

        JPanel divider = new JPanel();
        divider.setBackground(new Color(238, 225, 200));

        card.add(topBox, "growx");
        card.add(divider, "h 1!, growx, gapy 8 8");
        card.add(lDis, "growx, gapy 4 0");
        card.add(lMax, "growx");
        card.add(codeBox, "growx, gapy 8 2");
        card.add(lDesc, "growx");
        card.add(lDate, "growx, gapy 4 0");

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    openDialog(km);
                }
            }
        });

        return card;
    }

    private JPanel createStatusBadge(String text, Color color) {
        RoundedPanel badge = new RoundedPanel(
                12,
                new Color(color.getRed(), color.getGreen(), color.getBlue(), 28),
                new Color(color.getRed(), color.getGreen(), color.getBlue(), 80),
                1f
        );
        badge.setLayout(new BorderLayout());

        JLabel label = new JLabel("● " + text, SwingConstants.CENTER);
        label.setForeground(color);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));

        badge.add(label, BorderLayout.CENTER);
        return badge;
    }

    private void openDialog(Promotion km) {
        new PromotionDialog(
                SwingUtilities.getWindowAncestor(PromotionsPanel.this),
                km,
                PromotionsPanel.this::refreshData
        ).setVisible(true);
    }

    private ImageIcon loadIcon(String filename, int w, int h) {
        try {
            URL resource = getClass().getResource("/kqlhotel/resources/icons/" + filename);
            if (resource == null) {
                java.io.File file = new java.io.File("src/kqlhotel/resources/icons/" + filename);
                if (file.exists()) resource = file.toURI().toURL();
            }
            if (resource != null) {
                ImageIcon icon = new ImageIcon(resource);
                return new ImageIcon(icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
            }
        } catch (Exception ignored) {}
        return null;
    }

    private Image loadRawImage(String path) {
        try {
            URL resource = getClass().getResource(path);
            if (resource == null) {
                java.io.File file = new java.io.File("src" + path);
                if (file.exists()) resource = file.toURI().toURL();
            }
            if (resource != null) {
                return new ImageIcon(resource).getImage();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String removeDecimalZero(double value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}