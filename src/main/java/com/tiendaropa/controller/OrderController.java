package com.tiendaropa.controller;

import com.tiendaropa.model.Order;
import com.tiendaropa.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    // Obtener órdenes del usuario autenticado
    @GetMapping("/my-orders")
    public ResponseEntity<List<Order>> getMyOrders() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            List<Order> orders = orderService.getUserOrders(username);
            return ResponseEntity.ok(orders);
            
        } catch (Exception e) {
            System.err.println("Error al obtener órdenes del usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Obtener TODAS las órdenes (solo para admin)
    @GetMapping("/all")
    public ResponseEntity<?> getAllOrders() {
        try {
            System.out.println("=================================");
            System.out.println("🔍 INICIO - getAllOrders()");
            System.out.println("=================================");
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("✅ Usuario autenticado: " + authentication.getName());
            
            // Verificar que el usuario tenga rol ADMIN
            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            System.out.println("✅ Es admin: " + isAdmin);
            
            if (!isAdmin) {
                System.out.println("❌ Usuario NO es admin, denegando acceso");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"message\": \"No tienes permisos para ver todas las órdenes\"}");
            }
            
            System.out.println("📊 Llamando a orderService.getAllOrders()...");
            List<Order> orders = orderService.getAllOrders();
            
            System.out.println("✅ Órdenes obtenidas: " + orders.size());
            for (Order order : orders) {
                System.out.println("  - Orden " + order.getId() + " (" + order.getOrderNumber() + "): " + 
                                   order.getItems().size() + " items");
            }
            
            System.out.println("=================================");
            System.out.println("✅ FIN - Retornando órdenes");
            System.out.println("=================================");
            
            return ResponseEntity.ok(orders);
            
        } catch (Exception e) {
            System.err.println("=================================");
            System.err.println("❌ ERROR CRÍTICO en getAllOrders()");
            System.err.println("❌ Tipo: " + e.getClass().getName());
            System.err.println("❌ Mensaje: " + e.getMessage());
            System.err.println("❌ Stack trace:");
            e.printStackTrace();
            System.err.println("=================================");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"message\": \"Error al cargar las órdenes: " + e.getMessage() + "\"}");
        }
    }
    
    // Obtener una orden específica por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id);
            
            // Verificar que el usuario autenticado sea el dueño de la orden o admin
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            if (!order.getUser().getUsername().equals(username) && !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"message\": \"No tienes permiso para ver esta orden\"}");
            }
            
            return ResponseEntity.ok(order);
            
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Error al obtener orden: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"message\": \"Error al obtener la orden\"}");
        }
    }
    
    // Crear una nueva orden
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            System.out.println("📦 Creando orden para usuario: " + username);
            System.out.println("📦 Datos recibidos: " + order.getItems().size() + " items");
            
            // Validaciones
            if (order.getItems() == null || order.getItems().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body("{\"message\": \"La orden debe tener al menos un producto\"}");
            }
            
            if (order.getShippingAddress() == null || order.getShippingAddress().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body("{\"message\": \"La dirección de envío es requerida\"}");
            }
            
            if (order.getPhone() == null || order.getPhone().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body("{\"message\": \"El teléfono es requerido\"}");
            }
            
            Order createdOrder = orderService.createOrder(username, order);
            System.out.println("✅ Orden creada exitosamente con ID: " + createdOrder.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
            
        } catch (RuntimeException e) {
            System.err.println("❌ Error de negocio al crear orden: " + e.getMessage());
            return ResponseEntity.badRequest()
                .body("{\"message\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            System.err.println("❌ Error al crear orden: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"message\": \"Error al crear la orden: " + e.getMessage() + "\"}");
        }
    }
    
    // NUEVO: Actualizar estado de una orden (solo admin)
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            System.out.println("=================================");
            System.out.println("🔄 Actualizando estado de orden");
            System.out.println("🔄 ID: " + id);
            System.out.println("🔄 Nuevo estado: " + request.get("estado"));
            System.out.println("=================================");
            
            // Verificar que el usuario tenga rol ADMIN
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin) {
                System.out.println("❌ Usuario NO es admin, denegando acceso");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"message\": \"No tienes permisos para actualizar órdenes\"}");
            }
            
            String nuevoEstado = request.get("estado");
            
            // Validar que el estado sea válido
            if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body("{\"message\": \"El estado es requerido\"}");
            }
            
            // Mapear estados del frontend a los del backend
            String estadoBackend;
            switch (nuevoEstado) {
                case "Pendiente":
                    estadoBackend = "PENDING";
                    break;
                case "En Preparación":
                    estadoBackend = "PREPARING";
                    break;
                case "En Despacho":
                    estadoBackend = "SHIPPED";
                    break;
                case "Entregado":
                    estadoBackend = "DELIVERED";
                    break;
                default:
                    return ResponseEntity.badRequest()
                        .body("{\"message\": \"Estado no válido: " + nuevoEstado + "\"}");
            }
            
            Order updatedOrder = orderService.updateOrderStatus(id, estadoBackend);
            System.out.println("✅ Estado actualizado correctamente");
            
            return ResponseEntity.ok(updatedOrder);
            
        } catch (RuntimeException e) {
            System.err.println("❌ Error: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("❌ Error al actualizar estado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"message\": \"Error al actualizar el estado: " + e.getMessage() + "\"}");
        }
    }
    
    // Cancelar una orden (solo si está en estado PENDING)
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            Order order = orderService.getOrderById(id);
            
            // Verificar que el usuario sea el dueño de la orden
            if (!order.getUser().getUsername().equals(username)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"message\": \"No tienes permiso para cancelar esta orden\"}");
            }
            
            // Solo se puede cancelar si está pendiente
            if (!"PENDING".equals(order.getStatus())) {
                return ResponseEntity.badRequest()
                    .body("{\"message\": \"Solo se pueden cancelar órdenes pendientes\"}");
            }
            
            Order canceledOrder = orderService.updateOrderStatus(id, "CANCELLED");
            return ResponseEntity.ok(canceledOrder);
            
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Error al cancelar orden: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"message\": \"Error al cancelar la orden\"}");
        }
    }
    
    // Obtener órdenes por estado
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getOrdersByStatus(@PathVariable String status) {
        try {
            // Verificar si es admin
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            
            if (!isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("{\"message\": \"No tienes permisos para esta operación\"}");
            }
            
            List<Order> orders = orderService.getOrdersByStatus(status);
            return ResponseEntity.ok(orders);
            
        } catch (Exception e) {
            System.err.println("Error al obtener órdenes por estado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}