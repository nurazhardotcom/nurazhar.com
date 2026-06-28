Title: Singtel vs Simba — What You Actually Pay For in a Fibre Plan
Date: 2026-06-28
Tags: singtel, simba, broadband, comparison, networking, infrastructure, d2
Description: Why one ISP costs $42 and another costs $30 for 10Gbps, and what the price difference actually buys you.

---

On paper, Simba's 10Gbps at $29.99 looks unbeatable. Same speed as Singtel's 2Gbps at $42.02? The math writes itself.

But I switched from Simba back to Singtel and noticed zero difference in everyday speed. That's because over Wi-Fi, both plans deliver the same ~270 Mbps. The difference isn't speed — it's **everything else**.

## What You're Paying For

```d2
# Diagram 1: ISP Cost Breakdown
direction: down

Price: "Your Monthly Fee"
Infrastructure: "Physical Network\nSubmarine cables, fibre, data centres"
Peering: "Peering & Transit\nAgreements with other networks"
Support: "Support Staff\n24/7 technicians, NOC"
Overhead: "Overhead\nBilling, compliance, profit margin"

Price -> Infrastructure
Price -> Peering
Price -> Support
Price -> Overhead
```

Singtel spends heavily on all four. Simba minimizes the bottom three.

## The Network Difference

```d2
# Diagram 2: Singtel Network Architecture
direction: down

You: "Your Home\nHG8240T5"
Local: "Singtel OLT\nLow contention"
Backbone: "Singtel Backbone\nOwn submarine cables\n100K prefixes"
Peers: "Direct Peering\nGoogle, Netflix, Akamai"

You -> Local: "Low oversubscription"
Local -> Backbone: "Singtel owns the fibre"
Backbone -> Peers: "Settlement-free peering"
```

```d2
# Diagram 3: Simba Network Architecture
direction: down

You: "Your Home\nGeneric ONT"
Splitter: "GPON Splitter\n24:1 oversubscription"
Upstream: "Buys Transit from\nTier 1 providers"
CGNAT: "Carrier-Grade NAT\nShared public IP"

You -> Splitter: "Shared 10Gbps port\nwith 23 other homes"
Splitter -> Upstream: "No own backbone"
Upstream -> CGNAT: "No public IP"
```

### Contention Ratio

This is the hidden variable. A GPON port has finite capacity. The ISP decides how many homes share it.

| ISP | Contention | Peak Throughput per User |
|---|---|---|
| Singtel | Conservative (~8-12:1) | ~500+ Mbps |
| Simba | Aggressive (24:1) | As low as ~400 Mbps |

Simba themselves admit this in their support docs: *"a single 10Gbps port is shared by up to 24 users... the theoretical lowest speed at any time could be as low as ~400 Mbps."*

### CGNAT vs Public IP

Simba uses Carrier-Grade NAT. You share a public IPv4 address with other customers. This means:
- No port forwarding
- Can't host anything
- Some online games have NAT issues
- No remote access to your home network

Singtel gives you a dedicated public IP. You can run a server, access your NAS remotely, and your PS5 will have Open NAT.

### Support

Simba has basically no support. No hotline. If your internet goes down at 2 AM, you're waiting until someone reads your forum post. Singtel has 24/7 phone support and can dispatch a technician.

## The Real Question

Are you paying for **speed** or **reliability**?

For my use case (laptop on Wi-Fi), both deliver the same speed. But I value the public IP, the peace of mind that it just works, and knowing that if something breaks, it gets fixed.

If you're on a tight budget and don't care about any of that, Simba works fine most of the time. Just know what you're trading away for that $12/month savings.

*Related: [Transit vs Peering — The Economics of Internet Connectivity](./transit-vs-peering.html)*
