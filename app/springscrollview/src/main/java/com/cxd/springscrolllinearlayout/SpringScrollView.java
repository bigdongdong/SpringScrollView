package com.cxd.springscrolllinearlayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.OverScroller;

/**
 * 2020/9/18
 * 支持阻尼滑动的LinearLayout
 * 今天是九一八事件祭奠日，勿忘国耻
 *
 * 2020/9/21
 * 解决了事件冲突
 *
 * 2020/9/22
 * 解决了嵌套RecyclerView显示不全的bug
 * 最终还是移除了，因为菜搞不定，
 * 对measure机制还是掌握不够
 *
 * 2020/9/23
 * 1.解决了嵌套RecyclerView显示不全bug
 * 在onMeasure中对child重新measure
 * 且更改mode为MeasureSpec.UNSPECIFIED即可
 * 2.解决横向滑动出发child点击事件的bug
 *
 * 2020/9/25
 * 1.解决child match_parent布局超出屏幕问题
 * 2.重构项目extends FrameLayout
 * 并解决裸露子View不显示bug
 *
 * 2020/10/12
 * 优化了过度滑动回弹停顿的现象
 */

@SuppressLint("LongLogTag")
public class SpringScrollView extends FrameLayout {
    private final String TAG = "SpringScrollLinearLayout";
    private final int MIN_FINGER_MOVE_DISTANCE_PX = 150; //最小手指移动距离 px
    private final int MIN_FINGER_CLICK_TIME_MS = 200 ; //手指点击最短时间 ms

    private final int SPRING_MAX_DISTANCE_PX = 700; //最大阻尼px
    private final int SPRING_DEFAULT_TIME_MS = 500; //默认阻尼时间ms

    private final int OVER_SPRING_DEFAULT_DISTANCE_PX = 350; //默认过度阻尼px
    private final int OVER_SPRING_DEFAULT_TIME_MS = 800; //默认过度阻尼时间ms
    private final int VELOCITY_SYSTEM_MAX; //系统内置的最大速度

    private OverScroller scroller ;
    private VelocityTracker velocityTracker ;

    private int mMaxOverScrollY; //允许over scroll的最大的scrollY
    private boolean mIsFingerUp = false ; //手指是否抬起
    private boolean mIsScrolling = false ; //是否正在进行滚动

