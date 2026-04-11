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
import kqlhotel.gui.components.PrimaryButton;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.gui.theme.ThemeColors;
import net.miginfocom.swing.MigLayout;

public class PromotionsPanel extends JPanel {
    private static final Color PAGE_BG = new Color(245, 248, 252);
    private final JPanel listPanel = new JPanel(new MigLayout("wrap 3,insets 0,gap 20", "[grow,fill][grow,fill][grow,fill]", "[]"));

    private final List<PromoData> mockPromos = Arrays.asList(
        new PromoData("Mùa hè rực rỡ 2026", "Sắp diễn ra", "-30%", "Tối đa 2.000.000đ", "SUMMER30", 
                      "Giảm 30% cho tất cả các loại phòng trong suốt mùa hè", "01/06/2026 - 31/08/2026", "3.000.000đ", 0, 200, new Color(49, 106, 210)),
        new PromoData("Tuần trăng mật", "Đang áp dụng", "-20%", "Tối đa 3.000.000đ", "HONEYMOON26", 
                      "Ưu đãi đặc biệt 20% cho các cặp đôi tân hôn, kèm trang trí phòng miễn phí", "01/01/2026 - 31/12/2026", "5.000.000đ", 45, 100, ThemeColors.SUCCESS),
        new PromoData("Khách hàng thân thiết", "Đang áp dụng", "-500.000đ", "Tối đa 500.000đ", "LOYAL500K", 
                      "Giảm ngay 500.000đ cho khách hàng đã ở từ 3 lần trở lên", "01/01/2026 - 30/08/2026", "2.000.000đ", 126, 500, ThemeColors.SUCCESS),
        new PromoData("Đặt sớm - Giá tốt", "Đang áp dụng", "-15%", "Tối đa 1.500.000đ", "EARLY15", 
                      "Giảm 15% khi đặt phòng trước ít nhất 2 tuần", "01/02/2026 - 31/05/2026", "2.500.000đ", 67, 150, ThemeColors.SUCCESS),
        new PromoData("Tết Bính Ngọ", "Đã hết hạn", "-20%", "Tối đa 1.500.000đ", "TET2026", 
                      "Chào mừng Tết Nguyên Đán Bính Ngọ, giảm 20% tất cả phòng", "25/01/2026 - 05/02/2026", "2.000.000đ", 76, 80, new Color(240, 60, 60)),
        new PromoData("Cuối tuần vui vẻ", "Đang áp dụng", "-10%", "Tối đa 800.000đ", "WEEKEND10", 
                      "Giảm 10% khi đặt phòng cuối tuần (Thứ 6, Thứ 7, Chủ nhật)", "01/03/2026 - 31/12/2026", "1.200.000đ", 212, 1000, ThemeColors.SUCCESS)
    );

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

