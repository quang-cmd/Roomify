package kqlhotel.gui.tabs;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import kqlhotel.gui.components.PrimaryButton;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.gui.theme.ThemeColors;
import net.miginfocom.swing.MigLayout;

public class BookingPanel extends JPanel {
    private static final Color PAGE_BG = new Color(245, 248, 252);

    private final JComboBox<String> roomTypeCombo = new JComboBox<>(new String[]{"T\u1ea5t c\u1ea3", "Deluxe", "Grand Premium", "Suite"});
    private final JTextField checkInField = new JTextField("dd/mm/yyyy");
    private final JTextField checkOutField = new JTextField("dd/mm/yyyy");
    private int guestCount = 2;
    private JLabel guestCountLabel;

    private final JLabel step1Label = new JLabel();
    private final JLabel step2Label = new JLabel();
    private final CardLayout bookingCards = new CardLayout();
    private final JPanel bookingContent = new JPanel(bookingCards);

    private final JPanel roomList = new JPanel(new MigLayout("wrap 2,insets 0,gap 12", "[grow,fill][grow,fill]", "[]"));
    private final JLabel selectedRoomLabel = new JLabel("Ch\u01b0a ch\u1ecdn lo\u1ea1i ph\u00f2ng");
    private final JLabel selectedDateLabel = new JLabel("Ng\u00e0y nh\u1eadn/tr\u1ea3: --");
    private final JLabel selectedGuestLabel = new JLabel("S\u1ed1 kh\u00e1ch: --");
    private final JTextField customerNameField = new JTextField();
    private final JTextField customerPhoneField = new JTextField();
    private final JTextField customerIdField = new JTextField();

    private RoomTypeData selectedRoom;

    private final List<RoomTypeData> roomData = Arrays.asList(
        new RoomTypeData("Deluxe", "1.200.000\u0111", "2/5 tr\u1ed1ng", "40%", Arrays.asList("Wifi", "Minibar", "Ban c\u00f4ng"), new Color(235, 248, 255), new Color(49, 130, 206), 2),
        new RoomTypeData("Grand Premium 1", "2.200.000\u0111", "2/4 tr\u1ed1ng", "50%", Arrays.asList("Wifi", "Minibar", "Ban c\u00f4ng"), new Color(223, 248, 239), new Color(30, 180, 120), 2),
        new RoomTypeData("Grand Premium 2", "3.200.000\u0111", "2/3 tr\u1ed1ng", "67%", Arrays.asList("Wifi", "Minibar", "Ban c\u00f4ng"), new Color(238, 232, 255), new Color(143, 97, 255), 2),
        new RoomTypeData("Suite", "5.500.000\u0111", "2/3 tr\u1ed1ng", "67%", Arrays.asList("Wifi", "Minibar", "Ph\u00f2ng kh\u00e1ch"), new Color(255, 246, 220), new Color(230, 154, 30), 2)
    );

    public BookingPanel() {
        setOpaque(false);
        setBackground(PAGE_BG);
        setLayout(new MigLayout("insets 24,gap 20", "[grow 34][grow 66]", "[]"));

        RoundedPanel filterCard = createFilterCard();
        JPanel rightSide = createRightSide();

        add(filterCard, "growy");
        add(rightSide, "grow");

        renderRooms(roomData);
        setStep(1);
    }

