package kqlhotel.gui.tabs;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import kqlhotel.bus.booking.BookingService;
import kqlhotel.bus.booking.BookingServiceProvider;
import kqlhotel.bus.booking.BookingConfirmationResult;
import kqlhotel.bus.booking.BookingSearchRequest;
import kqlhotel.bus.booking.BookingSelectionSummary;
import kqlhotel.bus.booking.CreateBookingCommand;
import kqlhotel.bus.booking.GuestInfoDto;
import kqlhotel.bus.booking.RoomOptionDto;
import kqlhotel.bus.customer.CustomerDirectoryServiceProvider;
import kqlhotel.gui.components.PrimaryButton;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.gui.theme.ThemeColors;
import kqlhotel.gui.utils.IconLoader;
import kqlhotel.gui.model.RoomCardData;
import kqlhotel.gui.components.RoomCard;
import kqlhotel.gui.Session;
import javax.swing.JDialog;
import net.miginfocom.swing.MigLayout;

public class BookingPanel extends JPanel {
    private static final Color PAGE_BG = new Color(245, 248, 252);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int ROOMS_PER_SLIDE = 4;

    private final JComboBox<String> roomTypeCombo = new JComboBox<>();
    private final JTextField checkInField = new JTextField("dd/mm/yyyy");
    private final JTextField checkOutField = new JTextField("dd/mm/yyyy");
    private LocalDate selectedCheckInDate;
    private LocalDate selectedCheckOutDate;
    private static final int MAX_ADULTS = 10;
    private static final int MAX_CHILDREN = 10;
    private int adultsCount = 2;
    private int childrenCount = 0;
    private JLabel adultsCountLabel;
    private JLabel childrenCountLabel;

    private final CardLayout bookingCards = new CardLayout();
    private final JPanel bookingContent = new JPanel(bookingCards);

    // Row height is fixed ("[205!]") so the 2x2 grid always occupies the same
    // vertical space regardless of how many real cards (1..ROOMS_PER_SLIDE) are
    // shown on the current slide. 205px is tuned tight against the densest card
    // so the panel fits within the viewport leaving 24px whitespace at the bottom.
    private final JPanel roomList = new JPanel(new MigLayout("wrap 2,insets 0,gap 10", "[grow,fill][grow,fill]", "[205!]"));
    private final JLabel selectedRoomsLabel = new JLabel("Chưa chọn phòng");
    private final JLabel selectedDateLabel = new JLabel("Ngày nhận/trả: --");
    private final JLabel selectedGuestLabel = new JLabel("Số khách: --");
    private final JLabel guestFormsTitleLabel = new JLabel("Danh sách khách");
    private final JPanel guestFormsPanel = new JPanel(new MigLayout("wrap 1,insets 0,gap 8", "[grow,fill]", "[]"));
    private final List<GuestFormRow> guestFormRows = new ArrayList<>();

    private final BookingService bookingService;
    private BookingSearchRequest lastSearchRequest;

    private final List<RoomCardData> selectedRooms = new ArrayList<>();
    private final List<RoomCardData> displayedRooms = new ArrayList<>();
    private final JLabel selectionCountLabel = new JLabel("0 phòng đã chọn");
    private final JLabel selectionDetailLabel = new JLabel("Tổng: 0đ");
    private final JLabel slideInfoLabel = new JLabel("Slide 1/1");
    private PrimaryButton continueToGuestButton;
    private PrimaryButton searchButton;
    private JButton prevSlideButton;
    private JButton nextSlideButton;
    private JButton childrenMinusButton;
    private JButton childrenPlusButton;
    private JButton adultsMinusButton;
    private JButton adultsPlusButton;
    private boolean filterLocked;
    private boolean capacityHintsVisible;
    private int currentSlideIndex;
    private kqlhotel.entity.Customer preFilledCustomer;

    public BookingPanel() {
        this.bookingService = BookingServiceProvider.get();
        initializeDefaultDates();
        
        loadRoomTypes();

        setOpaque(false);
        setBackground(PAGE_BG);
        setLayout(new MigLayout("insets 24,gap 20", "[grow 34][grow 66]", "[]"));

        RoundedPanel filterCard = createFilterCard();
        JPanel rightSide = createRightSide();

        // Anchor filter card to the top so its inner whitespace doesn't expand
        // to fill the entire page height (which made users feel there was hidden
        // content scrollable below the search button).
        add(filterCard, "aligny top");
        add(rightSide, "grow");

        renderInitialRooms();
        setStep(1);
    }

    private void initializeDefaultDates() {
        selectedCheckInDate = LocalDate.now();
        selectedCheckOutDate = selectedCheckInDate.plusDays(1);
        checkInField.setText(selectedCheckInDate.format(DATE_FORMAT));
        checkOutField.setText(selectedCheckOutDate.format(DATE_FORMAT));
    }

    private void loadRoomTypes() {
        roomTypeCombo.removeAllItems();
        roomTypeCombo.addItem("Tất cả");
        try {
            List<String> types = bookingService.getRoomTypes();
            if (types != null) {
                for (String t : types) {
                    roomTypeCombo.addItem(t);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderInitialRooms() {
        BookingSearchRequest initialRequest = new BookingSearchRequest("Tất cả", selectedCheckInDate, selectedCheckOutDate, adultsCount, childrenCount);
        lastSearchRequest = initialRequest;
        capacityHintsVisible = false;
        List<RoomOptionDto> rooms = bookingService.searchAvailableRooms(initialRequest);
        renderRooms(mapToCardData(rooms));
    }

    private RoundedPanel createFilterCard() {
        RoundedPanel filterCard = new RoundedPanel(16, Color.WHITE, new Color(225, 231, 245), 1f);
        filterCard.setLayout(new MigLayout("wrap 1,insets 18,gap 10", "[grow,fill]", "[]"));

        // Title row with icon
        JPanel titleRow = new JPanel(new MigLayout("insets 0,gap 10", "[][grow,fill]", "[]"));
        titleRow.setOpaque(false);

        JPanel iconBox = new JPanel(new BorderLayout());
        iconBox.setOpaque(false);
        ImageIcon filterIcon = IconLoader.loadIcon("filter-badge.png", 44, 44);
        if (filterIcon != null) {
            iconBox.add(new JLabel(filterIcon), BorderLayout.CENTER);
        } else {
            JPanel fallback = makeBadgeIcon(new Color(235, 248, 255), new Color(49, 130, 206), "\u25A1");
            iconBox = fallback;
        }

        JPanel titleText = new JPanel(new MigLayout("insets 0,wrap 1,gap 2", "[grow,fill]", "[]"));
        titleText.setOpaque(false);
        JLabel title = new JLabel("Yêu cầu phòng");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(new Color(24, 40, 66));
        JLabel subtitle = new JLabel("Chọn loại phòng, ngày & số khách để tìm kiếm");
        subtitle.setForeground(new Color(102, 124, 155));
        subtitle.setFont(subtitle.getFont().deriveFont(12f));
        titleText.add(title);
        titleText.add(subtitle);

        titleRow.add(iconBox, "w 44!,h 44!");
        titleRow.add(titleText, "aligny center");

        filterCard.add(titleRow, "gapy 0 8");
        filterCard.add(new JLabel("Loại phòng"));
        filterCard.add(roomTypeCombo, "h 40");

        // Side-by-side date fields, each with label above the input
        JPanel dateRow = new JPanel(new MigLayout("insets 0,gap 10,wrap 2", "[grow,fill][grow,fill]", "[]"));
        dateRow.setOpaque(false);

        dateRow.add(makeDateBlock("Nhận phòng *", checkInField), "grow");
        dateRow.add(makeDateBlock("Trả phòng *", checkOutField), "grow");

        filterCard.add(dateRow);
        filterCard.add(new JLabel("Khách"));
        filterCard.add(createGuestStepper(), "h 88");

        JLabel note = new JLabel("Tối đa 2 trẻ < 12 tuổi/phòng");
        note.setForeground(new Color(150, 165, 190));
        note.setFont(note.getFont().deriveFont(11f));
        filterCard.add(note, "gapy 0 4");

        String searchText = "Tìm phòng trống";
        searchButton = new PrimaryButton(searchText);
        searchButton.setBackground(ThemeColors.PREMIUM_PRIMARY);
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
        ImageIcon calendarPNG = IconLoader.loadIcon("calendar.png", 16, 16);
        if (calendarPNG != null) {
            calIcon.setIcon(calendarPNG);
        } else {
            calIcon.setText("\u25A1 ");
            calIcon.setForeground(new Color(150, 165, 190));
            calIcon.setFont(calIcon.getFont().deriveFont(13f));
        }

        field.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        field.setOpaque(false);
        field.setEditable(false);
        field.setFocusable(false);
        field.setForeground(new Color(60, 80, 110));
        field.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));

        wrap.add(calIcon, BorderLayout.WEST);
        wrap.add(field, BorderLayout.CENTER);

        MouseAdapter openPicker = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (filterLocked) {
                    return;
                }
                showDatePicker(wrap, field);
            }
        };
        wrap.addMouseListener(openPicker);
        calIcon.addMouseListener(openPicker);
        field.addMouseListener(openPicker);
        return wrap;
    }

