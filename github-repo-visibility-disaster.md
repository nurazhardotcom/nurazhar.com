Title: How I Accidentally Hid My Entire GitHub — and What It Taught Me About Visibility, Forks, and Irreversible Decisions
Date: 2026-06-19
Tags: github, devops, career, mistakes, mermaid
Description: A detailed post-mortem of the 24-hour window where all my public repos went private — the bad advice I followed, the irreversible GitHub mechanics I didn't understand, and the Mermaid diagrams that explain what actually happened.
---

## The 24 Hours That Broke My GitHub

Yesterday, I made a decision that cost me an entire day of visibility on GitHub. This is the detailed breakdown of what happened, why it happened, and the technical mechanics behind it — with Mermaid diagrams so you can see exactly what went wrong.

---

## Act I: The Bad Advice

I was doing a "GitHub audit" for job hunting. The goal: make my GitHub profile look clean for Singapore cybersecurity hiring managers.

An AI assistant (yes, I got bad advice from another AI — the irony is not lost on me) gave me this table:

| Repository | Called It | Actual Status |
|---|---|---|
| `bitcoin` | "Fork, no contribution" | **Not a fork** — standalone repo |
| `bitcoin-wiki` | "Academic, irrelevant" | **Not a fork** — standalone repo |
| `frontend` | "Fork, abandoned" | **Not a fork** — standalone repo |
| `hermes-agent` | "Fork, not your code" | **Not a fork** — standalone repo |
| `headhunter-agent` | "Fork, AI agent" | **Not a fork** — standalone repo |

**Every single one was labeled "fork." Not one was actually a fork.**

The advice: make all 12 private. "Clean signal: 3–5 relevant projects." "Noise: forks, tutorials, abandoned experiments."

I said yes.

---

## Act II: The Execution

```bash
for r in bitcoin bitcoin-wiki frontend hermes-agent headhunter-agent; do
  gh repo edit nurazhardotcom/$r --visibility private \
    --accept-visibility-change-consequences
done
```

One by one, 12 repos went dark.

---

## Act III: The Reversal

This morning, I realized the advice was wrong. I ran:

```bash
for r in bitcoin bitcoin-wiki frontend hermes-agent headhunter-agent; do
  gh repo edit nurazhardotcom/$r --visibility public \
    --accept-visibility-change-consequences
done
```

All 21 repos are now public again. But the story doesn't end there.

---

## The Technical Deep Dive

### What `gh repo edit --visibility` Actually Does

```d2
# Diagram 97
direction: down

A: "Repo: PUBLIC"
B: "Repo: PRIVATE"
C: "Repo: PUBLIC"
D: "Fork Network: Connected to parent"

E: "Fork Network: DISCONNECTED" {
  style.fill: "#fafafa"
}
F: "Fork Network: Still DISCONNECTED" {
  style.fill: "#fafafa"
}

B -> C: "gh repo edit --visibility public"
A -> D
B -> E: "GitHub severs fork relationship"
C -> F: "Does NOT auto-reconnect"
```

### The Fork Network Problem

Here's the critical thing: **if a repo IS a real fork**, flipping it private severs the parent-child relationship in GitHub's fork network. Flipping it back to public does NOT restore that connection.

```d2
# Diagram 98
direction: down

D1: "Downstream Forks\nother-user/project"
D2: "Downstream Forks\nother-user/project"
D3: "Downstream Forks\nother-user/project"

F1: "Your Fork\nnurazhardotcom/project\n🟢 PUBLIC"

F2: "Your Repo\nnurazhardotcom/project\n🔴 PRIVATE" {
  style.fill: "#fafafa"
}
F3: "Your Repo\nnurazhardotcom/project\n🟢 PUBLIC\nbut orphaned" {
  style.fill: "#fafafa"
}

P1: "Parent Repo\nupstream/project"
P2: "Parent Repo\nupstream/project"
P3: "Parent Repo\nupstream/project"

F1 -> D1
F2 -> D2: "SEVERED"
F3 -> D3: "NOT RESTORED"
```

### But Mine Weren't Forks!

Here's the twist: **my repos were never forks to begin with.**

