Title: Gotong Royong — Nusantara Resource Allocation Engine, Refactored
Date: 2026-06-26
Tags: gotong-royong, nusantara, clojure, agpl3, anti-hoarding, sambatan, mapalus, ngayah, bsiv, small-world, mandala
Description: The Gotong Royong Local Resource Abstraction Engine has been refactored to embed Nusantara cultural constraints — anti-hoarding velocity mechanics, Small-World/Mandala network topology, and indigenous linguistic layers (Sambatan, Mapalus, Ngayah). This post documents the architectural decisions.

---

The Gotong Royong repository at `gitlab.com/nurazhar/gotong-royong` has been refactored from a generic resource-abstraction proof-of-concept into a **high-density, culturally-embedded Nusantara Protocol engine**.

The refactor injects three layers directly into the Clojure core:

## 1. Linguistic / Bahasa Layer (Indigenous Protocol Primitives)

The namespace and data-structure layout now mirrors regional social architectures:

- **Sambatan** — spontaneous communal assistance (urgency-based, trustless allocation)
- **Mapalus** — balanced reciprocity protocol (rotational, closed-loop exchange)
- **Ngayah** — volunteer-driven resource routing (capacity-aware, non-reciprocal)

These are not decorative labels. They are validation shapes in `clojure.spec` — machine-readable protocols that constrain how agents route resources based on the specific reciprocity model in play.

Example from `core-schemas.clj`:

```clojure
(spec/def :sambatan-protocol
  (spec/keys :req-un
    ::origin-wallet
    :gotong/nusantara-grid-index
    :resource/type
    :resource/quantity
    :sambatan/urgency-level
    :sambatan/community-impact))

(spec/def :sambatan/urgency-level
  (spec/or :levels (spec/in [0 1 2 3 4])))
```

## 2. Anti-Hoarding Velocity Engine

The central metric has been inverted from **token accumulation** to **resource velocity**. The allocation engine actively penalises resource stagnation and rewards rapid circulation through the community.

Key design decisions:

- `calculate-resource-velocity-priority` — scores resources higher based on allocation speed, not static quantity
- **Hoarding penalty** — reduces priority for resources with low circulation history
- **Community trust score** — resources that circulate with higher velocity earn higher trust multipliers
- **Stagnation decay** — resources held beyond a regional time window lose allocation priority

This aligns with the Gotong Royong principle: resources held in common circulate continuously; defensive hoarding is treated as a system anomaly.

From `agent-allocation.clj`:

```clojure
(defn calculate-resource-velocity-priority
  [resource request]
  ...
  hoarding-penalty (let [current-velocity (or (:gotong/resource-velocity resource) 0.0)]
                    (/ 1.0 (+ 1.0 current-velocity)))
  ...)
```

## 3. Small-World / Mandala Core

The geohash routing layer has been restructured to model **Mandala network topology**:

- **Edge layer**: Lightweight SPV wallets (the *bilid* — family unit in longhouse architecture)
- **Overlay layer**: Regional resource pools and community agents (the *ruai* — communal veranda)
- **Core layer**: Transaction settlement and global state (the *mandala* — the dense core where every node reaches every other node in ~1.3 hops)

The 8-character H3 geohash grid (`:gotong/nusantara-grid-index`) indexes resources specifically for the Nusantara region, enabling constant-hop routing between any edge wallet and the nearest surplus resource.

```clojure
(spec/def :gotong/nusantara-grid-index
  (spec/and string? #(= 8 (count %))
            :description "8-char geohash for Nusantara coordination"))

(spec/def :gotong/community-layer
  (spec/or :layers (spec/in [:bilid :ruai :mandala])))
```

## Why This Matters

The original implementation applied Western individualistic defaults — treating resource allocation as an optimisation between isolated agents who maximise personal utility. This is a mismatch for the Nusantara region, where the indigenous operating system runs on:

- **Gotong Royong** — collective reciprocity, not individual utility
- **Rumah Panjang** architecture — radical transparency (the *ruai*), not privacy-as-default
- **Communal enforcement** — social equity over financial capital

By embedding these constraints directly into the Clojure spec validation layer and the agent allocation loop, the protocol now reasons in the cultural terms of the region it serves.

## Repository

```bash
git clone git@gitlab.com:nurazhar/gotong-royong.git
```

The full diff from the first prototype to the Nusantara-refactored engine is available in the commit history. AGPL3 licensed — any network-visible improvements must remain open.

## Next

The repository now reflects the core architectural decisions. If you are building for the Nusantara region and want to extend this protocol — or adapt the anti-hoarding engine for another cultural OS — the issue tracker is open.
