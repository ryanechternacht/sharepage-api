create table buyer_session (
  id integer primary key generated always as identity,
  organization_id int references organization(id) not null,
  buyersphere_id int references buyersphere(id) not null,
  user_account_id int,
  linked_name text,
  anonymous_id text,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);
--;;
create trigger buyer_session_insert_timestamp
before insert on buyer_session
for each row execute procedure trigger_insert_timestamps();
--;;
create trigger buyer_session_update_timestamp
before update on buyer_session
for each row execute procedure trigger_update_timestamp();
--;;

create table buyer_session_timing (
  organization_id int references organization(id) not null,
  buyersphere_id int references buyersphere(id) not null,
  buyer_session_id integer references buyer_session(id) not null,
  page text not null,
  time_on_page smallint not null default 0,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null,
  primary key (buyer_session_id, page)
);
--;;
create trigger buyer_session_timing_insert_timestamp
before insert on buyer_session_timing
for each row execute procedure trigger_insert_timestamps();
--;;
create trigger buyer_session_timing_update_timestamp
before update on buyer_session_timing
for each row execute procedure trigger_update_timestamp();
