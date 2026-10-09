package netcafe.service;

public class Settings {
    private long pricePerHour;

    public Settings(long pricePerHour) {
        setPricePerHour(pricePerHour);
    }

    public long getPricePerHour() { return pricePerHour; }

    public void setPricePerHour(long pricePerHour) {
        if (pricePerHour <= 0) {
            throw new IllegalArgumentException("Giá mỗi giờ phải lớn hơn 0");
        }
        this.pricePerHour = pricePerHour;
    }

    public long moneyToSeconds(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số tiền phải lớn hơn 0");
        }
        return amount * 3600 / pricePerHour;
    }
}
