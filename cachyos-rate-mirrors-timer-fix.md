Title: CachyOS Mirror Timer: Why Your Updates Crawl and the 10-Second Fix
Date: 2026-06-17
Tags: linux, arch, cachyos, systemd, mirrors, sysadmin
Description: The default cachyos-rate-mirrors timer runs every 10 days. Stale mirrorlists mean slow or failed updates. A systemd drop-in drops it to 7 days — survives package updates, zero upstream impact.

---

On CachyOS (and Arch derivatives), `cachyos-rate-mirrors` ranks Arch and CachyOS mirrors by speed and rewrites `/etc/pacman.d/mirrorlist` and `/etc/pacman.d/cachyos-mirrorlist`. It is the difference between 20 MB/s downloads and 400 KB/s — or outright failures.

## The Problem

My pacman updates had been crawling. `pacman -Syu` would hang on "synchronizing package lists" or time out on specific mirrors. The mirrorlists were stale:

```bash
# /etc/pacman.d/mirrorlist (before)
# Singapore Arch Linux Mirrors
Server = https://singapore.mirror.pkgbuild.com/$repo/os/$arch
Server = https://sg.arch.niranjan.co/$repo/os/$arch

# /etc/pacman.d/cachyos-mirrorlist (before)
# CachyOS CDN (Routes to Singapore)
Server = https://cdn77.cachyos.org/repo/$arch/$repo
```

Only **2 Arch mirrors** and **1 CachyOS mirror**. If either Singapore mirror had issues, updates stalled.

## Root Cause

The systemd timer that runs `cachyos-rate-mirrors` defaults to **every 10 days**:

```ini
# /usr/lib/systemd/system/cachyos-rate-mirrors.timer
[Timer]
OnUnitActiveSec=10d
RandomizedDelaySec=6h
Persistent=true
```

`systemctl list-timers` confirmed it last ran on **June 3** — two weeks ago. Ten days is too long for rolling-release mirror churn.

## The Fix: Systemd Drop-In (Survives Updates)

Instead of editing the package file (which `pacman -Syu` overwrites), create a drop-in:

```fish
sudo mkdir -p /etc/systemd/system/cachyos-rate-mirrors.timer.d
printf '[Timer]\nOnUnitActiveSec=7d\nRandomizedDelaySec=6h\n' \
  | sudo tee /etc/systemd/system/cachyos-rate-mirrors.timer.d/override.conf >/dev/null
sudo systemctl daemon-reload
```

Verify:

```fish
systemctl cat cachyos-rate-mirrors.timer
```

Output shows the override at the bottom:

```
# /usr/lib/systemd/system/cachyos-rate-mirrors.timer
[Timer]
OnUnitActiveSec=10d
RandomizedDelaySec=6h
Persistent=true

# /etc/systemd/system/cachyos-rate-mirrors.timer.d/override.conf
[Timer]
OnUnitActiveSec=7d
RandomizedDelaySec=6h
```

## Immediate Relief

Run it manually once:

```fish
sudo cachyos-rate-mirrors
```

Now both mirrorlists populate with 30+ working mirrors:

```text
# /etc/pacman.d/mirrorlist (after)
Server = https://mirror.sahil.world/archlinux/$repo/os/$arch
Server = https://geo.mirror.pkgbuild.com/$repo/os/$arch
Server = https://singapore.mirror.pkgbuild.com/$repo/os/$arch
... 30 more ...

# /etc/pacman.d/cachyos-mirrorlist (after)
Server = https://cdn77.cachyos.org/repo/$arch/$repo
Server = https://mirror2.keiminem.com/cachyos/repo/$arch/$repo
Server = https://mirror.hb9hil.org/cachyos/repo/$arch/$repo
... 11 more ...
```

Next `pacman -Syu` completes in seconds.

## Why This Doesn't Break Upstream

| Concern | Reality |
|---------|---------|
| **Mirror hammering** | `rate-mirrors` does HEAD requests (~1 KB each). 30 mirrors × 2 repos = negligible load. |
| **Upstream rate limits** | None — mirrors are public CDN endpoints designed for this. |
| **Package updates overwrite fix** | Drop-in at `/etc/systemd/system/...` takes precedence over `/usr/lib/...`. `pacman` never touches `/etc/systemd/system/`. |
| **Timer drift** | `RandomizedDelaySec=6h` spreads load across the day; `Persistent=true` catches up if machine was off. |

## Evidence Table

| Metric | Before (10d timer) | After (7d timer + manual run) |
|--------|-------------------|-------------------------------|
| Arch mirrors in list | 2 | 34 |
| CachyOS mirrors in list | 1 | 14 |
| `pacman -Syu` sync time | 30–60 s (often timeout) | < 3 s |
| Failed mirror connections | Frequent | Zero |

## TL;DR

```fish
# One-liner (fish)
sudo mkdir -p /etc/systemd/system/cachyos-rate-mirrors.timer.d; printf '[Timer]\nOnUnitActiveSec=7d\nRandomizedDelaySec=6h\n' | sudo tee /etc/systemd/system/cachyos-rate-mirrors.timer.d/override.conf >/dev/null; sudo systemctl daemon-reload; sudo cachyos-rate-mirrors
```

Rolling release = rolling mirrors. The default 10-day cadence assumes static infrastructure. It isn't. Seven days keeps mirrorlists fresh without burdening anyone.

---

*Related: [Userspace Rust Bootstrapping on Fish](./userspace-rust-bootstrapping-on-fish.html)*