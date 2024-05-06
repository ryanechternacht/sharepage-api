create or replace function trigger_update_buyersphere_timestamp_on_old() 
returns trigger as $$
begin
  update buyersphere
  set updated_at = CURRENT_TIMESTAMP
  where id = old.buyersphere_id;

  return new;
end;
$$ LANGUAGE plpgsql;
--;;
create trigger buyersphere_page_update_buyersphere_timestamp
before update on buyersphere_page
for each row execute procedure trigger_update_buyersphere_timestamp_on_old();
--;;
create trigger buyersphere_page_delete_buyersphere_timestamp
before delete on buyersphere_page
for each row execute procedure trigger_update_buyersphere_timestamp_on_old();
--;;

create or replace function trigger_update_buyersphere_timestamp_on_new()
returns trigger as $$
begin
  update buyersphere
  set updated_at = CURRENT_TIMESTAMP
  where id = new.buyersphere_id;

  return new;
end;
$$ LANGUAGE plpgsql;
--;;
create trigger buyersphere_page_insert_buyersphere_timestamp
before insert on buyersphere_page
for each row execute procedure trigger_update_buyersphere_timestamp_on_new();
