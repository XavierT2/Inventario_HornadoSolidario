package ec.edu.uce.pos.dto;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistorialDTO {
    public Long id;
    public String fecha;
    public BigDecimal total;
    public List<HistorialDetalleDTO> detalles;

    public HistorialDTO() {}

    public HistorialDTO(Long id, java.time.LocalDateTime fechaHora, BigDecimal total, List<HistorialDetalleDTO> detalles) {
        this.id = id;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm");
        if (fechaHora != null) {
            this.fecha = fechaHora.format(formatter);
        } else {
            this.fecha = "Sin fecha";
        }
        this.total = total;
        this.detalles = detalles;
    }
}
