create table persona (
  id serial primary key,
  organization_id int references organization(id),
  ordering int not null,
  title text,
  description text,
  created_at timestamp with time zone default now(),
  updated_at timestamp with time zone default now()
)
--;;
create table pain_point (
  id serial primary key,
  organization_id int references organization(id),
  ordering int not null,
  title text,
  description text,
  created_at timestamp with time zone default now(),
  updated_at timestamp with time zone default now()
)
--;;
create table feature (
  id serial primary key,
  organization_id int references organization(id),
  ordering int not null,
  title text,
  description text,
  created_at timestamp with time zone default now(),
  updated_at timestamp with time zone default now()
)
--;;
create table deal_timing (
  id serial primary key,
  organization_id int references organization(id),
  qualified_days int,
  evaluation_days int,
  decision_days int,
  created_at timestamp with time zone default now(),
  updated_at timestamp with time zone default now()
)
--;;
create table deal_resource (
  id serial primary key,
  organization_id int references organization(id),
  title text,
  link text,
  created_at timestamp with time zone default now(),
  updated_at timestamp with time zone default now()
)
