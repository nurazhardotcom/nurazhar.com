Title: Least Privilege Devops: Userspace Rust Toolchain Bootstrapping on Fish Shell
Date: 2026-06-14
Tags: security, rust, linux, systems, fish
Description: Why installing developer toolchains like Rust inside userspace ($HOME) is structurally superior to system-wide installations, and how to bootstrap it cleanly using the Fish shell path architecture.

---

In systems administration and software security engineering, one of the most common anti-patterns is installing development runtimes globally via system package managers (e.g., `sudo pacman -S` or `sudo apt-get install`).

While convenient, this practice introduces unnecessary structural entropy and violates the **Principle of Least Privilege (PoLP)**. Here is why userspace installations are superior, followed by a clean bootstrapping script for modern environments running the **Fish shell**.

---

### The Security & Operations Case for Userspace

1. **Privilege Isolation**: Compilers, linters, package managers, and binary downloaders do not require root permissions. Running them system-wide creates unnecessary attack surface. A vulnerability in a cargo build script (`build.rs`) or a malicious dependency should never have write access to `/usr/bin` or `/etc`.
2. **Deterministic Version Management**: Modern development moves faster than operating system release cycles. Systems that manage toolchains locally in `$HOME/.cargo` or `$HOME/.rustup` allow developers to pin toolchains per project without risking conflicts with OS-level binaries.
3. **No Configuration Drift**: System updates should not break your development environments. Keeping the compiler toolchain within the user context ensures clean isolation from core OS updates.

---

### Bootstrapping Rust in Userspace (via Fish Shell)

Most legacy bootstrap instructions assume Bash or Zsh, resulting in messy exports appended to `.bashrc` or `.zshrc`. 

On modern setups running the **Fish shell**, we can use Fish's native universal path variables to cleanly expose the userspace binaries without modifying global configuration files.

#### Step 1: Userspace Installation
Run the official installer. The `--no-modify-path` flag is critical—it prevents the installer from trying to append legacy bash syntax to your dotfiles.

```fish
curl --proto '=https' --sv1.2 -sSf https://sh.rustup.rs | sh -s -- --no-modify-path
```

#### Step 2: Configure the User Path in Fish
Instead of manually editing config files, Fish provides the `fish_add_path` helper. This modifies the `fish_user_paths` universal variable securely and permanently.

```fish
fish_add_path $HOME/.cargo/bin
```

This updates your user path configuration immediately across all active and future Fish sessions.

---

### Verification
Confirm that the compiler is running from userspace:

```fish
which rustc
# Output: /home/username/.cargo/bin/rustc
```

By confining the compiler, compiler tooling, and cargo crates to userspace, we achieve zero system footprint, high version control flexibility, and a much cleaner security posture.

---

*Related: [CachyOS Mirror Timer Fix](./cachyos-rate-mirrors-timer-fix.html)*
