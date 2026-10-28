package pl.edu.ug.cmms;

import org.springframework.boot.CommandLineRunner;

public class ReportRunner implements CommandLineRunner {

    private final ReportService reportService;

    public ReportRunner(ReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    public void run(String... args) throws Exception {
        reportService.generateAndSaveReport();
    }
    
}
