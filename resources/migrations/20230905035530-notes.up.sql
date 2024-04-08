create table buyersphere_note (
  id integer primary key generated always as identity,
  organization_id int references organization(id),
  buyersphere_id int references buyersphere(id),
  title text,
  body text,
  author int references user_account(id),
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);
--;;
create trigger buyersphere_note_insert_timestamp
before insert on buyersphere_note
for each row execute procedure trigger_insert_timestamps();
--;;

create trigger buyersphere_note_update_timestamp
before update on buyersphere_note
for each row execute procedure trigger_update_timestamp();

