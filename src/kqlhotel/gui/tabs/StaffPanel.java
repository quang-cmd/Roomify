package kqlhotel.gui.tabs;

import kqlhotel.bus.staff.StaffBUS;
import kqlhotel.entity.Staff;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class StaffPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private StaffBUS bus = new StaffBUS();

    private JTextField txtMa, txtTen, txtSDT, txtTK;

    public StaffPanel() {

        setLayout(new BorderLayout(10,10));

        JLabel title = new JLabel("QUẢN LÝ NHÂN VIÊN");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        // ===== TABLE =====
        model = new DefaultTableModel(
                new String[]{"Mã","Tên","SĐT","Tài khoản"},0);

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== FORM =====
        JPanel form = new JPanel(new GridLayout(2,4,10,10));

        txtMa = new JTextField();
        txtTen = new JTextField();
        txtSDT = new JTextField();
        txtTK = new JTextField();

        JButton btnThem = new JButton("Thêm");
        JButton btnXoa = new JButton("Xóa");

        form.add(new JLabel("Mã NV"));
        form.add(txtMa);
        form.add(new JLabel("Tên"));
        form.add(txtTen);
        form.add(new JLabel("SĐT"));
        form.add(txtSDT);
        form.add(new JLabel("Tài khoản"));
        form.add(txtTK);

        form.add(btnThem);
        form.add(btnXoa);

        add(form, BorderLayout.SOUTH);

        // load dữ liệu
        loadData();

        // ===== EVENT =====
        btnThem.addActionListener(e -> {
            Staff s = new Staff(
                    txtMa.getText(),
                    txtTen.getText(),
                    txtSDT.getText(),
                    txtTK.getText()
            );
            bus.them(s);
            loadData();
        });

        btnXoa.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row >= 0){
                String ma = model.getValueAt(row,0).toString();
                bus.xoa(ma);
                loadData();
            }
        });
    }

    private void loadData() {
        model.setRowCount(0);
        for(Staff s : bus.getAll()){
            model.addRow(new Object[]{
                    s.getMaNV(),
                    s.getFullName(),
                    s.getPhone(),
                    s.getAccount()
            });
        }
    }
}