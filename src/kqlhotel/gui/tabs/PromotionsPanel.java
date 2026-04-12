package kqlhotel.gui.tabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import kqlhotel.gui.components.PrimaryButton;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.gui.theme.ThemeColors;
import net.miginfocom.swing.MigLayout;
import kqlhotel.bus.promotion.PromotionsBUS;
import kqlhotel.entity.Promotion;
import kqlhotel.utils.CurrencyUtils;
import kqlhotel.utils.DateUtils;

public class PromotionsPanel extends JPanel {
    private static final Color PAGE_BG = new Color(245, 248, 252);
    private final JPanel listPanel = new JPanel(new MigLayout("wrap 3,insets 0,gap 20", "[grow,fill][grow,fill][grow,fill]", "[]"));
    private final PromotionsBUS promotionsBUS = new PromotionsBUS();
    private List<Promotion> currentList;
    private final JLabel summaryLabel = new JLabel();
    private final List<PrimaryButton> filterButtons = new java.util.ArrayList<>();

    public PromotionsPanel() {
        setOpaque(false);
        setBackground(PAGE_BG);
        setLayout(new BorderLayout());

        JPanel header = createHeader();
        JPanel filterBar = createFilterBar();
        
        listPanel.setOpaque(false);
        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 24));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(header, BorderLayout.NORTH);
        topContainer.add(filterBar, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        refreshData();
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
        summaryLabel.setText(String.format("%d chương trình \u00B7 %d đang áp dụng", counts[0], counts[1]));
        
        if (filterButtons.size() >= 4) {
            filterButtons.get(0).setText("Tất cả (" + counts[0] + ")");
            filterButtons.get(1).setText("Đang áp dụng (" + counts[1] + ")");
            filterButtons.get(2).setText("Sắp diễn ra (" + counts[2] + ")");
            filterButtons.get(3).setText("Đã hết hạn (" + counts[3] + ")");
        }
    }

    private void updateFilterButtonStyles(String activeText) {
        for (PrimaryButton btn : filterButtons) {
            if (btn.getText().startsWith(activeText)) {
                btn.setBackground(new Color(24, 34, 52));
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(Color.WHITE);
                btn.setForeground(new Color(100, 115, 135));
                btn.setBorder(BorderFactory.createLineBorder(new Color(225, 231, 245), 1));
            }
        }
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new MigLayout("insets 20 24 10 24", "[grow][]", "[]"));
        panel.setOpaque(false);
        
        JPanel titleBox = new JPanel(new MigLayout("insets 0, wrap 1", "[]", "[]"));
        titleBox.setOpaque(false);
        JLabel title = new JLabel("Khuyến mãi");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setForeground(new Color(24, 40, 66));
        summaryLabel.setForeground(new Color(119, 137, 168));
        summaryLabel.setFont(summaryLabel.getFont().deriveFont(13f));
        titleBox.add(title);
        titleBox.add(summaryLabel);

        PrimaryButton btnAdd = new PrimaryButton("+ Tạo khuyến mãi");
        btnAdd.setBackground(new Color(24, 34, 52));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> {
            new PromotionDialog(SwingUtilities.getWindowAncestor(this), null, this::refreshData).setVisible(true);
        });
        
        panel.add(titleBox, "aligny center");
        panel.add(btnAdd, "aligny center, h 40!");
        
        return panel;
    }

    private JPanel createFilterBar() {
        JPanel bar = new JPanel(new MigLayout("insets 0 24 20 24, gap 10", "[][][][]", "[]"));
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
        if (active) {
            btn.setBackground(new Color(24, 34, 52));
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

    private void renderList(List<Promotion> list) {
        listPanel.removeAll();
        for (Promotion data : list) {
            listPanel.add(createPromoCard(data));
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createPromoCard(Promotion km) {
        // Prepare display status
        String statusText;
        Color borderColor;
        if ("DangHoatDong".equals(km.getTrangThaiKM())) {
            statusText = "Đang áp dụng";
            borderColor = ThemeColors.SUCCESS;
        } else if ("SapDienRa".equals(km.getTrangThaiKM())) {
            statusText = "Sắp diễn ra";
            borderColor = new Color(49, 106, 210);
        } else {
            statusText = "Đã hết hạn";
            borderColor = new Color(240, 100, 100);
        }

        RoundedPanel card = new RoundedPanel(16, Color.WHITE, borderColor, 2.5f);
        card.setHoverEffectEnabled(true);
        card.setLayout(new MigLayout("wrap 1,insets 20", "[grow,fill]", "[]"));

        // Header: Icon + Name
        JPanel topBox = new JPanel(new MigLayout("insets 0", "[][grow,fill][]", "[]"));
        topBox.setOpaque(false);
        
        RoundedPanel iconBox = new RoundedPanel(8, new Color(245, 248, 252), borderColor, 1f);
        iconBox.setLayout(new BorderLayout());
        iconBox.setPreferredSize(new Dimension(36, 36));
        JLabel iconL = new JLabel("%".equals(km.getLoaiKM()) ? "%" : "$", SwingConstants.CENTER);
        iconL.setForeground(borderColor);
        iconL.setFont(iconL.getFont().deriveFont(Font.BOLD, 18f));
        iconBox.add(iconL);
        
        JPanel titleBox = new JPanel(new MigLayout("insets 0,wrap 1", "[]", "[][]"));
        titleBox.setOpaque(false);
        JLabel lName = new JLabel(km.getTenKM());
        lName.setFont(lName.getFont().deriveFont(Font.BOLD, 15f));
        lName.setForeground(new Color(24, 40, 66));
        
        JLabel lStatus = new JLabel("\u2022 " + statusText);
        lStatus.setFont(lStatus.getFont().deriveFont(Font.BOLD, 11f));
        lStatus.setForeground(borderColor);
        
        titleBox.add(lName);
        titleBox.add(lStatus);
        
        JLabel editIcon = new JLabel(loadIcon("edit.png", 16, 16));
        editIcon.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        editIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new PromotionDialog(SwingUtilities.getWindowAncestor(PromotionsPanel.this), km, PromotionsPanel.this::refreshData).setVisible(true);
            }
        });
        
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    new PromotionDialog(SwingUtilities.getWindowAncestor(PromotionsPanel.this), km, PromotionsPanel.this::refreshData).setVisible(true);
                }
            }
        });
        
        topBox.add(iconBox, "w 36!, h 36!");
        topBox.add(titleBox, "gapx 10");
        topBox.add(editIcon, "aligny top");

        // Discount details
        JPanel distBox = new JPanel(new MigLayout("insets 0", "[grow,fill][]", "[]"));
        distBox.setOpaque(false);
        
        JPanel pBox = new JPanel(new MigLayout("insets 0,wrap 1,gap 0", "[]", "[]"));
        pBox.setOpaque(false);
        
        String discountText = "%".equals(km.getLoaiKM()) 
            ? "-" + ((int) km.getTienKhuyenMai()) + "%"
            : "-" + CurrencyUtils.formatVND(km.getTienKhuyenMai());
            
        JLabel lDis = new JLabel(discountText);
        lDis.setFont(lDis.getFont().deriveFont(Font.BOLD, 22f));
        lDis.setForeground(new Color(24, 40, 66));
        
        JLabel lMax = new JLabel("Tối đa " + CurrencyUtils.formatVND(km.getGiaTriToiDa()));
        lMax.setForeground(new Color(110, 125, 145));
        lMax.setFont(lMax.getFont().deriveFont(12f));
        if (km.getGiaTriToiDa() <= 0) lMax.setVisible(false);
        
        pBox.add(lDis);
        pBox.add(lMax);
        
        RoundedPanel tagBox = new RoundedPanel(6, Color.WHITE, new Color(225, 150, 50), 1f);
        tagBox.setLayout(new BorderLayout());
        tagBox.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        JLabel lTag = new JLabel(km.getMaKM());
        lTag.setForeground(new Color(220, 120, 30));
        lTag.setFont(lTag.getFont().deriveFont(Font.BOLD, 11f));
        tagBox.add(lTag, BorderLayout.CENTER);
        
        distBox.add(pBox);
        distBox.add(tagBox, "aligny center");

        // Description
        JLabel lDesc = new JLabel("<html><p style='color:#506580;line-height:1.4'>" + km.getDieuKienApDung() + "</p></html>");
        
        // Dates & Min
        String strDate = DateUtils.format(km.getNgayBatDau()).substring(0, 10) + " - " + DateUtils.format(km.getNgayKetThuc()).substring(0, 10);
        JLabel lDate = new JLabel(strDate);
        lDate.setForeground(new Color(110, 125, 145));
        lDate.setFont(lDate.getFont().deriveFont(12f));
        lDate.setIcon(loadIcon("clock-circle.png", 14, 14));

        // Add to card
        card.add(topBox, "growx");
        card.add(new JPanel(){{setBackground(new Color(240, 245, 250));}}, "h 1!, growx, gapy 10 10");
        card.add(distBox, "growx");
        card.add(lDesc, "gapy 10 0");
        card.add(lDate, "gapy 6 12");

        return card;
    }

    private ImageIcon loadIcon(String filename, int w, int h) {
        try {
            java.net.URL resource = getClass().getResource("/kqlhotel/resources/icons/" + filename);
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
}
