# Data Model

## Persistence Unit

- Persistence unit name: `daytrader`
- Transaction type: JTA
- Datasource: `jdbc/TradeDataSource`
- Shared cache mode: `NONE`

## Entities

1. `AccountDataBean`
   Table: `accountejb`
   Relationships: one-to-many orders, one-to-many holdings, one-to-one profile
   Key generation: `KEYGENEJB` table generator
2. `AccountProfileDataBean`
   Table: `accountprofileejb`
   Relationships: one-to-one back-reference to account
3. `HoldingDataBean`
   Table: `holdingejb`
   Relationships: many-to-one account, many-to-one quote
   Key generation: `KEYGENEJB` table generator
4. `OrderDataBean`
   Table: `orderejb`
   Relationships: many-to-one account, many-to-one quote, one-to-one holding
   Key generation: `KEYGENEJB` table generator
   Named queries: order status and user-scoped order retrieval/update flows
5. `QuoteDataBean`
   Table: `quoteejb`
   Named queries and native query for quote retrieval and update-for-lock semantics

## Schema Coupling

- Schema names are embedded directly in entity mappings and in the direct JDBC SQL paths.
- Key generation depends on the existing `KEYGENEJB` table contract.
- Buy/sell correctness depends on transactional consistency across account, order, holding, and
  quote updates.
- The constitution requirement to preserve the existing schema is feasible, but only if the Spring
  target keeps both JPA mappings and direct-SQL expectations aligned to the same table/column model.

## Migration Implications

- The entity set is small and stable, which is favorable.
- The data-access strategy is not uniform: JPA and manual JDBC coexist, so downstream work must
  either consolidate persistence behind one approach or deliberately preserve dual access paths
  without drifting from the shared schema.