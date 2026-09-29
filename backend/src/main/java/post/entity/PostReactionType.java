package post.entity;

public enum PostReactionType {
    RECOMMEND("recommend"),
    DISLIKE("dislike");

    private final String apiValue;

    PostReactionType(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }
}
