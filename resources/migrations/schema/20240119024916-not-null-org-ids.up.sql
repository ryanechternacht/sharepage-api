alter table user_account alter column organization_id set not null;
--;;
alter table persona alter column organization_id set not null;
--;;
alter table pain_point alter column organization_id set not null;
--;;
alter table feature alter column organization_id set not null;
--;;
alter table deal_resource alter column organization_id set not null;
--;;
alter table pricing_tier alter column organization_id set not null;
--;;
alter table buyersphere alter column organization_id set not null;
--;;
alter table buyersphere_resource alter column organization_id set not null;
--;;
alter table buyersphere_conversation alter column organization_id set not null;
--;;
alter table buyersphere_user_account alter column organization_id set not null;
--;;
alter table buyersphere_note alter column organization_id set not null;
--;;
alter table salesforce_access alter column organization_id set not null;
--;;
alter table buyer_tracking alter column organization_id set not null;
--;;
alter table conversation_template_item alter column organization_id set not null;
