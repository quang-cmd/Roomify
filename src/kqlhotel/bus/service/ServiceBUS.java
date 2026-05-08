package kqlhotel.bus.service;

import java.util.List;
import kqlhotel.dao.service.ServiceDAO;
import kqlhotel.entity.Service;

public class ServiceBUS {

    private final ServiceDAO serviceDAO = new ServiceDAO();

    public List<Service> getAll() {
        return serviceDAO.getAll();
    }

    public List<Service> getAllDetailed() {
        return serviceDAO.getAllDetailed();
    }

    public List<Service> search(String query) {
        return serviceDAO.search(query);
    }

    public List<Service> getAllActive() {
        return serviceDAO.getAllActive();
    }

    public boolean insert(Service dichVu) {
        return serviceDAO.create(dichVu);
    }

    public boolean update(Service dichVu) {
        return serviceDAO.update(dichVu);
    }

    public boolean delete(String id) {
        return serviceDAO.delete(id);
    }

    public boolean updateStatus(String maDV, String trangThai) {
        return serviceDAO.updateStatus(maDV, trangThai);
    }

    public String getNextId() {
        return serviceDAO.getNextId();
    }
}
