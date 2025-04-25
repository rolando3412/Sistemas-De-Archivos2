package org.example;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class Indice_De_Archivos {
    //!indice de busqueda

    private Map<String , List<Nodo>> indice;

    public Indice_De_Archivos() {
        indice = new HashMap<>();
    }
    //!metodos para agregar , buscar , eliminar archivos
    public void agregarArchivo(String nombre, Nodo nodo) {
        if (!indice.containsKey(nombre)) {
            indice.put(nombre, new ArrayList<>());
        }
        indice.get(nombre).add(nodo);
    }

    public List<Nodo> buscarArchivo(String nombre) {
        return indice.getOrDefault(nombre, new ArrayList<>());
    }

    public void eliminarArchivo(String nombre, Nodo nodo) {
        if (indice.containsKey(nombre)) {
            indice.get(nombre).remove(nodo);
            if (indice.get(nombre).isEmpty()) {
                indice.remove(nombre);
            }
        }
    }
//!metodo para limpiar el indice
    public void limpiarIndice() {
        indice.clear();
    }
}

