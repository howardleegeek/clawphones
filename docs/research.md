# API Design Review: clawphones

Executive summary
- Provide a concise, versioned, and well-documented API design for clawphones.
- Align REST API practices with OpenAPI-driven tooling to improve maintainability, onboarding, and testing.
- Include clear authentication, authorization, rate limiting, versioning, and error handling strategies.

Scope and goals
- Review current API design (endpoints, resources, data models) and identify gaps.
- Propose pragmatic improvements that can be implemented with minimal risk to existing behavior.
- Outline a path to a stable, evolvable API surface that supports future features.

Observations (general design patterns)
- Prefer explicit resource modeling: devices, users, calls, messages, artifacts.
- Use predictable, pluralized endpoints (e.g., /devices, /devices/{id}).
- Establish a single, consistent error format across all APIs (e.g., {"error": {"code": "INVALID_INPUT", "message": "...", "details": {}}}).
- Version APIs (e.g., /v1/devices, /v2/devices) and route to corresponding controllers/services.
- Provide pagination for list endpoints; support limit/offset or cursor-based pagination depending on data volume.
- Centralize authentication (e.g., OAuth 2.0 / JWT) and authorization checks at the route or service boundary.
- Introduce OpenAPI (Swagger) specifications and generate client SDKs where helpful.
- Add traceability: request IDs in logs, and correlation IDs across services.

Key gaps and issues to address
- Inconsistent endpoint naming and HTTP method usage (guesswork without spec).
- Missing standard error payload and codes, leading to ambiguous failures.
- Lack of versioned contracts and clear deprecation plan for older endpoints.
- Sparse or missing input validation rules and schema definitions.
- No formal API testing strategy (contract tests, integration tests, fuzz tests).
- No documented authentication flow or scopes/roles for authorization.
- Absence of rate limiting, quotas, and throttling policy.
- Missing observability hooks: request tracing, metrics, and correlation IDs.

Recommendations (concrete steps)
- Establish an API design governance doc and publish a baseline OpenAPI v3 spec:
  - Define core resources: User, Device, Call, Message, Channel, File.
  - Clarify relationships, allowed actions, and required vs optional fields.
  - Specify error responses for common scenarios: INVALID_INPUT, UNAUTHORIZED, FORBIDDEN, NOT_FOUND, SERVER_ERROR, RATE_LIMIT.
  - Add standard pagination parameters: page, pageSize (or cursor-based with nextToken).
  - Include securitySchemes: OAuth2 with PKCE or JWT Bearer tokens; roles/scopes.
- Versioning plan: /v1/* for current surface; plan deprecation cycles and a path to v2.
- Implement request IDs and tracing: X-Request-Id header, propagate to logs and traces.
- Add an API gateway or middleware layer to enforce rate limits, auth, and input validation early.
- Adopt robust input validation using a shared schema (OpenAPI + runtime validators). Use JSON Schema or similar.
- Adopt OpenAPI-driven tests:
  - Contract tests that validate responses against the OpenAPI schema.
  - Integration tests with a representative data set.
  - Property-based tests for edge-case inputs where useful.
- Create a minimal, stable client SDK scaffolding generator (from OpenAPI) for common languages used by clawphones consumers.
- Observability: add metrics (latency, error rate, 95th percentile), and log correlation IDs.
- Security: add standard auth headers, validate tokens, and implement least-privilege scopes.
- Documentation: generate API docs from the spec and keep docs in sync with code.

Example: OpenAPI skeleton (v1)
```yaml
openapi: 3.1.0
info:
  title: clawphones API
  version: v1
paths:
  /v1/devices:
    get:
      summary: List devices
      parameters:
        - in: query
          name: limit
          schema:
            type: integer
            default: 50
        - in: query
          name: offset
          schema:
            type: integer
            default: 0
      responses:
        '200':
          description: A list of devices
          content:
            application/json:
              schema:
                type: object
                properties:
                  items:
                    type: array
                    items:
                      $ref: '#/components/schemas/Device'
        '401':
          $ref: '#/components/responses/Unauthorized'
components:
  schemas:
    Device:
      type: object
      properties:
        id:
          type: string
        name:
          type: string
        status:
          type: string
  responses:
    Unauthorized:
      description: Missing or invalid authentication
      content:
        application/json:
          schema:
            type: object
            properties:
              error:
                type: object
                properties:
                  code:
                    type: string
                  message:
                    type: string
```

Migration plan and milestones
- Week 1-2: Define OpenAPI baseline, security model, and versioning strategy.
- Week 3-4: Implement OpenAPI generation, contract tests, and initial docs.
- Month 2: Introduce pagination and error schema; add tracing and correlation IDs.
- Ongoing: Security hardening, rate limiting, and SDK generation.

Risks and considerations
- API changes may affect existing clients; communicate deprecations clearly.
- Ensure data migrations and backward-compatibility for field changes.
- Balance speed of delivery with correctness; start with a minimal viable spec and iterate.

Next steps for the reviewer
- Confirm alignment with product goals and customer use-cases.
- Approve the baseline OpenAPI spec structure and security approach, then begin implementation in a safe branch.

Appendix: Metrics to track
- API latency (p50, p95), error rate, and request per second by endpoint.
- Token validation latency and cache hit rate for auth.
- Number of API changes per release and time-to-deploy for new versions.
