create table if not exists big_brother.t_tasks
(
    id         bigint generated always as identity,
    topic      text not null,
    title      text not null,
    creator_id text not null,
    constraint t_tasks_id_pk primary key (id),
    constraint t_tasks_topic_title_uindex unique (topic, title)
);