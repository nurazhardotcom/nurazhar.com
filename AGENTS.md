# Blog Conventions

- Write in D2, output SVG. Skip PNG entirely. No Playwright needed.
- H1 should be conversational, not academic.
- D2 SVG image right below title.
- Open with personal hook (2-3 sentences).
- Use tables for comparisons.
- Short sections with bold subheads.
- Short paragraphs (1-4 lines max).
- Bold key terms for scanability.
- No TOC, no footnotes, no fluff.
- End with one-liner or takeaway.

# D2 Diagram Framework

When creating or editing diagrams in blog posts, use the three-tier decision framework to ensure mobile-friendly rendering:

**Tier 1: Simple Relationships (2-4 nodes) → HTML Cards**
- Use for diagrams showing simple relationships (e.g., system component flow, decision trees with ≤4 nodes)
- Render as vertical HTML cards instead of D2 SVG
- Ensures readable text at any width
- Zero dependency on SVG rendering

**Tier 2: Complex Diagrams (5+ nodes) → D2 SVG + CSS Scaling**
- Use for architecture diagrams, framework overviews, and process flows
- Use D2's layout engine for diagrams with ≥5 nodes and labeled connections
- Apply `max-width: 100%` and `overflow-x: auto` CSS for mobile
- SVG scales down proportionally, pinch-zoom available for detail

**Tier 3: Data Comparisons → Markdown Tables**
- Use for dimension-by-dimension comparisons (e.g., feature matrices)
- Built-in `overflow-x: auto` on tables handles horizontal scrolling
- More efficient than wide D2 containers

Validation:
- Auto-escape $ signs in D2 code before compilation (D2 v0.6.9 treats $ as substitution prefix)
- Remove bare `direction:` directives outside d2 code fences
- Inline mentions of ``d2 in prose (inside quotes or lists) trigger the build pipeline's regex as false-positive code fences — keep them out of all .md files
- Contact if in doubt

# Git Workflow

- Push to `origin` (GitLab) only — GitHub mirror is automatic via GitLab push mirror.
- Run `bb build && bb test && bb validate-links` before committing (also wired into `.git/hooks/pre-commit`, but a manual run catches failures faster than waiting for the hook to abort).
- Use `bb bench` to time a warm local build before quoting build / pipeline numbers in a blog post. Manual `time` measurements drift over time as the corpus grows and d2 SVG complexity shifts. The task is the audit trail.
- Use `--no-verify` only if pre-commit hook blocks on pre-existing broken links (not your fault).

# Pending Tasks (Ask Before Continuing)

Items below are started but incomplete. Agents MUST ask the user before continuing work on them.

## brain-guard CLI Tool (`brain-guard/`)
- JVM-based CLI to prevent LLM cognitive overload
- Track AI interaction count per session
- Warn when approaching limits
- Save state to a local file
- Started but incomplete — user will continue later

## Publii-like GUI Desktop App
- Deferred — decided to build CLI tool first
- Revisit after brain-guard CLI is stable

# CI

- `pages` job runs on a **self-hosted** runner (`shell` executor, tag `homepage-self-hosted`). GitLab shared runners are not tagged for the job and never pick it up.
- Self-hosted runners do **not** consume GitLab.com's 400-min free-tier compute quota — the quota only applies to GitLab-managed shared runners.
- Pipeline: `bb build` → `bb validate-links` → upload `public/` artifact. No docker dependency — `bb`, `pandoc`, `d2`, `make` are installed natively on the runner host.
- The runner is intentionally the **only** path for the `pages` job — if the runner host is offline, jobs queue indefinitely (no shared-runner fallback by design).
- Register/install commands and the architecture rationale live in [`local-gitlab-runner-unlimited-ci.md`](local-gitlab-runner-unlimited-ci.md).
