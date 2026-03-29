package ec.edu.uce.pos.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "detalles_venta")
public class DetalleVenta extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id")
    @JsonIgnore
    public Venta venta;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id")
    public Producto producto;
    
    public Integer cantidad;
    
    public BigDecimal subtotal;
}
