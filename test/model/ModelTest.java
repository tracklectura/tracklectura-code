package model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Modelos de Datos")
class ModelTest {

    // ─────────────────────────────────────────────────────────────
    // Sesion
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Sesion")
    class SesionTest {

        private Sesion crearSesion() {
            return new Sesion(
                    1,                    // id
                    "uuid-abc-123",       // uuid
                    42,                   // libroId
                    "17/03/2026 14:30",   // fecha
                    "Capítulo 5",         // capitulo
                    100,                  // paginaInicio
                    150,                  // paginaFin
                    50,                   // paginasLeidas
                    30.0,                 // minutos
                    1.667,                // ppm
                    100.0                 // pph
            );
        }

        @Test
        @DisplayName("getId() devuelve el id correcto")
        void getId() {
            assertEquals(1, crearSesion().getId());
        }

        @Test
        @DisplayName("getUuid() devuelve el uuid correcto")
        void getUuid() {
            assertEquals("uuid-abc-123", crearSesion().getUuid());
        }

        @Test
        @DisplayName("getLibroId() devuelve el libroId correcto")
        void getLibroId() {
            assertEquals(42, crearSesion().getLibroId());
        }

        @Test
        @DisplayName("getFecha() devuelve la fecha correcta")
        void getFecha() {
            assertEquals("17/03/2026 14:30", crearSesion().getFecha());
        }

        @Test
        @DisplayName("getCapitulo() devuelve el capítulo correcto")
        void getCapitulo() {
            assertEquals("Capítulo 5", crearSesion().getCapitulo());
        }

        @Test
        @DisplayName("getPaginaInicio() y getPaginaFin() correctos")
        void paginas() {
            Sesion s = crearSesion();
            assertEquals(100, s.getPaginaInicio());
            assertEquals(150, s.getPaginaFin());
        }

        @Test
        @DisplayName("getPaginasLeidas() = paginaFin - paginaInicio")
        void paginasLeidas() {
            Sesion s = crearSesion();
            assertEquals(50, s.getPaginasLeidas());
        }

        @Test
        @DisplayName("getMinutos() correcto")
        void minutos() {
            assertEquals(30.0, crearSesion().getMinutos(), 0.001);
        }

        @Test
        @DisplayName("getPpm() correcto")
        void ppm() {
            assertEquals(1.667, crearSesion().getPpm(), 0.001);
        }

        @Test
        @DisplayName("getPph() correcto")
        void pph() {
            assertEquals(100.0, crearSesion().getPph(), 0.001);
        }

        @Test
        @DisplayName("Sesión con uuid null no lanza excepción")
        void uuidNull() {
            assertDoesNotThrow(() -> new Sesion(2, null, 1, "17/03/2026", null, 0, 0, 0, 0.0, 0.0, 0.0));
        }

        @Test
        @DisplayName("Sesión con valores mínimos (todo cero)")
        void todoCero() {
            Sesion s = new Sesion(0, "", 0, "", "", 0, 0, 0, 0.0, 0.0, 0.0);
            assertEquals(0, s.getId());
            assertEquals(0, s.getPaginasLeidas());
        }

        @Test
        @DisplayName("Coherencia: ppm * 60 ≈ pph (para valores reales)")
        void coherenciaPpmPph() {
            Sesion s = crearSesion();
            // En esta sesión ppm=1.667, pph=100 → 1.667*60 ≈ 100
            assertEquals(s.getPph(), s.getPpm() * 60, 1.0);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Libro
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Libro")
    class LibroTest {

        private Libro crearLibro() {
            return new Libro(
                    7,
                    "El Señor de los Anillos",
                    1178,
                    "https://covers.openlibrary.org/b/id/8743437-L.jpg",
                    "Leyendo"
            );
        }

        @Test
        @DisplayName("getId() correcto")
        void getId() {
            assertEquals(7, crearLibro().getId());
        }

        @Test
        @DisplayName("getNombre() correcto")
        void getNombre() {
            assertEquals("El Señor de los Anillos", crearLibro().getNombre());
        }

        @Test
        @DisplayName("getPaginasTotales() correcto")
        void getPaginasTotales() {
            assertEquals(1178, crearLibro().getPaginasTotales());
        }

        @Test
        @DisplayName("getCoverUrl() correcto")
        void getCoverUrl() {
            assertNotNull(crearLibro().getCoverUrl());
            assertTrue(crearLibro().getCoverUrl().startsWith("https://"));
        }

        @Test
        @DisplayName("getEstado() correcto")
        void getEstado() {
            assertEquals("Leyendo", crearLibro().getEstado());
        }

        @Test
        @DisplayName("Libro sin portada (cover_url null) no lanza excepción")
        void sinPortada() {
            Libro libro = new Libro(1, "Sin portada", 200, null, "Pendiente");
            assertNull(libro.getCoverUrl());
        }

        @Test
        @DisplayName("Libro con 0 páginas totales es válido como modelo")
        void sinPaginas() {
            Libro libro = new Libro(1, "Desconocido", 0, null, "Leyendo");
            assertEquals(0, libro.getPaginasTotales());
        }

        @Test
        @DisplayName("Estados válidos aceptados como modelo")
        void estadosValidos() {
            String[] estados = {"Leyendo", "Terminado", "Pendiente", "Abandonado"};
            for (String estado : estados) {
                Libro l = new Libro(1, "Libro", 100, null, estado);
                assertEquals(estado, l.getEstado());
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Integración: Sesion + ReadingCalculator
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Integración Sesion + ReadingCalculator")
    class IntegracionSesionCalculator {

        @Test
        @DisplayName("PPM calculado == PPM almacenado en Sesion (con margen)")
        void ppmCalculadoVsAlmacenado() {
            int paginas = 60;
            double minutos = 30.0;
            double ppmCalculado = utils.ReadingCalculator.calcularPPM(paginas, minutos);
            double pphCalculado = utils.ReadingCalculator.calcularPPH(ppmCalculado);

            Sesion s = new Sesion(1, "u", 1, "17/03/2026", "Cap1",
                    10, 70, paginas, minutos, ppmCalculado, pphCalculado);

            assertEquals(ppmCalculado, s.getPpm(), 0.001);
            assertEquals(pphCalculado, s.getPph(), 0.001);
        }

        @Test
        @DisplayName("Porcentaje de progreso con datos de Libro y Sesion")
        void porcentajeProgreso() {
            Libro libro = new Libro(1, "Dune", 412, null, "Leyendo");
            Sesion sesion = new Sesion(1, "u", 1, "17/03/2026", "Cap3",
                    0, 206, 206, 120.0, 1.717, 103.0);

            double porcentaje = utils.ReadingCalculator.calcularPorcentaje(
                    sesion.getPaginaFin(), libro.getPaginasTotales());

            assertEquals(50.0, porcentaje, 0.1);
        }
    }
}