DELETE FROM company_status;
INSERT INTO company_status (name)
VALUES ('bookmarked'),
       ('preentry'),
       ('informalContact'),
       ('experienceProgram'),
       ('preparingApplication'),
       ('underScreening'),
       ('interviewScheduling'),
       ('interviewInProgress'),
       ('finalSelection'),
       ('offerReceived'),
       ('offerAccepted'),
       ('offerDeclined'),
       ('selectionWithdrawn'),
       ('rejected');

DELETE FROM contact_types;
INSERT INTO contact_types (name)
VALUES ('email'),
       ('phone'),
       ('line'),
       ('platformMessage'),
       ('contactForm'),
       ('other');

DELETE FROM application_routes;
INSERT INTO application_routes (name)
VALUES ('mynavi'),
       ('rikunabi'),
       ('oneCareer'),
       ('gaishiShukatsu'),
       ('offerBox'),
       ('kimisuka'),
       ('dodaCampus'),
       ('iroots'),
       ('supporterz'),
       ('paiza'),
       ('levtech'),
       ('trackJob'),
       ('wantedly'),
       ('direct'),
       ('agent'),
       ('referral'),
       ('other');
