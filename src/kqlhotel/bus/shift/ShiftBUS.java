package kqlhotel.bus.shift;

import kqlhotel.dao.shift.ShiftDAO;
import kqlhotel.dao.shift.ShiftDAO.ShiftInfo;
import kqlhotel.dao.shift.ShiftDAO.ShiftReconciliationRow;

import java.util.List;

public class ShiftBUS {
    private final ShiftDAO shiftDAO = new ShiftDAO();

    public boolean openShift(String maNV, long tienMoCa) {
        return shiftDAO.openShift(maNV, tienMoCa);
    }

    public boolean canOpenShift(String maNV) {
        return shiftDAO.canOpenShift(maNV);
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

    public List<ShiftReconciliationRow> getRecentShiftReconciliations(int limit) {
        return shiftDAO.getRecentShiftReconciliations(limit);
    }

    public List<ShiftReconciliationRow> getActiveAndAssignedShiftReconciliations(int limit) {
        return shiftDAO.getActiveAndAssignedShiftReconciliations(limit);
    }

    public String getOpenShiftIdByStaff(String maNV) {
        return shiftDAO.getOpenShiftIdByStaff(maNV);
    }
}
