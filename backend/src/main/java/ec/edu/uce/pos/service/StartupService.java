package ec.edu.uce.pos.service;

import ec.edu.uce.pos.model.Producto;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;

@ApplicationScoped
public class StartupService {

    @Inject
    EntityManager em;

    @Transactional
    public void init(@Observes StartupEvent event) {
        // Crear esquema nativamente para SQLite sin borrar datos (IF NOT EXISTS)
        em.createNativeQuery("CREATE TABLE IF NOT EXISTS productos (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre VARCHAR(255), precio DECIMAL(19,2))").executeUpdate();
        em.createNativeQuery("CREATE TABLE IF NOT EXISTS ventas (id INTEGER PRIMARY KEY AUTOINCREMENT, fechaHora TIMESTAMP, total DECIMAL(19,2), cajero VARCHAR(255), estado VARCHAR(50) DEFAULT 'PREPARANDO')").executeUpdate();
        em.createNativeQuery("CREATE TABLE IF NOT EXISTS detalles_venta (id INTEGER PRIMARY KEY AUTOINCREMENT, cantidad INTEGER, subtotal DECIMAL(19,2), producto_id INTEGER, venta_id INTEGER, FOREIGN KEY(producto_id) REFERENCES productos(id), FOREIGN KEY(venta_id) REFERENCES ventas(id))").executeUpdate();
        
        // Fallback for existing dbs that lack the estado column (preventing breaking schema updates):
        try {
            em.createNativeQuery("ALTER TABLE ventas ADD COLUMN estado VARCHAR(50) DEFAULT 'PREPARANDO'").executeUpdate();
        } catch (Exception e) {
            // Ignorar si la columna ya existe
        }

        if (Producto.count() == 0) {
            new Producto("Hornado (Boleto Entregado)", new BigDecimal("0.00")).persist();
            new Producto("Hornado (Venta Directa)", new BigDecimal("5.00")).persist();
            new Producto("Cerveza / Bebida", new BigDecimal("2.00")).persist();
            System.out.println("Base de datos inicializada con productos base.");
        }
    }
}
