create table buyersphere_link (
  id integer primary key generated always as identity,
  organization_id int references organization(id) not null,
  buyersphere_id int references buyersphere(id) not null,
  title text not null,
  link_url text not null,
  ordering int not null,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);
--;;
create trigger buyersphere_link_insert_timestamp
before insert on buyersphere_link
for each row execute procedure trigger_insert_timestamps();
--;;
create trigger buyersphere_link_update_timestamp
before update on buyersphere_link
for each row execute procedure trigger_update_timestamp();
