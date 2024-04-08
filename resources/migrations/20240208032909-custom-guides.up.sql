create table buyersphere_page (
  id integer primary key generated always as identity,
  organization_id int references organization(id) not null,
  buyersphere_id int references buyersphere(id) not null,
  title text not null,
  body jsonb default '{"sections":[]}'::jsonb,
  is_public boolean default false,
  ordering int not null,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);
--;;
create trigger buyersphere_page_insert_timestamp
before insert on buyersphere_page
for each row execute procedure trigger_insert_timestamps();
--;;
create trigger buyersphere_page_update_timestamp
before update on buyersphere_page
for each row execute procedure trigger_update_timestamp();
