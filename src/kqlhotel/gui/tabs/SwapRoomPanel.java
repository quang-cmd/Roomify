package kqlhotel.gui.tabs;

import javax.swing.*;
import java.awt.*;

public class SwapRoomPanel extends JPanel {

    public SwapRoomPanel() {
        setOpaque(false);
        setLayout(new GridBagLayout());

        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(500, 350));
        card.setBackground(Color.WHITE);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220,220,220),1,true),
                BorderFactory.createEmptyBorder(20,20,20,20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("ĐỔI PHÒNG", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(33,37,41));

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        card.add(title, gbc);

        gbc.gridwidth = 1;

        // ComboBox style
        String[] rooms = {"101", "102", "103", "201", "202"};

        JComboBox<String> cbCurrent = new JComboBox<>(rooms);
        JComboBox<String> cbNew = new JComboBox<>(rooms);

        cbCurrent.setPreferredSize(new Dimension(200, 35));
        cbNew.setPreferredSize(new Dimension(200, 35));

        gbc.gridx = 0; gbc.gridy = 1;
        card.add(new JLabel("Phòng hiện tại:"), gbc);

        gbc.gridx = 1;
        card.add(cbCurrent, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        card.add(new JLabel("Phòng muốn chuyển:"), gbc);

        gbc.gridx = 1;
        card.add(cbNew, gbc);

        JButton btnSwap = new JButton("Đổi phòng");
        btnSwap.setBackground(new Color(255, 140, 0)); // cam
        btnSwap.setForeground(Color.WHITE);
        btnSwap.setFocusPainted(false);
        btnSwap.setFont(new Font("Segoe UI", Font.BOLD, 14));

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        card.add(btnSwap, gbc);

        add(card);

        // Event
        btnSwap.addActionListener(e -> {
            String c = cbCurrent.getSelectedItem().toString();
            String n = cbNew.getSelectedItem().toString();

            if (c.equals(n)) {
                JOptionPane.showMessageDialog(this, "Không thể đổi cùng phòng!");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Đã đổi từ phòng " + c + " sang phòng " + n);
            }
        });
    }
}