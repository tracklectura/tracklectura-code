package ui.charts;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utilidades de parseo de fechas compartidas por los paneles de gráfica
 * y por {@link ui.GraphDataProcessor}.
 */
public final class GraphDateUtils {

    private static final DateTimeFormatter FMT_DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private GraphDateUtils() { /* no instanciable */ }

    /**
     * Acepta cadenas con hora ("15/03/2026 06:26"), solo fecha ("15/03/2026")
     * o formato ISO ("2026-03-15").
     * Devuelve {@code null} si no puede parsear.
     */
    public static LocalDate parsearFecha(String s) {
        if (s == null) return null;
        String d = s.length() > 10 ? s.substring(0, 10).trim() : s.trim();
        try {
            return LocalDate.parse(d, FMT_DMY);
        } catch (Exception e1) {
            try {
                return LocalDate.parse(d, FMT_ISO);
            } catch (Exception e2) {
                return null;
            }
        }
    }
}