package kqlhotel.gui.tabs;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import kqlhotel.bus.customer.CustomerBUS;
import kqlhotel.entity.CustomerBookingHistory;
import kqlhotel.entity.Customer;

public class CustomersPanel extends JPanel {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final DateTimeFormatter LDT_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("#,##0");

    private kqlhotel.gui.AppFrame appFrame;
    private final CustomerBUS bus = new CustomerBUS();
    private final JTextField searchField = new JTextField();
    private final JPanel customerListPanel = new JPanel();
    private final JPanel detailPanel = new JPanel(new BorderLayout());
    private final JLabel countLabel = new JLabel();
    private final JComboBox<String> rankFilter = new JComboBox<>(new String[]{"Tất cả hạng", "Đồng", "Bạc", "Vàng", "Kim cương"});
    private List<Customer> customers = new ArrayList<>();
    private Customer selectedCustomer;

    public CustomersPanel(kqlhotel.gui.AppFrame appFrame) {
        this.appFrame = appFrame;
        setOpaque(false);
        setLayout(new BorderLayout(0, 18));
        setBorder(new EmptyBorder(22, 26, 22, 26));

        add(createHeader(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        loadCustomers();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleWrap = new JPanel();
        titleWrap.setOpaque(false);
        titleWrap.setLayout(new BoxLayout(titleWrap, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Quản lý khách hàng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(15, 23, 42));

        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        countLabel.setForeground(new Color(100, 116, 139));

        titleWrap.add(countLabel);

        JButton addButton = createPrimaryButton("Thêm khách hàng", "client.png");
        addButton.addActionListener(e -> showCustomerDialog(null));

        header.add(titleWrap, BorderLayout.WEST);
        header.add(addButton, BorderLayout.EAST);
        return header;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(18, 0));
        content.setOpaque(false);

        JPanel leftPane = new RoundedPanel(20, Color.WHITE, new Color(226, 232, 240), 1f, new Color(15, 23, 42, 10), 4);
        leftPane.setLayout(new BorderLayout(0, 12));
        leftPane.setPreferredSize(new Dimension(360, 0));
        leftPane.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel searchPanel = new JPanel(new BorderLayout(0, 10));
        searchPanel.setOpaque(false);
        searchPanel.add(createSearchBox(), BorderLayout.CENTER);

        rankFilter.setPreferredSize(new Dimension(100, 36));
        rankFilter.addActionListener(e -> renderCustomerList(filterCustomers(searchField.getText().trim())));
        searchPanel.add(rankFilter, BorderLayout.SOUTH);

        leftPane.add(searchPanel, BorderLayout.NORTH);

        customerListPanel.setOpaque(false);
        customerListPanel.setLayout(new BoxLayout(customerListPanel, BoxLayout.Y_AXIS));

        JScrollPane listScroll = new JScrollPane(customerListPanel);
        listScroll.setOpaque(false);
        listScroll.getViewport().setOpaque(false);
        listScroll.setBorder(null);
        listScroll.getVerticalScrollBar().setUnitIncrement(16);
        leftPane.add(listScroll, BorderLayout.CENTER);

        detailPanel.setOpaque(false);

        content.add(leftPane, BorderLayout.WEST);
        content.add(detailPanel, BorderLayout.CENTER);
        return content;
    }

    private JPanel createSearchBox() {
        JPanel wrapper = new RoundedPanel(16, new Color(248, 250, 252), new Color(226, 232, 240), 1f, new Color(15, 23, 42, 0), 0);
        wrapper.setLayout(new BorderLayout());
        wrapper.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel icon = new JLabel();
        ImageIcon searchIcon = loadIcon("search.png", 16, 16);
        if (searchIcon != null) {
            icon.setIcon(searchIcon);
        } else {
            icon.setText("?");
        }
        icon.setBorder(new EmptyBorder(0, 0, 0, 8));

        searchField.setBorder(null);
        searchField.setOpaque(false);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setToolTipText("Search by name, phone or ID...");
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                renderCustomerList(filterCustomers(searchField.getText().trim()));
            }
        });

