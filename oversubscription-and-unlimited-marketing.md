Title: Oversubscription and "Unlimited" Marketing — The Hidden Math of Broadband
Date: 2026-06-28
Tags: networking, oversubscription, unlimited, broadband, isp, infrastructure, d2
Description: Why "unlimited" internet isn't unlimited and how ISP math keeps the pipes from bursting.

---

Every fibre broadband plan in Singapore claims to be "unlimited." No data caps, no throttling. But there's a dirty secret: the physical pipe coming into your neighbourhood is finite. The math only works because ISPs bet you won't all use it at the same time.

## The Contradiction

An ISP buys a 10 Gbps connection to serve a neighbourhood. They sell "unlimited 10 Gbps" to 100 households in that neighbourhood for $30 each.

That's $3,000/month revenue from 100 customers, but the pipe is only 10 Gbps. If all 100 customers simultaneously ran a speed test, each would get 100 Mbps — not 10 Gbps. The plan is mathematically impossible to deliver to everyone at once.

```d2
# Diagram 1: The Oversubscription Problem
direction: down

ISP: "ISP Backbone\n10 Gbps pipe"
House1: "House 1\n'Unlimited 10 Gbps'"
House2: "House 2\n'Unlimited 10 Gbps'"
House3: "House 3\n'Unlimited 10 Gbps'"
Houses: "House 4-100\n'Unlimited 10 Gbps'"

ISP -> House1: "If all active at once:\n10 Gbps / 100 = 100 Mbps each"
ISP -> House2: ""
ISP -> House3: ""
ISP -> Houses: ""
```

## The Statistical Multiplexing Bet

ISPs rely on **statistical multiplexing** — the observation that not everyone uses their full bandwidth simultaneously.

At 8 PM in a typical housing block:
- 20 households are watching Netflix (~25 Mbps each)
- 5 are gaming (~20 Mbps each)
- 10 are browsing social media (~5 Mbps each)
- 10 are on Zoom calls (~10 Mbps each)
- 55 are doing nothing significant (~1 Mbps each)

Total simultaneous usage: 20×25 + 5×20 + 10×5 + 10×10 + 55×1 = ~955 Mbps — well within the 10 Gbps pipe.

```d2
# Diagram 2: Statistical Multiplexing
direction: down

Time: "8 PM in a housing block"
Netflix: "20 households\nNetflix 4K\n25 Mbps each = 500 Mbps"
Gaming: "5 households\nOnline gaming\n20 Mbps each = 100 Mbps"
Social: "10 households\nBrowsing\n5 Mbps each = 50 Mbps"
Zoom: "10 households\nVideo calls\n10 Mbps each = 100 Mbps"
Idle: "55 households\nIdle/email\n1 Mbps each = 55 Mbps"
Total: "Total: ~805 Mbps\nPipe: 10 Gbps\nUtilization: 8%"

Time -> Netflix
Time -> Gaming
Time -> Social
Time -> Zoom
Time -> Idle
Netflix -> Total
Gaming -> Total
Social -> Total
Zoom -> Total
Idle -> Total
```

At 8% utilization, the pipe is nearly empty. The ISP could sell 10x more "unlimited" plans and still be fine.

## The Contention Ratio

The oversubscription ratio (or contention ratio) is how many customers share a port:

| ISP | Typical Contention | Peak Headroom |
|---|---|---|
| Singtel GPON | ~8-12:1 | Comfortable |
| Simba GPON | 24:1 (per their own docs) | Tight |
| Typical residential | 20-50:1 | Aggressive |

Simba's documentation admits: *"a single 10Gbps port is shared by up to 24 users... the theoretical lowest speed at any time could be as low as ~400 Mbps."*

```d2
# Diagram 3: Contention Ratio Comparison
direction: down

Singtel: "Singtel\n1 GPON port\n8-12 homes"
Simba: "Simba\n1 GPON port\n24 homes"
S1: "Each home:\n~800-1250 Mbps peak"
S2: "Each home:\n~400 Mbps peak"

Singtel -> S1: ""
Simba -> S2: ""
```

## Why Unlimited Works Despite the Math

Three reasons "unlimited" doesn't collapse the network:

### 1. Peak Hours Are Short

The 8 PM crunch lasts 2-3 hours. The rest of the day, utilization is under 5%. ISPs buy enough transit to handle the peaks, but they don't overprovision for worst-case scenarios.

### 2. Applications Don't Actually Need 10 Gbps

| Activity | Bandwidth Needed |
|---|---|
| 4K Netflix | 25 Mbps |
| Zoom 4K | 15 Mbps |
| Web browsing | 1-5 Mbps |
| Steam download | Burst then idle |
| YouTube 1080p | 5 Mbps |

Even heavy users rarely sustain more than 100 Mbps for more than a few minutes.

### 3. You Can't Physically Consume It

Your Wi-Fi caps at ~270 Mbps (as I showed in my connection audit). Even wired Gigabit Ethernet caps at ~940 Mbps. To use even 1 Gbps, you need a 2.5 GbE NIC and a willing server on the other end — most people don't have this.

```d2
# Diagram 4: The Real Bottleneck
direction: down

Plan: "Your Plan\n2 Gbps / 10 Gbps"
ONT: "ONT Port\n1 Gbps"
WiFi: "Wi-Fi Link\n~300 Mbps PHY"
You: "You\n~270 Mbps real"

Plan -> ONT: "Plan maxes at port speed"
ONT -> WiFi: "Gigabit port feeds AP"
WiFi -> You: "Air interface is bottleneck"
```

## The Truth

"Unlimited" doesn't mean unlimited. It means "unlimited at the oversubscribed ratio we've calculated you won't exceed, based on decades of usage statistics."

The plans work because:
- You don't use your full speed most of the time
- Even when you try, your hardware caps you
- ISPs have decades of data on peak usage patterns

The only time this breaks is when there's a massive shift in behavior (like everyone switching to 4K streaming overnight) or when an ISP oversubscribes too aggressively (hello, Simba 24:1).

*Related: [Hardware Bottlenecks — Why Your Wi-Fi Caps Your Speed](./hardware-bottlenecks-wifi-vs-plan.html)*
