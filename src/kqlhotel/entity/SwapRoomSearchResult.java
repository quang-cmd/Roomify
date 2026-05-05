package kqlhotel.entity;

import java.time.LocalDateTime;

public class SwapRoomSearchResult {
    private String bookingDetailId;
    private String bookingId;
    private String customerId;
    private String customerName;
    private String phoneNumber;
    private String idCard;
    private String currentRoomId;
    private String currentRoomTypeId;
    private String currentRoomTypeName;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private int occupantCount;
    private int currentRoomMaxCapacity;

    public SwapRoomSearchResult() {}

    public String getBookingDetailId() { return bookingDetailId; }
    public void setBookingDetailId(String bookingDetailId) { this.bookingDetailId = bookingDetailId; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }

    public String getCurrentRoomId() { return currentRoomId; }
    public void setCurrentRoomId(String currentRoomId) { this.currentRoomId = currentRoomId; }

    public String getCurrentRoomTypeId() { return currentRoomTypeId; }
    public void setCurrentRoomTypeId(String currentRoomTypeId) { this.currentRoomTypeId = currentRoomTypeId; }

    public String getCurrentRoomTypeName() { return currentRoomTypeName; }
    public void setCurrentRoomTypeName(String currentRoomTypeName) { this.currentRoomTypeName = currentRoomTypeName; }

    public LocalDateTime getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDateTime checkInDate) { this.checkInDate = checkInDate; }

    public LocalDateTime getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDateTime checkOutDate) { this.checkOutDate = checkOutDate; }

    public int getOccupantCount() { return occupantCount; }
    public void setOccupantCount(int occupantCount) { this.occupantCount = occupantCount; }

    public int getCurrentRoomMaxCapacity() { return currentRoomMaxCapacity; }
    public void setCurrentRoomMaxCapacity(int currentRoomMaxCapacity) { this.currentRoomMaxCapacity = currentRoomMaxCapacity; }
}
