Title: Transit vs Peering — The Economics of Internet Connectivity
Date: 2026-06-28
Tags: networking, transit, peering, bgp, infrastructure, economics, d2
Description: Why some networks pay each other and others exchange traffic for free.

---

Every network on the internet needs to reach every other network. There are two ways to do it: **transit** (paying for access) or **peering** (exchanging for free).

## Transit — Paying for the Internet

When an ISP buys transit, they pay a larger network (usually a Tier 1) to carry their traffic to destinations they can't reach directly.

It works like a utility: you pay a monthly fee based on port speed or bandwidth usage, and the upstream provider handles the rest.

```d2
# Diagram 1: Transit
direction: down

ISP: "Small ISP\n(Simba, for example)"
Transit: "Tier 1 Provider\n(Hurricane Electric, NTT)"
Dest1: "Server in Europe"
Dest2: "Server in US"
Dest3: "Server in Japan"

ISP -> Transit: "Pays monthly transit fee"
Transit -> Dest1: "Delivers traffic"
Transit -> Dest2: ""
Transit -> Dest3: ""
```

Simba doesn't own submarine cables or have global backbone infrastructure. They pay Tier 1 providers (like Hurricane Electric, NTT, or Telia) to carry their traffic overseas. Every byte that leaves Simba's network costs them money.

## Peering — Trading Traffic for Free

Peering is when two networks agree to exchange traffic without money changing hands. Why would a large network peer for free? Because it benefits both sides.

```d2
# Diagram 2: Peering
direction: down

Singtel: "Singtel Network"
Google: "Google Network"
SGIX: "SGIX (Exchange Point)"

Singtel -> SGIX: "Connects at 100G port"
Google -> SGIX: "Connects at 100G port"
Singtel <-> Google: "Exchange traffic for free\nSettlement-free peering"
```

Singtel peers with Google: Singtel customers get fast YouTube, Google offloads transit costs. Both win.

The general rule: if traffic between two networks is roughly balanced in volume, they peer for free. If one network sends much more than it receives, the other may demand payment.

## The Economic Difference

```d2
# Diagram 3: Transit vs Peering Cost Structure
direction: down

Cost: "Cost per bit"
Transit: "Transit\nPay per Mbps\n~$1-5/Mbps"
Peering: "Peering\nFree\n(one-time port cost)"
Size: "Network Size"

Cost -> Transit
Cost -> Peering
```

A network that:
- **Peers a lot** has low marginal cost for traffic. Their only cost is the physical port at the exchange.
- **Buys transit** pays for every Mbps. More traffic = more money.

This is why Singtel (peers with everyone) can offer generous bandwidth while Simba (buys transit) has tighter margins.

## The Settlement-Free Peering Club

The Tier 1 networks (see my earlier post) are defined by this: they peer with every other Tier 1 for free and never buy transit from anyone. They're the top of the pyramid — everyone below them pays.

```d2
# Diagram 4: The Peering Hierarchy
direction: down

T1: "Tier 1 Networks\nPeer with each other for free"
T2_Peer: "Large Tier 2\nPeer with Tier 1s, peer with content"
T2_Pay: "Small Tier 2\nPeer locally, pay for international"
T3: "Tier 3 Resellers\nPure transit customers"

T1 -> T1: "Settlement-free"
T2_Peer -> T1: "Mixed (pay + peer)"
T2_Pay -> T1: "Mostly pay"
T3 -> T2_Pay: "100% transit"
```

## Why This Matters for You

When you visit a website:

1. **If the content is cached locally** (Google, Netflix, Akamai) — the ISP peers directly or via SGIX. No transit cost. Fast.
2. **If the content is overseas on a peered network** — still fast, no transit cost.
3. **If the content is on a network the ISP has no peering with** — transit cost incurred. Traffic goes through a Tier 1.

For popular content, premium ISPs (Singtel, StarHub) peer directly. Budget ISPs (Simba) handle most traffic via transit.

The difference for you is negligible for everyday browsing. But during peak hours, transit-heavy ISPs can feel the pinch as their purchased bandwidth runs thin while peered ISPs hum along unaffected.

*Related: [Tier 1 and Tier 2 Networks Explained](./tier-1-2-networks-explained.html)*
