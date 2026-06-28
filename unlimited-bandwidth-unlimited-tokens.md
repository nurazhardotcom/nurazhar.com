Title: Unlimited Bandwidth and Unlimited Tokens — They're the Same Lie
Date: 2026-06-28
Tags: llm, tokens, bandwidth, analogy, infrastructure, oversubscription, d2
Description: How ISPs and LLM providers both sell "unlimited" based on the same oversubscription math.

---

While researching ISP pricing and oversubscription, I noticed something familiar. The same math that makes "unlimited 10 Gbps" work for broadband also makes "unlimited tokens" work for LLM inference. Both are selling potential capacity they know you can't fully consume.

## The ISP Model

An ISP sells 100 households "unlimited 10 Gbps" on a shared 10 Gbps pipe. They bet that at any given moment, only a fraction of those households are actively using bandwidth. The math holds because:
- Most browsing uses < 5 Mbps
- Peak hour lasts 2-3 hours
- Even "heavy" use is bursty

```d2
# Diagram 1: ISP Oversubscription
direction: down

Pipe: "ISP Pipe\n10 Gbps capacity"
Users: "100 Customers\nEach sold 'unlimited 10 Gbps'"
Active: "Peak Hour Usage\n~20% active"
Idle: "80% idle or low usage"

Pipe -> Users: "Contention ratio is hidden"
Users -> Active: "Statistical multiplexing"
Active -> Idle: "Shared capacity works"
```

## The LLM Model

Now look at a GPT-5.6 inference API. They provision GPUs that can handle X tokens per second. They sell "unlimited tokens" or generous rate limits to Y customers where Y × typical_usage ≫ X.

The bet:
- Most users send short queries
- Peak hours are predictable (work hours in their region)
- Even "heavy" users average far below their peak burst
- Real-time inference is bursty (you think for 10 seconds, then send a prompt in 2 seconds)

```d2
# Diagram 2: LLM Oversubscription
direction: down

GPUs: "GPU Cluster\nX tokens/second capacity"
API: "API Users\nEach sold 'high rate limit'"
Active: "Peak Usage\nSome users active"
Idle: "Most users idle or thinking"

GPUs -> API: "Oversubscribed similarly"
API -> Active: "Statistical multiplexing"
Active -> Idle: "Users pause between prompts"
```

## Key Parallel

| Concept | ISP World | LLM World |
|---|---|---|
| The pipe | GPON port / submarine cable | GPU cluster / inference server |
| The product | Unlimited 10 Gbps | Unlimited tokens |
| The bet | Most users are idle | Most users are thinking |
| Peak time | 8 PM weekdays | US/EU business hours |
| Contention | 24:1 (Simba) | Unknown, but similar |
| Hardware cap | Wi-Fi / Ethernet | Token context window |
| What you actually get | 10 Gbps / 24 = 400 Mbps | X tokens / Y users |

## The Hardware Cap — You Can't Consume It

Just as Wi-Fi caps your real bandwidth at ~270 Mbps regardless of plan, your **reading speed** caps your token consumption. Even with the fastest LLM:

- A human reads at ~200-300 words per minute
- A good LLM generates ~50-100 tokens per second
- A 1,000-token response takes 10-20 seconds to generate
- You then spend 30-60 seconds reading it

Your consumption rate is far below what the API can deliver. The provider knows this.

```d2
# Diagram 3: The Human Bottleneck
direction: down

LLM: "LLM API\n500+ tokens/sec burst"
Output: "Generated Response\n1,000 tokens"
You: "You reading\n200-300 words/min"
Actual: "Actual throughput\n~30 tokens/sec sustained"

LLM -> Output: "Fast generation"
Output -> You: "Slow consumption"
You -> Actual: "Human reading is the bottleneck"
```

## The Economic Incentive Is the Same

ISP:
- Sell big plans people don't need
- Profit from users who never saturate
- Raise prices only when contention breaks

LLM Provider:
- Sell "unlimited" API tiers
- Profit from users who hit rate limits rarely
- Introduce tiered pricing when usage spikes

Both are selling **headroom** based on the statistical reality that most customers don't use what they pay for.

```d2
# Diagram 4: The Common Pattern
direction: down

Sell: "Sell 'unlimited' capacity"
Bet: "Bet on statistical multiplexing"
Profit: "Profit from unused headroom"
Crisis: "When everyone uses it\nsimultaneously → Raised prices,\nrationing, or degraded service"

Sell -> Bet: ""
Bet -> Profit: ""
Profit -> Crisis: "The only failure mode"
```

## The Difference

There is one difference: LLM inference can't be as aggressively oversubscribed as bandwidth because inference is **compute-bound** not pipe-bound.

A GPON port can handle 24 homes because idle customers use near-zero bandwidth. LLM inference GPUs idle at near-zero utilization too — but the *burst* when a customer sends a prompt is much more intense relative to capacity. A single ChatGPT query can use hundreds of GPU-seconds.

So LLM providers are more cautious with oversubscription. But the principle is identical: they're selling you access to a shared pool, not dedicated capacity.

## What This Means

When you see "unlimited tokens" or "unlimited broadband," mentally translate it to: **"as many as we can give you before you notice the contention."**

For broadband, you don't notice because your Wi-Fi caps you before the contention does.

For LLMs, you don't notice because you read slower than the model generates.

In both cases, the bottleneck is **you** — not the provider.

*Related: [Oversubscription and "Unlimited" Marketing — The Hidden Math](./oversubscription-and-unlimited-marketing.html)*
