alter table buyersphere_conversation drop constraint buyersphere_conversation_collaboration_type;
--;;
alter table buyersphere_conversation add constraint buyersphere_conversation_collaboration_type 
  check (collaboration_type in ('task', 'question', 'comment', 'meeting', 'milestone'));
