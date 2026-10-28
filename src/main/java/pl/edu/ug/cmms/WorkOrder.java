package pl.edu.ug.cmms;

import java.time.LocalDateTime;
import java.util.Objects;

public class WorkOrder {

    public enum WorkOrderType {
        PREVENTIVE,
        INSPECTION,
        BREAKDOWN
    }

    public enum Priority {
        CRITICAL,
        HIGH,
        NORMAL,
        LOW
    }

    public enum WorkOrderStatus {
        OPEN,
        IN_PROGRESS,
        DONE,
        CANCELLED
    }

    private final String id;
    private final String machineId;
    private final WorkOrderType type;
    private final Priority priority;
    private final WorkOrderStatus status;
    private final LocalDateTime reportedAt;
    private final LocalDateTime closedAt;

    public WorkOrder(String id, String machineId, WorkOrderType type, Priority priority,
                     WorkOrderStatus status, LocalDateTime reportedAt, LocalDateTime closedAt) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Work order ID cannot be null or blank");
        }
        if (status == WorkOrderStatus.DONE && closedAt == null) {
            throw new IllegalArgumentException("DONE work order must have a closed date");
        }
        if (closedAt != null && reportedAt != null && closedAt.isBefore(reportedAt)) {
            throw new IllegalArgumentException("Closed date cannot be earlier than reported date");
        }
        this.id = id;
        this.machineId = machineId;
        this.type = type;
        this.priority = priority;
        this.status = status;
        this.reportedAt = reportedAt;
        this.closedAt = closedAt;
    }

    public String getId() {
        return id;
    }

    public String getMachineId() {
        return machineId;
    }

    public WorkOrderType getType() {
        return type;
    }

    public Priority getPriority() {
        return priority;
    }

    public WorkOrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkOrder workOrder = (WorkOrder) o;
        return Objects.equals(id, workOrder.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}