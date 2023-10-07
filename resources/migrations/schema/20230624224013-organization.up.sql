create extension if not exists "uuid-ossp";
--;;
set timezone = 'Etc/UTC';
--;;

-- credit https://x-team.com/blog/automatic-timestamps-with-postgresql/
create or replace function trigger_insert_timestamps()
returns trigger as $$
begin 
  new.created_at = CURRENT_TIMESTAMP;
  new.updated_at = CURRENT_TIMESTAMP;
  return new;
end;
$$ LANGUAGE plpgsql;
--;;

create or replace function trigger_update_timestamp()
returns trigger as $$
begin 
  new.updated_at = CURRENT_TIMESTAMP;
  return new;
end;
$$ LANGUAGE plpgsql;
--;;

create table organization (
  id integer primary key generated always as identity,
  name text,
  domain text,
  subdomain text,
  logo text,
  stytch_organization_id text,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);
--;;

create trigger organization_insert_timestamp
before insert on organization
for each row execute procedure trigger_insert_timestamps();
--;;

create trigger organization_update_timestamp
before update on organization
for each row execute procedure trigger_update_timestamp();
