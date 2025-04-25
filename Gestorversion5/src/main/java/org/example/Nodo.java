package org.example;

import java.util.ArrayList;
import java.util.List;

public class Nodo {
    private String nombre;
    private boolean esCarpeta;
    private List<Nodo> hijos;
    private Nodo padre;
    private String ruta;

    //! constructor para la carpeta
    public Nodo(String nombre, boolean esCarpeta) {
        this.nombre = nombre;
        this.esCarpeta = esCarpeta;
        if (esCarpeta) {
            this.hijos = new ArrayList<Nodo>();
        }
    }

    //! constructor para el archivo
    public Nodo(String nombre, boolean esCarpeta, String ruta) {
        this.nombre = nombre;
        this.esCarpeta = esCarpeta;
        this.ruta = ruta;
        if (esCarpeta) {
            this.hijos = new ArrayList<Nodo>();
        }
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isEsCarpeta() {
        return esCarpeta;
    }

    public List<Nodo> getHijos() {
        return hijos;
    }

    public void setHijos(List<Nodo> hijos) {
        this.hijos = hijos;
    }

    public Nodo getPadre() {
        return padre;
    }

    public void setPadre(Nodo padre) {
        this.padre = padre;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    //!También podría ser útil agregar un métdo toString para facilitar la depuración
    @Override
    public String toString() {
        return "Nodo{" +
                "nombre='" + nombre + '\'' +
                ", esCarpeta=" + esCarpeta +
                ", ruta='" + ruta + '\'' +
                '}';
    }
}

