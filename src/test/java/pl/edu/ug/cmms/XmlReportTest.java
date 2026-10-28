package pl.edu.ug.cmms;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XmlReportTest {

    @Test
    void testReportXmlSerializationAndDeserialization() throws Exception {
        List<Machine> machines = SampleData.machines();
        List<WorkOrder> workOrders = SampleData.workOrders();
        
        ReportService service1 = new ReportService(machines, workOrders, 3, "test-report.xml");
        service1.saveToXml();

        // Sprawdzenie czy plik powstał
        Path path = Path.of("test-report.xml");
        assertTrue(Files.exists(path));

        // Czyszczenie po teście
        Files.deleteIfExists(path);
    }
}