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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class UsersWorker extends Worker {

    public UsersWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
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
            String direccion = "http://ec2-52-56-170-196.eu-west-2.compute.amazonaws.com/aarias023/WEB/das_grupal/users.php";
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
                String username = getInputData().getString("username");
                String password = getInputData().getString("password");
                String token = getInputData().getString("token");
                String cookie = "";

                // Preparar los parámetros para enviar en la petición
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("function", funcion)
                        .appendQueryParameter("username", username)
                        .appendQueryParameter("password", password)
                        .appendQueryParameter("token", token);
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

                // Conseguimos la cookie
                cookie = urlConnection.getHeaderField("Set-Cookie").substring(10).split(";")[0];
                Log.d("cookie_worker",cookie);

                System.out.println(result);

                Data resultados = new Data.Builder()
                        .putString("datos", result.toString())
                        .putString("cookie", cookie)
                        .build();

                // Devolver que t0do ha ido bien
                return Result.success(resultados);

            } else if("register".equals(funcion)) {
                // Se realizar el registro del usuario en la base de datos
                String username = getInputData().getString("username");
                String password = getInputData().getString("password");
                String name = getInputData().getString("name");
                String city = getInputData().getString("city");
                String age = getInputData().getString("age");

                // Preparar los parámetros para enviar en la petición
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("function", funcion)
                        .appendQueryParameter("username", username)
                        .appendQueryParameter("password", password)
                        .appendQueryParameter("name", name)
                        .appendQueryParameter("city", city)
                        .appendQueryParameter("age", age);
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
            } else if("logout".equals(funcion)) {

                // Se ejecuta la llamada al servicio web
                urlConnection.getResponseCode();

                return Result.success();
            } else if("verifysession".equals(funcion)) {

                // Se ejecuta la llamada al servicio web
                String cookie = getInputData().getString("cookie");

                // Preparar los parámetros para enviar en la petición
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("function", "verifysession")
                        .appendQueryParameter("cookie", cookie);
                String parametros = builder.build().getEncodedQuery();

                // Se incluyen los parámetros en la petición HTTP
                PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                out.print(parametros);
                out.close();

                // Se ejecuta la llamada al servicio web
                int statusCode = urlConnection.getResponseCode();
                String line;
                StringBuilder result = new StringBuilder();
                result.append("");
                if (statusCode == 200) {
                    // Cósigo 200 OK, se leen los datos de la respuesta
                    BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

                    while ((line = bufferedReader.readLine()) != null) {
                        result.append(line);
                    }
                    inputStream.close();
                }

                Log.d("sout_wer",result.toString());

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
