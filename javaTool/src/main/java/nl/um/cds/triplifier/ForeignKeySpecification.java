package nl.um.cds.triplifier;

public class ForeignKeySpecification {
    public String primaryKeyTable = "";
    public String primaryKeyColumn = "";
    public String foreignKeyTable = "";
    public String foreignKeyColumn = "";

    public ForeignKeySpecification(String primaryKeyTable, String primaryKeyColumn, String foreignKeyTable, String foreignKeyColumn) {
        this.primaryKeyTable = primaryKeyTable;
        this.primaryKeyColumn = primaryKeyColumn;
        this.foreignKeyTable = foreignKeyTable;
        this.foreignKeyColumn = foreignKeyColumn;
    }

    public String getPrimaryKeyTable() {
        return primaryKeyTable;
    }

    public String getPrimaryKeyColumn() {
        return primaryKeyColumn;
    }

    public String getForeignKeyTable() {
        return foreignKeyTable;
    }

    public String getForeignKeyColumn() {
        return foreignKeyColumn;
    }
}
