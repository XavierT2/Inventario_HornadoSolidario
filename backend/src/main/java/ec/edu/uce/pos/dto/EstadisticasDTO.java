package ec.edu.uce.pos.dto;

import java.math.BigDecimal;
import java.util.Map;

public class EstadisticasDTO {
    public BigDecimal totalCajaEfectivo;
    public Map<String, Integer> conteoProductos;
    public Map<String, EstadisticasCajeroDTO> estadisticasPorCajero;
    
    public EstadisticasDTO(BigDecimal totalCajaEfectivo, Map<String, Integer> conteoProductos, Map<String, EstadisticasCajeroDTO> estadisticasPorCajero) {
        this.totalCajaEfectivo = totalCajaEfectivo;
        this.conteoProductos = conteoProductos;
        this.estadisticasPorCajero = estadisticasPorCajero;
    }
}
