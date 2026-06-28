Title: Hardware Bottlenecks — Why Your Wi-Fi Caps Your Speed, Not Your Plan
Date: 2026-06-28
Tags: networking, wifi, hardware, bottleneck, speed, infrastructure, d2
Description: Why upgrading to 10Gbps won't make your laptop any faster and what actually limits your throughput.

---

This is the most important networking lesson I've learned: **your internet plan is never the bottleneck if you're on Wi-Fi.**

I proved this to myself by switching from Simba 10Gbps to Singtel 2Gbps. The speed was identical — ~270 Mbps in both cases. Not because the plans are the same, but because my laptop's Wi-Fi card hit its ceiling at that speed and couldn't go higher regardless of what the ISP was offering.

## The Throughput Chain

Every byte you download travels through a chain of links. Your connection is only as fast as the **slowest link** in that chain.

```d2
# Diagram 1: The Throughput Chain
direction: down

Server: "Remote Server\nYouTube / Netflix"
Internet: "Internet Backbone\n10 Gbps+"
ONT: "ONT Port\n1 Gbps"
WiFi: "Wi-Fi Link\n~288 Mbps PHY"
Laptop: "Your Laptop\n~270 Mbps real"

Server -> Internet: "∞ (not the bottleneck)"
Internet -> ONT: "Your plan speed (2-10 Gbps)"
ONT -> WiFi: "Gigabit Ethernet (940 Mbps)"
WiFi -> Laptop: "Wi-Fi 6 at -65 dBm (288 Mbps PHY)"
```

The slowest link determines your speed. If that's your Wi-Fi (288 Mbps PHY), you'll get ~270 Mbps regardless of whether your plan is 2 Gbps or 10 Gbps.

## Why Wi-Fi Is Always the Bottleneck

Wi-Fi is shared, half-duplex, and susceptible to interference. Even with Wi-Fi 6:

| Wi-Fi Generation | PHY Rate (2 streams) | Real Throughput | Bottleneck vs 1 Gbps Plan |
|---|---|---|---|
| Wi-Fi 4 (802.11n) | 300 Mbps | ~150 Mbps | Severe |
| Wi-Fi 5 (802.11ac) | 867 Mbps | ~400 Mbps | Significant |
| **Wi-Fi 6 (802.11ax)** | **1.2 Gbps** | **~600 Mbps** | **Moderate** |
| Wi-Fi 6E | 2.4 Gbps | ~900 Mbps | Mild |
| Wi-Fi 7 | 2.9+ Gbps | ~1.5+ Gbps | None (for now) |

My laptop has a Realtek RTL8852BE (Wi-Fi 6, 2×2 MIMO, 80 MHz channel). At -65 dBm signal:
- Theoretical PHY: 288 Mbps
- Real throughput: ~270 Mbps

```d2
# Diagram 2: Wi-Fi PHY vs Real Throughput
direction: down

PHY: "PHY Rate\n288 Mbps"
Overhead: "Protocol Overhead\nTCP/IP, 802.11, retransmissions"
Contention: "Air Contention\nOther devices, neighbours"
Real: "Real Throughput\n~270 Mbps"

PHY -> Overhead: "~10-15% loss"
Overhead -> Contention: "Another ~5-10%"
Contention -> Real: ""
```

## Ethernet — The Forgotten Option

Plug in a cable and the picture changes completely.

I ran a test on my machine:
- **Single-stream Wi-Fi**: ~147 Mbps
- **Multi-stream Wi-Fi (3 streams)**: ~273 Mbps
- **Gigabit Ethernet**: ~940 Mbps (theoretical, if I had a cable)

The difference isn't the plan — it's the medium.

```d2
# Diagram 3: Wi-Fi vs Ethernet
direction: down

Plan: "2 Gbps Plan"
WifiResult: "Wi-Fi 6\n~270 Mbps"
EthResult: "Gigabit Ethernet\n~940 Mbps"

Plan -> WifiResult: "Wireless bottleneck"
Plan -> EthResult: "Full port speed"
```

## The Real Test: Saturation

To find your true max, run parallel downloads. A single TCP stream might not saturate the link:

```bash
# Run 3 parallel downloads from a fast server
for i in 1 2 3; do
  curl -o /dev/null -s \
    http://speedtest.singapore.linode.com/100MB-singapore.bin &
done
wait
```

On my machine, this hit ~273 Mbps total — the Wi-Fi link was fully saturated. No single-stream test could achieve this because TCP overhead and Wi-Fi's half-duplex nature limited each stream.

## What This Means

| If you're on... | Your max real speed | Upgrade to 10Gbps? |
|---|---|---|
| Wi-Fi 5 | ~200-400 Mbps | Won't help |
| Wi-Fi 6 (2 streams) | ~500-600 Mbps | Won't help |
| Wi-Fi 6E | ~800-900 Mbps | Would help slightly |
| Wi-Fi 7 | ~1.5+ Gbps | Would help |
| Gigabit Ethernet | ~940 Mbps | Would help |
| 2.5 GbE Ethernet | ~2.3 Gbps | Would help |

If you're on Wi-Fi 6 or older, **switching to a faster plan changes nothing**. The bottleneck is the wireless link, not the subscription.

The only practical ways to get more speed:
1. Get closer to the router (better signal = higher PHY rate)
2. Use Ethernet (USB-C adapter is ~$15)
3. Upgrade to Wi-Fi 7 (requires new laptop and new router)

For most people on Wi-Fi, the cheapest 10 Gbps plan is as fast (and as slow) as the most expensive one. Don't let the marketing fool you.

*Related: [Oversubscription and "Unlimited" Marketing — The Hidden Math](./oversubscription-and-unlimited-marketing.html)*
