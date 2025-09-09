/**
 * @Author Hiro
 * @Date 2025/09/09 01:56
 * @AIDE AIDE+ 
 */
package com.losthiro.ottohubclient.impl;
import android.view.*;
import android.view.animation.*;
import android.animation.*;
import android.os.*;

public class ClientAnim {
	public static final int ANIM_SPEED = 300;
	private static final float POTATION_VALUE = 7.0f;
	private static final float SCALE_END = 0.95f;
	private static final float SHADOW_END = 0.0f;
	public static OvershootInterpolator interpolator = new OvershootInterpolator(3.0f);

	public static int startAnimDown(View view, boolean isAnim, float x, float y) {
		int status = 0;
		if (view.isClickable() && isAnim) {
			//if (isAnim) {
			int width = view.getWidth();
			int height = view.getHeight();
			if ((width / 5) * 2 < x && y < (width / 5) * 3 && (height / 5) * 2 < y && y < (height / 5) * 3) {
				status = 0;
			} else if (x >= width / 2 || y >= height / 2) {
				if (x < width / 2 && y >= height / 2) {
					status = (((float) width) - x) / ((float) (width / 2)) <= y / ((float) (height / 2)) ? 3 : 4;
				} else if (x >= width / 2 && y >= height / 2) {
					status = (((float) width) - x) / ((float) (width / 2)) <= (((float) height) - y)
							/ ((float) (height / 2)) ? 2 : 3;
				} else if (x / (width / 2) > (height - y) / (height / 2)) {
					status = 2;
				}
			} else if (x / (width / 2) <= y / (height / 2)) {
				status = 4;
			}
			String rotation = null;
			switch (status) {
				case 0 :
					ObjectAnimator scale = ObjectAnimator.ofPropertyValuesHolder(view,
							PropertyValuesHolder.ofFloat("translationZ", view.getTranslationZ(), 0.0f),
							PropertyValuesHolder.ofFloat("scaleX", view.getScaleX(), SCALE_END),
							PropertyValuesHolder.ofFloat("scaleY", view.getScaleY(), SCALE_END));
					scale.setDuration(ANIM_SPEED);
					scale.setInterpolator(interpolator);
					scale.start();
					return 0;
				case 1 :
				case 3 :
					rotation = "rotationX";
					break;
				case 2 :
				case 4 :
					rotation = "rotationY";
					break;
			}
			if (rotation != null) {
				froBig_ToSmall(view, status, rotation);
			}
		}
		return status;
	}

	public static void startAnimUp(View view, int status) {
		int i2;
		if (view.isClickable()) {
			if (status != 0) {
				switch (status) {
					case 1 :
					case 3 :
						froSmall_ToBig(view, status, "rotationX");
						//                        i2 = ai[1];
						//                        if (i2 < 0 || i2 % (0x04f5ccad ^ i2) != 0) {
						//                            return;
						//                        }
						break;
					case 2 :
					case 4 :
						froSmall_ToBig(view, status, "rotationY");
						break;
					//                        i2 = ai[1];
					//                        if (i2 < 0) {
					//                            return;
					//                        } else {
					//                            return;
					//                        }
					default :
//						froSmall_ToBig(view, status, str);
						//                        i2 = ai[1];
						//                        if (i2 < 0) {
						//                        }
						break;
				}
			} else {
				froSmall_ToBig(view);
				//                int i3 = ai[0];
				//                if (i3 < 0 || (i3 & (0x019080fc ^ i3)) == 4478721) {
				//                }
				//                return;
			}
		}
	}

