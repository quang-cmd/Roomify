package kqlhotel.entity;

import java.util.Date;

public class Invoice {
    private String invoiceId;
    private Date createdDate;
    private Date paymentDate;
    private String note;
    private Integer guestCount;
    private Double roomTotal;
    private Double serviceTotal;
    private Double promotionTotal;
    private Double taxTotal;
    private Double finalTotal;
    private Double roomChangeFee;
    private String promotionId;
    private String customerId;
    private String staffId;
    private String paymentMethod;
    private String status;

    public Invoice() {}

    public String getInvoiceId() { return invoiceId; }
    public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }

    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Integer getGuestCount() { return guestCount; }
    public void setGuestCount(Integer guestCount) { this.guestCount = guestCount; }

    public Double getRoomTotal() { return roomTotal; }
    public void setRoomTotal(Double roomTotal) { this.roomTotal = roomTotal; }

    public Double getServiceTotal() { return serviceTotal; }
    public void setServiceTotal(Double serviceTotal) { this.serviceTotal = serviceTotal; }

    public Double getPromotionTotal() { return promotionTotal; }
    public void setPromotionTotal(Double promotionTotal) { this.promotionTotal = promotionTotal; }

    public Double getTaxTotal() { return taxTotal; }
    public void setTaxTotal(Double taxTotal) { this.taxTotal = taxTotal; }

    public Double getFinalTotal() { return finalTotal; }
    public void setFinalTotal(Double finalTotal) { this.finalTotal = finalTotal; }

    public Double getRoomChangeFee() { return roomChangeFee; }
    public void setRoomChangeFee(Double roomChangeFee) { this.roomChangeFee = roomChangeFee; }

    public String getPromotionId() { return promotionId; }
    public void setPromotionId(String promotionId) { this.promotionId = promotionId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
