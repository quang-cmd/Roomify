package kqlhotel.bus.account;

import kqlhotel.dao.account.AccountDAO;
import kqlhotel.entity.Account;
import java.util.List;

public class AccountBUS {
    private AccountDAO accountDAO;

    public AccountBUS() {
        accountDAO = new AccountDAO();
    }

    public List<Account> getAll() {
        return accountDAO.getAll();
    }

    public boolean insert(Account acc) {
        return accountDAO.insert(acc);
    }

    public boolean update(Account acc) {
        return accountDAO.update(acc);
    }
}
