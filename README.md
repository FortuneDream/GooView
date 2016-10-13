# GooView
## 这是我做过的最酷炫的View！！没有之一！！！
* Path
```Java
 path.moveTo(mStickPoints[0].x, mStickPoints[0].y);
                //画贝塞尔曲线:第一个点是控制点,第二个点是目标点
                path.quadTo(mControlPoint.x, mControlPoint.y, mDragPoints[0].x, mDragPoints[0].y);
                //画直线
                path.lineTo(mDragPoints[1].x, mDragPoints[1].y);
                //第二条贝塞尔
                path.quadTo(mControlPoint.x, mControlPoint.y, mStickPoints[1].x, mStickPoints[1].y);
                path.close();//自动封闭（回到起始点）
```
* 其他的？ 直接看代码吧。。。反正能抽出来借鉴的代码应该不会很多了。
