do $$
begin
    if exists (
        select 1
        from information_schema.columns
        where table_schema = 'public'
          and table_name = 'post_comments'
          and column_name = 'parent_cooment_id'
    ) and not exists (
        select 1
        from information_schema.columns
        where table_schema = 'public'
          and table_name = 'post_comments'
          and column_name = 'parent_comment_id'
    ) then
        alter table public.post_comments
            rename column parent_cooment_id to parent_comment_id;
    elsif not exists (
        select 1
        from information_schema.columns
        where table_schema = 'public'
          and table_name = 'post_comments'
          and column_name = 'parent_comment_id'
    ) then
        alter table public.post_comments
            add column parent_comment_id bigint
                references public.post_comments(comment_id);
    end if;
end $$;
