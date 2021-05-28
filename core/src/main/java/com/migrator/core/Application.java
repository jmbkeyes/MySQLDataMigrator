package com.migrator.core;

import com.migrator.core.dao.SchemaDAO;
import com.migrator.core.service.DataMigratorService;

import java.io.IOException;
import java.sql.SQLException;

public class Application {
    public static void main(String[] args) throws IOException, SQLException {
        DataMigratorService migratorService = new DataMigratorService(new SchemaDAO());
        migratorService.migrate();
    }
}