        wrapper.add(icon, BorderLayout.WEST);
        wrapper.add(searchField, BorderLayout.CENTER);
        return wrapper;
    }

    private void loadCustomers() {
        customers = bus.getAllWithStats();
        updateCounts();
        renderCustomerList(customers);
        if (!customers.isEmpty()) {
            showCustomerDetail(customers.get(0));
        } else {
            showEmptyDetail();
        }
    }

    private void updateCounts() {
        long activeCount = customers.stream().filter(Customer::isDangHoatDong).count();
        countLabel.setText(customers.size() + " khách hàng · " + activeCount + " đang hoạt động");
    }

    private List<Customer> filterCustomers(String keyword) {
        String selectedRank = (String) rankFilter.getSelectedItem();
        List<Customer> filtered = new ArrayList<>();
        String loweredKeyword = keyword.toLowerCase().trim();

        for (Customer customer : customers) {
            // Check rank
            boolean matchesRank = "Tất cả hạng".equals(selectedRank) || mapRank(customer.getHangKH()).equals(selectedRank);

            // Check keyword
            boolean matchesKeyword = loweredKeyword.isEmpty()
                    || safe(customer.getHoTenKH()).toLowerCase().contains(loweredKeyword)
                    || safe(customer.getSdt()).contains(loweredKeyword)
                    || safe(customer.getMaKH()).toLowerCase().contains(loweredKeyword);

            if (matchesRank && matchesKeyword) {
                filtered.add(customer);
            }
        }
        return filtered;
    }

    private void renderCustomerList(List<Customer> data) {
        customerListPanel.removeAll();
        for (Customer customer : data) {
            customerListPanel.add(createCustomerRow(customer));
            customerListPanel.add(Box.createVerticalStrut(10));
        }
        customerListPanel.revalidate();
        customerListPanel.repaint();
    }

    private JPanel createCustomerRow(Customer customer) {
        boolean selected = selectedCustomer != null && safe(selectedCustomer.getMaKH()).equals(customer.getMaKH());
        JPanel row = new RoundedPanel(18, selected ? new Color(239, 246, 255) : Color.WHITE, new Color(226, 232, 240), 1f, new Color(15, 23, 42, 4), 2);
        row.setLayout(new BorderLayout(12, 0));
        row.setBorder(new EmptyBorder(14, 14, 14, 14));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel avatar = createAvatar(customer.getHoTenKH(), selected ? new Color(16, 185, 129) : pickAvatarColor(customer.getMaKH()));
        row.add(avatar, BorderLayout.WEST);

        JPanel textWrap = new JPanel(new GridLayout(2, 1, 0, 2));
        textWrap.setOpaque(false);

        JLabel name = new JLabel(customer.getHoTenKH());
        name.setFont(new Font("Segoe UI", Font.BOLD, 15));
        name.setForeground(new Color(15, 23, 42));

        JLabel phone = new JLabel(safe(customer.getSdt()));
        phone.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        phone.setForeground(new Color(148, 163, 184));

        textWrap.add(name);
        JPanel subText = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        subText.setOpaque(false);
        subText.add(phone);
        subText.add(new JLabel("•"));
        subText.add(createRankBadge(customer.getHangKH()));
        textWrap.add(subText);
        row.add(textWrap, BorderLayout.CENTER);

        JPanel statusWrap = new JPanel();
        statusWrap.setOpaque(false);
        statusWrap.setLayout(new BoxLayout(statusWrap, BoxLayout.Y_AXIS));

        JLabel badge = createStatusBadge(customer.isDangHoatDong() ? "Hoạt động" : "Không hoạt động", customer.isDangHoatDong());
        badge.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel arrow = new JLabel("›");
        arrow.setFont(new Font("Segoe UI", Font.BOLD, 18));
        arrow.setForeground(new Color(148, 163, 184));
        arrow.setAlignmentX(Component.RIGHT_ALIGNMENT);

        statusWrap.add(badge);
        statusWrap.add(Box.createVerticalGlue());
        statusWrap.add(arrow);
        row.add(statusWrap, BorderLayout.EAST);

        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showCustomerDetail(customer);
                renderCustomerList(filterCustomers(searchField.getText().trim()));
            }
        });
        return row;
    }

    private void showCustomerDetail(Customer customer) {
        selectedCustomer = customer;
        detailPanel.removeAll();

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        RoundedPanel profileCard = new RoundedPanel(24, Color.WHITE, new Color(226, 232, 240), 1f, new Color(15, 23, 42, 10), 4);
        profileCard.setLayout(new BoxLayout(profileCard, BoxLayout.Y_AXIS));
        profileCard.setBorder(new EmptyBorder(20, 20, 20, 20));

        profileCard.add(createProfileHeader(customer));
        profileCard.add(Box.createVerticalStrut(18));
        profileCard.add(createStatsRow(customer));
        profileCard.add(Box.createVerticalStrut(18));
        profileCard.add(createInfoGrid(customer));

        RoundedPanel historyCard = new RoundedPanel(24, Color.WHITE, new Color(226, 232, 240), 1f, new Color(15, 23, 42, 10), 4);
        historyCard.setLayout(new BoxLayout(historyCard, BoxLayout.Y_AXIS));
        historyCard.setBorder(new EmptyBorder(20, 20, 20, 20));
        historyCard.add(createHistorySection(customer));

        content.add(profileCard);
        content.add(Box.createVerticalStrut(16));
        content.add(historyCard);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        detailPanel.add(scrollPane, BorderLayout.CENTER);
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private JPanel createProfileHeader(Customer customer) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel left = new JPanel(new BorderLayout(14, 0));
        left.setOpaque(false);

        JLabel avatar = createAvatar(customer.getHoTenKH(), new Color(16, 185, 129));
        avatar.setPreferredSize(new Dimension(52, 52));
        avatar.setMinimumSize(new Dimension(52, 52));
        avatar.setMaximumSize(new Dimension(52, 52));

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 6));
        text.setOpaque(false);

        JLabel name = new JLabel(customer.getHoTenKH());
        name.setFont(new Font("Segoe UI", Font.BOLD, 18));
        name.setForeground(new Color(15, 23, 42));

        JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        meta.setOpaque(false);
        meta.add(createRankBadge(customer.getHangKH()));
        meta.add(Box.createHorizontalStrut(8));
        
        JLabel pointsLabel = new JLabel(customer.getDiemTichLuy() + " điểm");
        pointsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pointsLabel.setForeground(new Color(245, 158, 11)); // Amber color
        meta.add(pointsLabel);
        meta.add(Box.createHorizontalStrut(8));

        meta.add(createStatusBadge(customer.isDangHoatDong() ? "Hoạt động" : "Không hoạt động", customer.isDangHoatDong()));
        meta.add(Box.createHorizontalStrut(8));
        meta.add(createMutedLabel("Lần cuối: " + formatDate(customer.getNgayDatGanNhatDate())));

        text.add(name);
        text.add(meta);

        left.add(avatar, BorderLayout.WEST);
        left.add(text, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton editButton = createPrimaryButton("Sửa thông tin", "edit.png");
        editButton.addActionListener(e -> showCustomerDialog(customer));

        JButton bookingButton = createOutlineButton("Đặt phòng mới");
        bookingButton.addActionListener(e -> {
            if (selectedCustomer != null) {
                appFrame.getBookingPanel().preFillCustomer(selectedCustomer);
                appFrame.navigateTo("booking");
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng trước.");
            }
        });

        actions.add(editButton);
        actions.add(bookingButton);

        header.add(left, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        return header;
    }

    private JPanel createStatsRow(Customer customer) {
        JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
        row.setOpaque(false);
        row.add(createStatCard("Tổng đặt phòng", String.valueOf(customer.getTongDatPhong())));
        row.add(createStatCard("Tổng chi tiêu", MONEY_FORMAT.format(customer.getTongChiTieu()) + "đ"));
        row.add(createStatCard("Đặt phòng gần nhất", customer.getNgayDatGanNhat() != null ? customer.getNgayDatGanNhat().format(LDT_FORMAT) : "-"));
        return row;
    }

    private JPanel createStatCard(String labelText, String value) {
        JPanel card = new RoundedPanel(18, new Color(248, 250, 252), new Color(241, 245, 249), 1f, new Color(15, 23, 42, 0), 0);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 14, 16, 14));

        JLabel label = new JLabel(labelText, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(new Color(148, 163, 184));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(new Color(15, 23, 42));
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(label);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        return card;
    }

    private JPanel createInfoGrid(Customer customer) {
        JPanel grid = new JPanel(new GridLayout(3, 2, 14, 14));
        grid.setOpaque(false);
        grid.add(createInfoCard("Số điện thoại", safe(customer.getSdt()), "telephone.png"));
        grid.add(createInfoCard("Địa chỉ email", safe(customer.getEmail()), "email.png"));
        grid.add(createInfoCard("Địa chỉ cư trú", safe(customer.getDiaChi()), "location.png"));
        grid.add(createInfoCard("Ngày sinh", customer.getNgaySinh() != null ? customer.getNgaySinh().format(LDT_FORMAT) : "-", "calendar.png"));
        grid.add(createInfoCard("CCCD / Hộ chiếu", safe(customer.getCCCD()), "client.png"));
        grid.add(createInfoCard("Quốc tịch", safe(customer.getQuocTich()), "location.png"));
        return grid;
    }

    private JPanel createInfoCard(String title, String value, String iconFile) {
        JPanel card = new RoundedPanel(18, new Color(248, 250, 252), new Color(241, 245, 249), 1f, new Color(15, 23, 42, 0), 0);
        card.setLayout(new BorderLayout(10, 0));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel icon = new JLabel();
        ImageIcon imageIcon = loadIcon(iconFile, 16, 16);
        if (imageIcon != null) {
            icon.setIcon(imageIcon);
        } else {
            icon.setText("•");
        }

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 2));
        text.setOpaque(false);

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(148, 163, 184));

        JLabel content = new JLabel(value.isBlank() ? "-" : value);
        content.setFont(new Font("Segoe UI", Font.BOLD, 14));
        content.setForeground(new Color(15, 23, 42));

        text.add(label);
        text.add(content);

        card.add(icon, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private JPanel createHistorySection(Customer customer) {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Lịch sử đặt phòng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(15, 23, 42));
        section.add(title);
        section.add(Box.createVerticalStrut(14));

        List<CustomerBookingHistory> histories = bus.getBookingHistory(customer.getMaKH());
        if (histories.isEmpty()) {
            section.add(createMutedLabel("Khách hàng này chưa có lịch sử đặt phòng."));
            return section;
        }

        for (CustomerBookingHistory history : histories) {
            section.add(createHistoryRow(history));
            section.add(Box.createVerticalStrut(10));
        }
        return section;
    }

    private JPanel createHistoryRow(CustomerBookingHistory history) {
        JPanel row = new RoundedPanel(18, new Color(248, 250, 252), new Color(241, 245, 249), 1f, new Color(15, 23, 42, 0), 0);
        row.setLayout(new BorderLayout());
        row.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);

        JLabel booking = new JLabel("Đặt phòng " + safe(history.getMaDatPhong()) + " · Hóa đơn " + safe(history.getMaHoaDon()));
        booking.setFont(new Font("Segoe UI", Font.BOLD, 15));
        booking.setForeground(new Color(15, 23, 42));

        JLabel date = new JLabel(formatDate(history.getNgayLap()));
        date.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        date.setForeground(new Color(148, 163, 184));

        left.add(booking);
        left.add(date);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        JLabel amount = new JLabel(MONEY_FORMAT.format(history.getTongTien()) + "đ");
        amount.setFont(new Font("Segoe UI", Font.BOLD, 20));
        amount.setForeground(new Color(15, 23, 42));
        amount.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel status = createStatusBadge(mapInvoiceStatus(history.getTrangThai()), "DaThanhToan".equalsIgnoreCase(history.getTrangThai()));
        status.setAlignmentX(Component.RIGHT_ALIGNMENT);

        right.add(amount);
        right.add(Box.createVerticalStrut(6));
        right.add(status);

        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private void showCustomerDialog(Customer existing) {
        boolean editing = existing != null;
        JDialog dialog = new JDialog();
        dialog.setModal(true);
        dialog.setTitle(editing ? "Sửa khách hàng" : "Thêm khách hàng mới");
        dialog.setSize(420, 600);
        dialog.setLocationRelativeTo(this);

        JPanel fieldContainer = new JPanel();
        fieldContainer.setLayout(new BoxLayout(fieldContainer, BoxLayout.Y_AXIS));
        fieldContainer.setBorder(new EmptyBorder(18, 18, 18, 18));
        fieldContainer.setBackground(Color.WHITE);

        JTextField nameField = createDialogField(editing ? existing.getHoTenKH() : "");
        JTextField phoneField = createDialogField(editing ? existing.getSdt() : "");
        JTextField cccdField = createDialogField(editing ? existing.getCCCD() : "");
        JTextField emailField = createDialogField(editing ? safe(existing.getEmail()) : "");
        JTextField addressField = createDialogField(editing ? safe(existing.getDiaChi()) : "");
        JTextField nationalityField = createDialogField(editing ? safe(existing.getQuocTich()) : "Việt Nam");
        JTextField birthField = createDialogField(editing && existing.getNgaySinh() != null ? existing.getNgaySinh().format(LDT_FORMAT) : "01/01/1990");
        JTextField pointsField = createDialogField(editing ? String.valueOf(existing.getDiemTichLuy()) : "0");
        JComboBox<String> genderBox = new JComboBox<>(new String[]{"Nam", "Nữ"});
        genderBox.setSelectedItem(editing ? safe(existing.getGioiTinh()) : "Nam");
        JComboBox<String> rankBox = new JComboBox<>(new String[]{"Đồng", "Bạc", "Vàng", "Kim cương"});
        rankBox.setSelectedItem(mapRank(existing != null ? existing.getHangKH() : "Dong"));

        fieldContainer.add(createDialogFieldGroup("Họ và tên", nameField));
        fieldContainer.add(Box.createVerticalStrut(10));
        fieldContainer.add(createDialogFieldGroup("Số điện thoại", phoneField));
        fieldContainer.add(Box.createVerticalStrut(10));
        fieldContainer.add(createDialogFieldGroup("CCCD", cccdField));
        fieldContainer.add(Box.createVerticalStrut(10));
        fieldContainer.add(createDialogFieldGroup("Email", emailField));
        fieldContainer.add(Box.createVerticalStrut(10));
        fieldContainer.add(createDialogFieldGroup("Địa chỉ", addressField));
        fieldContainer.add(Box.createVerticalStrut(10));
        fieldContainer.add(createDialogFieldGroup("Quốc tịch", nationalityField));
        fieldContainer.add(Box.createVerticalStrut(10));
        fieldContainer.add(createDialogFieldGroup("Ngày sinh (dd/MM/yyyy)", birthField));
        fieldContainer.add(Box.createVerticalStrut(10));
        fieldContainer.add(createDialogFieldGroup("Giới tính", genderBox));
        fieldContainer.add(Box.createVerticalStrut(10));
        fieldContainer.add(createDialogFieldGroup("Hạng khách hàng", rankBox));
        fieldContainer.add(Box.createVerticalStrut(10));
        fieldContainer.add(createDialogFieldGroup("Điểm tích lũy", pointsField));

        JPanel actions = new JPanel(new BorderLayout(10, 0));
        actions.setOpaque(false);
        JButton cancelButton = createOutlineButton("Hủy");
        cancelButton.addActionListener(e -> dialog.dispose());
        JButton saveButton = createPrimaryButton(editing ? "Cập nhật" : "Lưu khách hàng", "check.png");
        saveButton.addActionListener(e -> {
            Customer payload = editing ? existing : new Customer();
            payload.setHoTenKH(nameField.getText().trim());
            payload.setSdt(phoneField.getText().trim());
            payload.setCCCD(cccdField.getText().trim());
            payload.setEmail(emailField.getText().trim());
            payload.setDiaChi(addressField.getText().trim());
            payload.setQuocTich(nationalityField.getText().trim());
            payload.setGioiTinh(String.valueOf(genderBox.getSelectedItem()));
            payload.setHangKH(mapRankToCode(String.valueOf(rankBox.getSelectedItem())));
            try {
                payload.setDiemTichLuy(Integer.parseInt(pointsField.getText().trim()));
            } catch (Exception ex) {
                payload.setDiemTichLuy(editing ? existing.getDiemTichLuy() : 0);
            }

            try {
                String birthStr = birthField.getText().trim();
                java.time.LocalDate ld = java.time.LocalDate.parse(birthStr, LDT_FORMAT);
                payload.setNgaySinh(ld.atStartOfDay());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Ngày sinh phải đúng định dạng dd/MM/yyyy.");
                return;
            }

            if (payload.getHoTenKH().isBlank() || payload.getSdt().isBlank() || payload.getCCCD().isBlank()) {
                JOptionPane.showMessageDialog(dialog, "Họ tên, số điện thoại và CCCD là bắt buộc.");
                return;
            }

            boolean success = editing ? bus.update(payload) : bus.insert(payload);
            if (success) {
                dialog.dispose();
                loadCustomers();
            } else {
                JOptionPane.showMessageDialog(dialog, "Không thể lưu khách hàng. Vui lòng kiểm tra dữ liệu.");
            }
        });

        actions.add(cancelButton, BorderLayout.WEST);
        actions.add(saveButton, BorderLayout.EAST);
        actions.setBorder(new EmptyBorder(12, 18, 18, 18));
        actions.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(fieldContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(actions, BorderLayout.SOUTH);

        dialog.setContentPane(mainPanel);
        dialog.setVisible(true);
    }

    private JPanel createDialogFieldGroup(String labelText, Component field) {
        JPanel group = new JPanel();
        group.setOpaque(false);
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(new Color(51, 65, 85));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (field instanceof JComponent) {
            ((JComponent) field).setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        group.add(label);
        group.add(Box.createVerticalStrut(6));
        group.add(field);
        return group;
    }

    private JTextField createDialogField(String value) {
        JTextField field = new JTextField(value);
        field.setMaximumSize(new Dimension(320, 36));
        field.setPreferredSize(new Dimension(320, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }

    private JButton createPrimaryButton(String text, String iconFile) {
        JButton button = new JButton(text);
        ImageIcon icon = loadIcon(iconFile, 14, 14);
        if (icon != null) {
            button.setIcon(icon);
            button.setIconTextGap(8);
        }
        button.setBackground(new Color(15, 23, 42));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 18, 12, 18));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return button;
    }

    private JButton createOutlineButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(37, 99, 235));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(191, 219, 254)),
                new EmptyBorder(12, 18, 12, 18)
        ));
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return button;
    }

    private JLabel createAvatar(String fullName, final Color bgColor) {
        String initials = "KH";
        if (fullName != null && !fullName.isBlank()) {
            String[] parts = fullName.trim().split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Math.min(parts.length, 2); i++) {
                if (!parts[i].isEmpty()) {
                    sb.append(Character.toUpperCase(parts[i].charAt(0)));
                }
            }
            initials = sb.toString();
        }

        JLabel label = new JLabel(initials, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setOpaque(false);
        label.setBackground(bgColor);
        label.setPreferredSize(new Dimension(42, 42));
        label.setMinimumSize(new Dimension(42, 42));
        label.setMaximumSize(new Dimension(42, 42));
        return label;
    }

    private JLabel createStatusBadge(String text, boolean active) {
        JLabel label = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        label.setOpaque(false);
        label.setBorder(new EmptyBorder(4, 10, 4, 10));
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setBackground(active ? new Color(220, 252, 231) : new Color(241, 245, 249));
        label.setForeground(active ? new Color(22, 163, 74) : new Color(148, 163, 184));
        return label;
    }

    private JLabel createRankBadge(String rankCode) {
        String text = mapRank(rankCode);
        Color bg;
        Color fg;

        switch (safe(rankCode).toLowerCase()) {
            case "bac":
            case "silver":
                bg = new Color(241, 245, 249);
                fg = new Color(71, 85, 105);
                break;
            case "vang":
            case "gold":
                bg = new Color(254, 249, 195);
                fg = new Color(161, 98, 7);
                break;
            case "kimcuong":
            case "diamond":
                bg = new Color(219, 234, 254);
                fg = new Color(29, 78, 216);
                break;
            default: // Dong / Bronze
                bg = new Color(255, 237, 213);
                fg = new Color(154, 52, 18);
                break;
        }

        JLabel label = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        label.setOpaque(false);
        label.setBorder(new EmptyBorder(3, 8, 3, 8));
        label.setFont(new Font("Segoe UI", Font.BOLD, 10));
        label.setBackground(bg);
        label.setForeground(fg);
        return label;
    }

    private JLabel createMutedLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(new Color(148, 163, 184));
        return label;
    }

    private void showEmptyDetail() {
        detailPanel.removeAll();
        JLabel label = new JLabel("Không có dữ liệu khách hàng", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        label.setForeground(new Color(148, 163, 184));
        detailPanel.add(label, BorderLayout.CENTER);
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

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String formatDate(Date date) {
        return date == null ? "-" : DATE_FORMAT.format(date);
    }

    private String mapInvoiceStatus(String status) {
        if ("DaThanhToan".equalsIgnoreCase(status)) {
            return "Đã thanh toán";
        }
        if ("ChuaThanhToan".equalsIgnoreCase(status)) {
            return "Chưa thanh toán";
        }
        return safe(status);
    }

    private String mapRank(String rank) {
        if ("Bac".equalsIgnoreCase(rank)) {
            return "Bạc";
        }
        if ("Vang".equalsIgnoreCase(rank)) {
            return "Vàng";
        }
        if ("KimCuong".equalsIgnoreCase(rank)) {
            return "Kim cương";
        }
        return "Đồng";
    }

    private String mapRankToCode(String rank) {
        if ("Bạc".equalsIgnoreCase(rank) || "Silver".equalsIgnoreCase(rank)) {
            return "Bac";
        }
        if ("Vàng".equalsIgnoreCase(rank) || "Gold".equalsIgnoreCase(rank)) {
            return "Vang";
        }
        if ("Kim cương".equalsIgnoreCase(rank) || "Diamond".equalsIgnoreCase(rank)) {
            return "KimCuong";
        }
        return "Dong";
    }

    private Color pickAvatarColor(String seed) {
        Color[] colors = {
                new Color(59, 130, 246),
                new Color(139, 92, 246),
                new Color(236, 72, 153),
                new Color(245, 158, 11),
                new Color(6, 182, 212)
        };
        int index = Math.abs(safe(seed).hashCode()) % colors.length;
        return colors[index];
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
}