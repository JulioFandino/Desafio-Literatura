package com.aluracursos.desafio.principal;

import com.aluracursos.desafio.model.*;
import com.aluracursos.desafio.repository.AutorRepository;
import com.aluracursos.desafio.repository.LibroRepository;
import com.aluracursos.desafio.service.ConsumoAPI;
import com.aluracursos.desafio.service.ConvierteDatos;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private static final String URL_BASE = "https://gutendex.com/books/";
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private Scanner teclado = new Scanner(System.in);
    private LibroRepository libroRepositorio;
    private AutorRepository autorRepositorio;
    private List<Libro> librosGuardados = new ArrayList<>();
    private List<Autor> autoresGuardados = new ArrayList<>();
    private List<Autor> autoresPorAño = new ArrayList<>();
    private Datos datos;

    public Principal(AutorRepository autorRepository, LibroRepository libroRepository) {
        this.libroRepositorio = libroRepository;
        this.autorRepositorio = autorRepository;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {

            var menu = """
                    1- Buscar y guardar libro con autor
                    2- Buscar autor por nombre
                    3- Listar Autores registrados
                    4- Listar Libros registrados
                    5- Buscar por lapso de años
                    6- Listar por idiomas
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarLibroYGuardar();
                    break;
                case 2:
                    buscarAutor();
                    break;
                case 3:
                    mostrarAutoresBuscados();
                    break;
                case 4:
                    mostrarLibrosBuscados();
                    break;
                case 5:
                    buscarAutoresPorAño();
                    break;
                case 6:
                    buscarPorIdioma();
                    break;
            }
        }
    }

    private Libro crearLibro(DatosLibros datosLibros, Autor autor) {
        Libro libro = new Libro(datosLibros, autor);
        return libroRepositorio.save(libro);
    }

    private Datos getLibro () {
        System.out.println("Ingrese el nombre del libro que desea buscar");
        var tituloLibro = teclado.nextLine();
        var json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + tituloLibro.replace(" ", "+"));
        datos = conversor.obtenerDatos(json, Datos.class);
        return datos;
    }
    private Datos getAutor(){
        System.out.println("¿Que Autor desea buscar?");
        var nombreAutor = teclado.nextLine();
        var json = consumoAPI.obtenerDatos(URL_BASE+"?search=" + nombreAutor.replace(" ", "+"));
        datos = conversor.obtenerDatos(json, Datos.class);
        return datos;
    }

    private void buscarLibroYGuardar () {
        Datos datos = getLibro();
        if (!datos.resultados().isEmpty()) {
            DatosLibros datosLibros = datos.resultados().get(0);
            DatosAutor datosAutor = datosLibros.autor().get(0);
            Libro libro1 = null;
            Libro libroDb = libroRepositorio.findByTitulo(datosLibros.titulo());
            if (libroDb != null) {
                System.out.println("El libro está en nuestra base de datos");
                System.out.println(libroDb);
            } else {
                Autor autorDb = autorRepositorio.findByNombreIgnoreCase(datosLibros.autor().get(0).nombre());
                if (autorDb == null) {
                    Autor autor1 = new Autor(datosAutor);
                    autor1 = autorRepositorio.save(autor1);
                    libro1 = crearLibro(datosLibros, autor1);
                    System.out.println("Ni el libro, ni el autor estan en nuestra base de datos, los agregaremos mediante Gutendex");
                    System.out.println(libro1);
                } else {
                    libro1 = crearLibro(datosLibros, autorDb);
                    System.out.println("El autor está en nuestra base de datos, pero el libro no, lo agregaremos mediante Gutendex");
                    System.out.println(libro1);
                }
            }
        } else {
            System.out.println("""
        El Libro no esta en la base de datos ni en Gutendex, lamentamos la molestia      
                                """);
        }
    }

    private void buscarAutor() {
        System.out.println("Escribe el nomnbre del autor que buscas");
        var nombreAutor = teclado.nextLine();
        autoresGuardados = autorRepositorio.findAll();
        autoresGuardados.stream()
                .filter(autor -> autor.getNombre().contains(nombreAutor))
                .collect(Collectors.toList())
                .forEach(System.out::println);
    }

    private void mostrarAutoresBuscados() {
        autoresGuardados = autorRepositorio.findAll();
        autoresGuardados.stream()
                .sorted(Comparator.comparing(Autor::getNombre))
                .forEach(System.out::println);
    }

    private void mostrarLibrosBuscados() {
        librosGuardados = libroRepositorio.findAll();
        librosGuardados.stream()
                .sorted(Comparator.comparing(Libro::getTitulo))
                .forEach(System.out::println);
    }

    private void buscarPorIdioma(){
        System.out.println("Escribe el nomnbre del autor que buscas");
        var idioma = teclado.nextLine();
        librosGuardados = libroRepositorio.findAll();
        librosGuardados.stream()
                .filter(libro -> libro.getIdiomas().contains(idioma))
                .collect(Collectors.toList())
                .forEach(System.out::println);
    }

    private void buscarAutoresPorAño() {
        System.out.println("¿Entre que años quiere buscar autores?");
        System.out.println("Año de inicio:");
        int año1 = teclado.nextInt();
        System.out.println("Año de fin:");
        int año2 = teclado.nextInt();
        autoresPorAño = autorRepositorio.findAll();
        var autoresAños = autoresPorAño.stream()
                .filter(autor -> autor.getFechaDeNacimiento() <= año2 && autor.getFechaDeMuerte() >= año1)
                .collect(Collectors.toList());
        if (autoresAños.isEmpty()) {
            System.out.println("No hay autores regisrados en esos años");
        } else {
            for (Autor autor : autoresAños) {
                System.out.println(autor);
            }

        }
    }

}


