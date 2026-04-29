package kqlhotel.gui.tabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import kqlhotel.gui.components.PrimaryButton;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.gui.theme.ThemeColors;
import net.miginfocom.swing.MigLayout;

public class ServicesPanel extends JPanel {
    private static final Color COLOR_ACTIVE = new Color(56, 161, 105);
    private static final Color COLOR_PAUSED = new Color(220, 53, 69);
    private static final Color COLOR_FOOD = new Color(237, 137, 54);
    private static final Color COLOR_SPA = new Color(143, 97, 255);
    private static final Color ACTIVE_FILTER = new Color(18, 35, 67);

    private final JPanel filterRow = new JPanel(new MigLayout("insets 0,gap 10", "[]", "[]"));
    private final JPanel gridContainer = new JPanel(new java.awt.GridLayout(0, 3, 16, 16));

    private final List<ServiceCardData> mockData = Arrays.asList(
        new ServiceCardData("DV001", "Breakfast Buffet", "Am thuc", "Mo cua 06:00 - 10:00", "Dang ap dung", "199.000d", "Phuc vu tai nha hang tang 1 cho toi da 2 khach", COLOR_FOOD),
        new ServiceCardData("DV002", "Laundry Express", "Tien ich", "Tra do trong 4 gio", "Dang ap dung", "89.000d", "Nhan va giao do tai phong trong ngay", ThemeColors.PRIMARY),
        new ServiceCardData("DV003", "Airport Pickup", "Di chuyen", "Dat truoc 3 gio", "Dang ap dung", "350.000d", "Don san bay bang xe 7 cho, bao gom 1 diem dung", new Color(17, 24, 39)),
        new ServiceCardData("DV004", "Spa Relax 60'", "Spa", "Khung gio 09:00 - 22:00", "Dang ap dung", "650.000d", "Lieu trinh massage va xong hoi co ban", COLOR_SPA),
        new ServiceCardData("DV005", "Mini Bar Combo", "Am thuc", "Ap dung tai phong", "Tam ngung", "149.000d", "Combo snack va do uong cho khach luu tru", COLOR_PAUSED),
        new ServiceCardData("DV006", "Romantic Setup", "Trang tri", "Dat truoc 6 gio", "Dang ap dung", "490.000d", "Trang tri phong voi nen, hoa va bang chao mung", ThemeColors.ACCENT)
    );

    public ServicesPanel() {
        setOpaque(false);
        setLayout(new MigLayout("insets 24,gap 18,wrap 1", "[grow,fill]", "[][][][][grow,fill]"));

        add(createHeader());
        add(createStatsRow());
        add(createQuickOverview());
        add(createFilterRow());
        add(createContent(), "grow");

        applyFilter("Tat ca");
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new MigLayout("insets 0", "[grow,fill][][]", "[]"));
        header.setOpaque(false);

        JPanel titleWrap = new JPanel(new MigLayout("insets 0,wrap 1,gap 2", "[grow,fill]", "[]"));
        titleWrap.setOpaque(false);