	public static void froBig_ToSmall(View view) {
		//        int i;
		//        int i2;
		float translate = 0;
		try {
			Object tag = view.getTag(0);
			if (Build.VERSION.SDK_INT >= 21) {
				translate = view.getTranslationZ();
				if (tag == null || !(tag instanceof Float)) {
					view.setTag(0, new Float(translate));
					//                    int i3 = ad[0];
					//                    if (i3 < 0 || i3 % (0x03c7ba0f ^ i3) == 0x054385cb) {
					//                    }
				}
				ObjectAnimator duration = ObjectAnimator.ofPropertyValuesHolder(view,
						PropertyValuesHolder.ofFloat("translationZ", translate, 0.0f),
						PropertyValuesHolder.ofFloat("scaleX", view.getScaleX(), SCALE_END),
						PropertyValuesHolder.ofFloat("scaleY", view.getScaleY(), SCALE_END));
				duration.setDuration(ANIM_SPEED);
				duration.setInterpolator(interpolator);
				//            int i4 = ad[1];
				//            if (i4 >= 0) {
				//                do {
				//                    i2 = i4 & (6646361 ^ i4);
				//                    i4 = 0x03920000;
				//                } while (i2 != 0x03920000);
				//            }
				duration.start();
				//            int i5 = ad[2];
				//            if (i5 >= 0) {
				//                do {
				//                    i = i5 & (0x0411d1ab ^ i5);
				//                    i5 = 0x03cc2400;
				//                } while (i != 0x03cc2400);
				//            }
			}
		} catch (Exception e) {
		}
	}

	public static void froSmall_ToBig(View view) {
		float lastTranslate = 0;
		int i;
		int i2;
		int i3;
		float currentTranslate = 0;
		try {
			Object tag = view.getTag(0x7f1000fc);
			if (Build.VERSION.SDK_INT >= 21) {
				currentTranslate = view.getTranslationZ();
				if (tag != null && (tag instanceof Float)) {
					lastTranslate = ((Float) tag).floatValue();
					//                    i = af[0];
					//                    if (i >= 0 || (i & (15855531 ^ i)) == 0x02061010) {
					//                    }
					//                    i2 = af[1];
					//                    if (i2 < 0) {
					//                        do {
					//                            i3 = i2 & (0x03b3007a ^ i2);
					//                            i2 = 51717;
					//                        } while (i3 != 51717);
					//                        return;
					//                    }
				}
			}
			ObjectAnimator scale = ObjectAnimator.ofPropertyValuesHolder(view,
					PropertyValuesHolder.ofFloat("translationZ", view.getTranslationZ(), lastTranslate),
					PropertyValuesHolder.ofFloat("scaleX", view.getScaleX(), SCALE_END),
					PropertyValuesHolder.ofFloat("scaleY", view.getScaleY(), SCALE_END));
			scale.setDuration(ANIM_SPEED);
			scale.setInterpolator(interpolator);
			//            i = af[0];
			//            if (i >= 0) {
			//            }
			scale.start();
			//            i2 = af[1];
			//            if (i2 < 0) {
			//            }
		} catch (Exception e) {
		}
	}

	private static void froSmall_ToBig(View view, int i, String str) {
		ObjectAnimator duration = ObjectAnimator.ofFloat(view, str,
				(i == 2 || i == 4) ? (int) view.getRotationY() : (int) view.getRotationX(), 0);
		duration.setDuration(ANIM_SPEED);
		duration.setInterpolator(interpolator);
		//        int i2 = ag[0];
		//        if (i2 < 0 || i2 % (0x0336f293 ^ i2) == 0x043416e2) {
		//        }
		duration.start();
		//        int i3 = ag[1];
		//        if (i3 < 0) {
		//            return;
		//        }
		//        do {
		//        } while ((i3 & (0x023a3064 ^ i3)) <= 0);
	}

	private static void froBig_ToSmall(View view, int i, String str) {
		//        int i2;
		//        do {
		ObjectAnimator duration = ObjectAnimator.ofFloat(view, str,
				(i == 2 || i == 4) ? (int) view.getRotationY() : (int) view.getRotationX(),
				(i == 3 || i == 4) ? -POTATION_VALUE : POTATION_VALUE);
		duration.setDuration(ANIM_SPEED);
		duration.setInterpolator(interpolator);
		//            int i3 = ae[0];
		//            if (i3 < 0 || i3 % (0x0458fffd ^ i3) == 8224946) {
		//            }
		duration.start();
		//            i2 = ae[1];
		//            if (i2 < 0) {
		//                return;
		//            }
		//        } while ((i2 & (0x01c98306 ^ i2)) == 0);
	}
}

