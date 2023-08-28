create extension if not exists "uuid-ossp"
--;;
set timezone = 'Etc/UTC'
--;;
-- TODO switch to the auto timestamp versions
create table organization (
  id integer primary key generated always as identity,
  name text,
  domain text,
  subdomain text,
  logo text,
  stytch_organization_id text,
  created_at timestamp with time zone default now(),
  updated_at timestamp with time zone default now()
)
