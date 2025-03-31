/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.threadpool;
import java.util.concurrent.*;

public class ThreadPoolExample {
   
      public static void main(String[] args) {
        // Crear un pool de 3 hilos con Java estándar
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Crear y enviar 5 tareas
        for (int i = 1; i <= 5; i++) {
            final int taskId = i;
            // Enviar tarea al pool
            Future<String> future = executor.submit(() -> {
                System.out.println("Tarea " + taskId + " ejecutándose en " + Thread.currentThread().getName());
                try {
                    Thread.sleep(2000); // Simula una tarea pesada
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return "Tarea " + taskId + " completada!";
            });

            // Agregar un listener para cuando la tarea termine sin bloquear el hilo principal
            new Thread(() -> {
                try {
                    System.out.println(future.get());  // Imprime cuando la tarea se complete
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        // Apagar el executor después de completar las tareas
        executor.shutdown();
    }
    
    
    
}
