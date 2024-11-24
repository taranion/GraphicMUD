/*
 * SPDX-FileCopyrightText: 2024 Stefan Prelle <stefan at prelle dot org>
 * SPDX-License-Identifier: MPL-2.0
 */
package com.graphicmud.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.SimpleFileServer;
import com.sun.net.httpserver.SimpleFileServer.OutputLevel;

/**
 *
 */
public class WebServerImpl implements WebServer {

	static class FileHandler implements HttpHandler {
		private Path baseDirectory;
		public FileHandler(Path baseDirectory) {
            this.baseDirectory = baseDirectory.toAbsolutePath().normalize();
        }
        @Override
        public void handle(HttpExchange exchange) throws java.io.IOException {
        	logger.log(Level.INFO, "Request for {0}", exchange.getRequestURI().getPath());

        	String requestedPath = exchange.getRequestURI().getPath();
             Path filePath = baseDirectory.resolve(requestedPath.substring(1)).normalize();
             logger.log(Level.INFO, "Normalized {0}", filePath);

        	// Sicherheitsüberprüfung: Der angeforderte Pfad darf nicht außerhalb des Basisverzeichnisses liegen
            if (!filePath.startsWith(baseDirectory)) {
                exchange.sendResponseHeaders(403, -1); // Zugriff verweigert
                return;
            }
            File file = filePath.toFile();
         // Überprüfen, ob die Datei existiert und eine Datei ist (kein Verzeichnis)
            if (!file.exists() || !file.isFile()) {
                exchange.sendResponseHeaders(404, -1); // Datei nicht gefunden
                return;
            }
            // Prüfe den "If-Modified-Since"-Header
            String ifModifiedSince = exchange.getRequestHeaders().getFirst("If-Modified-Since");
            if (ifModifiedSince != null) {
                Instant ifModifiedSinceInstant = Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(ifModifiedSince));
                FileTime lastModifiedTime = Files.getLastModifiedTime(file.toPath());

                // Wenn die Datei nicht seit dem angegebenen Datum geändert wurde, 304 zurückgeben
                if (lastModifiedTime.toInstant().isBefore(ifModifiedSinceInstant) || lastModifiedTime.toInstant().equals(ifModifiedSinceInstant)) {
                    exchange.sendResponseHeaders(304, -1);
                    return;
                }
            }

            // Ansonsten die Datei senden
            exchange.sendResponseHeaders(200, file.length());
            try (OutputStream os = exchange.getResponseBody(); FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        }

    }

	private final static Logger logger = System.getLogger("mud.web");

	private String hostName;
	private InetSocketAddress socket;
	private Path dataDir;
	private HttpServer server;
	private String baseURL;

	//-------------------------------------------------------------------
	public WebServerImpl(String hostname, InetAddress listenHost, int port, Path dataDir) {
		this.hostName = hostname;
		socket = new InetSocketAddress(listenHost, port);
		this.dataDir = dataDir;
		baseURL = String.format("http://%s:%d", hostname, port);
		System.err.println("baseURL "+baseURL);
		//baseURL = "http://prelle.selfhost.eu:4080/";
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.web.WebServer#start()
	 */
	@Override
	public void start() throws IOException {
//		server = HttpServer.create(socket, 5, getBaseURL(), null, null);
		server = SimpleFileServer.createFileServer(socket, Files.createTempDirectory("MUD"), OutputLevel.INFO);
		// Prepare delivering symbols
		HttpHandler handler = SimpleFileServer.createFileHandler(dataDir.resolve("symbols"));
		server.createContext("/symbols", handler);
		handler = SimpleFileServer.createFileHandler(dataDir.resolve("audio"));
		handler = new FileHandler(dataDir);
		server.createContext("/audio", handler);
		server.createContext("//audio", handler);
		handler = new FileHandler(dataDir);
		server.createContext("/html", handler);
//		HttpHandler deniedResponse = HttpHandlers.of(401, Headers.of("Deny", "GET"), "Denied");
//		server.createContext("/", deniedResponse);

		server.start();
		logger.log(Level.INFO, "Starting webserver on {2} to serve as {0} and deliver from {1}", baseURL, dataDir, socket.getAddress().getHostAddress(), hostName);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.web.WebServer#stop()
	 */
	@Override
	public void stop() {
		server.stop(1);
	}

	//-------------------------------------------------------------------
	/**
	 * @see com.graphicmud.web.WebServer#getBaseURL()
	 */
	@Override
	public String getBaseURL() {
		return baseURL;
	}

}
