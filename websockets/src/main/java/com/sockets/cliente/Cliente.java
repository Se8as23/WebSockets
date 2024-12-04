package com.sockets.cliente;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

public class Cliente {

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String clienteNombre;
    private boolean conectado = false;

    public boolean iniciarConexion(String ip, int port){
        try{
            clientSocket = new Socket(ip, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            conectado = clientSocket.isConnected();
            return true;
        }catch(IOException ex){
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public void setNombre(String nombre){
        this.clienteNombre = nombre;
    }

    public String getNombre(){
        return this.clienteNombre;
    }

    public boolean isConectado(){
        return conectado;
    }

    public void enviarMensaje(String mensaje){
        mensaje = clienteNombre + ": " + mensaje;
        mensaje = encriptar(mensaje, 10);
        out.println(mensaje);
        System.out.println("Mando: " + mensaje);
    }

    public void recibirMensajes(JTextArea textArea){
        Thread lectura = new Thread(new Runnable() {
            @Override
            public void run(){
                try{
                    if(in.ready()){
                        String mensaje;
                        do{
                            mensaje = in.readLine();
                            System.out.println("Recibo: " + mensaje);
                            mensaje = desencriptar(mensaje, 10);
                            textArea.setText(textArea.getText() + mensaje + "\n");
                        }while(mensaje != null);
                    }
                }catch(Exception ex){
                    Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        lectura.start();
    }

    public void pararConexion(){
        try {
            in.close();
            out.close();
            clientSocket.close();
            conectado = false;
        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String encriptar(String texto, int desplazamiento) {
        StringBuilder cifrado = new StringBuilder();
        for (int i = 0; i < texto.length(); i++) {
            char caracter = texto.charAt(i);
            if (Character.isLetter(caracter)) {
                char base = Character.isUpperCase(caracter) ? 'A' : 'a';
                cifrado.append((char) ((caracter - base + desplazamiento) % 26 + base));
            } else {
                cifrado.append(caracter);
            }
        }
        return cifrado.toString();
    }

    public static String desencriptar(String textoCifrado, int desplazamiento) {
        return encriptar(textoCifrado, 26 - desplazamiento);
    }
}
