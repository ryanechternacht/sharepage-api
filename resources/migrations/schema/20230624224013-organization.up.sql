create extension if not exists "uuid-ossp"
--;;
set timezone = 'Etc/UTC'
--;;
-- TODO switch to the auto timestamp versions
create table organization (
  id serial primary key,
  name text,
  domain text,
  subdomain text,
  created_at timestamp with time zone default now(),
  updated_at timestamp with time zone default now()
)