    public SpringScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        VELOCITY_SYSTEM_MAX = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        scroller = new OverScroller(context);

    }

    private void initVelocityTrackerIfNotExists(){
        if(velocityTracker == null){
            velocityTracker = VelocityTracker.obtain();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(widthMeasureSpec != 0 && heightMeasureSpec != 0){
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                child.measure(MeasureSpec.makeMeasureSpec(child.getMeasuredWidth(),MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(),MeasureSpec.UNSPECIFIED));
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        final int h = getMeasuredHeight();
        int totalContentHeight = 0 ; //所有子view的总高度
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i) ;
            LayoutParams params = (LayoutParams) child.getLayoutParams();
            totalContentHeight += child.getMeasuredHeight() + params.topMargin + params.bottomMargin;

            //layout:match_parent's 超出屏幕的bug
            if(params.width == -1){
                final int marginLeft = params.leftMargin ;
                final int marginRight = params.rightMargin ;
                child.layout(marginLeft,child.getTop(),getMeasuredWidth() - marginRight,child.getBottom());
            }

        }

        mMaxOverScrollY = totalContentHeight > h ? totalContentHeight - h : 0 ;
    }

    private long mEventDownTime ; //event down 时的时间戳
    private float mEventDownX ; //event down 时的手指 x 坐标
    private float mEventDownY ; //event down 时的手指 y 坐标
    private float mInitialScrollY ; //event down 时采集的原本scroll y 大小
    private float mEventCurrY ; //event 现在的 y 坐标
    private boolean isHorizontalMove ; //是否是横向滑动事件，如果是则屏蔽点击事件

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action){
            //如果当前正在scroll，则down用于停止scroll，拦截
            //其余情况 down一律不拦截
            case MotionEvent.ACTION_DOWN: //0
                mIsFingerUp = false ;
                mEventDownX = ev.getX();
                mEventDownY = ev.getY();
                mInitialScrollY = getScrollY();
                mEventDownTime = System.currentTimeMillis();
                isHorizontalMove = false ;
                if(mIsScrolling){
                    scroller.forceFinished(true);
                    return true ;
                }
                return false ;
            case MotionEvent.ACTION_MOVE: //2
                final float dy = Math.abs(ev.getY() - mEventDownY);
                final float dx = Math.abs(ev.getX() - mEventDownX);
                //判断是否为横向滑动事件
                if(!isHorizontalMove){
                    isHorizontalMove = dx > MIN_FINGER_MOVE_DISTANCE_PX ;
                }
                /*如果dy > dx 则拦截，否则不拦截*/
                return dy > dx ;
            case MotionEvent.ACTION_UP: //1
            case MotionEvent.ACTION_CANCEL: //3
                // 横向滑动事件，拦截
                if(isHorizontalMove){
                    return true;
                }
                //正在滚动，拦截
                if(mIsScrolling){
                    return true ;
                }
                //如果离down时间很近，则不拦截，ev给予下层view
                //如果离down时间比较久，则拦截
                final long lastTime = System.currentTimeMillis() - mEventDownTime ;
                return lastTime > MIN_FINGER_CLICK_TIME_MS;
            default:
                return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch(ev.getAction()) {
            case MotionEvent.ACTION_MOVE: //2
                initVelocityTrackerIfNotExists();
                velocityTracker.addMovement(ev); //将event交给VT 计算速度
                mEventCurrY = ev.getY();
                //targetScrollY 范围: [-MAX_SPRING_PX ~ MAX_SPRING_PX + mMaxScrollY]
                int targetScrollY =  (int) (mEventDownY - mEventCurrY + mInitialScrollY) ;
                if(targetScrollY < -SPRING_MAX_DISTANCE_PX){
                    targetScrollY = -SPRING_MAX_DISTANCE_PX;
                }else if(targetScrollY > SPRING_MAX_DISTANCE_PX + mMaxOverScrollY){
                    targetScrollY = SPRING_MAX_DISTANCE_PX + mMaxOverScrollY;
                }
                scrollTo(0, targetScrollY);
                break;
            case MotionEvent.ACTION_UP: //1
            case MotionEvent.ACTION_CANCEL: //3
                //scroller 改变速率的惯性滑动
                mIsFingerUp = true ;

                final int mCurrScrollY = getScrollY();
                mEventCurrY = ev.getY();
                //阻尼回弹 & 惯性滚动
                if (mCurrScrollY < 0) {
                    //顶部脱离
                    scroller.startScroll(0,mCurrScrollY,0,-mCurrScrollY, SPRING_DEFAULT_TIME_MS);
                }else if(mCurrScrollY > mMaxOverScrollY){
                    //底部脱离
                    scroller.startScroll(0,mCurrScrollY,0,-(mCurrScrollY - mMaxOverScrollY), SPRING_DEFAULT_TIME_MS);
                }else{
                    //未脱离，需要惯性滑动
                    initVelocityTrackerIfNotExists();
                    velocityTracker.computeCurrentVelocity(1000); //得到的速度是 ?px/s
                    final int currVelocityY = Math.min(VELOCITY_SYSTEM_MAX,(int)velocityTracker.getYVelocity());
                    scroller.fling(0,mCurrScrollY,0,-currVelocityY,
                            0,0,-OVER_SPRING_DEFAULT_DISTANCE_PX, mMaxOverScrollY + OVER_SPRING_DEFAULT_DISTANCE_PX);
                    velocityTracker.clear();
                }
                postInvalidate();
                break;
            default:
                break;
        }
        return true ;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        //如果还在计算，说明还在滚动
        mIsScrolling = scroller.computeScrollOffset() ;

        //越界阻尼
        final int mCurrScrollY = getScrollY();
        if(mIsFingerUp && (mCurrScrollY == -OVER_SPRING_DEFAULT_DISTANCE_PX
                ||mCurrScrollY == mMaxOverScrollY + OVER_SPRING_DEFAULT_DISTANCE_PX)){
            if(scroller.computeScrollOffset()){
                scroller.forceFinished(true);
            }
            if (mCurrScrollY < 0) {
                //顶部脱离
                scroller.startScroll(0,mCurrScrollY-1,0,-mCurrScrollY, OVER_SPRING_DEFAULT_TIME_MS);
            }else if(mCurrScrollY > mMaxOverScrollY){
                //底部脱离
                scroller.startScroll(0,mCurrScrollY+1,0,-(mCurrScrollY - mMaxOverScrollY), OVER_SPRING_DEFAULT_TIME_MS);
            }
            invalidate();
        }

        //动画未结束 ， 计算还在进行中
        if(mIsScrolling){
            // view 滚动到对应位置
            this.scrollTo(scroller.getCurrX(),scroller.getCurrY());
            // 出发draw
            postInvalidate();
        }
    }
//
//    /**
//     * 根据手指move的y来换算阻尼的px
//     * 默认递进采集效率为-30%
//     * @param moveY
//     * @return
//     */
//    private int convertOffsetYbyMoveY(int moveY){
//        return (int) (moveY * 0.7f);
////        int y = Math.abs(moveY);
////        if(y < 100){
////            return y ;
////        }
////        int offsetY = 0 ;
////        final int levels = y / 100 ;
////        for (int i = 0; i < levels; i++) {
////            offsetY += 100 * Math.pow(0.9f,i) ;
////        }
////        offsetY += (y%100) * Math.pow(0.9f,levels + 1) ;
////
////        return offsetY * (moveY > 0 ? 1: -1) ;
//    }
}
