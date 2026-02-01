package com.tiendaropa.mapper;

import com.tiendaropa.dto.*;
import com.tiendaropa.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {
    
    public OrderDTO toDTO(Order order) {
        if (order == null) {
            return null;
        }
        
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setTotal(order.getTotal());
        dto.setStatus(order.getStatus());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setPhone(order.getPhone());
        dto.setCreatedAt(order.getCreatedAt());
        
        // Mapear usuario
        if (order.getUser() != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(order.getUser().getId());
            userDTO.setUsername(order.getUser().getUsername());
            userDTO.setEmail(order.getUser().getEmail());
            userDTO.setFullName(order.getUser().getFullName());
            dto.setUser(userDTO);
        }
        
        // Mapear items
        if (order.getItems() != null) {
            List<OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(this::toItemDTO)
                .collect(Collectors.toList());
            dto.setItems(itemDTOs);
        }
        
        return dto;
    }
    
    private OrderItemDTO toItemDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setSubtotal(item.getSubtotal());
        
        // Mapear producto
        if (item.getProduct() != null) {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setId(item.getProduct().getId());
            productDTO.setName(item.getProduct().getName());
            productDTO.setPrice(item.getProduct().getPrice());
            productDTO.setImageUrl(item.getProduct().getImageUrl());
            dto.setProduct(productDTO);
        }
        
        return dto;
    }
    
    public List<OrderDTO> toDTOList(List<Order> orders) {
        return orders.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
}