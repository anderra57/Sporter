package com.anderpri.das_grupal;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseService extends FirebaseMessagingService {

    private String actividad;
    private String grupo;
    private String function;

    // Contenido de la notificación
    private String titulo;
    private String contenido;
    private String subtexto;

    public FirebaseService(){}

    // Método que se ejecuta al recibir un mensaje FCM
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // Si el mensaje FCM viene con datos guardarlas en las variables
        if (remoteMessage.getData().size() > 0) {
            // Si el mensaje FCM es una notificación
            if (remoteMessage.getNotification() != null) {
                // Creación del canal de notificaciones
                NotificationManager elManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationCompat.Builder elBuilder = new NotificationCompat.Builder(this, "Notificaciones");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel elCanal = new NotificationChannel("Notificaciones", "CanalNotificaciones",
                            NotificationManager.IMPORTANCE_DEFAULT);
                    elCanal.setDescription("Canal de notificaciones");
                    elCanal.enableLights(true);
                    elCanal.setLightColor(Color.RED);
                    elCanal.setVibrationPattern(new long[]{0, 1000, 500, 1000});
                    elCanal.enableVibration(true);
                    elManager.createNotificationChannel(elCanal);
                }

                // Ahora vamos a comprobar que tipo la notificación por qué ha surgido
                function = remoteMessage.getData().get("function");
                actividad = remoteMessage.getData().get("actividad");

                if(function.equals("borrar")) { // El administrador ha borrado una actividad
                    titulo = getString(R.string.borrar_titulo);
                    contenido = getString(R.string.borrar_contenido) + actividad;
                    subtexto = "";
                } else if(function.equals("crear")) { // El administrador ha creado una actividad
                    titulo = getString(R.string.crear_titulo);
                    contenido = getString(R.string.crear_contenido) + actividad;
                    subtexto = "";
                } else if(function.equals("sugerir")) { // Un grupo ha sugerido una actividad
                    grupo = remoteMessage.getData().get("receptor");
                    titulo = getString(R.string.sugerencia_titulo);
                    contenido = grupo + getString(R.string.sugerencia_contenido) + actividad;
                    subtexto = "";
                } else if(function.equals("rechazar")) { // Se ha rechazado la sugerencia de la actividad
                    titulo = getString(R.string.rechazar_titulo);
                    contenido = getString(R.string.rechazar_contenido) + actividad;
                    subtexto = "";
                } else if(function.equals("aceptar_grupo")) { // Notificar al grupo que su sugerencia ha sido aceptada
                    titulo = getString(R.string.aceptar_grupo_titulo);
                    contenido = getString(R.string.aceptar_contenido_grupo) + actividad;
                    subtexto = "";
                } else if(function.equals("aceptar_general")) { // Notificar a los usuarios que una nueva actividad ha sido creada
                    titulo = getString(R.string.crear_titulo);
                    contenido = getString(R.string.crear_contenido) + actividad;
                    subtexto = "";
                }

                // Configurar el texto del comentario
                elBuilder.setSmallIcon(android.R.drawable.ic_menu_send)
                        .setContentTitle(titulo)
                        .setContentText(contenido)
                        .setSubText(subtexto)
                        .setVibrate(new long[]{0, 1000, 500, 1000})
                        .setAutoCancel(true);


                elManager.notify(1, elBuilder.build());

            }
        }
    }
}
