package com.example.order.infrastructure.adapter.outbound.persistence;

import com.example.order.application.port.outbound.OrderRepository;
import com.example.order.domain.model.aggregate.Order;
import com.example.order.domain.model.valueobject.OrderId;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * JPA implementation of OrderRepository port.
 */
@Component
public class JpaOrderRepository implements OrderRepository {

    private final SpringDataOrderRepository springDataRepository;
    private final OrderMapper mapper;

    public JpaOrderRepository(SpringDataOrderRepository springDataRepository, OrderMapper mapper) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public Order save(Order order) {
        Optional<OrderJpaEntity> existingEntity = springDataRepository
                .findByOrderId(order.getOrderId().value());

        OrderJpaEntity entity;
        if (existingEntity.isPresent()) {
            entity = existingEntity.get();
            mapper.updateEntity(entity, order);
        } else {
            entity = mapper.toEntity(order);
        }

        OrderJpaEntity savedEntity = springDataRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Order> findById(OrderId orderId) {
        return springDataRepository.findByOrderId(orderId.value())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Order> findByIdempotencyKey(String idempotencyKey) {
        return springDataRepository.findByIdempotencyKey(idempotencyKey)
                .map(mapper::toDomain);
    }
}