    private RoundedPanel createFilterCard() {
        RoundedPanel filterCard = new RoundedPanel(16, Color.WHITE, new Color(225, 231, 245), 1f);
        filterCard.setLayout(new MigLayout("wrap 1,insets 18,gap 10", "[grow,fill]", "[]"));

        // Title row with icon
        JPanel titleRow = new JPanel(new MigLayout("insets 0,gap 10", "[][grow,fill]", "[]"));
        titleRow.setOpaque(false);

        JPanel iconBox = new JPanel(new BorderLayout());
        iconBox.setOpaque(false);
        ImageIcon filterIcon = loadIcon("filter-badge.png", 44, 44);
        if (filterIcon != null) {
            iconBox.add(new JLabel(filterIcon), BorderLayout.CENTER);
        } else {
            JPanel fallback = makeBadgeIcon(new Color(235, 248, 255), new Color(49, 130, 206), "\u25A1");
            iconBox = fallback;
        }

        JPanel titleText = new JPanel(new MigLayout("insets 0,wrap 1,gap 2", "[grow,fill]", "[]"));
        titleText.setOpaque(false);
        JLabel title = new JLabel("Y\u00eau c\u1ea7u ph\u00f2ng");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(new Color(24, 40, 66));
        JLabel subtitle = new JLabel("Ch\u1ecdn lo\u1ea1i ph\u00f2ng, ng\u00e0y & s\u1ed1 kh\u00e1ch \u0111\u1ec3 t\u00ecm ki\u1ebfm");
        subtitle.setForeground(new Color(102, 124, 155));
        subtitle.setFont(subtitle.getFont().deriveFont(12f));
        titleText.add(title);
        titleText.add(subtitle);

        titleRow.add(iconBox, "w 44!,h 44!");
        titleRow.add(titleText, "aligny center");

        filterCard.add(titleRow, "gapy 0 8");
        filterCard.add(new JLabel("Lo\u1ea1i ph\u00f2ng"));
        filterCard.add(roomTypeCombo, "h 40");

        // Side-by-side date fields
        JPanel dateRow = new JPanel(new MigLayout("insets 0,gap 10", "[grow,fill][grow,fill]", "[][]"));
        dateRow.setOpaque(false);

        JLabel checkInLbl = new JLabel("Nh\u1eadn ph\u00f2ng *");
        checkInLbl.setForeground(new Color(30, 50, 80));
        JLabel checkOutLbl = new JLabel("Tr\u1ea3 ph\u00f2ng *");
        checkOutLbl.setForeground(new Color(30, 50, 80));

        dateRow.add(checkInLbl);
        dateRow.add(checkOutLbl);
        dateRow.add(makeCalendarField(checkInField), "h 40,grow");
        dateRow.add(makeCalendarField(checkOutField), "h 40,grow");

        filterCard.add(dateRow);
        filterCard.add(new JLabel("S\u1ed1 kh\u00e1ch"));
        filterCard.add(createGuestStepper(), "h 44");

        JLabel note = new JLabel("T\u1ed1i \u0111a 4 kh\u00e1ch m\u1ed7i ph\u00f2ng");
        note.setForeground(new Color(150, 165, 190));
        note.setFont(note.getFont().deriveFont(11f));
        filterCard.add(note, "gapy 0 4");

        String searchText = "T\u00ecm ph\u00f2ng tr\u1ed1ng";
        PrimaryButton searchButton = new PrimaryButton(searchText);
        searchButton.setBackground(new Color(17, 24, 39));
        searchButton.setForeground(Color.WHITE);
        searchButton.addActionListener(e -> runSearch());
        filterCard.add(searchButton, "h 44,gapy 6 0");

        return filterCard;
    }

