alter table buyersphere_conversation alter column assigned_to drop not null;
--;;


alter table buyersphere_conversation add column assigned_team text;
--;;
update buyersphere_conversation 
set assigned_team = team
from buyersphere_user_account
where buyersphere_conversation.assigned_to = buyersphere_user_account.id;
--;;
alter table buyersphere_conversation alter column assigned_team set not null;
--;;
alter table buyersphere_conversation add constraint buyersphere_assigned_team check (assigned_team in ('buyer', 'seller'));
--;;


alter table buyersphere_conversation add column collaboration_type text not null;
--;;
update buyersphere_conversation
set collaboration_type = 'question';
--;;
alter table buyersphere_conversation add constraint buyersphere_conversation_collaboration_type check (collaboration_type in ('task', 'question', 'comment', 'meeting'));
