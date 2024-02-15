create table buyersphere_milestone (
  id integer primary key generated always as identity,
  organization_id int references organization(id) not null,
  buyersphere_id int references buyersphere(id) not null,
  title text,
  ordering int,
  resolved boolean default false,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);
--;;
create trigger buyersphere_milestone_insert_timestamp
before insert on buyersphere_milestone
for each row execute procedure trigger_insert_timestamps();
--;;
create trigger buyersphere_milestone_update_timestamp
before update on buyersphere_milestone
for each row execute procedure trigger_update_timestamp();
--;;


create table buyersphere_activity (
  id integer primary key generated always as identity,
  organization_id int references organization(id) not null,
  buyersphere_id int references buyersphere(id) not null,
  milestone_id int references buyersphere_milestone(id) not null,
  creator_id int references user_account(id) not null,
  activity_type text not null,
  title text,
  assigned_to_id int references user_account(id),
  assigned_team text not null,
  due_date date,
  resolved boolean default false,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null,
  constraint buyersphere_assigned_team check (assigned_team in ('buyer', 'seller')),
  constraint buyersphere_activity_type check (activity_type in ('action', 'question', 'comment', 'meeting'))
);
--;;
create trigger buyersphere_activity_insert_timestamp
before insert on buyersphere_activity
for each row execute procedure trigger_insert_timestamps();
--;;
create trigger buyersphere_activity_update_timestamp
before update on buyersphere_activity
for each row execute procedure trigger_update_timestamp();
