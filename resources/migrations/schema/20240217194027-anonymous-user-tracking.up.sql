alter table buyer_tracking alter column organization_id set not null;
--;;
alter table buyer_tracking alter column buyersphere_id set not null;
--;;
alter table buyer_tracking add column linked_name text;
--;;
alter table buyer_tracking add column entered_name text;
