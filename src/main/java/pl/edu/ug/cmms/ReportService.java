package pl.edu.ug.cmms;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ReportService {

    private final List<Machine> machines;
    private final List<WorkOrder> workOrders;
    private final int top;
    private final String filePath;

    // Konstruktor przyjmujący parametry wstrzykiwane z beans.xml / application.yml
    public ReportService(List<Machine> machines, List<WorkOrder> workOrders, int top, String filePath) {
        this.machines = machines;
        this.workOrders = workOrders;
        this.top = top;
        this.filePath = filePath;
    }

    public void generateAndSaveReport() {
        printReport();
        saveToXml();
    }

    public void printReport() {
        System.out.println("Liczba maszyn: " + machines.size());
        System.out.println("Liczba zleceń: " + workOrders.size());

        System.out.println("\nNiezakończone zlecenia według priorytetu:");
        // pobiera status -> filtruje po niezakończonych (OPEN i IN_PROGRESS) -> grupuje po priorytecie i liczy
        Map<WorkOrder.Priority, Long> unfinishedByPriority = getUnfinishedByPriority();
        for (WorkOrder.Priority p : WorkOrder.Priority.values()) {
            long count = unfinishedByPriority.getOrDefault(p, 0L);
            System.out.println("- " + p + ": " + count);
        }

        System.out.println("\nRanking awarii (Top " + top + "):");
        List<Map.Entry<String, Long>> topBreakdowns = getTopBreakdowns();
        for (Map.Entry<String, Long> entry : topBreakdowns) {
            System.out.println("- " + entry.getKey() + " (" + entry.getValue() + ")");
        }

        System.out.println("\nMTTR hal:");
        Map<String, String> mttrByHall = calculateMttrByHall();
        for (Map.Entry<String, String> entry : mttrByHall.entrySet()) {
            System.out.println("- Hala " + entry.getKey() + ": " + entry.getValue());
        }
    }

    // wcześniejsze mapowanie teraz jako funkcje:

    // pobiera status -> filtruje po niezakończonych (OPEN i IN_PROGRESS) -> grupuje po priorytecie i liczy
    public Map<WorkOrder.Priority, Long> getUnfinishedByPriority() {
        return workOrders.stream()
                .filter(wo -> wo.getStatus() == WorkOrder.WorkOrderStatus.OPEN 
                           || wo.getStatus() == WorkOrder.WorkOrderStatus.IN_PROGRESS)
                .collect(Collectors.groupingBy(WorkOrder::getPriority, Collectors.counting()));
    }

    // pobiera awarie (BREAKDOWN i status CANCELLED) -> grupuje po id maszyny i liczy iloSC awarii
    public List<Map.Entry<String, Long>> getTopBreakdowns() {
        Map<String, Long> breakdownCounts = workOrders.stream()
                .filter(wo -> wo.getType() == WorkOrder.WorkOrderType.BREAKDOWN)
                .filter(wo -> wo.getStatus() != WorkOrder.WorkOrderStatus.CANCELLED)
                .collect(Collectors.groupingBy(WorkOrder::getMachineId, Collectors.counting()));

         // Sortuje maszyny malejąco wg. ilości awarii -> tylko 3 pierwsze wyniki
        return breakdownCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry::getKey))
                .limit(top)
                .toList();
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

    public void saveToXml() {
        try {
            // tworzy brakujące katalogi w ścieżce
            Path path = Paths.get(filePath);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            // inicjalizacja DOM Document
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            // element główny <report>
            Element rootElement = doc.createElement("report");
            doc.appendChild(rootElement);

            // liczba maszyn
            Element machinesElem = doc.createElement("machinesCount");
            machinesElem.setTextContent(String.valueOf(machines.size()));
            rootElement.appendChild(machinesElem);

            // liczba zleceń
            Element ordersElem = doc.createElement("workOrdersCount");
            ordersElem.setTextContent(String.valueOf(workOrders.size()));
            rootElement.appendChild(ordersElem);

            // niezakończone zlecenia według priorytetu
            Element unfinishedElem = doc.createElement("unfinishedWorkOrders");
            Map<WorkOrder.Priority, Long> unfinished = getUnfinishedByPriority();
            for (WorkOrder.Priority p : WorkOrder.Priority.values()) {
                Element pElem = doc.createElement(p.name().toLowerCase());
                pElem.setTextContent(String.valueOf(unfinished.getOrDefault(p, 0L)));
                unfinishedElem.appendChild(pElem);
            }
            rootElement.appendChild(unfinishedElem);

            // ranking awarii jako elementy <machine id="..." breakdowns="..."/>
            Element rankingElem = doc.createElement("breakdownRanking");
            List<Map.Entry<String, Long>> topBreakdowns = getTopBreakdowns();
            for (Map.Entry<String, Long> entry : topBreakdowns) {
                Element machineElem = doc.createElement("machine");
                machineElem.setAttribute("id", entry.getKey());
                machineElem.setAttribute("breakdowns", String.valueOf(entry.getValue()));
                rankingElem.appendChild(machineElem);
            }
            rootElement.appendChild(rankingElem);

            // MTTR każdej hali
            Element mttrElem = doc.createElement("hallMttr");
            Map<String, String> mttr = calculateMttrByHall();
            for (Map.Entry<String, String> entry : mttr.entrySet()) {
                Element hallElem = doc.createElement("hall");
                hallElem.setAttribute("name", entry.getKey());
                hallElem.setTextContent(entry.getValue());
                mttrElem.appendChild(hallElem);
            }
            rootElement.appendChild(mttrElem);

            // zapis do pliku XML z wcięciami (indentation)
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(path.toFile());
            transformer.transform(source, result);

        } catch (Exception e) {
            throw new RuntimeException("Nie udało się zapisać raportu do pliku XML", e);
        }
    }
}