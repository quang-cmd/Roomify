package kqlhotel.gui.tabs;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import kqlhotel.bus.service.ServiceBUS;
import kqlhotel.entity.Service;

public class ServicesPanel extends JPanel {

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0");
    private static final List<String> CATEGORIES = Arrays.asList("Tất cả", "Vệ sinh", "Ẩm thực", "Thư giãn", "Vận chuyển", "Tiện ích");



    private final ServiceBUS bus = new ServiceBUS();
    private final JPanel filterPanel = new JPanel();
    private final JPanel gridPanel = new JPanel();
    private final JLabel countLabel = new JLabel();
    private final Map<String, JButton> filterButtons = new LinkedHashMap<>();
    private List<Service> services = new ArrayList<>();
    private String selectedCategory = "Tất cả";



    public ServicesPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 18));
        setBorder(new EmptyBorder(22, 26, 22, 26));

        add(createHeader(), BorderLayout.NORTH);
        add(createBody(), BorderLayout.CENTER);

        loadServices();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        countLabel.setForeground(new Color(100, 116, 139));

        JButton addButton = createPrimaryButton("Thêm dịch vụ", "services.png");
        addButton.addActionListener(e -> showServiceDialog(null));

        header.add(countLabel, BorderLayout.WEST);
        header.add(addButton, BorderLayout.EAST);
        return header;
    }

    private JPanel createBody() {
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);

        filterPanel.setOpaque(false);
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.X_AXIS));
        for (String category : CATEGORIES) {
            JButton button = createFilterButton(category);
            filterButtons.put(category, button);
            filterPanel.add(button);
            filterPanel.add(Box.createHorizontalStrut(10));
        }

        gridPanel.setOpaque(false);
        gridPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 16, 16));

        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        body.add(filterPanel, BorderLayout.NORTH);
        body.add(scrollPane, BorderLayout.CENTER);
        return body;
    }





    private void loadServices() {
        services = bus.getAllDetailed();
        long active = services.stream().filter(s -> "DangHoatDong".equalsIgnoreCase(s.getTrangThai())).count();
        countLabel.setText(services.size() + " dịch vụ · " + active + " đang hoạt động");
        renderGrid();
        updateFilterStyles();
    }





    private void renderGrid() {
        gridPanel.removeAll();
        List<Service> filtered = getFilteredServices();
        for (Service service : filtered) {
            gridPanel.add(createServiceCard(service));
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private List<Service> getFilteredServices() {
        if ("Tất cả".equals(selectedCategory) || "All".equals(selectedCategory)) {
            return services;
        }
        List<Service> filtered = new ArrayList<>();
        for (Service service : services) {
            if (normalizeCategory(selectedCategory).equals(normalizeCategory(service.getLoaiDV()))) {
                filtered.add(service);
            }
        }
        return filtered;
    }





    private JPanel createServiceCard(Service service) {
        RoundedPanel card = new RoundedPanel(22, Color.WHITE, new Color(226, 232, 240), 1f, new Color(15, 23, 42, 10), 4);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        card.setPreferredSize(new Dimension(392, 260));
        card.setMinimumSize(new Dimension(392, 260));
        card.setMaximumSize(new Dimension(392, 260));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(createCategoryIcon(service.getLoaiDV()), BorderLayout.WEST);



        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));
        actions.add(createStatusBadge(service));
        actions.add(Box.createHorizontalStrut(10));
        JButton editButton = new JButton();
        ImageIcon editIcon = loadIcon("edit.png", 16, 16);
        if (editIcon != null) {
            editButton.setIcon(editIcon);
        } else {
            editButton.setText("✎");
        }
        editButton.setBorder(null);
        editButton.setContentAreaFilled(false);
        editButton.setFocusPainted(false);
        editButton.setForeground(new Color(148, 163, 184));
        editButton.setToolTipText("Sửa dịch vụ");
        editButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        editButton.addActionListener(e -> showServiceDialog(service));
        actions.add(editButton);
        top.add(actions, BorderLayout.EAST);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel name = new JLabel(service.getTenDV());
        name.setFont(new Font("Segoe UI", Font.BOLD, 22));
        name.setForeground(new Color(15, 23, 42));

        JLabel desc = new JLabel("<html><div style='width:320px;'>" + safe(service.getMoTa()) + "</div></html>");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        desc.setForeground(new Color(100, 116, 139));

        center.add(name);
        center.add(Box.createVerticalStrut(4));
        center.add(desc);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(6, 0, 0, 0));

        bottom.add(createCategoryChip(service.getLoaiDV()), BorderLayout.WEST);



        JPanel priceWrap = new JPanel();
        priceWrap.setOpaque(false);
        priceWrap.setLayout(new BoxLayout(priceWrap, BoxLayout.Y_AXIS));
        JLabel price = new JLabel(formatPrice(service.getGia()));
        price.setFont(new Font("Segoe UI", Font.BOLD, 24));
        price.setForeground(new Color(15, 23, 42));
        price.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel unit = new JLabel(getUnitLabel(service.getLoaiDV()));
        unit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        unit.setForeground(new Color(148, 163, 184));
        unit.setAlignmentX(Component.RIGHT_ALIGNMENT);




        priceWrap.add(price);
        priceWrap.add(unit);

        bottom.add(priceWrap, BorderLayout.EAST);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(bottom, BorderLayout.CENTER);

        boolean active = "DangHoatDong".equalsIgnoreCase(service.getTrangThai());
        JButton statusToggle = new JButton(active ? "Tạm dừng" : "Kích hoạt") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(226, 232, 240));
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        statusToggle.setFocusPainted(false);
        statusToggle.setContentAreaFilled(false);
        statusToggle.setBorder(new EmptyBorder(8, 12, 8, 12));
        statusToggle.setBackground(new Color(248, 250, 252));
        statusToggle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        statusToggle.setForeground(new Color(71, 85, 105));
        statusToggle.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        statusToggle.addActionListener(e -> toggleServiceStatus(service));
        footer.add(statusToggle, BorderLayout.SOUTH);

        card.add(top, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private void toggleServiceStatus(Service service) {
        String nextStatus = "DangHoatDong".equalsIgnoreCase(service.getTrangThai()) ? "NgungHoatDong" : "DangHoatDong";
        if (bus.updateStatus(service.getMaDV(), nextStatus)) {
            loadServices();
        } else {
            JOptionPane.showMessageDialog(this, "Không thể cập nhật trạng thái.");
        }
    }

    private void showServiceDialog(Service existing) {
        boolean editing = existing != null;
        JDialog dialog = new JDialog();
        dialog.setModal(true);
        dialog.setTitle(editing ? "Sửa dịch vụ" : "Thêm dịch vụ mới");
        dialog.setSize(440, 580);
        dialog.setLocationRelativeTo(this);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(18, 45, 18, 45));

        JTextField nameField = createDialogField(editing ? existing.getTenDV() : "");
        JTextField priceField = createDialogField(editing ? String.valueOf((long) existing.getGia()) : "");
        JComboBox<String> categoryBox = new JComboBox<>(new String[]{"Vệ sinh", "Ẩm thực", "Thư giãn", "Vận chuyển", "Tiện ích"});
        categoryBox.setSelectedItem(editing ? getCategoryLabel(normalizeCategory(existing.getLoaiDV())) : "Tiện ích");
        categoryBox.setPreferredSize(new Dimension(340, 38));
        categoryBox.setMaximumSize(new Dimension(340, 38));



        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Đang hoạt động", "Ngưng hoạt động"});
        statusBox.setSelectedItem(mapStatusLabel(editing ? existing.getTrangThai() : "DangHoatDong"));
        statusBox.setPreferredSize(new Dimension(340, 38));
        statusBox.setMaximumSize(new Dimension(340, 38));
        JTextArea descriptionArea = new JTextArea(editing ? safe(existing.getMoTa()) : "");
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setRows(3);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240)),
            new EmptyBorder(10, 12, 10, 12)
        ));
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        descriptionScroll.setMaximumSize(new Dimension(340, 80));
        descriptionScroll.setPreferredSize(new Dimension(340, 80));

        root.add(createDialogFieldGroup("Tên dịch vụ", nameField));
        root.add(Box.createVerticalStrut(10));
        root.add(createDialogFieldGroup("Đơn giá", priceField));
        root.add(Box.createVerticalStrut(10));
        root.add(createDialogFieldGroup("Danh mục", categoryBox));
        root.add(Box.createVerticalStrut(10));


        root.add(createDialogFieldGroup("Trạng thái", statusBox));
        root.add(Box.createVerticalStrut(10));
        root.add(createDialogFieldGroup("Mô tả", descriptionScroll));
        root.add(Box.createVerticalStrut(18));

        JPanel actions = new JPanel(new BorderLayout(10, 0));
        actions.setOpaque(false);
        actions.setMaximumSize(new Dimension(340, 40));
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton cancelButton = createOutlineButton("Hủy");
        cancelButton.addActionListener(e -> dialog.dispose());
        JButton saveButton = createPrimaryButton(editing ? "Cập nhật" : "Lưu dịch vụ", editing ? "edit.png" : "services.png");
        saveButton.addActionListener(e -> {
            if (nameField.getText().trim().isBlank() || priceField.getText().trim().isBlank()) {
                JOptionPane.showMessageDialog(dialog, "Tên và đơn giá là bắt buộc.");
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Đơn giá không hợp lệ.");
                return;
            }

            Service payload = editing ? existing : new Service();
            if (!editing) {
                payload.setMaDV(bus.getNextId());
            }
            payload.setTenDV(nameField.getText().trim());
            payload.setGia(price);
            payload.setLoaiDV(normalizeCategory(String.valueOf(categoryBox.getSelectedItem())));

            payload.setTrangThai(mapStatusCode(String.valueOf(statusBox.getSelectedItem())));
            payload.setMoTa(descriptionArea.getText().trim());

            boolean success = editing ? bus.update(payload) : bus.insert(payload);
            if (success) {
                dialog.dispose();
                loadServices();
            } else {
                JOptionPane.showMessageDialog(dialog, "Không thể lưu dịch vụ.");
            }
        });

        actions.add(cancelButton, BorderLayout.WEST);
        actions.add(saveButton, BorderLayout.EAST);
        root.add(actions);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private JPanel createDialogFieldGroup(String labelText, Component field) {
        JPanel group = new JPanel();
        group.setOpaque(false);
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(labelText);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        if (field instanceof javax.swing.JComponent) {
            ((javax.swing.JComponent) field).setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        group.add(label);
        group.add(Box.createVerticalStrut(6));
        group.add(field);
        group.setAlignmentX(Component.LEFT_ALIGNMENT);
        return group;
    }

    private JTextField createDialogField(String value) {
        JTextField field = new JTextField(value);
        field.setMaximumSize(new Dimension(340, 38));
        field.setPreferredSize(new Dimension(340, 38));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240)),
            new EmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }


    private String safe(String value) {
        return value == null ? "" : value;
    }


    private String formatPrice(double value) {
        return MONEY_FORMAT.format(value) + "đ";
    }

    private JButton createPrimaryButton(String text, String iconFile) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        ImageIcon icon = loadIcon(iconFile, 16, 16);
        if (icon != null) {
            button.setIcon(icon);
            button.setIconTextGap(10);
        }
        button.setBackground(new Color(15, 23, 42));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createOutlineButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(71, 85, 105));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240)),
            new EmptyBorder(10, 16, 10, 16)
        ));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return button;
    }

    private JLabel createStatusBadge(Service service) {
        boolean active = "DangHoatDong".equalsIgnoreCase(service.getTrangThai());
        JLabel label = new JLabel(active ? "Hoạt động" : "Tạm dừng", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        label.setOpaque(false);
        label.setBorder(new EmptyBorder(4, 12, 4, 12));
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setBackground(active ? new Color(220, 252, 231) : new Color(241, 245, 249));
        label.setForeground(active ? new Color(22, 163, 74) : new Color(148, 163, 184));
        return label;
    }

    private JButton createFilterButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                if (getBackground() == Color.WHITE) {
                    g2.setColor(new Color(226, 232, 240));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(8, 18, 8, 18));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        button.addActionListener(e -> {
            selectedCategory = text;
            updateFilterStyles();
            renderGrid();
        });
        return button;
    }

    private void updateFilterStyles() {
        for (Map.Entry<String, JButton> entry : filterButtons.entrySet()) {
            boolean active = entry.getKey().equals(selectedCategory);
            JButton button = entry.getValue();
            button.setBackground(active ? new Color(15, 23, 42) : Color.WHITE);
            button.setForeground(active ? Color.WHITE : new Color(71, 85, 105));
        }
    }

    private JPanel createCategoryIcon(String category) {
        Color bg;
        String file;
        switch (normalizeCategory(category)) {
            case "Housekeeping":
                bg = new Color(243, 232, 255); // Purple
                file = "hygiene.png";
                break;
            case "Food & Drink":
                bg = new Color(255, 247, 237); // Orange
                file = "cuisine.png";
                break;
            case "Relaxation":
                bg = new Color(236, 253, 245); // Green
                file = "star.png";
                break;
            case "Transport":
                bg = new Color(239, 246, 255); // Blue
                file = "transport.png";
                break;
            default:
                bg = new Color(238, 242, 255); // Indigo
                file = "wifi.png";
                break;
        }

        JPanel iconWrap = new RoundedPanel(16, bg, null, 0f, new Color(0, 0, 0, 0), 0);
        iconWrap.setLayout(new BorderLayout());
        iconWrap.setPreferredSize(new Dimension(42, 42));
        JLabel icon = new JLabel("", SwingConstants.CENTER);
        ImageIcon image = loadIcon(file, 18, 18);
        if (image != null) {
            icon.setIcon(image);
        } else {
            icon.setText("•");
        }
        iconWrap.add(icon, BorderLayout.CENTER);
        return iconWrap;
    }

    private JLabel createCategoryChip(String category) {
        String normalized = normalizeCategory(category);
        JLabel chip = new JLabel(getCategoryLabel(normalized));
        chip.setOpaque(true);
        chip.setBorder(new EmptyBorder(4, 10, 4, 10));
        chip.setFont(new Font("Segoe UI", Font.BOLD, 11));

        Color bg;
        Color fg;
        switch (normalized) {
            case "Housekeeping":
                bg = new Color(243, 232, 255);
                fg = new Color(124, 58, 237);
                break;
            case "Food & Drink":
                bg = new Color(255, 237, 213);
                fg = new Color(234, 88, 12);
                break;
            case "Relaxation":
                bg = new Color(220, 252, 231);
                fg = new Color(22, 163, 74);
                break;
            case "Transport":
                bg = new Color(219, 234, 254);
                fg = new Color(37, 99, 235);
                break;
            default:
                bg = new Color(224, 231, 255);
                fg = new Color(99, 102, 241);
                break;
        }

        chip.setBackground(bg);
        chip.setForeground(fg);
        return chip;
    }

    private String getCategoryLabel(String normalized) {
        switch (normalized) {
            case "Housekeeping": return "Vệ sinh";
            case "Food & Drink": return "Ẩm thực";
            case "Relaxation": return "Thư giãn";
            case "Transport": return "Vận chuyển";
            default: return "Tiện ích";
        }
    }

    private String getUnitLabel(String category) {
        switch (normalizeCategory(category)) {
            case "Food & Drink":
                return "/người";
            case "Housekeeping":
                return "/lần";
            case "Relaxation":
                return "/gói";
            case "Transport":
                return "/chuyến";
            default:
                return "/dịch vụ";
        }
    }

    private String normalizeCategory(String category) {
        String raw = safe(category).trim().toLowerCase();
        if (raw.isEmpty()) {
            return "Utilities";
        }
        if (raw.contains("house") || raw.contains("bu") || raw.contains("giat") || raw.contains("don phong") || raw.contains("vệ sinh") || raw.contains("ve sinh")) {
            return "Housekeeping";
        }
        if (raw.contains("food") || raw.contains("drink") || raw.contains("ăn") || raw.contains("uong") || raw.contains("buffet") || raw.contains("mi") || raw.contains("nuoc") || raw.contains("ẩm thực") || raw.contains("am thuc")) {
            return "Food & Drink";
        }
        if (raw.contains("relax") || raw.contains("thu gian") || raw.contains("spa") || raw.contains("massage") || raw.contains("gym") || raw.contains("thư giãn")) {
            return "Relaxation";
        }
        if (raw.contains("transport") || raw.contains("van chuyen") || raw.contains("dua don") || raw.contains("xe") || raw.contains("vận chuyển")) {
            return "Transport";
        }
        if (raw.contains("utilit") || raw.contains("tien ich") || raw.contains("tiện ích")) {
            return "Utilities";
        }
        return category;
    }

    private String mapStatusLabel(String status) {




        return "DangHoatDong".equalsIgnoreCase(status) ? "Đang hoạt động" : "Ngưng hoạt động";
    }

    private String mapStatusCode(String status) {
        return "Đang hoạt động".equalsIgnoreCase(status) ? "DangHoatDong" : "NgungHoatDong";
    }

    private ImageIcon loadIcon(String filename, int width, int height) {
        try {
            URL resource = getClass().getResource("/kqlhotel/resources/icons/" + filename);
            if (resource == null) {
                java.io.File file = new java.io.File("src/kqlhotel/resources/icons/" + filename);
                if (file.exists()) {
                    resource = file.toURI().toURL();
                }
            }
            if (resource != null) {
                ImageIcon icon = new ImageIcon(resource);
                return new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
            }
        } catch (Exception e) {
            // Ignore icon loading failures.
        }
        return null;
    }

    private static class RoundedPanel extends JPanel {
        private final int arc;
        private final Color fill;
        private final Color border;
        private final float borderWidth;
        private final Color shadow;
        private final int shadowSize;

        RoundedPanel(int arc, Color fill, Color border, float borderWidth, Color shadow, int shadowSize) {
            this.arc = arc;
            this.fill = fill;
            this.border = border;
            this.borderWidth = borderWidth;
            this.shadow = shadow;
            this.shadowSize = shadowSize;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int h = getHeight() - Math.max(shadowSize, 1);
            if (shadowSize > 0) {
                g2.setColor(shadow);
                g2.fillRoundRect(0, shadowSize, getWidth() - 1, h, arc, arc);
            }
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, h, arc, arc);
            if (border != null && borderWidth > 0f) {
                g2.setColor(border);
                g2.setStroke(new BasicStroke(borderWidth));
                g2.drawRoundRect(0, 0, getWidth() - 1, h, arc, arc);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override
        public Dimension preferredLayoutSize(java.awt.Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(java.awt.Container target) {
            Dimension minimum = layoutSize(target, false);
            minimum.width -= getHgap() + 1;
            return minimum;
        }

        private Dimension layoutSize(java.awt.Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                int targetWidth = target.getWidth();

                if (targetWidth == 0) {
                    targetWidth = Integer.MAX_VALUE;
                    for (java.awt.Container parent = target.getParent(); parent != null; parent = parent.getParent()) {
                        if (parent.getWidth() > 0) {
                            targetWidth = parent.getWidth();
                            break;
                        }
                    }
                }

                Insets insets = target.getInsets();
                int horizontalInsetsAndGap = insets.left + insets.right + getHgap() * 2;
                int maxWidth = targetWidth - horizontalInsetsAndGap;

                Dimension dim = new Dimension(0, 0);
                int rowWidth = 0;
                int rowHeight = 0;

                int members = target.getComponentCount();
                for (int i = 0; i < members; i++) {
                    Component component = target.getComponent(i);
                    if (!component.isVisible()) {
                        continue;
                    }

                    Dimension componentSize = preferred ? component.getPreferredSize() : component.getMinimumSize();
                    if (rowWidth + componentSize.width > maxWidth) {
                        addRow(dim, rowWidth, rowHeight);
                        rowWidth = 0;
                        rowHeight = 0;
                    }

                    if (rowWidth != 0) {
                        rowWidth += getHgap();
                    }
                    rowWidth += componentSize.width;
                    rowHeight = Math.max(rowHeight, componentSize.height);
                }

                addRow(dim, rowWidth, rowHeight);
                dim.width += horizontalInsetsAndGap;
                dim.height += insets.top + insets.bottom + getVgap() * 2;

                java.awt.Container scrollPane = javax.swing.SwingUtilities.getAncestorOfClass(JScrollPane.class, target);
                if (scrollPane != null) {
                    dim.width -= getHgap() + 1;
                }

                return dim;
            }
        }

        private void addRow(Dimension dim, int rowWidth, int rowHeight) {
            dim.width = Math.max(dim.width, rowWidth);
            if (dim.height > 0) {
                dim.height += getVgap();
            }
            dim.height += rowHeight;
        }
    }
}
