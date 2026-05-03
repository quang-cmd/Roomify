package kqlhotel.gui.components;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Date picker đơn giản với text field có mask dd/MM/yyyy và nút calendar.
 */
public class SimpleDatePicker extends JPanel {
    private final JFormattedTextField textField;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    
    public SimpleDatePicker() {
        setLayout(new BorderLayout(4, 0));
        setOpaque(false);
        
        // Create masked text field
        MaskFormatter mask = null;
        try {
            mask = new MaskFormatter("##/##/####");
            mask.setPlaceholderCharacter('_');
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        textField = new JFormattedTextField(mask);
        textField.setColumns(10);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        textField.setValue(dateFormat.format(new Date()));
        
        // Calendar button
        JButton calendarBtn = new JButton("📅");
        calendarBtn.setFocusable(false);
        calendarBtn.setPreferredSize(new Dimension(32, 28));
        calendarBtn.addActionListener(this::showCalendarPopup);
        
        add(textField, BorderLayout.CENTER);
        add(calendarBtn, BorderLayout.EAST);
    }
    
    public Date getDate() {
        try {
            String text = textField.getText();
            return dateFormat.parse(text);
        } catch (ParseException e) {
            return new Date();
        }
    }
    
    public void setDate(Date date) {
        textField.setValue(dateFormat.format(date));
    }
    
    public void addDateChangeListener(Runnable listener) {
        textField.addActionListener(e -> listener.run());
        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                listener.run();
            }
        });
    }
    
    private void showCalendarPopup(ActionEvent e) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Chọn ngày", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(280, 280);
        dialog.setLocationRelativeTo(this);
        
        CalendarPanel panel = new CalendarPanel();
        panel.setOnDateSelected(date -> {
            setDate(date);
            dialog.dispose();
        });
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    /**
     * Panel hiển thị calendar đơn giản.
     */
    private static class CalendarPanel extends JPanel {
        private final Calendar calendar = Calendar.getInstance();
        private final JLabel monthLabel;
        private final JPanel daysPanel;
        // private java.util.function.Consumer<Date> onDateSelected;
        
        public CalendarPanel() {
            setLayout(new BorderLayout(8, 8));
            setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            setBackground(Color.WHITE);
            
            // Header với tháng/năm và nút điều hướng
            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            
            monthLabel = new JLabel("", JLabel.CENTER);
            monthLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            
            JButton prevBtn = new JButton("◀");
            prevBtn.setFocusable(false);
            prevBtn.addActionListener(e -> {
                calendar.add(Calendar.MONTH, -1);
                updateDays();
            });
            
            JButton nextBtn = new JButton("▶");
            nextBtn.setFocusable(false);
            nextBtn.addActionListener(e -> {
                calendar.add(Calendar.MONTH, 1);
                updateDays();
            });
            
            header.add(prevBtn, BorderLayout.WEST);
            header.add(monthLabel, BorderLayout.CENTER);
            header.add(nextBtn, BorderLayout.EAST);
            
            add(header, BorderLayout.NORTH);
            
            // Days grid
            daysPanel = new JPanel(new GridLayout(7, 7, 2, 2));
            daysPanel.setOpaque(false);
            updateDays();
            
            add(daysPanel, BorderLayout.CENTER);
        }
        
        private java.util.function.Consumer<Date> onDateSelected;
        
        public void setOnDateSelected(java.util.function.Consumer<Date> callback) {
            this.onDateSelected = callback;
        }
        
        private void updateDays() {
            daysPanel.removeAll();
            
            // Weekday headers
            String[] weekdays = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
            for (String day : weekdays) {
                JLabel label = new JLabel(day, JLabel.CENTER);
                label.setFont(new Font("Segoe UI", Font.BOLD, 11));
                label.setForeground(new Color(100, 116, 139));
                daysPanel.add(label);
            }
            
            // Calendar days
            Calendar cal = (Calendar) calendar.clone();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // 0 = Sunday
            int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            
            // Empty cells before first day
            for (int i = 0; i < firstDayOfWeek; i++) {
                daysPanel.add(new JLabel(""));
            }
            
            // Day buttons
            int today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            int thisMonth = Calendar.getInstance().get(Calendar.MONTH);
            int thisYear = Calendar.getInstance().get(Calendar.YEAR);
            
            for (int day = 1; day <= daysInMonth; day++) {
                JButton dayBtn = new JButton(String.valueOf(day));
                dayBtn.setFocusable(false);
                dayBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                
                boolean isToday = (day == today && 
                    calendar.get(Calendar.MONTH) == thisMonth &&
                    calendar.get(Calendar.YEAR) == thisYear);
                
                if (isToday) {
                    dayBtn.setBackground(new Color(59, 130, 246));
                    dayBtn.setForeground(Color.WHITE);
                    dayBtn.setOpaque(true);
                } else {
                    dayBtn.setBackground(Color.WHITE);
                    dayBtn.setOpaque(true);
                }
                
                final int selectedDay = day;
                dayBtn.addActionListener(e -> {
                    calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                    if (onDateSelected != null) {
                        onDateSelected.accept(calendar.getTime());
                    }
                });
                
                daysPanel.add(dayBtn);
            }
            
            // Update month label
            String[] months = {"Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};
            monthLabel.setText(months[calendar.get(Calendar.MONTH)] + " " + calendar.get(Calendar.YEAR));
            
            daysPanel.revalidate();
            daysPanel.repaint();
        }
    }
}
