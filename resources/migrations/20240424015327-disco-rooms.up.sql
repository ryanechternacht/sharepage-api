alter table buyersphere add column if not exists room_type text not null default 'deal-room';
--;;
alter table buyersphere drop constraint if exists buyersphere_room_type;
--;;
alter table buyersphere add constraint buyersphere_room_type
  check (room_type in (
    'deal-room',
    'discovery-room'
  ));
