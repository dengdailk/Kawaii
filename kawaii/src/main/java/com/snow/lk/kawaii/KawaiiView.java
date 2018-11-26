package com.snow.lk.kawaii;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;




/**
 * Created by 29199 on 2018/1/27.
 */

public class KawaiiView extends View {

    // 固定方块 & 移动方块变量
    private fixedBlock[] mfixedBlocks;
    private MoveBlock mMoveBlock;

    //方块属性

    private int lineNumber;//方块行数
    private float half_BlockWidth;//半个方块宽度
    private float blockInterval;//方格间隔宽度
    private int blockColor;
    private int initPosition;
    private boolean isClock_Wise;
    private int mCurrEmptyPosition;

    private Paint mPaint;

    //方块的圆角半径
    private float moveBlock_Angle;//移动
    private float fixBlock_Angle;//固定

    // 动画属性
    private float mRotateDegree;
    private boolean mAllowRoll = false;
    private boolean isMoving = false;
    private int moveSpeed = 250;

    // 动画插值器（默认 = 线性）
    private Interpolator move_Interpolator;
    private AnimatorSet mAnimatorSet;

    public KawaiiView(Context context) {
        super(context);
    }

    public KawaiiView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        //1.
        initAttrs(context, attrs);
        //2.
        intit();
    }

    public KawaiiView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //1.
        initAttrs(context, attrs);
        //2.
        intit();
    }

    public KawaiiView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        //1.
        initAttrs(context, attrs);
        //2.
        intit();
    }


    private void intit() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(blockColor);

        initBlocks(initPosition);
    }

    private void initBlocks(int initPosition) {

        mfixedBlocks = new fixedBlock[lineNumber * lineNumber];

        for (int i = 0; i < mfixedBlocks.length; i++) {

            mfixedBlocks[i] = new fixedBlock();
            mfixedBlocks[i].index = i;
            mfixedBlocks[i].isShow = initPosition != i;
            mfixedBlocks[i].rectF = new RectF();
        }

        mMoveBlock = new MoveBlock();
        mMoveBlock.rectF = new RectF();
        mMoveBlock.isShow = false;

        relate_OuterBlock(mfixedBlocks,isClock_Wise);
    }

    private void relate_OuterBlock(fixedBlock[] fixedBlocks, boolean isClockwise) {
        int lineCount = (int) Math.sqrt(fixedBlocks.length);

        // 情况1：关联第1行
        for (int i = 0; i < lineCount; i++) {
            // 位于最左边
            if (i % lineCount == 0) {
                fixedBlocks[i].next = isClockwise ? fixedBlocks[i + lineCount] : fixedBlocks[i + 1];
                // 位于最右边
            } else if ((i + 1) % lineCount == 0) {
                fixedBlocks[i].next = isClockwise ? fixedBlocks[i - 1] : fixedBlocks[i + lineCount];
                // 中间
            } else {
                fixedBlocks[i].next = isClockwise ? fixedBlocks[i - 1] : fixedBlocks[i + 1];
            }
        }
        // 情况2：关联最后1行
        for (int i = (lineCount - 1) * lineCount; i < lineCount * lineCount; i++) {
            // 位于最左边
            if (i % lineCount == 0) {
                fixedBlocks[i].next = isClockwise ? fixedBlocks[i + 1] : fixedBlocks[i - lineCount];
                // 位于最右边
            } else if ((i + 1) % lineCount == 0) {
                fixedBlocks[i].next = isClockwise ? fixedBlocks[i - lineCount] : fixedBlocks[i - 1];
                // 中间
            } else {
                fixedBlocks[i].next = isClockwise ? fixedBlocks[i + 1] : fixedBlocks[i - 1];
            }
        }

        // 情况3：关联第1列
        for (int i = lineCount; i <= (lineCount - 1) * lineCount; i += lineCount) {
            // 若是第1列最后1个
            if (i == (lineCount - 1) * lineCount) {
                fixedBlocks[i].next = isClockwise ? fixedBlocks[i + 1] : fixedBlocks[i - lineCount];
                continue;
            }
            fixedBlocks[i].next = isClockwise ? fixedBlocks[i + lineCount] : fixedBlocks[i - lineCount];
        }

        // 情况4：关联最后1列
        for (int i = 2 * lineCount - 1; i <= lineCount * lineCount - 1; i += lineCount) {
            // 若是最后1列最后1个
            if (i == lineCount * lineCount - 1) {
                fixedBlocks[i].next = isClockwise ? fixedBlocks[i - lineCount] : fixedBlocks[i - 1];
                continue;
            }
            fixedBlocks[i].next = isClockwise ? fixedBlocks[i - lineCount] : fixedBlocks[i + lineCount];
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // 调用时刻：onCreate之后onDraw之前调用；view的大小发生改变就会调用该方法
        // 使用场景：用于屏幕的大小改变时，需要根据屏幕宽高来决定的其他变量可以在这里进行初始化操作
        super.onSizeChanged(w, h, oldw, oldh);

        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();

        // 1. 设置移动方块的旋转中心坐标
        int cx = measuredWidth / 2;
        int cy = measuredHeight / 2;

        // 2. 设置固定方块的位置 ->>关注1
        fixedBlockPosition(mfixedBlocks, cx, cy, blockInterval, half_BlockWidth);
        // 3. 设置移动方块的位置 ->>关注2
        MoveBlockPosition(mfixedBlocks, mMoveBlock, initPosition, isClock_Wise);
    }

    private void fixedBlockPosition(fixedBlock[] fixedBlocks, int cx, int cy, float dividerWidth, float halfSquareWidth) {

        // 1. 确定第1个方块的位置
        // 分为2种情况：行数 = 偶 / 奇数时
        // 主要是是数学知识，此处不作过多描述
        float squareWidth = halfSquareWidth * 2;
        int lineCount = (int) Math.sqrt(fixedBlocks.length);
        float firstRectLeft = 0;
        float firstRectTop = 0;

        // 情况1：当行数 = 偶数时
        if (lineCount % 2 == 0) {
            int squareCountInAline = lineCount / 2;
            int diviCountInAline = squareCountInAline - 1;
            float firstRectLeftTopFromCenter = squareCountInAline * squareWidth
                    + diviCountInAline * dividerWidth
                    + dividerWidth / 2;
            firstRectLeft = cx - firstRectLeftTopFromCenter;
            firstRectTop = cy - firstRectLeftTopFromCenter;

            // 情况2：当行数 = 奇数时
        } else {
            int squareCountInAline = lineCount / 2;
            int diviCountInAline = squareCountInAline;
            float firstRectLeftTopFromCenter = squareCountInAline * squareWidth
                    + diviCountInAline * dividerWidth
                    + halfSquareWidth;
            firstRectLeft = cx - firstRectLeftTopFromCenter;
            firstRectTop = cy - firstRectLeftTopFromCenter;
            firstRectLeft = cx - firstRectLeftTopFromCenter;
            firstRectTop = cy - firstRectLeftTopFromCenter;
        }

        // 2. 确定剩下的方块位置
        // 思想：把第一行方块位置往下移动即可
        // 通过for循环确定：第一个for循环 = 行，第二个 = 列
        for (int i = 0; i < lineCount; i++) {//行
            for (int j = 0; j < lineCount; j++) {//列
                if (i == 0) {
                    if (j == 0) {
                        fixedBlocks[0].rectF.set(firstRectLeft, firstRectTop,
                                firstRectLeft + squareWidth, firstRectTop + squareWidth);
                    } else {
                        int currIndex = i * lineCount + j;
                        fixedBlocks[currIndex].rectF.set(fixedBlocks[currIndex - 1].rectF);
                        fixedBlocks[currIndex].rectF.offset(dividerWidth + squareWidth, 0);
                    }
                } else {
                    int currIndex = i * lineCount + j;
                    fixedBlocks[currIndex].rectF.set(fixedBlocks[currIndex - lineCount].rectF);
                    fixedBlocks[currIndex].rectF.offset(0, dividerWidth + squareWidth);
                }
            }
        }
    }
    private void MoveBlockPosition(fixedBlock[] fixedBlocks,
                                   MoveBlock moveBlock, int initPosition, boolean isClockwise) {

        // 移动方块位置 = 设置初始的空出位置 的下一个位置（next）
        // 下一个位置 通过 连接的外部方块位置确定
        fixedBlock fixedBlock = fixedBlocks[initPosition];
        moveBlock.rectF.set(fixedBlock.next.rectF);
    }
    private void initAttrs(Context context, AttributeSet attrs) {

// 控件资源名称
        @SuppressLint("CustomViewStyleable") TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.Kawaii_LoadingView);

        // 方块行数量(最少3行)
        lineNumber = typedArray.getInteger(R.styleable.Kawaii_LoadingView_lineNumber, 3);
        if (lineNumber < 3) {
            lineNumber = 3;
        }

        // 半个方块的宽度（dp）
        half_BlockWidth = typedArray.getDimension(R.styleable.Kawaii_LoadingView_half_BlockWidth, 30);
        // 方块间隔宽度（dp）
        blockInterval = typedArray.getDimension(R.styleable.Kawaii_LoadingView_blockInterval, 10);

        // 移动方块的圆角半径
        moveBlock_Angle = typedArray.getFloat(R.styleable.Kawaii_LoadingView_moveBlock_Angle, 10);
        // 固定方块的圆角半径
        fixBlock_Angle = typedArray.getFloat(R.styleable.Kawaii_LoadingView_fixBlock_Angle, 30);
        // 通过设置两个方块的圆角半径使得二者不同可以得到更好的动画效果哦

        // 方块颜色（使用十六进制代码，如#333、#8e8e8e）
        int defaultColor = context.getResources().getColor(R.color.colorAccent); // 默认颜色
        blockColor = typedArray.getColor(R.styleable.Kawaii_LoadingView_blockColor, defaultColor);

        // 移动方块的初始位置（即空白位置）
        initPosition = typedArray.getInteger(R.styleable.Kawaii_LoadingView_initPosition, 0);

        // 由于移动方块只能是外部方块，所以这里需要判断方块是否属于外部方块 -->关注1
        if (isInsideTheRect(initPosition, lineNumber)) {
            initPosition = 0;
        }
        // 动画方向是否 = 顺时针旋转
        isClock_Wise = typedArray.getBoolean(R.styleable.Kawaii_LoadingView_isClock_Wise, true);

        // 移动方块的移动速度
        // 注：不建议使用者将速度调得过快
        // 因为会导致ValueAnimator动画对象频繁重复的创建，存在内存抖动
        moveSpeed = typedArray.getInteger(R.styleable.Kawaii_LoadingView_moveSpeed, 250);

        // 设置移动方块动画的插值器
        int move_InterpolatorResId = typedArray.getResourceId(R.styleable.Kawaii_LoadingView_move_Interpolator,
                android.R.anim.linear_interpolator);
        move_Interpolator = AnimationUtils.loadInterpolator(context, move_InterpolatorResId);

        // 当方块移动后，需要实时更新的空白方块的位置
        mCurrEmptyPosition = initPosition;

        // 释放资源
        typedArray.recycle();

    }

    private boolean isInsideTheRect(int initPosition, int lineNumber) {
        if (initPosition < lineNumber) {
            return false;
        } else if (initPosition > (lineNumber * lineNumber - 1 - lineNumber)) {
            return false;
        } else if ((initPosition + 1) % lineNumber == 0) {
            return false;
        } else if (initPosition % lineNumber == 0) {
            return false;
        }
        return true;
    }


    class fixedBlock {
        RectF rectF;
        int index;
        boolean isShow;
        fixedBlock next;


    }

    class MoveBlock {

        RectF rectF;
        int index;
        boolean isShow;
        float cx;
        float cy;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // 1. 绘制内部方块（固定的）
        for (int i = 0; i < mfixedBlocks.length; i++) {
            // 根据标志位判断是否需要绘制
            if (mfixedBlocks[i].isShow) {
                // 传入方块位置参数、圆角 & 画笔属性
                canvas.drawRoundRect(mfixedBlocks[i].rectF, fixBlock_Angle, fixBlock_Angle, mPaint);
            }
        }
        // 2. 绘制移动的方块（）
        if (mMoveBlock.isShow) {
            canvas.rotate(isClock_Wise ? mRotateDegree : -mRotateDegree, mMoveBlock.cx, mMoveBlock.cy);
            canvas.drawRoundRect(mMoveBlock.rectF, moveBlock_Angle, moveBlock_Angle, mPaint);
        }

    }


    public void startMoving() {

        // 1. 根据标志位 & 视图是否可见确定是否需要启动动画
        // 此处设置是为了方便手动 & 自动停止动画
        if (isMoving || getVisibility() != View.VISIBLE ) {
            return;
        }

        // 设置标记位：以便是否停止动画
        isMoving = true;
        mAllowRoll = true;

        // 2. 获取固定方块当前的空位置，即移动方块当前位置
        fixedBlock currEmptyfixedBlock = mfixedBlocks[mCurrEmptyPosition];
        // 3. 获取移动方块的到达位置，即固定方块当前空位置的下1个位置
        fixedBlock movedBlock = currEmptyfixedBlock.next;

        // 4. 设置方块动画 = 移动方块平移 + 旋转
        // 原理：设置平移动画（Translate） + 旋转动画（Rotate），最终通过组合动画（AnimatorSet）组合起来

        // 4.1 设置平移动画：createTranslateValueAnimator（） ->>关注1
        mAnimatorSet = new AnimatorSet();
        // 平移路径 = 初始位置 - 到达位置
        ValueAnimator translateConrtroller = createTranslateValueAnimator(currEmptyfixedBlock,
                movedBlock);

        // 4.2 设置旋转动画：createMoveValueAnimator(（）->>关注3
        ValueAnimator moveConrtroller = createMoveValueAnimator();

        // 4.3 将两个动画组合起来
        // 设置移动的插值器
        mAnimatorSet.setInterpolator(move_Interpolator);
        mAnimatorSet.playTogether(translateConrtroller, moveConrtroller);
        mAnimatorSet.addListener(new AnimatorListenerAdapter() {

            // 动画开始时进行一些设置
            @Override
            public void onAnimationStart(Animator animation) {

                // 每次动画开始前都需要更新移动方块的位置 ->>关注4
                updateMoveBlock();

                // 让移动方块的初始位置的下个位置也隐藏 = 两个隐藏的方块
                mfixedBlocks[mCurrEmptyPosition].next.isShow = false;

                // 通过标志位将移动的方块显示出来
                mMoveBlock.isShow = true;
            }

            // 结束时进行一些设置
            @Override
            public void onAnimationEnd(Animator animation) {
                isMoving = false;
                mfixedBlocks[mCurrEmptyPosition].isShow = true;
                mCurrEmptyPosition = mfixedBlocks[mCurrEmptyPosition].next.index;

                // 将移动的方块隐藏
                mMoveBlock.isShow = false;

                // 通过标志位判断动画是否要循环播放
                if (mAllowRoll) {
                    startMoving();
                }
            }
        });

        // 启动动画
        mAnimatorSet.start();
    }

    /**
     * 关注1：设置平移动画
     */
    private ValueAnimator createTranslateValueAnimator(fixedBlock currEmptyfixedBlock,
                                                       fixedBlock moveBlock) {
        float startAnimValue = 0;
        float endAnimValue = 0;
        PropertyValuesHolder left = null;
        PropertyValuesHolder top = null;

        // 1. 设置移动速度
        ValueAnimator valueAnimator = new ValueAnimator().setDuration(moveSpeed);


        // 2. 设置移动方向
        // 情况分为：4种，分别是移动方块向左、右移动 和 上、下移动
        // 注：需考虑 旋转方向（isClock_Wise），即顺逆时针 ->>关注1.1
        if (isNextRollLeftOrRight(currEmptyfixedBlock, moveBlock)) {

            // 情况1：顺时针且在第一行 / 逆时针且在最后一行时，移动方块向右移动
            if (isClock_Wise && currEmptyfixedBlock.index > moveBlock.index || !isClock_Wise && currEmptyfixedBlock.index > moveBlock.index) {

                startAnimValue = moveBlock.rectF.left;
                endAnimValue = moveBlock.rectF.left + blockInterval;

                // 情况2：顺时针且在最后一行 / 逆时针且在第一行，移动方块向左移动
            } else if (isClock_Wise && currEmptyfixedBlock.index < moveBlock.index
                    || !isClock_Wise && currEmptyfixedBlock.index < moveBlock.index) {

                startAnimValue = moveBlock.rectF.left;
                endAnimValue = moveBlock.rectF.left - blockInterval;
            }

            // 设置属性值
            left = PropertyValuesHolder.ofFloat("left", startAnimValue, endAnimValue);
            valueAnimator.setValues(left);

        } else {
            // 情况3：顺时针且在最左列 / 逆时针且在最右列，移动方块向上移动
            if (isClock_Wise && currEmptyfixedBlock.index < moveBlock.index
                    || !isClock_Wise && currEmptyfixedBlock.index < moveBlock.index) {

                startAnimValue = moveBlock.rectF.top;
                endAnimValue = moveBlock.rectF.top - blockInterval;

                // 情况4：顺时针且在最右列 / 逆时针且在最左列，移动方块向下移动
            } else if (isClock_Wise && currEmptyfixedBlock.index > moveBlock.index
                    || !isClock_Wise && currEmptyfixedBlock.index > moveBlock.index) {
                startAnimValue = moveBlock.rectF.top;
                endAnimValue = moveBlock.rectF.top + blockInterval;
            }

            // 设置属性值
            top = PropertyValuesHolder.ofFloat("top", startAnimValue, endAnimValue);
            valueAnimator.setValues(top);
        }

        // 3. 通过监听器更新属性值
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Object left = animation.getAnimatedValue("left");
                Object top = animation.getAnimatedValue("top");
                if (left != null) {
                    mMoveBlock.rectF.offsetTo((Float) left, mMoveBlock.rectF.top);
                }
                if (top != null) {
                    mMoveBlock.rectF.offsetTo(mMoveBlock.rectF.left, (Float) top);
                }
                // 实时更新旋转中心 ->>关注2
                setMoveBlockRotateCenter(mMoveBlock, isClock_Wise);

                // 更新绘制
                invalidate();
            }
        });
        return valueAnimator;
    }
    // 回到原处


    /**
     * 关注1.1：判断移动方向
     * 即上下 or 左右
     */
    private boolean isNextRollLeftOrRight(fixedBlock currEmptyfixedBlock, fixedBlock rollSquare) {
        if (currEmptyfixedBlock.rectF.left - rollSquare.rectF.left == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 关注2：实时更新移动方块的旋转中心
     * 因为方块在平移旋转过程中，旋转中心也会跟着改变，因此需要改变MoveBlock的旋转中心（cx,cy）
     */

    private void setMoveBlockRotateCenter(MoveBlock moveBlock, boolean isClockwise) {

        // 情况1：以移动方块的左上角为旋转中心
        if (moveBlock.index == 0) {
            moveBlock.cx = moveBlock.rectF.right;
            moveBlock.cy = moveBlock.rectF.bottom;

            // 情况2：以移动方块的右下角为旋转中心
        } else if (moveBlock.index == lineNumber * lineNumber - 1) {
            moveBlock.cx = moveBlock.rectF.left;
            moveBlock.cy = moveBlock.rectF.top;

            // 情况3：以移动方块的左下角为旋转中心
        } else if (moveBlock.index == lineNumber * (lineNumber - 1)) {
            moveBlock.cx = moveBlock.rectF.right;
            moveBlock.cy = moveBlock.rectF.top;

            // 情况4：以移动方块的右上角为旋转中心
        } else if (moveBlock.index == lineNumber - 1) {
            moveBlock.cx = moveBlock.rectF.left;
            moveBlock.cy = moveBlock.rectF.bottom;
        }

        //以下判断与旋转方向有关：即顺 or 逆顺时针

        // 情况1：左边
        else if (moveBlock.index % lineNumber == 0) {
            moveBlock.cx = moveBlock.rectF.right;
            moveBlock.cy = isClockwise ? moveBlock.rectF.top : moveBlock.rectF.bottom;

            // 情况2：上边
        } else if (moveBlock.index < lineNumber) {
            moveBlock.cx = isClockwise ? moveBlock.rectF.right : moveBlock.rectF.left;
            moveBlock.cy = moveBlock.rectF.bottom;

            // 情况3：右边
        } else if ((moveBlock.index + 1) % lineNumber == 0) {
            moveBlock.cx = moveBlock.rectF.left;
            moveBlock.cy = isClockwise ? moveBlock.rectF.bottom : moveBlock.rectF.top;

            // 情况4：下边
        } else if (moveBlock.index > (lineNumber - 1) * lineNumber) {
            moveBlock.cx = isClockwise ? moveBlock.rectF.left : moveBlock.rectF.right;
            moveBlock.cy = moveBlock.rectF.top;
        }
    }
    // 回到原处




    /**
     * 关注3：设置旋转动画
     */
    private ValueAnimator createMoveValueAnimator() {

        // 通过属性动画进行设置
        ValueAnimator moveAnim = ValueAnimator.ofFloat(0, 90).setDuration(moveSpeed);

        moveAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Object animatedValue = animation.getAnimatedValue();

                // 赋值
                mRotateDegree = (float) animatedValue;

                // 更新视图
                invalidate();
            }
        });
        return moveAnim;
    }
    // 回到原处

    /**
     * 关注4：更新移动方块的位置
     */

    private void updateMoveBlock() {

        mMoveBlock.rectF.set(mfixedBlocks[mCurrEmptyPosition].next.rectF);
        mMoveBlock.index = mfixedBlocks[mCurrEmptyPosition].next.index;
        setMoveBlockRotateCenter(mMoveBlock, isClock_Wise);
    }
    // 回到原处


    /**
     * 停止动画
     */
    public void stopMoving() {

        // 通过标记位来设置
        mAllowRoll = false;
    }
}