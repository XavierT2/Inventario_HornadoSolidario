package ec.edu.uce.pos.dto;

import java.math.BigDecimal;
import java.util.Map;

public class EstadisticasCajeroDTO {
    public BigDecimal totalEfectivo;
    public Map<String, Integer> conteoProductos;
    
    public EstadisticasCajeroDTO(BigDecimal totalEfectivo, Map<String, Integer> conteoProductos) {
        this.totalEfectivo = totalEfectivo;
        this.conteoProductos = conteoProductos;
    }
}
