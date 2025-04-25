package org.example;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

public class InterfazDeUsuario {
    private JPanel root;
    private JTree arbolArchivos;
    private JList<String> listaArchivos;
    private JTextField barraBusqueda;
    private JFrame frame;
    private Sistema_De_archivos sistema;
    private DefaultTreeModel treeModel;
    private DefaultListModel<String> listModel;
    private String rutaActual = "/";

    public InterfazDeUsuario() {
        sistema = new Sistema_De_archivos();
        inicializarComponentes();
        configurarEventos();
    }

    private void inicializarComponentes() {
        //! Crear frame principal
        frame = new JFrame("Sistema de Archivos");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        root = new JPanel(new BorderLayout());

        // Inicializar árbol
        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode("/");
        treeModel = new DefaultTreeModel(raiz);
        arbolArchivos = new JTree(treeModel);

        // Inicializar lista
        listModel = new DefaultListModel<>();
        listaArchivos = new JList<>(listModel);

        barraBusqueda = new JTextField("Buscar archivos...");

        //! Panel superior con barra de búsqueda y botones
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(barraBusqueda, BorderLayout.CENTER);

        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnNuevaCarpeta = new JButton("Nueva carpeta");
        JButton btnNuevoArchivo = new JButton("Nuevo archivo");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnAbrir = new JButton("Abrir carpeta");

        botonesPanel.add(btnNuevaCarpeta);
        botonesPanel.add(btnNuevoArchivo);
        botonesPanel.add(btnEliminar);
        botonesPanel.add(btnAbrir);
        topPanel.add(botonesPanel, BorderLayout.EAST);

        //! Panel izquierdo con árbol
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        leftPanel.setPreferredSize(new Dimension(250, 500));
        JScrollPane scrollTree = new JScrollPane(arbolArchivos);
        leftPanel.add(scrollTree, BorderLayout.CENTER);

        //! Panel central con lista
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scrollList = new JScrollPane(listaArchivos);
        centerPanel.add(scrollList, BorderLayout.CENTER);

        // Split pane
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                leftPanel,
                centerPanel
        );
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(250);

        root.add(topPanel, BorderLayout.NORTH);
        root.add(splitPane, BorderLayout.CENTER);

        frame.setContentPane(root);
        frame.setLocationRelativeTo(null);

