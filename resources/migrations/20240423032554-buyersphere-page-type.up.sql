alter table buyersphere_page add column page_type text not null default ('general');
--;;
alter table buyersphere_page add constraint buyersphere_page_page_type
  check (page_type in (
    'general',
    'follow-up',
    'guide',
    'discussion',
    'business-case',
    'notes'
  ));
