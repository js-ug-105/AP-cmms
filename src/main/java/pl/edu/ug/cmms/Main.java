package pl.edu.ug.cmms;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Machine> machines = SampleData.machines();
        List<WorkOrder> workOrders = SampleData.workOrders();

        ReportService reportService = new ReportService(machines, workOrders);
        reportService.printReport();
    }
}