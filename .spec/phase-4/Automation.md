# Phase 4 Automation Specification

## Automation Principle

Automate repetitive work.

Require human review for ambiguous or high-impact actions.

---

# Application Tracking

Automatically:

- calculate application age
- update timeline
- calculate next action
- detect overdue follow-ups
- notify user

---

# Email Processing

Scheduled process:

```text
Every N minutes
      ↓
Fetch new messages
      ↓
Normalize
      ↓
Filter job-related messages
      ↓
Classify
      ↓
Match application
      ↓
Validate confidence
      ↓
Update application