package app.com.unifyid;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.frosquivel.magicaltakephoto.MagicalTakePhoto;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import app.com.unifyid.R;

public class MainActivity extends AppCompatActivity {

    Button mTakePictures;
    static Handler h = new Handler();
    int delay = 500; //0.5 seconds
    static Runnable runnable;
    Uri file;

    static int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTakePictures = (Button) findViewById(R.id.btnTakePictures);

        mTakePictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                file = Uri.fromFile(getOutputMediaFile());
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, file);
//
//                startActivityForResult(intent, 100);
                h.postDelayed(new Runnable() {
                    public void run() {
                        //do something
                        final SurfaceTexture surfaceTexture = new SurfaceTexture(0);
                        final Camera cam = openFrontCamera(getApplicationContext());
                        if (cam != null) {
                            try {


                                System.out.println("in here");

                                try {
                                    cam.setPreviewTexture(surfaceTexture);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                cam.startPreview();
                                runnable = this;
                                cam.takePicture(null, null, mPicture);
                                h.postDelayed(runnable, delay);


                                //Do something after 100ms


                            } catch (Exception ex) {
                                Log.d("Cam", "Can't take picture!");
                            }
                        }
                    }
                }, delay);
            }
        });


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            mTakePictures.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                mTakePictures.setEnabled(true);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
//                imageView.setImageURI(file);
                Toast.makeText(MainActivity.this, "Image taken", Toast.LENGTH_LONG).show();
            }
        }
    }

    private static Camera openFrontCamera(Context context) {
        try {
            boolean hasCamera = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
            if (hasCamera) {
                int cameraCount = 0;
                Camera cam = null;
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                cameraCount = Camera.getNumberOfCameras();
                for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
                    Camera.getCameraInfo(camIdx, cameraInfo);
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        try {
                            cam = Camera.open(camIdx);
                            Log.d("Cam", "cam is open");
                        } catch (RuntimeException e) {
                            Log.e("Cam", "Camera failed to open: " + e.getLocalizedMessage());
                        }
                    }
                }

                return cam;
            }
        } catch (Exception ex) {
            Log.d("Cam", "Can't open front camera");
        }

        return null;
    }

    private static Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            BitmapFactory.Options bfo = new BitmapFactory.Options();
            bfo.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(data), null, bfo);
            // Eye distance detection here and saving data
            String root = Environment.getExternalStorageDirectory().getAbsolutePath();
            File myDir = new File(root + "/saved_images");
            myDir.mkdirs();

            String fname = "Image" + i + ".jpg";
            i++;
            if (i >= 10) {
                h.removeCallbacks(runnable);
            }
            File file = new File(myDir, fname);
            if (file.exists()) file.delete();
            try {


                FileOutputStream out = new FileOutputStream(file);

                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
                FileInputStream fis = new FileInputStream(file);
                String fname1 = "encImage" + i;
                File file1 = new File(myDir, fname1);
                FileOutputStream out1 = new FileOutputStream(file1);
                SecretKeySpec sks = new SecretKeySpec("MyDifficultPassw".getBytes(), "AES");
                // Create cipher
                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.ENCRYPT_MODE, sks);
                // Wrap the output stream
                CipherOutputStream cos = new CipherOutputStream(out1, cipher);
                // Write bytes
                int b;
                byte[] d = new byte[8];
                while((b = fis.read(d)) != -1) {
                    cos.write(d, 0, b);
                }
                // Flush and close streams.
                cos.flush();
                cos.close();
                out.flush();
                out.close();
                file.delete();


            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("Cam", "Pic is taken");
            camera.stopPreview();
            camera.release();
        }
    };


}