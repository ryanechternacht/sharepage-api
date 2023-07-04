create table user_account (
  id serial primary key,
  email text not null,
  role text not null, 
  organization_id int references organization(id),
  created_at timestamp with time zone default now(),
  updated_at timestamp with time zone default now(),
  unique (email, organization_id),
  constraint user_accout_role check (role in ('admin', 'buyer'))
)