        renderList(mockPromos);
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new MigLayout("insets 20 24 10 24", "[grow][]", "[]"));
        panel.setOpaque(false);
        
        JPanel titleBox = new JPanel(new MigLayout("insets 0, wrap 1", "[]", "[]"));
        titleBox.setOpaque(false);
        JLabel title = new JLabel("Khuyến mãi");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        title.setForeground(new Color(24, 40, 66));
        JLabel subtitle = new JLabel("6 chương trình \u00B7 4 đang áp dụng");
        subtitle.setForeground(new Color(119, 137, 168));
        subtitle.setFont(subtitle.getFont().deriveFont(13f));
        titleBox.add(title);
        titleBox.add(subtitle);

        PrimaryButton btnAdd = new PrimaryButton("+ Tạo khuyến mãi");
        btnAdd.setBackground(new Color(24, 34, 52));
        btnAdd.setForeground(Color.WHITE);
        
        panel.add(titleBox, "aligny center");
        panel.add(btnAdd, "aligny center, h 40!");
        
        return panel;
    }

    private JPanel createFilterBar() {
        JPanel bar = new JPanel(new MigLayout("insets 0 24 20 24, gap 10", "[][][][]", "[]"));
        bar.setOpaque(false);

        bar.add(createFilterBtn("Tất cả (6)", true, null));
        bar.add(createFilterBtn("Đang áp dụng (4)", false, ThemeColors.SUCCESS));
        bar.add(createFilterBtn("Sắp diễn ra (1)", false, new Color(49, 106, 210)));
        bar.add(createFilterBtn("Đã hết hạn (1)", false, new Color(150, 160, 175)));

        return bar;
    }

    private PrimaryButton createFilterBtn(String text, boolean active, Color dotColor) {
        PrimaryButton btn = new PrimaryButton(text);
        if (active) {
            btn.setBackground(new Color(24, 34, 52));
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(100, 115, 135));
            btn.setBorder(BorderFactory.createLineBorder(new Color(225, 231, 245), 1));
        }
        return btn;
    }

    private void renderList(List<PromoData> list) {
        listPanel.removeAll();
        for (PromoData data : list) {
            listPanel.add(createPromoCard(data));
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createPromoCard(PromoData data) {
        // Dynamic border color based on status
        Color borderColor = new Color(225, 231, 245);
        if (data.status.equals("Đang áp dụng")) borderColor = ThemeColors.SUCCESS;
        else if (data.status.equals("Sắp diễn ra")) borderColor = new Color(49, 106, 210);
        else if (data.status.equals("Đã hết hạn")) borderColor = new Color(240, 100, 100);

        RoundedPanel card = new RoundedPanel(16, Color.WHITE, borderColor, 2f);
        card.setLayout(new MigLayout("wrap 1,insets 20", "[grow,fill]", "[]"));

        // Header: Icon + Name
        JPanel topBox = new JPanel(new MigLayout("insets 0", "[][grow,fill][]", "[]"));
        topBox.setOpaque(false);
        
        RoundedPanel iconBox = new RoundedPanel(8, new Color(245, 248, 252), borderColor, 1f);
        iconBox.setLayout(new BorderLayout());
        iconBox.setPreferredSize(new Dimension(36, 36));
        JLabel iconL = new JLabel("%", SwingConstants.CENTER);
        iconL.setForeground(borderColor);
        iconL.setFont(iconL.getFont().deriveFont(Font.BOLD, 18f));
        iconBox.add(iconL);
        
        JPanel titleBox = new JPanel(new MigLayout("insets 0,wrap 1", "[]", "[][]"));
        titleBox.setOpaque(false);
        JLabel lName = new JLabel(data.name);
        lName.setFont(lName.getFont().deriveFont(Font.BOLD, 15f));
        lName.setForeground(new Color(24, 40, 66));
        
        JLabel lStatus = new JLabel("\u2022 " + data.status);
        lStatus.setFont(lStatus.getFont().deriveFont(Font.BOLD, 11f));
        lStatus.setForeground(borderColor);
        
        titleBox.add(lName);
        titleBox.add(lStatus);
        
        JLabel editIcon = new JLabel(loadIcon("edit.png", 16, 16));
        
        topBox.add(iconBox, "w 36!, h 36!");
        topBox.add(titleBox, "gapx 10");
        topBox.add(editIcon, "aligny top");

        // Discount details
        JPanel distBox = new JPanel(new MigLayout("insets 0", "[grow,fill][]", "[]"));
        distBox.setOpaque(false);
        
        JPanel pBox = new JPanel(new MigLayout("insets 0,wrap 1,gap 0", "[]", "[]"));
        pBox.setOpaque(false);
        JLabel lDis = new JLabel(data.discount);
        lDis.setFont(lDis.getFont().deriveFont(Font.BOLD, 22f));
        lDis.setForeground(new Color(24, 40, 66));
        JLabel lMax = new JLabel(data.maxDiscount);
        lMax.setForeground(new Color(110, 125, 145));
        lMax.setFont(lMax.getFont().deriveFont(12f));
        pBox.add(lDis);
        pBox.add(lMax);
        
        RoundedPanel tagBox = new RoundedPanel(6, Color.WHITE, new Color(225, 150, 50), 1f);
        tagBox.setLayout(new BorderLayout());
        tagBox.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        JLabel lTag = new JLabel(data.tag);
        lTag.setForeground(new Color(220, 120, 30));
        lTag.setFont(lTag.getFont().deriveFont(Font.BOLD, 11f));
        tagBox.add(lTag, BorderLayout.CENTER);
        
        distBox.add(pBox);
        distBox.add(tagBox, "aligny center");

        // Description
        JLabel lDesc = new JLabel("<html><p style='color:#506580;line-height:1.4'>" + data.description + "</p></html>");
        
        // Dates & Min
        JLabel lDate = new JLabel(data.dates + "   Min: " + data.minVal);
        lDate.setForeground(new Color(110, 125, 145));
        lDate.setFont(lDate.getFont().deriveFont(12f));
        lDate.setIcon(loadIcon("clock-circle.png", 14, 14)); // mock icon
        
        // Progress
        JPanel progBox = new JPanel(new MigLayout("insets 0", "[grow,fill][]", "[]"));
        progBox.setOpaque(false);
        JLabel pt = new JLabel("Đã sử dụng");
        pt.setForeground(new Color(110, 125, 145));
        pt.setFont(pt.getFont().deriveFont(11f));
        
        int percent = data.used * 100 / data.total;
        JLabel ptu = new JLabel(data.used + "/" + data.total + " (" + percent + "%)");
        ptu.setFont(ptu.getFont().deriveFont(Font.BOLD, 11f));
        ptu.setForeground(new Color(24, 40, 66));
        
        progBox.add(pt);
        progBox.add(ptu);
        
        JPanel progressBar = createProgressBar(borderColor, percent);

        // Add to card
        card.add(topBox, "growx");
        card.add(new JPanel(){{setBackground(new Color(240, 245, 250));}}, "h 1!, growx, gapy 10 10");
        card.add(distBox, "growx");
        card.add(lDesc, "gapy 10 0");
        card.add(lDate, "gapy 6 0");
        card.add(progBox, "growx, gapy 12 0");
        card.add(progressBar, "growx, h 6!, gapy 2 0");

        return card;
    }

    private JPanel createProgressBar(Color c, int p) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230, 235, 245));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                int fillW = Math.max(0, (int) (getWidth() * p / 100.0));
                g2.setColor(c);
                g2.fillRoundRect(0, 0, fillW, getHeight(), getHeight(), getHeight());
                g2.dispose();
            }
        };
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

    private static class PromoData {
        String name, status, discount, maxDiscount, tag, description, dates, minVal;
        int used, total; Color statusColor;
        PromoData(String n, String st, String d, String m, String tg, String ds, String dt, String mv, int u, int t, Color c) {
            name=n; status=st; discount=d; maxDiscount=m; tag=tg; description=ds; dates=dt; minVal=mv; used=u; total=t; statusColor=c;
        }
    }
}
