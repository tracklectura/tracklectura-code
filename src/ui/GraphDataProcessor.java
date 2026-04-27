package ui;

import db.DatabaseManager;
import model.DataPoint;
import model.Sesion;
import ui.charts.GraphDateUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Encapsula toda la lógica de consulta y transformación de datos para las
 * gráficas de {@link GraphWindow}.
 * <p>
 * Esta clase no contiene ninguna referencia a componentes Swing; opera
 * únicamente sobre listas de datos primitivos, lo que facilita tests
 * unitarios independientes de la UI.
 *
 * <h3>Resultado de cada método</h3>
 * Todos los métodos devuelven un {@link GraphData} con tres listas paralelas:
 * <ul>
 *   <li>{@code fechas}   — etiqueta del eje X (puede contener capítulo separado por ";")</li>
 *   <li>{@code valores}  — eje Y principal</li>
 *   <li>{@code valoresSec} — eje Y secundario (solo gráfica dual)</li>
 * </ul>
 */
public final class GraphDataProcessor {

    private GraphDataProcessor() { /* no instanciable */ }





    /** Contenedor inmutable de los tres ejes de datos de una gráfica. */
    public record GraphData(List<String> fechas, List<Double> valores, List<Double> valoresSec) {
        public GraphData {
            fechas      = Collections.unmodifiableList(fechas);
            valores     = Collections.unmodifiableList(valores);
            valoresSec  = Collections.unmodifiableList(valoresSec);
        }

        public boolean isEmpty() { return fechas.isEmpty(); }
    }





    /**
     * Obtiene y procesa los datos necesarios para la métrica indicada.
     *
     * @param metrica         Nombre de la métrica (coincide con los ítems del JComboBox)
     * @param libroSeleccionado Nombre del libro activo (puede ser null para métricas globales)
     * @param minPag          Filtro: páginas mínimas por sesión
     * @param fechaFiltro     Filtro: fecha mínima en formato "dd/MM/yyyy"
     * @param agruparPorDia   Si se deben agregar sesiones del mismo día
     * @param mostrarCapitulo Si las etiquetas deben incluir el capítulo
     * @return {@link GraphData} listo para pasarle a un panel de gráfica
     */
    public static GraphData obtener(String metrica, String libroSeleccionado,
                                    int minPag, String fechaFiltro,
                                    boolean agruparPorDia, boolean mostrarCapitulo) {
        return switch (metrica) {
            case "Mapa de Consistencia"          -> heatmap();
            case "Meta Anual"                    -> vacio();
            case "PPM Comparativa"               -> vacio();
            case "Evolución Mensual"             -> evolucionMensual();
            case "Correlación: Minutos vs PPM"   -> correlacionMinutosPpm(libroSeleccionado);
            case "Páginas por día de la semana"  -> paginasPorDiaSemana(libroSeleccionado);
            case "Actividad por Hora"            -> actividadPorHora(libroSeleccionado);
            case "Progreso Acumulado"            -> progresoAcumulado(libroSeleccionado);
            default                              -> metricaEstandar(metrica, libroSeleccionado,
                    minPag, fechaFiltro,
                    agruparPorDia, mostrarCapitulo);
        };
    }





    /** Datos para el heatmap de consistencia (todas las sesiones globales). */
    private static GraphData heatmap() {
        List<String> fechas  = new ArrayList<>();
        List<Double> valores = new ArrayList<>();
        try {
            List<DataPoint> puntos = DatabaseManager.obtenerDatosGrafica("paginas", -1, 0, true, true, false);
            for (DataPoint p : puntos) {
                fechas.add(p.getEtiqueta());
                valores.add(p.getValor());
            }
        } catch (Exception e) {
            System.err.println("[GraphDataProcessor] Error heatmap: " + e.getMessage());
        }
        return new GraphData(fechas, valores, List.of());
    }

    /** Evolución de páginas leídas por mes (global, todos los libros). */
    private static GraphData evolucionMensual() {
        List<String> fechas  = new ArrayList<>();
        List<Double> valores = new ArrayList<>();
        try {
            List<Sesion> todas = DatabaseManager.obtenerTodasLasSesionesDesde("1970-01-01");
            Map<String, Double> porMes = new TreeMap<>();
            for (Sesion s : todas) {
                String mes = utils.ReadingCalculator.extraerMesAnio(s.getFecha());
                if (mes != null)
                    porMes.merge(mes, (double) s.getPaginasLeidas(), Double::sum);
            }
            fechas.addAll(porMes.keySet());
            valores.addAll(porMes.values());
        } catch (Exception e) {
            System.err.println("[GraphDataProcessor] Error evolución mensual: " + e.getMessage());
        }
        return new GraphData(fechas, valores, List.of());
    }

