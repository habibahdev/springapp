-- V2 : ajout de deux colonnes dans la table Purchase

alter table purchase
add column stripe_session_id varchar(255),
add column stripe_payment_intent_id varchar(255);

alter table purchase
add constraint uk_purchase_stripe_session_id unique(stripe_session_id);