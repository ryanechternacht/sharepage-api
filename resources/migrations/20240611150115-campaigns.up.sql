create table campaign (
  uuid uuid primary key,
  organization_id int not null references organization(id),
  title text not null,
  csv_upload_uuid uuid references csv_upload(uuid),
  swaypage_template_id int references buyersphere(id),
  columns_approved boolean default false,
  ai_prompts_approved boolean default false,
  is_published boolean default false,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);
--;;
create trigger campaign_insert_timestamp
before insert on campaign
for each row execute procedure trigger_insert_timestamps();
--;;
create trigger campaign_update_timestamp
before update on campaign
for each row execute procedure trigger_update_timestamp();
