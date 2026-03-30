package ec.edu.uce.pos.dto;

import java.math.BigDecimal;

public class HistorialDetalleDTO {
    public Long id;
    public String productoNombre;
    public Integer cantidad;
    public BigDecimal precioUnitario;
    public BigDecimal subtotal;

    public HistorialDetalleDTO() {}

    public HistorialDetalleDTO(Long id, String productoNombre, Integer cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
        this.id = id;
        this.productoNombre = productoNombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }
}
