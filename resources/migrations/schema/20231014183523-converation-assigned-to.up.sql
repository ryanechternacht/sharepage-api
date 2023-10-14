alter table buyersphere_conversation add column assigned_to int references user_account(id);
--;;
update buyersphere_conversation 
set assigned_to = author;
--;;
alter table buyersphere_conversation alter column assigned_to set not null;
