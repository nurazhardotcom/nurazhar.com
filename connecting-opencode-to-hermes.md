title: "OpenCode + Hermes Agent: Architecture & Connection"

```d2
direction: RIGHT

# Nodes
OpenCode: "OpenCode\n(UI: Terminal/Desktop)" as OpenCode
HermesAPI: "Hermes Agent\nAPI Server (9119)" as HermesAPI
HermesCore: "Hermes Core\n(routing + memory)" as HermesCore
Models: "Any OpenAI-compatible\n(Cohere, GPT, etc.)" as Models
Tools: "Hermes Tools\n(registry + exec)" as Tools

# Connections
OpenCode -> HermesAPI: "HTTP/HTTPS\n(OpenAI API)"
HermesAPI -> HermesCore: "Internal RPC"
HermesCore -> Models: "Provider routing"
HermesCore -> Tools: "Tool registry"
HermesCore -> OpenCode: "Responses + memory"

# Styling
OpenCode: fill:#f0f8ff,stroke:#4682b4,stroke-width:2px
HermesAPI: fill:#e6ffe6,stroke:#228b22,stroke-width:2px
HermesCore: fill:#fff0e5,stroke:#ff8c00,stroke-width:2px
Models: fill:#f5f5dc,stroke:#696969,stroke-width:2px
Tools: fill:#fafad2,stroke:#8b4513,stroke-width:2px

# Labels
OpenCode.in: "Your interface\nto AI"
HermesAPI.in: "Entry point\n(OpenAI compatible)"
HermesCore.in: "The brain\n(routing + memory)"
Models.in: "Actual LLMs\nany provider"
Tools.in: "Actionable tools\nregistry + exec"

# Legend
legend: "OpenCode = UI\nHermesAPI = HTTP server\nHermesCore = Core logic\nModels = LLMs\nTools = Tool execution"
```

# Architecture Overview

OpenCode sits between you and Hermes Agent. You talk to OpenCode (terminal or desktop). OpenCode makes OpenAI-compatible requests to Hermes Agent's HTTP server. Hermes Agent routes those requests to the actual model (Cohere, GPT, etc.) and manages conversation memory. It can also execute tools. Everything stays local for privacy.

## Detailed Network Flow

```d2
direction: DOWN

# HTTP Request Flow
Client: "OpenCode\n(terminal/desktop)" as Client
HTTP: "POST /v1/chat/completions\nOpenAI API format" as HTTP
Router: "Hermes Router\n(provider selection)" as Router
LLM: "Cohere Command\n(cohere/command-a-plus-05-2026)" as LLM
Response: "JSON response\n(back to OpenCode)" as Response

Client -> HTTP: "User message + context"
HTTP -> Router: "Parse + route"
Router -> LLM: "Forward to chosen model"
LLM -> Response: "Generate + format"
Response -> HTTP: "Add memory context"
HTTP -> Client: "Return final response"
```

## OpenCode Configuration

Edit `~/.config/opencode/opencode.json` (create if missing):

```json
{
  "api": {
    "endpoint": "http://localhost:9119/v1",
    "model": "cohere/command-a-plus-05-2026",
    "api_key": "your-openai-key-or-empty",
    "temperature": 0.7,
    "max_tokens": 4096
  },
  "memory": {
    "persist": true,
    "storage": "~/.local/share/opencode/memory"
  }
}
```

## Hermes Agent Configuration

Hermes uses `config.yaml` in `~/.hermes/hermes-agent/`:

```yaml
api:
  enabled: true
  port: 9119
  cors_origins: ["*"]

providers:
  - name: cohere
    type: "cohere"
    model: "command-a-plus-05-2026"
    api_key: "${COHERE_API_KEY}"

memory:
  backend: "sqlite"
  path: "~/.local/share/hermes/memory.db"

tools:
  registry_path: "~/.local/share/hermes/tools"
```

## Security & Privacy

| Aspect | Implementation | Result |
|----|----------------|--------|
|----|----------------|--------|
| **Network** | Localhost only (127.0.0.1) | No external exposure |
| **Data** | No cloud transmission | Your data stays local |
| **Storage** | Encrypted SQLite | Memory persisted securely |
| **Access** | File system permissions | Only you can read/write |

## Performance Characteristics

- **Latency**: 50-200ms for local inference
- **Throughput**: Single model ~2-3 RPS
- **Memory**: ~500MB per active session
- **CPU**: 4 cores recommended for Cohere models

## Troubleshooting

| Symptom | Check | Fix |
|----|-------|-----|
|----|-------|-----|
| Can't connect | `curl http://localhost:9119/v1/models` | Start `hermes serve --no-gui` in background |
| Wrong model | OpenCode shows different model | Verify Hermes config `providers[0].model` matches |
| No memory | Conversations reset | Check `memory.persist: true` in both configs |
| Tools not working | Tool calls fail | Ensure `tools.registry_path` exists and has YAML files |

## Advanced Usage

### Multiple Sessions

```bash
# Session A: coding
opencode --session coding --model cohere/command-a-plus-05-2026

# Session B: writing
opencode --session writing --model openai/gpt-4
```

### Tool Execution

Hermes tools are YAML files in `~/.local/share/hermes/tools/`:

```yaml
# tools/git.yaml
name: git
description: Git operations
parameters:
  commit_message: string
  repository: string
```

OpenCode will auto-complete tool usage based on registry.

> TL;DR: OpenCode is the UI, Hermes Agent is the brain. Run `--no-gui` to expose the API port. Configure both via JSON/YAML for full control.

*Published from OpenCode terminal using Hermes Agent for model inference*