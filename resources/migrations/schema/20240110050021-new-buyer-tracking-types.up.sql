alter table buyer_tracking drop constraint buyer_tracking_activity;
--;;
alter table buyer_tracking add constraint buyer_tracking_activity 
  check (activity in (
    'site-activity', 
    'create-conversation', 
    'edit-conversation', 
    'resolve-conversation', 
    'delete-conversation', 
    'edit-constraints',
    'edit-success-criteria',
    'edit-features',
    'edit-objectives',
    'opened-asset',
    'clicked-share',
    'deal-hold',
    'deal-reactivate',
    'invited-user',
    'accepted-invite',
    'removed-user'
  ));
