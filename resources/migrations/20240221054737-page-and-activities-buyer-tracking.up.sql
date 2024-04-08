alter table buyer_tracking drop constraint buyer_tracking_activity;
--;;
alter table buyer_tracking add constraint buyer_tracking_activity 
  check (activity in (
    'site-activity',
    'create-activity',
    'edit-activity',
    'resolve-activity',
    'unresolve-activity',
    'delete-activity',
    'edit-constraints',
    'edit-success-criteria',
    'edit-features',
    'edit-objectives',
    'open-asset',
    'click-share',
    'hold-deal',
    'reactivate-deal',
    'invite-user',
    'edit-user',
    'accept-invite',
    'remove-user',
    'edit-page'
  ));
  