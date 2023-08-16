create table pricing_tier (
  id integer generated always as identity,
  organization_id int references organization(id),
  ordering int not null,
  title text,
  description text,
  best_for text,
  amount_per_period decimal,
  amount_other text,
  period_type text,
  created_at timestamp with time zone default now(),
  updated_at timestamp with time zone default now(),
  constraint pricing_tier_period_type check (period_type in ('monthly', 'annually', 'per seat', 'other'))
)
