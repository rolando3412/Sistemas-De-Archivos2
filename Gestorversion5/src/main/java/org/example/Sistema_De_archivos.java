package org.example;

import java.util.List;
import java.util.ArrayList;

public class Sistema_De_archivos {
    private Arbol_Nario arbol_Nario;
    private Nodo nodo;
    private Indice_De_Archivos indice_De_Archivos;

    public Sistema_De_archivos() {
        arbol_Nario = new Arbol_Nario();
        nodo = new Nodo("", false);
        indice_De_Archivos = new Indice_De_Archivos();
    }

    public boolean crearElemento(String ruta, String nombre, boolean esCarpeta) {
        // Validar nombre
        if (nombre == null || nombre.trim().isEmpty() || nombre.contains("/")) {
            return false;
        }

        // ! Validar ruta
        Nodo padre = arbol_Nario.buscarNodo(arbol_Nario.getRaiz(), ruta);
        if (padre == null || !padre.isEsCarpeta()) {
            return false;
        }

        //! Verificar duplicados
        if (arbol_Nario.existeElemento(ruta, nombre)) {
            return false;
        }

        //!  Crear el elemento
        Nodo nuevoNodo = new Nodo(nombre, esCarpeta);
        nuevoNodo.setPadre(padre);
        String rutaCompleta = ruta.equals("/") ? "/" + nombre : ruta + "/" + nombre;
        nuevoNodo.setRuta(rutaCompleta);

        if (padre.getHijos() == null) {
            padre.setHijos(new ArrayList<>());
        }
        padre.getHijos().add(nuevoNodo);

        //! Actualizar índice para archivos
        if (!esCarpeta) {
            indice_De_Archivos.agregarArchivo(nombre, nuevoNodo);
        }

        return true;
    }

    public void crearCarpeta(String rutaPadre, String nombre) {
        crearElemento(rutaPadre, nombre, true);
    }

    public void crearArchivo(String rutaPadre, String nombre) {
        if (crearElemento(rutaPadre, nombre, false)) {
            // El índice ya se actualiza dentro de crearElemento
        }
    }
     //! metodos requeridos para el sistema de archivos

    public boolean moverElemento(String rutaOrigen, String rutaDestino) {
        // 1. Encontrar nodo origen
        Nodo nodoOrigen = arbol_Nario.buscarNodo(arbol_Nario.getRaiz(), rutaOrigen);

        // 2. Encontrar nodo destino
        Nodo nodoDestino = arbol_Nario.buscarNodo(arbol_Nario.getRaiz(), rutaDestino);

        if (nodoOrigen == null || nodoDestino == null || !nodoDestino.isEsCarpeta()) {
            return false; // Origen no existe, destino no existe, o destino no es carpeta
        }

        //! Si es un archivo, actualizar índice (eliminar referencia anterior)
        if (!nodoOrigen.isEsCarpeta()) {
            indice_De_Archivos.eliminarArchivo(nodoOrigen.getNombre(), nodoOrigen);
        }

        //! Cambiar la ubicación del nodo usando el métdo existente
        arbol_Nario.moverNodo(rutaOrigen, rutaDestino);

        //! 4. Actualizar indice (agregar en nueva ubicación)
        if (!nodoOrigen.isEsCarpeta()) {
            // La nueva ruta debe ser rutaDestino + "/" + nombreArchivo
            String nuevaRuta = rutaDestino + "/" + nodoOrigen.getNombre();
            // Buscar el nodo en su nueva ubicación para asegurar que tenemos la referencia actualizada
            Nodo nodoActualizado = arbol_Nario.buscarNodo(arbol_Nario.getRaiz(), nuevaRuta);
            if (nodoActualizado != null) {
                indice_De_Archivos.agregarArchivo(nodoOrigen.getNombre(), nodoActualizado);
            }
        }

        return true;
    }
   //! Métdo para copiar un elemento
    public boolean copiarElemento(String rutaOrigen, String rutaDestino) {
        Nodo nodoOrigen = arbol_Nario.buscarNodo(arbol_Nario.getRaiz(), rutaOrigen);
        Nodo nodoDestino = arbol_Nario.buscarNodo(arbol_Nario.getRaiz(), rutaDestino);

        if (nodoOrigen == null || nodoDestino == null || !nodoDestino.isEsCarpeta()) {
            return false;
        }

        String nuevaRuta = rutaDestino + "/" + nodoOrigen.getNombre();
        if (arbol_Nario.buscarNodo(arbol_Nario.getRaiz(), nuevaRuta) != null) {
            return false;
        }

        arbol_Nario.copiarNodo(rutaOrigen, rutaDestino);

        //! Actualizar índice para archivos (no carpetas)
        if (!nodoOrigen.isEsCarpeta()) {
            Nodo nuevaCopia = arbol_Nario.buscarNodo(arbol_Nario.getRaiz(), nuevaRuta);
            if (nuevaCopia != null) {
                indice_De_Archivos.agregarArchivo(nodoOrigen.getNombre(), nuevaCopia);
            }
        }

        return true;
    }