        // Inicializar sistema
        sistema.crearCarpeta("", "root");
        actualizarVistaArbol();
    }

    private void configurarEventos() {
        //!Evento de selección en el árbol
        arbolArchivos.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode nodoSeleccionado =
                    (DefaultMutableTreeNode) arbolArchivos.getLastSelectedPathComponent();

            if (nodoSeleccionado != null) {
                rutaActual = obtenerRutaCompleta(nodoSeleccionado);
                actualizarListaArchivos(rutaActual);
            }
        });

        //! Panel de botones
        JPanel topPanel = (JPanel) root.getComponent(0);
        JPanel botonesPanel = (JPanel) topPanel.getComponent(1);

        //! Botón nueva carpeta
        JButton btnNuevaCarpeta = (JButton) botonesPanel.getComponent(0);
        btnNuevaCarpeta.addActionListener(e -> {
            String nombreCarpeta = JOptionPane.showInputDialog(frame, "Nombre de la carpeta:");
            if (nombreCarpeta != null && !nombreCarpeta.trim().isEmpty()) {
                sistema.crearCarpeta(rutaActual, nombreCarpeta);
                actualizarVistaArbol();
                actualizarListaArchivos(rutaActual);
            }
        });

        //! Botón nuevo archivo
        JButton btnNuevoArchivo = (JButton) botonesPanel.getComponent(1);
        btnNuevoArchivo.addActionListener(e -> {
            String nombreArchivo = JOptionPane.showInputDialog(frame, "Nombre del archivo:");
            if (nombreArchivo != null && !nombreArchivo.trim().isEmpty()) {
                sistema.crearArchivo(rutaActual, nombreArchivo);
                actualizarListaArchivos(rutaActual);
            }
        });

        //! Botón eliminar
        JButton btnEliminar = (JButton) botonesPanel.getComponent(2);
        btnEliminar.addActionListener(e -> {
            String elementoAEliminar = obtenerElementoSeleccionado();
            if (elementoAEliminar != null) {
                confirmarYEliminarElemento(elementoAEliminar);
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Por favor, seleccione un elemento para eliminar",
                        "Aviso",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        //! Botón abrir carpeta
        JButton btnAbrir = (JButton) botonesPanel.getComponent(3);
        btnAbrir.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fileChooser.showOpenDialog(root) == JFileChooser.APPROVE_OPTION) {
                File selectedDirectory = fileChooser.getSelectedFile();
                JOptionPane.showMessageDialog(frame,
                        "Carpeta seleccionada: " + selectedDirectory.getAbsolutePath());
            }
        });

        // Menú contextual
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItemMover = new JMenuItem("Mover a...");
        popupMenu.add(menuItemMover);

        arbolArchivos.setComponentPopupMenu(popupMenu);
        listaArchivos.setComponentPopupMenu(popupMenu);

        menuItemMover.addActionListener(e -> {
            DefaultMutableTreeNode nodoSeleccionado =
                    (DefaultMutableTreeNode) arbolArchivos.getLastSelectedPathComponent();

            if (nodoSeleccionado == null && listaArchivos.getSelectedValue() == null) {
                JOptionPane.showMessageDialog(frame,
                        "Seleccione un archivo o carpeta para mover",
                        "Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String rutaOrigen = nodoSeleccionado != null ?
                    obtenerRutaCompleta(nodoSeleccionado) :
                    rutaActual + "/" + listaArchivos.getSelectedValue();

            mostrarDialogoMover(rutaOrigen);
        });

        //! Barra de búsqueda
        barraBusqueda.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (barraBusqueda.getText().equals("Buscar archivos...")) {
                    barraBusqueda.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (barraBusqueda.getText().isEmpty()) {
                    barraBusqueda.setText("Buscar archivos...");
                }
            }
        });

        barraBusqueda.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String textoBusqueda = barraBusqueda.getText().trim();
                if (!textoBusqueda.isEmpty() && !textoBusqueda.equals("Buscar archivos...")) {
                    List<Nodo> resultados = sistema.buscarPorCoincidenciaParcial(textoBusqueda);
                    mostrarResultadosBusqueda(resultados);
                } else {
                    actualizarListaArchivos(rutaActual);
                }
            }
        });
    }

    private void mostrarDialogoMover(String rutaOrigen) {
        JDialog dialog = new JDialog(frame, "Seleccionar destino", true);
        dialog.setLayout(new BorderLayout());

        JTree arbolDestino = new JTree(crearModeloSoloCarpetas());
        JScrollPane scrollPane = new JScrollPane(arbolDestino);

        JPanel buttonPanel = new JPanel();
        JButton aceptar = new JButton("Aceptar");
        JButton cancelar = new JButton("Cancelar");

        buttonPanel.add(aceptar);
        buttonPanel.add(cancelar);

        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        aceptar.addActionListener(e -> {
            DefaultMutableTreeNode nodoDestino =
                    (DefaultMutableTreeNode) arbolDestino.getLastSelectedPathComponent();

            if (nodoDestino != null) {
                String rutaDestino = obtenerRutaCompleta(nodoDestino);
                sistema.mover(rutaOrigen, rutaDestino);
                actualizarVistaArbol();
                actualizarListaArchivos(rutaActual);
            }
            dialog.dispose();
        });

        cancelar.addActionListener(e -> dialog.dispose());

        dialog.setSize(300, 400);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private TreeModel crearModeloSoloCarpetas() {
        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode("/");
        construirArbolCarpetas(raiz, "/");
        return new DefaultTreeModel(raiz);
    }

    private void construirArbolCarpetas(DefaultMutableTreeNode nodoPadre, String ruta) {
        List<String> contenidos = sistema.listarContenido(ruta);
        for (String item : contenidos) {
            String rutaCompleta = ruta.equals("/") ? "/" + item : ruta + "/" + item;
            Nodo nodoReal = sistema.getArbol_Nario().buscarNodo(
                    sistema.getArbol_Nario().getRaiz(),
                    rutaCompleta
            );

            if (nodoReal != null && nodoReal.isEsCarpeta()) {
                DefaultMutableTreeNode nodoHijo = new DefaultMutableTreeNode(item);
                nodoPadre.add(nodoHijo);
                construirArbolCarpetas(nodoHijo, rutaCompleta);
            }
        }
    }

    private void actualizarVistaArbol() {
        DefaultMutableTreeNode raiz = (DefaultMutableTreeNode) treeModel.getRoot();
        raiz.removeAllChildren();
        construirArbolVisual(raiz, "/");
        treeModel.reload();
        arbolArchivos.expandRow(0);
    }

    private void construirArbolVisual(DefaultMutableTreeNode nodoPadre, String ruta) {
        List<String> contenidos = sistema.listarContenido(ruta);
        for (String item : contenidos) {
            DefaultMutableTreeNode nodoHijo = new DefaultMutableTreeNode(item);
            nodoPadre.add(nodoHijo);

            String rutaCompleta = ruta.equals("/") ? "/" + item : ruta + "/" + item;
            Nodo nodoReal = sistema.getArbol_Nario().buscarNodo(
                    sistema.getArbol_Nario().getRaiz(),
                    rutaCompleta
            );

            if (nodoReal != null && nodoReal.isEsCarpeta()) {
                construirArbolVisual(nodoHijo, rutaCompleta);
            }
        }
    }

    private String obtenerRutaCompleta(DefaultMutableTreeNode nodo) {
        StringBuilder ruta = new StringBuilder(nodo.getUserObject().toString());
        DefaultMutableTreeNode padre = (DefaultMutableTreeNode) nodo.getParent();

        while (padre != null) {
            String nombrePadre = padre.getUserObject().toString();
            if (!nombrePadre.equals("/")) {
                ruta.insert(0, nombrePadre + "/");
            }
            padre = (DefaultMutableTreeNode) padre.getParent();
        }

        return ruta.toString();
    }

    private void actualizarListaArchivos(String ruta) {
        listModel.clear();
        List<String> contenidos = sistema.listarContenido(ruta);
        for (String item : contenidos) {
            listModel.addElement(item);
        }
    }

    private void mostrarResultadosBusqueda(List<Nodo> resultados) {
        listModel.clear();
        for (Nodo nodo : resultados) {
            String ruta = nodo.getRuta();
            String prefijo = nodo.isEsCarpeta() ? "📁 " : "📄 ";
            // Guardar la ruta completa como el elemento de la lista para facilitar la eliminación
            listModel.addElement(prefijo + nodo.getNombre() + " [" + ruta + "]");
        }
    }

    private String obtenerElementoSeleccionado() {
        //! Primero intentar obtener selección de la lista
        String elementoSeleccionado = listaArchivos.getSelectedValue();
        if (elementoSeleccionado != null) {
            // Buscar si es un resultado de búsqueda con formato especial (contiene ruta entre corchetes)
            if (elementoSeleccionado.contains(" [") && elementoSeleccionado.endsWith("]")) {
                // Extraer la ruta completa desde los corchetes
                int inicio = elementoSeleccionado.indexOf(" [") + 2;
                int fin = elementoSeleccionado.length() - 1;
                return elementoSeleccionado.substring(inicio, fin);
            }
            
            // Eliminar posibles prefijos de iconos (📁 o 📄)
            if (elementoSeleccionado.startsWith("📁 ") || elementoSeleccionado.startsWith("📄 ")) {
                elementoSeleccionado = elementoSeleccionado.substring(2).trim();
            }
            
            // Para elementos normales en la lista, construir la ruta completa
            return rutaActual.equals("/") ?
                    "/" + elementoSeleccionado :
                    rutaActual + "/" + elementoSeleccionado;
        }

        // Si no hay selección en la lista, intentar con el árbol
        DefaultMutableTreeNode nodoSeleccionado =
                (DefaultMutableTreeNode) arbolArchivos.getLastSelectedPathComponent();
        if (nodoSeleccionado != null) {
            String rutaCompleta = obtenerRutaCompleta(nodoSeleccionado);
            //! Validar que no sea la raíz
            if (!rutaCompleta.equals("/") && !rutaCompleta.equals("/root")) {
                return rutaCompleta;
            }
        }

        return null;
    }

    private void confirmarYEliminarElemento(String rutaElemento) {
        //! Validar que la ruta no sea nula o vacía
        if (rutaElemento == null || rutaElemento.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                    "No se ha seleccionado ningún elemento para eliminar",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String nombreElemento = obtenerNombreDeRuta(rutaElemento);
        int respuesta = JOptionPane.showConfirmDialog(
                frame,
                "¿Está seguro de que desea eliminar '" + nombreElemento + "' (ruta: " + rutaElemento + ")?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (respuesta == JOptionPane.YES_OPTION) {
            try {
                // Asegurarse de que no estamos intentando eliminar la raíz
                if (rutaElemento.equals("/") || rutaElemento.equals("/root")) {
                    throw new Exception("No se puede eliminar el directorio raíz");
                }

                //! Depuración para ayudar a diagnosticar problemas
                System.out.println("Intentando eliminar: " + rutaElemento);
                
                if (sistema.eliminar(rutaElemento)) {
                    SwingUtilities.invokeLater(() -> {
                        actualizarVistaArbol();
                        actualizarListaArchivos(rutaActual);
                        JOptionPane.showMessageDialog(frame,
                                "Elemento eliminado correctamente",
                                "Éxito",
                                JOptionPane.INFORMATION_MESSAGE);
                    });
                } else {
                    throw new Exception("No se pudo eliminar el elemento. Verifique que exista y no esté en uso.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame,
                        "Error al eliminar: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String obtenerNombreDeRuta(String ruta) {
        if (ruta == null || ruta.isEmpty()) return "";
        // Manejar correctamente la ruta eliminando cualquier slash final
        String rutaLimpia = ruta.endsWith("/") ? ruta.substring(0, ruta.length() - 1) : ruta;
        int ultimoSlash = rutaLimpia.lastIndexOf('/');
        if (ultimoSlash != -1 && ultimoSlash < rutaLimpia.length() - 1) {
            return rutaLimpia.substring(ultimoSlash + 1);
        }
        return rutaLimpia;
    }

    public void mostrar() {
        SwingUtilities.invokeLater(() -> {
            frame.setVisible(true);
            frame.toFront();
        });
    }
}
