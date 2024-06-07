create table csv_upload (
  uuid uuid primary key,
  organization_id int not null references organization(id),
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);
--;;
create trigger csv_upload_insert_timestamp
before insert on csv_upload
for each row execute procedure trigger_insert_timestamps();
--;;
create trigger csv_upload_update_timestamp
before update on csv_upload
for each row execute procedure trigger_update_timestamp();
--;;

create table csv_upload_row (
  organization_id int not null references organization(id),
  csv_upload_uuid uuid not null references csv_upload(uuid),
  row_number int not null,
  row_data jsonb not null,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null,
  primary key (csv_upload_uuid, row_number)
);
--;;
create trigger csv_upload_row_insert_timestamp
before insert on csv_upload_row
for each row execute procedure trigger_insert_timestamps();
--;;
create trigger csv_upload_row_update_timestamp
before update on csv_upload_row
for each row execute procedure trigger_update_timestamp();