    private JPanel makeBadgeIcon(Color bg, Color accent, String symbol) {
        JPanel box = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        box.setOpaque(false);
        box.setLayout(new BorderLayout());
        JLabel lbl = new JLabel(symbol, SwingConstants.CENTER);
        lbl.setForeground(accent);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 18f));
        box.add(lbl);
        return box;
    }

    private JPanel makeCalendarField(JTextField field) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(Color.WHITE);
        wrap.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 230), 1),
            BorderFactory.createEmptyBorder(0, 6, 0, 6)
        ));

        JLabel calIcon = new JLabel();
        ImageIcon calendarPNG = loadIcon("calendar.png", 16, 16);
        if (calendarPNG != null) {
            calIcon.setIcon(calendarPNG);
        } else {
            calIcon.setText("\u25A1 ");
            calIcon.setForeground(new Color(150, 165, 190));
            calIcon.setFont(calIcon.getFont().deriveFont(13f));
        }

        field.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        field.setOpaque(false);
        field.setForeground(new Color(60, 80, 110));

        wrap.add(calIcon, BorderLayout.WEST);
        wrap.add(field, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel createGuestStepper() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230), 1));
        panel.setBackground(Color.WHITE);

        JButton minus = createStepperBtn("-");
        JButton plus = createStepperBtn("+");

        guestCountLabel = new JLabel("\u2022 " + guestCount + " kh\u00e1ch", SwingConstants.CENTER);
        guestCountLabel.setForeground(new Color(30, 50, 80));
        guestCountLabel.setFont(guestCountLabel.getFont().deriveFont(13f));

        minus.addActionListener(e -> {
            if (guestCount > 1) {
                guestCount--;
                guestCountLabel.setText("\u2022 " + guestCount + " kh\u00e1ch");
            }
        });
        plus.addActionListener(e -> {
            if (guestCount < 4) {
                guestCount++;
                guestCountLabel.setText("\u2022 " + guestCount + " kh\u00e1ch");
            }
        });

        panel.add(minus, BorderLayout.WEST);
        panel.add(guestCountLabel, BorderLayout.CENTER);
        panel.add(plus, BorderLayout.EAST);
        return panel;
    }

    private JButton createStepperBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 18f));
        btn.setPreferredSize(new Dimension(44, 44));
        btn.setBackground(new Color(240, 244, 250));
        btn.setForeground(new Color(50, 70, 100));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder());
        return btn;
    }

    private JPanel createRightSide() {
        JPanel right = new JPanel(new MigLayout("wrap 1,insets 0,gap 12", "[grow,fill]", "[]"));
        right.setOpaque(false);

        // Stepper indicator
        JPanel stepper = createStepperPanel();

        bookingContent.setOpaque(false);
        bookingContent.add(createSelectRoomView(), "select-room");
        bookingContent.add(createCustomerInfoView(), "customer-info");

        right.add(stepper, "w 520!,alignx right");
        right.add(bookingContent, "grow");

        return right;
    }

    private JPanel createStepperPanel() {
        JPanel stepper = new JPanel(new MigLayout("insets 6 10,gap 0", "[grow,fill][grow,fill]", "[]"));
        stepper.setOpaque(false);
        stepper.setBorder(BorderFactory.createLineBorder(new Color(225, 231, 245), 1));
        stepper.setBackground(new Color(242, 246, 252));

        step1Label.setHorizontalAlignment(SwingConstants.CENTER);
        step2Label.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel s1Wrap = new JPanel(new BorderLayout());
        s1Wrap.setOpaque(false);
        s1Wrap.add(step1Label);

        JLabel arrow = new JLabel(" > ", SwingConstants.CENTER);
        arrow.setForeground(new Color(180, 190, 210));

        JPanel s2Wrap = new JPanel(new BorderLayout());
        s2Wrap.setOpaque(false);
        s2Wrap.add(step2Label);

        stepper.add(s1Wrap, "h 36");
        stepper.add(s2Wrap, "h 36");

        return stepper;
    }

    private JPanel createSelectRoomView() {
        JPanel panel = new JPanel(new MigLayout("wrap 1,insets 0,gap 12", "[grow,fill]", "[]"));
        panel.setOpaque(false);

        RoundedPanel overview = new RoundedPanel(16, new Color(18, 35, 67), new Color(40, 64, 112), 1f);
        overview.setLayout(new MigLayout("wrap 1,insets 16", "[grow,fill]", "[]"));

        JPanel ovTitleRow = new JPanel(new MigLayout("insets 0,gap 8", "[][]", "[]"));
        ovTitleRow.setOpaque(false);
        JLabel star = new JLabel();
        ImageIcon starPNG = loadIcon("star.png", 24, 24);
        if (starPNG != null) {
            star.setIcon(starPNG);
        } else {
            star.setText("\u2605");
            star.setForeground(new Color(237, 192, 54));
            star.setFont(star.getFont().deriveFont(22f));
        }
        JLabel ovTitle = new JLabel("T\u1ed5ng quan ph\u00f2ng kh\u00e1ch s\u1ea1n");
        ovTitle.setForeground(new Color(245, 248, 255));
        ovTitle.setFont(ovTitle.getFont().deriveFont(Font.BOLD, 22f));
        ovTitleRow.add(star);
        ovTitleRow.add(ovTitle);

        JLabel ovDesc = new JLabel("<html>Di\u1ec1n th\u00f4ng tin y\u00eau c\u1ea7u b\u00ean tr\u00e1i v\u00e0 nh\u1ea5n <b>\"T\u00ecm ph\u00f2ng tr\u1ed1ng\"</b> \u0111\u1ec3 b\u1eaft \u0111\u1ea7u \u0111\u1eb7t ph\u00f2ng. D\u01b0\u1edbi \u0111\u00e2y l\u00e0 t\u1ed5ng quan c\u00e1c lo\u1ea1i ph\u00f2ng hi\u1ec7n c\u00f3.</html>");
        ovDesc.setForeground(new Color(170, 188, 220));

        overview.add(ovTitleRow);
        overview.add(ovDesc);

        roomList.setOpaque(false);

        panel.add(overview, "growx");
        panel.add(roomList, "grow");
        return panel;
    }

    private JPanel createCustomerInfoView() {
        RoundedPanel panel = new RoundedPanel(16, new Color(242, 246, 252), new Color(225, 231, 245), 1f);
        panel.setLayout(new MigLayout("wrap 1,insets 18,gap 10", "[grow,fill]", "[]"));

        JLabel title = new JLabel("Th\u00f4ng tin kh\u00e1ch h\u00e0ng");
        title.setForeground(new Color(24, 40, 66));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));

        RoundedPanel selectedCard = new RoundedPanel(12, new Color(233, 240, 253), new Color(201, 216, 243), 1f);
        selectedCard.setLayout(new MigLayout("wrap 1,insets 10,gap 4", "[grow,fill]", "[]"));
        selectedRoomLabel.setForeground(new Color(38, 71, 126));
        selectedDateLabel.setForeground(new Color(68, 93, 135));
        selectedGuestLabel.setForeground(new Color(68, 93, 135));
        selectedCard.add(selectedRoomLabel);
        selectedCard.add(selectedDateLabel);
        selectedCard.add(selectedGuestLabel);

        customerNameField.putClientProperty("JTextField.placeholderText", "Nh\u1eadp h\u1ecd t\u00ean kh\u00e1ch");
        customerPhoneField.putClientProperty("JTextField.placeholderText", "Nh\u1eadp s\u1ed1 \u0111i\u1ec7n tho\u1ea1i");
        customerIdField.putClientProperty("JTextField.placeholderText", "CCCD/H\u1ed9 chi\u1ebfu");

        PrimaryButton backButton = new PrimaryButton("Quay l\u1ea1i ch\u1ecdn ph\u00f2ng");
        backButton.setBackground(new Color(226, 235, 250));
        backButton.setForeground(new Color(44, 71, 117));
        backButton.addActionListener(e -> {
            bookingCards.show(bookingContent, "select-room");
            setStep(1);
        });

        PrimaryButton confirmButton = new PrimaryButton("X\u00e1c nh\u1eadn \u0111\u1eb7t ph\u00f2ng");
        confirmButton.setBackground(ThemeColors.ACCENT);
        confirmButton.setForeground(Color.WHITE);
        confirmButton.addActionListener(e -> submitBooking());

        JPanel actions = new JPanel(new MigLayout("insets 0,gap 10", "[grow,fill][grow,fill]", "[]"));
        actions.setOpaque(false);
        actions.add(backButton, "h 44");
        actions.add(confirmButton, "h 44");

        panel.add(title);
        panel.add(selectedCard);
        panel.add(new JLabel("H\u1ecd v\u00e0 t\u00ean"));
        panel.add(customerNameField, "h 40");
        panel.add(new JLabel("S\u1ed1 \u0111i\u1ec7n tho\u1ea1i"));
        panel.add(customerPhoneField, "h 40");
        panel.add(new JLabel("Gi\u1ea5y t\u1edd tu\u1ef3 th\u00e2n"));
        panel.add(customerIdField, "h 40");
        panel.add(actions, "gapy 8 0");

        JPanel wrapper = new JPanel(new MigLayout("insets 0", "[grow,fill]", "[grow]"));
        wrapper.setOpaque(false);
        wrapper.add(panel, "grow");
        return wrapper;
    }

    private JPanel roomCard(RoomTypeData data) {
        RoundedPanel card = new RoundedPanel(16, data.bg, data.tone, 1f);
        card.setLayout(new MigLayout("wrap 1,insets 14,gap 6", "[grow,fill]", "[]"));

        // Top row: colored icon + room name + availability badge
        JPanel topRow = new JPanel(new MigLayout("insets 0,gap 8", "[][grow,fill][]", "[]"));
        topRow.setOpaque(false);

        JPanel roomIcon = makeRoomTypeIcon(data.tone, getRoomInitial(data.roomType));

        JLabel nameLbl = new JLabel(data.roomType);
        nameLbl.setForeground(new Color(30, 53, 86));
        nameLbl.setFont(nameLbl.getFont().deriveFont(Font.BOLD, 16f));

        JPanel availBadge = makeAvailBadge(data.status, data.tone);

        topRow.add(roomIcon, "w 36!,h 36!,aligny center");
        topRow.add(nameLbl, "aligny center");
        topRow.add(availBadge, "aligny center");

        // Price row
        JPanel priceRow = new JPanel(new MigLayout("insets 0,gap 4", "[][grow,fill]", "[]"));
        priceRow.setOpaque(false);
        JLabel priceLb = new JLabel(data.price);
        priceLb.setForeground(new Color(28, 42, 68));
        priceLb.setFont(priceLb.getFont().deriveFont(Font.BOLD, 22f));
        JLabel perNight = new JLabel(" /\u0111\u00eam");
        perNight.setForeground(new Color(100, 120, 150));
        perNight.setFont(perNight.getFont().deriveFont(13f));
        priceRow.add(priceLb);
        priceRow.add(perNight, "aligny bottom");

        // Progress bar row
        JPanel progressRow = new JPanel(new MigLayout("insets 0,gap 6", "[grow,fill][]", "[]"));
        progressRow.setOpaque(false);
        JLabel progressLbl = new JLabel("T\u1ef7 l\u1ec7 c\u00f2n tr\u1ed1ng");
        progressLbl.setForeground(new Color(100, 120, 150));
        progressLbl.setFont(progressLbl.getFont().deriveFont(12f));
        JLabel percentLbl = new JLabel(data.occupancyRate);
        percentLbl.setForeground(data.tone);
        percentLbl.setFont(percentLbl.getFont().deriveFont(Font.BOLD, 12f));

        JPanel labelRow = new JPanel(new MigLayout("insets 0", "[grow,fill][]", "[]"));
        labelRow.setOpaque(false);
        labelRow.add(progressLbl);
        labelRow.add(percentLbl);

        int pct = parsePercent(data.occupancyRate);
        JPanel progressBar = makeProgressBar(data.tone, pct);

        // Amenities
        JPanel amenitiesRow = new JPanel(new MigLayout("insets 0,gap 10", "[]".repeat(data.amenities.size()), "[]"));
        amenitiesRow.setOpaque(false);
        for (String amenity : data.amenities) {
            JLabel aLbl = new JLabel(amenity);
            ImageIcon icon = getAmenityIcon(amenity);
            if (icon != null) {
                aLbl.setIcon(icon);
                aLbl.setIconTextGap(6);
            }
            aLbl.setForeground(new Color(80, 100, 130));
            aLbl.setFont(aLbl.getFont().deriveFont(12f));
            amenitiesRow.add(aLbl);
        }

        String pickText = "Nh\u1ea5n \u0111\u1ec3 ch\u1ecdn lo\u1ea1i ph\u00f2ng n\u00e0y";
        PrimaryButton pickButton = new PrimaryButton(pickText);
        pickButton.setBackground(new Color(255, 255, 255, 200));
        pickButton.setForeground(new Color(36, 58, 91));
        pickButton.addActionListener(e -> selectRoom(data));

        card.add(topRow, "growx");
        card.add(priceRow, "gapy 4 0");
        card.add(labelRow, "growx");
        card.add(progressBar, "growx,h 6!");
        card.add(amenitiesRow, "gapy 4 0");
        card.add(pickButton, "h 38,gapy 4 0");

        return card;
    }

    private JPanel makeRoomTypeIcon(Color color, String letter) {
        JPanel circle = new JPanel(new BorderLayout());
        circle.setOpaque(false);
        
        ImageIcon icon = loadIcon(getIconFile(letter), 36, 36);
        if (icon != null) {
            JLabel iconLabel = new JLabel(icon);
            circle.add(iconLabel, BorderLayout.CENTER);
        } else {
            // Fallback: vẽ hình tròn như cũ nếu PNG không tìm được
            JPanel fallback = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
                    g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                    g2.setColor(color);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawOval(1, 1, getWidth() - 3, getHeight() - 3);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            fallback.setOpaque(false);
            fallback.setLayout(new BorderLayout());
            JLabel lbl = new JLabel(letter, SwingConstants.CENTER);
            lbl.setForeground(color);
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 13f));
            fallback.add(lbl);
            circle.add(fallback, BorderLayout.CENTER);
        }
        return circle;
    }

    private JPanel makeAvailBadge(String status, Color color) {
        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 25));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setLayout(new BorderLayout());
        badge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        JLabel lbl = new JLabel(status, SwingConstants.CENTER);
        lbl.setForeground(color);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 12f));
        badge.add(lbl);
        return badge;
    }

    private JPanel makeProgressBar(Color color, int percent) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(210, 220, 235));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                int fillWidth = Math.max(0, (int) (getWidth() * percent / 100.0));
                g2.setColor(color);
                g2.fillRoundRect(0, 0, fillWidth, getHeight(), getHeight(), getHeight());
                g2.dispose();
            }
        };
    }

    private String getRoomInitial(String roomType) {
        if (roomType.startsWith("Grand Premium 1")) return "G1";
        if (roomType.startsWith("Grand Premium 2")) return "G2";
        if (roomType.startsWith("Grand")) return "G";
        if (roomType.startsWith("Suite")) return "S";
        return roomType.substring(0, 1).toUpperCase();
    }

    private ImageIcon getAmenityIcon(String amenity) {
        switch (amenity) {
            case "Wifi": return loadIcon("wifi.png", 16, 16);
            case "Minibar": return loadIcon("minibar.png", 16, 16);
            case "Ban c\u00f4ng": return loadIcon("balcony.png", 16, 16);
            case "Ph\u00f2ng kh\u00e1ch": return loadIcon("room.png", 16, 16);
            default: return null;
        }
    }

    private ImageIcon loadIcon(String filename, int width, int height) {
        try {
            // Thử load từ classpath (khi chạy từ JAR)
            URL resource = getClass().getResource("/kqlhotel/resources/icons/" + filename);
            
            // Nếu không tìm được, thử load từ src folder (khi chạy từ bin)
            if (resource == null) {
                String srcPath = "src/kqlhotel/resources/icons/" + filename;
                java.io.File file = new java.io.File(srcPath);
                if (file.exists()) {
                    resource = file.toURI().toURL();
                }
            }
            
            if (resource != null) {
                ImageIcon icon = new ImageIcon(resource);
                Image scaledImage = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }
        } catch (Exception e) {
            // Icon loading failed silently
        }
        return null;
    }

    private String getIconFile(String letter) {
        switch (letter) {
            case "D": return "deluxe.png";
            case "G1": return "grand-premium-1.png";
            case "G2": return "grand-premium-2.png";
            case "S": return "suite.png";
            default: return null;
        }
    }

    private int parsePercent(String pct) {
        try {
            return Integer.parseInt(pct.replace("%", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void runSearch() {
        String selectedType = (String) roomTypeCombo.getSelectedItem();
        String checkIn = checkInField.getText().trim();
        String checkOut = checkOutField.getText().trim();

        if (checkIn.isEmpty() || checkOut.isEmpty() || checkIn.equalsIgnoreCase("dd/mm/yyyy") || checkOut.equalsIgnoreCase("dd/mm/yyyy")) {
            JOptionPane.showMessageDialog(
                this,
                "Vui l\u00f2ng nh\u1eadp ng\u00e0y nh\u1eadn v\u00e0 tr\u1ea3 ph\u00f2ng theo \u0111\u1ecbnh d\u1ea1ng dd/mm/yyyy.",
                "Thi\u1ebfu th\u00f4ng tin",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (guestCount < 1 || guestCount > 4) {
            JOptionPane.showMessageDialog(
                this,
                "S\u1ed1 kh\u00e1ch ph\u1ea3i trong kho\u1ea3ng t\u1eeb 1 \u0111\u1ebfn 4.",
                "D\u1eef li\u1ec7u kh\u00f4ng h\u1ee3p l\u1ec7",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        List<RoomTypeData> filtered = new ArrayList<>();
        for (RoomTypeData room : roomData) {
            boolean matchType = "T\u1ea5t c\u1ea3".equals(selectedType)
                || room.roomType.equalsIgnoreCase(selectedType)
                || room.roomType.startsWith(selectedType);
            boolean hasCapacity = room.availableRooms > 0;
            if (matchType && hasCapacity) {
                filtered.add(room);
            }
        }

        renderRooms(filtered);
        bookingCards.show(bookingContent, "select-room");
        setStep(1);
    }

    private void selectRoom(RoomTypeData data) {
        selectedRoom = data;
        selectedRoomLabel.setText("\u0110\u00e3 ch\u1ecdn: " + data.roomType + " - " + data.price + " /\u0111\u00eam");
        selectedDateLabel.setText("Ng\u00e0y nh\u1eadn/tr\u1ea3: " + checkInField.getText().trim() + " -> " + checkOutField.getText().trim());
        selectedGuestLabel.setText("S\u1ed1 kh\u00e1ch: " + guestCount);
        bookingCards.show(bookingContent, "customer-info");
        setStep(2);
    }

    private void submitBooking() {
        if (selectedRoom == null) {
            JOptionPane.showMessageDialog(this, "Vui l\u00f2ng ch\u1ecdn lo\u1ea1i ph\u00f2ng tr\u01b0\u1edbc.", "Thi\u1ebfu th\u00f4ng tin", JOptionPane.WARNING_MESSAGE);
            setStep(1);
            bookingCards.show(bookingContent, "select-room");
            return;
        }

        String name = customerNameField.getText().trim();
        String phone = customerPhoneField.getText().trim();
        String id = customerIdField.getText().trim();

        if (name.isEmpty() || phone.isEmpty() || id.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui l\u00f2ng nh\u1eadp \u0111\u1ea7y \u0111\u1ee7 th\u00f4ng tin kh\u00e1ch h\u00e0ng.", "Thi\u1ebfu th\u00f4ng tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(
            this,
            "\u0110\u1eb7t ph\u00f2ng th\u00e0nh c\u00f4ng cho kh\u00e1ch " + name + " (" + selectedRoom.roomType + ")",
            "Th\u00e0nh c\u00f4ng",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void renderRooms(List<RoomTypeData> data) {
        roomList.removeAll();

        if (data.isEmpty()) {
            RoundedPanel empty = new RoundedPanel(16, new Color(29, 46, 78), new Color(255, 255, 255, 20), 1f);
            empty.setLayout(new MigLayout("insets 20", "[grow,fill]", "[]"));
            JLabel msg = new JLabel("Kh\u00f4ng t\u00ecm th\u1ea5y lo\u1ea1i ph\u00f2ng ph\u00f9 h\u1ee3p.", SwingConstants.CENTER);
            msg.setForeground(new Color(239, 244, 255));
            empty.add(msg);
            roomList.add(empty, "span 2,growx");
        } else {
            for (RoomTypeData room : data) {
                roomList.add(roomCard(room));
            }
        }

        roomList.revalidate();
        roomList.repaint();
    }

    private void setStep(int step) {
        if (step == 1) {
            step1Label.setText("1  Ch\u1ecdn ph\u00f2ng");
            step2Label.setText("2  Th\u00f4ng tin kh\u00e1ch h\u00e0ng");
            step1Label.setOpaque(true);
            step2Label.setOpaque(true);
            step1Label.setBackground(new Color(18, 35, 67));
            step1Label.setForeground(new Color(245, 248, 255));
            step2Label.setBackground(new Color(230, 238, 252));
            step2Label.setForeground(new Color(119, 137, 168));
        } else {
            step1Label.setText("1  Ch\u1ecdn ph\u00f2ng");
            step2Label.setText("2  Th\u00f4ng tin kh\u00e1ch h\u00e0ng");
            step1Label.setOpaque(true);
            step2Label.setOpaque(true);
            step1Label.setBackground(new Color(230, 238, 252));
            step1Label.setForeground(new Color(119, 137, 168));
            step2Label.setBackground(new Color(18, 35, 67));
            step2Label.setForeground(new Color(245, 248, 255));
        }
    }

    private static final class RoomTypeData {
        private final String roomType;
        private final String price;
        private final String status;
        private final String occupancyRate;
        private final List<String> amenities;
        private final Color bg;
        private final Color tone;
        private final int availableRooms;

        private RoomTypeData(String roomType, String price, String status, String occupancyRate, List<String> amenities, Color bg, Color tone, int availableRooms) {
            this.roomType = roomType;
            this.price = price;
            this.status = status;
            this.occupancyRate = occupancyRate;
            this.amenities = amenities;
            this.bg = bg;
            this.tone = tone;
            this.availableRooms = availableRooms;
        }
    }
}