```d2
# Diagram 99
direction: down

A: "21 repos"
B: "12 FORKS → make private" {
  style.fill: "#fafafa"
}
C: "9 ORIGINAL → keep public"

X: "21 repos"
Y: "0 FORKS" {
  style.fill: "#fafafa"
}
Z: "21 STANDALONE REPOS"

A -> C
X -> Z
```

I verified this via GitHub's own API:

```bash
gh api repos/nurazhardotcom/bitcoin --jq '{fork: .fork, parent: .parent.full_name}'
# {"fork": false, "parent": null}
```

Every single repo returned `fork: false`. The "fork network" problem was irrelevant because there was no fork network to begin with.

---

## The Timeline

```d2
direction: right

t0: "Day 0, 10 PM\nAsked AI about cleanup"
t1: "Day 0, 11 PM\ngit repo edit --visibility private" {
  style.fill: "#fff3cd"
}
t2: "Day 0, 11:30 PM\n21 repos → PRIVATE\nForks severed" {
  style.fill: "#f8d7da"
}
t3: "Day 1, 8 AM\nNoticed repos gone" {
  style.fill: "#f8d7da"
}
t4: "Day 1, 10 AM\nChecked API: no forks" {
  style.fill: "#d4edda"
}
t5: "Day 1, 11 AM\nFlipped all → PUBLIC"
t6: "Day 1, 12 PM\nProfile restored\nForks still orphaned" {
  style.fill: "#fff3cd"
}

t0 -> t1 -> t2 -> t3 -> t4 -> t5 -> t6
```

---

## What I Learned

### 1. Verify Before You Act

One `gh api` call would have prevented this:

```bash
gh api repos/nurazhardotcom/$repo --jq '.fork'
```

I didn't run it. I took the AI's word for it. That was the root cause.

### 2. Public Repos Don't Hurt Your Resume

The advice said "21 public repos = noise." That's not how hiring works. Hiring managers look at:
- **What you built** (not how many repos)
- **Code quality** (not repo count)
- **Relevant projects** (not whether you have irrelevant ones)

Having 21 repos with 4 pinned is not "noise." It's a portfolio.

### 3. `--accept-visibility-change-consequences` Is a Warning, Not a Formality

GitHub makes you add this flag for a reason. It's not just "I accept." It's "I understand this might break things." I clicked through without reading.

### 4. Visibility Changes Are (Mostly) Reversible — But Not Free

```d2
# Diagram 101
direction: down

NET: "Fork Graph"
NET2: "Fork Graph Broken" {
  style.fill: "#fafafa"
}
PRIV: "🔴 Private"
PUB: "🟢 Public"
PUB2: "🟢 Public"

PRIV -> PUB2: "--visibility public"
PUB -> NET: "Fork network: connected"
PRIV -> NET2: "Fork network: SEVERED"
PUB2 -> NET2: "Fork network: NOT restored"
```

For standalone repos (like mine), the flip is fully reversible — no permanent damage. But for real forks, the fork network break is **permanent** even after flipping back.

### 5. AI Advice Is Not Gospel

I got this advice from another AI tool. It was confident, detailed, and **factually wrong**. The lesson: always verify factual claims before acting on them, especially when the action is hard to undo.

---

## The Current State

All 21 repos are **PUBLIC** and staying that way:

```bash
$ gh repo list nurazhardotcom --limit 25 --json name,visibility --jq '.[] | "\(.visibility)\t\(.name)"'

PUBLIC  blog.nurazhar.com
PUBLIC  hermes-agent
PUBLIC  sol-de-tracker
PUBLIC  nurazhardotcom
PUBLIC  lagu-lagu
PUBLIC  frontend
PUBLIC  bsv-clj
PUBLIC  aur-audit
PUBLIC  bitcoin-wiki
PUBLIC  headhunter-agent
PUBLIC  bitcoin
PUBLIC  lithan_smartshop
PUBLIC  lithan_assignments
PUBLIC  lithan-dev-sandbox
```

---

## Key Takeaway

> **Never change repo visibility based on unverified advice.**
>
> One API call. Five seconds. Would have saved 24 hours.

---

*Written at 2:00 AM because I couldn't sleep thinking about this. The Mermaid diagrams render on GitHub natively — if you're reading this in a viewer that doesn't support Mermaid, the source is in the [blog repo](https://gitlab.com/nurazhar/blog.nurazhar.com).*
