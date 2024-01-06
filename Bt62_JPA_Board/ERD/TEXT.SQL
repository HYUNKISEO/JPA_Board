select * from t7_authority;
select * from t7_user;
select * from t7_post;
select * from t7_attachment;

select *from t7_comment;

INSERT INTO t7_post (subject, content, reg_date, view_cnt, user_id)
VALUES
    ('Sample Post 1', 'This is sample content for post 1', NOW(), 0, 1),
    ('Sample Post 2', 'This is sample content for post 2', NOW(), 0, 1),
    ('Sample Post 3', 'This is sample content for post 3', NOW(), 0, 1),
    ('Sample Post 4', 'This is sample content for post 4', NOW(), 0, 1),
    ('Sample Post 5', 'This is sample content for post 5', NOW(), 0, 1);

select *from t7_user_authority;