    /** Correlación minutos de sesión vs PPM. */
    private static GraphData correlacionMinutosPpm(String libro) {
        List<String> fechas  = new ArrayList<>();
        List<Double> valores = new ArrayList<>();
        try {
            List<Sesion> sesiones = obtenerSesiones(libro);
            for (Sesion s : sesiones) {
                if (s.getMinutos() > 0 && s.getPpm() > 0) {
                    fechas.add(String.valueOf(s.getMinutos()));
                    valores.add(s.getPpm());
                }
            }
        } catch (Exception e) {
            System.err.println("[GraphDataProcessor] Error correlación: " + e.getMessage());
        }
        return new GraphData(fechas, valores, List.of());
    }

    /** Suma de páginas leídas por día de la semana (Lun–Dom). */
    private static GraphData paginasPorDiaSemana(String libro) {
        List<String> fechas  = new ArrayList<>();
        List<Double> valores = new ArrayList<>();
        try {
            double[] pags = new double[7];
            String[] nombres = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
            boolean hay = false;
            for (Sesion s : obtenerSesiones(libro)) {
                Integer dia = utils.ReadingCalculator.extraerDiaSemana(s.getFecha());
                if (dia != null) {
                    pags[dia - 1] += s.getPaginasLeidas();
                    hay = true;
                }
            }
            if (hay) {
                for (int i = 0; i < 7; i++) {
                    fechas.add(nombres[i]);
                    valores.add(pags[i]);
                }
            }
        } catch (Exception e) {
            System.err.println("[GraphDataProcessor] Error pág/día semana: " + e.getMessage());
        }
        return new GraphData(fechas, valores, List.of());
    }

    /** Suma de páginas leídas por hora del día (0–23). */
    private static GraphData actividadPorHora(String libro) {
        List<String> fechas  = new ArrayList<>();
        List<Double> valores = new ArrayList<>();
        try {
            double[] pags = new double[24];
            boolean hay = false;
            for (Sesion s : obtenerSesiones(libro)) {
                Integer hora = utils.ReadingCalculator.extraerHoraSegura(s.getFecha());
                if (hora != null) {
                    pags[hora] += s.getPaginasLeidas();
                    hay = true;
                }
            }
            if (hay) {
                for (int i = 0; i < 24; i++) {
                    fechas.add(i + ":00");
                    valores.add(pags[i]);
                }
            }
        } catch (Exception e) {
            System.err.println("[GraphDataProcessor] Error actividad/hora: " + e.getMessage());
        }
        return new GraphData(fechas, valores, List.of());
    }

    /**
     * Progreso acumulado de páginas leídas para un libro concreto.
     * Devuelve la página máxima por día para evitar duplicados.
     */
    private static GraphData progresoAcumulado(String libro) {
        if (libro == null) return vacio();
        List<String> fechas  = new ArrayList<>();
        List<Double> valores = new ArrayList<>();
        try {
            int libroId = DatabaseManager.obtenerLibroId(libro);
            List<DataPoint> puntos = DatabaseManager.obtenerDatosGrafica(
                    "pag_fin", libroId, 0, false, false, false);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            Map<LocalDate, Double> porDia = new TreeMap<>();
            for (DataPoint p : puntos) {
                LocalDate d = GraphDateUtils.parsearFecha(p.getEtiqueta());
                if (d != null) porDia.merge(d, p.getValor(), Double::max);
            }
            for (Map.Entry<LocalDate, Double> e : porDia.entrySet()) {
                fechas.add(e.getKey().format(fmt));
                valores.add(e.getValue());
            }


            if (!valores.isEmpty() && valores.getFirst() > 0) {
                fechas.addFirst("Inicio");
                valores.addFirst(0.0);
            }
        } catch (Exception e) {
            System.err.println("[GraphDataProcessor] Error progreso acumulado: " + e.getMessage());
        }
        return new GraphData(fechas, valores, List.of());
    }

