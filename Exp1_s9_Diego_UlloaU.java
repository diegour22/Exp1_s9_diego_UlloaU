package Exp1_s9_Diego_UlloaU;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Aplicación única de venta de entradas para Teatro Moro,
 * con entrada independiente si es con mayuscula/minuscula.
 */
public class Exp1_s9_Diego_UlloaU {
    private static final Scanner sc = new Scanner(System.in);
    private static final List<Asiento> asientos = new ArrayList<>();
    private static final List<Venta> ventas = new ArrayList<>();

    public static void main(String[] args) {
        inicializarAsientos();
        menu();
        sc.close();
    }

    private static void inicializarAsientos() {
        String[] secciones = {"VIP","Palco","PlateaBaja","PlateaAlta","Galeria"};
        for (String sec : secciones) {
            for (int i = 1; i <= 5; i++) {
                asientos.add(new Asiento(sec.charAt(0) + "" + i, sec));
            }
        }
    }

    private static void menu() {
        int opcion;
        do {
            System.out.println("\n--- Teatro Moro ---");
            System.out.println("1. Vender entrada");
            System.out.println("2. Mostrar resumen de ventas");
            System.out.println("3. Generar boleta");
            System.out.println("4. Reasignar asiento");
            System.out.println("5. Eliminar venta");
            System.out.println("6. Salir");
            opcion = leerEntero("Opción: ");
            switch (opcion) {
                case 1 -> vender();
                case 2 -> mostrarResumen();
                case 3 -> generarBoleta();
                case 4 -> reasignar();
                case 5 -> eliminar();
                case 6 -> System.out.println("Saliendo...");
                default -> System.out.println("Opción inválida.");
            }
        } while (opcion != 6);
    }

    private static void vender() {
        String nombre  = leerTexto("Nombre: ");
        int    edad    = leerEntero("Edad: ");
        String genero  = leerGenero("Género (M/F): ");
        boolean est    = leerBoolean("¿Estudiante? (s/n): ");

        String sec = validarSeccion();
        List<String> libres = asientosLibresDeSeccion(sec);
        if (libres.isEmpty()) {
            System.out.println("No hay asientos libres en " + sec);
            return;
        }

        String cod = validarAsientoEnSeccion(sec, libres);
        Asiento asiento = buscarAsiento(sec, cod);
        asiento.ocupar();

        Venta v = new Venta(ventas.size() + 1, nombre, edad, genero, est, asiento);
        ventas.add(v);
        System.out.println("Venta registrada con ID: " + v.getId());
    }

    /* ————— Lectura y validación genérica ————— */

    private static String leerTexto(String msg) {
        System.out.print(msg);
        return sc.nextLine().trim();
    }

