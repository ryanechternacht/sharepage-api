alter table buyersphere
add column campaign_row_number int;
--;;
update buyersphere
set campaign_row_number = rownumber - 1
from (
    select
        id, 
        campaign_uuid, 
        campaign_row_number, 
        ROW_NUMBER() over (partition by campaign_uuid order by id) as rownumber
    from
        buyersphere
    where campaign_uuid is not null
) as in_campaign
where buyersphere.id = in_campaign.id;
