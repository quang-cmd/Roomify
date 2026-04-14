package kqlhotel.dao.Staff;

import kqlhotel.entity.Staff;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    private List<Staff> ds = new ArrayList<>();

    public StaffDAO() {
        // dữ liệu cứng
        ds.add(new Staff("NV01","Nguyễn Văn A","0123456789","admin"));
        ds.add(new Staff("NV02","Trần Thị B","0987654321","user"));
    }

    public List<Staff> getAll() {
        return ds;
    }

    public void insert(Staff s) {
        ds.add(s);
    }

    public void delete(String maNV) {
        ds.removeIf(s -> s.getMaNV().equals(maNV));
    }
}