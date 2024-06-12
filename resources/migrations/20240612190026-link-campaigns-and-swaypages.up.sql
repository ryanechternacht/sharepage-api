alter table buyersphere
add column campaign_uuid uuid references campaign(uuid);
