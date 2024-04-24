alter table buyersphere_page add column status text not null default 'active';
--;;
alter table buyersphere_page add constraint buyersphere_page_status
  check (status in (
    'active',
    'archived',
    'deleted'
  ));
