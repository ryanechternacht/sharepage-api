create table buyersphere (
  id serial primary key,
  organization_id int references organization(id),
  buyer text,
  buyer_logo text,
  intro_message text,
  intro_video_link text,
  features_answer jsonb default '{"interests":{}}'::jsonb,
  pricing_answer jsonb default '{"selected_level":{}}'::jsonb,
  current_stage text not null default 'qualification',
  qualification_date date,
  evaluation_date date,
  decision_date date,
  qualified_on timestamp with time zone,
  evaluation_on timestamp with time zone,
  decision_on timestamp with time zone,
  created_at timestamp with time zone default now(),
  updated_at timestamp with time zone default now(),
  constraint buyersphere_current_stage check (current_stage in ('qualification', 'evaluation', 'decision', 'adoption', 'closed'))
)
--;;
create table buyersphere_resource (
  id serial primary key,
  organization_id int references organization(id),
  buyersphere_id int references buyersphere(id),
  title text,
  link text,
  created_at timestamp with time zone default now(),
  updated_at timestamp with time zone default now()
)
--;;
create table buyersphere_conversation (
  id serial primary key,
  organization_id int references organization(id),
  buyersphere_id int references buyersphere(id),
  author int references user_account(id),
  message text,
  resolved boolean default false,
  created_at timestamp with time zone default now(),
  updated_at timestamp with time zone default now()
)
--;;
create table buyersphere_user_account (
  id serial primary key,
  organization_id int references organization(id),
  buyersphere_id int references buyersphere(id),
  user_account_id int references user_account(id),
  team text not null,
  ordering int not null,
  created_at timestamp with time zone default now(),
  updated_at timestamp with time zone default now()
)
