Title: Why We Built Our Own Hermes Desktop — The Electron GUI Is Buggy, So We Wrote 500 Lines of Clojure/JavaFX
Date: 2026-06-30
Tags: hermes, clojure, cljfx, javafx, desktop-gui, electron, bug-report
Description: The official Hermes Desktop (Electron/React) has persistent bugs that never get fixed because the dev team only uses the CLI. We built a stable replacement in 500 lines of Clojure/JavaFX. Here is why and how.

---

# Why We Built Our Own Hermes Desktop — The Electron GUI Is Buggy, So We Wrote 500 Lines of Clojure/JavaFX

## The Setup

We use [Hermes Agent](https://hermes-agent.nousresearch.com/) daily. It is the best local-first AI agent we have used — tool execution, session management, provider routing, all solid.

But only on the CLI.

The Desktop GUI (Electron/React) is a different story.

## The Bug Log

Over three weeks of using Hermes Desktop, we hit:

| Bug | Frequency | Status |
|-----|-----------|--------|
| Model picker shows wrong provider | Every launch | Open |
| Provider identity lost on restart | Every session | Open |
| Config changes not picked up by GUI | Intermittent | Open |
| Session restore broken | Frequent | Open |
| Gateway mode crashes Desktop | Rare | Open |

Every single one of these works perfectly on the CLI.

## The Root Cause

The Hermes team dogfoods the CLI. That is where their attention goes. The Desktop GUI is a separate Electron wrapper with its own model picker, session restore, and gateway logic — all of which have known bugs that linger because the team ships on the CLI.

The GitHub issue tracker makes it obvious: Desktop-specific bugs get filed by users and fixed weeks later. CLI-breaking bugs get fixed in hours.

This is not malice. It is the natural result of a small, fast-moving team optimizing for what they actually use.

## The Diagnosis

The Desktop GUI has two problems:

1. **Too much logic** — The Electron app duplicates provider routing, model selection, and session management instead of delegating to the CLI
2. **Not dogfooded** — If the devs don't open it, they do not feel the pain

The fix: a thin GUI that does nothing but render chat and delegate everything else to the CLI.

## Why Clojure/JavaFX

We already proved this pattern works. Our [headhunter-agent](https://nurazhar.com/native-clojure-desktop-gui-pivot) started as a web app, broke constantly, and got nuked for a Clojure/JavaFX rewrite in under ten minutes. That app has been stable ever since.

Clojure + cljfx gives us:

- **No browser sandbox** — Native window, native threads, no memory pressure from a hidden Chromium instance
- **~500 LOC** — A chat UI is not complex. cljfx (React-like declarative API over JavaFX) keeps it minimal
- **Stable ABI** — JavaFX has not broken the public API in years. We are not chasing framework updates
- **Babashka compatible** — Can run as a script if we want

## The Architecture

```d2
direction: right

USER: "User" {
  shape: person
}

JFX: "Our JavaFX GUI\n(500 lines of cljfx)" {
  CHAT: "Chat View\nmessage bubbles + input"
  PICKER: "Model Picker\nfrom GET /v1/models"
  BRIDGE: "HTTP Bridge\nto localhost:9119"
  CHAT -> BRIDGE: "user input"
  PICKER -> BRIDGE: "model selection"
}

HERMES: "Hermes CLI (hermes serve)\nlocalhost:9119/v1" {
  ROUTER: "Provider Router"
  TOOLS: "Tool Executor\nsearch / files / code"
  STATE: "Session Store\n~/.hermes/"
}

PROVIDERS: "Remote Providers" {
  NOUS: "Nous Portal\n(free OAuth)"
  ZAI: "Z.AI / GLM\n(paid + free tiers)"
  CUSTOM: "Any OpenAI-\ncompatible endpoint"
}

USER -> JFX: "types message"
BRIDGE -> HERMES: "POST /v1/chat/completions"
HERMES -> PROVIDERS: "route to provider"
HERMES -> BRIDGE: "streaming response"
BRIDGE -> CHAT: "render tokens"
```

## The Data Flow

```
User types message → JavaFX UI → HTTP POST (localhost:9119/v1/chat/completions)
                                → hermes serve routes to Nous Portal / Z.AI / etc.
                                → streaming response back
                                → UI renders token by token
```

That is it. The GUI does nothing clever. It sends requests and renders responses. Everything else — provider routing, tool execution, session persistence — stays in the battle-tested Hermes CLI.

## Why This Is 500 Lines

Because `hermes serve` already exposes a complete HTTP API. There is no model management logic in the GUI. There is no session storage logic. There is no tool execution logic.

The GUI:
- Reads model list from `GET /v1/models`
- Sends chat requests to `POST /v1/chat/completions`
- Renders streaming SSE events
- Lists saved sessions from `GET /v1/sessions`

Everything else is `hermes serve` doing its job.

## The Result

| Aspect | Official Desktop | Ours |
|--------|-----------------|------|
| Model picker | Broken (wrong provider shown) | Reads live from API |
| Config sync | Lost on restart | CLI owns config |
| Session restore | Intermittent | CLI owns sessions |
| Resource usage | 200MB+ (Electron + React) | ~60MB (JavaFX) |
| LOC | Thousands | ~500 |
| Fix turnaround | Weeks | We own the code |

## What We Lost

- Pre-built installer (we build from source)
- Tray/minimize-to-icon (not built yet)
- Dark/light theme sync (not built yet)

All of these are additive. The core functionality — chat with any Hermes provider — works on day one and will never break from an upstream Desktop GUI refactor.

## The Code

The project lives at [github.com/nurazhar/hermes-clj-gui](https://github.com/nurazhar/hermes-clj-gui). It is a `deps.edn` Clojure project with cljfx. The entire chat UI is a single namespace. The HTTP bridge is another.

If you are hitting the same Hermes Desktop bugs, consider this: the CLI works. A GUI is just a chat window talking to `hermes serve`. You do not need Electron for that.
