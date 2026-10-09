package netcafe.service;

public class Settings {
    public static final long TOP_UP_UNIT = 1000;
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
        if (amount % TOP_UP_UNIT != 0) {
            throw new IllegalArgumentException("Số tiền nạp phải là bội số của " + TOP_UP_UNIT + " VND");
        }
        return amount * 3600 / pricePerHour;
    }
}
