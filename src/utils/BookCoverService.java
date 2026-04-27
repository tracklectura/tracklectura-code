package utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import db.SupabaseAuthService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class BookCoverService {





    public static BufferedImage fetchCoverImage(String title) {
        List<String> urls = fetchCoverUrls(title);
        if (urls != null && !urls.isEmpty()) {
            return downloadImage(urls.get(0));
        }
        return null;
    }

    public static List<String> fetchCoverUrls(String title) {
        if (title == null || title.trim().isEmpty())
            return new ArrayList<>();

        List<String> urls = fetchUrlsFromGoogle(title);
        if (urls.isEmpty()) {
            System.out.println("No cover found on Google Books. Fetching from OpenLibrary...");
            urls = fetchUrlsFromOpenLibrary(title);
        }
        return urls;
    }

    public static BufferedImage downloadImage(String urlStr) {
        try {
            if (urlStr == null || urlStr.isEmpty())
                return null;

            if (urlStr.startsWith("http")) {
                HttpURLConnection conn = (HttpURLConnection) URI.create(urlStr).toURL().openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                return ImageIO.read(conn.getInputStream());
            } else {

                File file = new File(urlStr);
                if (file.exists()) {
                    return ImageIO.read(file);
                }
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error downloading/loading image: " + e.getMessage());
            return null;
        }
    }





    /**
     * Sube una imagen local al bucket "covers" de Supabase Storage y devuelve
     * la URL pública permanente.
     *
     * La URL resultante es accesible desde cualquier PC sin autenticación
     * (el bucket debe ser público), lo que resuelve el problema de las
     * rutas locales que no funcionan al iniciar sesión desde otro equipo.
     *
     * Flujo:
     * 1. Lee los bytes del archivo local
     * 2. Hace PUT al endpoint de Supabase Storage
     * 3. Devuelve la URL pública:
     * https:
     *
     * Si falla (sin conexión, token caducado, etc.) devuelve null y el
     * llamador puede hacer fallback a la ruta local para modo offline.
     *
     * @param imageFile Archivo de imagen seleccionado por el usuario
     * @param libroId   ID del libro (usado como nombre único del archivo)
     * @param extension Extensión del archivo, ej: "jpg", "png"
     * @return URL pública de la imagen, o null si falla
     */
    public static String subirPortadaASupabase(File imageFile, int libroId, String extension) {
        String supabaseUrl = ConfigManager.getSupabaseUrl();
        String accessToken = SupabaseAuthService.getCurrentAccessToken();


        if (supabaseUrl == null || supabaseUrl.isEmpty()) {
            System.err.println("[BookCoverService] Supabase URL no configurada.");
            return null;
        }
        if (accessToken == null || accessToken.isEmpty()) {
            System.err.println("[BookCoverService] Sin token de acceso. ¿Estás en modo invitado?");
            return null;
        }

        try {
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());




            String nombreArchivo = "libro_" + libroId + "." + extension.toLowerCase();
            String uploadUrl = supabaseUrl
                    + "/storage/v1/object/covers/"
                    + nombreArchivo;

            HttpURLConnection conn = (HttpURLConnection) URI.create(uploadUrl).toURL().openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            conn.setRequestProperty("Content-Type", detectarMimeType(extension));

            conn.setRequestProperty("x-upsert", "true");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(imageBytes);
            }

            int statusCode = conn.getResponseCode();

            if (statusCode == 200 || statusCode == 201) {

                String publicUrl = supabaseUrl
                        + "/storage/v1/object/public/covers/"
                        + nombreArchivo;
                System.out.println("[BookCoverService] Portada subida: " + publicUrl);
                return publicUrl;
            } else {

                String errorBody = leerRespuesta(conn.getErrorStream());
                System.err.println("[BookCoverService] Error al subir portada (" + statusCode + "): " + errorBody);
                return null;
            }

        } catch (Exception e) {
            System.err.println("[BookCoverService] Excepción al subir portada: " + e.getMessage());
            return null;
        }
    }





    private static String detectarMimeType(String extension) {
        return switch (extension.toLowerCase()) {
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "image/jpeg";
        };
    }

    private static String leerRespuesta(InputStream is) {
        if (is == null)
            return "(sin cuerpo)";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line);
            return sb.toString();
        } catch (Exception e) {
            return "(error leyendo respuesta)";
        }
    }

    private static List<String> fetchUrlsFromGoogle(String title) {
        List<String> urls = new ArrayList<>();
        try {
            String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
            String apiUrl = "https://www.googleapis.com/books/v1/volumes?q=intitle:" + encodedTitle + "&maxResults=5";

            String json = fetchStringFromUrl(apiUrl);
            if (json == null)
                return urls;

            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (root.has("items")) {
                JsonArray items = root.getAsJsonArray("items");
                for (JsonElement item : items) {
                    JsonObject volumeInfo = item.getAsJsonObject().getAsJsonObject("volumeInfo");
                    if (volumeInfo.has("imageLinks")) {
                        JsonObject imageLinks = volumeInfo.getAsJsonObject("imageLinks");
                        String link = null;
                        if (imageLinks.has("thumbnail")) {
                            link = imageLinks.get("thumbnail").getAsString();
                        } else if (imageLinks.has("smallThumbnail")) {
                            link = imageLinks.get("smallThumbnail").getAsString();
                        }
                        if (link != null) {
                            String u = link.replace("http://", "https://").replace("\\u0026", "&");
                            if (!urls.contains(u))
                                urls.add(u);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Google fetch error: " + e.getMessage());
        }
        return urls;
    }

    private static List<String> fetchUrlsFromOpenLibrary(String title) {
        List<String> urls = new ArrayList<>();
        try {
            String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
            String apiUrl = "https://openlibrary.org/search.json?q=" + encodedTitle + "&limit=5";

            String json = fetchStringFromUrl(apiUrl);
            if (json == null)
                return urls;

            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (root.has("docs")) {
                JsonArray docs = root.getAsJsonArray("docs");
                for (JsonElement doc : docs) {
                    JsonObject docObj = doc.getAsJsonObject();
                    if (docObj.has("cover_i")) {
                        String imgUrl = "https://covers.openlibrary.org/b/id/"
                                + docObj.get("cover_i").getAsString() + "-L.jpg";
                        if (!urls.contains(imgUrl))
                            urls.add(imgUrl);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("OpenLibrary fetch error: " + e.getMessage());
        }
        return urls;
    }

    private static String fetchStringFromUrl(String urlStr) {
        try {
            URL url = URI.create(urlStr).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "ReadingTracker/1.0");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() != 200)
                return null;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
                    sb.append(line);
                return sb.toString();
            }
        } catch (Exception e) {
            return null;
        }
    }
}