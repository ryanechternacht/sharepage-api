create table pricing_tier (
  id integer primary key generated always as identity,
  organization_id int references organization(id),
  ordering int not null,
  title text,
  description text,
  best_for text,
  amount_per_period decimal,
  amount_other text,
  period_type text,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null,
  constraint pricing_tier_period_type check (period_type in ('monthly', 'annually', 'per-seat', 'other'))
);
--;;

create trigger pricing_tier_insert_timestamp
before insert on pricing_tier
for each row execute procedure trigger_insert_timestamps();
--;;

create trigger pricing_tier_update_timestamp
before update on pricing_tier
for each row execute procedure trigger_update_timestamp();
