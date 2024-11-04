# Sharepage Rationale

This repo is the frontend (Nuxt, Vue 3) repo for Sharepage, a product designed to be a custom landing page that can be incluced in cold email outreach. 

## Interesting Technical Details

The most interesting technical details were

1. We built a WYSIWYG editor capability similar to Notion on top of TipTap. We also included touches like Unsplash hero images. This was built by 1 dev (me) in a matter of months, so there was a lot of room to evolve it, but the bones were solid.
2. The core value prop was landing pages, so we needed to support a url scheme that worked well for cold email. We landed on a scheme like `https://www.sharepage.io/asdf1234/ryan+echternacht`, where `asdf1234` was a shortcode that uniquely identified the page, and `ryan+echternacht` was fluff but made the urls look legitimate to humans and email spam blockers.
3. We started leveraging a set of chatgpt prompts to generate pages from a few key data points about a prospect.

## Buyersphere

This repo started as a different product (Buyersphere) that was a collaboration platform aimed at partner professionals. I think the web repo fully removed references to buyersphere (but they can be found throughout the api repo). 
