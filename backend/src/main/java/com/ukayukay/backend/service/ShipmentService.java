package com.ukayukay.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ukayukay.backend.entity.Order;
import com.ukayukay.backend.entity.Shipment;
import com.ukayukay.backend.repository.OrderRepository;
import com.ukayukay.backend.repository.ShipmentRepository;
import com.ukayukay.backend.util.AppConstants;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    public ShipmentService(ShipmentRepository shipmentRepository, OrderRepository orderRepository,
                           NotificationService notificationService) {
        this.shipmentRepository = shipmentRepository;
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
    }

    public Shipment createShipment(Integer orderId, Shipment shipment) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException(AppConstants.ORDER_NOT_FOUND));

        shipment.setOrder(order);
        shipment.setStatus(shipment.getStatus() == null ? "PENDING" : shipment.getStatus().toUpperCase());
        shipment.setTrackingNumber(shipment.getTrackingNumber() == null ? generateTrackingNumber(orderId) : shipment.getTrackingNumber());
        shipment.setCreatedAt(LocalDateTime.now());
        shipment.setUpdatedAt(LocalDateTime.now());

        return shipmentRepository.save(shipment);
    }

    public Shipment updateShipmentStatus(Integer shipmentId, String status) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found with ID: " + shipmentId));

        shipment.setStatus(status.toUpperCase());
        if ("SHIPPED".equalsIgnoreCase(status)) {
            shipment.setShippedDate(LocalDateTime.now());
        } else if ("DELIVERED".equalsIgnoreCase(status)) {
            shipment.setDeliveredDate(LocalDateTime.now());
        }
        shipment.setUpdatedAt(LocalDateTime.now());
        Shipment savedShipment = shipmentRepository.save(shipment);

        if (savedShipment.getOrder() != null && savedShipment.getOrder().getCustomer() != null) {
            Integer customerId = savedShipment.getOrder().getCustomer().getUserId();
            notificationService.createNotification(customerId,
                    "SHIPMENT_UPDATED",
                    "Shipment " + savedShipment.getStatus(),
                    String.format("Your shipment for order #%d is now %s.", savedShipment.getOrder().getOrderId(), savedShipment.getStatus()),
                    savedShipment.getOrder().getOrderId(),
                    null);
        }

        return savedShipment;
    }

    public Shipment updateTrackingInfo(Integer shipmentId, Shipment request) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found with ID: " + shipmentId));

        if (request.getTrackingNumber() != null) {
            shipment.setTrackingNumber(request.getTrackingNumber());
        }
        if (request.getCourierCompany() != null) {
            shipment.setCourierCompany(request.getCourierCompany());
        }
        if (request.getShippingAddress() != null) {
            shipment.setShippingAddress(request.getShippingAddress());
        }
        if (request.getEstimatedDeliveryDate() != null) {
            shipment.setEstimatedDeliveryDate(request.getEstimatedDeliveryDate());
        }

        shipment.setUpdatedAt(LocalDateTime.now());
        return shipmentRepository.save(shipment);
    }

    public List<Shipment> getShipmentsByOrder(Integer orderId) {
        return shipmentRepository.findByOrder_OrderId(orderId);
    }

    private String generateTrackingNumber(Integer orderId) {
        return "TK" + orderId + System.currentTimeMillis();
    }
}
