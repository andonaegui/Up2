package com.kosalgeek.android.uploadimagetoserver;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.kosalgeek.android.photoutil.CameraPhoto;
import com.kosalgeek.android.photoutil.GalleryPhoto;
import com.kosalgeek.android.photoutil.ImageBase64;
import com.kosalgeek.android.photoutil.ImageLoader;
import com.kosalgeek.genasync12.AsyncResponse;
import com.kosalgeek.genasync12.EachExceptionsHandler;
import com.kosalgeek.genasync12.PostResponseAsyncTask;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    //Iniciando la variable para obtener un Log.d() en la variable TAG en OnActivityResult
    private final String TAG = this.getClass().getName();

    //Declarando los Image View de los iconos
    ImageView ivCamera, ivGallery, ivUpload, ivImage;

    //Declarando la libreria de photoutil y G.Photo del author
    CameraPhoto cameraPhoto;
    GalleryPhoto galleryPhoto;

    //Este es un numero cualquiera para cumplir con los parametros pedidos en el camara request
    final int CAMERA_REQUEST = 13323;
    final int GALLERY_REQUEST = 22131;

    //Declaramos esta variable para utilizarla en el ivUpload.setOnclickListener
    String selectedPhoto;

    //Declaracion del tipo editText para luego editar la ip en patantalla
    EditText etIpAddress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Este es el icono del correo o snackbar que al apretarlo muestra ese text
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //Para editar la barra donde pondremos nuestro ip
        etIpAddress = (EditText)findViewById(R.id.etIpAddress);

        //El contexto podria ser this
        cameraPhoto = new CameraPhoto(getApplicationContext());
        galleryPhoto = new GalleryPhoto(getApplicationContext());

        //Con r.id.ivCamara buscamos por el id el elemento en el content_main.xml
        ivImage = (ImageView)findViewById(R.id.ivImage);
        ivCamera = (ImageView)findViewById(R.id.ivCamera);
        ivGallery = (ImageView)findViewById(R.id.ivGallery);
        ivUpload = (ImageView)findViewById(R.id.ivUpload);

        //Ahora generamos un evento para la camara
        ivCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Aqui usaremos una libreria sugerida por el creador Oum.
                  Existe un gran codigo atras de esta libreria, basicamente es para usar la camara.
                  Cuando abres la camara y luego la cierras quieres un resultado de vuelta, esto
                  lo haremos afuera del onCreate.
                  Esto es startActivity FOR RESULT*/
                try {
                    startActivityForResult(cameraPhoto.takePhotoIntent(), CAMERA_REQUEST);
                    cameraPhoto.addToGallery();
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(),
                            "Something Wrong while taking photos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ivGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Al clickear el gallery iniciamos la actividad y utilizando Gallery Request
                startActivityForResult(galleryPhoto.openGalleryIntent(), GALLERY_REQUEST);
            }
        });

        ivUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(selectedPhoto == null || selectedPhoto.equals("")){
                    Toast.makeText(getApplicationContext(), "No Image Selected.", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    //Convertimos a bitmap usando el loader de Oum, pero mas grande (depende de los requerimientos)
                    Bitmap bitmap = ImageLoader.init().from(selectedPhoto).requestSize(1024, 1024).getBitmap();
                    //Encodeamos ese bitmap a un string usando la libreria de Oum
                    String encodedImage = ImageBase64.encode(bitmap);
                    //Creamos un log de dicho string para verlo en consola (es gigante)
                    Log.d(TAG, encodedImage);

                    //El hashmap es como un arreglo
                    HashMap<String, String> postData = new HashMap<String, String>();
                    //El parametro "image" tiene que ser el mismo que en el archivo.php
                    postData.put("image", encodedImage);

                    //De la libreria de Oum
                    PostResponseAsyncTask task = new PostResponseAsyncTask(MainActivity.this, postData, new AsyncResponse() {
                        @Override
                        public void processFinish(String s) {
                            Log.d(TAG, s);
                            //Si contiene ese string en algun lado. Es diferente a == x
                            if(s.contains("uploaded_success")){
                                Toast.makeText(getApplicationContext(), "Image Uploaded Successfully.",
                                        Toast.LENGTH_SHORT).show();

                            }
                            else{
                                Toast.makeText(getApplicationContext(), "Error while uploading.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    //Para modificar la barra de ip en la pantalla inicial
                    String ip = etIpAddress.getText().toString();
                    //Este es mi localhost en la carpeta news esta el .php
                    task.execute("http://"+ip+"/news/upload.php");

                    //Distintas excepciones.
                    task.setEachExceptionsHandler(new EachExceptionsHandler() {
                        @Override
                        public void handleIOException(IOException e) {
                            Toast.makeText(getApplicationContext(), "Cannot Connect to Server.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void handleMalformedURLException(MalformedURLException e) {
                            Toast.makeText(getApplicationContext(), "URL Error.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void handleProtocolException(ProtocolException e) {
                            Toast.makeText(getApplicationContext(), "Protocol Error.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void handleUnsupportedEncodingException(UnsupportedEncodingException e) {
                            Toast.makeText(getApplicationContext(), "Encoding Error.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (FileNotFoundException e) {
                    Toast.makeText(getApplicationContext(),
                            "Something Wrong while encoding photos", Toast.LENGTH_SHORT).show();
                    }

            }
        });

    }

    // Este es el result que obtenemos de la camara
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == CAMERA_REQUEST){
                String photoPath = cameraPhoto.getPhotoPath();
                //Obtenido de la libreria de Oum para poner la imagen como preview en la estrella
                selectedPhoto = photoPath;
                Bitmap bitmap = null;
                try {
                    //modifica la imagen para que no sea tan grande en el recuadro
                    bitmap = ImageLoader.init().from(photoPath).requestSize(512, 512).getBitmap();
                    ivImage.setImageBitmap(getRotatedBitmap(bitmap, 90));
                } catch (FileNotFoundException e) {
                    Toast.makeText(getApplicationContext(),
                            "Something Wrong while loading photos", Toast.LENGTH_SHORT).show();
                }

            }
            else if(requestCode == GALLERY_REQUEST){
                Uri uri = data.getData();

                galleryPhoto.setPhotoUri(uri);
                String photoPath = galleryPhoto.getPath();
                selectedPhoto = photoPath;
                try {
                    Bitmap bitmap = ImageLoader.init().from(photoPath).requestSize(512, 512).getBitmap();
                    ivImage.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    Toast.makeText(getApplicationContext(),
                            "Something Wrong while choosing photos", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    //9re exifutil.java - Libreria para rotar la imagen que reemplaza la estrella
    private Bitmap getRotatedBitmap(Bitmap source, float angle){
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap bitmap1 = Bitmap.createBitmap(source,
                0, 0, source.getWidth(), source.getHeight(), matrix, true);
        return bitmap1;
    }
}
