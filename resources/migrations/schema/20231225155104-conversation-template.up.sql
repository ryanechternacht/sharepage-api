create table conversation_template (
  id integer primary key generated always as identity,
  organization_id int references organization(id),
  message text,
  due_date_days int not null,
  assigned_to int references user_account(id),
  assigned_team text,
  collaboration_type text,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null,
  constraint conversation_template_assigned_team check (assigned_team in ('buyer', 'seller')),
  constraint conversation_template_collaboration_type check (collaboration_type in ('task', 'question', 'comment', 'meeting', 'milestone'))
);
--;;
create trigger conversation_template_insert_timestamp
before insert on conversation_template
for each row execute procedure trigger_insert_timestamps();
--;;
create trigger conversation_template_update_timestamp
before update on conversation_template
for each row execute procedure trigger_update_timestamp();
