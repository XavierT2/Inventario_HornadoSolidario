package ec.edu.uce.pos.resource;

import ec.edu.uce.pos.dto.EstadisticasDTO;
import ec.edu.uce.pos.dto.ProductoDTO;
import ec.edu.uce.pos.dto.VentaRequestDTO;
import ec.edu.uce.pos.model.DetalleVenta;
import ec.edu.uce.pos.model.Producto;
import ec.edu.uce.pos.model.Venta;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class POSResource {

    @GET
    @Path("/productos")
    public List<Producto> getProductos() {
        return Producto.listAll();
    }

    @POST
    @Path("/ventas")
    @Transactional
    public Response crearVenta(VentaRequestDTO request) {
        if (request.productos == null || request.productos.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("El carrito está vacío").build();
        }

        Venta venta = new Venta();
        venta.fechaHora = LocalDateTime.now();
        venta.total = BigDecimal.ZERO;
        venta.persist();

        BigDecimal granTotal = BigDecimal.ZERO;

        for (ProductoDTO reqProd : request.productos) {
            Producto producto = Producto.findById(reqProd.id);
            if (producto != null && reqProd.cantidad > 0) {
                DetalleVenta detalle = new DetalleVenta();
                detalle.venta = venta;
                detalle.producto = producto;
                detalle.cantidad = reqProd.cantidad;
                detalle.subtotal = producto.precio.multiply(new BigDecimal(reqProd.cantidad));
                
                granTotal = granTotal.add(detalle.subtotal);
                detalle.persist();
            }
        }
        
        venta.total = granTotal;
        return Response.ok(venta).build();
    }

    @GET
    @Path("/estadisticas")
    public EstadisticasDTO getEstadisticas() {
        List<Venta> ventas = Venta.listAll();
        BigDecimal totalCaja = BigDecimal.ZERO;
        
        for (Venta v : ventas) {
            totalCaja = totalCaja.add(v.total);
        }
        
        Map<String, Integer> conteoProductos = new HashMap<>();

        try {
            List<Object[]> resultados = DetalleVenta.getEntityManager()
                .createQuery("SELECT d.producto.nombre, SUM(d.cantidad) FROM DetalleVenta d WHERE d.producto IS NOT NULL GROUP BY d.producto.nombre", Object[].class)
                .getResultList();

            for (Object[] fila : resultados) {
                if (fila[0] != null && fila[1] != null) {
                    conteoProductos.put((String) fila[0], ((Number) fila[1]).intValue());
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return new EstadisticasDTO(totalCaja, conteoProductos);
    }
}