    /**
     * Métricas estándar: "Páginas Totales" y "PPM (Velocidad)".
     * Aplica filtros de página mínima, fecha y agrupación por día.
     */
    private static GraphData metricaEstandar(String metrica, String libro,
                                             int minPag, String fechaFiltro,
                                             boolean agruparPorDia, boolean mostrarCap) {
        if (libro == null) return vacio();

        List<String> fechas     = new ArrayList<>();
        List<Double> valores    = new ArrayList<>();
        List<Double> valoresSec = new ArrayList<>();

        try {
            boolean esDual   = "Págs + Tiempo".equals(metrica);
            String  colSql   = metrica.startsWith("Páginas") ? "paginas" : "ppm";
            int libroId      = DatabaseManager.obtenerLibroId(libro);
            List<DataPoint> puntos = DatabaseManager.obtenerDatosGrafica(
                    colSql, libroId, minPag, agruparPorDia, false, esDual);

            Map<LocalDate, double[]>  mapaAgrupado    = new TreeMap<>();
            Map<LocalDate, double[]>  mapaAgrupadoSec = new TreeMap<>();
            Map<LocalDate, String>    mapaCap         = new TreeMap<>();
            Map<LocalDate, String>    mapaEtiqueta    = new TreeMap<>();

            LocalDate fFiltroDate = (fechaFiltro != null && !fechaFiltro.isBlank())
                    ? GraphDateUtils.parsearFecha(fechaFiltro) : null;

            for (DataPoint p : puntos) {
                LocalDate fecha = GraphDateUtils.parsearFecha(p.getEtiqueta());
                if (fecha == null) continue;


                double paginasDelPunto = "ppm".equals(colSql) ? p.getValorSec() : p.getValor();
                if (minPag > 0 && paginasDelPunto < minPag) continue;


                if (fFiltroDate != null && fecha.isBefore(fFiltroDate)) continue;

                double val    = p.getValor();
                double valSec = p.getValorSec();
                String cap    = p.getCapitulos();
                String etiq   = fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                if (agruparPorDia) {
                    double[] acum = mapaAgrupado.getOrDefault(fecha, new double[]{0.0, 0});
                    if ("paginas".equals(colSql) || esDual) {
                        acum[0] += val;
                    } else {
                        acum[0] += val;
                        acum[1]++;
                    }
                    mapaAgrupado.put(fecha, acum);
                    if (esDual) {
                        double[] acumSec = mapaAgrupadoSec.getOrDefault(fecha, new double[]{0.0, 0});
                        acumSec[0] += valSec;
                        mapaAgrupadoSec.put(fecha, acumSec);
                    }
                    String capExist = mapaCap.getOrDefault(fecha, "");
                    if (cap != null && !cap.isEmpty())
                        mapaCap.put(fecha, capExist.isEmpty() ? cap : capExist + ";" + cap);
                    mapaEtiqueta.put(fecha, etiq);
                } else {
                    fechas.add(etiq + (mostrarCap && cap != null ? ";" + cap : ""));
                    valores.add(val);
                    if (esDual) valoresSec.add(valSec);
                }
            }

            if (agruparPorDia) {
                for (Map.Entry<LocalDate, double[]> entry : mapaAgrupado.entrySet()) {
                    LocalDate fech = entry.getKey();
                    double[]  acum = entry.getValue();
                    double valor   = ("paginas".equals(colSql) || esDual)
                            ? acum[0]
                            : (acum[1] > 0 ? acum[0] / acum[1] : 0);
                    String etiq = mapaEtiqueta.get(fech);
                    if (mostrarCap && mapaCap.containsKey(fech))
                        etiq += ";" + mapaCap.get(fech);
                    fechas.add(etiq);
                    valores.add(valor);
                    if (esDual) {
                        double[] acumSec = mapaAgrupadoSec.getOrDefault(fech, new double[]{0, 0});
                        valoresSec.add(acumSec[0]);
                    }
                }
            } else {

                ordenarPorFecha(fechas, valores, valoresSec, esDual);
            }

        } catch (Exception e) {
            System.err.println("[GraphDataProcessor] Error métrica estándar: " + e.getMessage());
        }

        return new GraphData(fechas, valores, valoresSec);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private static GraphData vacio() {
        return new GraphData(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    private static List<Sesion> obtenerSesiones(String libro) {
        if ("--- Todos los libros ---".equals(libro))
            return DatabaseManager.obtenerTodasLasSesionesDesde("1970-01-01");
        if (libro == null) return List.of();
        int id = DatabaseManager.obtenerLibroId(libro);
        return DatabaseManager.obtenerSesionesPorLibro(id);
    }

    /**
     * Ordena en paralelo las tres listas por la fecha contenida en la primera
     * parte de cada etiqueta (antes del ";").
     */
    private static void ordenarPorFecha(List<String> fechas, List<Double> valores,
                                        List<Double> valoresSec, boolean conSec) {
        if (fechas.size() < 2) return;

        List<LocalDate> parsed = new ArrayList<>();
        for (String f : fechas) {
            String solo = f.contains(";") ? f.split(";")[0] : f;
            parsed.add(GraphDateUtils.parsearFecha(solo));
        }

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < fechas.size(); i++) indices.add(i);
        indices.sort(Comparator.comparing(i -> {
            LocalDate d = parsed.get(i);
            return d != null ? d : LocalDate.MIN;
        }));

        List<String> fo = new ArrayList<>();
        List<Double> vo = new ArrayList<>();
        List<Double> so = new ArrayList<>();
        for (int idx : indices) {
            fo.add(fechas.get(idx));
            vo.add(valores.get(idx));
            if (conSec) so.add(valoresSec.get(idx));
        }
        fechas.clear();  fechas.addAll(fo);
        valores.clear(); valores.addAll(vo);
        if (conSec) { valoresSec.clear(); valoresSec.addAll(so); }
    }
}