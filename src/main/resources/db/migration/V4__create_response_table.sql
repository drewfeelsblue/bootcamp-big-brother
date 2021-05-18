create table if not exists big_brother.t_responses
(
    id      bigint generated always as identity,
    task_id bigint not null,
    user_id text   not null,
    constraint t_responses_id_pk primary key (id),
    constraint t_responses_task_id_user_id_uindex unique (task_id, user_id),
    constraint t_responses_task_id_fk foreign key (task_id) references big_brother.t_tasks (id)
);