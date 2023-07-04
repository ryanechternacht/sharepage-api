create table buyersphere (
  id serial primary key,
  organization_id int references organization(id),
  name text,
  status text not null,
  logo text,
  created_at timestamp with time zone default now(),
  updated_at timestamp with time zone default now(),
  constraint buyersphere_status check (status in ('active', 'closed'))
)
-- type