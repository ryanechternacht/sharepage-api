create table user_account (
  id integer primary key generated always as identity,
  email text not null,
  buyersphere_role text not null,
  display_role text,
  first_name text,
  last_name text,
  organization_id int references organization(id),
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null,
  unique (email, organization_id),
  constraint user_accout_buyersphere_role check (buyersphere_role in ('admin', 'buyer'))
);
--;;

create trigger user_account_insert_timestamp
before insert on user_account
for each row execute procedure trigger_insert_timestamps();
--;;

create trigger user_account_update_timestamp
before update on user_account
for each row execute procedure trigger_update_timestamp();
