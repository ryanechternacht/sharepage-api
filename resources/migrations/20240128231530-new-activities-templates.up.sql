create table buyersphere_milestone_template (
  id integer primary key generated always as identity,
  organization_id int references organization(id) not null,
  title text,
  ordering int,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);
--;;
create trigger buyersphere_milestone_template_insert_timestamp
before insert on buyersphere_milestone_template
for each row execute procedure trigger_insert_timestamps();
--;;
create trigger buyersphere_milestone_template_update_timestamp
before update on buyersphere_milestone_template
for each row execute procedure trigger_update_timestamp();
--;;


create table buyersphere_activity_template (
  id integer primary key generated always as identity,
  organization_id int references organization(id) not null,
  milestone_template_id int references buyersphere_milestone_template(id) not null,
  activity_type text not null,
  title text,
  assigned_team text not null,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null,
  constraint buyersphere_assigned_team check (assigned_team in ('buyer', 'seller')),
  constraint buyersphere_activity_type check (activity_type in ('action', 'question', 'comment', 'meeting'))
);
--;;
create trigger buyersphere_activity_template_insert_timestamp
before insert on buyersphere_activity_template
for each row execute procedure trigger_insert_timestamps();
--;;
create trigger buyersphere_activity_template_update_timestamp
before update on buyersphere_activity_template
for each row execute procedure trigger_update_timestamp();
