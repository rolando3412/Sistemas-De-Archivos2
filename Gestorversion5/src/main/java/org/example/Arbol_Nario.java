package org.example;

import java.util.ArrayList;
import java.util.List;

public class Arbol_Nario {
    private Nodo raiz;


    public Arbol_Nario() {
        //!la raiz sera la carpeta
        raiz = new Nodo("/", true);
        raiz.setRuta("/");
    }
    //!metodos para insertar , eliminar , mover nodos etc

    public void insertarNodo(String rutaPadre, String nombre, boolean esCarpeta) {
        Nodo padre = buscarNodo(raiz, rutaPadre);
        if (padre != null && padre.isEsCarpeta() && !existeElemento(rutaPadre, nombre)) {
            Nodo nuevoNodo = new Nodo(nombre, esCarpeta);
            nuevoNodo.setPadre(padre);
            nuevoNodo.setRuta(rutaPadre + "/" + nombre);
            if (padre.getHijos() == null) {
                padre.setHijos(new ArrayList<>());
            }
            padre.getHijos().add(nuevoNodo);
        }
    }

    public void eliminarNodo(String ruta) {
        Nodo nodo = buscarNodo(raiz, ruta);
        if (nodo != null && nodo.getPadre() != null) {
            nodo.getPadre().getHijos().remove(nodo);
        }
    }

    public void moverNodo(String rutaOrigen, String rutaDestino) {
        Nodo nodo = buscarNodo(raiz, rutaOrigen);
        Nodo destino = buscarNodo(raiz, rutaDestino);
        if (nodo != null && destino != null && destino.isEsCarpeta() && !existeElemento(rutaDestino, nodo.getNombre())) {
            nodo.getPadre().getHijos().remove(nodo);
            nodo.setPadre(destino);
            if (destino.getHijos() == null) {
                destino.setHijos(new ArrayList<>());
            }
            destino.getHijos().add(nodo);
            actualizarRutas(nodo, destino.getRuta());
        }
    }

    private void actualizarRutas(Nodo nodo, String nuevaRutaPadre) {
        nodo.setRuta(nuevaRutaPadre + "/" + nodo.getNombre());
        if (nodo.getHijos() != null) {
            for (Nodo hijo : nodo.getHijos()) {
                actualizarRutas(hijo, nodo.getRuta());
            }
        }
    }

    Nodo buscarNodo(Nodo actual, String ruta) {
        //! buscando la ruta eliminando la barra final si existe
        String rutaNormalizada = ruta;
        while (rutaNormalizada.endsWith("/") && rutaNormalizada.length() > 1) {
            rutaNormalizada = rutaNormalizada.substring(0, rutaNormalizada.length() - 1);
        }
        
        //! Comparar con la ruta normalizada
        if (actual.getRuta().equals(rutaNormalizada)) {
            return actual;
        }
        
        //! Búsqueda en los hijos
        if (actual.getHijos() != null) {
            for (Nodo hijo : actual.getHijos()) {
                Nodo encontrado = buscarNodo(hijo, rutaNormalizada);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }
        return null;
    }

    public List<String> listarContenido(String ruta) {
        List<String> contenido = new ArrayList<>();
        Nodo carpeta = buscarNodo(raiz, ruta);
        if (carpeta != null && carpeta.getHijos() != null) {
            for (Nodo hijo : carpeta.getHijos()) {
                contenido.add(hijo.getNombre() + (hijo.isEsCarpeta() ? "/" : ""));
            }
        }
        return contenido;
    }

    public Nodo getRaiz() {
        return raiz;
    }

    public void copiarNodo(String rutaOrigen, String rutaDestino) {
        Nodo origen = buscarNodo(raiz, rutaOrigen);
        Nodo destino = buscarNodo(raiz, rutaDestino);

        if (origen != null && destino != null && destino.isEsCarpeta()) {
            Nodo copia = copiarNodoRecursivo(origen);
            copia.setPadre(destino);
            if (destino.getHijos() == null) {
                destino.setHijos(new ArrayList<>());
            }
            destino.getHijos().add(copia);
            actualizarRutas(copia, destino.getRuta());
        }
    }

    private Nodo copiarNodoRecursivo(Nodo original) {
        Nodo copia = new Nodo(original.getNombre(), original.isEsCarpeta());
        if (original.isEsCarpeta() && original.getHijos() != null) {
            copia.setHijos(new ArrayList<>());
            for (Nodo hijo : original.getHijos()) {
                Nodo copiaHijo = copiarNodoRecursivo(hijo);
                copiaHijo.setPadre(copia);
                copia.getHijos().add(copiaHijo);
            }
        }
        return copia;
    }

    public boolean existeElemento(String rutaPadre, String nombre) {
        Nodo padre = buscarNodo(raiz, rutaPadre);
        if (padre != null && padre.getHijos() != null) {
            return padre.getHijos().stream()
                    .anyMatch(hijo -> hijo.getNombre().equals(nombre));
        }
        return false;
    }

    public void mostrarEstructura() {
        //! Recorrer árbol e imprimir con formato de árbol
        System.out.println("Estructura del sistema de archivos:");
        imprimirEstructura(getRaiz(), 0);
    }

    private void imprimirEstructura(Nodo nodo, int nivel) {
        // Crear la sangría según el nivel
        StringBuilder sangria = new StringBuilder();
        for (int i = 0; i < nivel; i++) {
            sangria.append("  │ ");
        }

        //! Para los niveles mayores a 0, agregar el conector adecuado
        String prefijo = nivel > 0 ? sangria.toString() + "  ├─ " : "";

        //! Imprimir el nodo actual con formato adecuado según su tipo
        String marcadorTipo = nodo.isEsCarpeta() ? "📁 " : "📄 ";
        System.out.println(prefijo + marcadorTipo + nodo.getNombre());

        //! Si es una carpeta, imprimir sus hijos
        if (nodo.isEsCarpeta() && nodo.getHijos() != null) {
            for (Nodo hijo : nodo.getHijos()) {
                imprimirEstructura(hijo, nivel + 1);
            }
        }
    }
}

