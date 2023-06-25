create table orbit (
  id serial primary key,
  organization_id int references organization(id),
  name text,
  created_at timestamp with time zone default now(),
  updated_at timestamp with time zone default now()
)
-- type