    private JPanel makeDateBlock(String labelText, JTextField field) {
        JPanel block = new JPanel(new MigLayout("insets 0,wrap 1,gap 6", "[grow,fill]", "[]"));
        block.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setForeground(new Color(30, 50, 80));
        label.setFont(label.getFont().deriveFont(13f));

        block.add(label);
        block.add(makeCalendarField(field), "h 40,growx");
        return block;
    }

    private void showDatePicker(JPanel anchor, JTextField targetField) {
        LocalDate today = LocalDate.now();
        boolean isCheckOutPicker = targetField == checkOutField;
        LocalDate initialDate;
        if (targetField == checkInField && selectedCheckInDate != null) {
            initialDate = selectedCheckInDate;
        } else if (targetField == checkOutField && selectedCheckOutDate != null) {
            initialDate = selectedCheckOutDate;
        } else {
            initialDate = LocalDate.now();
        }

        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(new Color(200, 210, 230), 1));

        JPanel calendar = new JPanel(new MigLayout("insets 12,gap 8,wrap 7", "[center][center][center][grow,fill][center][center][center]", "[]"));
        calendar.setBackground(Color.WHITE);

        JLabel monthYearLabel = new JLabel();
        monthYearLabel.setForeground(new Color(24, 40, 66));
        monthYearLabel.setFont(monthYearLabel.getFont().deriveFont(Font.BOLD, 13f));

        JButton prevButton = new JButton("<");
        JButton nextButton = new JButton(">");
        prevButton.setFocusable(false);
        nextButton.setFocusable(false);

        LocalDate[] displayedMonth = new LocalDate[]{initialDate.withDayOfMonth(1)};
        JPanel daysGrid = new JPanel(new MigLayout("insets 0,gap 4,wrap 7", "[24!][24!][24!][24!][24!][24!][24!]", "[]"));
        daysGrid.setOpaque(false);
        final Runnable[] refreshCalendar = new Runnable[1];

        prevButton.addActionListener(ev -> {
            displayedMonth[0] = displayedMonth[0].minusMonths(1).withDayOfMonth(1);
            refreshCalendar[0].run();
            popup.pack();
        });
        nextButton.addActionListener(ev -> {
            displayedMonth[0] = displayedMonth[0].plusMonths(1).withDayOfMonth(1);
            refreshCalendar[0].run();
            popup.pack();
        });

        refreshCalendar[0] = () -> {
            calendar.removeAll();
            daysGrid.removeAll();

            LocalDate monthBase = displayedMonth[0];
            monthYearLabel.setText(monthBase.getMonth().name() + " " + monthBase.getYear());

            calendar.add(prevButton, "span 1, w 28!, h 28!");
            calendar.add(monthYearLabel, "span 5, alignx center");
            calendar.add(nextButton, "span 1, w 28!, h 28!");

            String[] weekdays = {"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"};
            for (String weekday : weekdays) {
                JLabel weekdayLabel = new JLabel(weekday, SwingConstants.CENTER);
                weekdayLabel.setForeground(new Color(120, 135, 160));
                weekdayLabel.setFont(weekdayLabel.getFont().deriveFont(Font.BOLD, 11f));
                daysGrid.add(weekdayLabel);
            }

            int firstDayIndex = monthBase.getDayOfWeek().getValue();
            for (int i = 1; i < firstDayIndex; i++) {
                daysGrid.add(new JLabel(""));
            }

            int maxDays = YearMonth.of(monthBase.getYear(), monthBase.getMonth()).lengthOfMonth();
            for (int day = 1; day <= maxDays; day++) {
                final int selectedDay = day;
                LocalDate buttonDate = LocalDate.of(monthBase.getYear(), monthBase.getMonth(), selectedDay);
                JButton dayButton = new JButton(String.valueOf(day));
                dayButton.setFocusable(false);
                dayButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
                dayButton.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
                dayButton.setBackground(Color.WHITE);
                dayButton.setForeground(new Color(40, 58, 91));
                dayButton.setPreferredSize(new Dimension(24, 24));

                LocalDate minCheckoutBase = selectedCheckInDate != null ? selectedCheckInDate : today;
                boolean disabled = isCheckOutPicker
                    ? !buttonDate.isAfter(minCheckoutBase)
                    : buttonDate.isBefore(today);

                if (disabled) {
                    dayButton.setEnabled(false);
                    dayButton.setBackground(new Color(45, 45, 45));
                    dayButton.setForeground(new Color(220, 220, 220));
                } else if (buttonDate.equals(today)) {
                    dayButton.setBorder(BorderFactory.createLineBorder(new Color(58, 119, 246), 1));
                    dayButton.setBackground(new Color(232, 242, 255));
                }

                dayButton.addActionListener(e -> {
                    LocalDate pickedDate = LocalDate.of(monthBase.getYear(), monthBase.getMonth(), selectedDay);
                    targetField.setText(pickedDate.format(DATE_FORMAT));

                    if (targetField == checkInField) {
                        selectedCheckInDate = pickedDate;
                        if (selectedCheckOutDate == null || !selectedCheckOutDate.isAfter(selectedCheckInDate)) {
                            selectedCheckOutDate = selectedCheckInDate.plusDays(1);
                            checkOutField.setText(selectedCheckOutDate.format(DATE_FORMAT));
                        }
                    } else if (targetField == checkOutField) {
                        selectedCheckOutDate = pickedDate;
                    }

                    popup.setVisible(false);
                });
                daysGrid.add(dayButton);
            }

            calendar.add(daysGrid, "span 7, growx");
            calendar.revalidate();
            calendar.repaint();
        };

