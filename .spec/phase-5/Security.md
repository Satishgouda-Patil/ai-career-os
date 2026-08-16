# Phase 5 Security Specification

## 1. Security Goal

AI Career OS will handle:

- personal information
- resumes
- job applications
- recruiter communication
- email metadata
- OAuth connections
- potentially sensitive career information

Security is therefore a launch requirement.

---

# 2. Authentication

Use existing JWT authentication.

Requirements:

- secure password hashing
- token expiration
- authorization checks
- protected endpoints
- no authentication bypass

---

# 3. Secrets

Never commit:

- passwords
- JWT secrets
- OAuth secrets
- API keys
- database passwords
- bot tokens
- access tokens
- refresh tokens

Use environment variables or secure secret storage.

---

# 4. Email OAuth

OAuth credentials must never be displayed to the user after authorization.

Tokens must be encrypted at rest where persistence is required.

Never log tokens.

---

# 5. API Security

Implement:

- authentication
- authorization
- input validation
- rate limiting where appropriate
- CORS policy
- secure headers
- request size limits

---

# 6. Automation Safety

Default dangerous automation to OFF.

Examples:

```text
AUTO_APPLY = OFF
AUTO_SEND_EMAIL = OFF
AUTO_FOLLOW_UP = OFF