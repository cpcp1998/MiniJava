package symbol;

import java.util.List;

public class MVar extends MIdentifier {
    public String type;

    public MVar(MClassList allClassList, String name, int row, int col, String type) {
        super(allClassList, name, "var", row, col);
        this.type = type;
    }

    @Override
    public void checkUndefinedType(List<String> errMessage) {
        if (!getAllClassList().containsKey(type)) {
            errMessage.add("Undefined type \"" + type + "\" of var at line " + row + ", column " + col);
        }
    }
}
