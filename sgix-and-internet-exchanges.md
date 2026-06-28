Title: SGIX and Internet Exchanges — Where Networks Meet
Date: 2026-06-28
Tags: sgix, internet-exchange, peering, networking, singapore, infrastructure, d2
Description: How the Singapore Internet Exchange keeps local traffic local and why it matters.

---

The internet is a network of networks. Those networks need a place to meet and exchange traffic. That place is called an **Internet Exchange Point (IXP)**.

Singapore has two major IXPs: **SGIX** (Singapore Internet Exchange) and **Equinix Internet Exchange Singapore**. SGIX is the larger of the two by participant count, and it plays a pivotal role in keeping Singapore's internet fast and cheap.

## What an IXP Does

Without an IXP, if a Singtel user wanted to visit a StarHub-hosted website, the traffic would have to:

1. Leave Singtel's network
2. Travel to a Tier 1 transit provider in Hong Kong or Tokyo
3. Come back to StarHub in Singapore

That's absurd — two packets in the same city taking an international round trip.

```d2
# Diagram 1: Without an IXP (The Wrong Way)
direction: down

User: "Singtel User\nSingapore"
Singtel: "Singtel Network"
Transit: "Tier 1 Transit\nHong Kong"
Starhub: "StarHub Network"
Server: "StarHub Server\nSingapore"

User -> Singtel: "Request"
Singtel -> Transit: "No local peering\nMust go abroad"
Transit -> Starhub: "Back to Singapore"
Starhub -> Server: ""
```

With an IXP, both networks connect to a shared switch in a Singapore data centre. Traffic goes directly from one to the other — it never leaves the country.

```d2
# Diagram 2: With SGIX (The Right Way)
direction: down

User: "Singtel User"
Singtel: "Singtel Network"
SGIX: "SGIX\n(Singapore Data Centre)"
Starhub: "StarHub Network"
Server: "StarHub Server"

User -> Singtel: ""
Singtel -> SGIX: "Direct peering via IXP switch"
SGIX -> Starhub: ""
Starhub -> Server: ""
```

Latency drops from 100ms+ (via Hong Kong) to under 2ms. And neither ISP pays transit fees for this traffic.

## SGIX by the Numbers

Founded in 2010 by IMDA (the Singapore regulator) as a not-for-profit, member-owned exchange:

- **180+ peers** (ISPs, content providers, CDNs, gaming companies)
- **10+ data centre locations** across Singapore
- **Neutral and open** — any network can join
- **Not-for-profit** — operates on cost recovery, not margin

```d2
# Diagram 3: SGIX Peering Community
direction: down

SGIX: "SGIX\nNeutral Exchange Fabric"
Telcos: "Telcos\nSingtel, StarHub, M1, Simba"
Cloud: "Cloud & CDN\nGoogle, Amazon, Cloudflare"
Content: "Content\nNetflix, Meta, Akamai"
Gaming: "Gaming\nValve, Riot, Tencent"

Telcos -> SGIX: "Peer at 100G+ ports"
Cloud -> SGIX: ""
Content -> SGIX: ""
Gaming -> SGIX: ""
```

## Why Singapore Needed Its Own IXP

Before SGIX, Singapore ISPs routed local traffic through international exchanges because peering directly was expensive and complex. The IMDA recognized this was hurting Singapore's ambition to be a digital hub.

SGIX was designed to:
- **Lower costs** — ISPs pay less transit fees
- **Reduce latency** — local traffic stays local
- **Attract content** — Netflix, Google, and others set up local caches
- **Increase resilience** — cable outages don't affect local traffic

```d2
# Diagram 4: Benefits of Local IXP
direction: down

SGIX: "SGIX"
Cost: "Lower transit costs\nTraffic stays local"
Speed: "Faster speeds\n< 2ms local latency"
Resilience: "Cable outage resilience\nLocal traffic uninterrupted"
Attract: "Content & DC investment\nGoogle, Netflix host locally"

SGIX -> Cost: ""
SGIX -> Speed: ""
SGIX -> Resilience: ""
SGIX -> Attract: ""
```

## What This Means for You

When you visit a popular website as a Singtel user, SGIX silently handles the exchange:

- **YouTube** — Google peers at SGIX. Traffic stays in Singapore.
- **Netflix** — Open Connect appliance peers via IXP. Local delivery.
- **WhatsApp / Instagram** — Meta peers at SGIX.

This is why your latency is 4ms. SGIX is invisible to you, but it's the reason your local internet feels snappy regardless of which ISP you're on.

The Simba difference: Simba is also at SGIX, so local content peering is just as fast. Their disadvantage isn't local — it's international transit, where they rely on paid upstream connections instead of their own infrastructure.

*Related: [Transit vs Peering — The Economics of Internet Connectivity](./transit-vs-peering.html)*
