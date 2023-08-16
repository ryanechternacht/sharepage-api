alter table deal_timing drop constraint deal_timing_pkey;
--;;
alter table deal_timing add column id serial primary key;
