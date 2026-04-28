package kqlhotel.entity;

import java.time.LocalDate;

public class Staff {
    private String staffId;
    private String fullName;
    private String phone;
    private Boolean gender;
    private Account account;
    private LocalDate ngayVao;
    private Double luong;

    public Staff() {}

    public Staff(String staffId, String fullName, String phone, Boolean gender, Account account) {
        this.staffId  = staffId;
        this.fullName = fullName;
        this.phone    = phone;
        this.gender   = gender;
        this.account  = account;
    }

    public Staff(String staffId, String fullName, String phone, Boolean gender,
                 Account account, LocalDate ngayVao, Double luong) {
        this(staffId, fullName, phone, gender, account);
        this.ngayVao = ngayVao;
        this.luong   = luong;
    }

    public String    getStaffId()  { return staffId;  }
    public void      setStaffId(String staffId)  { this.staffId  = staffId;  }

    // Alias for compatibility
    public String    getMaNV()     { return staffId; }

    public String    getFullName() { return fullName; }
    public void      setFullName(String fullName) { this.fullName = fullName; }

    public String    getPhone()    { return phone;    }
    public void      setPhone(String phone)    { this.phone    = phone;    }

    public Boolean   getGender()   { return gender;   }
    public void      setGender(Boolean gender) { this.gender   = gender;   }

    public Account   getAccount()  { return account;  }
    public void      setAccount(Account account)  { this.account  = account;  }

    public LocalDate getNgayVao()  { return ngayVao;  }
    public void      setNgayVao(LocalDate ngayVao) { this.ngayVao  = ngayVao;  }

    public Double    getLuong()    { return luong;    }
    public void      setLuong(Double luong)    { this.luong    = luong;    }
}
