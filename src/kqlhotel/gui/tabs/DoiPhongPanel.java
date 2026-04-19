package kqlhotel.gui.tabs;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import kqlhotel.bus.DoiPhongBus;
import kqlhotel.entity.DoiPhongRoomOption;
import kqlhotel.entity.DoiPhongSearchResult;

public class DoiPhongPanel extends JPanel {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DoiPhongBus doiPhongBus = new DoiPhongBus();
    private final InputField inpMaDatPhong;
    private final InputField inpTenKhach;
    private final InputField inpSoDienThoai;
    private final InputField inpSoPhong;
    private final JPanel rightPanel;
    private List<DoiPhongSearchResult> currentResults;

    public DoiPhongPanel() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 22));
        setBorder(new EmptyBorder(22, 28, 22, 28));

        inpMaDatPhong = new InputField("search.png", "Vi du: DP001");
        inpTenKhach = new InputField("customers.png", "Vi du: Nguyen Van A");
        inpSoDienThoai = new InputField("search.png", "Vi du: 0820000001");
        inpSoPhong = new InputField("room.png", "Vi du: P101");
        rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleWrap = new JPanel();
        titleWrap.setOpaque(false);
        titleWrap.setLayout(new BoxLayout(titleWrap, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("\u0110\u1ed5i ph\u00f2ng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(15, 23, 42));

        JLabel subtitle = new JLabel("Tra c\u1ee9u kh\u00e1ch \u0111ang l\u01b0u tr\u00fa v\u00e0 \u0111\u1ed5i sang ph\u00f2ng m\u1edbi ph\u00f9 h\u1ee3p");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitle.setForeground(new Color(100, 116, 139));

        titleWrap.add(title);
        titleWrap.add(Box.createVerticalStrut(6));
        titleWrap.add(subtitle);

        header.add(titleWrap, BorderLayout.WEST);
        header.add(createStepIndicator(), BorderLayout.EAST);
        return header;
    }

    private JPanel createStepIndicator() {
        JPanel wrapper = new RoundedBlockPanel(18, Color.WHITE, new Color(226, 232, 240), 1f, new Color(15, 23, 42, 10), 4);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
        wrapper.setBorder(new EmptyBorder(14, 16, 14, 16));

        wrapper.add(createStepChip("1", "Chon khach", true));
        wrapper.add(Box.createHorizontalStrut(16));
        wrapper.add(createStepDivider());
        wrapper.add(Box.createHorizontalStrut(16));
        wrapper.add(createStepChip("2", "Chon phong moi", false));
        return wrapper;
    }

    private JPanel createStepChip(String number, String text, boolean active) {
        JPanel chip = new JPanel();
        chip.setOpaque(false);
        chip.setLayout(new BoxLayout(chip, BoxLayout.X_AXIS));

        JPanel circle = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(active ? new Color(15, 23, 42) : new Color(241, 245, 249));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        circle.setOpaque(false);
        circle.setPreferredSize(new Dimension(30, 30));
        circle.setMaximumSize(new Dimension(30, 30));

        JLabel numberLabel = new JLabel(number, SwingConstants.CENTER);
        numberLabel.setForeground(active ? Color.WHITE : new Color(148, 163, 184));
        numberLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        circle.add(numberLabel, BorderLayout.CENTER);

        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 14));
        textLabel.setForeground(active ? new Color(15, 23, 42) : new Color(148, 163, 184));

        chip.add(circle);
        chip.add(Box.createHorizontalStrut(10));
        chip.add(textLabel);
        return chip;
    }

    private Component createStepDivider() {
        JPanel divider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(226, 232, 240));
                g2.fillRoundRect(0, getHeight() / 2 - 1, getWidth(), 2, 2, 2);
                g2.dispose();
            }
        };
        divider.setOpaque(false);
        divider.setPreferredSize(new Dimension(26, 10));
        divider.setMaximumSize(new Dimension(26, 10));
        return divider;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(26, 0));
        content.setOpaque(false);

        JPanel leftWrap = new JPanel(new BorderLayout());
        leftWrap.setOpaque(false);
        leftWrap.setPreferredSize(new Dimension(430, 0));
        leftWrap.add(createSearchCard(), BorderLayout.NORTH);

        showEmptyState();

        content.add(leftWrap, BorderLayout.WEST);
        content.add(rightPanel, BorderLayout.CENTER);
        return content;
    }

    private JPanel createSearchCard() {
        RoundedBlockPanel card = new RoundedBlockPanel(22, Color.WHITE, new Color(226, 232, 240), 1f, new Color(15, 23, 42, 12), 5);
        card.setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(new EmptyBorder(18, 20, 16, 20));

        JPanel titleRow = new JPanel();
        titleRow.setOpaque(false);
        titleRow.setLayout(new BoxLayout(titleRow, BoxLayout.X_AXIS));

        JPanel iconBadge = createSoftIconBadge("search.png", 18, new Color(239, 246, 255), new Color(59, 130, 246));
        JLabel title = new JLabel("Tim khach can doi phong");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(new Color(15, 23, 42));

        JLabel sub = new JLabel("<html>Nhap it nhat 1 thong tin de tra cuu khach can doi<br>phong</html>");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(148, 163, 184));

        titleRow.add(iconBadge);
        titleRow.add(Box.createHorizontalStrut(10));
        titleRow.add(title);

        top.add(titleRow);
        top.add(Box.createVerticalStrut(12));
        top.add(sub);

        JPanel divider = new JPanel();
        divider.setBackground(new Color(241, 245, 249));
        divider.setPreferredSize(new Dimension(1, 1));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(18, 20, 20, 20));

        form.add(createFieldGroup("Ma dat phong", inpMaDatPhong));
        form.add(Box.createVerticalStrut(14));
        form.add(createFieldGroup("Ten khach", inpTenKhach));
        form.add(Box.createVerticalStrut(14));
        form.add(createFieldGroup("So dien thoai", inpSoDienThoai));
        form.add(Box.createVerticalStrut(14));
        form.add(createFieldGroup("So phong", inpSoPhong));
        form.add(Box.createVerticalStrut(22));

        JButton searchButton = new JButton("Tim khach hang");
        ImageIcon buttonIcon = loadIcon("search.png", 15, 15);
        if (buttonIcon != null) {
            searchButton.setIcon(buttonIcon);
            searchButton.setIconTextGap(8);
        }
        searchButton.setBackground(new Color(23, 33, 54));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        searchButton.setFocusPainted(false);
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBorder(new EmptyBorder(14, 18, 14, 18));
        searchButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        searchButton.addActionListener(e -> performSearch());

        form.add(searchButton);

        card.add(top, BorderLayout.NORTH);
        card.add(divider, BorderLayout.CENTER);
        card.add(form, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createFieldGroup(String labelText, InputField field) {
        JPanel group = new JPanel();
        group.setOpaque(false);
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(71, 85, 105));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        group.add(label);
        group.add(Box.createVerticalStrut(8));
        group.add(field);
        return group;
    }

    private void performSearch() {
        String maDatPhong = inpMaDatPhong.getValue().trim();
        String tenKhach = inpTenKhach.getValue().trim();
        String soDienThoai = inpSoDienThoai.getValue().trim();
        String soPhong = inpSoPhong.getValue().trim();

        if (maDatPhong.isEmpty() && tenKhach.isEmpty() && soDienThoai.isEmpty() && soPhong.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui long nhap it nhat 1 thong tin de tim kiem.", "Thong bao", JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentResults = doiPhongBus.searchBookings(maDatPhong, tenKhach, soDienThoai, soPhong);
        if (currentResults.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Khong tim thay khach hang hoac don dat phong phu hop.", "Thong bao", JOptionPane.INFORMATION_MESSAGE);
            showEmptyState();
            return;
        }

        showSearchResult(currentResults.get(0));
    }

    private void showSearchResult(DoiPhongSearchResult result) {
        rightPanel.removeAll();

        RoundedBlockPanel card = new RoundedBlockPanel(24, Color.WHITE, new Color(226, 232, 240), 1f, new Color(15, 23, 42, 12), 5);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Thong tin khach va phong hien tai");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(15, 23, 42));

        String countText = currentResults != null && currentResults.size() > 1
            ? "Dang hien thi ket qua dau tien trong " + currentResults.size() + " ket qua tim thay."
            : "Da tim thay 1 ket qua phu hop voi thong tin tra cuu.";
        JLabel info = new JLabel(countText);
        info.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        info.setForeground(new Color(100, 116, 139));

        wrap.add(title);
        wrap.add(Box.createVerticalStrut(6));
        wrap.add(info);
        wrap.add(Box.createVerticalStrut(22));
        wrap.add(createInfoGrid(result));
        wrap.add(Box.createVerticalStrut(22));
        wrap.add(createRoomSelectionBlock(result));

        card.add(wrap, BorderLayout.CENTER);
        rightPanel.add(card, BorderLayout.NORTH);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private JPanel createInfoGrid(DoiPhongSearchResult result) {
        JPanel grid = new JPanel(new java.awt.GridLayout(0, 2, 16, 16));
        grid.setOpaque(false);
        grid.add(createInfoItem("Ma dat phong", result.getMaDatPhong()));
        grid.add(createInfoItem("Khach hang", result.getTenKhachHang()));
        grid.add(createInfoItem("So dien thoai", result.getSoDienThoai()));
        grid.add(createInfoItem("CCCD", result.getCccd()));
        grid.add(createInfoItem("Phong hien tai", result.getMaPhongHienTai()));
        grid.add(createInfoItem("Loai phong", result.getLoaiPhongHienTai()));
        grid.add(createInfoItem("Ngay nhan", formatDateTime(result.getNgayNhan())));
        grid.add(createInfoItem("Ngay tra", formatDateTime(result.getNgayTra())));
        return grid;
    }

    private JPanel createInfoItem(String labelText, String value) {
        JPanel item = new RoundedBlockPanel(18, new Color(248, 250, 252), new Color(226, 232, 240), 1f, new Color(15, 23, 42, 0), 0);
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(100, 116, 139));

        JLabel content = new JLabel(value == null || value.isEmpty() ? "-" : value);
        content.setFont(new Font("Segoe UI", Font.BOLD, 14));
        content.setForeground(new Color(15, 23, 42));

        item.add(label);
        item.add(Box.createVerticalStrut(6));
        item.add(content);
        return item;
    }

    private JPanel createRoomSelectionBlock(DoiPhongSearchResult result) {
        JPanel block = new RoundedBlockPanel(20, new Color(248, 250, 252), new Color(226, 232, 240), 1f, new Color(15, 23, 42, 0), 0);
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("Chon phong moi");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(15, 23, 42));

        JLabel subtitle = new JLabel("He thong se lay cac phong dang trong trong database QLKhachSan.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(100, 116, 139));

        List<DoiPhongRoomOption> availableRooms = doiPhongBus.getAvailableRooms(result.getMaPhongHienTai());
        JComboBox<DoiPhongRoomOption> roomCombo = new JComboBox<>(availableRooms.toArray(new DoiPhongRoomOption[0]));
        roomCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        roomCombo.setPreferredSize(new Dimension(0, 40));
        roomCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton changeButton = new JButton("Xac nhan doi phong");
        changeButton.setBackground(new Color(37, 99, 235));
        changeButton.setForeground(Color.WHITE);
        changeButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        changeButton.setFocusPainted(false);
        changeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        changeButton.setBorder(new EmptyBorder(12, 16, 12, 16));
        changeButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        changeButton.setMaximumSize(new Dimension(220, 44));
        changeButton.setEnabled(!availableRooms.isEmpty());
        changeButton.addActionListener(e -> confirmChangeRoom(result, roomCombo));

        JLabel emptyRoomsLabel = new JLabel("Khong co phong trong de doi.");
        emptyRoomsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        emptyRoomsLabel.setForeground(new Color(220, 38, 38));
        emptyRoomsLabel.setVisible(availableRooms.isEmpty());

        block.add(title);
        block.add(Box.createVerticalStrut(6));
        block.add(subtitle);
        block.add(Box.createVerticalStrut(16));
        block.add(roomCombo);
        block.add(Box.createVerticalStrut(10));
        block.add(emptyRoomsLabel);
        block.add(Box.createVerticalStrut(12));
        block.add(changeButton);
        return block;
    }

    private void confirmChangeRoom(DoiPhongSearchResult result, JComboBox<DoiPhongRoomOption> roomCombo) {
        DoiPhongRoomOption selectedRoom = (DoiPhongRoomOption) roomCombo.getSelectedItem();
        if (selectedRoom == null) {
            JOptionPane.showMessageDialog(this, "Vui long chon phong moi.", "Thong bao", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmed = JOptionPane.showConfirmDialog(
            this,
            "Doi phong tu " + result.getMaPhongHienTai() + " sang " + selectedRoom.getMaPhong() + "?",
            "Xac nhan doi phong",
            JOptionPane.YES_NO_OPTION
        );
        if (confirmed != JOptionPane.YES_OPTION) {
            return;
        }

        boolean success = doiPhongBus.changeRoom(result.getMaChiTietDatPhong(), selectedRoom.getMaPhong());
        if (success) {
            JOptionPane.showMessageDialog(this, "Doi phong thanh cong.", "Thong bao", JOptionPane.INFORMATION_MESSAGE);
            inpSoPhong.setValue(selectedRoom.getMaPhong());
            performSearch();
        } else {
            JOptionPane.showMessageDialog(this, "Khong the doi phong. Vui long kiem tra du lieu database.", "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showEmptyState() {
        rightPanel.removeAll();

        JPanel emptyWrap = new JPanel(new GridBagLayout());
        emptyWrap.setOpaque(false);

        JPanel empty = new JPanel();
        empty.setOpaque(false);
        empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));

        JPanel stateIcon = createLargeStateIcon();
        stateIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Chua co du lieu doi phong");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(51, 65, 85));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("<html><div style='text-align:center;'>Tim khach hang dang co phong hop le de thuc<br>hien doi phong</div></html>");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(new Color(148, 163, 184));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        empty.add(stateIcon);
        empty.add(Box.createVerticalStrut(24));
        empty.add(title);
        empty.add(Box.createVerticalStrut(12));
        empty.add(sub);

        emptyWrap.add(empty);
        rightPanel.add(emptyWrap, BorderLayout.CENTER);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private JPanel createLargeStateIcon() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(241, 245, 249));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(82, 82));

        JLabel icon = new JLabel("", SwingConstants.CENTER);
        ImageIcon swapIcon = loadIcon("swap-room.png", 38, 38);
        if (swapIcon != null) {
            icon.setIcon(swapIcon);
        } else {
            icon.setText("\u21c4");
            icon.setFont(new Font("Segoe UI", Font.PLAIN, 28));
            icon.setForeground(new Color(191, 219, 254));
        }
        panel.add(icon, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSoftIconBadge(String iconFile, int size, Color bg, Color fallbackColor) {
        JPanel badge = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(28, 28));
        badge.setMaximumSize(new Dimension(28, 28));

        JLabel iconLabel = new JLabel("", SwingConstants.CENTER);
        ImageIcon icon = loadIcon(iconFile, size, size);
        if (icon != null) {
            iconLabel.setIcon(icon);
        } else {
            iconLabel.setText("\u25cb");
            iconLabel.setForeground(fallbackColor);
            iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        }
        badge.add(iconLabel, BorderLayout.CENTER);
        return badge;
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? "-" : value.format(DATE_TIME_FORMATTER);
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
                Image scaledImage = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }
        } catch (Exception e) {
            // Ignore icon loading failures.
        }
        return null;
    }

    private static class RoundedBlockPanel extends JPanel {
        private final int arc;
        private final Color fillColor;
        private final Color borderColor;
        private final float borderWidth;
        private final Color shadowColor;
        private final int shadowSize;

        RoundedBlockPanel(int arc, Color fillColor, Color borderColor, float borderWidth, Color shadowColor, int shadowSize) {
            this.arc = arc;
            this.fillColor = fillColor;
            this.borderColor = borderColor;
            this.borderWidth = borderWidth;
            this.shadowColor = shadowColor;
            this.shadowSize = shadowSize;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();

            if (shadowSize > 0) {
                g2.setColor(shadowColor);
                g2.fillRoundRect(0, shadowSize, width - 1, height - shadowSize - 1, arc, arc);
            }

            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, width - 1, height - Math.max(shadowSize, 1), arc, arc);

            if (borderColor != null && borderWidth > 0f) {
                g2.setColor(borderColor);
                g2.setStroke(new BasicStroke(borderWidth));
                g2.drawRoundRect(0, 0, width - 1, height - Math.max(shadowSize, 1), arc, arc);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private class InputField extends JPanel {
        private final PlaceholderTextField textField;

        InputField(String iconFile, String placeholder) {
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(0, 12, 0, 12)
            ));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            setPreferredSize(new Dimension(0, 42));
            setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel iconLabel = new JLabel();
            ImageIcon icon = loadIcon(iconFile, 15, 15);
            if (icon != null) {
                iconLabel.setIcon(icon);
            } else {
                iconLabel.setText("\u25cb");
                iconLabel.setForeground(new Color(148, 163, 184));
            }
            iconLabel.setBorder(new EmptyBorder(0, 0, 0, 8));
            add(iconLabel, BorderLayout.WEST);

            textField = new PlaceholderTextField(placeholder);
            textField.setBorder(null);
            textField.setOpaque(false);
            textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            textField.setForeground(new Color(15, 23, 42));
            add(textField, BorderLayout.CENTER);

            textField.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(191, 219, 254), 2),
                        new EmptyBorder(0, 11, 0, 11)
                    ));
                }

                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(226, 232, 240)),
                        new EmptyBorder(0, 12, 0, 12)
                    ));
                }
            });
        }

        public String getValue() {
            return textField.getText();
        }

        public void setValue(String value) {
            textField.setText(value);
        }
    }

    private static class PlaceholderTextField extends JTextField {
        private final String placeholder;

        PlaceholderTextField(String placeholder) {
            this.placeholder = placeholder;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!getText().isEmpty() || isFocusOwner()) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(148, 163, 184));
            g2.setFont(getFont());
            Insets insets = getInsets();
            int y = (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
            g2.drawString(placeholder, insets.left, y);
            g2.dispose();
        }
    }
}
