/*     */ package kr.poturns.blink.demo.healthmanager.util;
/*     */ 
/*     */ import android.content.Context;
/*     */ import android.graphics.Bitmap;
/*     */ import android.graphics.BitmapFactory;
/*     */ import android.graphics.Canvas;
import android.graphics.Color;
/*     */ import android.graphics.DashPathEffect;
/*     */ import android.graphics.LinearGradient;
/*     */ import android.graphics.Matrix;
/*     */ import android.graphics.Paint;
/*     */ import android.graphics.Paint.Style;
/*     */ import android.graphics.Rect;
/*     */ import android.graphics.Shader.TileMode;
/*     */ import android.util.Log;
/*     */ import android.view.MotionEvent;
/*     */ import android.view.SurfaceHolder;
/*     */ import android.view.SurfaceHolder.Callback;
/*     */ import android.view.SurfaceView;

/*     */ import com.handstudio.android.hzgrapherlib.animation.GraphAnimation;
/*     */ import com.handstudio.android.hzgrapherlib.canvas.GraphCanvasWrapper;
/*     */ import com.handstudio.android.hzgrapherlib.error.ErrorCode;
/*     */ import com.handstudio.android.hzgrapherlib.error.ErrorDetector;
/*     */ import com.handstudio.android.hzgrapherlib.path.GraphPath;
/*     */ import com.handstudio.android.hzgrapherlib.vo.GraphNameBox;
/*     */ import com.handstudio.android.hzgrapherlib.vo.linegraph.LineGraph;
/*     */ import com.handstudio.android.hzgrapherlib.vo.linegraph.LineGraphVO;

/*     */ import java.util.List;
/*     */ import java.util.WeakHashMap;

/*     */ import com.handstudio.android.hzgrapherlib.graphview.IGraphView;

import android.graphics.Shader;

