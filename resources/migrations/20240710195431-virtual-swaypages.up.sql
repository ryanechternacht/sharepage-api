create table virtual_swaypage (
  id integer primary key generated always as identity,
  organization_id int not null references organization(id),
  shortcode text not null,
  campaign_uuid uuid not null references campaign(uuid),
  page_data jsonb not null,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null,
  UNIQUE (organization_id, shortcode)
);
--;;
create trigger virtual_swaypage_insert_timestamp
before insert on virtual_swaypage
for each row execute procedure trigger_insert_timestamps();
--;;
create trigger virtual_swaypage_update_timestamp
before update on virtual_swaypage
for each row execute procedure trigger_update_timestamp();