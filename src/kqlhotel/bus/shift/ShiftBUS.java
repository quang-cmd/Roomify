package kqlhotel.bus.shift;

import kqlhotel.dao.shift.ShiftDAO;
import kqlhotel.dao.shift.ShiftDAO.ShiftInfo;

public class ShiftBUS {
    private final ShiftDAO shiftDAO = new ShiftDAO();

    public boolean openShift(String maNV, long tienMoCa) {
        return shiftDAO.openShift(maNV, tienMoCa);
    }

    public boolean hasOpenShiftNow(String maNV) {
        return shiftDAO.hasOpenShiftNow(maNV);
    }

    public ShiftInfo getOpenShiftByStaff(String maNV) {
        return shiftDAO.getOpenShiftByStaff(maNV);
    }

    public boolean closeShift(String maPC, long tienKetCa) {
        return shiftDAO.closeShift(maPC, tienKetCa);
    }

    public String getLatestOpenShiftStaffId() {
        return shiftDAO.getLatestOpenShiftStaffId();
    }

    public ShiftInfo getCurrentShift() {
        return shiftDAO.getCurrentShift();
    }

    public String getOpenShiftIdByStaff(String maNV) {
        return shiftDAO.getOpenShiftIdByStaff(maNV);
    }
}