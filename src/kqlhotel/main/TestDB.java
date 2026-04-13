package kqlhotel.main;
// hahahah

import kqlhotel.dao.ConnectDB;
import kqlhotel.dao.Account.AccountDAO;
import kqlhotel.dao.Staff.StaffDAO;
import kqlhotel.entity.Account;
import kqlhotel.entity.Staff;

import java.time.LocalDate;
import java.util.List;

public class TestDB {
    public static void main(String[] args) {
        System.out.println("Testing DB Connection...");
        try {
            ConnectDB.getInstance().connect();
            System.out.println("Connected!");
            
            AccountDAO adao = new AccountDAO();
            StaffDAO sdao = new StaffDAO();
            
            // Insert account
            Account acc = new Account("testuser99", "pass123", "NhanVien", "DangHoatDong");
            System.out.println("Inserting account: " + adao.insert(acc));
            
            // Insert staff
            Staff staff = new Staff("NV99", "Test User", "0123456789", true, acc, LocalDate.now(), 5000000.0);
            System.out.println("Inserting staff: " + sdao.insert(staff));
            
            List<Staff> list = sdao.getAll();
            System.out.println("Total staff in DB: " + list.size());
            
            Account acc2 = new Account("testuser100", "pass123", "NhanVien", "DangHoatDong");
            System.out.println("Inserting account2: " + adao.insert(acc2));
            Staff staff2 = new Staff("NV100", "Test User 2", "0123456788", false, acc2, LocalDate.now(), null);
            System.out.println("Inserting staff2 (null salary): " + sdao.insert(staff2));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
