alter table buyersphere add column if not exists shortcode text;
--;;
create index if not exists idx_buyersphere_organization_id_shortcode
on buyersphere(organization_id, shortcode);
