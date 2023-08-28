create table user_account (
  id integer primary key generated always as identity,
  email text not null,
  buyersphere_role text not null,
  display_role text,
  first_name text,
  last_name text,
  organization_id int references organization(id),
  created_at timestamp with time zone default now(),
  updated_at timestamp with time zone default now(),
  unique (email, organization_id),
  constraint user_accout_buyersphere_role check (buyersphere_role in ('admin', 'buyer'))
)
