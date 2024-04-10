create table persona (
  id integer primary key generated always as identity,
  organization_id int references organization(id),
  ordering int not null,
  title text,
  description text,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);
--;;
create trigger persona_insert_timestamp
before insert on persona
for each row execute procedure trigger_insert_timestamps();
--;;
create trigger persona_update_timestamp
before update on persona
for each row execute procedure trigger_update_timestamp();
--;;

create table pain_point (
  id integer primary key generated always as identity,
  organization_id int references organization(id),
  ordering int not null,
  title text,
  description text,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);
--;;
create trigger pain_point_insert_timestamp
before insert on pain_point
for each row execute procedure trigger_insert_timestamps();
--;;
create trigger pain_point_update_timestamp
before update on pain_point
for each row execute procedure trigger_update_timestamp();
--;;

create table feature (
  id integer primary key generated always as identity,
  organization_id int references organization(id),
  ordering int not null,
  title text,
  description text,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);
--;;
create trigger feature_insert_timestamp
before insert on feature
for each row execute procedure trigger_insert_timestamps();
--;;
create trigger feature_update_timestamp
before update on feature
for each row execute procedure trigger_update_timestamp();
--;;

create table deal_timing (
  organization_id int primary key references organization(id),
  qualified_days int,
  evaluation_days int,
  decision_days int,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);
--;;
create trigger deal_timing_insert_timestamp
before insert on deal_timing
for each row execute procedure trigger_insert_timestamps();
--;;
create trigger deal_timing_update_timestamp
before update on deal_timing
for each row execute procedure trigger_update_timestamp();
--;;

create table deal_resource (
  id integer primary key generated always as identity,
  organization_id int references organization(id),
  title text,
  link text,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);
--;;
create trigger deal_resource_insert_timestamp
before insert on deal_resource
for each row execute procedure trigger_insert_timestamps();
--;;
create trigger deal_resource_update_timestamp
before update on deal_resource
for each row execute procedure trigger_update_timestamp();
--;;
