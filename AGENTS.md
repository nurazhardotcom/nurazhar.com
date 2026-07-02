# Blog Conventions

- Write in D2, output SVG. Skip PNG entirely. No Playwright needed.
- H1 should be conversational, not academic. No quotes around H1.
- D2 SVG image right below title (but only if D2 compiles — check build output).
- Open with personal hook (2-3 sentences) — not a technical statement.
- Use tables for comparisons.
- Short sections with bold subheads.
- Short paragraphs (1-4 lines max).
- Bold key terms for scanability.
- No TOC, no footnotes, no fluff.
- End with one-liner or takeaway.
- Frontmatter (Title, Date, Tags, Description, `---`) is REQUIRED. Every post must have it at the top.
- Check the build output before committing. If D2 diagrams fail to compile, fix them first.
- After build, verify the post appears on the index: `grep "post-slug" public/index.html`. If missing, check frontmatter format (Title, Date, Tags are all required).

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

## Common D2 Mistakes

These errors cause silent build failures. Fix them before committing.

| Mistake | Wrong | Correct |
|---------|-------|---------|
| **No `as` keyword** | `Node: "Label" as Alias` | `Node: "Label"` (node name IS the alias) |
| **No bare direction outside code fence** | Text before `` `d2` | Keep all direction inside `` `d2 code fence |
| **Escape `$` signs** | `$VAR` in D2 string | `\$VAR` (D2 treats `$` as substitution prefix) |
| **No `\n` in string labels** | `"Line1\nLine2"` | Use D2 shape stacking or separate nodes |
| **Inline `d2` in prose** | `... like d2 does ...` | Reword to avoid `d2` outside code fences |
| **No `:` in node name colon** | `Node:Name: "Label"` | `NodeName: "Label"` |
| **Check build output** | D2 compiles silently fail | Look for `⚠️ D2 compilation failed` in build log |

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
- The runner binary is at `~/.local/bin/gitlab-runner`, config at `~/.config/gitlab-runner/config.toml`. It runs headless via `setsid` as a background process (no systemd unit on this machine).
- To check if the runner is alive: `ps aux | grep gitlab-runner` or check the log at `~/.local/var/gitlab-runner.log`.
- After `git push origin main`, the runner picks up the `pages` job within ~10s. Pipeline does a full `bb build && bb validate-links` natively.
- **Runner goes down when the shell dies.** If deployment is stuck after push, restart the runner manually:
  ```bash
  setsid ~/.local/bin/gitlab-runner run \
    --config ~/.config/gitlab-runner/config.toml \
    > ~/.local/var/gitlab-runner.log 2>&1 < /dev/null &
```
