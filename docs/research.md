API Design Review & Improvement Suggestions for clawphones

Context
- clawphones targets on-device AI on mobile. The API design (how apps talk to the on-device engine, manage models, run inferences) greatly impacts performance, privacy, and developer experience.
- This document proposes an API design direction, inspired by modern API practices (OpenAPI-first, versioned contracts, clear error models) while remaining practical for on-device constraints.

Scope for review
- Define a stable, minimal, well-documented surface for: model management, inference requests, and runtime/configuration.
- Propose an OpenAPI-based contract, versioned endpoints, and a strategy for incremental adoption with backward compatibility.
- Consider security, observability, testing, and developer experience (SDKs and docs).

Findings (high level)
- Strengths to leverage: predictable, self-contained on-device services reduce latency and privacy leakage.
- Gaps to address: missing public API contract, no versioning guidance, limited guidance on payload formats, and weak emphasis on testing at the contract level.

Recommendations (core)
- Establish an OpenAPI 3.x contract that codifies endpoints, payload schemas, and error formats.
- Introduce a versioned API path (e.g. /api/v1/...) with a deprecation policy and clear upgrade guide.
- Separate concerns: /models for model life cycle; /inference for runtime invocations; /config for runtime preferences; /status for health metrics.
- Define data formats that balance efficiency with simplicity: JSON for metadata; binary payloads (base64-encoded or multipart) for models and tensors; consider protobuf/flatbuffers for internal IPC if performance dictates.
- Implement a robust error model with standard HTTP codes and a structured error payload including code, message, and optional details.
- Provide a minimal SDK/comms wrapper for major platforms (Android, iOS) that generates API clients from the OpenAPI spec.
- Add contract tests (pact-like or OpenAPI-driven tests) to ensure interface stability across changes.
- Plan for observability: structured logs with fields (task_id, endpoint, model_id, latency) and metrics (requests/sec, inference latency, error rate).

Proposed API surface (illustrative)
- Base path: /api/v1/
- Models
  - GET /models - list models
  - POST /models - upload/register a new model package
  - GET /models/{modelId} - details and status
  - PATCH /models/{modelId} - update metadata or configuration
  - DELETE /models/{modelId} - remove model (soft delete preferred on-device)
- Inference
  - POST /inference - run single inference (payload includes modelId, input data, optional config)
  - POST /inference/batch - multiple inputs in one request (if useful for performance)
- Config
  - GET /config - current runtime configuration
  - PATCH /config - update runtime options (precision, batching, resource limits)
- Status
  - GET /status - engine health, available memory, supported models

OpenAPI sketch (illustrative, not final)
```yaml
openapi: 3.0.3
info:
  title: clawphones API
  version: v1
paths:
  /api/v1/models:
    get:
      summary: List models
      responses:
        '200': {"description": "OK", "content": {"application/json": {"schema": {"$ref": "#/components/schemas/ModelList"}}}}
  /api/v1/models:
    post:
      summary: Upload a new model package
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ModelCreate'
      responses:
        '201': {"description": "Created", "content": {"application/json": {"schema": {"$ref": "#/components/schemas/ModelInfo"}}}}
components:
  schemas:
    ModelCreate:
      type: object
      properties:
        modelId:
          type: string
        version:
          type: string
        metadata:
          type: object
      required: [modelId, version]
    ModelInfo:
      type: object
      properties:
        modelId:
          type: string
        version:
          type: string
        status:
          type: string
        createdAt:
          type: string
          format: date-time
```

How to operationalize this
- Start with a minimal, stable v1 API around /models and /inference.
- Generate client SDKs from the OpenAPI spec to accelerate platform integrations.
- Add contract tests and API documentation generation in CI.
- Establish a changelog and deprecation policy before introducing breaking changes.

Risks and trade-offs
- On-device constraints require careful payload design; keep the public surface small and stable.
- Binary payloads may complicate cross-platform interoperability; plan for an HTTP+multipart or protobuf path if needed.
- Security: even on-device services deserve deterministic access control; avoid exposing endpoints broadly without OS isolation.

Next steps (quick wins)
- Create a v1 OpenAPI document and wire a tiny in-memory mock service to demonstrate the contract.
- Add an initial unit-test suite for the /inference contract with a dummy model.
- Prepare platform-specific integration notes for Android and iOS.

Note: this is an analytical design guide. It should evolve with real usage data and stakeholder feedback.
