insert into organization (name, domain, subdomain, logo, stytch_organization_id) 
  values ('House Stark', 
    'https://www.house-stark.com', 
    'stark', 
    '/house_stark.png', 
    'organization-test-4f1a88d6-b33c-4a12-8d8d-466bdb89c781'
);

insert into buyersphere (organization_id, buyer, buyer_logo, intro_message, 
                         qualification_date, evaluation_date, decision_date) 
  values (1, 'House Tully', '/house_tully.png', 
    'Thank you for considering my son, Ned, for a marriage with your daughter. Together we can build an alliance to keep the peace and promote prosperity throughout the North and Riverlands.', 
    now() + interval '30 day', now() + interval '60 day', now() + interval '90 day');

insert into deal_resource (organization_id, title, link) values
(1, 'Where to buy our banner', 'https://www.amazon.com/Calhoun-Sportswear-Thrones-Banner-Fringe/dp/B01LW86RL5/ref=asc_df_B01LW86RL5/?tag=hyprod-20&linkCode=df0&hvadid=198074483184&hvpos=&hvnetw=g&hvrand=13151580190832163203&hvpone=&hvptwo=&hvqmt=&hvdev=c&hvdvcmdl=&hvlocint=&hvlocphy=9015695&hvtargid=pla-324365812736&psc=1'),
(1, 'House Stark items on Etsy', 'https://www.etsy.com/market/house_stark_sign');

insert into buyersphere_resource (organization_id, buyersphere_id, title, link)
select organization_id, 1, title, link from deal_resource;

insert into persona (organization_id, ordering, title, description) values
(1, 0, 'Honorable Men', 'House Stark is the most honorable house in westeros.'),
(1, 1, 'Those skeptical of the seven', 'We keep the old gods in the north. Come worship trees with us!'),
(1, 2, 'Those who love snow', 'We see a lot of snowfall. If you love sledding, ice luge, or ice sculpting, you''ll love it here!');

insert into pain_point (organization_id, ordering, title, description) values
(1, 0, 'Putting down ironmen raiding', 'We''ve done it before, and we''ll do it again!'),
(1, 1, 'Tired of the politicking of Southron lords?', 'We''re a simple people. We keep our word, work our land, and kill wildlings. ''nuff said.');

insert into feature (organization_id, ordering, title, description) values
(1, 0, 'We have the largest land holdings in Westeros', 'There''s plenty of land to settle, farm, and develop. We''re excited to welcome new hardworking Westerosi''s to our land to join us. We have honor, low taxes, and no Lannisters!'),
(1, 1, 'We have some of the most storied Architecture in Westeros', 'Come and enjoy the legacy of Bran the Builder. A 700 foot wall! A castle with warm water coursing through it''s walls to keep it warm! The only remaining godswood in Westeros!'),
(1, 2, 'North of the Wall tours', 'You''ve never seen true beauty unil you''ve seen a wildling kissed by fire. Join us on our North of the Wall tour to see Wargs, Wolves, and more!' );

-- real users
insert into user_account (organization_id, email, name, buyersphere_role, display_role) values 
(1, 'ryan@echternacht.org', 'ryan echternacht', 'admin', 'Narrator');

-- fake users
insert into user_account (organization_id, email, name, buyersphere_role, display_role) values 
(1, 'rickon@stark.com', 'Rickon Stark', 'admin', 'Lord of Winterfell'),
(1, 'ned@stark.com', 'Ned Stark', 'admin', 'Heir of Winterfell'),
(1, 'holster@tully.com', 'Holster Tully', 'buyer', 'Lord of Riverrun'),
(1, 'brynden@tully.com', 'Brynden Stark', 'buyer', 'Blackfish'),
(1, 'minisa@tully.com', 'Minisa Tully', 'buyer', 'Of House Whent'),
(1, 'catelyn@tully.com', 'Catelyn Tully', 'buyer', 'Cherished Daughter');

insert into buyersphere_user_account (organization_id, buyersphere_id, user_account_id, team, ordering) values
(1, 1, 1, 'seller', 2),
(1, 1, 2, 'seller', 0),
(1, 1, 3, 'seller', 1),
(1, 1, 4, 'buyer', 0),
(1, 1, 5, 'buyer', 1),
(1, 1, 6, 'buyer', 2),
(1, 1, 7, 'buyer', 3);

insert into buyersphere_conversation (organization_id, buyersphere_id, author, message, resolved) values
(1, 1, 4, 'How do you tax your peasants? Fair and equitable tax rates is important to me and Minisa', true),
(1, 1, 5, 'When was your last festival? How big are your jousting tourneys?', false),
(1, 1, 6, 'Will my daughter have a sept to pray in?', false);

