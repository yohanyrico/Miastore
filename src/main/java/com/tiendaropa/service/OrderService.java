package com.tiendaropa.service;

import com.tiendaropa.model.Order;
import com.tiendaropa.model.OrderItem;
import com.tiendaropa.model.Product;
import com.tiendaropa.model.User;
import com.tiendaropa.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @Transactional(readOnly = true)
    public List<Order> getUserOrders(String username) {
        User user = userService.getUserByUsername(username);
        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        
        // Forzar la inicialización de los items
        orders.forEach(order -> order.getItems().size());
        
        return orders;
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));
        
        // Forzar la inicialización de los items
        order.getItems().size();
        
        return order;
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        System.out.println("📊 Obteniendo todas las órdenes de la base de datos...");
        
        List<Order> orders = orderRepository.findAll();
        
        System.out.println("✅ Total de órdenes encontradas: " + orders.size());
        
        // IMPORTANTE: Forzar la carga de items y productos
        orders.forEach(order -> {
            System.out.println("  - Cargando items de orden " + order.getId());
            order.getItems().forEach(item -> {
                // Esto fuerza la carga del producto
                if (item.getProduct() != null) {
                    item.getProduct().getName();
                }
            });
        });
        
        System.out.println("✅ Todas las órdenes cargadas completamente");
        
        return orders;
    }

    @Transactional
    public Order createOrder(String username, Order order) {
        User user = userService.getUserByUsername(username);
        order.setUser(user);
        order.setStatus("PENDING");

        Double total = 0.0;

        for (OrderItem item : order.getItems()) {
            Long productId = item.getProductId(); 
            
            if (productId == null) {
                throw new RuntimeException("El productId no puede ser null");
            }
            
            Product product = productService.getProductById(productId)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + productId));

            if (product.getStock() < item.getQuantity()) {
                throw new RuntimeException("Stock insuficiente para: " + product.getName());
            }

            item.setProduct(product);
            item.setPrice(product.getPrice());
            item.setSubtotal(product.getPrice() * item.getQuantity());
            item.setOrder(order);

            total += item.getSubtotal();

            product.setStock(product.getStock() - item.getQuantity());
            productService.updateProduct(product.getId(), product);
        }

        order.setTotal(total);
        
        Order savedOrder = orderRepository.save(order);
        
        System.out.println("✅ Orden creada exitosamente: ID=" + savedOrder.getId() + 
                           ", Total=$" + savedOrder.getTotal() + 
                           ", Items=" + savedOrder.getItems().size());
        
        return savedOrder;
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = getOrderById(orderId);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(String status) {
        List<Order> orders = orderRepository.findByStatus(status);
        
        // Forzar la carga de items
        orders.forEach(order -> order.getItems().size());
        
        return orders;
    }
}