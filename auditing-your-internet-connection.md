Title: Auditing Your Internet Connection — What Every Packet Reveals
Date: 2026-06-28
Tags: networking, diagnostics, singtel, wifi, infrastructure, d2
Description: How to inspect your internet connection from the command line and understand what each hop means.

---

A few weeks ago I wanted to know exactly what my internet connection looked like. Not just "am I getting my plan speed" but the full path: my Wi-Fi link quality, the ONT model, GPON signal levels, the ISP's backbone, and where my packets go after they leave my house.

Turns out, almost all of this is visible from a Linux terminal.

## Start With the Wi-Fi Link

Before blaming your ISP, check the actual link between your machine and the router:

```bash
iw dev wlan0 link
```

This shows your SSID, frequency, signal strength, and PHY rate. My output:

```
signal: -65 dBm
rx bitrate: 288 MBit/s HE-MCS 5
tx bitrate: 51 MBit/s HE-MCS 4
```

The PHY rate (288 Mbps) is the theoretical max at this signal level. Real TCP throughput is roughly 50-70% of that after overhead and contention.

```d2
# Diagram 1: Wi-Fi Link Anatomy
direction: down

Laptop: "Your Laptop\nRTL8852BE Wi-Fi 6"
AP: "Access Point\n(Huawei HG8240T5)"
Air: "5 GHz 802.11ax\n80 MHz channel\n-65 dBm"

Laptop -> Air: "PHY: 288 Mbps\nReal: ~150-270 Mbps"
Air -> AP: "Signal: -65 dBm\nChannel 36"
```

## Find Your Modem Model

Your gateway is at `192.168.1.254` (or whatever your DHCP server says). Hit it in a browser, or poke at it from the terminal:

```bash
curl -s http://192.168.1.254/ | grep -i "product\|model\|huawei"
```

I found mine is a **Huawei HG8240T5** — a GPON ONT with an integrated router and Wi-Fi AP, Singtel-branded firmware. This little box terminates the fibre from the exchange.

## Check Your Optical Signal

GPON (Gigabit Passive Optical Network) uses a splitter in the field — one fibre from the exchange splits to serve multiple homes. The ONT reports its optical levels if you can reach the admin panel.

The key metrics:

| Metric | Good Range | My Value |
|---|---|---|
| RX Power | -27 to -8 dBm | -15 dBm |
| TX Power | 0.5 to 5 dBm | 1.93 dBm |
| Temperature | -10 to 85 °C | 60 °C |

-15 dBm RX is excellent. You only start seeing bit errors below -25 dBm.

```d2
# Diagram 2: GPON Signal Path
direction: down

ONT: "Your ONT\nHG8240T5"
Splitter: "GPON Splitter\n1:32 or 1:64 ratio"
OLT: "Singtel OLT\nAt Exchange"
BRAS: "Singtel BRAS/BNG"
Internet: "The Internet"

ONT -> Splitter: "1310 nm TX (1.93 dBm)\n1490 nm RX (-15 dBm)"
Splitter -> OLT: "GPON encapsulation"
OLT -> BRAS: "VLAN aggregation"
BRAS -> Internet: "Public IP assigned"
```

## Trace the Path

A simple `ping` and `traceroute` reveals more than you'd think:

```bash
ping -c 10 8.8.8.8
traceroute -n 8.8.8.8
```

My latency to Google DNS averages **4 ms**. That's remarkable — it means the exchange is very close (likely within a few kilometres) and the GPON network has minimal oversubscription.

```d2
# Diagram 3: Full Network Stack
direction: down

PC: "This Machine\n192.168.1.74/24"
ONT: "Huawei HG8240T5\n192.168.1.254"
OLT: "Singtel OLT\nUlu Bedok Exchange"
Edge: "Singtel IP Edge\n116.15.188.9"
Internet: "Internet\n8.8.8.8 @ 4ms"

PC -> ONT: "Wi-Fi 6\n-65 dBm"
ONT -> OLT: "GPON Fibre\n~1-2km"
OLT -> Edge: "Metro Backbone"
Edge -> Internet: "Tier 1 Transit"
```

## Measure Real Throughput

Speed test websites are fine but `curl` gives you the raw number without browser overhead:

```bash
curl -o /dev/null -w "%{speed_download}\n" \
  http://speedtest.singapore.linode.com/100MB-singapore.bin
```

Convert to Mbps: `bytes_per_sec × 8 ÷ 1,000,000`.

My Wi-Fi test: ~147 Mbps. A parallel test with 3 concurrent streams hit ~273 Mbps — the Wi-Fi link was saturated.

```d2
# Diagram 4: Where Your Speed Gets Lost
direction: down

Plan: "Plan Speed\n2 Gbps"
ONT: "ONT Port 1\n1 Gbps max"
WiFi: "Wi-Fi 6 Link\n288 Mbps PHY"
Real: "Real Throughput\n~270 Mbps"

Plan -> ONT: "Aggregate across 4 ports"
ONT -> WiFi: "Gigabit Ethernet to AP"
WiFi -> Real: "Overhead, contention, signal"
```

## Key Takeaway

Your internet connection is a chain. The weakest link determines your experience. For most people on Wi-Fi, **the plan speed is irrelevant** — the wireless link is the bottleneck, not the subscription.

*Related: [Hardware Bottlenecks — Why Your Wi-Fi Caps Your Speed](./hardware-bottlenecks-wifi-vs-plan.html)*
