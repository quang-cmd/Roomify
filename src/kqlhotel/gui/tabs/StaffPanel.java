package kqlhotel.gui.tabs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import kqlhotel.bus.staff.StaffBUS;
import kqlhotel.entity.Staff;
import kqlhotel.gui.components.PrimaryButton;
import kqlhotel.gui.components.RoundedPanel;
import net.miginfocom.swing.MigLayout;

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
        staffList = staffBUS.getAll();

        setOpaque(false);
        setBackground(PAGE_BG);
        setLayout(new MigLayout("insets 24,gap 20,wrap 1", "[grow,fill]", "[][][grow,fill]"));

        // ===== 1. Header =====
        JPanel header = new JPanel(new MigLayout("insets 0", "[][grow][]", "[]"));
        header.setOpaque(false);

        JPanel titlePanel = new JPanel(new MigLayout("insets 0,wrap 1,gap 2", "[]", "[]"));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("Nhân sự");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        title.setForeground(new Color(24, 40, 66));
        
        long activeCount = staffList.stream().filter(s -> s.getAccount() != null && "DangHoatDong".equals(s.getAccount().getStatus())).count();
        subtitle = new JLabel(staffList.size() + " nhân viên - " + activeCount + " đang làm việc");
        subtitle.setForeground(new Color(150, 165, 190));
        
        titlePanel.add(title);
        titlePanel.add(subtitle);

        PrimaryButton btnAdd = new PrimaryButton("+ Thêm nhân viên");
        btnAdd.setBackground(new Color(17, 24, 39));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.addActionListener(e -> {
            java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
            kqlhotel.gui.components.AddStaffDialog dialog =
                new kqlhotel.gui.components.AddStaffDialog(owner, staffBUS, this::reloadData);
            dialog.setVisible(true);
        });

        header.add(titlePanel);
        header.add(btnAdd, "alignx right,h 44!");

        // ===== 2. Search & Filter Bar =====
        RoundedPanel filterBar = new RoundedPanel(16, Color.WHITE, new Color(225, 231, 245), 1f);
        filterBar.setLayout(new MigLayout("insets 12 16,gap 16", "[250!][][][][]", "[]"));

        JTextField searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "🔍 Tìm nhân viên...");
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(225, 231, 245), 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateSearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateSearch(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateSearch(); }
            private void updateSearch() {
                currentSearchQuery = searchField.getText().toLowerCase().trim();
                applyFilters();
            }
        });

        filterBar.add(searchField, "growy,h 38!");
        
        JButton btnAll = createFilterBtn("Tất cả", true);
        JButton btnActive = createFilterBtn("Đang làm", false);
        JButton btnStopped = createFilterBtn("Nghỉ việc", false);
        
        filterButtons.add(btnAll);
        filterButtons.add(btnActive);
        filterButtons.add(btnStopped);

        filterBar.add(btnAll, "h 38!");
        filterBar.add(btnActive, "h 38!");
        filterBar.add(btnStopped, "h 38!");
        
        // Gắn listener cho các nút lọc
        btnAll.addActionListener(e -> selectFilter(btnAll, "Tất cả"));
        btnActive.addActionListener(e -> selectFilter(btnActive, "Đang làm"));
        btnStopped.addActionListener(e -> selectFilter(btnStopped, "Nghỉ việc"));

        // ===== 3. List Container =====
        RoundedPanel listWrapper = new RoundedPanel(16, Color.WHITE, new Color(225, 231, 245), 1f);
        listWrapper.setLayout(new BorderLayout());

        // Header of list
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
        renderStaffList();

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        listWrapper.add(listHeader, BorderLayout.NORTH);
        listWrapper.add(scrollPane, BorderLayout.CENTER);

        // ===== Assemble =====
        add(header, "growx");
        add(filterBar, "growx");
        add(listWrapper, "grow");
    }

    private JLabel createColHeader(String text) {
        JLabel lbl = new JLabel(text + "  ▼");
        lbl.setForeground(new Color(130, 145, 170));
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 11f));
        return lbl;
    }

    private JButton createFilterBtn(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(btn.getFont().deriveFont(13f));
        if (active) {
            btn.setBackground(new Color(17, 24, 39));
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(new Color(100, 120, 150));
        }
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(active ? new Color(17, 24, 39) : new Color(220, 230, 245), 1),
            BorderFactory.createEmptyBorder(6, 16, 6, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void selectFilter(JButton selectedBtn, String filterText) {
        currentStatusFilter = filterText;
        for (JButton btn : filterButtons) {
            boolean active = (btn == selectedBtn);
            btn.setBackground(active ? new Color(17, 24, 39) : Color.WHITE);
            btn.setForeground(active ? Color.WHITE : new Color(100, 120, 150));
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(active ? new Color(17, 24, 39) : new Color(220, 230, 245), 1),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)
            ));
        }
        applyFilters();
    }

    private void applyFilters() {
        java.util.stream.Stream<Staff> stream = staffList.stream();
        
        // 1. Lọc theo trạng thái
        if ("Đang làm".equals(currentStatusFilter)) {
            stream = stream.filter(s -> s.getAccount() != null && "DangHoatDong".equals(s.getAccount().getStatus()));
        } else if ("Nghỉ việc".equals(currentStatusFilter)) {
            stream = stream.filter(s -> s.getAccount() != null && "NgungHoatDong".equals(s.getAccount().getStatus()));
        }
        
        // 2. Lọc theo tên (search)
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
        applyFilters(); // Apply current filters to new data
    }

    private void renderStaffList() {
        renderStaffList(this.staffList);
    }

    private void renderStaffList(List<Staff> listToRender) {
        listContainer.removeAll();
        for (int i = 0; i < listToRender.size(); i++) {
            Staff staff = listToRender.get(i);
            JPanel row = createStaffRow(staff);
            if (i < listToRender.size() - 1) {
                row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(240, 244, 250)));
            }
            listContainer.add(row, "growx");
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
        String email = "Chưa cập nhật"; // Not in DB schema
        String department = roleDisplay;
        String shift = "Hành chính"; // Not in DB schema
        String dateJoined;
        if (staff.getNgayVao() != null) {
            dateJoined = staff.getNgayVao()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
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

        // Tình trạng (Account Status)
        String accountStatus = staff.getAccount() != null ? staff.getAccount().getStatus() : null;
        String statusLabel;
        Color statusBg, statusFg;
        if ("DangHoatDong".equals(accountStatus)) {
            statusLabel = "● Đang làm";
            statusBg = new Color(220, 252, 231);
            statusFg = new Color(22, 163, 74);
        } else if ("NghiPhep".equals(accountStatus)) {
            statusLabel = "● Nghỉ phép";
            statusBg = new Color(254, 243, 199);
            statusFg = new Color(180, 120, 10);
        } else if ("NghiViec".equals(accountStatus)) {
            statusLabel = "● Nghỉ việc";
            statusBg = new Color(254, 226, 226);
            statusFg = new Color(185, 28, 28);
        } else if ("NgungHoatDong".equals(accountStatus)) {
            statusLabel = "● Đã nghỉ"; // Hiển thị chung cho các trạng thái không hoạt động
            statusBg = new Color(254, 226, 226);
            statusFg = new Color(185, 28, 28);
        } else {
            statusLabel = "Chưa rõ";
            statusBg = new Color(240, 240, 240);
            statusFg = new Color(130, 130, 130);
        }

        Color bg = "QuanLy".equals(roleCode) ? new Color(223, 248, 239) : new Color(238, 232, 255);
        Color tone = "QuanLy".equals(roleCode) ? new Color(30, 180, 120) : new Color(143, 97, 255);

        JPanel row = new JPanel(new MigLayout("insets 16 20,gap 10", "[250,fill][120,fill][150,fill][200,fill][120,fill][120,fill][100,fill][grow,right]", "[]"));
        row.setOpaque(false);
        row.setBackground(Color.WHITE);

        // Column 1: Info (Avatar + Name + Role)
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

        // Column 2: Department Badge
        JPanel deptBadge = makeBadge(department, new Color(240, 244, 255), new Color(60, 100, 200));

        // Column 3: Shift
        JLabel shiftLbl = new JLabel("⏱ " + shift);
        shiftLbl.setForeground(new Color(80, 100, 130));

        // Column 4: Contact
        JPanel contactGroup = new JPanel(new MigLayout("insets 0,wrap 1,gap 2", "[]", "[]"));
        contactGroup.setOpaque(false);
        JLabel phoneLbl = new JLabel("📞 " + phone);
        phoneLbl.setForeground(new Color(30, 50, 80));
        JLabel emailLbl = new JLabel("✉ " + email);
        emailLbl.setFont(emailLbl.getFont().deriveFont(11f));
        emailLbl.setForeground(new Color(130, 145, 170));
        contactGroup.add(phoneLbl);
        contactGroup.add(emailLbl);

        // Column 5: Date
        JLabel dateLbl = new JLabel(dateJoined);
        dateLbl.setForeground(new Color(80, 100, 130));

        // Column 6: Status Badge
        JPanel statusBadge = makeBadge(statusLabel, statusBg, statusFg);

        // Column 7: Salary
        JLabel salaryLbl = new JLabel(salary);
        salaryLbl.setFont(salaryLbl.getFont().deriveFont(Font.BOLD, 13f));
        salaryLbl.setForeground(new Color(30, 50, 80));

        // Column 8: Actions
        JPanel actionGroup = new JPanel(new MigLayout("insets 0,gap 8", "[][]", "[]"));
        actionGroup.setOpaque(false);

        javax.swing.ImageIcon editIcon = loadScaledIcon("edit.png", 18, 18);
        JLabel editBtn = editIcon != null ? new JLabel(editIcon) : new JLabel("✎");
        if (editIcon == null) {
            editBtn.setForeground(new Color(60, 120, 220));
            editBtn.setFont(editBtn.getFont().deriveFont(Font.BOLD, 16f));
        }
        editBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editBtn.setToolTipText("Chỉnh sửa nhân viên");
        editBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(StaffPanel.this);
                kqlhotel.gui.components.EditStaffDialog dialog =
                    new kqlhotel.gui.components.EditStaffDialog(owner, staffBUS, staff, StaffPanel.this::reloadData);
                dialog.setVisible(true);
            }
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                editBtn.setOpaque(true);
                editBtn.setBackground(new Color(230, 240, 255));
                editBtn.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
                editBtn.repaint();
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                editBtn.setOpaque(false);
                editBtn.setBorder(null);
                editBtn.repaint();
            }
        });

        JLabel delBtn = new JLabel("🗑");
        delBtn.setForeground(new Color(150, 165, 190));
        delBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        actionGroup.add(editBtn);
        actionGroup.add(delBtn);

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
                g2.setColor(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 50));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
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
            java.awt.image.BufferedImage img;
            if (resource != null) {
                img = javax.imageio.ImageIO.read(resource);
            } else {
                java.io.File file = new java.io.File("src/kqlhotel/resources/icons/" + filename);
                if (!file.exists()) return null;
                img = javax.imageio.ImageIO.read(file);
            }
            java.awt.Image scaled = img.getScaledInstance(w, h, java.awt.Image.SCALE_SMOOTH);
            return new javax.swing.ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        }
    }
}
