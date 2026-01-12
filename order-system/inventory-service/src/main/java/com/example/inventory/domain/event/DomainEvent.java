package com.example.inventory.domain.event;

import java.time.LocalDateTime;

/**
 * Base interface for all domain events.
 */
public interface DomainEvent {
    LocalDateTime occurredOn();
}
