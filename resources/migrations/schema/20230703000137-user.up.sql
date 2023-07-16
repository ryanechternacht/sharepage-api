create table user_account (
  id serial primary key,
  email text not null,
  buyersphere_role text not null,
  display_role text,
  name text,
  organization_id int references organization(id),
  created_at timestamp with time zone default now(),
  updated_at timestamp with time zone default now(),
  unique (email, organization_id),
  constraint user_accout_buyersphere_role check (buyersphere_role in ('admin', 'buyer'))
)
