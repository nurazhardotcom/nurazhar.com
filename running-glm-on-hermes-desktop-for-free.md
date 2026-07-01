Title: The $0 GLM Hack — Running GLM-4.7 on Hermes Desktop for Free
Date: 2026-06-30
Tags: glm, hermes, zhipu, ai, inference, free-tier, cachyos, hack, guide
Description: Z.AI does not expose a separate model ID for their free GLM-4.7-Flash tier. You just call glm-4.7 with a free account and it works. Here is how to use it from Hermes Desktop at zero cost.

---

# The $0 GLM Hack — Running GLM-4.7 on Hermes Desktop for Free

## The Trap

You find `unsloth/GLM-5.2-GGUF` on Hugging Face. MIT licensed. Free to download. 466 GB (Q4_K_M) down to 217 GB (IQ1_S). You think: I can run this.

You cannot run this.

## The Numbers

My machine — CachyOS, Ryzen 7 7730U, 15 GB RAM, integrated AMD graphics:

| Resource | Available | GLM-5.2 IQ1_S minimum | Gap |
|----------|-----------|----------------------|-----|
| RAM | 15 GB | 256 GB | 17x short |
| VRAM | ~2 GB shared | 80+ GB (GPU offload) | 40x short |
| Disk | 317 GB free | 217 GB (just the model) | barely fits, no room for swap |

A 5.2-trillion-parameter MoE model does not run on a laptop.

## The Hack

Zhipu AI operates `z.ai` — an OpenAI-compatible API. Their pricing page lists **GLM-4.7-Flash** as permanently free ($0/M tokens, no credit card).

But Z.AI's API does **not** expose `glm-4.7-flash` as a separate model ID. The model name on their API is simply `glm-4.7`. Free accounts calling `glm-4.7` get routed to the Flash variant automatically. Paid accounts calling the same model ID get the full GLM-4.7.

The result: you type `glm-4.7` — you get free GLM-4.7-Flash. There is no separate model selector for the free tier. That is the hack.

Hermes Desktop has a built-in `zai` provider. Total setup time: 2 minutes.

## Architecture

```d2
direction: right

FREE_TIER: "Z.AI Free Tier\napi.z.ai" {
  shape: cloud
  GLM47: "glm-4.7\n(Flash for\nfree accounts)"
}

YOUR_MACHINE: "CachyOS (this laptop)" {
  HERMES: "Hermes Desktop\nconfig.yaml"
  CACHE: "GLM_API_KEY\nin ~/.hermes/.env"
  HERMES -> CACHE: "reads secret"
}

HERMES -> FREE_TIER: "model: glm-4.7\nprovider: zai"

AGENT: "You\n(terminal / Hermes TUI)"

AGENT -> HERMES: "chat, code, tools"
HERMES -> AGENT: "GLM-4.7-Flash responses"
```

## Step-by-Step

### 1. Create a Z.AI account

```
https://z.ai → sign up → https://z.ai/manage-apikey/apikey-list → Create API key
```

Phone number required. No credit card.

### 2. Set the API key

```bash
echo "GLM_API_KEY=key-from-above" >> ~/.hermes/.env
```

### 3. Verify

```bash
hermes chat --provider zai --model glm-4.7 --message "what model are you?"
```

### 4. Set as default

```yaml
# ~/.hermes/config.yaml
model:
  default: glm-4.7
  provider: zai
```

## What About GLM-5.2?

Z.AI sells GLM-5.2 at $1.40/M input / $4.40/M output. Also accessible through Hermes:

```bash
hermes chat --provider zai --model glm-5.2
```

Not free, but works through the same provider with zero config change.

## Why This Is a Hack

Most API providers put free tiers behind separate model IDs (e.g. `gpt-4o-mini` vs `gpt-4o`). Z.AI does not. The same model ID `glm-4.7` returns different quality depending on your account's billing tier. Free accounts get Flash. Paid accounts get full.

This means:
- The model picker in Hermes only shows `glm-4.7` — there is no `glm-4.7-flash` entry
- You cannot accidentally select the paid variant (it is the same name)
- If you ever add credits to your Z.AI account, `glm-4.7` silently upgrades to full GLM-4.7 without any config change

## Summary

| Goal | Feasible | Cost | Model ID |
|------|----------|------|----------|
| GLM-4.7 (Flash) via free API | **Yes** | **$0** | `glm-4.7` |
| GLM-5.2 via paid API | Yes | $1.40-$4.40/M | `glm-5.2` |
| GLM-5.2 locally | No | — | — |

## What About Local Inference?

If you want fully local zero-cost inference on this laptop, use Ollama with a model that fits 15 GB RAM:

```bash
curl -fsSL https://ollama.com/install.sh | sh
ollama pull qwen2.5:7b
```

Then point Hermes:

```yaml
# ~/.hermes/config.yaml
model:
  default: qwen2.5:7b
  provider: openai-api
  base_url: http://127.0.0.1:11434/v1
```

~20 tok/s on CPU. Not GLM, not 5.2T — but private and offline.

## D2 Diagram Compilation

```bash
d2 --theme 0 --dark-theme 0 -l dagre diagram.d2 output.svg
```
