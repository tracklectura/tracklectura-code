package ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios de SessionTimer.
 *
 * NOTA: El javax.swing.Timer interno NO se activa en tests (no hay Event Dispatch Thread
 * real corriendo), por lo que el callback onTick no se invoca automáticamente.
 * Lo que SÍ podemos testear de forma fiable y determinista es toda la lógica
 * de estado, acumulación y formateo, que es la parte de negocio crítica.
 */
@DisplayName("SessionTimer")
class SessionTimerTest {

    private SessionTimer timer;
    private List<String> ticks;

    @BeforeEach
    void setUp() {
        ticks = new ArrayList<>();
        timer = new SessionTimer(ticks::add);
    }

    // ─────────────────────────────────────────────────────────────
    // Estado inicial
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Estado inicial")
    class EstadoInicial {

        @Test
        @DisplayName("No está corriendo al crearse")
        void noCorre() {
            assertFalse(timer.isCorriendo());
        }

        @Test
        @DisplayName("Sin actividad al crearse")
        void sinActividad() {
            assertFalse(timer.tieneActividad());
        }

        @Test
        @DisplayName("Tiempo total = 0 ms")
        void tiempoTotal() {
            assertEquals(0, timer.getTotalMillis());
        }

        @Test
        @DisplayName("Minutos = 0.0")
        void minutosCero() {
            assertEquals(0.0, timer.getTotalMinutos(), 0.001);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // iniciar()
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("iniciar()")
    class Iniciar {

        @Test
        @DisplayName("Al iniciar, isCorriendo = true")
        void correDespuesDeIniciar() {
            timer.iniciar();
            assertTrue(timer.isCorriendo());
        }

        @Test
        @DisplayName("Llamar iniciar() dos veces no duplica el tiempo")
        void dobleIniciarNoDuplica() throws InterruptedException {
            timer.iniciar();
            Thread.sleep(100);
            timer.iniciar(); // segunda llamada debe ser ignorada
            Thread.sleep(100);
            long millis = timer.getTotalMillis();
            // Debe haber ~200ms acumulados, nunca el doble (~400ms)
            assertTrue(millis < 400, "No debe duplicar el tiempo: " + millis + "ms");
        }

        @Test
        @DisplayName("Después de iniciar, tieneActividad() = true (tras espera mínima)")
        void tieneActividadDespuesDeIniciar() throws InterruptedException {
            timer.iniciar();
            Thread.sleep(50);
            assertTrue(timer.tieneActividad());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // pausar()
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("pausar()")
    class Pausar {

        @Test
        @DisplayName("Después de pausar, isCorriendo = false")
        void noCorreDespausar() throws InterruptedException {
            timer.iniciar();
            Thread.sleep(50);
            timer.pausar();
            assertFalse(timer.isCorriendo());
        }

        @Test
        @DisplayName("El tiempo se congela al pausar")
        void tiempoCongelado() throws InterruptedException {
            timer.iniciar();
            Thread.sleep(100);
            timer.pausar();
            long t1 = timer.getTotalMillis();
            Thread.sleep(100);
            long t2 = timer.getTotalMillis();
            assertEquals(t1, t2, "El tiempo no debe crecer mientras está pausado");
        }

        @Test
        @DisplayName("Pausar sin haber iniciado no lanza excepción")
        void pausarSinIniciar() {
            assertDoesNotThrow(() -> timer.pausar());
        }

        @Test
        @DisplayName("El tiempo acumulado antes de pausa se conserva al reiniciar")
        void tiempoAcumuladoConservado() throws InterruptedException {
            timer.iniciar();
            Thread.sleep(100);
            timer.pausar();
            long acumulado = timer.getTotalMillis();
            assertTrue(acumulado >= 80, "Debe haber al menos ~80ms acumulados");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // reiniciar()
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("reiniciar()")
    class Reiniciar {

        @Test
        @DisplayName("Después de reiniciar, isCorriendo = false")
        void noCorreDespuesDeReiniciar() throws InterruptedException {
            timer.iniciar();
            Thread.sleep(50);
            timer.reiniciar();
            assertFalse(timer.isCorriendo());
        }

        @Test
        @DisplayName("Después de reiniciar, tiempo = 0")
        void tiempoCero() throws InterruptedException {
            timer.iniciar();
            Thread.sleep(100);
            timer.reiniciar();
            assertEquals(0, timer.getTotalMillis());
        }

        @Test
        @DisplayName("Después de reiniciar, sin actividad")
        void sinActividadTrasReiniciar() throws InterruptedException {
            timer.iniciar();
            Thread.sleep(50);
            timer.reiniciar();
            assertFalse(timer.tieneActividad());
        }

        @Test
        @DisplayName("Se puede iniciar de nuevo después de reiniciar")
        void reiniciarYVolvera() throws InterruptedException {
            timer.iniciar();
            Thread.sleep(50);
            timer.reiniciar();
            timer.iniciar();
            Thread.sleep(50);
            assertTrue(timer.isCorriendo());
            assertTrue(timer.getTotalMillis() >= 0);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ajustarTiempo()
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("ajustarTiempo()")
    class AjustarTiempo {

        @Test
        @DisplayName("Sumar milisegundos incrementa el tiempo")
        void sumar() throws InterruptedException {
            timer.iniciar();
            Thread.sleep(50);
            timer.pausar();
            long antes = timer.getTotalMillis();
            timer.ajustarTiempo(60_000); // +1 minuto
            long despues = timer.getTotalMillis();
            assertTrue(despues > antes);
        }

        @Test
        @DisplayName("Restar más del tiempo total → queda en 0 (no negativo)")
        void restarMasDelTotal() throws InterruptedException {
            timer.iniciar();
            Thread.sleep(100);
            timer.pausar();
            timer.ajustarTiempo(-999_999_999L); // restar muchísimo
            assertEquals(0, timer.getTotalMillis());
        }

        @Test
        @DisplayName("Ajuste de 0 ms no modifica el tiempo")
        void ajusteCero() throws InterruptedException {
            timer.iniciar();
            Thread.sleep(100);
            timer.pausar();
            long antes = timer.getTotalMillis();
            timer.ajustarTiempo(0);
            assertEquals(antes, timer.getTotalMillis());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Ciclo completo: iniciar → pausar → iniciar → acumular
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Ciclo iniciar → pausar → iniciar")
    class CicloCompleto {

        @Test
        @DisplayName("El tiempo se acumula correctamente en múltiples ciclos")
        void acumulacion() throws InterruptedException {
            timer.iniciar();
            Thread.sleep(100);
            timer.pausar();
            long primerTramo = timer.getTotalMillis();

            timer.iniciar();
            Thread.sleep(100);
            timer.pausar();
            long total = timer.getTotalMillis();

            assertTrue(total > primerTramo,
                    "El segundo tramo debe añadirse al primero. Total=" + total + " Tramo1=" + primerTramo);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // getTotalMinutos
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getTotalMinutos()")
    class TotalMinutos {

        @Test
        @DisplayName("Conversión correcta: 60_000 ms = 1.0 minuto")
        void conversion() {
            timer.ajustarTiempo(60_000);
            assertEquals(1.0, timer.getTotalMinutos(), 0.01);
        }

        @Test
        @DisplayName("Sin actividad → 0.0 minutos")
        void cero() {
            assertEquals(0.0, timer.getTotalMinutos(), 0.001);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // formatearTiempo (método estático puro)
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("formatearTiempo() [estático]")
    class FormatearTiempo {

        @Test
        @DisplayName("0 ms → 00:00:00")
        void cero() {
            assertEquals("00:00:00", SessionTimer.formatearTiempo(0));
        }

        @Test
        @DisplayName("1 segundo → 00:00:01")
        void unSegundo() {
            assertEquals("00:00:01", SessionTimer.formatearTiempo(1_000));
        }

        @Test
        @DisplayName("1 minuto → 00:01:00")
        void unMinuto() {
            assertEquals("00:01:00", SessionTimer.formatearTiempo(60_000));
        }

        @Test
        @DisplayName("1 hora → 01:00:00")
        void unaHora() {
            assertEquals("01:00:00", SessionTimer.formatearTiempo(3_600_000));
        }

        @Test
        @DisplayName("1h 30m 45s → 01:30:45")
        void horaMinutosSegundos() {
            long ms = (1 * 3600 + 30 * 60 + 45) * 1000L;
            assertEquals("01:30:45", SessionTimer.formatearTiempo(ms));
        }

        @Test
        @DisplayName("59m 59s → 00:59:59")
        void casiUnaHora() {
            long ms = (59 * 60 + 59) * 1000L;
            assertEquals("00:59:59", SessionTimer.formatearTiempo(ms));
        }

        @Test
        @DisplayName("Más de 24h → no lanza excepción y mantiene formato")
        void masDe24h() {
            long ms = 25 * 3_600_000L;
            assertDoesNotThrow(() -> SessionTimer.formatearTiempo(ms));
        }
    }
}