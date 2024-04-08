alter table buyersphere drop constraint buyersphere_current_stage;
--;;
alter table buyersphere add constraint buyersphere_current_stage
check (current_stage in ('qualification', 'evaluation', 'decision', 'adoption', 'closed'));
--;;

update buyersphere
set current_stage = 'closed',
  status = 'opt-out'
where status = 'closed';
--;;

alter table buyersphere drop constraint buyersphere_status;
--;;
alter table buyersphere add constraint buyersphere_status
check (status in ('active', 'on-hold', 'opt-out'));
--;;


