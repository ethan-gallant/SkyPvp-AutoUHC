package io.skypvp.uhc.database;

import java.sql.Connection;
import java.sql.ResultSet;

public class DatabaseQuery {
    
    private final Connection connection;
    private final ResultSet rs;
    
    public DatabaseQuery(Connection conn, ResultSet rs) {
        this.connection = conn;
        this.rs = rs;
    }
    
    public Connection getConnection() {
        return this.connection;
    }
    
    public ResultSet getResultSet() {
       return this.rs;
    }

}
