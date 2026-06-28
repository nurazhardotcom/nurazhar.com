Title: How Submarine Cables Made Tiny Singapore a Global Internet Hub
Date: 2026-06-28
Tags: submarine-cables, singapore, networking, infrastructure, geopolitics, d2
Description: Why a city-state of 5.6 million became one of the most connected places on earth.

---

There are 28 submarine cables landing in Singapore, with another 13 on the way. That's more than most entire continents. How did a city-state smaller than most metropolitan areas become a global internet node?

## It Started in 1870

Singapore's role as a communications hub predates the internet by over a century. The first submarine telegraph cable landed here in 1870, connecting London to Australia via India and Singapore. The British Empire needed a relay point between the Indian Ocean and the South China Sea, and Singapore sat right at the chokepoint.

```d2
# Diagram 1: Singapore at the Geographic Chokepoint
direction: down

Indian: "Indian Ocean"
Singapore: "Singapore"
SCS: "South China Sea"
East: "East Asia (China, Japan, Korea)"
West: "Middle East, Europe, Africa"
South: "Australia, New Zealand"

Indian -> Singapore: "Cables from Europe & India"
Singapore -> SCS: "Cables to East Asia"
Singapore -> South: "Cables to Australia"
West -> Indian: ""
East -> SCS: ""
```

This hasn't changed in 150 years. The technology evolved from telegraph to fibre optic, but the geography stayed the same.

## Why Singapore and Not Jakarta or Kuala Lumpur?

Three reasons:

### 1. Political Stability

Submarine cables are critical infrastructure. A single cut can disrupt a country's entire internet. Cable owners want to land them in jurisdictions that are:
- Politically stable (no coups, no wars)
- Legally predictable (clear property rights)
- Geologically safe (no earthquakes, no tsunamis)

Singapore scores perfectly on all three. Malaysia and Indonesia have more seismic activity and more political uncertainty.

```d2
# Diagram 2: Cable Landing Criteria
direction: down

Cable: "Cable Owners\n(Google, Meta, Telcos)"
Stability: "Political Stability"
Legal: "Legal Framework"
Geo: "Geological Safety"
Biz: "Business Environment"

Cable -> Stability: "Must have"
Stability -> Legal: ""
Legal -> Geo: ""
Geo -> Biz: ""
Biz -> Cable: "Singapore ticks all boxes"
```

### 2. Neutral Territory

Singapore is politically neutral. Malaysia's TM and Indonesia's Telkom are both state-owned incumbents with national interests. Would you want your cable to land in a country where the government controls the ISP? Singapore's openness and neutrality make it a safe choice for everyone.

### 3. Government Strategy

Starting in the 1990s and accelerating with the Digital Connectivity Blueprint, Singapore actively courted cable landings. The Infocomm Media Development Authority (IMDA):
- Streamlined landing permits
- Invested in data centre infrastructure
- Created the Singapore Internet Exchange (SGIX)
- Offered tax incentives for content providers to host locally

This strategy worked. Google, Netflix, Amazon, and Microsoft all have data centres in Singapore, which means content is served locally instead of traversing undersea cables.

```d2
# Diagram 3: Content Delivery in Singapore
direction: down

User: "You in Singapore"
GoogleDC: "Google Data Centre\nSingapore"
NetflixDC: "Netflix Open Connect\nSingapore"
AWS: "AWS Region\nSingapore"

User -> GoogleDC: "YouTube: stays in Singapore\n~2ms latency"
User -> NetflixDC: "Netflix: cached locally\n~3ms latency"
User -> AWS: "AWS services: local region\n~2ms latency"
```

Without local caches, your Netflix request would travel to Hong Kong or Japan and back. With local servers, it stays entirely within Singapore.

## The Result

```d2
# Diagram 4: Singapore Cable Hub
direction: down

SG: "Singapore\n28 cables (13 more planned)"
Asia: "Asia\nJapan, China, India"
AUS: "Australia\nSydney, Perth"
ME: "Middle East\nvia India Ocean"
US: "United States\nvia Guam & Hawaii"
EU: "Europe\nvia Middle East"

SG -> Asia: "SeaMeWe-5, APCN-2, etc."
SG -> AUS: "Australia-Singapore Cable (ASC)"
SG -> ME: "SEA-ME-WE series"
SG -> US: "SEA-US, SJC2"
SG -> EU: "via multiple paths"
```

Singtel is the biggest beneficiary of this. They own or co-own many of these cables, sell transit to half of Southeast Asia, and have amassed 100,000 IP prefixes — 10x more than any neighbour. The tiny island is the region's internet landlord.

## Why This Matters for Your Connection

When you load a website as a Singtel customer:
1. Your request travels over fibre to the exchange (~1ms)
2. If the content is cached in Singapore (Google, Netflix), it's served locally
3. If not, it goes via one of 28 submarine cables to the destination
4. The path is short, well-provisioned, and has multiple redundancies

That's why your ping to Google DNS is 4ms. You're living on top of one of the best-connected pieces of land on earth.

*Related: [SGIX and Internet Exchanges — Where Networks Meet](./sgix-and-internet-exchanges.html)*
