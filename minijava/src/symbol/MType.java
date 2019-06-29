package symbol;

import java.util.List;

public interface MType {
    MClassList getAllClassList();
    void checkUndefinedType(List<String> errMessage);
}
