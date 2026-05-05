package kqlhotel.gui;

import kqlhotel.entity.Account;
import kqlhotel.entity.Staff;

public class Session {
    public static Staff currentStaff;
    public static Account currentAccount;
    
    public static void clear() {
        currentStaff = null;
        currentAccount = null;
    }
}
