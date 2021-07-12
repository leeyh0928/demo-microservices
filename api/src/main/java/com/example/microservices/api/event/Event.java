package com.example.microservices.api.event;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event<K, T> {
    public enum Type {CREATE, DELETE}

    private Event.Type eventType;
    private K key;
    private T data;
    private LocalDateTime eventCreatedAt;

    @Builder
    public Event(Event.Type eventType, K key, T data) {
        this.eventType = eventType;
        this.key = key;
        this.data = data;
        this.eventCreatedAt = LocalDateTime.now();
    }
}
