-- This resetting the column to not null isn't safe, and we can't assume a default here :/
-- alter table buyersphere_conversation alter column assigned_to set not null;

alter table buyersphere_conversation drop column assigned_team;
