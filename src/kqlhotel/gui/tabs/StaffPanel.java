package kqlhotel.gui.tabs;

import kqlhotel.bus.staff.StaffBUS;
import kqlhotel.entity.Staff;
import kqlhotel.gui.components.PrimaryButton;
import kqlhotel.gui.components.RoundedPanel;
import kqlhotel.gui.theme.ThemeColors;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class StaffPanel extends JPanel {
    private static final Color PAGE_BG = new Color(245, 248, 252);
    private final JPanel listContainer = new JPanel(new MigLayout("wrap 1,insets 0,gap 0", "[grow,fill]", "[]"));
    
    private final StaffBUS staffBUS = new StaffBUS();
    private List<Staff> staffList;
    private JLabel subtitle;
    
    private String currentStatusFilter = "Tất cả";
    private String currentSearchQuery = "";
    private final java.util.List<JButton> filterButtons = new java.util.ArrayList<>();

    public StaffPanel() {
        setOpaque(false);
        setBackground(PAGE_BG);
        setLayout(new MigLayout("insets 24,gap 0,wrap 1", "[grow,fill]", "[][][grow,fill]"));

        // Header
        JPanel header = new JPanel(new MigLayout("insets 0 0 20 0,gap 0", "[grow][]", "[]"));
        header.setOpaque(false);
        
        JPanel titleGroup = new JPanel(new MigLayout("insets 0,wrap 1,gap 2", "[]", "[]"));
        titleGroup.setOpaque(false);
        JLabel title = new JLabel("Nhân sự");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        title.setForeground(new Color(24, 40, 66));
        subtitle = new JLabel("0 nhân viên - 0 đang làm việc");
        subtitle.setForeground(new Color(100, 116, 139));
        titleGroup.add(title);
        titleGroup.add(subtitle);
        
        PrimaryButton btnAdd = new PrimaryButton("+ Thêm nhân viên");
        btnAdd.setBackground(new Color(17, 24, 39));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> {
            java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
            kqlhotel.gui.components.AddStaffDialog dialog =
                new kqlhotel.gui.components.AddStaffDialog(owner, staffBUS, this::reloadData);
            dialog.setVisible(true);
        });

        header.add(titleGroup, "aligny center");
        header.add(btnAdd, "h 44!,aligny center");

        // Filter Bar
        JPanel filterBar = new JPanel(new MigLayout("insets 0 0 20 0,gap 12", "[400!][][][][grow,fill]", "[]"));
        filterBar.setOpaque(false);
        
        JTextField searchField = new JTextField(20);
        searchField.putClientProperty("JTextField.placeholderText", "Tìm nhân viên...");
        
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateSearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateSearch(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateSearch(); }
            private void updateSearch() {
                currentSearchQuery = searchField.getText().toLowerCase().trim();
                applyFilters();
            }
        });

        RoundedPanel searchWrapper = new RoundedPanel(38, new Color(248, 250, 252), new Color(226, 232, 240), 1f);
        searchWrapper.setLayout(new BorderLayout());
        searchWrapper.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        JLabel searchIconLbl = new JLabel(loadScaledIcon("search.png", 18, 18));
        
        searchField.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 0));
        searchField.setOpaque(false);
        searchField.setForeground(new Color(30, 41, 59));
        
        searchWrapper.add(searchIconLbl, BorderLayout.WEST);
        searchWrapper.add(searchField, BorderLayout.CENTER);
        
        filterBar.add(searchWrapper, "growx, h 38!");
        
        JButton btnAll = createFilterBtn("Tất cả", true);
        JButton btnActive = createFilterBtn("Đang làm", false);
        JButton btnStopped = createFilterBtn("Nghỉ việc", false);
        
        filterButtons.add(btnAll);
        filterButtons.add(btnActive);
        filterButtons.add(btnStopped);

        filterBar.add(btnAll, "h 38!");
        filterBar.add(btnActive, "h 38!");
        filterBar.add(btnStopped, "h 38!");
        
        btnAll.addActionListener(e -> selectFilter(btnAll, "Tất cả"));
        btnActive.addActionListener(e -> selectFilter(btnActive, "Đang làm"));
        btnStopped.addActionListener(e -> selectFilter(btnStopped, "Nghỉ việc"));

        // List Table
        JPanel listWrapper = new JPanel(new BorderLayout());
        listWrapper.setBackground(Color.WHITE);
        listWrapper.setBorder(BorderFactory.createLineBorder(new Color(230, 235, 245), 1));
        
        JPanel listHeader = new JPanel(new MigLayout("insets 16 20,gap 10", "[250][120][150][200][120][120][100][grow]", "[]"));
        listHeader.setOpaque(false);
        listHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 235, 245)));
        
        listHeader.add(createColHeader("NHÂN VIÊN"));
        listHeader.add(createColHeader("BỘ PHẬN"));
        listHeader.add(createColHeader("CA LÀM VIỆC"));
        listHeader.add(createColHeader("LIÊN HỆ"));
        listHeader.add(createColHeader("NGÀY VÀO"));
        listHeader.add(createColHeader("TÌNH TRẠNG"));
        listHeader.add(createColHeader("LƯƠNG"));
        
        listContainer.setOpaque(false);
        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        scrollPane.setColumnHeaderView(listHeader);
        listWrapper.add(scrollPane, BorderLayout.CENTER);

        add(header, "growx");
        add(filterBar, "growx");
        add(listWrapper, "grow");

        reloadData();
    }

    private JLabel createColHeader(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(new Color(130, 145, 170));
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 11f));
        return lbl;
    }

    private JButton createFilterBtn(String text, boolean active) {
        JButton btn = new JButton(text) {
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
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 13f));
        btn.setContentAreaFilled(false);
        if (active) {
            btn.setBackground(new Color(17, 24, 39));
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(new Color(241, 245, 249));
            btn.setForeground(new Color(100, 116, 139));
        }
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void selectFilter(JButton selectedBtn, String filterText) {
        currentStatusFilter = filterText;
        for (JButton btn : filterButtons) {
            boolean active = (btn == selectedBtn);
            btn.setBackground(active ? new Color(17, 24, 39) : new Color(241, 245, 249));
            btn.setForeground(active ? Color.WHITE : new Color(100, 116, 139));
        }
        applyFilters();
    }

    private void applyFilters() {
        if (staffList == null) return;
        java.util.stream.Stream<Staff> stream = staffList.stream();
        
        if ("Đang làm".equals(currentStatusFilter)) {
            stream = stream.filter(s -> s.getAccount() != null && "DangHoatDong".equals(s.getAccount().getStatus()));
        } else if ("Nghỉ việc".equals(currentStatusFilter)) {
            stream = stream.filter(s -> s.getAccount() != null && "NgungHoatDong".equals(s.getAccount().getStatus()));
        }
        
        if (!currentSearchQuery.isEmpty()) {
            stream = stream.filter(s -> {
                String name = s.getFullName() != null ? s.getFullName().toLowerCase() : "";
                return name.contains(currentSearchQuery);
            });
        }
        
        List<Staff> filteredList = stream.collect(java.util.stream.Collectors.toList());
        renderStaffList(filteredList);
    }

    public void reloadData() {
        staffList = staffBUS.getAll();
        long activeCount = staffList.stream().filter(s -> s.getAccount() != null && "DangHoatDong".equals(s.getAccount().getStatus())).count();
        subtitle.setText(staffList.size() + " nhân viên - " + activeCount + " đang làm việc");
        applyFilters();
    }

    private void renderStaffList(List<Staff> listToRender) {
        listContainer.removeAll();
        for (int i = 0; i < listToRender.size(); i++) {
            Staff staff = listToRender.get(i);
            JPanel row = createStaffRow(staff);
            if (i < listToRender.size() - 1) {
                row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 244, 250)));
            }
            listContainer.add(row);
        }
        listContainer.revalidate();
        listContainer.repaint();
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "NA";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    private JPanel createStaffRow(Staff staff) {
        String name = staff.getFullName() != null && !staff.getFullName().trim().isEmpty() ? staff.getFullName() : "Nhân viên vô danh";
        String initials = getInitials(name);
        
        String roleCode = staff.getAccount() != null ? staff.getAccount().getRole() : "Chưa có";
        String roleDisplay = "QuanLy".equals(roleCode) ? "Quản lý" : ("NhanVien".equals(roleCode) ? "Nhân viên" : roleCode);
        
        String phone = staff.getPhone() != null && !staff.getPhone().isEmpty() ? staff.getPhone() : "Chưa cập nhật";
        String email = "Chưa cập nhật";
        String department = roleDisplay;
        String shift = "Hành chính";
        String dateJoined;
        if (staff.getNgayVao() != null) {
            dateJoined = staff.getNgayVao().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } else {
            dateJoined = "Chưa cập nhật";
        }
        String salary;
        if (staff.getLuong() != null) {
            long l = staff.getLuong().longValue();
            salary = String.format("%,d", l).replace(',', '.') + "đ";
        } else {
            salary = "Chưa cập nhật";
        }

        String accountStatus = staff.getAccount() != null ? staff.getAccount().getStatus() : null;
        String statusLabel;
        Color statusBg, statusFg;
        if ("DangHoatDong".equals(accountStatus)) {
            statusLabel = "● Đang làm";
            statusBg = new Color(220, 252, 231);
            statusFg = new Color(22, 163, 74);
        } else {
            statusLabel = "● Đã nghỉ";
            statusBg = new Color(254, 226, 226);
            statusFg = new Color(185, 28, 28);
        }

        Color bg = "QuanLy".equals(roleCode) ? new Color(223, 248, 239) : new Color(238, 232, 255);
        Color tone = "QuanLy".equals(roleCode) ? new Color(30, 180, 120) : new Color(143, 97, 255);

        JPanel row = new JPanel(new MigLayout("insets 16 20,gap 10", "[250,fill][120,fill][150,fill][200,fill][120,fill][120,fill][100,fill][grow,right]", "[]"));
        row.setOpaque(false);

        JPanel infoCol = new JPanel(new MigLayout("insets 0,gap 12", "[][grow]", "[]"));
        infoCol.setOpaque(false);
        JPanel avatar = makeAvatar(bg, tone, initials);
        
        JPanel textGroup = new JPanel(new MigLayout("insets 0,wrap 1,gap 2", "[]", "[]"));
        textGroup.setOpaque(false);
        JLabel nameLbl = new JLabel(name);
        nameLbl.setFont(nameLbl.getFont().deriveFont(Font.BOLD, 14f));
        nameLbl.setForeground(new Color(30, 50, 80));
        JLabel roleLbl = new JLabel(roleDisplay);
        roleLbl.setFont(roleLbl.getFont().deriveFont(12f));
        roleLbl.setForeground(new Color(130, 145, 170));
        textGroup.add(nameLbl);
        textGroup.add(roleLbl);
        
        infoCol.add(avatar, "w 40!,h 40!");
        infoCol.add(textGroup);

        JPanel deptBadge = makeBadge(department, new Color(240, 244, 255), new Color(60, 100, 200));
        JLabel shiftLbl = new JLabel(shift);
        shiftLbl.setForeground(new Color(80, 100, 130));

        JPanel contactGroup = new JPanel(new MigLayout("insets 0,wrap 1,gap 2", "[]", "[]"));
        contactGroup.setOpaque(false);
        
        JPanel phoneRow = new JPanel(new MigLayout("insets 0, gap 6", "[]", "[]"));
        phoneRow.setOpaque(false);
        JLabel phoneLbl = new JLabel(phone);
        phoneLbl.setForeground(new Color(30, 50, 80));
        phoneRow.add(phoneLbl);
        
        JPanel emailRow = new JPanel(new MigLayout("insets 0, gap 6", "[]", "[]"));
        emailRow.setOpaque(false);
        JLabel emailLbl = new JLabel(email);
        emailLbl.setFont(emailLbl.getFont().deriveFont(11f));
        emailLbl.setForeground(new Color(130, 145, 170));
        emailRow.add(emailLbl);
        
        contactGroup.add(phoneRow);
        contactGroup.add(emailRow);

        JLabel dateLbl = new JLabel(dateJoined);
        JPanel statusBadge = makeBadge(statusLabel, statusBg, statusFg);
        JLabel salaryLbl = new JLabel(salary);
        salaryLbl.setFont(salaryLbl.getFont().deriveFont(Font.BOLD, 13f));
        salaryLbl.setForeground(new Color(30, 50, 80));

        JPanel actionGroup = new JPanel(new MigLayout("insets 0,gap 8", "[][]", "[]"));
        actionGroup.setOpaque(false);

        javax.swing.ImageIcon editIcon = loadScaledIcon("edit.png", 18, 18);
        JLabel editBtn = editIcon != null ? new JLabel(editIcon) : new JLabel("✎");
        editBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(StaffPanel.this);
                kqlhotel.gui.components.EditStaffDialog dialog =
                    new kqlhotel.gui.components.EditStaffDialog(owner, staffBUS, staff, StaffPanel.this::reloadData);
                dialog.setVisible(true);
            }
        });

        actionGroup.add(editBtn);

        row.add(infoCol, "aligny center");
        row.add(deptBadge, "aligny center,left");
        row.add(shiftLbl, "aligny center");
        row.add(contactGroup, "aligny center");
        row.add(dateLbl, "aligny center");
        row.add(statusBadge, "aligny center,left");
        row.add(salaryLbl, "aligny center");
        row.add(actionGroup, "aligny center,right");

        return row;
    }

    private JPanel makeAvatar(Color bg, Color fg, String txt) {
        JPanel circle = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        circle.setOpaque(false);
        JLabel lbl = new JLabel(txt, SwingConstants.CENTER);
        lbl.setForeground(fg);
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 13f));
        circle.add(lbl);
        return circle;
    }

    private JPanel makeBadge(String text, Color bg, Color fg) {
        JPanel badge = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        badge.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setForeground(fg);
        lbl.setFont(lbl.getFont().deriveFont(12f));
        badge.add(lbl);
        return badge;
    }

    private javax.swing.ImageIcon loadScaledIcon(String filename, int w, int h) {
        try {
            java.net.URL resource = getClass().getResource("/kqlhotel/resources/icons/" + filename);
            if (resource != null) {
                return new javax.swing.ImageIcon(new javax.swing.ImageIcon(resource).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
            }
            java.io.File file = new java.io.File("src/kqlhotel/resources/icons/" + filename);
            if (file.exists()) {
                return new javax.swing.ImageIcon(new javax.swing.ImageIcon(file.toURI().toURL()).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
            }
        } catch (Exception ignored) {}
        return null;
    }
}
