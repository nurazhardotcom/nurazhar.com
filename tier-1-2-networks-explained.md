Title: Tier 1 and Tier 2 Networks — The Internet Backbone Hierarchy
Date: 2026-06-28
Tags: networking, tier1, tier2, backbone, bgp, peering, infrastructure, d2
Description: How the internet's core networks are classified and why it matters for your connection.

---

The internet isn't one network. It's tens of thousands of independently operated networks stitched together by BGP (Border Gateway Protocol). These networks fall into a loose hierarchy.

## The Three Tiers

```d2
# Diagram 1: Internet Tier Hierarchy
direction: down

T1: "Tier 1 — Global Backbone\n~8-15 networks\nReach entire internet without paying"
T2: "Tier 2 — Regional/National ISPs\nPay Tier 1s for transit\nPeer with each other for free"
T3: "Tier 3 — Resellers\nBuy transit from Tier 2s\nNo network of their own"

T1 -> T1: "Peer with each other\nsettlement-free"
T2 -> T1: "Pay for transit"
T2 -> T2: "Peer settlement-free"
T3 -> T2: "Pay for transit"
```

### Tier 1 — The Backbone

A Tier 1 network can reach every other network on the internet without paying anyone for transit. They achieve this through **settlement-free peering** agreements with other Tier 1s — mutual handshakes where no money changes hands.

The recognized global Tier 1s (as of 2026):

| Network | ASN | Headquarters |
|---|---|---|
| Lumen (formerly CenturyLink/Level 3) | AS3356 | USA |
| Cogent Communications | AS174 | USA |
| NTT Communications | AS2914 | Japan |
| Telia Carrier (now Arelion) | AS1299 | Sweden |
| GTT Communications | AS3257 | USA |
| AT&T | AS7018 | USA |
| Verizon | AS701 | USA |
| Zayo | AS6461 | USA |
| PCCW Global | AS3491 | Hong Kong |
| Hurricane Electric | AS6939 | USA |
| Deutsche Telekom | AS3320 | Germany |
| Telecom Italia Sparkle | AS6762 | Italy |

There are about 8-15 depending on who you ask. The exact list is debated because peering relationships shift over time.

```d2
# Diagram 2: Tier 1 Mesh
direction: down

T1a: "Lumen (AS3356)"
T1b: "NTT (AS2914)"
T1c: "Cogent (AS174)"
T1d: "Arelion (AS1299)"
T1e: "HE (AS6939)"

T1a <-> T1b: "settlement-free\npeering"
T1a <-> T1c: ""
T1a <-> T1d: ""
T1b <-> T1c: ""
T1b <-> T1e: ""
T1c <-> T1d: ""
T1c <-> T1e: ""
T1d <-> T1e: ""
T1a <-> T1e: ""
```

A fully meshed backbone. Each Tier 1 connects directly to every other Tier 1 so traffic can flow without intermediaries.

### Tier 2 — Where Your ISP Lives

Tier 2 networks peer with some networks for free but **pay Tier 1s for transit** to reach parts of the internet they can't access directly.

Singtel (AS7473), StarHub (AS4657), M1 (AS4773), and Simba (AS4817) are all Tier 2. The difference is scale:

| ISP | ASN | IPv4 Prefixes | Peers | Traffic |
|---|---|---|---|---|
| Singtel | AS7473 | ~100,000 | 253 | Not disclosed |
| StarHub | AS4657 | ~5,000 | Moderate | ~200-300 Gbps |
| M1 | AS4773 | ~500 | ~100G at SGIX | Not disclosed |
| Simba | AS4817 | ~40 | Limited | ~100-200 Gbps |

```d2
# Diagram 3: How a Tier 2 Reaches the Internet
direction: down

You: "You\n(Singtel customer)"
Singtel: "Singtel (AS7473)\nTier 2"
Upstream: "Tier 1 Transit\nBuys from HE, NTT, Telia"
Remote: "Remote Server\n(e.g. Europe)"

You -> Singtel: "Local traffic stays on-net"
Singtel -> Upstream: "Pays for transit"
Upstream -> Remote: "Delivered via Tier 1 backbone"
```

### Tier 3 — Pure Resellers

Tier 3 networks own no infrastructure. They buy transit from Tier 2 or Tier 1 providers and resell it to end users. Some of the smallest MVNOs operate this way.

## Why Tier Matters

A Tier 1 network has:
- Global reach without middlemen (fewer hops)
- Massive capital investment in physical fibre
- Redundancy across multiple routes
- Direct peering with content providers (Netflix, Google, etc.)

A small Tier 2 like Simba has:
- Fewer direct routes (traffic takes more hops)
- Higher oversubscription (more customers sharing less capacity)
- Dependence on upstream providers working correctly

When a submarine cable breaks, Tier 1 networks reroute instantly through their mesh. A small Tier 2 that buys transit from that Tier 1 is at the mercy of that upstream provider's rerouting speed.

## The Reality

For browsing YouTube and Instagram from Singapore, Tier 1 vs Tier 2 makes zero difference. Content is cached locally (Google has servers in Singapore). Your latency is 4ms either way.

The tier distinction matters for:
- Niche international routes (game servers in South America)
- Network resilience during outages
- Business/enterprise connectivity where SLAs matter
- Understanding why some ISPs are more expensive

*Related: [What IP Prefixes Measure and Why They Matter](./what-ip-prefixes-measure.html)*
