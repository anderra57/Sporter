package com.anderpri.das_grupal.controllers.webservices;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SolicitudesWorker extends Worker {

    public SolicitudesWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Se recoge la operación que se quiere realizar en la base de datos
        String funcion = getInputData().getString("funcion");

        try {
            /*
            TODO
             Cambiar cosas que pueda haberme dejado por haber copiado la clase entera de TeamsWorker
             */

            // Se genera un HttpURLConnection para conectarse con el script de php
            // Direción en la que se encuentra el fichero php
            String direccion = "http://ec2-52-56-170-196.eu-west-2.compute.amazonaws.com/aarias023/WEB/das_grupal/solicitudes.php";
            HttpURLConnection urlConnection = null;
            URL url = new URL(direccion);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);

            // Todas las peticiones que se realicen (login y register) se realizarán mediante el método POST
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            // Cabecera para especificar de qué forma se envía la información
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // Comprobar qué se quiere hacer

            if ("aceptar".equals(funcion)) {
                String cookie = getInputData().getString("cookie");
                String actividad = getInputData().getString("actividad");
                urlConnection.setRequestProperty("Cookie", "PHPSESSID=" + cookie);
                // Preparar los parámetros para enviar en la petición
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("function", funcion)
                        .appendQueryParameter("actividad", actividad);
                String parametros = builder.build().getEncodedQuery();

                // Se incluyen los parámetros en la petición HTTP
                PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                out.print(parametros);
                out.close();

                // Se ejecuta la llamada al servicio web
                int statusCode = urlConnection.getResponseCode();
                String result = "";
                if (statusCode != 200) {
                    result = "Invalid request";
                }

                Data resultados = new Data.Builder()
                        .putString("datos", result)
                        .putString("cookie", cookie)
                        .build();

                return Result.success(resultados);

            }else if("rechazar".equals(funcion)){
                String cookie = getInputData().getString("cookie");
                String actividad = getInputData().getString("actividad");
                urlConnection.setRequestProperty("Cookie", "PHPSESSID=" + cookie);
                // Preparar los parámetros para enviar en la petición
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("function", funcion)
                        .appendQueryParameter("actividad", actividad);
                String parametros = builder.build().getEncodedQuery();

                // Se incluyen los parámetros en la petición HTTP
                PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                out.print(parametros);
                out.close();

                // Se ejecuta la llamada al servicio web
                int statusCode = urlConnection.getResponseCode();
                String result = "";
                if (statusCode != 200) {
                    result = "Invalid request";
                }

                Data resultados = new Data.Builder()
                        .putString("datos", result)
                        .putString("cookie", cookie)
                        .build();

                return Result.success(resultados);

            }else {
                // Algo no ha ido de forma correcta
                return Result.failure();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.failure();
    }
}
