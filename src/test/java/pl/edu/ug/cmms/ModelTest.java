package pl.edu.ug.cmms;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class ModelTest {

    @Test
    void testBlankMachineId() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Machine("", "Tokarka", Machine.MachineType.CNC_LATHE, "A", LocalDate.now());
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new Machine(null, "Tokarka", Machine.MachineType.CNC_LATHE, "A", LocalDate.now());
        });
    }

    @Test
    void testDoneWorkOrderWithoutClosedDate() {
        assertThrows(IllegalArgumentException.class, () -> {
            new WorkOrder("WO-999", "M-01", WorkOrder.WorkOrderType.BREAKDOWN,
                    WorkOrder.Priority.HIGH, WorkOrder.WorkOrderStatus.DONE,
                    LocalDateTime.now(), null);
        });
    }

    @Test
    void testClosedDateEarlierThanReported() {
        LocalDateTime now = LocalDateTime.now();
        assertThrows(IllegalArgumentException.class, () -> {
            new WorkOrder("WO-999", "M-01", WorkOrder.WorkOrderType.BREAKDOWN,
                    WorkOrder.Priority.HIGH, WorkOrder.WorkOrderStatus.DONE,
                    now, now.minusHours(1));
        });
    }
}