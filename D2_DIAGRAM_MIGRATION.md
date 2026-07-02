# D2 Diagram Migration Proposal

## Summary
The current D2 setup in the blog produces broken diagrams with unreadable text on mobile devices. The key issues need manual fixing ($ escaping, `direction:` placement, mobile layout). This adds significant overhead.

## Problems (Based on [making-d2-diagrams-mobile-friendly-framework.md](making-d2-diagrams-mobile-friendly-framework.md))

**Problem #1: $ Substitution Issue**
- D2 v0.6.9 treats $ as a substitution prefix
- Diagrams with $200/session or ~$0.50/hr fail: "substitutions must begin on {"
- Manual escaping required for every post with dollar amounts

**Problem #2: Nested direction: down**
- The financial-butler.md diagram shows multiple direction: down directives
- D2 only uses the first one - subsequent ones are warnings: "bare 'direction:' directive (missing d2 fence)"
- Creates corrupted blocks that show raw source in HTML

**Problem #3: Mobile Layout Failures**
- Complex diagrams render as 700px+ wide SVGs
- Scale to ~7px text on 375px phones
- Inline HTML cards fix needed for simple relationships (2-4 nodes)

## Solution: Apply Three-Tier Framework

The blog already has a documented solution in `making-d2-diagrams-mobile-friendly-framework.md`:

**Tier 1: Simple Relationships (2-4 nodes) → HTML Cards**
- Instead of D2 SVG
- Vertical HTML cards with readable text at any width
- Zero dependency on SVG rendering

**Tier 2: Complex Diagrams (5+ nodes) → D2 SVG + CSS Scaling**
- Use D2 for architecture flows
- Apply max-width: 100% CSS for mobile
- Add overflow-x: auto for very wide diagrams

**Tier 3: Data Comparisons → Markdown Tables**
- For dimension-by-dimension comparisons
- Built-in overflow-x: auto on tables

## What Needs to Change

### 1. Update AGENTS.md
- Document the three-tier decision framework
- Add validation warnings for $ escaping and direction directives

### 2. Add Preprocessing Logic
- Auto-escape $ in D2 code before compilation
- Remove orphaned direction: directives outside code fences

### 3. Convert Simple Relationship Diagrams

Current files needing conversion to HTML cards (Tier 1):
- ai-vs-human-life-coach.md (Human Coach vs AI Coach relationship)

### 4. Fix Nested direction: in financial-butler.md
- Remove "bare direction:" directives outside d2 fences
- Keep one direction: down at the top level

## Pro-Freebuff (Working on Related Issues)

The freebuff discovery in demystifying-arch-packaging-pkgbuilds-aur-cachyos.md:
- Is about AUR package binaries, unrelated to D2 diagrams
- User-specific: Freebuff creates binary packages for users who don't want to compile
- No overlap with D2 mobile fixes needed

## Example Fixes

### financial-butler.md (before)

```text
# Diagram 79
direction: down

User: "I want to buy X for $Y" {
  style.fill: "#f8f9fa"
  style.stroke: "#dee2e6"
}

Butler: "Financial Butler" {
  style.fill: "#f8f9fa"
  style.stroke: "#dee2e6"
  Check: "Check: current balance - (debt obligations + upcoming expenses + reserves)" {
    style.fill: "#ffffff"
    style.stroke: "#dee2e6"
  }
}

direction: down
```

### financial-butler.md (after)

```text
# Diagram 79
direction: down

User: "I want to buy X for $Y" {
  style.fill: "#f8f9fa"
}

Butler: "Financial Butler" {
  style.fill: "#f8f9fa"
  Check: "Check: current balance - (debt obligations + upcoming expenses + reserves)" {
    style.fill: "#ffffff"
  }
}
```

### ai-vs-human-life-coach.md (convert to HTML cards)

```html
<div style="display: flex; flex-direction: column; gap: 8px; max-width: 380px; margin: 24px auto; font-size: 14px; line-height: 1.5;">

<div style="border: 2px solid #0ea5e9; background: #f0f9ff; border-radius: 12px; padding: 14px; text-align: center;">
  <strong style="font-size: 15px;">Human Coach</strong><br>
  <span style="font-size: 12px; color: #475569;">Accountability · Emotional support</span>
</div>

<div style="text-align: center; font-size: 20px; color: #64748b;">↓</div>

<div style="border: 2px solid #10b981; background: #d5f5e3; border-radius: 12px; padding: 14px; text-align: center;">
  <strong style="font-size: 15px;">AI Coach (LLM)</strong><br>
  <span style="font-size: 12px; color: #475569;">Data-driven analysis · 24/7 availability</span>
</div>

<div style="text-align: center; font-size: 20px; color: #64748b;">↓</div>

<div style="border: 2px dashed #6366f1; background: #eef2ff; border-radius: 12px; padding: 14px; text-align: center;">
  <strong style="font-size: 15px;">Best Together</strong><br>
  <span style="font-size: 12px; color: #475569;">AI does the audit. Human does the grounding.</span>
</div>

</div>
```

## Next Steps

1. Review which posts have simple 2-4 node relationships (like Human Coach vs AI Coach)
2. Fix nested direction: in financial-butler.md
3. Update AGENTS.md with the three-tier framework
4. Add preprocessing validation for $ escaping and malformed D2
5. Convert identified posts to HTML cards

This will eliminate manual fixes and ensure all diagrams work on mobile.
