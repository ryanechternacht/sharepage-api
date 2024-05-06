alter table buyersphere add column owner_id int references user_account(id);
--;;
alter table buyersphere add column priority int not null default 2;
