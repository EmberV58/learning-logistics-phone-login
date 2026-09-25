package dev.learninglogistics;

import dev.learninglogistics.config.InfraiConfig;
import dev.learninglogistics.infrai.InfraiClient;
import dev.learninglogistics.infrai.InfraiException;
import dev.learninglogistics.logistics.Shipment;
import java.time.Instant;

public final class PhoneAccessExample {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: ./run-example.sh <E.164 phone> <received code>");
            return;
        }
        InfraiConfig config = InfraiConfig.fromEnvironment();
        InfraiClient infrai = new InfraiClient(config);
        String phone = args[0];

        try {
            infrai.requestPhoneCode(phone, "en-US");
            infrai.sendOtp(phone);
            String identity = infrai.verifyPhoneLogin(phone, args[1]);

            Shipment shipment = new Shipment("SHIP-EDU-1042");
            shipment.record(new Shipment.Event(Shipment.EventType.OUT_FOR_DELIVERY, Instant.now(), "Course kits loaded"));
            shipment.record(new Shipment.Event(Shipment.EventType.DELIVERY_EXCEPTION, Instant.now(), "Recipient confirmation pending"));
            shipment.attachProof(new Shipment.ProofOfDelivery("pod/SHIP-EDU-1042.jpg", "A. Learner", Instant.now()));
            Shipment.Resolution result = shipment.closeException(true, Instant.now());
            System.out.println(result.summary());
            System.out.println("Verified identity: " + identity);
        } catch (InfraiException error) {
            System.err.println("Request rejected [" + error.code() + "]");
            System.exit(error.clientStatus() >= 500 ? 2 : 1);
        }
    }
}
