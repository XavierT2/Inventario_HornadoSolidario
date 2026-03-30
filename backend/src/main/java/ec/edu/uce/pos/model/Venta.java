package ec.edu.uce.pos.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ventas")
public class Venta extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    
    public LocalDateTime fechaHora;
    
    public BigDecimal total;

    public String cajero;
    
    public String estado = "PREPARANDO";
    
    public Venta() {}
    
    public Venta(LocalDateTime fechaHora, BigDecimal total, String cajero) {
        this.fechaHora = fechaHora;
        this.total = total;
        this.cajero = cajero;
    }
}
