package com.hindbyte.velocity.util;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

public final class AnimatedProgressBar extends ProgressBar {

   private ValueAnimator primaryAnimator;
   private final ValueAnimator closingAnimator;
   private float clipRegion;
   private int expectedProgress;
   private final Rect tempRect;
   private boolean isRtl;
   private final AnimatorUpdateListener animatorListener;
   private static final int PROGRESS_DURATION = 300;
   private static final int CLOSING_DELAY = 300;
   private static final int CLOSING_DURATION = 300;

   public void setProgress(int progress) {
      int nextProgress = Math.min(progress, this.getMax());
      nextProgress = Math.max(0, nextProgress);
      this.expectedProgress = nextProgress;
      if (this.expectedProgress < progress && progress == this.getMax()) {
         this.setProgressImmediately(0);
      }
      ValueAnimator valueAnimator;
      ValueAnimator valueAnimator1;
      label26: {
         valueAnimator = this.primaryAnimator;
         if (valueAnimator != null) {
            valueAnimator1 = valueAnimator;
            valueAnimator1.cancel();
            valueAnimator1.setIntValues(progress, nextProgress);
            valueAnimator1.start();
            break label26;
         }
         AnimatedProgressBar animatedProgressBar = this;
         animatedProgressBar.setProgressImmediately(nextProgress);
      }

      valueAnimator = this.closingAnimator;
      if (valueAnimator != null) {
         valueAnimator1 = valueAnimator;
         if (nextProgress != this.getMax()) {
            valueAnimator1.cancel();
            this.clipRegion = 0.0F;
         }
      }

   }

   public void onDraw(Canvas canvas) {
      if (this.clipRegion == 0.0F) {
         super.onDraw(canvas);
      } else {
         canvas.getClipBounds(this.tempRect);
         float clipWidth = (float)this.tempRect.width() * this.clipRegion;
         int saveCount = canvas.save();
         if (this.isRtl) {
            canvas.clipRect((float)this.tempRect.left, (float)this.tempRect.top, (float)this.tempRect.right - clipWidth, (float)this.tempRect.bottom);
         } else {
            canvas.clipRect((float)this.tempRect.left + clipWidth, (float)this.tempRect.top, (float)this.tempRect.right, (float)this.tempRect.bottom);
         }

         super.onDraw(canvas);
         canvas.restoreToCount(saveCount);
      }
   }

   public void setVisibility(int value) {
      if (value == View.GONE) {
         if (this.expectedProgress == this.getMax()) {
            this.animateClosing();
         } else {
            this.setVisibilityImmediately(value);
         }
      } else {
         this.setVisibilityImmediately(value);
      }
   }

   public void setVisibilityImmediately(int value) {
      super.setVisibility(value);
   }

   private void animateClosing() {
      this.isRtl = ViewCompat.getLayoutDirection((View)this) == ViewCompat.LAYOUT_DIRECTION_RTL;
      this.closingAnimator.cancel();
      Handler handler = this.getHandler();
      if (handler != null) {
         handler.postDelayed((Runnable)(AnimatedProgressBar.this.closingAnimator::start), (long)CLOSING_DELAY);
      }

   }

   private void setProgressImmediately(int progress) {
      super.setProgress(progress);
   }

   private void init() {
      ValueAnimator valueAnimator = ValueAnimator.ofInt(this.getProgress(), this.getMax());
      valueAnimator.setInterpolator((TimeInterpolator)(new LinearInterpolator()));
      valueAnimator.setDuration((long)PROGRESS_DURATION);
      valueAnimator.addUpdateListener(this.animatorListener);
      this.primaryAnimator = valueAnimator;
      this.closingAnimator.setDuration((long)CLOSING_DURATION);
      this.closingAnimator.setInterpolator((TimeInterpolator)(new LinearInterpolator()));
      this.closingAnimator.addUpdateListener((AnimatorUpdateListener)(valueAnimator1 -> {
         Object object = valueAnimator1.getAnimatedValue();
         if (object != null && AnimatedProgressBar.this.clipRegion != (Float)object) {
            AnimatedProgressBar.this.clipRegion = (Float)object;
            AnimatedProgressBar.this.invalidate();
         }
      }));
      this.closingAnimator.addListener((AnimatorListener)(new AnimatorListener() {
         public void onAnimationStart(Animator animator) {
            AnimatedProgressBar.this.clipRegion = 0.0F;
         }

         public void onAnimationEnd(Animator animator) {
            AnimatedProgressBar.this.setVisibilityImmediately(8);
         }

         public void onAnimationCancel(Animator animator) {
            AnimatedProgressBar.this.clipRegion = 0.0F;
         }

         public void onAnimationRepeat(Animator animator) {
         }
      }));
      Drawable drawable = this.getProgressDrawable();
      this.setProgressDrawable(drawable);
   }

   public AnimatedProgressBar(Context context) {
      super(context, (AttributeSet)null);
      this.closingAnimator = ValueAnimator.ofFloat(0.0F, 1.0F);
      this.tempRect = new Rect();
      this.animatorListener = (AnimatorUpdateListener)(it -> {
         AnimatedProgressBar animatedProgressBar = AnimatedProgressBar.this;
         ValueAnimator valueAnimator = AnimatedProgressBar.this.primaryAnimator;
         Object object = valueAnimator.getAnimatedValue();
         if (object != null) {
            animatedProgressBar.setProgressImmediately((Integer)object);
         }
      });
      this.init();
   }

   public AnimatedProgressBar(Context context, @Nullable AttributeSet attrs) {
      super(context, attrs);
      this.closingAnimator = ValueAnimator.ofFloat(0.0F, 1.0F);
      this.tempRect = new Rect();
      this.animatorListener = (AnimatorUpdateListener)(it -> {
         AnimatedProgressBar animatedProgressBar = AnimatedProgressBar.this;
         ValueAnimator valueAnimator = AnimatedProgressBar.this.primaryAnimator;
         Object object = valueAnimator.getAnimatedValue();
         if (object != null) {
            animatedProgressBar.setProgressImmediately((Integer)object);
         }
      });
      this.init();
   }

   public AnimatedProgressBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
      super(context, attrs, defStyleAttr);
      this.closingAnimator = ValueAnimator.ofFloat(0.0F, 1.0F);
      this.tempRect = new Rect();
      this.animatorListener = (AnimatorUpdateListener)(it -> {
         AnimatedProgressBar animatedProgressBar = AnimatedProgressBar.this;
         ValueAnimator valueAnimator = AnimatedProgressBar.this.primaryAnimator;
         Object object = valueAnimator.getAnimatedValue();
         if (object != null) {
            animatedProgressBar.setProgressImmediately((Integer)object);
         }
      });
      this.init();
   }
}
