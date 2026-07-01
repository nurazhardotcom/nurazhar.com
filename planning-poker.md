Title: Why Your Team Should Estimate With Cards, Not Hours
Date: 2026-07-01
Tags: agile, estimation, planning-poker, story-points, velocity
Description: Planning Poker is a consensus-based estimation technique using Fibonacci cards. Here's how it works and why relative sizing beats hourly estimates.

---

![Why Your Team Should Estimate With Cards, Not Hours](planning-poker.svg)

Hours-based estimation feels precise but is almost always wrong. **Planning Poker** fixes this by swapping calendar time for relative sizing.

---

## What Is Planning Poker?

Planning Poker is a **consensus-based estimation technique** where the whole team estimates together. Instead of arguing hours, devs vote on relative effort using a Fibonacci deck (1, 2, 3, 5, 8, 13, 21, 34, 55, ?).

The gap between cards grows as estimates get larger — because big unknowns are harder to predict than small ones. The deck is designed to reflect that uncertainty.

## The 5-Step Flow

| Step | What Happens | Why It Matters |
|------|-------------|----------------|
| **1. PO presents story** | Product Owner reads the user story and acceptance criteria | Common baseline for the whole team |
| **2. Devs discuss** | Team asks clarifying questions, points out assumptions | Surfaces hidden complexity before voting |
| **3. Private vote** | Everyone picks a card simultaneously, face-down | Eliminates **anchoring bias** — no one is influenced by senior voices |
| **4. Reveal & discuss** | Cards flipped at once. High/low outliers explain their reasoning | The most valuable conversation in the process |
| **5. Consensus** | Team converges on a single estimate, or re-votes | Shared ownership of the estimate |

If consensus isn't reached after revealing, the outliers explain their reasoning and the team re-votes (step 3 → 4). Most stories converge in 2-3 rounds. If a story is consistently estimated > 13, **it's too big** — split it.

## Why Relative Sizing Wins

**Absolute estimates** (hours/days) fail because software work is non-deterministic. A "2-hour" bugfix can turn into a 3-day rabbit hole. Relative sizing bypasses this by comparing stories:

| Approach | Accuracy | Team Speed | Cognitive Load |
|----------|----------|------------|----------------|
| **Hourly** | False precision — usually wrong | Slow — endless debate | High — everyone estimates differently |
| **Relative (Fibonacci)** | Consistently close at sprint scale | Fast — vote, discuss, move on | Low — "is this smaller or bigger than story X?" |

Think of it like T-shirt sizing (S, M, L, XL) — but with enough granularity for sprint planning.

## How It Feeds Velocity

Once stories are estimated in **story points**, the team tracks how many points they complete per sprint (velocity). That number becomes a **forecasting tool**:

- "Our average velocity is 25 points/sprint"
- "The backlog totals 150 points"
- "That's roughly 6 sprints"

Velocity isn't a productivity score — it's a **prediction instrument**. Never use it to compare teams or evaluate performance. It's contextual to that team, that tech stack, that domain.

## Practical Tips

- **Don't round up estimates** — they compound across the backlog. If it's a 3, call it a 3.
- **Watch for anchoring** — the first person to speak after a reveal can pull the group. Let outliers speak first.
- **Keep it fast** — if a story takes > 5 minutes of discussion, you're over-thinking. Mark it for further refinement.
- **Include the whole team** — developers, testers, and the PO should all participate (PO observes, doesn't vote).

## One-Liner

Estimate in story points, not hours — your Sprint Forecast will thank you.
