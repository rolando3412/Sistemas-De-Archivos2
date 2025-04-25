
package org.example;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            InterfazDeUsuario interfaz = new InterfazDeUsuario();
            interfaz.mostrar();
        });
    }
}