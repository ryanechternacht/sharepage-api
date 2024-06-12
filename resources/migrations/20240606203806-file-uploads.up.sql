create table csv_upload (
  uuid uuid primary key,
  organization_id int not null references organization(id),
  file_name text not null,
  header_row jsonb not null,
  data_rows jsonb not null,
  data_rows_count int not null,
  sample_rows jsonb not null,
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
