package kqlhotel.gui.components;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import kqlhotel.bus.customer.CustomerBUS;
import kqlhotel.entity.Customer;
import kqlhotel.gui.theme.ThemeColors;

public class CustomerSearchDialog extends JDialog {
    private final CustomerBUS bus = new CustomerBUS();
    private final JTextField searchField = new JTextField();
    private final JPanel listPanel = new JPanel();
    private final Consumer<Customer> onSelected;
    private List<Customer> allCustomers;

    public CustomerSearchDialog(Window owner, Consumer<Customer> onSelected) {
        super(owner, "Chọn khách hàng", ModalityType.APPLICATION_MODAL);
        this.onSelected = onSelected;
        
        setSize(450, 550);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        
        JPanel header = new JPanel(new BorderLayout(10, 10));
        header.setBorder(new EmptyBorder(15, 15, 15, 15));
        header.setBackground(Color.WHITE);
        
        JLabel title = new JLabel("Tìm kiếm khách hàng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(title, BorderLayout.NORTH);
        
        searchField.setPreferredSize(new Dimension(0, 40));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 230, 245), 1),
            new EmptyBorder(0, 10, 0, 10)
        ));
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                filter(searchField.getText().trim());
            }
        });
        header.add(searchField, BorderLayout.CENTER);
        
        add(header, BorderLayout.NORTH);
        
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(245, 248, 252));
        
        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
        
        JButton btnClose = new JButton("Hủy bỏ");
        btnClose.addActionListener(e -> dispose());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBorder(new EmptyBorder(10, 15, 10, 15));
        footer.add(btnClose);
        add(footer, BorderLayout.SOUTH);
        
        loadData();
    }

    private void loadData() {
        allCustomers = bus.getAllWithStats();
        filter("");
    }

    private void filter(String keyword) {
        listPanel.removeAll();
        String kw = keyword.toLowerCase();
        for (Customer c : allCustomers) {
            if (kw.isEmpty() || c.getHoTenKH().toLowerCase().contains(kw) || c.getSdt().contains(kw) || c.getCCCD().contains(kw)) {
                listPanel.add(createRow(c));
                listPanel.add(Box.createVerticalStrut(8));
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createRow(Customer c) {
        JPanel row = new JPanel(new BorderLayout(15, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(12, 15, 12, 15));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);
        JLabel name = new JLabel(c.getHoTenKH());
        name.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel meta = new JLabel(c.getCCCD() + " • " + c.getSdt());
        meta.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        meta.setForeground(new Color(120, 135, 160));
        info.add(name);
        info.add(meta);
        
        row.add(info, BorderLayout.CENTER);
        
        JButton btnSelect = new JButton("Chọn");
        btnSelect.setBackground(ThemeColors.PRIMARY);
        btnSelect.setForeground(Color.WHITE);
        btnSelect.setFocusPainted(false);
        row.add(btnSelect, BorderLayout.EAST);
        
        btnSelect.addActionListener(e -> {
            onSelected.accept(c);
            dispose();
        });
        
        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                onSelected.accept(c);
                dispose();
            }
        });
        
        return row;
    }
}
