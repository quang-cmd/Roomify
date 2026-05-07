package kqlhotel.gui.tabs;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import kqlhotel.bus.swaproom.SwapRoomBUS;
import kqlhotel.entity.SwapRoomOption;
import kqlhotel.entity.SwapRoomSearchResult;

public class SwapRoomPanel extends JPanel {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color PAGE_BG = new Color(245, 248, 252);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color SURFACE = Color.WHITE;
    private static final Color SURFACE_SOFT = new Color(248, 250, 252);
    private static final Color ACTION = new Color(37, 99, 235);

    private final SwapRoomBUS bus = new SwapRoomBUS();

    private final InputField inpMaDatPhong = new InputField("search.png", "VD: DP001");
    private final InputField inpTenKhach = new InputField("client.png", "VD: Nguyễn Văn A");
    private final InputField inpSoDienThoai = new InputField("telephone.png", "VD: 0987654321");
    private final InputField inpSoPhong = new InputField("room.png", "VD: 101");

    private final JPanel resultListPanel = new JPanel();
    private final JPanel detailPanel = new JPanel(new BorderLayout());
    private final JLabel resultCountLabel = new JLabel("Chưa có kết quả");

    private List<SwapRoomSearchResult> currentResults = new ArrayList<>();
    private SwapRoomSearchResult selectedResult;
    private SwapRoomOption selectedRoomOption;

    public SwapRoomPanel() {
        setOpaque(false);
        setBackground(PAGE_BG);
        setLayout(new BorderLayout(0, 20));
        setBorder(new EmptyBorder(22, 24, 22, 24));

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        showEmptyState();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(createProcessBadge(), BorderLayout.EAST);
        return header;
    }

