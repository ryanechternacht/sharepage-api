alter table buyer_session 
  add column swaypage_type text not null default 'swaypage';
--;;
alter table buyer_session add constraint buyer_session_swaypage_type
  check (swaypage_type in ('swaypage', 'virtual-swaypage'));
--;;
alter table buyer_session alter column buyersphere_id drop not null;
--;;
alter table buyer_session add column virtual_swaypage_id int;
--;;

alter table buyer_session_event
  add column swaypage_type text not null default 'swaypage';
--;;
alter table buyer_session_event add constraint buyer_session_event_swaypage_type
  check (swaypage_type in ('swaypage', 'virtual-swaypage'));
--;;
alter table buyer_session_event alter column buyersphere_id drop not null;
--;;
alter table buyer_session_event add column virtual_swaypage_id int;
--;;

alter table buyer_session_timing
  add column swaypage_type text not null default 'swaypage';
--;;
alter table buyer_session_timing add constraint buyer_session_timing_swaypage_type
  check (swaypage_type in ('swaypage', 'virtual-swaypage'));
--;;
alter table buyer_session_timing alter column buyersphere_id drop not null;
--;;
alter table buyer_session_timing add column virtual_swaypage_id int;
--;;
