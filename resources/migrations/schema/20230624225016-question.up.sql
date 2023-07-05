-- TODO needs a better name
create table question ( 
  id serial primary key,
  organization_id int references organization(id),
  buyersphere_id int references buyersphere(id),
  page text not null,
  ordering int not null,
  type text,
  question text,
  answer jsonb,
  created_at timestamp with time zone default now(),
  updated_at timestamp with time zone default now(),
  constraint question_type check (type in ('text', 'list', 'pricing', 'resource')),
  constraint question_page check (page in ('overview', 'features', 'pricing', 'resources'))
)
-- add resources to type check