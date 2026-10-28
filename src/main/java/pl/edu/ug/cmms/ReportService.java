package pl.edu.ug.cmms;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ReportService {

    private final List<Machine> machines;
    private final List<WorkOrder> workOrders;

    public ReportService(List<Machine> machines, List<WorkOrder> workOrders) {
        this.machines = machines;
        this.workOrders = workOrders;
    }

    public void printReport() {
        System.out.println("Liczba maszyn: " + machines.size());
        System.out.println("Liczba zleceń: " + workOrders.size());

        System.out.println("\nNiezakończone zlecenia według priorytetu:");
        // pobiera status -> filtruje po niezakończonych (OPEN i IN_PROGRESS) -> grupuje po priorytecie i liczy
        Map<WorkOrder.Priority, Long> unfinishedByPriority = workOrders.stream()
                .filter(wo -> wo.getStatus() == WorkOrder.WorkOrderStatus.OPEN || wo.getStatus() == WorkOrder.WorkOrderStatus.IN_PROGRESS)
                .collect(Collectors.groupingBy(WorkOrder::getPriority, Collectors.counting()));

        for (WorkOrder.Priority p : WorkOrder.Priority.values()) {
            long count = unfinishedByPriority.getOrDefault(p, 0L);
            System.out.println("- " + p + ": " + count);
        }

        
        System.out.println("\nRanking awarii (Top 3):");
        // pobiera awarie (BREAKDOWN i status CANCELLED) -> grupuje po id maszyny i liczy iloSC awarii
        Map<String, Long> breakdownCounts = workOrders.stream()
                .filter(wo -> wo.getType() == WorkOrder.WorkOrderType.BREAKDOWN)
                .filter(wo -> wo.getStatus() != WorkOrder.WorkOrderStatus.CANCELLED)
                .collect(Collectors.groupingBy(WorkOrder::getMachineId, Collectors.counting()));

        // Sortuje maszyny malejąco wg. ilości awarii -> tylko 3 pierwsze wyniki
        List<Map.Entry<String, Long>> topBreakdowns = breakdownCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry::getKey))
                .limit(3)
                .toList();

        for (Map.Entry<String, Long> entry : topBreakdowns) {
            System.out.println("- " + entry.getKey() + " (" + entry.getValue() + ")");
        }

        System.out.println("\nMTTR hal:");
        Map<String, String> mttrByHall = calculateMttrByHall();
        for (String hall : mttrByHall.keySet().stream().sorted().toList()) {
            System.out.println("- Hala " + hall + ": " + mttrByHall.get(hall));
        }
    }

    public Map<String, String> calculateMttrByHall() {
        Map<String, String> result = new TreeMap<>();
        Map<String, Machine> machineMap = machines.stream()
                .collect(Collectors.toMap(Machine::getId, m -> m));

        Map<String, List<Duration>> hallDurations = new HashMap<>();

        // tylko zakończone breakdowny -> java.time liczy czas trwania
        for (WorkOrder wo : workOrders) {
            if (wo.getType() == WorkOrder.WorkOrderType.BREAKDOWN && wo.getStatus() == WorkOrder.WorkOrderStatus.DONE) {
                Machine m = machineMap.get(wo.getMachineId());
                if (m != null) {
                    Duration duration = Duration.between(wo.getReportedAt(), wo.getClosedAt());
                    hallDurations.computeIfAbsent(m.getHall(), k -> new ArrayList<>()).add(duration);
                }
            }
        }
        // przygotowanie danych do wypisania
        for (Map.Entry<String, List<Duration>> entry : hallDurations.entrySet()) {
            List<Duration> durations = entry.getValue();
            if (durations.isEmpty()) {
                result.put(entry.getKey(), "0 h 0 min");
                continue;
            }
            long totalMinutes = durations.stream().mapToLong(Duration::toMinutes).sum();
            long avgMinutes = totalMinutes / durations.size();
            long hours = avgMinutes / 60;
            long minutes = avgMinutes % 60;
            result.put(entry.getKey(), hours + " h " + minutes + " min");
        }

        return result;
    }
}