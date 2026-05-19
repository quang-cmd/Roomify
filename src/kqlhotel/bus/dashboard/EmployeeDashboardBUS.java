package kqlhotel.bus.dashboard;

import kqlhotel.dao.dashboard.EmployeeDashboardDAO;
import kqlhotel.dto.dashboard.RoomScheduleDTO;

import java.time.LocalDate;
import java.util.List;

public class EmployeeDashboardBUS {
    private final EmployeeDashboardDAO dao = new EmployeeDashboardDAO();

    public int countCheckInBookingsToday() {
        return dao.countCheckInBookingsToday(LocalDate.now());
    }

    public int countCheckInRoomsToday() {
        return dao.countCheckInRoomsToday(LocalDate.now());
    }

    public int countCheckOutRoomsToday() {
        return dao.countCheckOutRoomsToday(LocalDate.now());
    }

    public int countNeedPayment() {
        return dao.countNeedPayment();
    }

    public List<RoomScheduleDTO> getTodayCheckInRooms() {
        return dao.getTodayCheckInRooms(LocalDate.now());
    }

    public List<RoomScheduleDTO> getTodayCheckOutRooms() {
        return dao.getTodayCheckOutRooms(LocalDate.now());
    }
}