        refreshCalendar[0].run();
        popup.add(calendar);
        popup.show(anchor, 0, anchor.getHeight());
    }

    private JPanel createGuestStepper() {
        JPanel wrapper = new JPanel(new MigLayout("insets 0, gap 4, wrap 1", "[grow,fill]", "[][]"));
        wrapper.setOpaque(false);

        // Adults
        JPanel adultsPanel = new JPanel(new BorderLayout());
        adultsPanel.setBorder(BorderFactory.createLineBorder(new Color(193, 206, 231), 1));
        adultsPanel.setBackground(new Color(244, 248, 255));
        adultsMinusButton = createStepperBtn("-");
        adultsPlusButton = createStepperBtn("+");
        adultsCountLabel = new JLabel(adultsCount + " người lớn", SwingConstants.CENTER);
        adultsCountLabel.setForeground(new Color(26, 49, 86));
        adultsCountLabel.setFont(adultsCountLabel.getFont().deriveFont(Font.BOLD, 13f));
        JPanel adultsCenter = new JPanel(new BorderLayout());
        adultsCenter.setOpaque(true);
        adultsCenter.setBackground(Color.WHITE);
        adultsCenter.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 1, new Color(221, 231, 248)));
        adultsCenter.add(adultsCountLabel, BorderLayout.CENTER);
        adultsMinusButton.addActionListener(e -> {
            if (!filterLocked && adultsCount > 1) { adultsCount--; updateGuestStepperState(); syncGuestForms(); }
        });
        adultsPlusButton.addActionListener(e -> {
            if (!filterLocked && adultsCount < MAX_ADULTS) { adultsCount++; updateGuestStepperState(); syncGuestForms(); }
        });
        adultsPanel.add(adultsMinusButton, BorderLayout.WEST);
        adultsPanel.add(adultsCenter, BorderLayout.CENTER);
        adultsPanel.add(adultsPlusButton, BorderLayout.EAST);

        // Children
        JPanel childrenPanel = new JPanel(new BorderLayout());
        childrenPanel.setBorder(BorderFactory.createLineBorder(new Color(193, 206, 231), 1));
        childrenPanel.setBackground(new Color(244, 248, 255));
        childrenMinusButton = createStepperBtn("-");
        childrenPlusButton = createStepperBtn("+");
        childrenCountLabel = new JLabel(childrenCount + " trẻ em (<12)", SwingConstants.CENTER);
        childrenCountLabel.setForeground(new Color(26, 49, 86));
        childrenCountLabel.setFont(childrenCountLabel.getFont().deriveFont(Font.BOLD, 13f));
        JPanel childrenCenter = new JPanel(new BorderLayout());
        childrenCenter.setOpaque(true);
        childrenCenter.setBackground(Color.WHITE);
        childrenCenter.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 1, new Color(221, 231, 248)));
        childrenCenter.add(childrenCountLabel, BorderLayout.CENTER);
        childrenMinusButton.addActionListener(e -> {
            if (!filterLocked && childrenCount > 0) { childrenCount--; updateGuestStepperState(); syncGuestForms(); }
        });
        childrenPlusButton.addActionListener(e -> {
            if (!filterLocked && childrenCount < MAX_CHILDREN) { childrenCount++; updateGuestStepperState(); syncGuestForms(); }
        });
        childrenPanel.add(childrenMinusButton, BorderLayout.WEST);
        childrenPanel.add(childrenCenter, BorderLayout.CENTER);
        childrenPanel.add(childrenPlusButton, BorderLayout.EAST);

        updateGuestStepperState();
        wrapper.add(adultsPanel, "h 42");
        wrapper.add(childrenPanel, "h 42");
        return wrapper;
    }

    private JButton createStepperBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 20f));
        btn.setPreferredSize(new Dimension(44, 44));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(36, 63, 106));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!btn.isEnabled()) {
                    return;
                }
                btn.setBackground(new Color(231, 239, 252));
                btn.setForeground(new Color(23, 55, 102));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                updateGuestStepperState();
            }
        });
        return btn;
    }

    private void updateGuestStepperState() {
        if (adultsMinusButton == null) return;
        adultsCountLabel.setText(adultsCount + " người lớn");
        childrenCountLabel.setText(childrenCount + " trẻ em (<12)");
        applyGuestStepperButtonState(adultsMinusButton, !filterLocked && adultsCount > 1);
        applyGuestStepperButtonState(adultsPlusButton, !filterLocked && adultsCount < MAX_ADULTS);
        applyGuestStepperButtonState(childrenMinusButton, !filterLocked && childrenCount > 0);
        applyGuestStepperButtonState(childrenPlusButton, !filterLocked && childrenCount < MAX_CHILDREN);
        Color textCol = filterLocked ? new Color(138, 149, 170) : new Color(26, 49, 86);
        adultsCountLabel.setForeground(textCol);
        childrenCountLabel.setForeground(textCol);
    }

    private void applyGuestStepperButtonState(JButton button, boolean enabled) {
        button.setEnabled(enabled);
        if (enabled) {
            button.setBackground(Color.WHITE);
            button.setForeground(new Color(36, 63, 106));
        } else {
            button.setBackground(new Color(236, 241, 249));
            button.setForeground(new Color(158, 170, 194));
        }
    }

    private JPanel createRightSide() {
        JPanel right = new JPanel(new MigLayout("wrap 1,insets 0,gap 10", "[grow,fill]", "[]"));
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

    // Stepper UI: two rounded pills, each with a circle badge holding the step
    // number. The active pill is dark navy + white text + white circle with
    // navy number. The inactive pill is soft slate background + muted text.
    private StepPill step1Pill;
    private StepPill step2Pill;

    private JPanel createStepperPanel() {
        JPanel stepper = new JPanel(new MigLayout("insets 0,gap 8", "[grow,fill][grow,fill]", "[]"));
        stepper.setOpaque(false);

        step1Pill = new StepPill("1", "Chọn phòng");
        step2Pill = new StepPill("2", "Thông tin khách hàng");

        stepper.add(step1Pill, "h 38!");
        stepper.add(step2Pill, "h 38!");
        return stepper;
    }

    /**
     * Rounded "pill" used for the booking stepper: circular numbered badge
     * on the left + descriptive text on the right. The active state uses the
     * brand navy background; inactive uses a calm slate tint. Paints its own
     * rounded background so we can swap colours on activation.
     */
    private static class StepPill extends JPanel {
        private static final int ARC = 12;
        private final CircleBadge badge;
        private final JLabel textLabel;
        private Color bgColor = COLOR_PILL_INACTIVE_BG;
        private Color borderColor = COLOR_PILL_BORDER;

        StepPill(String number, String text) {
            setOpaque(false);
            // GridBagLayout centers a single child both horizontally and vertically
            // with no extra constraints. We build [badge | text] as ONE group and
            // drop it in the centre — this guarantees the group's vertical
            // position is consistent (top + bottom margins equal) regardless of
            // the pill's actual height.
            setLayout(new java.awt.GridBagLayout());

            badge = new CircleBadge(number);
            textLabel = new JLabel(text);
            textLabel.setFont(textLabel.getFont().deriveFont(Font.BOLD, 13f));

            JPanel group = new JPanel(new MigLayout("insets 0,gap 8", "[][]", "[center]"));
            group.setOpaque(false);
            group.add(badge, "w 24!,h 24!");
            group.add(textLabel);

            add(group); // default GridBagConstraints centers the group
            setActive(false);
        }

        void setActive(boolean active) {
            if (active) {
                bgColor = COLOR_PILL_ACTIVE_BG;
                borderColor = COLOR_PILL_ACTIVE_BG;
                textLabel.setForeground(COLOR_PILL_ACTIVE_TEXT);
                badge.setColors(COLOR_PILL_ACTIVE_TEXT, COLOR_PILL_ACTIVE_BG);
            } else {
                bgColor = COLOR_PILL_INACTIVE_BG;
                borderColor = COLOR_PILL_BORDER;
                textLabel.setForeground(COLOR_PILL_INACTIVE_TEXT);
                badge.setColors(COLOR_PILL_INACTIVE_BADGE_BG, COLOR_PILL_INACTIVE_BADGE_TEXT);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
            g2.setColor(borderColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Small circular badge that paints a coloured circle with a number drawn
     * directly using FontMetrics so the number is precisely centered (a JLabel
     * inside a BorderLayout drifts a pixel or two off due to font baseline).
     */
    private static class CircleBadge extends JPanel {
        private static final Font BADGE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 12);
        private Color bg = COLOR_PILL_INACTIVE_BADGE_BG;
        private Color textColor = COLOR_PILL_INACTIVE_BADGE_TEXT;
        private final String number;

        CircleBadge(String number) {
            this.number = number;
            setOpaque(false);
        }

        void setColors(Color bgColor, Color textColor) {
            this.bg = bgColor;
            this.textColor = textColor;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Filled circle
            g2.setColor(bg);
            g2.fillOval(0, 0, w, h);

            // Centered number using exact font metrics
            g2.setColor(textColor);
            g2.setFont(BADGE_FONT);
            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(number);
            int textH = fm.getAscent() - fm.getDescent();
            int x = (w - textW) / 2;
            int y = (h + textH) / 2;
            g2.drawString(number, x, y);
            g2.dispose();
        }
    }

    // Stepper palette (aligned with PREMIUM brand navy)
    private static final Color COLOR_PILL_ACTIVE_BG          = new Color(0x1E3A8A); // ThemeColors.PREMIUM_PRIMARY
    private static final Color COLOR_PILL_ACTIVE_TEXT        = new Color(245, 248, 255);
    private static final Color COLOR_PILL_INACTIVE_BG        = new Color(238, 243, 250);
    private static final Color COLOR_PILL_INACTIVE_TEXT      = new Color(118, 134, 162);
    private static final Color COLOR_PILL_INACTIVE_BADGE_BG  = new Color(208, 217, 234);
    private static final Color COLOR_PILL_INACTIVE_BADGE_TEXT= new Color(118, 134, 162);
    private static final Color COLOR_PILL_BORDER             = new Color(225, 231, 245);

    private JPanel createSelectRoomView() {
        JPanel panel = new JPanel(new MigLayout("wrap 1,insets 0,gap 10", "[grow,fill]", "[]"));
        panel.setOpaque(false);

        RoundedPanel overview = new RoundedPanel(16, new Color(18, 35, 67), new Color(40, 64, 112), 1f);
        overview.setLayout(new MigLayout("wrap 1,insets 12 16", "[grow,fill]", "[]"));

        JPanel ovTitleRow = new JPanel(new MigLayout("insets 0,gap 8", "[][]", "[]"));
        ovTitleRow.setOpaque(false);
        JLabel star = new JLabel();
        ImageIcon starPNG = IconLoader.loadIcon("star-book.png", 24, 24);
        if (starPNG != null) {
            star.setIcon(starPNG);
        } else {
            star.setText("★");
            star.setForeground(new Color(237, 192, 54));
            star.setFont(star.getFont().deriveFont(22f));
        }
        JLabel ovTitle = new JLabel("Tổng quan phòng khách sạn");
        ovTitle.setForeground(new Color(245, 248, 255));
        ovTitle.setFont(ovTitle.getFont().deriveFont(Font.BOLD, 18f));
        ovTitleRow.add(star);
        ovTitleRow.add(ovTitle);

        JLabel ovDesc = new JLabel("<html>Điền thông tin yêu cầu bên trái và nhấn <b>\"Tìm phòng trống\"</b> để bắt đầu đặt phòng. Dưới đây là tổng quan các loại phòng hiện có.</html>");
        ovDesc.setForeground(new Color(170, 188, 220));

        overview.add(ovTitleRow);
        overview.add(ovDesc);

        roomList.setOpaque(false);

        panel.add(overview, "growx");
        panel.add(createSlideControls(), "growx");
        panel.add(roomList, "grow");

        RoundedPanel selectionBar = new RoundedPanel(18, new Color(20, 31, 59), new Color(45, 66, 110), 1f);
        selectionBar.setLayout(new MigLayout("insets 14 16,gap 12", "[][grow,fill][]", "[]"));

        JPanel selectionIcon = new JPanel(new BorderLayout());
        selectionIcon.setOpaque(false);
        selectionIcon.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        ImageIcon selectionPng = IconLoader.loadIcon("bed.png", 28, 28);
        if (selectionPng != null) {
            selectionIcon.add(new JLabel(selectionPng, SwingConstants.CENTER), BorderLayout.CENTER);
        } else {
            selectionIcon = makeBadgeIcon(new Color(35, 53, 92), new Color(81, 130, 255), "\u25A1");
        }
        selectionCountLabel.setForeground(Color.WHITE);
        selectionCountLabel.setFont(selectionCountLabel.getFont().deriveFont(Font.BOLD, 15f));
        selectionDetailLabel.setForeground(new Color(170, 188, 220));

        JPanel selectionTextWrap = new JPanel(new MigLayout("insets 0,wrap 1,gap 2", "[grow,fill]", "[]"));
        selectionTextWrap.setOpaque(false);
        selectionTextWrap.add(selectionCountLabel);
        selectionTextWrap.add(selectionDetailLabel);

        continueToGuestButton = new PrimaryButton("Nhập thông tin khách");
        continueToGuestButton.setBackground(ThemeColors.PREMIUM_PRIMARY);
        continueToGuestButton.setForeground(Color.WHITE);
        continueToGuestButton.addActionListener(e -> openCustomerInfo());

        selectionBar.add(selectionIcon, "w 44!,h 44!");
        selectionBar.add(selectionTextWrap, "growx");
        selectionBar.add(continueToGuestButton, "h 44");

        panel.add(selectionBar, "growx");
        return panel;
    }

    private JPanel createSlideControls() {
        JPanel controls = new JPanel(new MigLayout("insets 4 0 0 0,gap 8", "[][grow,fill][]", "[]"));
        controls.setOpaque(false);

        prevSlideButton = new JButton("<");
        prevSlideButton.setFocusable(false);
        prevSlideButton.setPreferredSize(new Dimension(34, 30));
        prevSlideButton.addActionListener(e -> {
            if (currentSlideIndex > 0) {
                currentSlideIndex--;
                renderCurrentSlide();
            }
        });

        nextSlideButton = new JButton(">");
        nextSlideButton.setFocusable(false);
        nextSlideButton.setPreferredSize(new Dimension(34, 30));
        nextSlideButton.addActionListener(e -> {
            int maxSlide = getMaxSlideIndex();
            if (currentSlideIndex < maxSlide) {
                currentSlideIndex++;
                renderCurrentSlide();
            }
        });

        slideInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        slideInfoLabel.setForeground(new Color(84, 104, 136));
        slideInfoLabel.setFont(slideInfoLabel.getFont().deriveFont(Font.BOLD, 12f));

        controls.add(prevSlideButton, "w 34!,h 30!");
        controls.add(slideInfoLabel, "alignx center");
        controls.add(nextSlideButton, "w 34!,h 30!");
        return controls;
    }

    private JPanel createCustomerInfoView() {
        RoundedPanel panel = new RoundedPanel(16, new Color(242, 246, 252), new Color(225, 231, 245), 1f);
        panel.setLayout(new MigLayout("wrap 1,insets 18,gap 10", "[grow,fill]", "[]"));

        JLabel title = new JLabel("Thông tin khách hàng");
        title.setForeground(new Color(24, 40, 66));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));

        RoundedPanel selectedCard = new RoundedPanel(12, new Color(233, 240, 253), new Color(201, 216, 243), 1f);
        selectedCard.setLayout(new MigLayout("wrap 1,insets 10,gap 4", "[grow,fill]", "[]"));
        selectedRoomsLabel.setForeground(new Color(38, 71, 126));
        selectedDateLabel.setForeground(new Color(68, 93, 135));
        selectedGuestLabel.setForeground(new Color(68, 93, 135));
        selectedCard.add(selectedRoomsLabel);
        selectedCard.add(selectedDateLabel);
        selectedCard.add(selectedGuestLabel);

        guestFormsTitleLabel.setForeground(new Color(30, 50, 80));
        guestFormsTitleLabel.setFont(guestFormsTitleLabel.getFont().deriveFont(Font.BOLD, 14f));
        guestFormsPanel.setOpaque(false);
        syncGuestForms();

        PrimaryButton backButton = new PrimaryButton("Quay lại chọn phòng");
        backButton.setBackground(new Color(226, 235, 250));
        backButton.setForeground(new Color(44, 71, 117));
        backButton.addActionListener(e -> {
            bookingCards.show(bookingContent, "select-room");
            setStep(1);
        });

        // Deposit = secondary CTA -> violet accent. Full payment = primary CTA -> navy.
        PrimaryButton depositButton = new PrimaryButton("ĐẶT CỌC 30%");
        depositButton.setBackground(ThemeColors.PREMIUM_ACCENT);
        depositButton.setForeground(Color.WHITE);
        depositButton.addActionListener(e -> submitBookingWithPayment("ĐẶT CỌC 30%", CreateBookingCommand.DEPOSIT_RATIO));

        PrimaryButton fullPaymentButton = new PrimaryButton("THANH TOÁN 100%");
        fullPaymentButton.setBackground(ThemeColors.PREMIUM_PRIMARY);
        fullPaymentButton.setForeground(Color.WHITE);
        fullPaymentButton.addActionListener(e -> submitBookingWithPayment("THANH TOÁN 100%", CreateBookingCommand.FULL_RATIO));

        JPanel actions = new JPanel(new MigLayout("insets 0,gap 10", "[grow,fill][grow,fill][grow,fill]", "[]"));
        actions.setOpaque(false);
        actions.add(backButton, "h 44");
        actions.add(depositButton, "h 44");
        actions.add(fullPaymentButton, "h 44");

        panel.add(title);
        panel.add(selectedCard);
        panel.add(guestFormsTitleLabel);
        panel.add(guestFormsPanel, "growx");
        panel.add(actions, "gapy 8 0");

        JPanel wrapper = new JPanel(new MigLayout("insets 0", "[grow,fill]", "[grow]"));
        wrapper.setOpaque(false);
        wrapper.add(panel, "grow");
        return wrapper;
    }

    private JPanel roomCard(RoomCardData data) {
        int selectedCount = countSelectedRooms(data);
        return new RoomCard(data, selectedCount > 0, selectedCount,
            this::addRoomSelection,
            this::removeRoomSelection);
    }

    

    // private JPanel makeRoomTypeIcon(Color color, String letter) {
    //     JPanel circle = new JPanel(new BorderLayout());
    //     circle.setOpaque(false);
        
    //     ImageIcon icon = IconLoader.loadIcon(IconLoader.getIconFile(letter), 36, 36);
    //     if (icon != null) {
    //         JLabel iconLabel = new JLabel(icon);
    //         circle.add(iconLabel, BorderLayout.CENTER);
    //     }  
    //     return circle;
    // }

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
        badge.setBorder(BorderFactory.createEmptyBorder(3, 9, 3, 9));

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

    private void runSearch() {
        if (filterLocked) {
            return;
        }

        String selectedType = (String) roomTypeCombo.getSelectedItem();
        LocalDate checkInDate = selectedCheckInDate;
        LocalDate checkOutDate = selectedCheckOutDate;

        if (checkInDate == null || checkOutDate == null) {
            JOptionPane.showMessageDialog(
                this,
                "Vui lòng chọn ngày nhận và trả phòng từ lịch.",
                "Thiếu thông tin",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (!checkOutDate.isAfter(checkInDate)) {
            JOptionPane.showMessageDialog(
                this,
                "Ngày trả phòng phải sau ngày nhận phòng.",
                "Dữ liệu không hợp lệ",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (adultsCount < 1 || adultsCount > MAX_ADULTS) {
            JOptionPane.showMessageDialog(
                this,
                "Số người lớn không hợp lệ.",
                "Dữ liệu không hợp lệ",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        BookingSearchRequest request = new BookingSearchRequest(selectedType, checkInDate, checkOutDate, adultsCount, childrenCount);
        lastSearchRequest = request;
        capacityHintsVisible = true;
        currentSlideIndex = 0;

        selectedRooms.clear();
        List<RoomOptionDto> filtered = bookingService.searchAvailableRooms(request);
        renderRooms(mapToCardData(filtered));
        bookingCards.show(bookingContent, "select-room");
        setStep(1);
    }



    private void submitBooking() {
        if (selectedRooms.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui l\u00f2ng ch\u1ecdn ít nh\u1ea5t 1 ph\u00f2ng tr\u01b0\u1edbc.", "Thi\u1ebfu th\u00f4ng tin", JOptionPane.WARNING_MESSAGE);
            setStep(1);
            bookingCards.show(bookingContent, "select-room");
            return;
        }

        if (!validateSelectedRoomCapacity()) {
            setStep(1);
            bookingCards.show(bookingContent, "select-room");
            return;
        }

        List<GuestInfoDto> guestInfos = collectGuestInfos();
        if (guestInfos == null) {
            return;
        }
        if (!validateGuestData(guestInfos)) {
            return;
        }

        LocalDate checkInDate = selectedCheckInDate;
        LocalDate checkOutDate = selectedCheckOutDate;
        if (checkInDate == null || checkOutDate == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn lại ngày nhận/trả phòng hợp lệ.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CreateBookingCommand command = new CreateBookingCommand(
            checkInDate,
            checkOutDate,
            adultsCount,
            childrenCount,
            guestInfos,
            toSelectedRoomOptions(),
            0L,
            CreateBookingCommand.FULL_RATIO,
            "TienMat",
            "",
            currentStaffId()
        );

        BookingConfirmationResult result = bookingService.createBooking(command);
        if (!result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Không thể đặt phòng", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String leadGuestName = guestInfos.get(0).getHoTenNV();
        String successMessage = "Đặt phòng thành công cho khách " + leadGuestName + " (" + selectedRooms.size() + " phòng)";
        if (result.getBookingCode() != null && !result.getBookingCode().trim().isEmpty()) {
            successMessage = successMessage + "\nMã đặt phòng: " + result.getBookingCode();
        }
        JOptionPane.showMessageDialog(this, successMessage, "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
        
        // Refresh RoomManagementPanel data
        java.awt.Window win = javax.swing.SwingUtilities.getWindowAncestor(this);
        if (win instanceof kqlhotel.gui.AppFrame) {
            ((kqlhotel.gui.AppFrame) win).refreshRoomManagementData();
        }
    }

    private String currentStaffId() {
        return Session.currentStaff == null ? null : Session.currentStaff.getMaNV();
    }

    private void submitBookingWithPayment(String paymentPlanLabel, double paymentRatio) {
        if (selectedRooms.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất 1 phòng trước.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            setStep(1);
            bookingCards.show(bookingContent, "select-room");
            return;
        }

        if (!validateSelectedRoomCapacity()) {
            setStep(1);
            bookingCards.show(bookingContent, "select-room");
            return;
        }

        List<GuestInfoDto> guestInfos = collectGuestInfos();
        if (guestInfos == null) {
            return;
        }
        if (!validateGuestData(guestInfos)) {
            return;
        }

        LocalDate checkInDate = selectedCheckInDate;
        LocalDate checkOutDate = selectedCheckOutDate;
        if (checkInDate == null || checkOutDate == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn lại ngày nhận/trả phòng hợp lệ.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Compute real total from selected rooms × nights
        BookingSelectionSummary summary = bookingService.summarizeSelection(toSelectedRoomOptions(), getSummaryRequest());
        long totalAmount = summary.getTotalAmount();
        long paymentAmount = Math.round(totalAmount * paymentRatio);

        // Choose payment method
        String[] paymentLabels = {"Tiền mặt", "Chuyển khoản"};
        String[] paymentCodes = {"TienMat", "ChuyenKhoan"};
        int choice = JOptionPane.showOptionDialog(
            this,
            "<html>Phương án: <b>" + paymentPlanLabel + "</b><br>" +
                "Tổng hóa đơn tạm tính: <b>" + formatMoney(totalAmount) + "</b><br>" +
                "<span style='color:#64748b'>(Đã bao gồm VAT 10%)</span><br>" +
                "Cần thu: <b>" + formatMoney(paymentAmount) + "</b><br><br>" +
                "Chọn phương thức thanh toán:</html>",
            "Phương thức thanh toán",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            paymentLabels,
            paymentLabels[0]
        );

        if (choice == JOptionPane.CLOSED_OPTION) {
            return;
        }

        String referenceNumber = "";
        if (choice == 1) {
            // Show VietQR dialog for bank transfer
            String bookingRef = "DP-" + System.currentTimeMillis() % 100000;
            referenceNumber = showQrPaymentDialog(paymentAmount, bookingRef);
            if (referenceNumber == null) {
                return; // User cancelled
            }
        }

        // Final confirmation
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "<html>Xác nhận đặt phòng?<br>" +
                "Khách: <b>" + guestInfos.get(0).getFullName() + "</b><br>" +
                "Số phòng: <b>" + selectedRooms.size() + "</b> · " + summary.getNights() + " đêm<br>" +
                "Phương thức: <b>" + paymentLabels[choice] + "</b><br>" +
                "Số tiền thu: <b>" + formatMoney(paymentAmount) + "</b> / Tổng " + formatMoney(totalAmount) + "</html>",
            "Xác nhận thanh toán",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        CreateBookingCommand command = new CreateBookingCommand(
            checkInDate,
            checkOutDate,
            adultsCount,
            childrenCount,
            guestInfos,
            toSelectedRoomOptions(),
            totalAmount,
            paymentRatio,
            paymentCodes[choice],
            referenceNumber,
            currentStaffId()
        );

        BookingConfirmationResult result = bookingService.createBooking(command);
        if (!result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(), "Không thể đặt phòng", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String leadGuestName = guestInfos.get(0).getFullName();
        StringBuilder successMessage = new StringBuilder();
        successMessage.append("Đặt phòng thành công!\n\n");
        successMessage.append("Khách: ").append(leadGuestName).append("\n");
        successMessage.append("Số phòng: ").append(selectedRooms.size()).append(" · ").append(summary.getNights()).append(" đêm\n");
        successMessage.append("Phương thức: ").append(paymentLabels[choice]).append("\n");
        successMessage.append("Đã thu (").append(paymentPlanLabel).append("): ").append(formatMoney(paymentAmount)).append("\n");
        successMessage.append("Tổng hóa đơn: ").append(formatMoney(totalAmount)).append("\n");
        if (!referenceNumber.isEmpty()) {
            successMessage.append("Số tham chiếu: ").append(referenceNumber).append("\n");
        }
        if (result.getBookingCode() != null && !result.getBookingCode().trim().isEmpty()) {
            successMessage.append("Mã đặt phòng: ").append(result.getBookingCode()).append("\n");
        }
        if (result.getMessage() != null) {
            successMessage.append("\n").append(result.getMessage());
        }

        JOptionPane.showMessageDialog(this, successMessage.toString(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
        
        // Refresh RoomManagementPanel data
        java.awt.Window win = javax.swing.SwingUtilities.getWindowAncestor(this);
        if (win instanceof kqlhotel.gui.AppFrame) {
            ((kqlhotel.gui.AppFrame) win).refreshRoomManagementData();
        }

        // Reset form and refresh room list
        selectedRooms.clear();
        guestFormRows.clear();
        guestFormsPanel.removeAll();
        guestFormsPanel.revalidate();
        guestFormsPanel.repaint();
        this.preFilledCustomer = null;
        adultsCount = 2;
        childrenCount = 0;
        updateGuestStepperState();
        setStep(1);
        bookingCards.show(bookingContent, "select-room");
        runSearch();
    }

    private boolean validateGuestData(List<GuestInfoDto> guestInfos) {
        for (int i = 0; i < guestInfos.size(); i++) {
            GuestInfoDto guest = guestInfos.get(i);
            String idNo = guest.getIdNo();
            String phone = guest.getPhone();
            String name = guest.getFullName();
            String email = guest.getEmail();
            boolean isChild = i >= adultsCount;

            if (name == null || name.trim().length() < 2) {
                JOptionPane.showMessageDialog(this,
                    "Họ tên Khách " + (i + 1) + " không hợp lệ.",
                    "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            if (!isChild && (idNo == null || !idNo.matches("\\d{9,12}"))) {
                JOptionPane.showMessageDialog(this,
                    "CCCD/Hộ chiếu của Khách " + (i + 1) + " (Người lớn) phải gồm 9-12 chữ số.",
                    "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            if (isChild && idNo != null && !idNo.isEmpty() && !idNo.matches("\\d{9,12}")) {
                JOptionPane.showMessageDialog(this,
                    "CCCD/Hộ chiếu của Khách " + (i + 1) + " phải gồm 9-12 chữ số.",
                    "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            if (phone != null && !phone.isEmpty() && !phone.matches("0\\d{9,10}")) {
                JOptionPane.showMessageDialog(this,
                    "Số điện thoại của Khách " + (i + 1) + " phải bắt đầu bằng 0 và có 10-11 chữ số.",
                    "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            if (i == 0 && (email == null || email.trim().isEmpty())) {
                JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập email của khách chính để gửi xác nhận đặt phòng.",
                    "Thiếu email", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            if (email != null && !email.trim().isEmpty()
                    && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                JOptionPane.showMessageDialog(this,
                    "Email của Khách " + (i + 1) + " không hợp lệ.",
                    "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }
        return true;
    }

    private List<GuestInfoDto> collectGuestInfos() {
        syncGuestForms();
        List<GuestInfoDto> guestInfos = new ArrayList<>();
        for (int i = 0; i < guestFormRows.size(); i++) {
            GuestFormRow row = guestFormRows.get(i);
            String fullName = row.nameField.getText().trim();
            String phone = row.phoneField.getText().trim();
            String idNo = row.idField.getText().trim();
            String email = row.emailField.getText().trim();

            boolean isChild = i >= adultsCount;

            if (fullName.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập họ tên cho Khách " + (i + 1) + ".",
                    "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE
                );
                return null;
            }
            
            if (!isChild && idNo.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng nhập CCCD/Hộ chiếu cho Khách " + (i + 1) + " (Người lớn).",
                    "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE
                );
                return null;
            }

            guestInfos.add(new GuestInfoDto(fullName, phone, idNo, email));
        }
        return guestInfos;
    }

    private void syncGuestForms() {
        int totalPeople = adultsCount + childrenCount;
        guestFormsTitleLabel.setText("Danh sách khách (" + totalPeople + " người) - Ưu tiên CCCD để định danh và auto-fill");

        while (guestFormRows.size() < totalPeople) {
            guestFormRows.add(createGuestFormRow(guestFormRows.size() + 1));
        }
        while (guestFormRows.size() > totalPeople) {
            guestFormRows.remove(guestFormRows.size() - 1);
        }

        // Auto-fill first guest if there's a pre-filled customer
        if (preFilledCustomer != null && !guestFormRows.isEmpty()) {
            GuestFormRow firstRow = guestFormRows.get(0);
            if (firstRow.idField.getText().trim().isEmpty()) {
                firstRow.idField.setText(preFilledCustomer.getCCCD());
                firstRow.nameField.setText(preFilledCustomer.getHoTenKH());
                firstRow.phoneField.setText(preFilledCustomer.getSdt());
                firstRow.emailField.setText(preFilledCustomer.getEmail());
            }
        }

        guestFormsPanel.removeAll();
        for (GuestFormRow row : guestFormRows) {
            guestFormsPanel.add(row.panel, "growx");
        }
        guestFormsPanel.revalidate();
        guestFormsPanel.repaint();
    }

    private GuestFormRow createGuestFormRow(int index) {
        JPanel rowPanel = new RoundedPanel(10, Color.WHITE, new Color(225, 231, 245), 1f);
        rowPanel.setLayout(new MigLayout("insets 8,gap 8", "[grow,fill][grow,fill][grow,fill][grow,fill]", "[][]"));

        JLabel label = new JLabel("Khách " + index);
        label.setForeground(new Color(44, 71, 117));
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));

        JTextField idField = new JTextField();
        idField.putClientProperty("JTextField.placeholderText", "CCCD/Hộ chiếu (ưu tiên)");

        JTextField nameField = new JTextField();
        nameField.putClientProperty("JTextField.placeholderText", "Họ tên");

        JTextField phoneField = new JTextField();
        phoneField.putClientProperty("JTextField.placeholderText", "Số điện thoại");

        JTextField emailField = new JTextField();
        emailField.putClientProperty("JTextField.placeholderText", index == 1 ? "Email nhận xác nhận" : "Email (nếu có)");

        attachAutoFillById(idField, nameField, phoneField, emailField);

        rowPanel.add(label, "span 4, wrap");
        rowPanel.add(idField, "grow,h 34");
        rowPanel.add(nameField, "grow,h 34");
        rowPanel.add(phoneField, "grow,h 34");
        rowPanel.add(emailField, "grow,h 34");

        return new GuestFormRow(rowPanel, nameField, phoneField, idField, emailField);
    }

    private void attachAutoFillById(JTextField idField, JTextField nameField, JTextField phoneField, JTextField emailField) {
        idField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                autoFillIfMatched();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                autoFillIfMatched();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                autoFillIfMatched();
            }

            private void autoFillIfMatched() {
                String idNo = idField.getText().trim();
                if (idNo.length() < 9) {
                    nameField.setText("");
                    phoneField.setText("");
                    emailField.setText("");
                    return;
                }

                Optional<GuestInfoDto> found = CustomerDirectoryServiceProvider.get().findByIdNo(idNo);
                if (found.isPresent()) {
                    GuestInfoDto customer = found.get();
                    nameField.setText(customer.getHoTenNV());
                    phoneField.setText(customer.getSdt());
                    emailField.setText(customer.getEmail());
                } else {
                    nameField.setText("");
                    phoneField.setText("");
                    emailField.setText("");
                }
            }
        });
    }

    private void renderRooms(List<RoomCardData> data) {
        displayedRooms.clear();
        displayedRooms.addAll(data);
        currentSlideIndex = Math.min(currentSlideIndex, getMaxSlideIndex());
        renderCurrentSlide();
        updateSelectionSummary();
    }

    private void renderCurrentSlide() {
        roomList.removeAll();

        if (displayedRooms.isEmpty()) {
            RoundedPanel empty = new RoundedPanel(16, new Color(29, 46, 78), new Color(255, 255, 255, 20), 1f);
            empty.setLayout(new MigLayout("insets 20", "[grow,fill]", "[]"));
            JLabel msg = new JLabel("Kh\u00f4ng t\u00ecm th\u1ea5y lo\u1ea1i ph\u00f2ng ph\u00f9 h\u1ee3p.", SwingConstants.CENTER);
            msg.setForeground(new Color(239, 244, 255));
            empty.add(msg);
            roomList.add(empty, "span 2,growx");
        } else {
            int start = currentSlideIndex * ROOMS_PER_SLIDE;
            int end = Math.min(start + ROOMS_PER_SLIDE, displayedRooms.size());
            List<RoomCardData> pageItems = displayedRooms.subList(start, end);
            for (RoomCardData room : pageItems) {
                roomList.add(roomCard(room));
            }
            // Pad remaining cells with invisible placeholders so the 2x2 grid
            // shape is preserved (row height is enforced by the layout itself).
            int filler = ROOMS_PER_SLIDE - pageItems.size();
            for (int i = 0; i < filler; i++) {
                JPanel placeholder = new JPanel();
                placeholder.setOpaque(false);
                roomList.add(placeholder);
            }
        }

        roomList.revalidate();
        roomList.repaint();
        updateSlideControls();
    }

    private void addRoomSelection(RoomCardData data) {
        int selectedCount = countSelectedRooms(data);
        int availableRooms = Math.max(0, data.optionDto.getAvailableRooms());
        if (selectedCount >= availableRooms) {
            JOptionPane.showMessageDialog(this,
                "Loại phòng này chỉ còn " + availableRooms + " phòng trống trong khoảng ngày đã chọn.",
                "Không thể chọn thêm", JOptionPane.WARNING_MESSAGE);
            return;
        }
        selectedRooms.add(data);
        renderCurrentSlide();
        updateSelectionSummary();
    }

    private void removeRoomSelection(RoomCardData data) {
        selectedRooms.remove(data);
        renderCurrentSlide();
        updateSelectionSummary();
    }

    private int countSelectedRooms(RoomCardData data) {
        int count = 0;
        for (RoomCardData selectedRoom : selectedRooms) {
            if (selectedRoom == data) {
                count++;
            }
        }
        return count;
    }

    private void updateSlideControls() {
        int totalSlides = getTotalSlides();
        int displaySlide = totalSlides == 0 ? 0 : currentSlideIndex + 1;
        slideInfoLabel.setText("Slide " + displaySlide + "/" + totalSlides);

        boolean hasMultipleSlides = totalSlides > 1;
        if (prevSlideButton != null) {
            prevSlideButton.setEnabled(hasMultipleSlides && currentSlideIndex > 0);
        }
        if (nextSlideButton != null) {
            nextSlideButton.setEnabled(hasMultipleSlides && currentSlideIndex < getMaxSlideIndex());
        }
    }

    private int getTotalSlides() {
        if (displayedRooms.isEmpty()) {
            return 1;
        }
        return (int) Math.ceil(displayedRooms.size() / (double) ROOMS_PER_SLIDE);
    }

    private int getMaxSlideIndex() {
        return Math.max(0, getTotalSlides() - 1);
    }

    private void updateSelectionSummary() {
        int selectedCount = selectedRooms.size();
        BookingSelectionSummary summary = bookingService.summarizeSelection(toSelectedRoomOptions(), getSummaryRequest());

        selectionCountLabel.setText(selectedCount == 0 ? "Chưa chọn phòng" : selectedCount + " phòng đã chọn");
        selectionDetailLabel.setText(
            selectedCount == 0
                ? "Chọn phòng ở bên phải để tiếp tục"
                : "Tổng: " + formatMoney(summary.getTotalAmount()) + " · " + summary.getNights() + " đêm"
        );

        if (continueToGuestButton != null) {
            continueToGuestButton.setEnabled(true);
            continueToGuestButton.setBackground(ThemeColors.PREMIUM_PRIMARY);
        }

        selectedRoomsLabel.setText(buildSelectedRoomsSummary());
        String dateText = "--";
        if (selectedCheckInDate != null && selectedCheckOutDate != null) {
            dateText = selectedCheckInDate.format(DATE_FORMAT) + " -> " + selectedCheckOutDate.format(DATE_FORMAT);
        }
        selectedDateLabel.setText("Ngày nhận/trả: " + dateText);
        selectedGuestLabel.setText("Số khách: " + (adultsCount + childrenCount));
    }

    private String buildSelectedRoomsSummary() {
        if (selectedRooms.isEmpty()) {
            return "Chưa chọn phòng";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("<html><b>Đã chọn: ").append(selectedRooms.size()).append(" phòng</b><br>");
        List<RoomCardData> uniqueRooms = new ArrayList<>();
        for (RoomCardData selectedRoom : selectedRooms) {
            if (!uniqueRooms.contains(selectedRoom)) {
                uniqueRooms.add(selectedRoom);
            }
        }
        for (int i = 0; i < uniqueRooms.size(); i++) {
            RoomCardData room = uniqueRooms.get(i);
            int count = countSelectedRooms(room);
            builder.append(room.roomType);
            if (count > 1) {
                builder.append(" x").append(count);
            }
            if (i < uniqueRooms.size() - 1) {
                builder.append(", ");
            }
        }
        builder.append("</html>");
        return builder.toString();
    }

    private BookingSearchRequest getSummaryRequest() {
        if (lastSearchRequest != null) {
            return lastSearchRequest;
        }

        LocalDate checkInDate = selectedCheckInDate;
        LocalDate checkOutDate = selectedCheckOutDate;
        if (checkInDate == null) {
            checkInDate = LocalDate.now();
        }
        if (checkOutDate == null || !checkOutDate.isAfter(checkInDate)) {
            checkOutDate = checkInDate.plusDays(1);
        }

        return new BookingSearchRequest((String) roomTypeCombo.getSelectedItem(), checkInDate, checkOutDate, adultsCount, childrenCount);
    }

    private List<RoomOptionDto> toSelectedRoomOptions() {
        List<RoomOptionDto> selected = new ArrayList<>();
        for (RoomCardData room : selectedRooms) {
            selected.add(room.optionDto);
        }
        return selected;
    }

    private List<RoomCardData> mapToCardData(List<RoomOptionDto> rooms) {
        List<RoomCardData> cards = new ArrayList<>();
        for (RoomOptionDto room : rooms) {
            cards.add(toCardData(room));
        }
        return cards;
    }

    private RoomCardData toCardData(RoomOptionDto room) {
        Color tone = resolveRoomTone(room.getRoomType());
        Color bg = resolveRoomBackground(room.getRoomType());
        return new RoomCardData(
            room,
            room.getRoomType(),
            formatMoney(room.getNightlyPrice()),
            room.getStatus(),
            calculateFreeRate(room.getStatus()),
            room.getMaxGuests(),
            room.getMaxChildren(),
            capacityHintsVisible ? buildCapacityHint(room) : "",
            hasEnoughRoomsForSearch(room),
            room.getAmenities(),
            bg,
            tone
        );
    }

    private String buildCapacityHint(RoomOptionDto room) {
        int requiredRooms = calculateRequiredRoomsForSearch(room);
        if (requiredRooms == Integer.MAX_VALUE) {
            return "Loại phòng này không phù hợp với số trẻ em đã nhập";
        }
        if (requiredRooms <= 1) {
            return "1 phòng đủ cho nhóm khách này";
        }
        if (room.getAvailableRooms() < requiredRooms) {
            return "Chỉ còn " + room.getAvailableRooms() + " phòng, chưa đủ cho nhóm khách này";
        }
        return "Cần chọn ít nhất " + requiredRooms + " phòng loại này";
    }

    private boolean hasEnoughRoomsForSearch(RoomOptionDto room) {
        return room.getAvailableRooms() >= calculateRequiredRoomsForSearch(room);
    }

    private int calculateRequiredRoomsForSearch(RoomOptionDto room) {
        int adultRooms = ceilDiv(Math.max(1, adultsCount), Math.max(1, room.getMaxGuests()));
        if (childrenCount > 0 && room.getMaxChildren() <= 0) {
            return Integer.MAX_VALUE;
        }
        int childRooms = childrenCount <= 0
            ? 0
            : ceilDiv(childrenCount, room.getMaxChildren());
        return Math.max(1, Math.max(adultRooms, childRooms));
    }

    private int ceilDiv(int value, int divisor) {
        return (value + divisor - 1) / divisor;
    }

    private Color resolveRoomTone(String roomType) {
        if (roomType.startsWith("Grand Premium 1")) {
            return new Color(30, 180, 120);
        }
        if (roomType.startsWith("Grand Premium 2")) {
            return new Color(143, 97, 255);
        }
        if (roomType.startsWith("Suite")) {
            return new Color(230, 154, 30);
        }
        return new Color(49, 130, 206);
    }

    private Color resolveRoomBackground(String roomType) {
        if (roomType.startsWith("Grand Premium 1")) {
            return new Color(223, 248, 239);
        }
        if (roomType.startsWith("Grand Premium 2")) {
            return new Color(238, 232, 255);
        }
        if (roomType.startsWith("Suite")) {
            return new Color(255, 246, 220);
        }
        return new Color(235, 248, 255);
    }

    private String calculateFreeRate(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "0%";
        }

        String[] splitBySlash = status.split("/");
        if (splitBySlash.length < 2) {
            return "0%";
        }

        try {
            int free = Integer.parseInt(splitBySlash[0].replaceAll("[^0-9]", ""));
            int total = Integer.parseInt(splitBySlash[1].replaceAll("[^0-9]", ""));
            if (total <= 0) {
                return "0%";
            }
            int percent = (int) Math.round((free * 100.0) / total);
            return percent + "%";
        } catch (NumberFormatException ex) {
            return "0%";
        }
    }

    private String formatMoney(long value) {
        return String.format("%,d", value).replace(',', '.') + "đ";
    }

    private void openCustomerInfo() {
        if (selectedRooms.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất 1 phòng trước.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validateSelectedRoomCapacity()) {
            return;
        }
        bookingCards.show(bookingContent, "customer-info");
        setStep(2);
    }

    private boolean validateSelectedRoomCapacity() {
        int totalAdultCapacity = selectedRooms.stream().mapToInt(r -> r.capacity).sum();
        if (adultsCount > totalAdultCapacity) {
            JOptionPane.showMessageDialog(this,
                "Tổng sức chứa người lớn của các phòng đã chọn (" + totalAdultCapacity + ") không đủ cho " + adultsCount + " người lớn.\n"
                + "Vui lòng chọn thêm phòng hoặc chọn loại phòng lớn hơn.",
                "Sức chứa không đủ", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        int totalChildCapacity = selectedRooms.stream().mapToInt(r -> r.childCapacity).sum();
        if (childrenCount > totalChildCapacity) {
            JOptionPane.showMessageDialog(this,
                "Tổng sức chứa trẻ em của các phòng đã chọn (" + totalChildCapacity + ") không đủ cho " + childrenCount + " trẻ em.\n"
                + "Vui lòng chọn thêm phòng hoặc chọn loại phòng lớn hơn.",
                "Sức chứa không đủ", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    private void setStep(int step) {
        boolean atStep1 = step == 1;
        setFilterLocked(!atStep1);
        if (step1Pill != null) step1Pill.setActive(atStep1);
        if (step2Pill != null) step2Pill.setActive(!atStep1);
    }

    private void setFilterLocked(boolean locked) {
        filterLocked = locked;
        roomTypeCombo.setEnabled(!locked);
        if (searchButton != null) {
            searchButton.setEnabled(!locked);
        }

        updateGuestStepperState();

        Color fieldColor = locked ? new Color(140, 150, 170) : new Color(60, 80, 110);
        checkInField.setForeground(fieldColor);
        checkOutField.setForeground(fieldColor);
    }

    public void preFillCustomer(kqlhotel.entity.Customer customer) {
        this.selectedRooms.clear();
        this.preFilledCustomer = customer;

        // Clear existing guest forms to ensure new data is synced correctly
        for (GuestFormRow row : guestFormRows) {
            row.idField.setText("");
            row.nameField.setText("");
            row.phoneField.setText("");
        }

        // Return to Step 1 (Select Room)
        setStep(1);
        bookingCards.show(bookingContent, "select-room");

        // Refresh UI state
        updateSelectionSummary();
        syncGuestForms();
    }

    

    /**
     * Shows a VietQR payment dialog. Returns the booking reference if user confirms,
     * or null if user cancels.
     */
    private String showQrPaymentDialog(long amount, String bookingRef) {
        String bankId   = "970405"; // Agribank BIN
        String acctNo   = "4306205595150";
        String acctName = "NGUYEN NGOC QUANG";
        String addInfoEncoded  = java.net.URLEncoder.encode("Dat phong " + bookingRef, java.nio.charset.StandardCharsets.UTF_8);
        String acctNameEncoded = java.net.URLEncoder.encode(acctName, java.nio.charset.StandardCharsets.UTF_8);
        String qrUrl = "https://img.vietqr.io/image/" + bankId + "-" + acctNo + "-compact2.png"
                     + "?amount=" + amount
                     + "&addInfo=" + addInfoEncoded
                     + "&accountName=" + acctNameEncoded;

        java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog((java.awt.Frame)(owner instanceof java.awt.Frame ? owner : null),
                "Chuy\u1ec3n kho\u1ea3n ng\u00e2n h\u00e0ng", true);
        dialog.setLayout(new MigLayout("insets 20,gap 12", "[center,grow]", "[][][][]"));
        dialog.setResizable(false);

        JLabel titleLbl = new JLabel("Qu\u00e9t m\u00e3 \u0111\u1ec3 thanh to\u00e1n");
        titleLbl.setFont(titleLbl.getFont().deriveFont(Font.BOLD, 16f));
        titleLbl.setForeground(new Color(30, 58, 102));
        titleLbl.setHorizontalAlignment(SwingConstants.CENTER);
        dialog.add(titleLbl, "wrap,growx");

        JLabel bankInfo = new JLabel("<html><center>"
                + "<b>Agribank</b> \u2014 STK: <b>" + acctNo + "</b><br>"
                + "Ch\u1ee7 TK: <b>" + acctName + "</b><br>"
                + "S\u1ed1 ti\u1ec1n: <b style='color:#1a5fb4'>" + formatMoney(amount) + "</b><br>"
                + "N\u1ed9i dung: <b>Dat phong " + bookingRef + "</b>"
                + "</center></html>");
        bankInfo.setHorizontalAlignment(SwingConstants.CENTER);
        dialog.add(bankInfo, "wrap,growx");

        JLabel qrLabel = new JLabel("\u0110ang t\u1ea3i m\u00e3 QR...", SwingConstants.CENTER);
        qrLabel.setPreferredSize(new java.awt.Dimension(260, 260));
        qrLabel.setBorder(BorderFactory.createLineBorder(new Color(210, 220, 240), 1));
        dialog.add(qrLabel, "wrap,align center");

        JLabel noteLbl = new JLabel("<html><center><i>Sau khi kh\u00e1ch chuy\u1ec3n ti\u1ec1n th\u00e0nh c\u00f4ng, nh\u1ea5n <b>\u0110\u00e3 nh\u1eadn ti\u1ec1n</b>.</i></center></html>");
        noteLbl.setForeground(new Color(100, 110, 130));
        noteLbl.setHorizontalAlignment(SwingConstants.CENTER);
        dialog.add(noteLbl, "wrap,growx");

        JPanel btnRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 16, 0));
        JButton cancelBtn  = new JButton("Hu\u1ef7");
        JButton confirmBtn = new JButton("\u0110\u00e3 nh\u1eadn ti\u1ec1n \u2713");
        confirmBtn.setBackground(new Color(30, 90, 200));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setOpaque(true);
        confirmBtn.setFocusPainted(false);
        btnRow.add(cancelBtn);
        btnRow.add(confirmBtn);
        dialog.add(btnRow, "growx");

        final String[] resultRef = {null};
        cancelBtn.addActionListener(e -> dialog.dispose());
        confirmBtn.addActionListener(e -> { resultRef[0] = bookingRef; dialog.dispose(); });

        // Fetch QR image on a background thread
        new Thread(() -> {
            try {
                java.net.URL url = java.net.URI.create(qrUrl).toURL();
                java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(url);
                if (img != null) {
                    java.awt.Image scaled = img.getScaledInstance(250, 250, java.awt.Image.SCALE_SMOOTH);
                    javax.swing.SwingUtilities.invokeLater(() -> {
                        qrLabel.setIcon(new ImageIcon(scaled));
                        qrLabel.setText("");
                        dialog.pack();
                    });
                } else {
                    javax.swing.SwingUtilities.invokeLater(() ->
                        qrLabel.setText("<html><center>Kh\u00f4ng t\u1ea3i \u0111\u01b0\u1ee3c QR.<br>Ki\u1ec3m tra k\u1ebft n\u1ed1i m\u1ea1ng.</center></html>"));
                }
            } catch (Exception ex) {
                javax.swing.SwingUtilities.invokeLater(() ->
                    qrLabel.setText("<html><center>L\u1ed7i k\u1ebft n\u1ed1i.<br>Ki\u1ec3m tra k\u1ebft n\u1ed1i m\u1ea1ng.</center></html>"));
            }
        }, "qr-loader").start();

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true); // blocks until disposed
        return resultRef[0];
    }

    private static final class GuestFormRow {
        private final JPanel panel;
        private final JTextField nameField;
        private final JTextField phoneField;
        private final JTextField idField;
        private final JTextField emailField;

        private GuestFormRow(JPanel panel, JTextField nameField, JTextField phoneField, JTextField idField, JTextField emailField) {
            this.panel = panel;
            this.nameField = nameField;
            this.phoneField = phoneField;
            this.idField = idField;
            this.emailField = emailField;
        }
    }
}
