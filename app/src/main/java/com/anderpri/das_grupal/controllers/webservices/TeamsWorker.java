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

public class TeamsWorker extends Worker {

    public TeamsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Se recoge la operación que se quiere realizar en la base de datos
        String funcion = getInputData().getString("funcion");

        try {
            // Se genera un HttpURLConnection para conectarse con el script de php
            // Direción en la que se encuentra el fichero php
            String direccion = "http://ec2-52-56-170-196.eu-west-2.compute.amazonaws.com/aarias023/WEB/das_grupal/teams.php";
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

            // Login
            if("login".equals(funcion)){
                // Se recogen el usuario y la contraseña
                String username = getInputData().getString("teamname");
                String password = getInputData().getString("teampass");
                String cookie = getInputData().getString("cookie");
                urlConnection.setRequestProperty("Cookie","PHPSESSID=" + cookie);

                // Preparar los parámetros para enviar en la petición
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("function", "join")
                        .appendQueryParameter("teamname", username)
                        .appendQueryParameter("teampass", password);
                String parametros = builder.build().getEncodedQuery();

                // Se incluyen los parámetros en la petición HTTP
                PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                out.print(parametros);
                out.close();

                // Se ejecuta la llamada al servicio web
                int statusCode = urlConnection.getResponseCode();
                String result = "";
                if (statusCode == 401 || statusCode == 500) {
                    result = "Invalid login";
                }

                Log.d("TeamsWorker", result);

                Data resultados = new Data.Builder()
                        .putString("datos", result)
                        .build();

                // Devolver que t0do ha ido bien
                return Result.success(resultados);

            } else if("register".equals(funcion)) {
                // Se realizar el registro del usuario en la base de datos
                String name = getInputData().getString("teamname");
                String password = getInputData().getString("teampass");
                String imageName = getInputData().getString("imageName");
                String cookie = getInputData().getString("cookie");
                Log.d("debasjd","PHPSESSID=" + cookie);
                urlConnection.setRequestProperty("Cookie","PHPSESSID=" + cookie);

                // Preparar los parámetros para enviar en la petición
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("function", "create")
                        .appendQueryParameter("teamname", name)
                        .appendQueryParameter("teampass", password)
                        .appendQueryParameter("imageName", imageName);
                String parametros = builder.build().getEncodedQuery();

                // Se incluyen los parámetros en la petición HTTP
                PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                out.print(parametros);
                out.close();

                // Se ejecuta la llamada al servicio web
                int statusCode = urlConnection.getResponseCode();
                String line;
                StringBuilder result = new StringBuilder();
                if (statusCode == 200) {
                    // Cósigo 200 OK, se leen los datos de la respuesta
                    BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

                    while ((line = bufferedReader.readLine()) != null) {
                        result.append(line);
                    }
                    inputStream.close();
                }

                Data resultados = new Data.Builder()
                        .putString("datos", result.toString())
                        .build();

                // Devolver que t0do ha ido bien
                return Result.success(resultados);
            }else if("updatepass".equals(funcion)) {
                // Se realizar el registro del usuario en la base de datos
                String name = getInputData().getString("teamname");
                String password = getInputData().getString("teampass");
                String cookie = getInputData().getString("cookie");
                Log.d("debasjd","PHPSESSID=" + cookie);
                urlConnection.setRequestProperty("Cookie","PHPSESSID=" + cookie);

                // Preparar los parámetros para enviar en la petición
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("function", "updatepass")
                        .appendQueryParameter("teamname", name)
                        .appendQueryParameter("teampass", password);
                String parametros = builder.build().getEncodedQuery();

                // Se incluyen los parámetros en la petición HTTP
                PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                out.print(parametros);
                out.close();

                // Se ejecuta la llamada al servicio web
                urlConnection.getResponseCode();


                Data resultados = new Data.Builder()
                        .putString("datos", "")
                        .build();

                // Devolver que t0do ha ido bien
                return Result.success(resultados);
            } else if("mostrarGruposActividad".equals(funcion)) {
                // Se realizar el registro del usuario en la base de datos
                String actividad = getInputData().getString("actividad");
                String cookie = getInputData().getString("cookie");
                urlConnection.setRequestProperty("Cookie","PHPSESSID=" + cookie);

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
                String line;
                StringBuilder result = new StringBuilder();
                if (statusCode == 200) {
                    // Cósigo 200 OK, se leen los datos de la respuesta
                    BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

                    while ((line = bufferedReader.readLine()) != null) {
                        result.append(line);
                    }
                    inputStream.close();
                }

                Data resultados = new Data.Builder()
                        .putString("datos", result.toString())
                        .build();

                // Devolver que t0do ha ido bien
                return Result.success(resultados);
            } else {
                // Algo no ha ido de forma correcta
                return Result.failure();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.failure();
    }
}
