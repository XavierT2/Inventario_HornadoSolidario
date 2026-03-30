package ec.edu.uce.pos.resource;

import ec.edu.uce.pos.dto.EstadisticasDTO;
import ec.edu.uce.pos.dto.ProductoDTO;
import ec.edu.uce.pos.dto.VentaRequestDTO;
import ec.edu.uce.pos.dto.HistorialDTO;
import ec.edu.uce.pos.dto.HistorialDetalleDTO;
import ec.edu.uce.pos.model.DetalleVenta;
import ec.edu.uce.pos.model.Producto;
import ec.edu.uce.pos.model.Venta;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        venta.cajero = request.cajero != null && !request.cajero.trim().isEmpty() ? request.cajero : "Desconocido";
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
        Map<String, ec.edu.uce.pos.dto.EstadisticasCajeroDTO> estadisticasPorCajero = new HashMap<>();

        try {
            List<Object[]> resultados = DetalleVenta.getEntityManager()
                .createQuery("SELECT d.producto.nombre, SUM(d.cantidad), d.venta.cajero FROM DetalleVenta d WHERE d.producto IS NOT NULL GROUP BY d.producto.nombre, d.venta.cajero", Object[].class)
                .getResultList();

            for (Object[] fila : resultados) {
                if (fila[0] != null && fila[1] != null) {
                    String nombreProducto = (String) fila[0];
                    Integer candidad = ((Number) fila[1]).intValue();
                    String cajero = fila[2] != null ? (String) fila[2] : "Desconocido";
                    
                    // Sum Global
                    conteoProductos.put(nombreProducto, conteoProductos.getOrDefault(nombreProducto, 0) + candidad);
                    
                    // Sum Por Cajero
                    estadisticasPorCajero.putIfAbsent(cajero, new ec.edu.uce.pos.dto.EstadisticasCajeroDTO(BigDecimal.ZERO, new HashMap<>()));
                    ec.edu.uce.pos.dto.EstadisticasCajeroDTO cajaCajero = estadisticasPorCajero.get(cajero);
                    cajaCajero.conteoProductos.put(nombreProducto, cajaCajero.conteoProductos.getOrDefault(nombreProducto, 0) + candidad);
                }
            }
            
            // Calculate sum per cashier
            for (Venta v : ventas) {
                String cajero = v.cajero != null ? v.cajero : "Desconocido";
                estadisticasPorCajero.putIfAbsent(cajero, new ec.edu.uce.pos.dto.EstadisticasCajeroDTO(BigDecimal.ZERO, new HashMap<>()));
                ec.edu.uce.pos.dto.EstadisticasCajeroDTO cajaCajero = estadisticasPorCajero.get(cajero);
                cajaCajero.totalEfectivo = cajaCajero.totalEfectivo.add(v.total);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        return new EstadisticasDTO(totalCaja, conteoProductos, estadisticasPorCajero);
    }

    @DELETE
    @Path("/cajeros/{cajero}")
    @Transactional
    public Response eliminarCajero(@PathParam("cajero") String cajero) {
        if (cajero == null || cajero.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Cajero inválido").build();
        }

        try {
            // First destroy all DetalleVenta relationships for this cajero to avoid ConstraintViolation
            DetalleVenta.getEntityManager()
                .createQuery("DELETE FROM DetalleVenta d WHERE d.venta.id IN (SELECT v.id FROM Venta v WHERE v.cajero = :cajero)")
                .setParameter("cajero", cajero)
                .executeUpdate();
                
            // Then manually delete the parent Venta entity
            long eliminados = Venta.delete("cajero", cajero);
            
            return Response.ok("Se han eliminado " + eliminados + " ventas de " + cajero).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.serverError().entity("Error eliminando cajero: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/cajeros/{cajero}/historial")
    public List<HistorialDTO> getHistorialCajero(@PathParam("cajero") String cajero) {
        List<Venta> ventas = Venta.find("cajero = ?1 ORDER BY fechaHora DESC", cajero).page(0, 20).list();
        return ventas.stream().map(v -> {
            List<DetalleVenta> dLista = DetalleVenta.find("venta = ?1", v).list();
            List<HistorialDetalleDTO> dets = dLista.stream().map(d -> 
                new HistorialDetalleDTO(d.id, d.producto.nombre, d.cantidad, d.producto.precio, d.subtotal)
            ).collect(Collectors.toList());
            return new HistorialDTO(v.id, v.fechaHora, v.total, dets);
        }).collect(Collectors.toList());
    }

    @GET
    @Path("/ventas/cocina")
    public List<HistorialDTO> getVentasCocina() {
        // Traer todas las ventas pendientes de todas las cajas
        List<Venta> ventas = Venta.find("estado = ?1 ORDER BY id ASC", "PREPARANDO").list();
        return ventas.stream().map(v -> {
            List<DetalleVenta> dLista = DetalleVenta.find("venta = ?1", v).list();
            List<HistorialDetalleDTO> dets = dLista.stream().map(d -> 
                new HistorialDetalleDTO(d.id, d.producto.nombre, d.cantidad, d.producto.precio, d.subtotal)
            ).collect(Collectors.toList());
            return new HistorialDTO(v.id, v.fechaHora, v.total, dets);
        }).collect(Collectors.toList());
    }

    @PUT
    @Transactional
    @Path("/ventas/{id}/despachar")
    public Response despacharVenta(@PathParam("id") Long id) {
        Venta venta = Venta.findById(id);
        if (venta != null) {
            venta.estado = "DESPACHADO";
            venta.persist();
            return Response.ok("Orden " + id + " lista para entrega").build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @PUT
    @Transactional
    @Path("/detalle/{id}/anular/{qty}")
    public Response anularUnidadDeDetalle(@PathParam("id") Long id, @PathParam("qty") Integer qty) {
        DetalleVenta dv = DetalleVenta.findById(id);
        if (dv == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Detalle no encontrado").build();
        }
        if (qty <= 0 || qty > dv.cantidad) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Cantidad a anular inválida").build();
        }
        
        Venta venta = dv.venta;
        BigDecimal precioU = dv.producto.precio;
        BigDecimal descuento = precioU.multiply(new BigDecimal(qty));

        if (dv.cantidad > qty) {
            dv.cantidad -= qty;
            dv.subtotal = dv.subtotal.subtract(descuento);
            venta.total = venta.total.subtract(descuento);
            dv.persist();
            venta.persist();
        } else {
            venta.total = venta.total.subtract(descuento);
            venta.persist();
            dv.delete();
            
            long remaining = DetalleVenta.count("venta = ?1", venta);
            if (remaining == 0) {
                venta.delete();
            }
        }
        
        return Response.ok("Anulación parcial procesada").build();
    }
}
