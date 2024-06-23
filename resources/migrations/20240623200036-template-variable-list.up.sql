alter table buyersphere add column template_custom_variables jsonb
  default '["field-1","field-2","field-3","field-4","field-5"]'::jsonb;
