create table orbit (
  id serial primary key,
  organization_id int references organization(id),
  name text,
  status text not null,
  logo text,
  created_at timestamp with time zone default now(),
  updated_at timestamp with time zone default now(),
  constraint orbit_status check (status in ('active', 'closed'))
)
-- type