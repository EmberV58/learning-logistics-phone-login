package dev.learninglogistics.logistics;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class Shipment {
    public enum EventType { OUT_FOR_DELIVERY, DELIVERY_EXCEPTION, DELIVERED }
    public record Event(EventType type, Instant occurredAt, String note) {}
    public record ProofOfDelivery(String objectKey, String recipientName, Instant capturedAt) {}

    private final String shipmentId;
    private final List<Event> events = new ArrayList<>();
    private ProofOfDelivery proof;
    private boolean exceptionOpen;

    public Shipment(String shipmentId) { this.shipmentId = shipmentId; }

    public void record(Event event) {
        events.add(event);
        if (event.type() == EventType.DELIVERY_EXCEPTION) exceptionOpen = true;
    }

    public void attachProof(ProofOfDelivery proof) { this.proof = proof; }

    public Resolution closeException(boolean phoneVerified, Instant now) {
        if (!exceptionOpen) return new Resolution(false, "No open delivery exception");
        if (!phoneVerified) return new Resolution(false, "Driver phone is not verified");
        if (proof == null) return new Resolution(false, "Proof of delivery is required");
        exceptionOpen = false;
        record(new Event(EventType.DELIVERED, now, "Exception closed with " + proof.objectKey()));
        return new Resolution(true, shipmentId + " delivered to " + proof.recipientName());
    }

    public List<Event> events() { return List.copyOf(events); }
    public record Resolution(boolean closed, String summary) {}
}
