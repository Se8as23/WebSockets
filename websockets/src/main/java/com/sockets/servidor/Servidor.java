package com.sockets.servidor;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Servidor {

    private ServerSocket serverSocket;
    private final List<PrintWriter> clientes = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        servidor.iniciar(8080);
    }

    public void iniciar(int puerto) {
        try {
            serverSocket = new ServerSocket(puerto);
            System.out.println("Servidor iniciado en el puerto " + puerto);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Nuevo cliente conectado: " + socket.getInetAddress());
                new ConexionCliente(socket).start();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            parar();
        }
    }

    public void parar() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private class ConexionCliente extends Thread {
        private final Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ConexionCliente(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Agregar la conexión a la lista de clientes
                clientes.add(out);

                String mensaje;
                while ((mensaje = in.readLine()) != null) {
                    if ("salir".equalsIgnoreCase(mensaje.trim())) {
                        out.println("Adiós");
                        break;
                    }
                    System.out.println("Mensaje recibido: " + mensaje);

                    // Enviar el mensaje a todos los demás clientes
                    for (PrintWriter cliente : clientes) {
                        if (cliente != out) {
                            cliente.println(mensaje);
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                clientes.remove(out);
            }
        }
    }
}
