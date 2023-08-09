alter table user_account add column first_name text;
--;;
alter table user_account add column last_name text;
--;;
update user_account
set first_name = split_part(name, ' ', 1),
    last_name = split_part(name, ' ', 2);
--;;
alter table user_account drop column name;
