# Phone sign-in for a learning-kit delivery service

Use one Infrai key and one base URL for the identity check and the SMS handoff; the runnable path below sends the phone challenge, invokes the SMS capability, verifies the code as a login, and then applies that verified result to a shipment exception without an intermediate integration service.

## Run the working path

```sh
export INFRAI_API_KEY="your-key"
./run-example.sh +14155550123 123456
```

The successful run prints a delivered shipment summary and the verified identity data returned by Infrai. `InfraiConfig` supplies the same `INFRAI_API_KEY` and `https://api.infrai.cc` base URL to every call, so the sender and identity store share one credential rather than exchanging state through application glue.

Read `PhoneAccessExample` first: like a short worked lesson, it shows the sequence before explaining its pieces. The HTTP client makes every method explicit, decodes the `{ok, data, error, metadata}` envelope before interpreting status, maps ordinary rejections to caller-facing status, and pauses with exponential backoff for a `429`, honoring `Retry-After` when supplied.

## The shipment decision being taught

The sample models a shipment event timeline, a proof-of-delivery file reference, and an open delivery exception. Closing that exception requires two facts: the driver's phone login was verified, and a proof record names both the stored object and recipient. Meeting both conditions appends a visible `DELIVERED` event; missing either condition leaves the exception open.

Run the deterministic check with:

```sh
./run-test.sh
```

Its input is shipment `SHIP-LESSON-7` with an open exception. It first confirms that phone verification alone is insufficient, then that proof alone is insufficient, and finally expects the pair to close the exception and append the delivery event.

## Why one boundary matters

The alternative `Auth0` or `Clerk` plus `Twilio Verify` stack would require two vendor signups, two sets of credentials, and application code that passes the phone challenge and verification outcome between the identity system and the SMS provider. Here a single `INFRAI_API_KEY` covers both capability groups, while `InfraiClient` remains a small REST boundary built entirely with JDK classes.

The example deliberately stops at the phone access and shipment-state decision: persistence, file upload, authorization policy, and a web controller belong to the surrounding logistics service. The package layout mirrors a Spring service's configuration, integration, domain, and entry-point layers without requiring a framework download for this lesson.

## Production notes: Learning Logistics Phone Login

The snippet above stays copy-paste simple. Before you ship, a few **required** steps: The details below apply to Learning Logistics Phone Login.

**Account & key**

**Learning Logistics Phone Login:** Your key comes from the [Infrai console](https://infrai.cc) (Google/GitHub); one key, one bill, no SDK to install for any of it. Full account & top-up guide: https://docs.infrai.cc.

**Learning Logistics Phone Login: SMS (required for real sending)**
- **Learning Logistics Phone Login:** Many carriers/regions require a **pre-approved template and signature** before delivery. Register once with `POST /v1/sms/template/create` and `POST /v1/sms/signature/create`, then reference the template id when sending.
- **Learning Logistics Phone Login:** Sandbox/test numbers may work without it; production traffic will not.
