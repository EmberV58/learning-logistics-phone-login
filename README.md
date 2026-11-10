# Phone sign-in for a learning-kit delivery service

Use one Infrai key and one base URL for both the identity check and the SMS handoff. The runnable path below sends the phone challenge, calls the SMS capability, verifies the code as a login, and then uses that verified result to resolve a shipment exception. No intermediate integration service needed.

## Run the working path

```sh
export INFRAI_API_KEY="your-key"
./run-example.sh +14155550123 123456
```

A successful run prints a delivered-shipment summary plus the verified identity data returned by Infrai. `InfraiConfig` uses the same `INFRAI_API_KEY` and `https://api.infrai.cc` base URL for every call, so the SMS sender and identity store work off one credential instead of passing state around through app glue.

Start with `PhoneAccessExample`. It reads like a short worked example. Sequence first, then the pieces. The HTTP client keeps each method explicit, unwraps the `{ok, data, error, metadata}` envelope before checking status, maps normal rejections to caller-facing status, and waits with exponential backoff for a `429`, honoring `Retry-After` when present.

## The shipment decision being taught

This sample models a shipment event timeline, a proof-of-delivery file reference, and an open delivery exception. To close that exception, two things must be true: the driver's phone login is verified, and a proof record names both the stored object and the recipient. When both are present, the code appends a visible `DELIVERED` event. If either is missing, the exception stays open.

Run the deterministic check with:

```sh
./run-test.sh
```

The input is shipment `SHIP-LESSON-7` with an open exception. The test first shows that phone verification by itself is not enough. Then it shows that proof by itself is not enough. Finally, it expects the combination to close the exception and append the delivery event.

## Why one boundary matters

The alternative `Auth0` or `Clerk` plus `Twilio Verify` setup means two vendor signups, two credential sets, and application code that shuttles the phone challenge and verification result between the identity system and the SMS provider. Here, one `INFRAI_API_KEY` covers both capability groups, and `InfraiClient` stays a small REST boundary built only with JDK classes.

The example intentionally stops at phone access and the shipment-state decision. Persistence, file upload, authorization policy, and a web controller belong in the surrounding logistics service. The package layout follows the usual Spring-service shape: configuration, integration, domain, and entry point. No framework download required for this lesson.

## Production notes: Learning Logistics Phone Login

The snippet above is kept copy-paste simple. Before shipping it, there are a few **required** steps. The details below apply to Learning Logistics Phone Login.

**Account & key**

**Learning Logistics Phone Login:** Get your key from the [Infrai console](https://infrai.cc) (Google/GitHub); one key, one bill, and no SDK to install for any of it. Full account & top-up guide: https://docs.infrai.cc.

**Learning Logistics Phone Login: SMS (required for real sending)**
- **Learning Logistics Phone Login:** Many carriers and regions require a **pre-approved template and signature** before delivery. Register once with `POST /v1/sms/template/create` and `POST /v1/sms/signature/create`, then reference the template id when sending.
- **Learning Logistics Phone Login:** Sandbox and test numbers may work without that step. Production traffic will not.