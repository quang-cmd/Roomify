package kqlhotel.gui.components;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import kqlhotel.gui.utils.IconLoader;
import kqlhotel.gui.theme.ThemeColors;
import kqlhotel.gui.model.RoomCardData;
import net.miginfocom.swing.MigLayout;
import javax.swing.SwingConstants;

public class RoomCard extends JPanel {
    private final RoomCardData data;
    private final boolean selected;
    private final Consumer<RoomCardData> onToggle;

    public RoomCard(RoomCardData data, boolean selected, Consumer<RoomCardData> onToggle) {
        this.data = data;
        this.selected = selected;
        this.onToggle = onToggle;

        setOpaque(false);
        setLayout(new MigLayout("insets 14 16,gap 6,wrap 1", "[grow,fill]", "[][][grow,fill][]"));
        setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

        initComponents();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onToggle.accept(RoomCard.this.data);
            }
        });
    }

    private void initComponents() {
        JLabel nameLbl = new JLabel(data.roomType);
        nameLbl.setForeground(Color.WHITE);
        nameLbl.setFont(nameLbl.getFont().deriveFont(Font.BOLD, 20f));

        JPanel titleRow = new JPanel(new MigLayout("insets 0,gap 10", "[grow,fill][]", "[]"));
        titleRow.setOpaque(false);
        titleRow.add(nameLbl, "aligny center");
        titleRow.add(makeAvailBadge(data.status, selected ? Color.WHITE : data.tone), "aligny center");

        JLabel priceLb = new JLabel(data.price);
        priceLb.setForeground(Color.WHITE);
        priceLb.setFont(priceLb.getFont().deriveFont(Font.BOLD, 26f));
        JLabel perNight = new JLabel(" /đêm");
        perNight.setForeground(new Color(230, 230, 230));
        perNight.setFont(perNight.getFont().deriveFont(13f));

        JLabel capacityLbl = new JLabel("Tối đa " + data.capacity + " khách");
        capacityLbl.setForeground(new Color(220, 220, 220));
        capacityLbl.setFont(capacityLbl.getFont().deriveFont(12f));

        JPanel priceRow = new JPanel(new MigLayout("insets 0,gap 8", "[][grow,fill][]", "[]"));
        priceRow.setOpaque(false);
        priceRow.add(priceLb);
        priceRow.add(perNight, "aligny bottom");
        priceRow.add(capacityLbl, "alignx right,aligny bottom");

        JPanel progressRow = new JPanel(new MigLayout("insets 0,gap 10", "[grow,fill][pref!]", "[]"));
        progressRow.setOpaque(false);
        JPanel progressBar = makeProgressBar(selected ? ThemeColors.PREMIUM_PRIMARY : data.tone, parsePercent(data.occupancyRate));
        JLabel percentLbl = new JLabel(data.occupancyRate);
        percentLbl.setForeground(Color.WHITE);
        percentLbl.setFont(percentLbl.getFont().deriveFont(Font.BOLD, 13f));
        progressRow.add(progressBar, "growx,h 8!");
        progressRow.add(percentLbl, "aligny center");

        JPanel amenitiesRow = new JPanel(new MigLayout("insets 0,gap 14", "[pref!][pref!][pref!][grow,fill]", "[]"));
        amenitiesRow.setOpaque(false);
        int added = 0;
        for (String amenity : data.amenities) {
            if (added >= 3) break;
            ImageIcon icon = IconLoader.getAmenityIcon(amenity);
            JLabel aLbl = new JLabel(amenity);
            if (icon != null) {
                aLbl.setIcon(icon);
                aLbl.setIconTextGap(6);
            }
            aLbl.setForeground(new Color(235, 235, 235));
            aLbl.setFont(aLbl.getFont().deriveFont(11f));
            amenitiesRow.add(aLbl);
            added++;
        }

        String pickText = selected ? "Đã chọn" : "+ Thêm phòng";
        PrimaryButton pickButton = new PrimaryButton(pickText);
        pickButton.setFont(pickButton.getFont().deriveFont(Font.BOLD, 13f));
        pickButton.setFocusPainted(false);

        if (selected) {
            ImageIcon checkIcon = IconLoader.loadIcon("check.png", 16, 16);
            if (checkIcon != null) {
                pickButton.setIcon(checkIcon);
                pickButton.setIconTextGap(8);
            }
            pickButton.setBackground(new Color(255, 255, 255, 235));
            pickButton.setForeground(ThemeColors.PREMIUM_PRIMARY);
        } else {
            pickButton.setBackground(new Color(255, 255, 255, 225));
            pickButton.setForeground(data.tone);
        }
        pickButton.addActionListener(e -> onToggle.accept(data));

        add(titleRow, "growx");
        add(priceRow, "gapy 6 0");
        add(progressRow, "growx,gapy 6 0");
        add(amenitiesRow, "growx,gapy 8 0");
        add(pickButton, "h 42,alignx center,gapy 8 0");
    }

    private JPanel makeAvailBadge(String status, Color color) {
        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean lightBadge = color.equals(Color.WHITE);
                if (lightBadge) {
                    g2.setColor(new Color(255, 255, 255, 54));
                } else {
                    g2.setColor(new Color(255, 255, 255, 205));
                }

                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);

                if (lightBadge) {
                    g2.setColor(new Color(255,255,255,90));
                }
                else {
                    g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 70));
                }
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setLayout(new BorderLayout());
        badge.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 9, 3, 9));

        JLabel lbl = new JLabel(status, SwingConstants.CENTER);
        lbl.setForeground(color.equals(Color.WHITE) ? Color.WHITE : color);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 12f));
        badge.add(lbl);
        return badge;
    }

    private JPanel makeProgressBar(Color color, int percent) {
        JPanel progress = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int w = getWidth();
                int h = getHeight();
                int arc = h;
                int y = 1;
                int barHeight = Math.max(1, h - 2);

                g2.setColor(new Color(255, 255, 255, 145));
                g2.fillRoundRect(0, y, w, barHeight, arc, arc);

                int fillWidth = Math.max(barHeight, (int) (w * Math.max(0, Math.min(100, percent)) / 100.0));
                fillWidth = Math.min(w, fillWidth);

                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 235));
                g2.fillRoundRect(0, y, fillWidth, barHeight, arc, arc);

                g2.dispose();
            }
        };

        progress.setOpaque(false);
        return progress;
    }

    private int parsePercent(String pct) {
        try {
            return Integer.parseInt(pct.replace("%", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        // paint thumbnail background + overlay
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        int CARD_ARC = 18;

        Shape roundedCard = new java.awt.geom.RoundRectangle2D.Float(0, 0, w - 1, h - 1, CARD_ARC, CARD_ARC);
        g2.setClip(roundedCard);

        ImageIcon cachedThumb = IconLoader.loadIcon(IconLoader.getThumbnailFile(data.roomType), w, h);
        if (cachedThumb != null) {
            g2.drawImage(cachedThumb.getImage(), 0, 0, w, h, null);
        } else {
            g2.setColor(selected ? ThemeColors.PREMIUM_PRIMARY_SOFT : data.bg);
            g2.fillRoundRect(0, 0, w - 1, h - 1, CARD_ARC, CARD_ARC);
        }

        GradientPaint overlay = new GradientPaint(0, 0, new Color(0,0,0,185), 0, h, new Color(0,0,0, selected ? 120 : 82));
        g2.setPaint(overlay);
        g2.fillRoundRect(0, 0, w - 1, h - 1, CARD_ARC, CARD_ARC);

        if (selected) {
            g2.setColor(new Color(ThemeColors.PREMIUM_PRIMARY.getRed(), ThemeColors.PREMIUM_PRIMARY.getGreen(), ThemeColors.PREMIUM_PRIMARY.getBlue(), 58));
            g2.fillRoundRect(0, 0, w - 1, h - 1, CARD_ARC, CARD_ARC);
        }

        g2.setClip(null);
        g2.setColor(selected ? Color.WHITE : new Color(255,255,255,135));
        g2.setStroke(new BasicStroke(selected ? 2.4f : 1.2f));
        g2.drawRoundRect(1, 1, w - 3, h - 3, CARD_ARC, CARD_ARC);
        g2.dispose();

        super.paintComponent(g);
    }
}
