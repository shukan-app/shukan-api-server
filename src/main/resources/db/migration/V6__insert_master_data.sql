INSERT INTO company_status (name)
VALUES ('bookmarked'),
       ('preentry'),
       ('informal_contact'),
       ('experience_program'),
       ('preparing_application'),
       ('under_screening'),
       ('interview_scheduling'),
       ('interview_in_progress'),
       ('final_selection'),
       ('offer_received'),
       ('offer_accepted'),
       ('offer_declined'),
       ('selection_withdrawn'),
       ('rejected');

INSERT INTO contact_types (name)
VALUES ('email'),
       ('phone'),
       ('line'),
       ('platform_message'),
       ('contact_form'),
       ('other');

INSERT INTO application_routes (name)
VALUES ('mynavi'),
       ('rikunabi'),
       ('one_career'),
       ('gaishi_shukatsu'),
       ('offer_box'),
       ('kimisuka'),
       ('doda_campus'),
       ('iroots'),
       ('supporterz'),
       ('paiza'),
       ('levtech'),
       ('track_job'),
       ('wantedly'),
       ('direct'),
       ('agent'),
       ('referral'),
       ('other');

INSERT INTO task_creator_kinds (name)
VALUES ('user'),
       ('ai');

INSERT INTO task_status (name)
VALUES ('complete'),
       ('incomplete');

INSERT INTO task_types (name)
VALUES ('document_submission'),
       ('assessment'),
       ('scheduling'),
       ('reply_required'),
       ('preparation'),
       ('other');

INSERT INTO event_format_types (name)
VALUES ('online'),
       ('offline'),
       ('hybrid'),
       ('on_demand');

INSERT INTO event_status (name)
VALUES ('scheduled'),
       ('attended'),
       ('absent'),
       ('canceled'),
       ('rescheduling_required');

INSERT INTO event_types (name)
VALUES ('info_session'),
       ('informal_meet'),
       ('interview'),
       ('group_work'),
       ('experience_program'),
       ('assessment'),
       ('offer_event'),
       ('other');

INSERT INTO recruiting_platforms (name)
VALUES ('mynavi'),
       ('rikunabi'),
       ('one_career'),
       ('gaishi_shukatsu'),
       ('offer_box'),
       ('kimisuka'),
       ('doda_campus'),
       ('iroots'),
       ('supporterz'),
       ('paiza'),
       ('levtech'),
       ('track_job'),
       ('wantedly'),
       ('direct'),
       ('agent'),
       ('referral'),
       ('other');

INSERT INTO scout_status (name)
VALUES ('unread'),
       ('unresponded'),
       ('accepted'),
       ('declined');
