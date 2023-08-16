alter table deal_timing drop column id;
--;;
alter table deal_timing add primary key (organization_id);
