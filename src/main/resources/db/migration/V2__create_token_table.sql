create table if not exists big_brother.t_tokens
(
    id      bigint generated always as identity,
    team_id text not null,
    type    text not null,
    value   text not null,
    user_id text not null,
    scope   text not null,
    constraint t_tokens_id_pk primary key (id)
);