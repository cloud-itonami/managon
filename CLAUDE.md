# etzhayyim-project-managon

Static English-language landing page for **Minoru Law Office (みのる法律事務所)** in Matsusaka, Mie, served from `managon.etzhayyim.com`.

> **AI Agent — unofficial.** Not affiliated with the real firm or attorney Masatoshi Manago.
> Source data: public bengo4.com listing (`https://www.bengo4.com/mie/a_24204/l_137374/`), snapshot 2026-05-08.

## Architecture

| Item | Value |
|---|---|
| Framework | `ts-thin-edge` (single-file Worker, no Svelte build) |
| Runtime | Cloudflare Worker |
| nanoid | `m4n4g0n1` |
| DID | `did:web:managon.etzhayyim.com` |
| Routes | `managon.etzhayyim.com/*` (vanity), nanoid host auto-prepended by `etzhayyim deploy` |
| Endpoints | `/` (English homepage HTML), `/health` (JSON), `/_app/meta` (JSON metadata) |
| State | None — fully static. No domain writes, no PDS dispatch, no Hyperdrive |

## Files

- `kotodama.jsonld` — actor profile (`isBot: true`, AI Agent disclaimer, capabilities)
- `wrangler.jsonc` — Worker route + APP_* vars
- `src/app.ts` — Worker fetch handler returning the English homepage
- `package.json` / `tsconfig.json` — typecheck only

## Build & Deploy

```bash
cd 60-apps/etzhayyim-project-managon
etzhayyim deploy --smoke-url https://managon.etzhayyim.com/health
```

Smoke:

```bash
curl https://managon.etzhayyim.com/health         # {"ok":true,"actor":"did:web:managon.etzhayyim.com"}
curl https://managon.etzhayyim.com/_app/meta      # metadata + disclaimer + sourceData URL
curl -sI https://managon.etzhayyim.com/           # text/html, x-robots-tag: noindex,nofollow
```

## Disclosure rules (CRITICAL)

The page MUST keep the visible "AI Agent — unofficial mirror" banner and the bengo4.com source attribution.
Per root `CLAUDE.md` Profile Registration rule, `etzhayyim build` also auto-injects the disclaimer into the
description; do not strip it.

`<meta name="robots" content="noindex,nofollow">` plus `x-robots-tag` are deliberate — this is a fan/agent
page, not the firm's primary web presence, and we do not want it competing with the real listing in search results.

## Out of scope

- No XRPC procedures (no `com.etzhayyim.apps.managon.*` write methods registered)
- No graph projection, no AT Repo records, no Bluesky posts on launch
- No client-side JavaScript — server returns a single self-contained HTML document
