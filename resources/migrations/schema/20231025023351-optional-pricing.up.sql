create table pricing_global_settings (
  organization_id int primary key references organization(id),
  show_by_default boolean default true,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
)
--;;
create trigger pricing_global_settings_insert_timestamp
before insert on pricing_global_settings
for each row execute procedure trigger_insert_timestamps();
--;;
create trigger pricing_global_settings_update_timestamp
before update on pricing_global_settings
for each row execute procedure trigger_update_timestamp();
--;;

insert into pricing_global_settings (organization_id, show_by_default)
select id, true from organization;
