alter table buyersphere drop constraint if exists buyersphere_room_type;
--;;
alter table buyersphere add constraint buyersphere_room_type
  check (room_type in (
    'deal-room',
    'discovery-room',
    'template'
  ));
--;;

alter table buyersphere drop constraint if exists buyersphere_status;
--;;
alter table buyersphere add constraint buyersphere_status
  check (status in (
    'active', 
    'on-hold', 
    'opt-out', 
    'closed',
    'archived'
  ));
