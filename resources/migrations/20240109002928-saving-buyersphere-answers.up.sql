alter table buyersphere add column 
  success_criteria_answer jsonb default '{"text": ""}'::jsonb;
--;;
alter table buyersphere add column
  objectives_answer jsonb default '{"text": ""}'::jsonb;
--;;
alter table buyersphere add column
  constraints_answer jsonb default '{"text": ""}'::jsonb;
