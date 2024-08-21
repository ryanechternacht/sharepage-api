alter table buyersphere alter column is_public set default true;
--;;
update buyersphere set is_public = true;
--;;
alter table buyersphere_page alter column can_buyer_edit set default false;
--;;
update buyersphere_page set can_buyer_edit = false;
