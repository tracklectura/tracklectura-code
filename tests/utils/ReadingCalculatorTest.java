package utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReadingCalculator")
class ReadingCalculatorTest {

    // ─────────────────────────────────────────────────────────────
    // calcularPaginasLeidas
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("calcularPaginasLeidas()")
    class PaginasLeidas {

        @Test
        @DisplayName("Caso normal: fin > inicio")
        void casoNormal() {
            assertEquals(50, ReadingCalculator.calcularPaginasLeidas(10, 60));
        }

        @Test
        @DisplayName("Una sola página leída")
        void unaPagina() {
            assertEquals(1, ReadingCalculator.calcularPaginasLeidas(5, 6));
        }

        @Test
        @DisplayName("Inicio igual a fin → 0 páginas")
        void inicioIgualFin() {
            assertEquals(0, ReadingCalculator.calcularPaginasLeidas(20, 20));
        }

        @Test
        @DisplayName("Fin menor que inicio → 0 (no negativo)")
        void finMenorQueInicio() {
            assertEquals(0, ReadingCalculator.calcularPaginasLeidas(50, 10));
        }

        @Test
        @DisplayName("Valores desde cero")
        void desdeZero() {
            assertEquals(100, ReadingCalculator.calcularPaginasLeidas(0, 100));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // calcularPPM
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("calcularPPM()")
    class PPM {

        @Test
        @DisplayName("Cálculo normal: 30 páginas en 10 minutos = 3.0 PPM")
        void casoNormal() {
            assertEquals(3.0, ReadingCalculator.calcularPPM(30, 10), 0.001);
        }

        @Test
        @DisplayName("Resultado fraccionario")
        void fraccionario() {
            assertEquals(2.5, ReadingCalculator.calcularPPM(5, 2), 0.001);
        }

        @Test
        @DisplayName("Páginas = 0 → 0.0")
        void paginasCero() {
            assertEquals(0.0, ReadingCalculator.calcularPPM(0, 10));
        }

        @Test
        @DisplayName("Minutos = 0 → 0.0 (no divide por cero)")
        void minutosCero() {
            assertEquals(0.0, ReadingCalculator.calcularPPM(10, 0));
        }

        @Test
        @DisplayName("Páginas negativas → 0.0")
        void paginasNegativas() {
            assertEquals(0.0, ReadingCalculator.calcularPPM(-5, 10));
        }

        @Test
        @DisplayName("Minutos negativos → 0.0")
        void minutosNegativos() {
            assertEquals(0.0, ReadingCalculator.calcularPPM(10, -5));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // calcularPPH
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("calcularPPH()")
    class PPH {

        @Test
        @DisplayName("2 PPM → 120 PPH")
        void conversion() {
            assertEquals(120.0, ReadingCalculator.calcularPPH(2.0), 0.001);
        }

        @Test
        @DisplayName("0 PPM → 0 PPH")
        void cero() {
            assertEquals(0.0, ReadingCalculator.calcularPPH(0.0));
        }

        @Test
        @DisplayName("Encadenado con calcularPPM: 60 págs / 30 min = 2 PPM → 120 PPH")
        void encadenado() {
            double ppm = ReadingCalculator.calcularPPM(60, 30);
            assertEquals(120.0, ReadingCalculator.calcularPPH(ppm), 0.001);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // estimarTiempoRestante
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("estimarTiempoRestante()")
    class TiempoRestante {

        @Test
        @DisplayName("0 páginas restantes → libro terminado")
        void libroTerminado() {
            assertEquals("¡Libro terminado!", ReadingCalculator.estimarTiempoRestante(0, 200));
        }

        @Test
        @DisplayName("Páginas negativas → libro terminado")
        void paginasNegativas() {
            assertEquals("¡Libro terminado!", ReadingCalculator.estimarTiempoRestante(-5, 200));
        }

        @Test
        @DisplayName("PPH = 0 → null (sin datos suficientes)")
        void sinDatos() {
            assertNull(ReadingCalculator.estimarTiempoRestante(100, 0));
        }

        @Test
        @DisplayName("PPH negativo → null")
        void pphNegativo() {
            assertNull(ReadingCalculator.estimarTiempoRestante(100, -10));
        }

        @Test
        @DisplayName("200 páginas a 100 PPH = 2h 0m")
        void dosHoras() {
            assertEquals("2h 0m", ReadingCalculator.estimarTiempoRestante(200, 100));
        }

        @Test
        @DisplayName("50 páginas a 100 PPH = 30m")
        void soloMinutos() {
            assertEquals("30m", ReadingCalculator.estimarTiempoRestante(50, 100));
        }

        @Test
        @DisplayName("150 páginas a 100 PPH = 1h 30m")
        void horasYMinutos() {
            assertEquals("1h 30m", ReadingCalculator.estimarTiempoRestante(150, 100));
        }

        @Test
        @DisplayName("Velocidad alta: 1 página a 120 PPH = 1m")
        void velocidadAlta() {
            assertEquals("1m", ReadingCalculator.estimarTiempoRestante(1, 120));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // calcularPorcentaje
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("calcularPorcentaje()")
    class Porcentaje {

        @Test
        @DisplayName("Mitad exacta → 50.0")
        void mitad() {
            assertEquals(50.0, ReadingCalculator.calcularPorcentaje(100, 200), 0.001);
        }

        @Test
        @DisplayName("Inicio del libro → 0.0")
        void inicio() {
            assertEquals(0.0, ReadingCalculator.calcularPorcentaje(0, 300), 0.001);
        }

        @Test
        @DisplayName("Libro completo → 100.0")
        void completo() {
            assertEquals(100.0, ReadingCalculator.calcularPorcentaje(300, 300), 0.001);
        }

        @Test
        @DisplayName("Superar total → máximo 100.0 (no > 100)")
        void superarTotal() {
            assertEquals(100.0, ReadingCalculator.calcularPorcentaje(400, 300), 0.001);
        }

        @Test
        @DisplayName("Total = 0 → -1 (sin total definido)")
        void sinTotal() {
            assertEquals(-1, ReadingCalculator.calcularPorcentaje(50, 0), 0.001);
        }

        @Test
        @DisplayName("Total negativo → -1")
        void totalNegativo() {
            assertEquals(-1, ReadingCalculator.calcularPorcentaje(50, -10), 0.001);
        }

        @ParameterizedTest(name = "pág {0} de {1} → {2}%")
        @CsvSource({"1, 4, 25.0", "3, 4, 75.0", "200, 400, 50.0"})
        @DisplayName("Casos paramétricos varios")
        void parametricos(int actual, int total, double esperado) {
            assertEquals(esperado, ReadingCalculator.calcularPorcentaje(actual, total), 0.001);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // validarSesion
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("validarSesion()")
    class ValidarSesion {

        @Test
        @DisplayName("Sesión válida → null (sin error)")
        void valida() {
            assertNull(ReadingCalculator.validarSesion(10, 50, 30.0));
        }

        @Test
        @DisplayName("Fin < inicio → mensaje de error")
        void finMenorInicio() {
            String error = ReadingCalculator.validarSesion(50, 10, 30.0);
            assertNotNull(error);
            assertTrue(error.toLowerCase().contains("final") || error.toLowerCase().contains("menor"));
        }

        @Test
        @DisplayName("Minutos = 0 → sesión demasiado corta")
        void minutosCero() {
            assertNotNull(ReadingCalculator.validarSesion(10, 50, 0.0));
        }

        @Test
        @DisplayName("Minutos muy bajos (0.001) → sesión demasiado corta")
        void minutosMuyBajos() {
            assertNotNull(ReadingCalculator.validarSesion(10, 50, 0.001));
        }

        @Test
        @DisplayName("Misma página inicio y fin → válido (0 páginas pero no error de orden)")
        void mismaPagina() {
            // fin >= inicio cumple la condición; minutos válidos
            assertNull(ReadingCalculator.validarSesion(20, 20, 5.0));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // extraerHoraSegura
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("extraerHoraSegura()")
    class ExtraerHora {

        @Test
        @DisplayName("Fecha con hora válida → extrae hora correcta")
        void horaValida() {
            assertEquals(14, ReadingCalculator.extraerHoraSegura("17/03/2026 14:30"));
        }

        @Test
        @DisplayName("Fecha con hora y coma → extrae hora correcta")
        void horaConComa() {
            assertEquals(9, ReadingCalculator.extraerHoraSegura("17/03/2026, 09:00"));
        }

        @Test
        @DisplayName("Solo fecha (sin hora) → null")
        void soloFecha() {
            assertNull(ReadingCalculator.extraerHoraSegura("17/03/2026"));
        }

        @Test
        @DisplayName("Null → null")
        void nulo() {
            assertNull(ReadingCalculator.extraerHoraSegura(null));
        }

        @Test
        @DisplayName("Cadena vacía → null")
        void vacia() {
            assertNull(ReadingCalculator.extraerHoraSegura(""));
        }

        @Test
        @DisplayName("Formato inválido → null")
        void invalido() {
            assertNull(ReadingCalculator.extraerHoraSegura("no-es-una-fecha"));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // extraerDiaSemana
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("extraerDiaSemana()")
    class ExtraerDiaSemana {

        @Test
        @DisplayName("Lunes (07/04/2025) → 1")
        void lunes() {
            assertEquals(1, ReadingCalculator.extraerDiaSemana("07/04/2025"));
        }

        @Test
        @DisplayName("Domingo (06/04/2025) → 7")
        void domingo() {
            assertEquals(7, ReadingCalculator.extraerDiaSemana("06/04/2025"));
        }

        @Test
        @DisplayName("Fecha con hora incluida → extrae día correctamente")
        void conHora() {
            assertEquals(1, ReadingCalculator.extraerDiaSemana("07/04/2025 10:00"));
        }

        @Test
        @DisplayName("Fecha con coma → extrae día correctamente")
        void conComa() {
            assertEquals(1, ReadingCalculator.extraerDiaSemana("07/04/2025, 10:00"));
        }

        @Test
        @DisplayName("Null → null")
        void nulo() {
            assertNull(ReadingCalculator.extraerDiaSemana(null));
        }

        @Test
        @DisplayName("Vacío → null")
        void vacio() {
            assertNull(ReadingCalculator.extraerDiaSemana(""));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // extraerMesAnio
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("extraerMesAnio()")
    class ExtraerMesAnio {

        @Test
        @DisplayName("Fecha normal → formato yyyy-MM")
        void normal() {
            assertEquals("2026-03", ReadingCalculator.extraerMesAnio("17/03/2026"));
        }

        @Test
        @DisplayName("Enero → mes con cero a la izquierda")
        void enero() {
            assertEquals("2025-01", ReadingCalculator.extraerMesAnio("15/01/2025"));
        }

        @Test
        @DisplayName("Fecha con hora → extrae sólo fecha")
        void conHora() {
            assertEquals("2026-03", ReadingCalculator.extraerMesAnio("17/03/2026 14:30"));
        }

        @Test
        @DisplayName("Null → null")
        void nulo() {
            assertNull(ReadingCalculator.extraerMesAnio(null));
        }

        @Test
        @DisplayName("Vacío → null")
        void vacio() {
            assertNull(ReadingCalculator.extraerMesAnio(""));
        }

        @Test
        @DisplayName("Formato inválido → null")
        void invalido() {
            assertNull(ReadingCalculator.extraerMesAnio("2026-03-17"));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // acortar
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("acortar()")
    class Acortar {

        @Test
        @DisplayName("Texto corto → sin cambios")
        void corto() {
            assertEquals("Hola", ReadingCalculator.acortar("Hola", 10));
        }

        @Test
        @DisplayName("Texto exactamente igual al límite → sin cambios")
        void exacto() {
            assertEquals("12345", ReadingCalculator.acortar("12345", 5));
        }

        @Test
        @DisplayName("Texto largo → truncado con '...'")
        void largo() {
            String resultado = ReadingCalculator.acortar("El Señor de los Anillos", 10);
            assertTrue(resultado.endsWith("..."));
            assertEquals(10, resultado.length());
        }

        @Test
        @DisplayName("Null → null")
        void nulo() {
            assertNull(ReadingCalculator.acortar(null, 10));
        }

        @Test
        @DisplayName("maxChars muy pequeño (3) → solo '...'")
        void maxMuyPequenio() {
            String resultado = ReadingCalculator.acortar("Texto largo", 3);
            assertEquals("...", resultado);
        }

        @Test
        @DisplayName("maxChars = 0 → resultado no lanza excepción")
        void maxCero() {
            assertDoesNotThrow(() -> ReadingCalculator.acortar("Texto", 0));
        }
    }
}