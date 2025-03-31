package com.mycompany.threadpool;

import java.sql.*;
import java.util.List;

public class ProcesarLote implements Runnable {

    private final List<Registro> lote;

    // Cargar la configuración
    Config config = new Config();

    public ProcesarLote(List<Registro> lote) {
        this.lote = lote;
    }

    @Override
    public void run() {
        System.out.println("🟢 Procesando lote de " + lote.size() + " registros en " + Thread.currentThread().getName());
        boolean exito = false;
        int intentos = 0;

        while (!exito && intentos < BDThreadPoolBatch.REINTENTOS_MAX) {
            try {
                insertarLoteEnBD(lote);
                exito = true;
            } catch (SQLException e) {
                intentos++;
                System.err.println("⚠️ Error insertando lote (Intento " + intentos + "): " + e.getMessage());

                if (intentos >= BDThreadPoolBatch.REINTENTOS_MAX) {
                    System.err.println("❌ Lote fallido tras " + BDThreadPoolBatch.REINTENTOS_MAX + " intentos.");
                } else {
                    System.out.println("🔄 Reintentando...");
                }
            }
        }
    }

    private void insertarLoteEnBD(List<Registro> lote) throws SQLException {
//        try (Connection conn = DriverManager.getConnection("jdbc:sqlserver://localhost;databaseName=miBD", "user", "pass")) {
        try (Connection conn = DriverManager.getConnection(config.getDbUrl(), config.getDbUsername(), config.getDbPassword())) {

            conn.setAutoCommit(false); // 🔹 Iniciar transacción
//            String sql = "INSERT INTO carriers_destino (id, carrier_company) VALUES (?, ?)";
            String sql = "INSERT INTO carriers_destino (carrier_company) VALUES (?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            for (Registro reg : lote) {
                try {
//                    pstmt.setInt(1, reg.id);
                    pstmt.setString(1, reg.nombre);
                    pstmt.addBatch();  // Agregar a batch
                } catch (Exception e) {
                    System.err.println("⚠️ Error con registro ID " + reg.id + ": " + e.getMessage());
                }
            }

            pstmt.executeBatch(); // 🔹 Ejecutar batch
            conn.commit(); // 🔹 Confirmar transacción
            System.out.println("✅ Insertados " + lote.size() + " registros en " + Thread.currentThread().getName());
        } catch (SQLException e) {
            throw new SQLException("Error en la base de datos: " + e.getMessage(), e);
        }
    }
}
