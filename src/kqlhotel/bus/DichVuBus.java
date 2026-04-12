package kqlhotel.bus;

import java.util.List;
import java.util.stream.Collectors;
import kqlhotel.dao.DichVuDao;
import kqlhotel.entity.Service;

public class DichVuBus {
    private final DichVuDao dichVuDao = new DichVuDao();

    public List<Service> getAllServices() {
        return dichVuDao.getAll();
    }

    public Service getServiceById(String maDV) {
        if (isBlank(maDV)) {
            return null;
        }
        return dichVuDao.getById(maDV.trim());
    }

    public List<Service> filterServices(String keyword, String status) {
        String normalizedKeyword = safeTrim(keyword);
        String normalizedStatus = safeTrim(status);

        if (normalizedKeyword.isEmpty() && normalizedStatus.isEmpty()) {
            return getAllServices();
        }
        if (!normalizedKeyword.isEmpty() && normalizedStatus.isEmpty()) {
            return dichVuDao.search(normalizedKeyword);
        }
        if (normalizedKeyword.isEmpty()) {
            return dichVuDao.filterByStatus(normalizedStatus);
        }

        return dichVuDao.search(normalizedKeyword).stream()
            .filter(service -> normalizedStatus.equalsIgnoreCase(safeTrim(service.getTrangThaiDV())))
            .collect(Collectors.toList());
    }

    public boolean createService(Service service) {
        if (!isValid(service, true)) {
            return false;
        }
        normalizeService(service);
        if (dichVuDao.getById(service.getMaDV()) != null) {
            return false;
        }
        return dichVuDao.create(service);
    }

    public boolean updateService(Service service) {
        if (!isValid(service, true)) {
            return false;
        }
        normalizeService(service);
        if (dichVuDao.getById(service.getMaDV()) == null) {
            return false;
        }
        return dichVuDao.update(service);
    }

    public boolean deleteService(String maDV) {
        if (isBlank(maDV)) {
            return false;
        }
        return dichVuDao.delete(maDV.trim());
    }

    public boolean isValid(Service service, boolean requireId) {
        if (service == null) {
            return false;
        }
        if (requireId && isBlank(service.getMaDV())) {
            return false;
        }
        return !isBlank(service.getTenDV()) && service.getDonGia() > 0 && !isBlank(service.getTrangThaiDV());
    }

    private void normalizeService(Service service) {
        service.setMaDV(safeTrim(service.getMaDV()));
        service.setTenDV(safeTrim(service.getTenDV()));
        service.setMoTaDV(safeTrim(service.getMoTaDV()));
        service.setTrangThaiDV(safeTrim(service.getTrangThaiDV()));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