        JLabel title = new JLabel("Dich vu");
        title.setForeground(new Color(24, 40, 66));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));

        JLabel subtitle = new JLabel("Quan ly danh muc dich vu bo sung, gia ban va trang thai ap dung");
        subtitle.setForeground(new Color(130, 145, 170));

        titleWrap.add(title);
        titleWrap.add(subtitle);

        JButton syncButton = createGhostButton("Dong bo bang gia");

        PrimaryButton addButton = new PrimaryButton("+ Them dich vu");
        addButton.setBackground(new Color(17, 24, 39));
        addButton.setForeground(Color.WHITE);

        header.add(titleWrap);
        header.add(syncButton, "h 42!");
        header.add(addButton, "h 42!");
        return header;
    }

    private JPanel createStatsRow() {
        JPanel row = new JPanel(new MigLayout("insets 0,gap 16", "[grow,fill][grow,fill][grow,fill][grow,fill]", "[]"));
        row.setOpaque(false);
        row.add(createStatCard("Tong dich vu", "24", "6 nhom dich vu dang khai thac", new Color(17, 24, 39)));
        row.add(createStatCard("Dang ap dung", "19", "79% san pham dang ban", COLOR_ACTIVE));
        row.add(createStatCard("Tam ngung", "5", "Can ra soat lai gia va nha cung cap", COLOR_PAUSED));
        row.add(createStatCard("Ban chay", "Breakfast", "156 luot su dung trong tuan", COLOR_FOOD));
        return row;
    }

    private JPanel createQuickOverview() {
        JPanel row = new JPanel(new MigLayout("insets 0,gap 16", "[grow,fill][260!]", "[]"));
        row.setOpaque(false);

        RoundedPanel note = new RoundedPanel(18, Color.WHITE, ThemeColors.BORDER, 1f);
        note.setLayout(new MigLayout("insets 16", "[grow,fill][]", "[]"));

        JPanel textWrap = new JPanel(new MigLayout("insets 0,wrap 1,gap 3", "[grow,fill]", "[]"));
        textWrap.setOpaque(false);
        JLabel title = new JLabel("Goi dich vu uu tien tuan nay");
        title.setForeground(new Color(24, 40, 66));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        JLabel text = new JLabel("Day manh breakfast, airport pickup va romantic setup cho booking cuoi tuan");
        text.setForeground(new Color(110, 125, 145));
        textWrap.add(title);
        textWrap.add(text);

        note.add(textWrap);
        note.add(makeBadge("Upsell focus", new Color(237, 137, 54, 28), ThemeColors.ACCENT), "aligny center");

        RoundedPanel revenue = new RoundedPanel(18, new Color(237, 247, 255), new Color(190, 227, 248), 1f);
        revenue.setLayout(new MigLayout("insets 14,wrap 1,gap 2", "[grow,fill]", "[]"));
        JLabel revenueTitle = new JLabel("Doanh thu dich vu");
        revenueTitle.setForeground(new Color(43, 108, 176));
        revenueTitle.setFont(revenueTitle.getFont().deriveFont(Font.BOLD, 13f));
        JLabel revenueValue = new JLabel("124.500.000d");
        revenueValue.setForeground(new Color(30, 64, 175));
        revenueValue.setFont(revenueValue.getFont().deriveFont(Font.BOLD, 24f));
        JLabel revenueNote = new JLabel("+18% so voi 7 ngay truoc");
        revenueNote.setForeground(new Color(59, 130, 246));
        revenue.add(revenueTitle);
        revenue.add(revenueValue);
        revenue.add(revenueNote);

        row.add(note, "h 74!");
        row.add(revenue, "h 74!");
        return row;
    }

    private JPanel createFilterRow() {
        filterRow.setOpaque(false);
        filterRow.add(createFilterButton("Tat ca", "6", true));
        filterRow.add(createFilterButton("Dang ap dung", "5", false));
        filterRow.add(createFilterButton("Tam ngung", "1", false));
        filterRow.add(createFilterButton("Am thuc", "2", false));
        filterRow.add(createFilterButton("Spa", "1", false));
        return filterRow;
    }

    private JScrollPane createContent() {
        gridContainer.setOpaque(false);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(gridContainer, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(wrap);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private RoundedPanel createStatCard(String label, String value, String note, Color accent) {
        RoundedPanel card = new RoundedPanel(18, Color.WHITE, ThemeColors.BORDER, 1f);
        card.setLayout(new MigLayout("insets 18,wrap 1,gap 6", "[grow,fill]", "[]"));

        JPanel top = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        top.setOpaque(false);
        JLabel title = new JLabel(label);
        title.setForeground(new Color(130, 145, 170));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
        top.add(title);
        top.add(createDot(accent), "w 10!,h 10!");

        JLabel bigValue = new JLabel(value);
        bigValue.setForeground(new Color(24, 40, 66));
        bigValue.setFont(bigValue.getFont().deriveFont(Font.BOLD, 28f));

        JLabel smallNote = new JLabel(note);
        smallNote.setForeground(new Color(150, 165, 190));

        card.add(top);
        card.add(bigValue);
        card.add(smallNote);
        return card;
    }

    private JButton createFilterButton(String label, String badge, boolean active) {
        JButton button = new JButton();
        button.putClientProperty("filterLabel", label);
        button.putClientProperty("filterBadge", badge);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(button.getFont().deriveFont(13f));
        styleFilterButton(button, active);
        button.addActionListener(e -> applyFilter(label));
        return button;
    }

    private void styleFilterButton(JButton button, boolean active) {
        String label = (String) button.getClientProperty("filterLabel");
        String badge = (String) button.getClientProperty("filterBadge");
        String text = "<html>" + label + " <span style='color:" + (active ? "#A0B0E0" : "#A0B0C0")
            + ";font-size:10px;'>&nbsp;" + badge + "&nbsp;</span></html>";
        button.setText(text);
        if (active) {
            button.setBackground(ACTIVE_FILTER);
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACTIVE_FILTER, 1),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)
            ));
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(new Color(100, 120, 150));
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 230, 245), 1),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)
            ));
        }
    }

    private void applyFilter(String filter) {
        for (int i = 0; i < filterRow.getComponentCount(); i++) {
            if (filterRow.getComponent(i) instanceof JButton) {
                JButton button = (JButton) filterRow.getComponent(i);
                styleFilterButton(button, filter.equals(button.getClientProperty("filterLabel")));
            }
        }

        gridContainer.removeAll();
        for (ServiceCardData data : mockData) {
            boolean matches = filter.equals("Tat ca")
                || data.status.equalsIgnoreCase(filter)
                || data.category.equalsIgnoreCase(filter);
            if (matches) {
                gridContainer.add(createServiceCard(data));
            }
        }

        gridContainer.revalidate();
        gridContainer.repaint();
    }

    private RoundedPanel createServiceCard(ServiceCardData data) {
        RoundedPanel card = new RoundedPanel(18, Color.WHITE, ThemeColors.BORDER, 1f);
        card.setLayout(new MigLayout("insets 18,wrap 1,gap 10", "[grow,fill]", "[]"));

        JPanel top = new JPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        top.setOpaque(false);

        JPanel codeWrap = new JPanel(new MigLayout("insets 0,wrap 1,gap 2", "[grow,fill]", "[]"));
        codeWrap.setOpaque(false);
        JLabel code = new JLabel(data.code);
        code.setForeground(new Color(130, 145, 170));
        code.setFont(code.getFont().deriveFont(Font.BOLD, 11f));
        JLabel name = new JLabel(data.name);
        name.setForeground(new Color(24, 40, 66));
        name.setFont(name.getFont().deriveFont(Font.BOLD, 18f));
        codeWrap.add(code);
        codeWrap.add(name);

        top.add(codeWrap);
        top.add(makeBadge(data.status, new Color(data.accent.getRed(), data.accent.getGreen(), data.accent.getBlue(), 20), data.accent), "aligny top");

        JPanel tags = new JPanel(new MigLayout("insets 0,gap 8", "[][]", "[]"));
        tags.setOpaque(false);
        tags.add(makeBadge(data.category, new Color(241, 245, 249), new Color(71, 85, 105)));
        tags.add(makeBadge(data.window, new Color(248, 250, 252), new Color(100, 116, 139)));

        JLabel description = new JLabel("<html><body style='width:260px'>" + data.description + "</body></html>");
        description.setForeground(new Color(95, 110, 135));

        JPanel bottom = new JPanel(new MigLayout("insets 0", "[grow,fill][]", "[]"));
        bottom.setOpaque(false);

        JPanel priceWrap = new JPanel(new MigLayout("insets 0,wrap 1,gap 1", "[grow,fill]", "[]"));
        priceWrap.setOpaque(false);
        JLabel priceTitle = new JLabel("Gia ban");
        priceTitle.setForeground(new Color(130, 145, 170));
        priceTitle.setFont(priceTitle.getFont().deriveFont(12f));
        JLabel priceValue = new JLabel(data.price);
        priceValue.setForeground(new Color(24, 40, 66));
        priceValue.setFont(priceValue.getFont().deriveFont(Font.BOLD, 20f));
        priceWrap.add(priceTitle);
        priceWrap.add(priceValue);

        bottom.add(priceWrap);
        bottom.add(createGhostButton("Chinh sua"), "h 36!");

        card.add(top);
        card.add(tags);
        card.add(description);
        card.add(bottom, "gapy 6 0,growx");
        return card;
    }

    private JButton createGhostButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(85, 105, 135));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 230, 245), 1),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        return button;
    }

    private JPanel makeBadge(String text, Color bg, Color fg) {
        JPanel badge = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(fg);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));
        badge.add(label);
        return badge;
    }

    private JPanel createDot(Color color) {
        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        return dot;
    }

    private static final class ServiceCardData {
        private final String code;
        private final String name;
        private final String category;
        private final String window;
        private final String status;
        private final String price;
        private final String description;
        private final Color accent;

        private ServiceCardData(
            String code,
            String name,
            String category,
            String window,
            String status,
            String price,
            String description,
            Color accent
        ) {
            this.code = code;
            this.name = name;
            this.category = category;
            this.window = window;
            this.status = status;
            this.price = price;
            this.description = description;
            this.accent = accent;
        }
    }
}
