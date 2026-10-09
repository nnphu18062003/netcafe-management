package netcafe.service;

/** Cấu hình tỉ lệ quy đổi: số tiền (VND) cho 1 giờ chơi. */
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

    /** Quy đổi số tiền sang số giây chơi. */
    public long moneyToSeconds(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số tiền phải lớn hơn 0");
        }
        return amount * 3600 / pricePerHour;
    }
}
