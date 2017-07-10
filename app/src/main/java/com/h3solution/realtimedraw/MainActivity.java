package com.h3solution.realtimedraw;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.h3solution.model.DrawPath;
import com.h3solution.model.DrawPoint;
import com.h3solution.sensor.ShakeSensorEventListener;
import com.h3solution.view.PencilView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final int EDGE_WIDTH = 683;
    private final String TAG = getClass().getSimpleName();

    @BindView(R.id.surface_view)
    SurfaceView surfaceView;

    private double ratio = -1;
    private double marginLeft;
    private double marginTop;
    private String currentColor = "Charcoal";
    private DrawPath currentPath;
    private PencilView currentPencil;
    private HashMap<String, Integer> nameToColorMap = new HashMap<>();
    private HashMap<Integer, String> colorIdToName = new HashMap<>();

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private ShakeSensorEventListener shakeSensorEventListener;

    FirebaseDatabase database;
    DatabaseReference myRef;
    ArrayList<DrawPath> allPath;

    String currentKey;
    DatabaseReference pointRef;
    DatabaseReference pushRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initEvents();
    }

    private void initEvents() {
        surfaceView.getHolder().addCallback(MainActivity.this);

        generateColorMap();

        currentPencil = (PencilView) findViewById(R.id.charcoal);
        currentPencil.setSelected(true);

        initializeShakeSensor();

        allPath = new ArrayList<>();

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        progressDialog.show();

        pushRef = database.getReference("paper").push();
        DatabaseReference paper = database.getReference("paper");
        paper.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                allPath = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    DrawPath drawPath = new DrawPath();
                    drawPath.setColor((String) snapshot.child("color").getValue());

                    ArrayList<DrawPoint> points = new ArrayList<>();
                    for (DataSnapshot aPoint : snapshot.child("points").getChildren()) {
                        points.add(new DrawPoint((Double) aPoint.child("x").getValue(), (Double) aPoint.child("y").getValue()));
                    }
                    drawPath.setPoints(points);
                    allPath.add(drawPath);
                }
                draw(allPath);
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "onCancelled");
            }
        });
    }

    private void generateColorMap() {
        nameToColorMap.put("Charcoal", 0xff1c283f);
        nameToColorMap.put("Elephant", 0xff9a9ba5);
        nameToColorMap.put("Dove", 0xffebebf2);
        nameToColorMap.put("Ultramarine", 0xff39477f);
        nameToColorMap.put("Indigo", 0xff59569e);
        nameToColorMap.put("GrapeJelly", 0xff9a50a5);
        nameToColorMap.put("Mulberry", 0xffd34ca3);
        nameToColorMap.put("Flamingo", 0xfffe5192);
        nameToColorMap.put("SexySalmon", 0xfff77c88);
        nameToColorMap.put("Peach", 0xfffc9f95);
        nameToColorMap.put("Melon", 0xfffcc397);
        colorIdToName.put(R.id.charcoal, "Charcoal");
        colorIdToName.put(R.id.elephant, "Elephant");
        colorIdToName.put(R.id.dove, "Dove");
        colorIdToName.put(R.id.ultramarine, "Ultramarine");
        colorIdToName.put(R.id.indigo, "Indigo");
        colorIdToName.put(R.id.grape_jelly, "GrapeJelly");
        colorIdToName.put(R.id.mulberry, "Mulberry");
        colorIdToName.put(R.id.flamingo, "Flamingo");
        colorIdToName.put(R.id.sexy_salmon, "SexySalmon");
        colorIdToName.put(R.id.peach, "Peach");
        colorIdToName.put(R.id.melon, "Melon");
    }

    private void wipeCanvas() {
        myRef.removeValue();
    }

    @OnClick({R.id.charcoal, R.id.elephant, R.id.indigo, R.id.dove, R.id.ultramarine, R.id.grape_jelly, R.id.mulberry, R.id.sexy_salmon, R.id.peach, R.id.flamingo, R.id.melon})
    public void onViewClicked(View view) {
        String colorName = colorIdToName.get(view.getId());
        if (colorName == null) {
            return;
        }
        currentColor = colorName;
        if (view instanceof PencilView) {
            currentPencil.setSelected(false);
            currentPencil.invalidate();
            PencilView pencil = (PencilView) view;
            pencil.setSelected(true);
            pencil.invalidate();
            currentPencil = pencil;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(shakeSensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(shakeSensorEventListener);
    }

    private void initializeShakeSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        shakeSensorEventListener = new ShakeSensorEventListener();
        shakeSensorEventListener.setOnShakeListener(new ShakeSensorEventListener.OnShakeListener() {
            @Override
            public void onShake(int count) {
                wipeCanvas();
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        ratio = -1;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        boolean isPortrait = width < height;
        if (isPortrait) {
            ratio = (double) EDGE_WIDTH / height;
        } else {
            ratio = (double) EDGE_WIDTH / width;
        }
        if (isPortrait) {
            marginLeft = (width - height) / 2.0;
            marginTop = 0;
        } else {
            marginLeft = 0;
            marginTop = (height - width) / 2.0;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int[] viewLocation = new int[2];
        surfaceView.getLocationInWindow(viewLocation);
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN
                || action == MotionEvent.ACTION_MOVE
                || action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_CANCEL) {
            float x = event.getRawX();
            float y = event.getRawY();
            double pointX = (x - marginLeft - viewLocation[0]) * ratio;
            double pointY = (y - marginTop - viewLocation[1]) * ratio;

            if (action == MotionEvent.ACTION_DOWN) {
                currentKey = pushRef.getKey();
                pointRef = database.getReference("paper/" + currentKey + "/points");

                currentPath = new DrawPath();
                currentPath.setColor(currentColor);
                DrawPoint point = new DrawPoint();
                point.setX(pointX);
                point.setY(pointY);
                currentPath.getPoints().add(new DrawPoint(pointX, pointY));
                pushRef.setValue(currentPath);
            } else if (action == MotionEvent.ACTION_MOVE) {
                DrawPoint point = new DrawPoint();
                point.setX(pointX);
                point.setY(pointY);
                currentPath.getPoints().add(point);
                pointRef.push().setValue(point);
            } else if (action == MotionEvent.ACTION_UP) {
                DrawPoint point = new DrawPoint();
                point.setX(pointX);
                point.setY(pointY);
                currentPath.getPoints().add(point);
                pointRef.push().setValue(point);
                currentPath = null;
                pushRef = database.getReference("paper").push();
            }
            return true;
        }
        return false;
    }

    private void draw(ArrayList<DrawPath> results) {
        Canvas canvas = null;

        try {
            final SurfaceHolder holder = surfaceView.getHolder();
            canvas = holder.lockCanvas();
            canvas.drawColor(Color.WHITE);
        } finally {
            if (canvas != null) {
                surfaceView.getHolder().unlockCanvasAndPost(canvas);
            }
        }

        try {
            final SurfaceHolder holder = surfaceView.getHolder();
            canvas = holder.lockCanvas();

            synchronized (holder) {
                canvas.drawColor(Color.WHITE);
                final Paint paint = new Paint();
                for (DrawPath drawPath : results) {
                    ArrayList<DrawPoint> points = drawPath.getPoints();
                    final Integer color = nameToColorMap.get(drawPath.getColor());
                    if (color != null) {
                        paint.setColor(color);
                    } else {
                        paint.setColor(nameToColorMap.get(currentColor));
                    }
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth((float) (4 / ratio));
                    final Iterator<DrawPoint> iterator = points.iterator();
                    final DrawPoint firstPoint = iterator.next();
                    final Path path = new Path();
                    final float firstX = (float) ((firstPoint.getX() / ratio) + marginLeft);
                    final float firstY = (float) ((firstPoint.getY() / ratio) + marginTop);
                    path.moveTo(firstX, firstY);
                    while(iterator.hasNext()) {
                        DrawPoint point = iterator.next();
                        final float x = (float) ((point.getX() / ratio) + marginLeft);
                        final float y = (float) ((point.getY() / ratio) + marginTop);
                        path.lineTo(x, y);
                    }
                    canvas.drawPath(path, paint);
                }
            }
        } finally {
            if (canvas != null) {
                surfaceView.getHolder().unlockCanvasAndPost(canvas);
            }
        }
    }
}