package com.example.hachem.capturcamera;


        import android.app.Activity;
        import android.content.pm.ActivityInfo;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.Canvas;
        import android.graphics.Matrix;
        import android.graphics.Paint;
        import android.graphics.PixelFormat;
        import android.graphics.PointF;
        import android.graphics.Bitmap.Config;
        import android.hardware.Camera;
        import android.hardware.Camera.PictureCallback;
        import android.media.FaceDetector;
        import android.media.FaceDetector.Face;
        import android.os.Bundle;
        import android.view.MotionEvent;
        import android.view.SurfaceHolder;
        import android.view.SurfaceView;
        import android.view.Window;
        import android.view.WindowManager;
        import android.widget.FrameLayout;
        import android.widget.ImageView;

public class Main extends Activity
        implements SurfaceHolder.Callback {

    private Camera 		camera;
    private FrameLayout layout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(null);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        SurfaceView view = new SurfaceView(this);
        layout = new FrameLayout(this);
        view.getHolder().addCallback(this);
        view.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        layout.addView(view);
        setContentView(layout);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                PictureCallback jpegCallback = new PictureCallback() {
                    public void onPictureTaken(byte[] _data, Camera _camera) {

                        // Load the picture
                        //final Bitmap src = BitmapFactory.decodeFile(Main.this.getFilesDir()+"/image.jpg");
                        final Bitmap src = BitmapFactory.decodeByteArray(_data, 0, _data.length);
                        final Bitmap dst = Bitmap.createBitmap(src.getWidth()/4, src.getHeight()/4, Config.RGB_565);
                        final Canvas cnv = new Canvas(dst);
                        rotateBitmap(cnv,dst,src,90,0.25f);

                        // Load faces
                        final int			MAX		= 2;
                        final int[]			clr		= {0x80ff0000,0x8000ff00};
                        final Face[]		faces  	= new Face[MAX];
                        final FaceDetector	detect 	= new FaceDetector(dst.getWidth(), dst.getHeight(), MAX);
                        final int			n 		= detect.findFaces(dst, faces);
                        final PointF 		pt 		= new PointF();
                        final Paint			brush	= new Paint();
                        for(int i=0; i<n; i++){
                            final Face f = faces[i];
                            f.getMidPoint(pt);
                            brush.setColor(clr[i%clr.length]);
                            cnv.drawCircle(pt.x, pt.y, f.eyesDistance(), brush);
                        }

                        final Bitmap copy = Bitmap.createBitmap(dst);
                        rotateBitmap(cnv,dst,copy,-90,1);

                        // Set the view
                        final ImageView im = new ImageView(Main.this);
                        im.setImageBitmap(dst);

                        layout.removeAllViews();
                        layout.addView(im);

                    }
                };
                camera.takePicture(null, null, jpegCallback);
                break;
        }
        return super.onTouchEvent(event);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera = Camera.open();
            camera.setPreviewDisplay(holder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPictureFormat(PixelFormat.JPEG);
        //parameters.setPreviewSize(width, height);
        parameters.setPreviewSize(480,320);
        camera.setParameters(parameters);
        camera.startPreview();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    private final static void rotateBitmap(Canvas cnv, Bitmap dst, Bitmap src, int angle, float scale){
        final Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        matrix.postScale(scale,scale);
        matrix.postTranslate(dst.getWidth()/2, dst.getHeight()/2);
        matrix.preTranslate(-src.getWidth()/2,-src.getHeight()/2);
        cnv.drawBitmap(src,matrix,null);
    }

    //private void savePicture(byte[] data){
    //	try {
    //		final File fs = new File(getFilesDir()+"/image.jpg");
    //		final FileOutputStream fos = new FileOutputStream(fs);
    //		fos.write(data);
    //		fos.flush();
    //		fos.close();
    //	} catch (Exception e) {
    //		e.printStackTrace();
    //	}
    //}
}