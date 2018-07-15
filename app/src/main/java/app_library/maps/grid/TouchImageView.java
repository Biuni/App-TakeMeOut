package app_library.maps.grid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.support.v7.widget.AppCompatImageView;
import android.widget.Toast;

import com.example.user.progetto_ids.R;

import java.util.ArrayList;

import app_library.MainApplication;


// Classe che permette di gestire le gesture di pitch e zoom nella mappa
public class TouchImageView extends AppCompatImageView {

    Matrix matrix;

    // ci possono essere 3 stati
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;

    // stato corrente
    int mode = NONE;

    // parametri per lo zoom
    PointF last = new PointF();
    PointF start = new PointF();
    float minScale = 1f;
    float maxScale = 3f;
    float[] m;
    int viewWidth, viewHeight;

    static final int CLICK = 3;

    float saveScale = 1f;

    protected float origWidth, origHeight;

    int oldMeasuredWidth, oldMeasuredHeight;

    ScaleGestureDetector mScaleDetector;

    Context context;

    // immagini bitmap degli elemnti sulla mappa
    /*private Bitmap bitmapUserCurrentPosition;
    private Bitmap bitmapDestination;
    private Bitmap bitmapNode;

    // immagine bitmap riferita al piano corrente
    private Bitmap bitmapMapCurrentFloor;

    // piano e stanza di destinazione
    private String destinationFloor;

    // piano e stanza correnti
    private String userCurrentFloor;
    private String userCurrentRoom;

    // percorso costituito da un insieme di nodi da seguire sulla mappa
    private String[] nodePathStartEndArray;

    // identificativo della risorsa drawable immagine bitmap riferita al piano corrente
    private int mapResId;

    // opzioni bitmap
    private BitmapFactory.Options options;

    // pennello per il disegno del percorso
    private Paint paint;

    // booleano per indicare che il percorso da seguire è stato completato
    private boolean pathCompleted;

    private boolean mapElementBitmapInitialized = false;

    private int canvasWidth;
    private int canvasHeight;*/


    // costruttori
    public TouchImageView(Context context) {
        super(context);
        sharedConstructing(context);

        /*this.options = new BitmapFactory.Options();
        this.options.inDither = true;
        this.options.inScaled = false;
        this.options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        this.paint = new Paint();
        this.paint.setAntiAlias(true);
        this.paint.setColor(Color.RED);
        this.paint.setStrokeWidth(6);

        this.pathCompleted = false;*/
    }

