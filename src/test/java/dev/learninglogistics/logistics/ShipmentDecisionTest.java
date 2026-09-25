package dev.learninglogistics.logistics;

import java.time.Instant;

public final class ShipmentDecisionTest {
    public static void main(String[] args) {
        Instant lessonTime = Instant.parse("2026-09-23T09:00:00Z");
        Shipment shipment = new Shipment("SHIP-LESSON-7");
        shipment.record(new Shipment.Event(Shipment.EventType.DELIVERY_EXCEPTION, lessonTime, "School office closed"));

        assertFalse(shipment.closeException(true, lessonTime).closed(), "proof is required");
        shipment.attachProof(new Shipment.ProofOfDelivery("pod/SHIP-LESSON-7.jpg", "M. Chen", lessonTime));
        assertFalse(shipment.closeException(false, lessonTime).closed(), "verified phone is required");
        assertTrue(shipment.closeException(true, lessonTime).closed(), "both facts close the exception");
        assertTrue(shipment.events().get(1).type() == Shipment.EventType.DELIVERED, "delivery event is observable");
        System.out.println("PASS: verified driver plus proof closes the delivery exception");
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static void assertFalse(boolean value, String message) { assertTrue(!value, message); }
}
