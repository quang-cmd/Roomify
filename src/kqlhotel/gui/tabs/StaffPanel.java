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
import kqlhotel.bus.StaffBUS;
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

        filterBar.add(searchField, "growy,h 38!");
        filterBar.add(createFilterBtn("Tất cả", true), "h 38!");
        filterBar.add(createFilterBtn("Đang làm", false), "h 38!");
        filterBar.add(createFilterBtn("Nghỉ phép", false), "h 38!");
        filterBar.add(createFilterBtn("Nghỉ việc", false), "h 38!");

        // ===== 3. List Container =====
        RoundedPanel listWrapper = new RoundedPanel(16, Color.WHITE, new Color(225, 231, 245), 1f);
        listWrapper.setLayout(new BorderLayout());

        // Header of list
        JPanel listHeader = new JPanel(new MigLayout("insets 16 20,gap 10", "[250][120][150][200][120][100][grow]", "[]"));
        listHeader.setOpaque(false);
        listHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 235, 245)));
        
        listHeader.add(createColHeader("NHÂN VIÊN"));
        listHeader.add(createColHeader("BỘ PHẬN"));
        listHeader.add(createColHeader("CA LÀM VIỆC"));
        listHeader.add(createColHeader("LIÊN HỆ"));
        listHeader.add(createColHeader("NGÀY VÀO"));
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

    public void reloadData() {
        staffList = staffBUS.getAll();
        long activeCount = staffList.stream().filter(s -> s.getAccount() != null && "DangHoatDong".equals(s.getAccount().getStatus())).count();
        subtitle.setText(staffList.size() + " nhân viên - " + activeCount + " đang làm việc");
        renderStaffList();
    }

    private void renderStaffList() {
        listContainer.removeAll();
        for (int i = 0; i < staffList.size(); i++) {
            Staff staff = staffList.get(i);
            JPanel row = createStaffRow(staff);
            if (i < staffList.size() - 1) {
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
        String department = "QuanLy".equals(roleCode) ? "Ban quản lý" : "Nội bộ";
        String shift = "Hành chính"; // Not in DB schema
        String dateJoined = "Chưa cập nhật"; // Not in DB schema
        String salary = "Chưa cập nhật"; // Not in DB schema

        Color bg = "QuanLy".equals(roleCode) ? new Color(223, 248, 239) : new Color(238, 232, 255);
        Color tone = "QuanLy".equals(roleCode) ? new Color(30, 180, 120) : new Color(143, 97, 255);

        JPanel row = new JPanel(new MigLayout("insets 16 20,gap 10", "[250,fill][120,fill][150,fill][200,fill][120,fill][100,fill][grow,right]", "[]"));
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

        // Column 6: Salary
        JLabel salaryLbl = new JLabel(salary);
        salaryLbl.setFont(salaryLbl.getFont().deriveFont(Font.BOLD, 13f));
        salaryLbl.setForeground(new Color(30, 50, 80));

        // Column 7: Actions
        JPanel actionGroup = new JPanel(new MigLayout("insets 0,gap 8", "[][]", "[]"));
        actionGroup.setOpaque(false);
        JLabel editBtn = new JLabel("✎");
        editBtn.setForeground(new Color(150, 165, 190));
        editBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
}
