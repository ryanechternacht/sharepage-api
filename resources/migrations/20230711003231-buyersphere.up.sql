create table buyersphere (
  id integer primary key generated always as identity,
  organization_id int references organization(id),
  buyer text,
  buyer_logo text,
  intro_message text,
  intro_video_link text,
  features_answer jsonb default '{"interests":{}}'::jsonb,
  pricing_tier_id int references pricing_tier(id),
  pricing_can_pay text,
  current_stage text not null default 'qualification',
  status text not null default 'active',
  qualification_date date,
  evaluation_date date,
  decision_date date,
  adoption_date date,
  qualified_on timestamp with time zone,
  evaluated_on timestamp with time zone,
  decided_on timestamp with time zone,
  adopted_on timestamp with time zone,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null,
  constraint buyersphere_current_stage check (current_stage in ('qualification', 'evaluation', 'decision', 'adoption', 'closed')),
  constraint buyersphere_status check (status in ('active', 'on-hold', 'opt-out')),
  constraint buyersphere_pricing_can_pay check (pricing_can_pay in ('yes', 'maybe', 'no'))
);
--;;
create trigger buyersphere_insert_timestamp
before insert on buyersphere
for each row execute procedure trigger_insert_timestamps();
--;;
create trigger buyersphere_update_timestamp
before update on buyersphere
for each row execute procedure trigger_update_timestamp();
--;;

create table buyersphere_resource (
  id integer primary key generated always as identity,
  organization_id int references organization(id),
  buyersphere_id int references buyersphere(id),
  title text,
  link text,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);
--;;
create trigger buyersphere_resource_insert_timestamp
before insert on buyersphere_resource
for each row execute procedure trigger_insert_timestamps();
--;;
create trigger buyersphere_resource_update_timestamp
before update on buyersphere_resource
for each row execute procedure trigger_update_timestamp();
--;;

create table buyersphere_conversation (
  id integer primary key generated always as identity,
  organization_id int references organization(id),
  buyersphere_id int references buyersphere(id),
  author int references user_account(id),
  message text,
  resolved boolean default false,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);
--;;
create trigger buyersphere_conversation_insert_timestamp
before insert on buyersphere_conversation
for each row execute procedure trigger_insert_timestamps();
--;;
create trigger buyersphere_conversation_update_timestamp
before update on buyersphere_conversation
for each row execute procedure trigger_update_timestamp();
--;;

create table buyersphere_user_account (
  id integer primary key generated always as identity,
  organization_id int references organization(id),
  buyersphere_id int references buyersphere(id),
  user_account_id int references user_account(id),
  team text not null,
  ordering int not null,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);
--;;
create trigger buyersphere_user_account_insert_timestamp
before insert on buyersphere_user_account
for each row execute procedure trigger_insert_timestamps();
--;;
create trigger buyersphere_user_account_update_timestamp
before update on buyersphere_user_account
for each row execute procedure trigger_update_timestamp();
