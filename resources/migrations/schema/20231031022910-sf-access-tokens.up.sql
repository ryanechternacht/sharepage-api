create table salesforce_access (
  user_account_id int primary key references user_account(id),
  organization_id int references organization(id) not null,
  access_token text,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);
--;;
create trigger salesforce_access_insert_timestamp
before insert on salesforce_access
for each row execute procedure trigger_insert_timestamps();
--;;
create trigger salesforce_access_update_timestamp
before update on salesforce_access
for each row execute procedure trigger_update_timestamp();
