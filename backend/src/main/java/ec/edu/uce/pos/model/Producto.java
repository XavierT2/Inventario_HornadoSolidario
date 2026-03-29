package ec.edu.uce.pos.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "productos")
public class Producto extends PanacheEntityBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    
    public String nombre;
    
    public BigDecimal precio;
    
    public Producto() {}
    
    public Producto(String nombre, BigDecimal precio) {
        this.nombre = nombre;
        this.precio = precio;
    }
}
