package pl.edu.ug.cmms;

import java.time.LocalDate;
import java.util.Objects;

public class Machine {

    public enum MachineType {
        CNC_LATHE,
        CONVEYOR,
        MILLING_MACHINE,
        WELDING_ROBOT,
        INJECTION_MOLDER,
        HYDRAULIC_PRESS,
        COMPRESSOR
    }

    private final String id;
    private final String name;
    private final MachineType type;
    private final String hall;
    private final LocalDate installationDate;

    public Machine(String id, String name, MachineType type, String hall, LocalDate installationDate) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Machine ID cannot be null or blank");
        }
        this.id = id;
        this.name = name;
        this.type = type;
        this.hall = hall;
        this.installationDate = installationDate;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public MachineType getType() {
        return type;
    }

    public String getHall() {
        return hall;
    }

    public LocalDate getInstallationDate() {
        return installationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Machine machine = (Machine) o;
        return Objects.equals(id, machine.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}