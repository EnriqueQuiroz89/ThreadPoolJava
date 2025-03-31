package com.mycompany.threadpool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class BDThreadPoolBatch {

    public static final int NUM_HILOS = 6;  // Hilos en el pool
    public static final int TAMANO_LOTE = 100000;  // Tamaño del lote
    public static final int REINTENTOS_MAX = 3; // Intentos en caso de fallo

    public static void main(String[] args) {

        // 1. Iniciar el temporizador
        long startTime = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(NUM_HILOS);
        List<Registro> registros = new ArrayList<>();

        // Cargar la configuración
        Config config = new Config();

        // 1️⃣ Consultar la BD y almacenar registros en la lista
//        try (Connection conn = DriverManager.getConnection("jdbc:sqlserver://localhost;databaseName=miBD", "user", "pass")) {
        try (Connection conn = DriverManager.getConnection(config.getDbUrl(), config.getDbUsername(), config.getDbPassword())) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM carriers_1 limit 1000000");

            while (rs.next()) {

                // Obtener los valores de cada columna del resultado
                int id = rs.getInt("id");          // Asumiendo que "id" es un campo entero
                String nombre = rs.getString("carrier_company");  // Asumiendo que "nombre" es una cadena
//                registros.add(new Registro(rs.getInt("id"), rs.getString("nombre")));
                registros.add(new Registro(rs.getInt("id"), rs.getString("carrier_company")));
                // Imprimir el registro para ver el resultado de la consulta
//                System.out.println("ID: " + id + ", Nombre: " + nombre);
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Error al consultar la base de datos: " + e.getMessage());
            return;
        }

        // 2️⃣ Dividir la lista en lotes y asignar cada lote a un hilo
        int totalRegistros = registros.size();
        for (int i = 0; i < totalRegistros; i += TAMANO_LOTE) {
            int fin = Math.min(i + TAMANO_LOTE, totalRegistros);
            List<Registro> lote = new ArrayList<>(registros.subList(i, fin));
            executor.submit(new ProcesarLote(lote));
        }

        // 3️⃣ Cerrar el pool cuando termine
        executor.shutdown();

        // 5. Finalizar el temporizador
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;  // Tiempo en milisegundos
//        System.out.println("⏱️ El procesamiento de los 10,000 registros tomó: " + duration + " milisegundos.");
        System.out.println("⏱️ El procesamiento de los "+ registros.size()  +" registros tomó: " + duration + " milisegundos.");
//        System.out.println("🟢 Procesando lote de " + lote.size() + " registros en " + Thread.currentThread().getName());
       

    }
}