    private static int leerEntero(String msg) {
        while (true) {
            System.out.print(msg);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Debe ingresar un número válido.");
            }
        }
    }

    private static String leerGenero(String msg) {
        String g;
        do {
            System.out.print(msg);
            g = sc.nextLine().trim().toUpperCase();
        } while (!g.equals("M") && !g.equals("F"));
        return g;
    }

    private static boolean leerBoolean(String msg) {
        String r;
        do {
            System.out.print(msg);
            r = sc.nextLine().trim().toLowerCase();
        } while (!r.equals("s") && !r.equals("n"));
        return r.equals("s");
    }

    /*  Validaciones específicas de negocio  */

    private static String validarSeccion() {
        String[] opciones = {"VIP","Palco","PlateaBaja","PlateaAlta","Galeria"};
        String sec;
        do {
            System.out.print("Sección " + Arrays.toString(opciones) + ": ");
            sec = sc.nextLine().trim();
            String finalSec = sec;
            if (!Arrays.stream(opciones).anyMatch(s -> s.equalsIgnoreCase(finalSec))) {
                System.out.println("Sección inválida.");
            } else {
                // devolvemos siempre la sección con mayúscula inicial tal como está en el array
                String finalSec1 = sec;
                return Arrays.stream(opciones)
                        .filter(s -> s.equalsIgnoreCase(finalSec1))
                        .findFirst()
                        .orElse(sec);
            }
        } while (true);
    }

    private static List<String> asientosLibresDeSeccion(String sec) {
        List<String> libres = asientos.stream()
                .filter(a -> a.getSeccion().equalsIgnoreCase(sec) && !a.isOcupado())
                .map(Asiento::getCodigo)
                .collect(Collectors.toList());
        System.out.println("Asientos libres en " + sec + ": " + libres);
        return libres;
    }

    private static String validarAsientoEnSeccion(String sec, List<String> libres) {
        String cod;
        do {
            System.out.print("Código de asiento: ");
            cod = sc.nextLine().trim();
            // comparamos insensible a mayúsculas
            String finalCod1 = cod;
            boolean existe = libres.stream().anyMatch(l -> l.equalsIgnoreCase(finalCod1));
            if (!existe) {
                System.out.println("Asiento inválido o ya ocupado.");
            } else {
                // retorna el código tal cual figura en la lista (mayúscula+numero)
                String finalCod = cod;
                return libres.stream().filter(l -> l.equalsIgnoreCase(finalCod)).findFirst().get();
            }
        } while (true);
    }

    private static Asiento buscarAsiento(String sec, String cod) {
        return asientos.stream()
                .filter(a -> a.getSeccion().equalsIgnoreCase(sec)
                        && a.getCodigo().equalsIgnoreCase(cod)
                        && !a.isOcupado())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Asiento no encontrado"));
    }

    /*  Resto de funcionalidades  */

    private static void mostrarResumen() {
        if (ventas.isEmpty()) {
            System.out.println("No hay ventas registradas.");
            return;
        }
        System.out.println("\n--- Resumen de Ventas ---");
        System.out.printf("%-4s %-15s %-8s %10s%n", "ID", "Cliente", "Asiento", "Precio");
        for (Venta v : ventas) {
            System.out.printf("%-4d %-15s %-8s $%10.2f%n",
                    v.getId(), v.getNombre(), v.getAsiento().getCodigo(), v.getPrecioFinal());
        }
    }

    private static void generarBoleta() {
        int id = leerEntero("ID venta para boleta: ");
        Venta v = ventas.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
        if (v == null) {
            System.out.println("Venta no encontrada.");
            return;
        }
        v.imprimirBoleta();
    }

    private static void reasignar() {
        int id = leerEntero("ID venta a reasignar: ");
        Venta v = ventas.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
        if (v == null) {
            System.out.println("Venta no encontrada."); return;
        }
        List<String> libres = asientosLibresDeSeccion(v.getAsiento().getSeccion());
        String cod = validarAsientoEnSeccion(v.getAsiento().getSeccion(), libres);
        Asiento nuevo = buscarAsiento(v.getAsiento().getSeccion(), cod);
        v.reasignarAsiento(nuevo);
        System.out.println("Asiento reasignado.");
    }

    private static void eliminar() {
        int id = leerEntero("ID venta a eliminar: ");
        Iterator<Venta> it = ventas.iterator();
        while (it.hasNext()) {
            Venta v = it.next();
            if (v.getId() == id) {
                v.getAsiento().liberar();
                it.remove();
                System.out.println("Venta eliminada.");
                return;
            }
        }
        System.out.println("Venta no encontrada.");
    }

    // ─── Clases internas ─────────────────────────────────────────────────────────

    /** Representa un asiento del teatro. */
    static class Asiento {
        private final String codigo;
        private final String seccion;
        private boolean ocupado;

        public Asiento(String codigo, String seccion) {
            this.codigo = codigo;
            this.seccion = seccion;
            this.ocupado = false;
        }
        public String getCodigo() { return codigo; }
        public String getSeccion() { return seccion; }
        public boolean isOcupado() { return ocupado; }
        public void ocupar() { ocupado = true; }
        public void liberar() { ocupado = false; }
    }

    /** Representa una venta con cálculo de descuento y boleta. */
    static class Venta {
        private final int id;
        private final String nombre;
        private final int edad;
        private final String genero;
        private final boolean estudiante;
        private Asiento asiento;
        private double descuento, precioFinal;

        public Venta(int id, String nombre, int edad, String genero, boolean estudiante, Asiento asiento) {
            this.id = id;
            this.nombre = nombre;
            this.edad = edad;
            this.genero = genero;
            this.estudiante = estudiante;
            this.asiento = asiento;
            aplicarDescuento();
        }

        private void aplicarDescuento() {
            double base = calcularPrecioBase();
            if      (edad < 12)                  descuento = 0.10 * base;
            else if ("F".equalsIgnoreCase(genero)) descuento = 0.20 * base;
            else if (estudiante)                 descuento = 0.15 * base;
            else if (edad >= 60)                 descuento = 0.25 * base;
            else                                 descuento = 0.0;
            precioFinal = base - descuento;
        }

        private double calcularPrecioBase() {
            return switch (asiento.getSeccion().toLowerCase()) {
                case "vip"        -> 50000.0;
                case "palco"      -> 40000.0;
                case "plateabaja" -> 30000.0;
                case "plateaalta" -> 25000.0;
                case "galeria"    -> 15000.0;
                default           -> 20000.0;
            };
        }

        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public Asiento getAsiento() { return asiento; }
        public double getPrecioFinal() { return precioFinal; }

        public void reasignarAsiento(Asiento nuevo) {
            this.asiento.liberar();
            nuevo.ocupar();
            this.asiento = nuevo;
            aplicarDescuento();
        }

        public void imprimirBoleta() {
            System.out.println("\n=== BOLETA DE VENTA ===");
            System.out.printf("Venta Nº: %d   Cliente: %s   Edad: %d   Género: %s   Estudiante: %s%n",
                    id, nombre, edad, genero, (estudiante ? "Sí" : "No"));
            System.out.printf("Asiento: %s (%s)%n", asiento.getCodigo(), asiento.getSeccion());
            System.out.printf("Descuento: $%.2f   Precio final: $%.2f%n", descuento, precioFinal);
        }
    }
}
