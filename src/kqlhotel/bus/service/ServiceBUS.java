package kqlhotel.bus.service;

import java.util.List;
import kqlhotel.dao.service.ServiceDAO;
import kqlhotel.entity.Service;

public class ServiceBUS {

    private final ServiceDAO serviceDAO = new ServiceDAO();

    public List<Service> getAll() {
        return serviceDAO.getAll();
    }

    public List<Service> getAllActive() {
        return serviceDAO.getAllActive();
    }

    public boolean insert(Service dichVu) {
        return serviceDAO.insert(dichVu);
    }

    public boolean update(Service dichVu) {
        return serviceDAO.update(dichVu);
    }

    public boolean updateStatus(String maDV, String trangThai) {
        return serviceDAO.updateStatus(maDV, trangThai);
    }
}
