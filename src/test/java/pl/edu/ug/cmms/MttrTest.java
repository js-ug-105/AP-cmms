package pl.edu.ug.cmms;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MttrTest {

    @Test
    void testMttrCalculation() {
        Machine m1 = new Machine("M-10", "Test Machine", Machine.MachineType.COMPRESSOR, "X", LocalDate.now());
        
        WorkOrder wo1 = new WorkOrder("WO-1", "M-10", WorkOrder.WorkOrderType.BREAKDOWN,
                WorkOrder.Priority.HIGH, WorkOrder.WorkOrderStatus.DONE,
                LocalDateTime.parse("2026-05-01T10:00"), LocalDateTime.parse("2026-05-01T14:00"));

        WorkOrder wo2 = new WorkOrder("WO-2", "M-10", WorkOrder.WorkOrderType.BREAKDOWN,
                WorkOrder.Priority.HIGH, WorkOrder.WorkOrderStatus.DONE,
                LocalDateTime.parse("2026-05-02T10:00"), LocalDateTime.parse("2026-05-02T16:00"));

        ReportService service = new ReportService(List.of(m1), List.of(wo1, wo2));
        Map<String, String> mttr = service.calculateMttrByHall();

        assertEquals("5 h 0 min", mttr.get("X"));
    }
}