    public TouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConstructing(context);
    }

    // inizializzazione degli eventi toccando la mappa
    private void sharedConstructing(Context context) {

        super.setClickable(true);

        this.context = context;

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        matrix = new Matrix();

        m = new float[9];

        setImageMatrix(matrix);

        setScaleType(ScaleType.MATRIX);

        setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                mScaleDetector.onTouchEvent(event);

                PointF curr = new PointF(event.getX(), event.getY());

                // si va a vedere il tipo di azione agendo opportunamente
                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:

                        last.set(curr);

                        start.set(last);

                        mode = DRAG;

                        break;

                    case MotionEvent.ACTION_MOVE:

                        if (mode == DRAG) {

                            float deltaX = curr.x - last.x;

                            float deltaY = curr.y - last.y;

                            float fixTransX = getFixDragTrans(deltaX, viewWidth, origWidth * saveScale);

                            float fixTransY = getFixDragTrans(deltaY, viewHeight, origHeight * saveScale);

                            matrix.postTranslate(fixTransX, fixTransY);

                            fixTrans();

                            last.set(curr.x, curr.y);

                        }

                        break;

                    case MotionEvent.ACTION_UP:

                        mode = NONE;

                        int xDiff = (int) Math.abs(curr.x - start.x);

                        int yDiff = (int) Math.abs(curr.y - start.y);

                        if (xDiff < CLICK && yDiff < CLICK)

                            performClick();

                        break;

                    case MotionEvent.ACTION_POINTER_UP:

                        mode = NONE;

                        break;

                }

                setImageMatrix(matrix);

                invalidate();

                return true;

            }

        });
    }

    public void setMaxZoom(float x) {

        maxScale = x;

    }

    /*public void setMapOnDrawInfo(String destinationFloor, String userCurrentFloor, String userCurrentRoom, String[] nodePathStartEndArray, int mapResId)
    {
        this.destinationFloor = destinationFloor;
        this.userCurrentFloor = userCurrentFloor;
        this.userCurrentRoom = userCurrentRoom;

        this.nodePathStartEndArray = new String[nodePathStartEndArray.length];

        for (int i = 0; i < nodePathStartEndArray.length; i++)
            this.nodePathStartEndArray[i] = nodePathStartEndArray[i];

        //this.mapResId = mapResId;

        if (this.mapElementBitmapInitialized)
            this.bitmapMapCurrentFloor = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), mapResId, options), canvasWidth, canvasHeight, true);
        else
            this.mapResId = mapResId;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        if (!this.mapElementBitmapInitialized)
        {
            this.canvasWidth = canvas.getWidth();
            this.canvasHeight = canvas.getHeight();
            this.bitmapUserCurrentPosition = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.map_user_pos, options), (int)(((double)canvas.getWidth() / 100) * 4), (int)(((double)canvas.getHeight() / 100) * 4), true);
            this.bitmapDestination = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.map_dest_pos, options), (int)(((double)canvas.getWidth() / 100) * 4), (int)(((double)canvas.getHeight() / 100) * 4), true);
            this.bitmapNode = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.map_node_pos, options), (int)(((double)canvas.getWidth() / 100) * 4), (int)(((double)canvas.getHeight() / 100) * 4), true);
            this.bitmapMapCurrentFloor = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), this.mapResId, options), canvas.getWidth(), canvas.getHeight(), true);
            this.mapElementBitmapInitialized = true;
        }


        // bitmap su cui mettere gli elementi
        canvas.drawBitmap(bitmapMapCurrentFloor, 0, 0, null);

        // si filtra del percorso da seguire solamente i nodi del piano corrente
        ArrayList<String> listRoomPathCurrentFloor = new ArrayList<>();

        for (int i = 0; i < nodePathStartEndArray.length; i++)
        {
            if (nodePathStartEndArray[i].startsWith(userCurrentFloor))
                listRoomPathCurrentFloor.add(nodePathStartEndArray[i]);
        }

        // bluetooth non attivo o emergenza e condividono il fatto di avere un percorso sorgente-destinazione già stabilito per il piano
        if (!MainApplication.controlBluetooth() || MainApplication.getEmergency())
        {
            // sono recuperate le coordinate x e y della stanza coorente
            int[] currentUserCoords = MainApplication.getFloors().get(userCurrentFloor).getNodes().get(userCurrentRoom).getCoords();

            currentUserCoords[0] = ((canvas.getWidth() * currentUserCoords[0]) / 1000);
            currentUserCoords[1] = ((canvas.getHeight() * currentUserCoords[1]) / 1600);

            // disegno dell'immagine della posizione corrente sulla mappa
            canvas.drawBitmap(bitmapUserCurrentPosition, currentUserCoords[0], currentUserCoords[1],null);

            // il numero di nodi del percorso filtrato è superiore a 2
            if (listRoomPathCurrentFloor.size() >= 2)
            {
                // si recupera la x e y del nodo precedente
                int previousX = currentUserCoords[0];
                int previousY = currentUserCoords[1];

                // si scorrono i nodi del percorso per disegnarli sulla mappa e i relativi archi per formare il percorso
                for (int i = 1; i < listRoomPathCurrentFloor.size(); i++)
                {
                    // coordinate x e y nodo
                    int[] currentNodeCoords = MainApplication.getFloors().get(userCurrentFloor).getNodes().get(listRoomPathCurrentFloor.get(i)).getCoords();

                    currentNodeCoords[0] = ((canvas.getWidth() * currentNodeCoords[0]) / 1000);
                    currentNodeCoords[1] = ((canvas.getHeight() * currentNodeCoords[1]) / 1600);

                    // se il nodo è l'ultimo del percorso filtrato utilizzo l'immagine della destinazione altrimenti di un nodo
                    if (i == (listRoomPathCurrentFloor.size() - 1))
                        canvas.drawBitmap(bitmapDestination, currentNodeCoords[0], currentNodeCoords[1],null);
                    else
                        canvas.drawBitmap(bitmapNode, currentNodeCoords[0], currentNodeCoords[1],null);

                    //canvas.drawCircle(currentNodeCoords[0], currentNodeCoords[1], 20, paint);

                    // disegno l'arco tra il nodo precedente e attuale
                    canvas.drawLine(previousX, previousY, currentNodeCoords[0], currentNodeCoords[1], paint);

                    previousX = currentNodeCoords[0];
                    previousY = currentNodeCoords[1];
                }
            }

            // non sono sul piano della destinazione e non sono in emergenza ho l'opzione per cambiare piano
            if (!userCurrentFloor.equals(destinationFloor) && !MainApplication.getEmergency())
                Toast.makeText(context, "Raggiunta la destinazione cambia piano con l'apposita voce nel menu", Toast.LENGTH_LONG).show();

            // sono sul piano della destinazione e non sono in emergenza e indico che sono sul piano della destinazione
            if (userCurrentFloor.equals(destinationFloor) && !MainApplication.getEmergency())
                Toast.makeText(context, "Hai raggiunto il piano della destinazione", Toast.LENGTH_LONG).show();

            // se sono in emergenza, ho solo un nodo nel percorso, piano coorente e destinazione coincidono e non ho ancora completato il percorso allora mostro il messaggio che sono arrivato alla posizione sicura
            if (MainApplication.getEmergency() && listRoomPathCurrentFloor.size() == 1 && userCurrentFloor.equals(destinationFloor) && !pathCompleted)
            {
                pathCompleted = true;
                Toast.makeText(context, "Hai raggiunto la posizione sicura", Toast.LENGTH_LONG).show();
            }
        }
        // bluetooth attivo e si visualizza il percorso rimanente per il piano
        else
        {
            // si filtra ulteriormente dal percorso per il piano corrente solamente i nodi che rimangono da percorrere
            ArrayList<String> listRemainingRoomPathCurrentFloor = new ArrayList<>();

            // booleano che indica la stanza attuale in cui si trova è stata trovata
            boolean currentRoomFound = false;

            for (int i = 0; i < listRoomPathCurrentFloor.size(); i++)
            {
                // la stanza non è stata ancora trovata
                if (!currentRoomFound)
                {
                    // la stanza della lista coincide con quella della posizione corrente allora da ora prendo tutti i nodi rimanenti impostando il relativo booleano
                    if (listRoomPathCurrentFloor.get(i).equals(userCurrentFloor))
                    {
                        currentRoomFound = true;
                        listRemainingRoomPathCurrentFloor.add(listRoomPathCurrentFloor.get(i));
                    }
                }
                // aggiungo tutti i nodi
                else
                    listRemainingRoomPathCurrentFloor.add(listRoomPathCurrentFloor.get(i));
            }

            // sono recuperate le coordinate x e y della stanza coorente
            int[] currentUserCoords = MainApplication.getFloors().get(userCurrentFloor).getNodes().get(userCurrentRoom).getCoords();

            currentUserCoords[0] = ((canvas.getWidth() * currentUserCoords[0]) / 1000);
            currentUserCoords[1] = ((canvas.getHeight() * currentUserCoords[1]) / 1600);


            // disegno dell'immagine della posizione corrente sulla mappa
            canvas.drawBitmap(bitmapUserCurrentPosition, currentUserCoords[0], currentUserCoords[1],null);

            // il numero di nodi del percorso filtrato due volte è superiore a 2
            if (listRemainingRoomPathCurrentFloor.size() >= 2)
            {
                // coordinate x e y nodo
                int previousX = currentUserCoords[0];
                int previousY = currentUserCoords[1];

                // si scorrono i nodi del percorso per disegnarli sulla mappa e i relativi archi per formare il percorso
                for (int i = 1; i < listRemainingRoomPathCurrentFloor.size(); i++)
                {
                    // coordinate x e y nodo
                    int[] currentNodeCoords = MainApplication.getFloors().get(userCurrentFloor).getNodes().get(listRemainingRoomPathCurrentFloor.get(i)).getCoords();

                    currentNodeCoords[0] = ((canvas.getWidth() * currentNodeCoords[0]) / 1000);
                    currentNodeCoords[1] = ((canvas.getHeight() * currentNodeCoords[1]) / 1600);

                    // se il nodo è l'ultimo del percorso filtrato utilizzo l'immagine della destinazione altrimenti di un nodo
                    if (i == (listRemainingRoomPathCurrentFloor.size() - 1))
                        canvas.drawBitmap(bitmapDestination, currentNodeCoords[0], currentNodeCoords[1],null);
                    else
                        canvas.drawBitmap(bitmapNode, currentNodeCoords[0], currentNodeCoords[1],null);

                    //canvas.drawCircle(currentNodeCoords[0], currentNodeCoords[1], 20, paint);

                    // disegno l'arco tra il nodo precedente e attuale
                    canvas.drawLine(previousX, previousY, currentNodeCoords[0], currentNodeCoords[1], paint);

                    previousX = currentNodeCoords[0];
                    previousY = currentNodeCoords[1];
                }
            }


            // se ho solo un nodo nel percorso, piano coorente e destinazione coincidono e non ho ancora completato il percorso allora mostro il messaggio che sono arrivato alla destinazione
            if (listRemainingRoomPathCurrentFloor.size() == 1 && userCurrentFloor.equals(destinationFloor) && !pathCompleted)
            {
                pathCompleted = true;
                Toast.makeText(context, "Hai raggiunto la destinazione", Toast.LENGTH_LONG).show();
            }
        }
    }*/

    // inizializzazione degli eventi di zoom sulla mappa
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {

            mode = ZOOM;

            return true;

        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            float mScaleFactor = detector.getScaleFactor();

            float origScale = saveScale;

            saveScale *= mScaleFactor;

            if (saveScale > maxScale) {

                saveScale = maxScale;

                mScaleFactor = maxScale / origScale;

            } else if (saveScale < minScale) {

                saveScale = minScale;

                mScaleFactor = minScale / origScale;

            }

            if (origWidth * saveScale <= viewWidth || origHeight * saveScale <= viewHeight)

                matrix.postScale(mScaleFactor, mScaleFactor, viewWidth / 2, viewHeight / 2);

            else

                matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());

            fixTrans();

            return true;

        }

    }

    // applicazione della translazione della matrice di pixel
    void fixTrans() {

        matrix.getValues(m);

        float transX = m[Matrix.MTRANS_X];

        float transY = m[Matrix.MTRANS_Y];

        float fixTransX = getFixTrans(transX, viewWidth, origWidth * saveScale);

        float fixTransY = getFixTrans(transY, viewHeight, origHeight * saveScale);

        if (fixTransX != 0 || fixTransY != 0)

            matrix.postTranslate(fixTransX, fixTransY);

    }

    // si ottiene la translazione di default della matrice di pixel
    float getFixTrans(float trans, float viewSize, float contentSize) {

        float minTrans, maxTrans;

        if (contentSize <= viewSize) {

            minTrans = 0;

            maxTrans = viewSize - contentSize;

        } else {

            minTrans = viewSize - contentSize;

            maxTrans = 0;

        }

        if (trans < minTrans)

            return -trans + minTrans;

        if (trans > maxTrans)

            return -trans + maxTrans;

        return 0;

    }

    // si ottiene la translazione di drag default della matrice di pixel
    float getFixDragTrans(float delta, float viewSize, float contentSize) {

        if (contentSize <= viewSize) {

            return 0;

        }

        return delta;

    }

    // evento attivato alla variazione dello stato attuale della mappa
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        viewWidth = MeasureSpec.getSize(widthMeasureSpec);

        viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        // si riscala l'immagine alla rotazione
        if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight

                || viewWidth == 0 || viewHeight == 0)

            return;

        oldMeasuredHeight = viewHeight;

        oldMeasuredWidth = viewWidth;

        if (saveScale == 1) {

            // adattamento allo schermo

            float scale;

            Drawable drawable = getDrawable();

            if (drawable == null || drawable.getIntrinsicWidth() == 0 || drawable.getIntrinsicHeight() == 0)

                return;

            int bmWidth = drawable.getIntrinsicWidth();

            int bmHeight = drawable.getIntrinsicHeight();

            Log.d("bmSize", "bmWidth: " + bmWidth + " bmHeight : " + bmHeight);

            float scaleX = (float) viewWidth / (float) bmWidth;

            float scaleY = (float) viewHeight / (float) bmHeight;

            scale = Math.min(scaleX, scaleY);

            matrix.setScale(scale, scale);

            // centrata l'immagine

            float redundantYSpace = (float) viewHeight - (scale * (float) bmHeight);

            float redundantXSpace = (float) viewWidth - (scale * (float) bmWidth);

            redundantYSpace /= (float) 2;

            redundantXSpace /= (float) 2;

            matrix.postTranslate(redundantXSpace, redundantYSpace);

            origWidth = viewWidth - 2 * redundantXSpace;

            origHeight = viewHeight - 2 * redundantYSpace;

            setImageMatrix(matrix);

        }

        fixTrans();

    }

}