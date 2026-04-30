package kqlhotel.main;
// hahahah

import kqlhotel.dao.ConnectDB;
import kqlhotel.dao.account.AccountDAO;
import kqlhotel.dao.Staff.StaffDAO;
import kqlhotel.entity.Account;
import kqlhotel.entity.Staff;

import java.time.LocalDate;
import java.util.List;

public class TestDB {
    public static void main(String[] args) {
        System.out.println("=== KIEM TRA KET NOI DU LIEU (LINK DATA) ===");
        try {
            System.out.print("Dang ket noi... ");
            ConnectDB.getInstance().connect();
            System.out.println("OK! (Thanh cong)");
            
            StaffDAO sdao = new StaffDAO();
            List<Staff> list = sdao.getAll();
            
            System.out.println("--- Ket qua truy van ---");
            System.out.println("So luong nhan vien hien co trong DB: " + list.size());
            
            if (list.size() > 0) {
                System.out.println("Vi du 1 nhan vien: " + list.get(0).getFullName());
            } else {
                System.out.println("Luu y: Database dang trong (chua co du lieu).");
            }
            
            System.out.println("==========================================");
            System.out.println("KET LUAN: CODE DA LINK DU LIEU THANH CONG!");
            
        } catch (Exception e) {
            System.err.println("\n!!! THAT BAI !!!");
            System.err.println("Khong the link du lieu. Vui long kiem tra lai ConnectDB.java");
        }
    }
}
