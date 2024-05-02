create table buyer_session_event (
  organization_id int references organization(id) not null,
  buyersphere_id int references buyersphere(id) not null,
  buyer_session_id integer references buyer_session(id) not null,
  page text not null,
  event_type text not null,
  event_data jsonb,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null,
  constraint buyer_session_event check (event_type in ('click-share', 'click-link'))
);
--;;
create trigger buyer_session_event_insert_timestamp
before insert on buyer_session_event
for each row execute procedure trigger_insert_timestamps();
--;;
create trigger buyer_session_event_update_timestamp
before update on buyer_session_event
for each row execute procedure trigger_update_timestamp();
--;;
create index if not exists idx_buyer_session_event_organization_id_buyersphere_id
on buyer_session_event(organization_id, buyersphere_id);