    public boolean eliminar(String ruta) {
        if (ruta == null || ruta.trim().isEmpty() || ruta.equals("/")) {
            System.out.println("Error: Intento de eliminar ruta inválida o raíz: " + ruta);
            return false;
        }

        //! Normalizar la ruta para evitar problemas con barras finales
        String rutaNormalizada = ruta;
        while (rutaNormalizada.endsWith("/") && rutaNormalizada.length() > 1) {
            rutaNormalizada = rutaNormalizada.substring(0, rutaNormalizada.length() - 1);
        }
        
        System.out.println("Buscando nodo a eliminar: " + rutaNormalizada);
        Nodo nodoAEliminar = arbol_Nario.buscarNodo(arbol_Nario.getRaiz(), rutaNormalizada);
        
        if (nodoAEliminar == null) {

            List<Nodo> posiblesNodos = buscarPorNombre(obtenerNombreDeRuta(rutaNormalizada));
            if (!posiblesNodos.isEmpty()) {

                nodoAEliminar = posiblesNodos.get(0);
                System.out.println("Usando alternativa - Nodo encontrado por nombre: " + nodoAEliminar.getRuta());
            } else {
                System.out.println("Error: No se encontró el nodo para la ruta: " + rutaNormalizada);
                return false;
            }
        }
        
        if (nodoAEliminar.getPadre() == null) {
            System.out.println("Error: No se puede eliminar un nodo sin padre (raíz)");
            return false;
        }

        try {
            //! Imprimir información útil para depuración
            System.out.println("Eliminando nodo: " + nodoAEliminar.getNombre() + 
                               ", Es carpeta: " + nodoAEliminar.isEsCarpeta() + 
                               ", Ruta: " + nodoAEliminar.getRuta());
            
            //!  Eliminar del índice si es archivo
            if (!nodoAEliminar.isEsCarpeta()) {
                indice_De_Archivos.eliminarArchivo(nodoAEliminar.getNombre(), nodoAEliminar);
            } else {
                //! Eliminar recursivamente contenido de carpeta
                eliminarArchivosRecursivamente(nodoAEliminar);
            }

            // Eliminar físicamente del árbol
            Nodo padre = nodoAEliminar.getPadre();
            if (padre != null && padre.getHijos() != null) {
                boolean eliminado = padre.getHijos().remove(nodoAEliminar);
                System.out.println("Nodo eliminado de la lista de hijos: " + eliminado);
            }
            nodoAEliminar.setPadre(null);
            return true;
        } catch (Exception e) {
            System.out.println("Excepción durante la eliminación: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Método auxiliar para obtener el nombre desde una ruta
    private String obtenerNombreDeRuta(String ruta) {
        if (ruta == null || ruta.isEmpty()) return "";
        String rutaLimpia = ruta.endsWith("/") ? ruta.substring(0, ruta.length() - 1) : ruta;
        int ultimoSlash = rutaLimpia.lastIndexOf('/');
        if (ultimoSlash != -1 && ultimoSlash < rutaLimpia.length() - 1) {
            return rutaLimpia.substring(ultimoSlash + 1);
        }
        return rutaLimpia;
    }

    private void eliminarArchivosRecursivamente(Nodo nodo) {
        if (nodo.getHijos() != null) {
            for (Nodo hijo : new ArrayList<>(nodo.getHijos())) {
                if (!hijo.isEsCarpeta()) {
                    indice_De_Archivos.eliminarArchivo(hijo.getNombre(), hijo);
                } else {
                    eliminarArchivosRecursivamente(hijo);
                }
            }
        }
    }

    public void mover(String rutaOrigen, String rutaDestino) {
        moverElemento(rutaOrigen, rutaDestino);
    }

    public List<String> listarContenido(String ruta) {
        return arbol_Nario.listarContenido(ruta);
    }

    public List<Nodo> buscarPorNombre(String nombre) {
        //! Primero obtener los archivos del índice
        List<Nodo> resultados = new ArrayList<>(indice_De_Archivos.buscarArchivo(nombre));

        // Luego buscar carpetas con ese nombre exacto
        List<Nodo> carpetas = new ArrayList<>();
        buscarCarpetasPorNombre(arbol_Nario.getRaiz(), nombre, carpetas);

        // Combinar resultados
        resultados.addAll(carpetas);
        return resultados;
    }

    private void buscarCarpetasPorNombre(Nodo nodo, String nombre, List<Nodo> resultados) {
        //! Si es carpeta y tiene el nombre exacto, añadir a resultados
        if (nodo.isEsCarpeta() && nodo.getNombre().equals(nombre)) {
            resultados.add(nodo);
        }

        //! Buscar recursivamente en los hijos
        if (nodo.getHijos() != null) {
            for (Nodo hijo : nodo.getHijos()) {
                buscarCarpetasPorNombre(hijo, nombre, resultados);
            }
        }
    }

    public Arbol_Nario getArbol_Nario() {
        return arbol_Nario;
    }

    public List<Nodo> buscarPorCoincidenciaParcial(String fragmento) {
        // Buscar elementos que contengan el fragmento en su nombre
        List<Nodo> resultados = new ArrayList<>();
        // Comenzar la búsqueda desde la raíz
        buscarCoincidenciasParciales(arbol_Nario.getRaiz(), fragmento.toLowerCase(), resultados);
        return resultados;
    }

    private void buscarCoincidenciasParciales(Nodo nodo, String fragmento, List<Nodo> resultados) {
        //! Comprobar si el nombre del nodo actual contiene el fragmento
        if (nodo.getNombre().toLowerCase().contains(fragmento)) {
            resultados.add(nodo);
        }

        //! Buscar recursivamente en los hijos
        if (nodo.getHijos() != null) {
            for (Nodo hijo : nodo.getHijos()) {
                buscarCoincidenciasParciales(hijo, fragmento, resultados);
            }
        }
    }
}
