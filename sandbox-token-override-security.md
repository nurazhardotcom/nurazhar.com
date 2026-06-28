Title: Sandbox vs. Keyring: Understanding Credential Precedence in Developer Security
Date: 2026-06-15
Tags: security, devops, linux, architecture
Description: How scoped environment variables override local keyrings in sandboxed environments, why this design protects developer secrets, and how to navigate credentials securely.

---

When building automated workflows, running developer agents, or operating within containers, you eventually run into a weird git authentication error. Your local machine is fully authenticated, your GitHub CLI (`gh`) is logged in, but the terminal inside your workspace flatly refuses to push:

```text
remote: Invalid username or token. Password authentication is not supported.
fatal: Authentication failed.
```

The culprit? A silent security design pattern: **Environment Variable Precedence**.

Here is why secure sandboxes inject dummy or low-privilege tokens, how they override your system’s credentials, and why understanding this is a major security asset.

---

## The Credential Hierarchy

When you run a command like `git push` or `gh repo sync`, the utility determines how to authenticate by stepping through a strict hierarchy:

1. **Environment Variables:** Checked first (e.g., `GITHUB_TOKEN`, `GH_TOKEN`).
2. **Local Configuration:** Repo-level configuration files (e.g., `.git/config`).
3. **Global Configuration:** User-level configuration (e.g., `~/.gitconfig`).
4. **Credential Helpers / OS Keyring:** Calls to macOS Keychain, Windows Credential Manager, or Linux `secret-tool`/`pass`.

Because environment variables sit at the **very top** of this food chain, any variable defined in your current shell session will immediately override whatever secure keys you have saved in your operating system's keyring.

---

## Why Sandboxes Hijack Your Session (By Design)

In modern development environments (including VS Code devcontainers, GitHub Codespaces, or agentic IDE sandboxes), the runtime environment will purposefully inject a scoped `GITHUB_TOKEN` environment variable. 

This is not a bug; it is an active security boundary designed to enforce **Least Privilege**:

### 1. Hardening the Blast Radius
If you run third-party scripts, test npm packages, or execute untrusted code in a workspace, those scripts run with the privileges of your terminal. If the terminal could read your master OS keyring, a compromised dependency could steal your global GitHub credentials. By forcing the process to use a scoped, temporary `GITHUB_TOKEN` set in the environment, the sandbox limits the damage a rogue script can do.

### 2. Preventing "Leakage" to Remote Sessions
When using remote shells or agents, injecting a temporary environment token prevents your private, long-lived SSH keys or master tokens from ever being sent over the network or exposed to the executing agent. 

---

## How to Safe-Bypass the Override

When you are explicitly interacting with a command and need to fallback to your secure keyring, you don't need to delete your global environment configurations. You can temporarily unset the overriding variable just for the scope of that single command execution:

```bash
# Temporarily drop the environment token so git looks at the OS keyring
env -u GITHUB_TOKEN git push origin main
```

Using `env -u` tells the system to execute the subsequent command in an environment that is clean of the specified variable, restoring the standard lookup chain down to the credential helper.

---

*Related: [Collapsing Auth Entropy to Zero](./collapsing-auth-entropy-to-zero.html)*
