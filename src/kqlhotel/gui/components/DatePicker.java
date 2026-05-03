package kqlhotel.gui.components;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.YearMonth;
import javax.swing.*;
import kqlhotel.gui.theme.ThemeColors;

public class DatePicker extends JPanel {
    private final JTextField textDateField;
    private final PrimaryButton btnCalendar;
    private final JPopupMenu popupMenu;
    private LocalDate selectedDate;
    private YearMonth currentMonth;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final java.util.List<Runnable> dateChangeListeners = new java.util.ArrayList<>();
    
    private final JLabel lblMonthYear;
    private final JPanel daysPanel;

    public DatePicker() {
        setLayout(new BorderLayout());
        setOpaque(false);
        
        selectedDate = LocalDate.now();
        currentMonth = YearMonth.from(selectedDate);
        
        textDateField = new JTextField();
        textDateField.setPreferredSize(new Dimension(120, 36));
        textDateField.setFont(textDateField.getFont().deriveFont(14f));
        textDateField.setText(selectedDate.format(dtf));

        ImageIcon icon = kqlhotel.gui.utils.IconLoader.loadIcon("calendar.png", 18, 18);

        btnCalendar = new PrimaryButton("");
        if (icon != null) {
            btnCalendar.setIcon(icon);
        } else {
            btnCalendar.setText("...");
        }
        btnCalendar.setPreferredSize(new Dimension(36, 36));
        btnCalendar.setArc(8);
        btnCalendar.setBackground(Color.WHITE);
        btnCalendar.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 235)));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 235)));

        wrapper.add(textDateField, BorderLayout.CENTER);
        wrapper.add(btnCalendar, BorderLayout.EAST);

        add(wrapper, BorderLayout.CENTER);
        
        popupMenu = new JPopupMenu();
        popupMenu.setLayout(new BorderLayout());
        popupMenu.setBackground(Color.WHITE);
        popupMenu.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 235), 1));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        JButton btnPrev = createNavButton("<");
        JButton btnNext = createNavButton(">");
        
        lblMonthYear = new JLabel("", SwingConstants.CENTER);
        lblMonthYear.setFont(lblMonthYear.getFont().deriveFont(Font.BOLD, 14f));
        
        headerPanel.add(btnPrev, BorderLayout.WEST);
        headerPanel.add(lblMonthYear, BorderLayout.CENTER);
        headerPanel.add(btnNext, BorderLayout.EAST);
        
        btnPrev.addActionListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            updateCalendar();
        });
        
        btnNext.addActionListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            updateCalendar();
        });
        
        JPanel daysHeaderPanel = new JPanel(new GridLayout(1, 7, 2, 2));
        daysHeaderPanel.setBackground(Color.WHITE);
        String[] days = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        for (String day : days) {
            JLabel l = new JLabel(day, SwingConstants.CENTER);
            l.setForeground(new Color(110, 125, 145));
            l.setFont(l.getFont().deriveFont(Font.BOLD, 12f));
            daysHeaderPanel.add(l);
        }
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(daysHeaderPanel, BorderLayout.SOUTH);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        daysPanel = new JPanel(new GridLayout(0, 7, 4, 4));
        daysPanel.setBackground(Color.WHITE);
        daysPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        popupMenu.add(topPanel, BorderLayout.NORTH);
        popupMenu.add(daysPanel, BorderLayout.CENTER);
        
        btnCalendar.addActionListener(e -> {
            currentMonth = YearMonth.from(selectedDate);
            updateCalendar();
            popupMenu.show(btnCalendar, 0, btnCalendar.getHeight());
        });
        
        updateCalendar();
    }
    
    private void updateCalendar() {
        lblMonthYear.setText(currentMonth.getMonthValue() + "/" + currentMonth.getYear());
        daysPanel.removeAll();
        
        LocalDate firstOfMonth = currentMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
        
        int daysInMonth = currentMonth.lengthOfMonth();
        
        // Blank leading days
        for (int i = 1; i < dayOfWeek; i++) {
            daysPanel.add(new JLabel(""));
        }
        
        // Days
        for (int i = 1; i <= daysInMonth; i++) {
            final int day = i;
            LocalDate thisDate = currentMonth.atDay(day);
            JLabel l = new JLabel(String.valueOf(day), SwingConstants.CENTER);
            l.setOpaque(true);
            l.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            if (thisDate.equals(selectedDate)) {
                l.setBackground(ThemeColors.ACCENT);
                l.setForeground(Color.WHITE);
            } else {
                l.setBackground(Color.WHITE);
                l.setForeground(new Color(50, 65, 80));
            }
            
            l.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedDate = thisDate;
                    textDateField.setText(selectedDate.format(dtf));
                    popupMenu.setVisible(false);
                    notifyDateChanged();
                }
                
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!thisDate.equals(selectedDate)) l.setBackground(new Color(240, 245, 250));
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    if (!thisDate.equals(selectedDate)) l.setBackground(Color.WHITE);
                }
            });
            daysPanel.add(l);
        }
        
        daysPanel.revalidate();
        daysPanel.repaint();
        popupMenu.pack();
    }
    
    private JButton createNavButton(String txt) {
        JButton b = new JButton(txt);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 14f));
        b.setForeground(ThemeColors.ACCENT);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
    
    public LocalDate getSelectedDate() {
        try {
            return LocalDate.parse(textDateField.getText(), dtf);
        } catch (Exception e) {
            return selectedDate;
        }
    }
    
    public void setSelectedDate(LocalDate date) {
        if (date != null) {
            this.selectedDate = date;
            textDateField.setText(date.format(dtf));
            currentMonth = YearMonth.from(date);
        }
    }
    
    /**
     * Thêm listener để lắng nghe thay đổi ngày.
     */
    public void addDateChangeListener(Runnable listener) {
        dateChangeListeners.add(listener);
        textDateField.addActionListener(e -> listener.run());
        textDateField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                // Validate và cập nhật selectedDate từ text field
                try {
                    LocalDate parsed = LocalDate.parse(textDateField.getText(), dtf);
                    selectedDate = parsed;
                    listener.run();
                } catch (Exception ex) {
                    // Nếu parse lỗi, giữ nguyên giá trị cũ và reset text
                    textDateField.setText(selectedDate.format(dtf));
                }
            }
        });
    }
    
    private void notifyDateChanged() {
        for (Runnable listener : dateChangeListeners) {
            listener.run();
        }
    }
}
