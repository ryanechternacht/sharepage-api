alter table buyersphere_conversation drop constraint buyersphere_conversation_collaboration_type;
--;;
update buyersphere_conversation
set collaboration_type = 'action'
where collaboration_type = 'task';
--;;
alter table buyersphere_conversation add constraint buyersphere_conversation_collaboration_type 
  check (collaboration_type in ('action', 'question', 'comment', 'meeting', 'milestone'));
--;;

alter table conversation_template_item drop constraint conversation_template_item_collaboration_type;
--;;
update conversation_template_item
set collaboration_type = 'action'
where collaboration_type = 'task';
--;;
alter table conversation_template_item add constraint conversation_template_item_collaboration_type 
  check (collaboration_type in ('action', 'question', 'comment', 'meeting', 'milestone'));
--;;
