insert into organization (name, domain, subdomain, logo, stytch_organization_id) 
  values ('House Atriedes', 
    'https://www.house-atriedes.com', 
    'atreides', 
    '/house_atreides.webp', 
    'organization-test-bd2b29e6-8c0a-48e6-a1c4-d9689883785e'
);

insert into buyersphere (organization_id, name, status, logo) 
  values (1, 'House Corrino', 'active', '/house_corrino.png');

insert into question (organization_id, buyersphere_id, page, ordering, type, question, answer) values
  (1, 1, 'overview', 1, 'text', 
  '👋 A Message from Duke Leto Atreides', 
  '{"text": "Thank you for trusting House Atreides with Arakis. With your help we can keep the spice lines open and the money pouring in!"}'),
  (1, 1, 'overview', 2, 'list', 
  '🚀 Who our product serves:',
  '{"items": ["Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."]}'),
  (1, 1, 'overview', 3, 'list', 
  '✅ The problems we solve:',
  '{"items": ["Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."]}'),
  (1, 1, 'overview', 4, 'text', 
  '📓 Resources', 
  '{"items": ["Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."]}'),
  (1, 1, 'features', 1, 'text', 
  'Title and high level description of the problem it solves ', 
  '{"text": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."}'),
  (1, 1, 'features', 2, 'text', 
  'Title and high level description of the problem it solves 2', 
  '{"text": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."}'),
  (1, 1, 'features', 3, 'text', 
  'Title and high level description of the problem it solves 3', 
  '{"text": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."}'),
  (1, 1, 'features', 4, 'text', 
  'Title and high level description of the problem it solves 4', 
  '{"text": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."}'),
  (1, 1, 'features', 5, 'text', 
  'Title and high level description of the problem it solves 5', 
  '{"text": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."}'),
  (1, 1, 'pricing', 1, 'pricing', 
  'Tier 1', 
  '{"pricing": "$25/user/month", "description": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."}'),
  (1, 1, 'pricing', 2, 'pricing', 
  'Tier 2', 
  '{"pricing": "$125/user/month", "description": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."}'),
  (1, 1, 'pricing', 3, 'pricing', 
  'Tier 3', 
  '{"pricing": "$250/user/month", "description": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."}'),
  (1, 1, 'pricing', 4, 'pricing', 
  'Tier 4', 
  '{"pricing": "Custom", "description": "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."}'),
  (1, 1, 'resources', 1, 'resource', 
  'How Seismic can save you 78% of your Outreach Time', 
  '{"link": "https://www.google.com"}'),
  (1, 1, 'resources', 2, 'resource', 
  'Important qualification resource with impressive title', 
  '{"link": "https://www.google.com"}'),
  (1, 1, 'resources', 3, 'resource', 
  'Would you believe marketing spent $800k on this document?', 
  '{"link": "https://www.google.com"}')
;


insert into user_account (email, organization_id, role) values ('ryan@echternacht.org', 1, 'admin');
