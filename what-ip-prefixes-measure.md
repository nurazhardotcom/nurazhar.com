Title: What IP Prefixes Measure — And Why 100,000 Is a Big Deal
Date: 2026-06-28
Tags: networking, bgp, prefixes, peering, ip, infrastructure, d2
Description: How the number of IP prefixes an ISP announces reveals the true size of their network.

---

When I first read that Singtel has ~100,000 IPv4 prefixes while Simba has ~40, the number itself meant nothing. What does a "prefix" even measure? And why would a Singapore ISP have more prefixes than Malaysia's TM (10,000) or Indonesia's Telkom (10,000)?

## What Is a Prefix?

A prefix is a block of IP addresses that a network owns and announces to the internet via BGP.

Think of it like **telephone number ranges**:

- A small ISP might own one prefix: `192.0.2.0/24` — that's 256 IP addresses
- A large ISP might own thousands of prefixes across many countries
- Each prefix is a block they can route traffic to without going through a middleman

The notation `192.0.2.0/24` means "the 256 addresses from 192.0.2.0 to 192.0.2.255."

```d2
# Diagram 1: What a Prefix Is
direction: down

Block: "Prefix: 192.0.2.0/24\n256 IP addresses"
Owner: "Owned by: Singtel"
BGP: "Announced via BGP:\n'I own these, send traffic directly'"
Peers: "Other Networks\nConnect directly to Singtel"

Owner -> BGP: "Advertises to the internet"
BGP -> Peers: "Direct route established"
```

## Why More Prefixes = Bigger Network

When an ISP has many prefixes, it means:

1. **They own more IP address space** — they acquired it early or bought it
2. **They have more customers** — each customer or facility needs IP blocks
3. **They are present in more locations** — different countries, data centres, submarine cable landing stations
4. **They peer directly with more networks** — instead of buying transit, they exchange traffic directly

```d2
# Diagram 2: ISP Scale Comparison
direction: down

Singtel: "Singtel (AS7473)\n~100K IPv4 prefixes\n~15K IPv6 prefixes"
TM: "Telekom Malaysia (AS4788)\n~10K IPv4 prefixes"
Telkom: "Telkom Indonesia (AS7713)\n~10K IPv4 prefixes\n~15K IPv6 prefixes"
Indosat: "Indosat (AS4761)\n~3K IPv4 prefixes"
Simba: "Simba (AS4817)\n~40 IPv4 prefixes"

Singtel -> TM: "10x larger"
Singtel -> Telkom: "10x larger"
Singtel -> Indosat: "33x larger"
Singtel -> Simba: "2500x larger"
```

## Why Does Small Singapore Have Such a Big ISP?

This is the counterintuitive part. Singapore has 5.6 million people. Malaysia has 34 million. Indonesia has 280 million. Yet Singtel has **10x the prefixes** of TM or Telkom.

The reason: **Singtel is not just a local ISP**. It's a **regional transit provider**. Those 100K prefixes include:

- IP space for Singtel's own customers in Singapore
- IP space for the submarine cables Singtel owns or co-owns
- IP space for wholesale transit customers across Southeast Asia
- IP space for data centres and infrastructure Singtel operates globally

```d2
# Diagram 3: Singtel's Prefix Breakdown
direction: down

Total: "100,000 Prefixes"
Local: "Singapore Consumers\n~10-15%"
Cables: "Submarine Cable Infrastructure\n~20-25%"
Transit: "Wholesale Transit Customers\nAcross Southeast Asia\n~40-50%"
DataCenters: "Data Centres & Peering\n~15-20%"

Total -> Local
Total -> Cables
Total -> Transit
Total -> DataCenters
```

Malaysia's TM and Indonesia's Telkom primarily serve their domestic markets. Singtel, by historical accident and deliberate government strategy, became the region's backbone carrier.

## The Practical Impact

More prefixes means:

- **Fewer hops** to reach destinations (direct peering)
- **Better redundancy** (more paths available)
- **Better peering leverage** (content providers want to connect directly)

But for you, the end user in Singapore, loading `google.com` at 4ms latency — none of this matters. The route is already optimal.

## Prefixes as a Proxy for Network Quality

Prefix count isn't a perfect metric. A network could announce many small prefixes and look bigger than it is (prefix hijacking used to be a problem). But combined with AS rank (which measures how central a network is to internet routing), it's a decent proxy for network size and importance.

On the CAIDA AS rank, Hurricane Electric (AS6939) sits at #6 globally with 210,000 prefixes and connections to ~7,700 other networks. Singtel doesn't crack the top tier — it's a large regional player, not a global backbone.

*Related: [Tier 1 and Tier 2 Networks Explained](./tier-1-2-networks-explained.html)*
