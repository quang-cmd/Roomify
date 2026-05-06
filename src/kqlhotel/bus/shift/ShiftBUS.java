package kqlhotel.bus.shift;

import kqlhotel.dao.shift.ShiftDAO;
import kqlhotel.dao.shift.ShiftDAO.ShiftInfo;

public class ShiftBUS {
    private final ShiftDAO shiftDAO = new ShiftDAO();

    public boolean openShift(String maNV, long tienMoCa) {
        return shiftDAO.openShift(maNV, tienMoCa);
    }

    public ShiftInfo getCurrentShift() {
        return shiftDAO.getCurrentShift();
    }
}
