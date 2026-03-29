package ec.edu.uce.pos.dto;

import java.math.BigDecimal;
import java.util.Map;

public class EstadisticasDTO {
    public BigDecimal totalCajaEfectivo;
    public Map<String, Integer> conteoProductos;
    
    public EstadisticasDTO(BigDecimal totalCajaEfectivo, Map<String, Integer> conteoProductos) {
        this.totalCajaEfectivo = totalCajaEfectivo;
        this.conteoProductos = conteoProductos;
    }
}
