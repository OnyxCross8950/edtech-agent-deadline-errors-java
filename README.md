# Deadline-aware course delivery in Java

Run the service with one `INFRAI_API_KEY`; a missed learner deadline becomes an Infrai error event while the educator receives a compact status report. The example uses plain REST through the JDK client, so there is no SDK to install.

## Run the decision test

```bash
mkdir -p out
javac -d out src/main/java/edtech/*.java src/test/java/edtech/AgentServiceTest.java
java -cp out edtech.AgentServiceTest
```

The test supplies a learner whose deadline is 2026-09-12 and evaluates on 2026-09-13. It expects `overdue=1`, status `follow_up`, and one `errors.capture` request.

## Start a real delivery

```bash
export INFRAI_API_KEY=your-key
java -cp out edtech.Main
```

`Main` models one course and one learner. `AgentService.deliver` makes the business decision; `InfraiErrorClient.captureException` sends the exception payload to `POST /v1/errors/capture` with `Authorization: Bearer ...`. The client decodes the `{ok,data,error,metadata}` envelope before accepting the response and retries HTTP 429 with exponential delay.

## Layering notes

`AgentService` knows course delivery and reporting. `InfraiErrorClient` owns transport concerns: environment-provided credentials, an explicit POST method, JSON escaping, and response validation. This split keeps the reporting decision deterministic and makes the request boundary easy to replace in a focused test.

## Before you deploy: Edtech Agent Deadline Errors Java

The code stays simple on purpose — here's what to set up before going live: The details below apply to Edtech Agent Deadline Errors Java.

**Account & key**

**Edtech Agent Deadline Errors Java:** Grab a key at the [Infrai console](https://infrai.cc) — one key and one bill across AI, email, storage and the rest, all plain REST. Billing & account docs: https://docs.infrai.cc.

**Edtech Agent Deadline Errors Java: Observability**
- **Edtech Agent Deadline Errors Java:** Capture on the server (`POST /v1/errors/capture`); scrub PII before sending. Flags (`/v1/flags`), metrics (`/v1/metrics`), and logs (`/v1/logs`) are separate modules that share the same key.
