package netcafe.model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Session {
    private final String customerId;
    private final LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean autoTerminated;

    public Session(String customerId, LocalDateTime startTime) {
        this.customerId = customerId;
        this.startTime = startTime;
    }

    public Session(String customerId, LocalDateTime startTime, LocalDateTime endTime, boolean autoTerminated) {
        this(customerId, startTime);
        this.endTime = endTime;
        this.autoTerminated = autoTerminated;
    }

    public String getCustomerId() { return customerId; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public boolean isAutoTerminated() { return autoTerminated; }
    public boolean isActive() { return endTime == null; }

    public void end(LocalDateTime endTime, boolean autoTerminated) {
        this.endTime = endTime;
        this.autoTerminated = autoTerminated;
    }

    public long elapsedSeconds(LocalDateTime now) {
        LocalDateTime to = endTime != null ? endTime : now;
        return Math.max(0, Duration.between(startTime, to).getSeconds());
    }
}