    private JPanel createProcessBadge() {
        RoundedBlockPanel wrapper = new RoundedBlockPanel(18, SURFACE, BORDER, 1f, new Color(15, 23, 42, 8), 4);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
        wrapper.setBorder(new EmptyBorder(12, 16, 12, 16));

        wrapper.add(createStepChip("1", "Tìm đặt phòng", true));
        wrapper.add(Box.createHorizontalStrut(10));
        wrapper.add(createDivider());
        wrapper.add(Box.createHorizontalStrut(10));
        wrapper.add(createStepChip("2", "Chọn phòng mới", selectedResult != null));
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
                g2.setColor(active ? new Color(17, 24, 39) : new Color(241, 245, 249));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        circle.setOpaque(false);
        circle.setPreferredSize(new Dimension(28, 28));
        circle.setMaximumSize(new Dimension(28, 28));

        JLabel numberLabel = new JLabel(number, SwingConstants.CENTER);
        numberLabel.setForeground(active ? Color.WHITE : TEXT_MUTED);
        numberLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        circle.add(numberLabel, BorderLayout.CENTER);

        JLabel textLabel = new JLabel(text);
        textLabel.setForeground(active ? TEXT_PRIMARY : TEXT_MUTED);
        textLabel.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 13));

        chip.add(circle);
        chip.add(Box.createHorizontalStrut(8));
        chip.add(textLabel);
        return chip;
    }

    private Component createDivider() {
        JPanel divider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BORDER);
                g2.fillRoundRect(0, getHeight() / 2 - 1, getWidth(), 2, 2, 2);
                g2.dispose();
            }
        };
        divider.setOpaque(false);
        divider.setPreferredSize(new Dimension(24, 10));
        return divider;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(22, 0));
        content.setOpaque(false);

        JPanel leftColumn = new JPanel();
        leftColumn.setOpaque(false);
        leftColumn.setPreferredSize(new Dimension(460, 0));
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));

        leftColumn.add(createSearchCard());
        leftColumn.add(Box.createVerticalStrut(18));
        leftColumn.add(createResultsCard());

        detailPanel.setOpaque(false);

        content.add(leftColumn, BorderLayout.WEST);
        content.add(detailPanel, BorderLayout.CENTER);
        return content;
    }

    private JPanel createSearchCard() {
        RoundedBlockPanel card = new RoundedBlockPanel(22, SURFACE, BORDER, 1f, new Color(15, 23, 42, 10), 4);
        card.setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(new EmptyBorder(12, 20, 8, 20));

        JLabel title = new JLabel("Tìm đặt phòng đang hoạt động");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Nhập thông tin để tìm khách đang lưu trú.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        top.add(title);
        top.add(Box.createVerticalStrut(6));
        top.add(sub);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(4, 20, 16, 20));

        form.add(createFieldGroup("Mã đặt phòng", inpMaDatPhong));
        form.add(Box.createVerticalStrut(6));
        form.add(createFieldGroup("Tên khách", inpTenKhach));
        form.add(Box.createVerticalStrut(6));
        form.add(createFieldGroup("Số điện thoại", inpSoDienThoai));
        form.add(Box.createVerticalStrut(6));
        form.add(createFieldGroup("Số phòng", inpSoPhong));
        form.add(Box.createVerticalStrut(12));

        JPanel buttonRow = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonRow.setOpaque(false);
        buttonRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        buttonRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton searchButton = createPrimaryButton("Tìm kiếm", "search.png", new Color(17, 24, 39));
        searchButton.addActionListener(e -> performSearch());

        JButton resetButton = createSecondaryButton("Xóa bộ lọc");
        resetButton.addActionListener(e -> resetSearch());

        buttonRow.add(searchButton);
        buttonRow.add(resetButton);
        form.add(buttonRow);

        card.add(top, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel createResultsCard() {
        RoundedBlockPanel card = new RoundedBlockPanel(22, SURFACE, BORDER, 1f, new Color(15, 23, 42, 10), 4);
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(460, 0));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(16, 18, 12, 18));

        JPanel titleWrap = new JPanel();
        titleWrap.setOpaque(false);
        titleWrap.setLayout(new BoxLayout(titleWrap, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Kết quả tìm kiếm");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_PRIMARY);

        resultCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resultCountLabel.setForeground(TEXT_MUTED);

        titleWrap.add(title);
        titleWrap.add(Box.createVerticalStrut(4));
        titleWrap.add(resultCountLabel);

        header.add(titleWrap, BorderLayout.WEST);

        resultListPanel.setOpaque(false);
        resultListPanel.setLayout(new BoxLayout(resultListPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(resultListPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setBackground(SURFACE);
        scrollPane.setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        card.add(header, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel createFieldGroup(String labelText, InputField field) {
        JPanel group = new JPanel();
        group.setOpaque(false);
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(71, 85, 105));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        group.add(label);
        group.add(Box.createVerticalStrut(4));
        group.add(field);
        return group;
    }

    private JButton createPrimaryButton(String text, String iconFile, Color bg) {
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
        button.setBackground(bg);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(226, 232, 240));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(71, 85, 105));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return button;
    }

    private void performSearch() {
        String maDatPhong = inpMaDatPhong.getValue().trim();
        String tenKhach = inpTenKhach.getValue().trim();
        String soDienThoai = inpSoDienThoai.getValue().trim();
        String soPhong = inpSoPhong.getValue().trim();

        if (maDatPhong.isEmpty() && tenKhach.isEmpty() && soDienThoai.isEmpty() && soPhong.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập ít nhất một tiêu chí tìm kiếm.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentResults = bus.searchBookings(maDatPhong, tenKhach, soDienThoai, soPhong);
        selectedRoomOption = null;

        if (currentResults.isEmpty()) {
            selectedResult = null;
            updateResultList();
            showEmptyState();
            JOptionPane.showMessageDialog(this, "Không tìm thấy đặt phòng phù hợp.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        selectedResult = currentResults.get(0);
        updateResultList();
        showSelectedResult();
    }

    private void resetSearch() {
        inpMaDatPhong.setValue("");
        inpTenKhach.setValue("");
        inpSoDienThoai.setValue("");
        inpSoPhong.setValue("");
        currentResults = new ArrayList<>();
        selectedResult = null;
        selectedRoomOption = null;
        updateResultList();
        showEmptyState();
    }

    private void updateResultList() {
        resultListPanel.removeAll();

        if (currentResults.isEmpty()) {
            resultCountLabel.setText("Không tìm thấy kết quả");
            resultListPanel.add(createEmptyListMessage());
        } else {
            resultCountLabel.setText("Tìm thấy " + currentResults.size() + " đặt phòng");
            for (SwapRoomSearchResult result : currentResults) {
                resultListPanel.add(createBookingCard(result));
                resultListPanel.add(Box.createVerticalStrut(10));
            }
        }

        resultListPanel.revalidate();
        resultListPanel.repaint();
    }

    private JPanel createEmptyListMessage() {
        RoundedBlockPanel panel = new RoundedBlockPanel(16, SURFACE_SOFT, BORDER, 1f, new Color(15, 23, 42, 0), 0);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 16, 20, 16));

        JLabel label = new JLabel("<html><div style='text-align:center;'>Nhập thông tin tìm kiếm bên trái để hiển thị đặt phòng.</div></html>", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXT_MUTED);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBookingCard(SwapRoomSearchResult result) {
        boolean active = selectedResult != null && selectedResult.getBookingDetailId().equals(result.getBookingDetailId());
        Color bg = active ? new Color(239, 246, 255) : SURFACE_SOFT;
        Color borderColor = active ? new Color(96, 165, 250) : BORDER;

        RoundedBlockPanel card = new RoundedBlockPanel(18, bg, borderColor, 1.2f, new Color(15, 23, 42, 0), 0);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel guestLabel = new JLabel(result.getCustomerName());
        guestLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        guestLabel.setForeground(TEXT_PRIMARY);

        top.add(guestLabel, BorderLayout.WEST);
        top.add(createTag(active ? "Đã chọn" : result.getBookingId(), active ? new Color(219, 234, 254) : new Color(241, 245, 249), active ? ACTION : TEXT_MUTED), BorderLayout.EAST);

        JLabel info = new JLabel(result.getCurrentRoomId() + "  •  " + result.getCurrentRoomTypeName() + "  •  " + result.getOccupantCount() + " khách");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        info.setForeground(TEXT_MUTED);

        JLabel date = new JLabel(formatDateTime(result.getCheckInDate()) + " -> " + formatDateTime(result.getCheckOutDate()));
        date.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        date.setForeground(TEXT_MUTED);

        JPanel meta = new JPanel();
        meta.setOpaque(false);
        meta.setLayout(new BoxLayout(meta, BoxLayout.Y_AXIS));
        meta.add(info);
        meta.add(Box.createVerticalStrut(4));
        meta.add(date);

        card.add(top, BorderLayout.NORTH);
        card.add(meta, BorderLayout.CENTER);

        MouseAdapter selectListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedResult = result;
                selectedRoomOption = null;
                updateResultList();
                showSelectedResult();
            }
        };
        card.addMouseListener(selectListener);
        top.addMouseListener(selectListener);
        meta.addMouseListener(selectListener);

        return card;
    }

    private JLabel createTag(String text, Color bg, Color fg) {
        JLabel tag = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tag.setOpaque(false);
        tag.setForeground(fg);
        tag.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tag.setBorder(new EmptyBorder(4, 10, 4, 10));
        return tag;
    }

    private void showSelectedResult() {
        if (selectedResult == null) {
            showEmptyState();
            return;
        }

        detailPanel.removeAll();

        RoundedBlockPanel card = new RoundedBlockPanel(24, SURFACE, BORDER, 1f, new Color(15, 23, 42, 10), 4);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(22, 22, 22, 22));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Chi tiết đổi phòng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);

        JLabel sub = new JLabel("Xác nhận thông tin đặt phòng, sau đó chọn phòng trống phù hợp.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);

        content.add(title);
        content.add(Box.createVerticalStrut(6));
        content.add(sub);
        content.add(Box.createVerticalStrut(18));
        content.add(createBookingSummary(selectedResult));
        content.add(Box.createVerticalStrut(18));
        content.add(createAvailableRoomsSection(selectedResult));

        card.add(content, BorderLayout.NORTH);
        detailPanel.add(card, BorderLayout.CENTER);
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private JPanel createBookingSummary(SwapRoomSearchResult result) {
        RoundedBlockPanel wrap = new RoundedBlockPanel(18, SURFACE_SOFT, BORDER, 1f, new Color(15, 23, 42, 0), 0);
        wrap.setLayout(new GridLayout(0, 2, 12, 12));
        wrap.setBorder(new EmptyBorder(16, 16, 16, 16));

        wrap.add(createInfoItem("Mã đặt phòng", result.getBookingId()));
        wrap.add(createInfoItem("Tên khách", result.getCustomerName()));
        wrap.add(createInfoItem("Số điện thoại", result.getPhoneNumber()));
        wrap.add(createInfoItem("CCCD", result.getIdCard()));
        wrap.add(createInfoItem("Phòng hiện tại", result.getCurrentRoomId()));
        wrap.add(createInfoItem("Loại phòng", result.getCurrentRoomTypeName()));
        wrap.add(createInfoItem("Ngày nhận", formatDateTime(result.getCheckInDate())));
        wrap.add(createInfoItem("Ngày trả", formatDateTime(result.getCheckOutDate())));

        return wrap;
    }

    private JPanel createInfoItem(String labelText, String value) {
        JPanel item = new JPanel();
        item.setOpaque(false);
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(TEXT_MUTED);

        JLabel content = new JLabel(safeText(value));
        content.setFont(new Font("Segoe UI", Font.BOLD, 14));
        content.setForeground(TEXT_PRIMARY);

        item.add(label);
        item.add(Box.createVerticalStrut(4));
        item.add(content);
        return item;
    }

    private JPanel createAvailableRoomsSection(SwapRoomSearchResult result) {
        RoundedBlockPanel wrap = new RoundedBlockPanel(20, SURFACE_SOFT, BORDER, 1f, new Color(15, 23, 42, 0), 0);
        wrap.setLayout(new BorderLayout(0, 14));
        wrap.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleWrap = new JPanel();
        titleWrap.setOpaque(false);
        titleWrap.setLayout(new BoxLayout(titleWrap, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Phòng trống có sẵn");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(TEXT_PRIMARY);

        JLabel sub = new JLabel("Danh sách phòng trống phù hợp với loại và sức chứa.");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(TEXT_MUTED);

        titleWrap.add(title);
        titleWrap.add(Box.createVerticalStrut(4));
        titleWrap.add(sub);

        header.add(titleWrap, BorderLayout.WEST);

        JPanel roomList = new JPanel();
        roomList.setOpaque(false);
        roomList.setLayout(new BoxLayout(roomList, BoxLayout.Y_AXIS));

        List<SwapRoomOption> availableRooms = bus.getAvailableRooms(result);
        if (availableRooms.isEmpty()) {
            roomList.add(createNoRoomState());
        } else {
            for (SwapRoomOption room : availableRooms) {
                roomList.add(createRoomOptionCard(result, room));
                roomList.add(Box.createVerticalStrut(10));
            }
        }

        JScrollPane scrollPane = new JScrollPane(roomList);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getViewport().setBackground(SURFACE_SOFT);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(0, 320));

        wrap.add(header, BorderLayout.NORTH);
        wrap.add(scrollPane, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel createNoRoomState() {
        RoundedBlockPanel panel = new RoundedBlockPanel(16, SURFACE, BORDER, 1f, new Color(15, 23, 42, 0), 0);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new EmptyBorder(22, 16, 22, 16));

        JLabel label = new JLabel("Không có phòng trống phù hợp.");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(new Color(185, 28, 28));
        panel.add(label);
        return panel;
    }

    private JPanel createRoomOptionCard(SwapRoomSearchResult result, SwapRoomOption room) {
        boolean selected = selectedRoomOption != null && selectedRoomOption.getRoomId().equals(room.getRoomId());
        boolean sameType = safeText(room.getRoomTypeId()).equalsIgnoreCase(safeText(result.getCurrentRoomTypeId()));

        RoundedBlockPanel card = new RoundedBlockPanel(
            18,
            selected ? new Color(239, 246, 255) : SURFACE,
            selected ? new Color(96, 165, 250) : BORDER,
            1.2f,
            new Color(15, 23, 42, 0),
            0
        );
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JPanel roomTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        roomTop.setOpaque(false);

        JLabel roomLabel = new JLabel(room.getRoomId());
        roomLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        roomLabel.setForeground(TEXT_PRIMARY);

        roomTop.add(roomLabel);
        roomTop.add(Box.createHorizontalStrut(10));
        roomTop.add(createTag(sameType ? "Cùng loại" : "Khác loại", sameType ? new Color(220, 252, 231) : new Color(254, 249, 195), sameType ? new Color(22, 101, 52) : new Color(133, 77, 14)));

        JLabel detail = new JLabel(room.getRoomTypeName() + "  •  Tầng " + room.getFloor() + "  •  " + room.getMaxCapacity() + " khách");
        detail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        detail.setForeground(TEXT_MUTED);

        JLabel note = new JLabel(sameType ? "Phù hợp nhất với loại phòng hiện tại." : "Đủ sức chứa, nhưng khác loại phòng.");
        note.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        note.setForeground(TEXT_MUTED);

        left.add(roomTop);
        left.add(Box.createVerticalStrut(8));
        left.add(detail);
        left.add(Box.createVerticalStrut(4));
        left.add(note);

        JPanel action = new JPanel();
        action.setOpaque(false);
        action.setLayout(new BoxLayout(action, BoxLayout.Y_AXIS));

        JButton selectButton = createSecondaryButton(selected ? "Đã chọn" : "Chọn phòng");
        selectButton.setEnabled(!selected);
        selectButton.addActionListener(e -> {
            selectedRoomOption = room;
            showSelectedResult();
        });

        JButton confirmButton = createPrimaryButton("Xác nhận đổi", "swap-room.png", ACTION);
        confirmButton.addActionListener(e -> confirmChangeRoom(result, room));

        action.add(selectButton);
        action.add(Box.createVerticalStrut(8));
        action.add(confirmButton);

        card.add(left, BorderLayout.CENTER);
        card.add(action, BorderLayout.EAST);
        return card;
    }

    private void confirmChangeRoom(SwapRoomSearchResult result, SwapRoomOption room) {
        int confirmed = JOptionPane.showConfirmDialog(
            this,
            "Đổi phòng từ " + result.getCurrentRoomId() + " sang " + room.getRoomId() + "?",
            "Xác nhận đổi phòng",
            JOptionPane.YES_NO_OPTION
        );
        if (confirmed != JOptionPane.YES_OPTION) {
            return;
        }

        boolean success = bus.changeRoom(result.getBookingId(), result.getCurrentRoomId(), room.getRoomId());
        if (!success) {
            JOptionPane.showMessageDialog(this, "Không thể đổi phòng. Vui lòng kiểm tra cơ sở dữ liệu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Đổi phòng thành công sang " + room.getRoomId() + ".", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        inpMaDatPhong.setValue(result.getBookingId());
        inpSoPhong.setValue(room.getRoomId());
        performSearch();
    }

    private void showEmptyState() {
        detailPanel.removeAll();

        JPanel emptyWrap = new JPanel(new GridBagLayout());
        emptyWrap.setOpaque(false);

        RoundedBlockPanel emptyCard = new RoundedBlockPanel(24, SURFACE, BORDER, 1f, new Color(15, 23, 42, 10), 4);
        emptyCard.setLayout(new BoxLayout(emptyCard, BoxLayout.Y_AXIS));
        emptyCard.setBorder(new EmptyBorder(34, 24, 34, 24));
        emptyCard.setPreferredSize(new Dimension(0, 360));

        JPanel iconBox = createLargeStateIcon();
        iconBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Sẵn sàng đổi phòng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("<html><div style='text-align:center;'>Tìm đặt phòng đang hoạt động ở cột bên trái.<br>Sau khi chọn, phòng trống phù hợp sẽ hiển thị ở đây.</div></html>");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyCard.add(iconBox);
        emptyCard.add(Box.createVerticalStrut(20));
        emptyCard.add(title);
        emptyCard.add(Box.createVerticalStrut(10));
        emptyCard.add(subtitle);

        emptyWrap.add(emptyCard);
        detailPanel.add(emptyWrap, BorderLayout.CENTER);
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private JPanel createLargeStateIcon() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(239, 246, 255));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(84, 84));
        panel.setMaximumSize(new Dimension(84, 84));

        JLabel icon = new JLabel("", SwingConstants.CENTER);
        ImageIcon swapIcon = loadIcon("swap-room.png", 38, 38);
        if (swapIcon != null) {
            icon.setIcon(swapIcon);
        } else {
            icon.setText("\u21c4");
            icon.setForeground(new Color(96, 165, 250));
            icon.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        }
        panel.add(icon, BorderLayout.CENTER);
        return panel;
    }

    private String safeText(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value;
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
                return new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
            }
        } catch (Exception e) {}
        return null;
    }

    private static class InputField extends JPanel {
        private final JTextField field = new JTextField();

        InputField(String iconFile, String placeholder) {
            setOpaque(false);
            setLayout(new BorderLayout(10, 0));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(8, 12, 8, 12)
            ));
            setBackground(Color.WHITE);

            JLabel icon = new JLabel();
            try {
                URL resource = getClass().getResource("/kqlhotel/resources/icons/" + iconFile);
                if (resource == null) resource = new java.io.File("src/kqlhotel/resources/icons/" + iconFile).toURI().toURL();
                if (resource != null) icon.setIcon(new ImageIcon(new ImageIcon(resource).getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH)));
            } catch (Exception e) {}
            
            field.setBorder(null);
            field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            field.setForeground(new Color(15, 23, 42));
            field.setToolTipText(placeholder);

            add(icon, BorderLayout.WEST);
            add(field, BorderLayout.CENTER);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        }

        public String getValue() { return field.getText(); }
        public void setValue(String val) { field.setText(val); }
    }

    private static class RoundedBlockPanel extends JPanel {
        private final int arc;
        private final Color fill;
        private final Color border;
        private final float borderWidth;
        private final Color shadow;
        private final int shadowSize;

        RoundedBlockPanel(int arc, Color fill, Color border, float borderWidth, Color shadow, int shadowSize) {
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
        }
    }
}
