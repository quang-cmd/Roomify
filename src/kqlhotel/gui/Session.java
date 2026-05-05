package kqlhotel.gui;

import kqlhotel.entity.Account;
import kqlhotel.entity.Staff;

public class Session {
    public static Account currentAccount;
    public static Staff currentStaff;

    public static void clear() {
        currentAccount = null;
        currentStaff = null;
    }
}