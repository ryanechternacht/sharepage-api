alter table buyersphere drop constraint buyersphere_status;
--;;
alter table buyersphere add constraint buyersphere_status
check (status in ('active', 'on-hold', 'opt-out', 'closed'));
--;;

update buyersphere
set status = 'closed',
  current_stage = 'qualification'
where current_stage = 'closed';
--;;

alter table buyersphere drop constraint buyersphere_current_stage;
--;;
alter table buyersphere add constraint buyersphere_current_stage
check (current_stage in ('qualification', 'evaluation', 'decision', 'adoption'));
