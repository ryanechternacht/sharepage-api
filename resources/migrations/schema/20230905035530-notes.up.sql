create table buyersphere_note (
  id integer primary key generated always as identity,
  organization_id int references organization(id),
  buyersphere_id int references buyersphere(id),
  title text,
  body text,
  author int references user_account(id),
  created_at timestamp with time zone default now(),
  updated_at timestamp with time zone default now()
)
