package kqlhotel.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class QrService {

    private static final String BANK_ID = "970422"; // Vietcombank
    private static final String ACCOUNT_NO = "0868465911"; // STK
    private static final String ACCOUNT_NAME = "NGUYEN KHA LUAN";

    public static String generateQrUrl(String maHD, double amount) {
        try {
            String note = "Thanh toan hoa don " + maHD;

            return "https://img.vietqr.io/image/"
                    + BANK_ID + "-" + ACCOUNT_NO + "-compact2.png"
                    + "?amount=" + (long) amount
                    + "&addInfo=" + URLEncoder.encode(note, StandardCharsets.UTF_8)
                    + "&accountName=" + URLEncoder.encode(ACCOUNT_NAME, StandardCharsets.UTF_8);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
