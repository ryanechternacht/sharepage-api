insert into organization (name, domain, subdomain) 
  values ('Dunder Mifflin', 'https://www.dunder-mifflin.com', 'dunder-mifflin');

insert into orbit (organization_id, name, status, logo) 
  values (1, 'test orbit', 'active', 'https://www.bankofamerica.com/content/images/ContextualSiteGraphics/Logos/en_US/logos/colored_flagscape-v2.png');

insert into question (organization_id, orbit_id, page, 
  ordering, type, question, answer) values
  (1, 1, 'overview', 1, 'text', 
  '🧙‍♀️ Why we think we''re better together', 
  '{"text": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Massa placerat duis ultricies lacus sed turpis tincidunt id aliquet. Diam in arcu cursus euismod quis viverra nibh."}'),
  (1, 1, 'overview', 2, 'list', 
  '🐙 The pain points we''re solving for',
  '{"items": ["Eget duis at tellus at. Amet mauris commodo quis imperdiet.", "Non odio euismod lacinia at quis risus sed vulputate odio. Ut eu sem integer vitae justo.", "Urna duis convallis convallis tellus id interdum. Pellentesque sit amet porttitor eget dolor morbi."]}'),
  (1, 1, 'overview', 3, 'list', 
  '🤖 How our tools solve against your paint points',
  '{ "items": ["Enim nec dui nunc mattis enim ut. Massa placerat duis ultricies lacus sed turpis tincidunt id.", "Orci phasellus egestas tellus rutrum tellus pellentesque eu tincidunt.", "Vitae congue mauris rhoncus aenean vel. Ac turpis egestas maecenas pharetra convallis posuere." ]}'),
  (1, 1, 'overview', 4, 'text', 
  '🔌 How our integration works', 
  '{"text": "Ac tincidunt vitae semper quis. Tincidunt praesent semper feugiat nibh sed. Sed felis eget velit aliquet sagittis id. Cum sociis natoque penatibus et. A diam sollicitudin tempor id eu nisl nunc."}'),
  (1, 1, 'jvp', 1, 'text', 
  '💾 How we save time', 
  '{"text": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Massa placerat duis ultricies lacus sed turpis tincidunt id aliquet. Diam in arcu cursus euismod quis viverra nibh."}')
;