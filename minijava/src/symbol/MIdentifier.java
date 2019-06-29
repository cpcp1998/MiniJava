package symbol;

public abstract class MIdentifier implements MType {
    public MClassList allClassList;
    public String name;
    public String category;
    public int row, col;

    public MIdentifier(MClassList allClassList, String name, String category, int row, int col) {
        this.allClassList = allClassList;
        this.name = name;
        this.category = category;
        this.row = row;
        this.col = col;
    }

    @Override
    public MClassList getAllClassList() {
        return allClassList;
    }
}