/*     */ public class CustomLineGraphView
/*     */   extends SurfaceView implements SurfaceHolder.Callback
/*     */ {
/*     */   public static final String TAG = "CustomLineGraphView";
/*     */   private SurfaceHolder mHolder;
/*     */   private DrawThread mDrawThread;
/*  36 */   private LineGraphVO mLineGraphVO = null;
/*     */   
/*     */ 
/*     */   public CustomLineGraphView(Context context, LineGraphVO vo)
/*     */   {
/*  41 */     super(context);
/*  42 */     this.mLineGraphVO = vo;
/*  43 */     initView(context, vo);
/*     */   }
/*     */   
/*     */   private void initView(Context context, LineGraphVO vo) {
/*  47 */     ErrorCode ec = ErrorDetector.checkGraphObject(vo);
/*  48 */     ec.printError();
/*     */     
/*  50 */     this.mHolder = getHolder();
/*  51 */     this.mHolder.addCallback(this);
/*     */   }
/*     */   
/*     */ 
/*     */ 
/*     */ 
/*     */   public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
/*     */   
/*     */ 
/*     */ 
/*     */   public void surfaceCreated(SurfaceHolder holder)
/*     */   {
/*  63 */     if (this.mDrawThread == null) {
/*  64 */       this.mDrawThread = new DrawThread(this.mHolder, getContext());
/*  65 */       this.mDrawThread.start();
/*     */     }
/*     */   }
/*     */   
/*     */   public void surfaceDestroyed(SurfaceHolder holder)
/*     */   {
/*  71 */     if (this.mDrawThread != null) {
/*  72 */       this.mDrawThread.setRunFlag(false);
/*  73 */       this.mDrawThread = null;
/*     */     }
/*     */   }
/*     */   
/*     */ 
/*  78 */   private static final Object touchLock = new Object();
/*     */   
/*     */   public boolean onTouchEvent(MotionEvent event) {
/*  81 */     int action = event.getAction();
/*     */     
/*  83 */     if (this.mDrawThread == null) {
/*  84 */       return false;
/*     */     }
/*     */     
/*  87 */     if (action == 0) {
/*  88 */       synchronized (touchLock) {
/*  89 */         this.mDrawThread.isDirty = true;
/*     */       }
/*  91 */       return true; }
/*  92 */     if (action == 2) {
/*  93 */       synchronized (touchLock) {
/*  94 */         this.mDrawThread.isDirty = true;
/*     */       }
/*  96 */       return true; }
/*  97 */     if (action == 1) {
/*  98 */       synchronized (touchLock) {
/*  99 */         this.mDrawThread.isDirty = true;
/*     */       }
/* 101 */       return true;
/*     */     }
/*     */     
/* 104 */     return super.onTouchEvent(event);
/*     */   }
/*     */   
/*     */   class DrawThread extends IGraphView.IDrawThread.Base
/*     */   {
/*     */     SurfaceHolder mHolder;
/*     */     Context mCtx;
/* 111 */     boolean isRun = true;
/* 112 */     boolean isDirty = true;
/*     */     
/* 114 */     Matrix matrix = new Matrix();
/*     */     
/* 116 */     int height = CustomLineGraphView.this.getHeight();
/* 117 */     int width = CustomLineGraphView.this.getWidth();
/*     */     
/*     */ 
/* 120 */     int xLength = this.width - (CustomLineGraphView.this.mLineGraphVO.getPaddingLeft() + CustomLineGraphView.this.mLineGraphVO.getPaddingRight() + CustomLineGraphView.this.mLineGraphVO.getMarginRight());
/* 121 */     int yLength = this.height - (CustomLineGraphView.this.mLineGraphVO.getPaddingBottom() + CustomLineGraphView.this.mLineGraphVO.getPaddingTop() + CustomLineGraphView.this.mLineGraphVO.getMarginTop());
/*     */     
/*     */ 
/* 124 */     int chartXLength = this.width - (CustomLineGraphView.this.mLineGraphVO.getPaddingLeft() + CustomLineGraphView.this.mLineGraphVO.getPaddingRight());
/* 125 */     int chartYLength = this.height - (CustomLineGraphView.this.mLineGraphVO.getPaddingBottom() + CustomLineGraphView.this.mLineGraphVO.getPaddingTop());
/*     */     
/* 127 */     Paint p = new Paint();
/* 128 */     Paint pCircle = new Paint();
/* 129 */     Paint pLine = new Paint();
/* 130 */     Paint pBaseLine = new Paint();
/* 131 */     Paint pBaseLineX = new Paint();
/* 132 */     Paint pMarkText = new Paint();
/*     */     
/*     */ 
/* 135 */     float anim = 0.0F;
/* 136 */     boolean isAnimation = false;
/* 137 */     boolean isDrawRegion = false;
/* 138 */     long animStartTime = -1L;
/*     */     
/* 140 */     WeakHashMap<Integer, Bitmap> arrIcon = new WeakHashMap();
/* 141 */     Bitmap bg = null;
/*     */     
/* 143 */     public DrawThread(SurfaceHolder holder, Context context) { this.mHolder = holder;
/* 144 */       this.mCtx = context;
/*     */       
/* 146 */       int size = CustomLineGraphView.this.mLineGraphVO.getArrGraph().size();
/* 147 */       for (int i = 0; i < size; i++) {
/* 148 */         int bitmapResource = ((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getBitmapResource();
/* 149 */         if (bitmapResource != -1) {
/* 150 */           this.arrIcon.put(Integer.valueOf(i), BitmapFactory.decodeResource(CustomLineGraphView.this.getResources(), bitmapResource));
/*     */         }
/* 152 */         else if (this.arrIcon.get(Integer.valueOf(i)) != null) {
/* 153 */           this.arrIcon.remove(Integer.valueOf(i));
/*     */         }
/*     */       }
/*     */       
/* 157 */       int bgResource = CustomLineGraphView.this.mLineGraphVO.getGraphBG();
/* 158 */       if (bgResource != -1) {
/* 159 */         Bitmap tempBg = BitmapFactory.decodeResource(CustomLineGraphView.this.getResources(), bgResource);
/* 160 */         this.bg = Bitmap.createScaledBitmap(tempBg, this.width, this.height, true);
/* 161 */         tempBg.recycle();
/*     */       }
/*     */     }
/*     */     
/*     */     public void setRunFlag(boolean bool) {
/* 166 */       this.isRun = bool;
/*     */     }
/*     */     
/*     */     public void run()
/*     */     {
/* 171 */       Canvas canvas = null;
/* 172 */       GraphCanvasWrapper graphCanvasWrapper = null;
/* 173 */       Log.e("CustomLineGraphView", "height = " + this.height);
/* 174 */       Log.e("CustomLineGraphView", "width = " + this.width);
/*     */       
/* 176 */       setPaint();
/* 177 */       isAnimation();
/* 178 */       isDrawRegion();
/*     */       
/* 180 */       this.animStartTime = System.currentTimeMillis();
/*     */       
/* 182 */       while (this.isRun)
/*     */       {
/*     */ 
/* 185 */         if (!this.isDirty) {
/*     */           try {
/* 187 */             Thread.sleep(100L);
/*     */           } catch (InterruptedException e1) {
/* 189 */             e1.printStackTrace();
/*     */           }
/*     */         }
/*     */         else
/*     */         {
/* 194 */           canvas = this.mHolder.lockCanvas();
/* 195 */           graphCanvasWrapper = new GraphCanvasWrapper(canvas, this.width, this.height, CustomLineGraphView.this.mLineGraphVO.getPaddingLeft(), CustomLineGraphView.this.mLineGraphVO.getPaddingBottom());
/*     */           try
/*     */           {
/* 198 */             Thread.sleep(0L);
/*     */           }
/*     */           catch (InterruptedException e) {
/* 201 */             e.printStackTrace();
/*     */           }
/* 203 */           calcTimePass();
/*     */           
/* 205 */           synchronized (this.mHolder) {
/* 206 */             synchronized (CustomLineGraphView.touchLock)
/*     */             {
/*     */               try
/*     */               {
/* 210 */                 canvas.drawColor(-1);
/* 211 */                 if (this.bg != null) {
/* 212 */                   canvas.drawBitmap(this.bg, 0.0F, 0.0F, null);
/*     */                 }
/*     */                 
/*     */ 
/* 216 */                 drawBaseLine(graphCanvasWrapper);
/*     */                 
/*     */ 
/* 219 */                 graphCanvasWrapper.drawLine(0.0F, 0.0F, 0.0F, this.chartYLength, this.pBaseLine);
/*     */                 
/*     */ 
/* 222 */                 graphCanvasWrapper.drawLine(0.0F, 0.0F, this.chartXLength, 0.0F, this.pBaseLine);
/*     */                 
/*     */ 
/* 225 */                 drawXMark(graphCanvasWrapper);
/* 226 */                 drawYMark(graphCanvasWrapper);
/*     */                 
/*     */ 
/* 229 */                 drawXText(graphCanvasWrapper);
/* 230 */                 drawYText(graphCanvasWrapper);
/*     */                 
/*     */ 
/* 233 */                 drawGraphRegion(graphCanvasWrapper);
/* 234 */                 drawGraph(graphCanvasWrapper);
/*     */                 
/*     */ 
/* 237 */                 drawGraphName(canvas);
/*     */ 
/*     */               }
/*     */               catch (Exception e)
/*     */               {
/*     */ 
/* 243 */                 e.printStackTrace();
/*     */               } finally {
/* 245 */                 if (graphCanvasWrapper.getCanvas() != null) {
/* 246 */                   this.mHolder.unlockCanvasAndPost(graphCanvasWrapper.getCanvas());
/*     */                 }
/*     */               }
/*     */             }
/*     */           }
/*     */         }
/*     */       }
/*     */     }
/*     */     
/*     */ 
/*     */     private void calcTimePass()
/*     */     {
/* 258 */       if (this.isAnimation) {
/* 259 */         long curTime = System.currentTimeMillis();
/* 260 */         long gapTime = curTime - this.animStartTime;
/* 261 */         long animDuration = CustomLineGraphView.this.mLineGraphVO.getAnimation().getDuration();
/* 262 */         if (gapTime >= animDuration) {
/* 263 */           gapTime = animDuration;
/* 264 */           this.isDirty = false;
/*     */         }
/*     */         
/* 267 */         this.anim = (((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(0)).getCoordinateArr().length * (float)gapTime / (float)animDuration);
/*     */       } else {
/* 269 */         this.isDirty = false;
/*     */       }
/*     */     }
/*     */     
/*     */ 
/*     */ 
/*     */ 
/*     */     public void drawGraphName(Canvas canvas)
/*     */     {
/* 278 */       GraphNameBox gnb = CustomLineGraphView.this.mLineGraphVO.getGraphNameBox();
/* 279 */       if (gnb != null) {
/* 280 */         int nameboxWidth = 0;
/* 281 */         int nameboxHeight = 0;
/*     */         
/* 283 */         int nameboxIconWidth = gnb.getNameboxIconWidth();
/* 284 */         int nameboxIconHeight = gnb.getNameboxIconHeight();
/*     */         
/* 286 */         int nameboxMarginTop = gnb.getNameboxMarginTop();
/* 287 */         int nameboxMarginRight = gnb.getNameboxMarginRight();
/* 288 */         int nameboxPadding = gnb.getNameboxPadding();
/*     */         
/* 290 */         int nameboxTextIconMargin = gnb.getNameboxIconMargin();
/* 291 */         int nameboxIconMargin = gnb.getNameboxIconMargin();
/*     */         
/* 293 */         int maxTextWidth = 0;
/* 294 */         int maxTextHeight = 0;
/*     */         
/* 296 */         Paint nameRextPaint = getNameBoxBorderPaint(gnb);
/* 297 */         Paint pIcon = getNameBoxIconPaint(gnb);
/* 298 */         Paint pNameText = getNameBoxTextPaint(gnb);
/*     */         
/*     */ 
/* 301 */         int graphSize = CustomLineGraphView.this.mLineGraphVO.getArrGraph().size();
/* 302 */         for (int i = 0; i < graphSize; i++)
/*     */         {
/*     */ 
/* 305 */           String text = ((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getName();
/* 306 */           Rect rect = new Rect();
/* 307 */           pNameText.getTextBounds(text, 0, text.length(), rect);
/*     */           
/* 309 */           if (rect.width() > maxTextWidth) {
/* 310 */             maxTextWidth = rect.width();
/* 311 */             maxTextHeight = rect.height();
/*     */           }
/*     */           
/* 314 */           ((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getName();
/*     */         }
/*     */         
/* 317 */         ((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(0)).getName();
/* 318 */         nameboxWidth = 1 * maxTextWidth + nameboxTextIconMargin + nameboxIconWidth;
/* 319 */         int maxCellHight = maxTextHeight;
/* 320 */         if (nameboxIconHeight > maxTextHeight) {
/* 321 */           maxCellHight = nameboxIconHeight;
/*     */         }
/* 323 */         nameboxHeight = graphSize * maxCellHight + (graphSize - 1) * nameboxIconMargin;
/*     */         
/* 325 */         canvas.drawRect(this.width - (nameboxMarginRight + nameboxWidth) - nameboxPadding * 2, 
/* 326 */           nameboxMarginTop, this.width - nameboxMarginRight, nameboxMarginTop + nameboxHeight + nameboxPadding * 2, nameRextPaint);
/*     */         
/* 328 */         for (int i = 0; i < graphSize; i++)
/*     */         {
/* 330 */           pIcon.setColor(((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getColor());
/* 331 */           canvas.drawRect(this.width - (nameboxMarginRight + nameboxWidth) - nameboxPadding, 
/* 332 */             nameboxMarginTop + maxCellHight * i + nameboxPadding + nameboxIconMargin * i, 
/* 333 */             this.width - (nameboxMarginRight + maxTextWidth) - nameboxPadding - nameboxTextIconMargin, 
/* 334 */             nameboxMarginTop + maxCellHight * (i + 1) + nameboxPadding + nameboxIconMargin * i, pIcon);
/*     */           
/* 336 */           String text = ((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getName();
/* 337 */           canvas.drawText(text, this.width - (nameboxMarginRight + maxTextWidth) - nameboxPadding, 
/* 338 */             nameboxMarginTop + maxTextHeight / 2 + maxCellHight * i + maxCellHight / 2 + nameboxPadding + nameboxIconMargin * i, pNameText);
/*     */         }
/*     */       }
/*     */     }
/*     */     
/*     */ 
/*     */ 
/*     */     private void isAnimation()
/*     */     {
/* 347 */       if (CustomLineGraphView.this.mLineGraphVO.getAnimation() != null) {
/* 348 */         this.isAnimation = true;
/*     */       } else {
/* 350 */         this.isAnimation = false;
/*     */       }
/*     */     }
/*     */     
/*     */ 
/*     */ 
/*     */     private void isDrawRegion()
/*     */     {
/* 358 */       if (CustomLineGraphView.this.mLineGraphVO.isDrawRegion()) {
/* 359 */         this.isDrawRegion = true;
/*     */       } else {
/* 361 */         this.isDrawRegion = false;
/*     */       }
/*     */     }
/*     */     
/*     */     private void drawBaseLine(GraphCanvasWrapper graphCanvas) {
/* 366 */       for (int i = 1; CustomLineGraphView.this.mLineGraphVO.getIncrement() * i <= CustomLineGraphView.this.mLineGraphVO.getMaxValue(); i++)
/*     */       {
/* 368 */         float y = this.yLength * CustomLineGraphView.this.mLineGraphVO.getIncrement() * i / CustomLineGraphView.this.mLineGraphVO.getMaxValue();
/*     */         
/* 370 */         graphCanvas.drawLine(0.0F, y, this.chartXLength, y, this.pBaseLineX);
/*     */       }
/*     */     }
/*     */     
/*     */ 
/*     */ 
/*     */     private void setPaint()
/*     */     {
/* 378 */       this.p = new Paint();
/* 379 */       this.p.setFlags(1);
/* 380 */       this.p.setAntiAlias(true);
/* 381 */       this.p.setFilterBitmap(true);
/* 382 */       this.p.setColor(Color.WHITE);
/* 383 */       this.p.setStrokeWidth(5.0F);
/* 384 */       this.p.setStyle(Paint.Style.STROKE);
/*     */       
/* 386 */       this.pCircle = new Paint();
/* 387 */       this.pCircle.setFlags(1);
/* 388 */       this.pCircle.setAntiAlias(true);
/* 389 */       this.pCircle.setFilterBitmap(true);
/* 390 */       this.pCircle.setColor(Color.WHITE);
/* 391 */       this.pCircle.setStrokeWidth(5.0F);
/* 392 */       this.pCircle.setStyle(Paint.Style.FILL_AND_STROKE);
/*     */       
/* 394 */       this.pLine = new Paint();
/* 395 */       this.pLine.setFlags(1);
/* 396 */       this.pLine.setAntiAlias(true);
/* 397 */       this.pLine.setFilterBitmap(true);
/* 398 */       this.pLine.setShader(new LinearGradient(0.0F, 300.0F, 0.0F, 0.0F, -16777216, -1, Shader.TileMode.MIRROR));
/*     */       
/* 400 */       this.pBaseLine = new Paint();
/* 401 */       this.pBaseLine.setFlags(1);
/* 402 */       this.pBaseLine.setAntiAlias(true);
/* 403 */       this.pBaseLine.setFilterBitmap(true);
/* 404 */       this.pBaseLine.setColor(Color.WHITE);
/* 405 */       this.pBaseLine.setStrokeWidth(5.0F);
/*     */       
/* 407 */       this.pBaseLineX = new Paint();
/* 408 */       this.pBaseLineX.setFlags(1);
/* 409 */       this.pBaseLineX.setAntiAlias(true);
/* 410 */       this.pBaseLineX.setFilterBitmap(true);
/* 411 */       this.pBaseLineX.setColor(Color.WHITE);
/* 412 */       this.pBaseLineX.setStrokeWidth(5.0F);
/* 413 */       this.pBaseLineX.setStyle(Paint.Style.STROKE);
/* 414 */       this.pBaseLineX.setPathEffect(new DashPathEffect(new float[] { 10.0F, 5.0F }, 0.0F));
/*     */       
/* 416 */       this.pMarkText = new Paint();
/* 417 */       this.pMarkText.setFlags(1);
/* 418 */       this.pMarkText.setAntiAlias(true);
/* 419 */       this.pMarkText.setColor(Color.WHITE);
				this.pMarkText.setStrokeWidth(7.0F);
/*     */     }
/*     */     
/*     */ 
/*     */ 
/*     */ 
/*     */     private void drawGraphRegion(GraphCanvasWrapper graphCanvas)
/*     */     {
/* 427 */       if (this.isDrawRegion) {
/* 428 */         if (this.isAnimation) {
/* 429 */           drawGraphRegionWithAnimation(graphCanvas);
/*     */         } else {
/* 431 */           drawGraphRegionWithoutAnimation(graphCanvas);
/*     */         }
/*     */       }
/*     */     }
/*     */     
/*     */ 
/*     */ 
/*     */ 
/*     */     private void drawGraph(GraphCanvasWrapper graphCanvas)
/*     */     {
/* 441 */       if (this.isAnimation) {
/* 442 */         drawGraphWithAnimation(graphCanvas);
/*     */       } else {
/* 444 */         drawGraphWithoutAnimation(graphCanvas);
/*     */       }
/*     */     }
/*     */     
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     private void drawGraphRegionWithoutAnimation(GraphCanvasWrapper graphCanvas)
/*     */     {
/* 454 */       boolean isDrawRegion = CustomLineGraphView.this.mLineGraphVO.isDrawRegion();
/*     */       
/* 456 */       for (int i = 0; i < CustomLineGraphView.this.mLineGraphVO.getArrGraph().size(); i++) {
/* 457 */         GraphPath regionPath = new GraphPath(this.width, this.height, CustomLineGraphView.this.mLineGraphVO.getPaddingLeft(), CustomLineGraphView.this.mLineGraphVO.getPaddingBottom());
/* 458 */         boolean firstSet = false;
/* 459 */         float x = 0.0F;
/* 460 */         float y = 0.0F;
/* 461 */         this.p.setColor(((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getColor());
/* 462 */         this.pCircle.setColor(((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getColor());
/* 463 */         float xGap = this.xLength / (((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getCoordinateArr().length - 1);
/*     */         
/* 465 */         for (int j = 0; j < ((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getCoordinateArr().length; j++) {
/* 466 */           if (j < ((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getCoordinateArr().length)
/*     */           {
/* 468 */             if (!firstSet)
/*     */             {
/* 470 */               x = xGap * j;
/* 471 */               y = this.yLength * ((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getCoordinateArr()[j] / CustomLineGraphView.this.mLineGraphVO.getMaxValue();
/*     */               
/* 473 */               regionPath.moveTo(x, 0.0F);
/* 474 */               regionPath.lineTo(x, y);
/*     */               
/* 476 */               firstSet = true;
/*     */             } else {
/* 478 */               x = xGap * j;
/* 479 */               y = this.yLength * ((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getCoordinateArr()[j] / CustomLineGraphView.this.mLineGraphVO.getMaxValue();
/*     */               
/* 481 */               regionPath.lineTo(x, y);
/*     */             }
/*     */           }
/*     */         }
/*     */         
/* 486 */         if (isDrawRegion) {
/* 487 */           regionPath.lineTo(x, 0.0F);
/* 488 */           regionPath.lineTo(0.0F, 0.0F);
/*     */           
/* 490 */           Paint pBg = new Paint();
/* 491 */           pBg.setFlags(1);
/* 492 */           pBg.setAntiAlias(true);
/* 493 */           pBg.setFilterBitmap(true);
/* 494 */           pBg.setStyle(Paint.Style.FILL);
/* 495 */           pBg.setColor(((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getColor());
/* 496 */           graphCanvas.getCanvas().drawPath(regionPath, pBg);
/*     */         }
/*     */       }
/*     */     }
/*     */     
/*     */ 
/*     */ 
/*     */ 
/*     */     private void drawGraphRegionWithAnimation(GraphCanvasWrapper graphCanvas)
/*     */     {
/* 506 */       float prev_x = 0.0F;
/* 507 */       float prev_y = 0.0F;
/*     */       
/* 509 */       float next_x = 0.0F;
/* 510 */       float next_y = 0.0F;
/*     */       
/* 512 */       int value = 0;
/* 513 */       float mode = 0.0F;
/*     */       
/* 515 */       boolean isDrawRegion = CustomLineGraphView.this.mLineGraphVO.isDrawRegion();
/*     */       
/* 517 */       for (int i = 0; i < CustomLineGraphView.this.mLineGraphVO.getArrGraph().size(); i++) {
/* 518 */         GraphPath regionPath = new GraphPath(this.width, this.height, CustomLineGraphView.this.mLineGraphVO.getPaddingLeft(), CustomLineGraphView.this.mLineGraphVO.getPaddingBottom());
/* 519 */         boolean firstSet = false;
/* 520 */         float x = 0.0F;
/* 521 */         float y = 0.0F;
/* 522 */         this.p.setColor(((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getColor());
/* 523 */         this.pCircle.setColor(((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getColor());
/* 524 */         float xGap = this.xLength / (((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getCoordinateArr().length - 1);
/*     */         
/* 526 */         value = (int)(this.anim / 1.0F);
/* 527 */         mode = this.anim % 1.0F;
/*     */         
/* 529 */         boolean isFinish = false;
/* 530 */         for (int j = 0; j <= value + 1; j++) {
/* 531 */           if (j < ((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getCoordinateArr().length)
/*     */           {
/* 533 */             if (!firstSet)
/*     */             {
/* 535 */               x = xGap * j;
/* 536 */               y = this.yLength * ((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getCoordinateArr()[j] / CustomLineGraphView.this.mLineGraphVO.getMaxValue();
/*     */               
/* 538 */               regionPath.moveTo(x, 0.0F);
/* 539 */               regionPath.lineTo(x, y);
/*     */               
/* 541 */               firstSet = true;
/*     */             } else {
/* 543 */               x = xGap * j;
/* 544 */               y = this.yLength * ((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getCoordinateArr()[j] / CustomLineGraphView.this.mLineGraphVO.getMaxValue();
/*     */               
/* 546 */               if (j > value) {
/* 547 */                 next_x = x - prev_x;
/* 548 */                 next_y = y - prev_y;
/* 549 */                 regionPath.lineTo(prev_x + next_x * mode, prev_y + next_y * mode);
/*     */               } else {
/* 551 */                 regionPath.lineTo(x, y);
/*     */               }
/*     */             }
/*     */             
/* 555 */             prev_x = x;
/* 556 */             prev_y = y;
/*     */           }
/*     */         }
/* 559 */         isFinish = true;
/*     */         
/* 561 */         if (isDrawRegion) {
/* 562 */           float x_bg = prev_x + next_x * mode;
/* 563 */           if (x_bg >= this.xLength) {
/* 564 */             x_bg = this.xLength;
/*     */           }
/* 566 */           regionPath.lineTo(x_bg, 0.0F);
/* 567 */           regionPath.lineTo(0.0F, 0.0F);
/*     */           
/* 569 */           Paint pBg = new Paint();
/* 570 */           pBg.setFlags(1);
/* 571 */           pBg.setAntiAlias(true);
/* 572 */           pBg.setFilterBitmap(true);
/* 573 */           pBg.setStyle(Paint.Style.FILL);
/* 574 */           pBg.setColor(((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getColor());
/* 575 */           graphCanvas.getCanvas().drawPath(regionPath, pBg);
/*     */         }
/*     */       }
/*     */     }
/*     */     
/*     */ 
/*     */ 
/*     */ 
/*     */     private void drawGraphWithoutAnimation(GraphCanvasWrapper graphCanvas)
/*     */     {
/* 585 */       for (int i = 0; i < CustomLineGraphView.this.mLineGraphVO.getArrGraph().size(); i++) {
/* 586 */         GraphPath linePath = new GraphPath(this.width, this.height, CustomLineGraphView.this.mLineGraphVO.getPaddingLeft(), CustomLineGraphView.this.mLineGraphVO.getPaddingBottom());
/* 587 */         GraphPath regionPath = new GraphPath(this.width, this.height, CustomLineGraphView.this.mLineGraphVO.getPaddingLeft(), CustomLineGraphView.this.mLineGraphVO.getPaddingBottom());
/* 588 */         boolean firstSet = false;
/* 589 */         float x = 0.0F;
/* 590 */         float y = 0.0F;
/* 591 */         this.p.setColor(((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getColor());
/* 592 */         this.pCircle.setColor(((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getColor());
/* 593 */         float xGap = this.xLength / (((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getCoordinateArr().length - 1);
/*     */         
/* 595 */         Bitmap icon = (Bitmap)this.arrIcon.get(Integer.valueOf(i));
/*     */         
/* 597 */         for (int j = 0; j < ((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getCoordinateArr().length; j++) {
/* 598 */           if (j < ((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getCoordinateArr().length)
/*     */           {
/* 600 */             if (!firstSet)
/*     */             {
/* 602 */               x = xGap * j;
/* 603 */               y = this.yLength * ((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getCoordinateArr()[j] / CustomLineGraphView.this.mLineGraphVO.getMaxValue();
/*     */               
/* 605 */               linePath.moveTo(x, y);
/*     */               
/* 607 */               firstSet = true;
/*     */             } else {
/* 609 */               x = xGap * j;
/* 610 */               y = this.yLength * ((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getCoordinateArr()[j] / CustomLineGraphView.this.mLineGraphVO.getMaxValue();
/*     */               
/* 612 */               linePath.lineTo(x, y);
/*     */             }
/*     */             
/* 615 */             if (icon == null) {
/* 616 */               graphCanvas.drawCircle(x, y, 4.0F, this.pCircle);
/*     */             } else {
/* 618 */               graphCanvas.drawBitmapIcon(icon, x, y, null);
/*     */             }
/*     */           }
/*     */         }
/*     */         
/* 623 */         graphCanvas.getCanvas().drawPath(linePath, this.p);
/*     */       }
/*     */     }
/*     */     
/*     */ 
/*     */ 
/*     */ 
/*     */     private void drawGraphWithAnimation(GraphCanvasWrapper graphCanvas)
/*     */     {
/* 632 */       float prev_x = 0.0F;
/* 633 */       float prev_y = 0.0F;
/*     */       
/* 635 */       float next_x = 0.0F;
/* 636 */       float next_y = 0.0F;
/*     */       
/* 638 */       float value = 0.0F;
/* 639 */       float mode = 0.0F;
/*     */       
/* 641 */       for (int i = 0; i < CustomLineGraphView.this.mLineGraphVO.getArrGraph().size(); i++) {
/* 642 */         GraphPath linePath = new GraphPath(this.width, this.height, CustomLineGraphView.this.mLineGraphVO.getPaddingLeft(), CustomLineGraphView.this.mLineGraphVO.getPaddingBottom());
/* 643 */         GraphPath regionPath = new GraphPath(this.width, this.height, CustomLineGraphView.this.mLineGraphVO.getPaddingLeft(), CustomLineGraphView.this.mLineGraphVO.getPaddingBottom());
/* 644 */         boolean firstSet = false;
/* 645 */         float x = 0.0F;
/* 646 */         float y = 0.0F;
/* 647 */         this.p.setColor(((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getColor());
/* 648 */         this.pCircle.setColor(((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getColor());
/* 649 */         float xGap = this.xLength / (((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getCoordinateArr().length - 1);
/*     */         
/* 651 */         Bitmap icon = (Bitmap)this.arrIcon.get(Integer.valueOf(i));
/* 652 */         value = this.anim / 1.0F;
/* 653 */         mode = this.anim % 1.0F;
/*     */         
/* 655 */         for (int j = 0; j < value + 1.0F; j++) {
/* 656 */           if (j < ((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getCoordinateArr().length)
/*     */           {
/* 658 */             if (!firstSet)
/*     */             {
/* 660 */               x = xGap * j;
/* 661 */               y = this.yLength * ((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getCoordinateArr()[j] / CustomLineGraphView.this.mLineGraphVO.getMaxValue();
/*     */               
/* 663 */               linePath.moveTo(x, y);
/*     */               
/* 665 */               firstSet = true;
/*     */             } else {
/* 667 */               x = xGap * j;
/* 668 */               y = this.yLength * ((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(i)).getCoordinateArr()[j] / CustomLineGraphView.this.mLineGraphVO.getMaxValue();
/*     */               
/* 670 */               if ((j > value) && (mode != 0.0F)) {
/* 671 */                 next_x = x - prev_x;
/* 672 */                 next_y = y - prev_y;
/*     */                 
/* 674 */                 linePath.lineTo(prev_x + next_x * mode, prev_y + next_y * mode);
/*     */               } else {
/* 676 */                 linePath.lineTo(x, y);
/*     */               }
/*     */             }
/*     */             
/* 680 */             if (icon == null) {
/* 681 */               graphCanvas.drawCircle(x, y, 4.0F, this.pCircle);
/*     */             } else {
/* 683 */               graphCanvas.drawBitmapIcon(icon, x, y, null);
/*     */             }
/* 685 */             prev_x = x;
/* 686 */             prev_y = y;
/*     */           }
/*     */         }
/*     */         
/* 690 */         graphCanvas.getCanvas().drawPath(linePath, this.p);
/*     */       }
/*     */     }
/*     */     
/*     */ 
/*     */ 
/*     */     private void drawXMark(GraphCanvasWrapper graphCanvas)
/*     */     {
/* 698 */       float x = 0.0F;
/* 699 */       float y = 0.0F;
/*     */       
/* 701 */       float xGap = this.xLength / (((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(0)).getCoordinateArr().length - 1);
/* 702 */       for (int i = 0; i < ((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(0)).getCoordinateArr().length; i++) {
/* 703 */         x = xGap * i;
/* 704 */         y = this.yLength * ((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(0)).getCoordinateArr()[i] / CustomLineGraphView.this.mLineGraphVO.getMaxValue();
/*     */         
/* 706 */         graphCanvas.drawLine(x, 0.0F, x, -10.0F, this.pBaseLine);
/*     */       }
/*     */     }
/*     */     
/*     */ 
/*     */ 
/*     */     private void drawYMark(GraphCanvasWrapper canvas)
/*     */     {
/* 714 */       for (int i = 0; CustomLineGraphView.this.mLineGraphVO.getIncrement() * i <= CustomLineGraphView.this.mLineGraphVO.getMaxValue(); i++)
/*     */       {
/* 716 */         float y = this.yLength * CustomLineGraphView.this.mLineGraphVO.getIncrement() * i / CustomLineGraphView.this.mLineGraphVO.getMaxValue();
/*     */         
/* 718 */         canvas.drawLine(0.0F, y, -10.0F, y, this.pBaseLine);
/*     */       }
/*     */     }
/*     */     
/*     */ 
/*     */ 
/*     */     private void drawXText(GraphCanvasWrapper graphCanvas)
/*     */     {
/* 726 */       float x = 0.0F;
/* 727 */       float y = 0.0F;
/*     */       
/* 729 */       float xGap = this.xLength / (((LineGraph)CustomLineGraphView.this.mLineGraphVO.getArrGraph().get(0)).getCoordinateArr().length - 1);
/* 730 */       for (int i = 0; i < CustomLineGraphView.this.mLineGraphVO.getLegendArr().length; i++) {
/* 731 */         x = xGap * i;
/*     */         
/* 733 */         String text = CustomLineGraphView.this.mLineGraphVO.getLegendArr()[i];
/* 734 */         this.pMarkText.measureText(text);
/* 735 */         this.pMarkText.setTextSize(25.0F);
/* 736 */         Rect rect = new Rect();
/* 737 */         this.pMarkText.getTextBounds(text, 0, text.length(), rect);
/*     */         
/* 739 */         graphCanvas.drawText(text, x - rect.width() / 2, -(20 + rect.height()), this.pMarkText);
/*     */       }
/*     */     }
/*     */     
/*     */ 
/*     */ 
/*     */     private void drawYText(GraphCanvasWrapper graphCanvas)
/*     */     {
/* 747 */       for (int i = 0; CustomLineGraphView.this.mLineGraphVO.getIncrement() * i <= CustomLineGraphView.this.mLineGraphVO.getMaxValue(); i++)
/*     */       {
/* 749 */         String mark = Float.toString(CustomLineGraphView.this.mLineGraphVO.getIncrement() * i);
/* 750 */         float y = this.yLength * CustomLineGraphView.this.mLineGraphVO.getIncrement() * i / CustomLineGraphView.this.mLineGraphVO.getMaxValue();
/* 751 */         this.pMarkText.measureText(mark);
/* 752 */         this.pMarkText.setTextSize(20.0F);
/* 753 */         Rect rect = new Rect();
/* 754 */         this.pMarkText.getTextBounds(mark, 0, mark.length(), rect);
/*     */         
/*     */ 
/* 757 */         graphCanvas.drawText(mark, -(rect.width() + 20), y - rect.height() / 2, this.pMarkText);
/*     */       }
/*     */     }
/*     */   }
/*     */ }


 