package com.axibase.crawler.atsd;

import java.io.*;
import java.net.*;
import java.util.*;

class AtsdTcpClient {

    private String host;
    private int port;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    AtsdTcpClient(String _host, int _port) {
        host = _host;
        port = _port;
    }

    void init() throws IOException {
        socket = new Socket(host, port);
        writer = new PrintWriter(socket.getOutputStream(), true);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    void shutdown() {
        try {
            writer.flush();
            writer.close();
            reader.close();
        } catch (Exception ioe) {
            // ignore
        }
        try {
            socket.close();
        } catch (Exception ioe) {
            // ignore
        }
    }

    private synchronized void writeCommand(String command) {
        writer.println(command);
        writer.flush();
    }

    void sendSeries(String isoDateString, String entity, String metric, Double value, Map<String, String> tags) throws IOException {
        if (entity.contains(" ")) {
            throw new IllegalArgumentException("Entity name can include only printable characters");
        }
        if (metric.contains(" ")) {
            throw new IllegalArgumentException("Metric name can include only printable characters");
        }

        String command = "series";
        command += " d:" + isoDateString;
        command += " e:" + escape(entity);
        command += " m:" + escape(metric) + "=" + value;
        if (tags != null) {
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                String key = entry.getKey();
                key = key.replace(' ', '_');

                if (entry.getValue() == null) {
                    throw new IllegalArgumentException("Property tag value cannot be null");
                }
                String val = escape(entry.getValue()).trim();
                if (val.isEmpty()) {
                    throw new IllegalArgumentException("Property tag value cannot be empty");
                }
                command += " t:" + escape(key) + "=" + val;
            }
        }
        writeCommand(command);
    }

    void sendProperty(String isoDateString, String entity, String type, Map<String, String> tags) throws IOException {
        if (entity.contains(" ")) {
            throw new IllegalArgumentException("Entity name can include only printable characters");
        }
        if (type.contains(" ")) {
            throw new IllegalArgumentException("Property type can include only printable characters");
        }
        String command = "property";
        command += " d:" + isoDateString;
        command += " e:" + escape(entity);
        command += " t:" + escape(type);
        if (tags != null) {
            for (Map.Entry<String, String> entry : tags.entrySet()) {
                String key = entry.getKey();
                key = key.replace(' ', '_');

                if (entry.getValue() == null) {
                    throw new IllegalArgumentException("Property tag value cannot be null");
                }
                String val = escape(entry.getValue()).trim();
                if (val.isEmpty()) {
                    throw new IllegalArgumentException("Property tag value cannot be empty");
                }
                command += " v:" + escape(key) + "=" + val;
            }
        }
        writeCommand(command);
    }

    void sendEntity(String entity, Map<String, String> tags) {
        if (entity.contains(" ")) {
            throw new IllegalArgumentException("Entity name can include only printable characters");
        }

        String command = "entity";
        command += " e:" + escape(entity);
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            String key = entry.getKey();

            if (entry.getValue() == null) {
                throw new IllegalArgumentException("Property tag value cannot be null");
            }
            String val = escape(entry.getValue()).trim();
            if (val.isEmpty()) {
                throw new IllegalArgumentException("Property tag value cannot be empty");
            }
            command += " t:" + escape(key) + "=" + val;
        }
        writeCommand(command);
    }

    void sendEntity(String entity, String label, Map<String, String> tags) {
        if (entity.contains(" ")) {
            throw new IllegalArgumentException("Entity name can include only printable characters");
        }

        String command = "entity";
        command += " e:" + escape(entity);
        command += " l:" + escape(label);
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            String key = entry.getKey();

            if (entry.getValue() == null) {
                throw new IllegalArgumentException("Property tag value cannot be null");
            }
            String val = escape(entry.getValue()).trim();
            if (val.isEmpty()) {
                throw new IllegalArgumentException("Property tag value cannot be empty");
            }
            command += " t:" + escape(key) + "=" + val;
        }
        writeCommand(command);
    }

    private String escape(String s) {
        if (s.contains("\"")) {
            s = s.replaceAll("\"", "\"\"");
        }
        char[] escapeChars = {'=', '"', ' ', '\r', '\n', '\t'};
        for (char c : escapeChars) {
            if (s.indexOf(c) >= 0) {
                s = "\"" + s + "\"";
                break;
            }
        }
        return s;
    }
}
