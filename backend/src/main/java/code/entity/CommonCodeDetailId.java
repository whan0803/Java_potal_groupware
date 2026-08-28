package code.entity;

import java.io.Serializable;
import java.util.Objects;

public class CommonCodeDetailId implements Serializable {

    private String codeGroupId;
    private String codeValue;

    public CommonCodeDetailId() {
    }

    public CommonCodeDetailId(String codeGroupId, String codeValue) {
        this.codeGroupId = codeGroupId;
        this.codeValue = codeValue;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof CommonCodeDetailId that)) return false;
        return Objects.equals(codeGroupId, that.codeGroupId)
                && Objects.equals(codeValue, that.codeValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codeGroupId, codeValue);
    }
}
