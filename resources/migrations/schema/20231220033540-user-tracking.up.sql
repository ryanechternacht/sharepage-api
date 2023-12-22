create table buyer_tracking (
  organization_id int references organization(id),
  buyersphere_id int references buyersphere(id),
  user_account_id int references user_account(id),
  activity text not null,
  activity_data jsonb,
  created_at timestamp with time zone not null,
  constraint buyer_tracking_activity check (activity in ('site-activity'))
);
--;;
create or replace function trigger_insert_only_timestamps()
returns trigger as $$
begin 
  new.created_at = CURRENT_TIMESTAMP;
  return new;
end;
$$ LANGUAGE plpgsql;
--;;
create trigger buyer_tracking_insert_timestamp
before insert on buyer_tracking
for each row execute procedure trigger_insert_only_timestamps();
--;;
create index idx_buyer_tracking_organization_id_buyersphere_id 
on buyer_tracking(organization_id, buyersphere_id);
