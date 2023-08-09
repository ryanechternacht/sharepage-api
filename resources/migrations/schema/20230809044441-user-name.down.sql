alter table user_account add column name text;
--;;
update user_account
set name = first_name || ' ' || last_name;
--;;
alter table user_account drop column last_name;
--;;
alter table user_account drop column first_name;
