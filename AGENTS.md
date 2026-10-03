# 🤖 CHOPEE - AGENT & AI ASSISTANT GOVERNANCE RULES

This file defines mandatory instructions for any AI assistant, agent, or automated workflow operating within the **Chopee Marketplace** repository.

---

## 1. Core Directives

1. **Adhere to `PROJECT_RULES.md`:**
   - Every agent must read and strictly respect the rules outlined in [`PROJECT_RULES.md`](./PROJECT_RULES.md).
   - Never skip pre-commit verification gates (`mvn test` and `npm run build`).

2. **Mandatory Change Logging with Change Key (`CHG-YYYYMMDD-XXX`):**
   - Whenever you alter the database schema, business rules, or core architecture, you MUST append a new change record to [`docs/specs/07_changelog.md`](./docs/specs/07_changelog.md).
   - The record must include: `Change Key`, `Date`, `Author`, `Type`, `Component`, `Description`, `Rationale`, `Before vs After`, and `Impact`.

3. **Keep Specifications Synchronized:**
   - Code and documents in [`docs/specs/`](./docs/specs/) must stay in 100% sync. Never change code behavior without updating the corresponding specification document.

4. **Multi-Vendor Integrity:**
   - Always preserve the multi-vendor separation rule: A buyer's order containing items from multiple shops MUST be cleanly split into separate `orders` per shop.
   - Enforce IDOR protection: Shop A's seller cannot read/modify Shop B's orders or products.

5. **Test-Driven & Evidence-First:**
   - Never claim a task or test is completed without executing the test command and inspecting the real output.
