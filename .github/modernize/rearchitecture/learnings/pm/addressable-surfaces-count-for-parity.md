# Addressable Surfaces Count For Parity

If a page or endpoint is directly reachable in the shipped app, treat it as in-scope parity unless a later approved artifact explicitly deprecates it.

## What Happened
For DayTrader8 task t3, the obvious product surface was the main `/app` trading flow, but the shipped application also exposes configuration utilities, scenario-driving endpoints, primitive launcher destinations, REST/SSE/WebSocket contracts, docs-linked pages, and alternate page variants. The constitution required full parity, so excluding those addressable surfaces would have under-scoped the rewrite.

## Takeaway
When inventorying a brownfield sample or benchmark app, do not limit parity to the “happy path” workflow. Include every surface that is linked from the UI, mapped by a public servlet/resource/websocket path, or directly reachable from packaged web content.

## History
- 2026-05-27 (sample.daytrader8/t3): initial
