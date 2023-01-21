package org.blinksd.board;

import static android.view.Gravity.CENTER;
import static android.view.View.OnTouchListener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.blinksd.utils.color.ColorUtils;
import org.blinksd.utils.layout.DensityUtils;

import java.util.Locale;

public class SuperBoard extends FrameLayout implements OnTouchListener {

	protected int selected = 0, shift = 0, keyclr = -1, hp = 40, wp = 100, y, shrad = 0, shclr = -1, txtclr = Color.WHITE, txts = 0, vib = 0, mult = 1, act = MotionEvent.ACTION_UP, iconmulti = 1;
	protected float txtsze = -1;
	protected static final int TAG_LP = R.string.app_name, TAG_NP = R.string.hello_world;
	private boolean clear = false;
	private boolean lng = false;
	private boolean dpopup = false;
	private boolean ppreview = false;
	protected Drawable keybg = null;
	private final String KEY_REPEAT = "10RePeAt01";
	private String[] x;
	private Typeface cFont = Typeface.DEFAULT;
	
	public static final int KEYCODE_CLOSE_KEYBOARD = -100;
	public static final int KEYCODE_SWITCH_LANGUAGE = -101;
	public static final int KEYCODE_OPEN_EMOJI_LAYOUT = -102;
	
	public static final int SHIFT_OFF = 0;
	public static final int SHIFT_ON = 1;
	public static final int SHIFT_LOCKED = 2;

	protected final Handler mHandler = new Handler(Looper.getMainLooper()){
		@Override
		public void handleMessage(Message msg){
			View v = null;
			if(msg.obj instanceof View){
				v = (View) msg.obj;
			}

			switch(msg.what){
				case 0:
					removeMessages(3);
					sendEmptyMessage(3);
					break;
				case 1:
					removeMessages(1);
					switch(act){
						case MotionEvent.ACTION_UP:
							removeMessages(3);
							sendEmptyMessage(3);
							break;
						case MotionEvent.ACTION_DOWN:
							if(isHasPopup(v)){
								onPopupEvent();
								removeMessages(3);
								sendEmptyMessage(3);
							} else if(isHasLongPressEvent(v)){
								String[] a = v.getTag(TAG_LP).toString().split(":");
								y = Integer.parseInt(a[0]);
								if(Boolean.parseBoolean(a[1])){
									sendKeyEvent(y);
								} else {
									commitText((char)y+"");
								}
								playSound(y);
								removeMessages(3);
								sendEmptyMessage(3);
							} else {
								if(!((InputMethodService) getContext()).isInputViewShown()) {
									act = MotionEvent.ACTION_UP;
								}
								Message m = obtainMessage(2,msg.obj);
								removeMessages(2);
								sendMessage(m);
							}
							break;
					}
					break;
				case 2:
					if(act == MotionEvent.ACTION_UP){
						removeMessages(3);
						sendEmptyMessage(3);
					} else {
						sendDefaultKeyboardEvent(v);
						if(isRepeat){
							Message n = obtainMessage(1,msg.obj);
							sendMessageDelayed(n, ((mult>1?15:20)*mult)*(lng?1:20));
							if(!lng) lng = true;
						} else {
							removeMessages(3);
							sendEmptyMessage(3);
						}
					}
					break;
				case 3:
					lng = false;
					removeMessages(3);
					afterKeyboardEvent();
					break;
				case 5:
					setEnabled(false);
					break;
				case 6:
					setEnabled(true);
					break;
			}
		}
	};
	
	private final Vibrator vb;

	public SuperBoard(Context c){
		super(c);
		if(c instanceof InputMethodService){
			curr = (InputMethodService) c;
		}
		vb = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
		// trigSystemSuggestions();
		setLayoutParams(new LayoutParams(-1,-1));
		setBackgroundColor(0xFF212121);
		createEmptyLayout();
		setKeyboardHeight(hp);
	}
	
	public void onKeyboardEvent(View v){
		
	}
	
	public void afterKeyboardEvent(){
		
	}
	
	public void onPopupEvent(){
		
	}
	
	public void afterPopupEvent(){
		mHandler.removeMessages(0);
		mHandler.sendEmptyMessage(0);
	}
	
	public void switchLanguage(){
		
	}
	
	public void openEmojiLayout(){
		
	}
	
	/*
	private void trigSystemSuggestions(){
		Locale.setDefault(loc);
		Configuration c = new Configuration();
		c.locale = loc;
		getResources().updateConfiguration(c,null);
	}
	*/
	
	public void setPadding(int p){
		setPadding(p,p,p,p);
	}

	public static int dp(int px){
		return (int)(Resources.getSystem().getDisplayMetrics().density * px);
	}
	
	public void setCustomFont(Typeface type){
		cFont = type;
	}
	
	public int getKeyboardHeight(){
		return getLayoutParams().height;
	}
	
	public int getKeyboardHeightPercent(){
		return hp;
	}
	
	public int getKeyboardWidth(){
		return getLayoutParams().width;
	}
	
	public int getKeyboardWidthPercent(){
		return wp;
	}
	
	public void fixHeight(){
		setKeyboardHeight(hp);
		for(int i = 0;i < getChildCount();i++){
			for(int g = 0;g < getKeyboard(i).getChildCount();g++){
				getRow(i,g).setKeyWidths();
			}
		}
	}

	public void setBackground(Drawable background){
		setBackgroundDrawable(background);
	}

	public void setBackgroundDrawable(Drawable background){
		super.setBackgroundDrawable(background.getConstantState().newDrawable());
	}
	
	public void setKeyVibrateDuration(int dur){
		vib = dur;
	}

	public void clear(){
		if(clear){
			for(int i = 0;i < getChildCount();i++){
				ViewGroup k = getKeyboard(i);
				for(int g = 0;g < k.getChildCount();g++)
					getRow(i,g).removeAllViewsInLayout();
				k.removeAllViewsInLayout();
			}
			removeAllViewsInLayout();
			createEmptyLayout();
			clear = false;
		}
	}

	public void setKeyTintColor(Key k, int normalColor, int pressColor){
		Drawable d = k.getBackground();
		try {
			if(Build.VERSION.SDK_INT > 21){
				d.setTintList(getTintListWithStates(normalColor, pressColor));
			} else {
				d.setColorFilter(normalColor,PorterDuff.Mode.SRC_ATOP);
			}
		} catch(Exception e){
			d.setColorFilter(normalColor,PorterDuff.Mode.SRC_ATOP);
		}
	}
	
	public void setKeyTintColor(int keyboardIndex, int rowIndex, int keyIndex, int normalColor, int pressColor){
		setKeyTintColor(getKey(keyboardIndex, rowIndex, keyIndex), normalColor, pressColor);
	}
	
	public ColorStateList getTintListWithStates(int normalColor, int pressColor){
		return new ColorStateList(new int[][]{
			{android.R.attr.state_selected},{}
		},new int[]{pressColor, normalColor});
	}

/*	
	public static int getColorWithState(int color, boolean selected){
		if(selected){
			int[] state = {Color.red(color),Color.green(color),Color.blue(color)};
			for(int i = 0;i < state.length;i++){
				state[i] /= 1.2;
			}
			return Color.argb(Color.alpha(color),state[0],state[1],state[2]);
		}
		return color;
	}
*/

	public void setKeyRepeat(int keyboardIndex, int rowIndex, int keyIndex){
		setKeyRepeat(keyboardIndex, rowIndex, keyIndex, true);
	}

	public void setKeyRepeat(int keyboardIndex, int rowIndex, int keyIndex, boolean repeat){
		getKey(keyboardIndex, rowIndex, keyIndex).setHint(repeat ? KEY_REPEAT : "");
	}

	public boolean isKeyRepeat(int keyboardIndex, int rowIndex, int keyIndex){
		return isKeyRepeat(getKey(keyboardIndex, rowIndex, keyIndex));
	}
	
	public boolean isKeyHasEvent(int keyboardIndex, int rowIndex, int keyIndex){
		return isKeyHasEvent(getKey(keyboardIndex,rowIndex,keyIndex));
	}
	
	public boolean isKeyHasEvent(Key k){
		return isKeyRepeat(k) || k.getTag(TAG_LP) != null || k.getTag(TAG_NP) != null;
	}

	private boolean isKeyRepeat(View v){
		CharSequence cs = ((Key)v).getHint();
		if(cs == null) return false;
		return cs.toString().equals(KEY_REPEAT);
	}

	public void setKeyWidthPercent(int keyboardIndex, int rowIndex, int keyIndex, int percent){
		Key k = getKey(keyboardIndex,rowIndex,keyIndex);
		k.getLayoutParams().width = DensityUtils.wpInt(percent);
		k.setId(percent);
	}
	
	public final void setLongPressMultiplier(int multi){
		mult = multi;
	}

	public final void setIconSizeMultiplier(int multi){
		iconmulti = multi;
		setKeysTextSize((int) txtsze, true);
	}
	
	private boolean isHasPopup(View v){
		CharSequence cs = ((Key)v).getHint();
		if(cs == null) return false;
		return (!isKeyRepeat(v)) && (cs.length() > 0);
	}
	
	public void setPopupForKey(int keyboardIndex, int rowIndex, int keyIndex, String chars){
		StringBuilder cs = new StringBuilder();
		for(String x : chars.split("")){
			if(!cs.toString().contains(x)){
				cs.append(x);
			}
		}
		Key key = getKey(keyboardIndex, rowIndex, keyIndex);
		key.setHint(cs.toString());
		if(cs.length() > 0)
			key.setSubText(String.valueOf(cs.charAt(0)));
	}

	public void setLayoutPopup(int keyboardIndex,String[][] chars){
		if(chars != null){
			if(keyboardIndex < getChildCount() && keyboardIndex >= 0){
				ViewGroup v = getKeyboard(keyboardIndex), r;
				for(int i = 0;i < v.getChildCount();i++){
					r = getRow(keyboardIndex,i);
					for(int g = 0;g < r.getChildCount();g++)
						setPopupForKey(keyboardIndex,i,g,chars[i][g]);
				}
			} else throw new RuntimeException("Invalid keyboard index number");
		}
	}
	
	public void setKeysPadding(final int padding){
		if(getKeyMargin(0,0,0) != padding)
			applyToAllKeys(new ApplyToKeyRunnable(){
					public void run(Key key){
						Row.LayoutParams l = (Row.LayoutParams) key.getLayoutParams();
						l.bottomMargin = l.topMargin = l.leftMargin = l.rightMargin = padding;
					}
				});
	}
	
	private int getKeyMargin(int k, int r, int y){
		try {
			Key key = getKey(k, r, y);
			Row.LayoutParams l = (Row.LayoutParams) key.getLayoutParams();
			return l.bottomMargin;
		} catch(Throwable ignored){}
		return -1;
	}

	public void setKeysTextColor(final int color){
		if(keyclr != color)
			applyToAllKeys(new ApplyToKeyRunnable(){
					public void run(Key key){
						key.setKeyItemColor(color);
					}
				});
		keyclr = color;
	}
	
	public void setKeysTextSize(final int size){
		setKeysTextSize(size, false);
	}
	
	public void setKeysTextSize(final int size, boolean force){
		if(txtsze != size || force)
			applyToAllKeys(new ApplyToKeyRunnable(){
					public void run(Key key){
						key.setKeyTextSize(size);
					}
				});
		txtsze = size;
	}

	public void setKeysBackground(final Drawable d){
		if(keybg != d)
			applyToAllKeys(new ApplyToKeyRunnable(){
					public void run(Key key){
						key.setBackground(d);
					}
				});
		keybg = d;
	}
	
	public void setKeysShadow(final int radius, final int color){
		if(shrad != radius || shclr != color)
			applyToAllKeys(new ApplyToKeyRunnable(){
					public void run(Key key){
						key.setKeyShadow(radius, color);
					}
				});
		shrad = radius;
		shclr = color;
	}
	
	public void setKeysPopupPreviewEnabled(final boolean enabled){
		if(enabled != ppreview)
			applyToAllKeys(new ApplyToKeyRunnable(){
					public void run(Key key){
						ppreview = enabled;
						key.setKeyImageVisible(key.isKeyIconSet());
					}
				});
	}
	
	public void setKeysTextType(final int style){
		if(txts != style)
			applyToAllKeys(new ApplyToKeyRunnable(){
					public void run(Key key){
						key.setKeyTextStyle(style);
					}
				});
		txts = style;
	}
	
	public void setKeysTextType(final TextType style){
		if(style == null){
			setKeysTextType(0);
			return;
		}
		int i = 0;
		for(TextType type : TextType.values()){
			if(style.name().equals(type.name())){
				setKeysTextType(i);
				break;
			}
			i++;
		}
	}
	
	public static abstract class ApplyToKeyRunnable {
		public abstract void run(Key key);
	}
	
	public void applyToAllKeys(ApplyToKeyRunnable runnable){
		for(int j = 0;j < getChildCount();j++){
			for(int i = 0;i < getKeyboard(j).getChildCount();i++){
				for(int g = 0;g < getRow(j,i).getChildCount();g++){
					runnable.run(getKey(j,i,g));
				}
			}
		}
	}

	public void setKeyboardHeight(int percent){
		//if(percent > 19 && percent < 81){
			hp = percent;
			getLayoutParams().height = DensityUtils.hpInt(percent);
			if(getChildCount() > 0){
				for(int i = 0;i < getChildCount();i++){
					getChildAt(i).getLayoutParams().height = getLayoutParams().height;
				}
			}
			int x = selected;
			
			// this line added because after
			// resizing layout, it needs recreation
			setEnabledLayout(findNumberKeyboardIndex());
			setEnabledLayout(x);
		//} else throw new RuntimeException("Invalid keyboard height");
	}
	
	public void setKeyboardWidth(int percent){
		//if(percent > 11 && percent < 101){
			wp = percent;
			getLayoutParams().width = DensityUtils.wpInt(percent);
			if(getChildCount() > 0){
				for(int i = 0;i < getChildCount();i++){
					getChildAt(i).getLayoutParams().width = getLayoutParams().width;
				}
			}
			int x = selected;
			setEnabledLayout(findNumberKeyboardIndex());
			setEnabledLayout(x);
		//} else throw new RuntimeException("Invalid keyboard width");
	}

	public void setKeyLongClickEvent(int keyboardIndex, int rowIndex, int keyIndex, OnLongClickListener event){
		getKey(keyboardIndex, rowIndex, keyIndex).setOnLongClickListener(event);
	}
	
	public void setKeyDrawable(int keyboardIndex, int rowIndex, int keyIndex, int resId){
		setKeyDrawable(keyboardIndex, rowIndex, keyIndex, getResources().getDrawable(resId));
	}

	public void setKeyDrawable(int keyboardIndex, int rowIndex, int keyIndex, Drawable d){
		d.setColorFilter(keyclr,PorterDuff.Mode.SRC_ATOP);
		Key t = getKey(keyboardIndex, rowIndex, keyIndex);
		((LinearLayout.LayoutParams)t.getLayoutParams()).gravity = CENTER;
		t.setKeyIcon(d);
	}

	public int getEnabledLayoutIndex(){
		return selected;
	}

	public void setEnabledLayout(int keyboardIndex){
		if(keyboardIndex < 0) keyboardIndex += getChildCount();
		if(keyboardIndex < getChildCount() && keyboardIndex >= 0){
			if(getChildCount() == 1 || keyboardIndex == selected) return;
			getChildAt(selected).setVisibility(GONE);
			selected = keyboardIndex;
			getChildAt(selected).setVisibility(VISIBLE);
		} else throw new RuntimeException("Invalid keyboard index number");
	}
	
	public void setLayoutType(int keyboardIndex, KeyboardType type){
		getKeyboard(keyboardIndex).setTag(type);
	}
	
	public void createLayoutWithRows(String[][] keys, KeyboardType type){
		createEmptyLayout(type);
		addRows(getChildCount()-1,keys);
	}

	public void createLayoutWithRows(String[][] keys){
		createLayoutWithRows(keys,KeyboardType.TEXT);
	}
	
	public void createEmptyLayout(){
		createEmptyLayout(KeyboardType.TEXT);
	}

	public void createEmptyLayout(KeyboardType type){
		LinearLayout ll = new LinearLayout(getContext());
		ll.setLayoutParams(new LayoutParams(-1,getLayoutParams().height));
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setTag(type);
		addView(ll);
		if(getChildCount() != 1){
			ll.setVisibility(GONE);
		}
	}

	public ViewGroup getCurrentKeyboard(){
		return getKeyboard(selected);
	}

	public ViewGroup getKeyboard(int keyboardIndex){
		if(keyboardIndex < 0) keyboardIndex += getChildCount();
		return (ViewGroup)getChildAt(keyboardIndex);
	}
	
	public void replaceNormalKeyboard(String[][] newKeyboard){
		ViewGroup vg = getKeyboard(findNormalKeyboardIndex());
		vg.removeAllViewsInLayout();
		addRows(findNormalKeyboardIndex(),newKeyboard);
	}
	
	public void replaceRowFromKeyboard(int keyboardIndex, int rowIndex, String[] chars){
		getRow(keyboardIndex, rowIndex).removeAllViewsInLayout();
		for (String aChar : chars) {
			addKeyToRow(keyboardIndex, rowIndex, aChar);
		}
	}
	
	public void removeRowFromKeyboard(int keyboardIndex, int rowIndex){
		getRow(keyboardIndex, rowIndex).removeAllViewsInLayout();
		getKeyboard(keyboardIndex).removeViewAt(rowIndex);
	}
	
	public void removeKeyFromRow(int keyboardIndex, int rowIndex, int keyIndex){
		getRow(keyboardIndex,rowIndex).removeViewAt(keyIndex);
	}

	public Row getRow(int keyboardIndex, int rowIndex){
		if(rowIndex < 0) rowIndex += getKeyboard(keyboardIndex).getChildCount();
		return (Row)getKeyboard(keyboardIndex).getChildAt(rowIndex);
	}

	public Key getKey(int keyboardIndex, int rowIndex, int keyIndex){
		if(keyIndex < 0) keyIndex += getRow(keyboardIndex,rowIndex).getChildCount();
		return (Key)getRow(keyboardIndex,rowIndex).getChildAt(keyIndex);
	}

	public void setKeyPopup(int keyboardIndex, int rowIndex, int keyIndex, String chars){
		getKey(keyboardIndex, rowIndex, keyIndex).setHint(chars);
	}

	public void addRows(int keyboardIndex,String[][] keys){
		if(keys != null){
			for (String[] key : keys) {
				addRow(keyboardIndex, key);
			}
		}
	}
	
	public void addKeyToRow(int keyboardIndex, int rowIndex, String key){
		addKeyToRow(keyboardIndex, rowIndex, key, "");
	}
	
	public void addKeyToRow(int keyboardIndex, int rowIndex, String key, String subKey){
		Row r = getRow(keyboardIndex, rowIndex);
		Key k = new Key(getContext());
		k.setText(key);
		k.setSubText(subKey);
		r.addKey(k);
		r.setKeyWidths();
	}
	
	public void addRow(int keyboardIndex,String[] keys){
		addRow(keyboardIndex,null,keys);
	}

	public void addRow(int keyboardIndex,Key template,String[] keys){
		clear = true;
		Row r = new Row(getContext());
		if(keys.length > 0){
			for (String key : keys) {
				Key k = new Key(getContext());
				if (template != null) {
					template.clone(k);
				}
				k.setText(key);
				r.addKey(k);
			}
			r.setKeyWidths();
		}
		getKeyboard(keyboardIndex).addView(r);
	}
	
	public void sendDefaultKeyboardEvent(View v){
		defaultKeyboardEvent((Key)v);
	}

	private void defaultKeyboardEvent(Key v){
		if(v.getTag(TAG_NP) != null){
			x = v.getTag(TAG_NP).toString().split(":");
			switch(y = Integer.parseInt(x[0])){
				case Keyboard.KEYCODE_SHIFT:
					setShiftState();
					break;
				case Keyboard.KEYCODE_CANCEL:
					setEnabledLayout((selected - 1) >= 0 ? selected - 1 : findSymbolKeyboardIndex());
					break;
				case Keyboard.KEYCODE_MODE_CHANGE:
					setEnabledLayout(selected==0?findSymbolKeyboardIndex():findNormalKeyboardIndex());
					if(getEnabledLayoutIndex() == findNormalKeyboardIndex() && shift != 2)
						updateKeyState();
					break;
				case Keyboard.KEYCODE_ALT:
					setEnabledLayout((selected + 1) % getChildCount());
					break;
				case Keyboard.KEYCODE_DELETE:
					sendKeyEvent(KeyEvent.KEYCODE_DEL);
					if(getEnabledLayoutIndex() == findNormalKeyboardIndex() && shift != 2)
						updateKeyState();
					break;
				case Keyboard.KEYCODE_DONE:
					switch(action){
						case EditorInfo.IME_ACTION_DONE:
						case EditorInfo.IME_ACTION_GO:
						case EditorInfo.IME_ACTION_SEARCH:
						case EditorInfo.IME_ACTION_SEND:
						case EditorInfo.IME_ACTION_NEXT:
						case EditorInfo.IME_ACTION_PREVIOUS:
							performEditorAction(action);
							break;
						default:
							sendKeyEvent(KeyEvent.KEYCODE_ENTER);
							break;
					}
					break;
				default:
					if(Boolean.parseBoolean(x[1])){
						sendKeyEvent(y);
					} else {
						commitText((char)y+"");
					}
					if(getEnabledLayoutIndex() == findNormalKeyboardIndex() && shift != 2)
						updateKeyState();
					break;
			}
			playSound(y);
		} else {
			commitText(v.getText().toString());
			if(getEnabledLayoutIndex() == findNormalKeyboardIndex() && shift != 2)
				updateKeyState();
			playSound(0);
		}
		if(vib > 0) {
            if(Build.VERSION.SDK_INT >= 26) {
                vb.vibrate(VibrationEffect.createOneShot(vib, 255));
            } else {
                vb.vibrate(vib);
            }
        }
	}
	
	public void fakeKeyboardEvent(Key v){
		if(v.getTag(TAG_NP) != null){
			x = v.getTag(TAG_NP).toString().split(":");
			y = Integer.parseInt(x[0]);
			playSound(y);
			return;
		}
		playSound(0);
	}
	
	protected InputMethodService getServiceContext(){
		return curr;
	}
	
	protected InputConnection getCurrentIC(){
		return getServiceContext().getCurrentInputConnection();
	}

	public void sendKeyEvent(int code){
		switch(code){
			case KEYCODE_CLOSE_KEYBOARD:
				closeKeyboard();
				break;
			case KEYCODE_SWITCH_LANGUAGE:
				switchLanguage();
				break;
			case KEYCODE_OPEN_EMOJI_LAYOUT:
				openEmojiLayout();
				break;
			default:
				int[] actions = {MotionEvent.ACTION_DOWN,MotionEvent.ACTION_UP};
				for (int action : actions)
					getCurrentIC().sendKeyEvent(new KeyEvent(action,code));	
		}
	}
	
	private void performEditorAction(int action){
		getCurrentIC().performEditorAction(action);
	}

	public final void commitText(String text){
		if(text == null) return;
		getCurrentIC().commitText(text,text.length());
		getCurrentIC().finishComposingText();
	}
	
	public int getShiftState(){
		return shift;
	}

	private void setShiftState(){
		setShiftState((shift+1) % 3);
	}

	public void setShiftState(int state){
		if(state == shift){
			return;
		}

		shift = state;
		
		ViewGroup k = getCurrentKeyboard();
		for(int i = 0;i < k.getChildCount();i++){
			Row r = getRow(selected,i);
			for(int g = 0;g < r.getChildCount();g++){
				Key t = (Key) r.getChildAt(g);
				if(!isKeyHasEvent(t) && t.getText() != null){
					String tText = t.getText().toString();
					String sText = t.getSubText().toString();
					t.setText(state > 0 
							  ? tText.toUpperCase(loc)
							  : tText.toLowerCase(loc));
					t.setSubText(state > 0 
								 ? sText.toUpperCase(loc)
								 : sText.toLowerCase(loc));
					t.setSelected(false);
				}
			}
		}
	}
	
	private static Locale loc = new Locale("tr","TR");
	
	public void setKeyboardLanguage(String lang){
		if(lang != null){
			String[] la = lang.split("_");
			loc = la.length > 1 ? new Locale(la[0],la[1]) : new Locale(la[0].toLowerCase(),la[0].toUpperCase());
			// trigSystemSuggestions();
		}
	}
	
	private boolean isRepeat = true;
	
	public void setRepeating(boolean repeat){
		isRepeat = repeat;
	}
	
	private boolean shiftDetect = true;
	
	public void setShiftDetection(boolean detect){
		shiftDetect = detect;
	}
	
	InputMethodService curr = null;
	
	private void updateKeyState(){
		updateKeyState(curr);
	}
	
	int action = 0;

	public void updateKeyState(InputMethodService s){
		EditorInfo ei = s.getCurrentInputEditorInfo();
		
		action = ei.imeOptions & (EditorInfo.IME_MASK_ACTION | EditorInfo.IME_FLAG_NO_ENTER_ACTION);
		
		switch (ei.inputType & InputType.TYPE_MASK_CLASS){
			case InputType.TYPE_CLASS_NUMBER:
            case InputType.TYPE_CLASS_PHONE:
				setEnabledLayout(findNumberKeyboardIndex());
				break;
			default:
				setEnabledLayout(findNormalKeyboardIndex());
				if(shiftDetect){
					int caps = ei.inputType != InputType.TYPE_NULL 
						? s.getCurrentInputConnection().getCursorCapsMode(ei.inputType)
						: 0;
					setShiftState(caps==0?0:1);
				} else setShiftState(0);
				break;
		}
		
		Key k;
		switch(ei.inputType & InputType.TYPE_MASK_VARIATION){
			case InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
			case InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
				k = findKeyByLabel(0, ",");
				if(k != null){
					k.setText("@");
					// k.setSubText("→");
				}
				break;
			default:
				k = findKeyByLabel(0, "@");
				if(k != null){
					k.setText(",");
					// k.setSubText("→");
				}
				break;
		}
    }

	@Override
	protected void onConfigurationChanged(Configuration newConfig){
		fixHeight();
	}
	
	public Key findKeyByLabel(int keyboard, String label){
		ViewGroup k = getKeyboard(keyboard);
		for(int i = 0;i < k.getChildCount();i++){
			Row r = (Row) k.getChildAt(i);
			for(int g = 0;g < r.getChildCount();g++){
				Key t = (Key) r.getChildAt(g);
				if(t.getText() != null && t.getText().equals(label)){
					return t;
				}
			}
		}
		return null;
	}
	
	public Key findKey(int keyboard, int keyAction){
		ViewGroup k = getKeyboard(keyboard);
		for(int i = 0;i < k.getChildCount();i++){
			Row r = (Row) k.getChildAt(i);
			for(int g = 0;g < r.getChildCount();g++){
				Key t = (Key) r.getChildAt(g);
				if((t.getText() != null && t.getText().charAt(0) == keyAction) ||
					(t.getTag(TAG_NP) != null && Integer.parseInt(t.getTag(TAG_NP).toString().split(":")[0]) == keyAction)){
					return t;
				}
			}
		}
		return null;
	}
	
	public void setRowPadding(int keyboardIndex, int rowIndex, int padding){
		getRow(keyboardIndex, rowIndex).setPadding(padding,0,padding,0);
	}
	
	private boolean isHasLongPressEvent(View v){
		return v != null && v.getTag(TAG_LP) != null;
	}
	
	public void setPressEventForKey(int keyboardIndex, int rowIndex, int keyIndex, int keyCode){
		setPressEventForKey(keyboardIndex, rowIndex, keyIndex, keyCode, true);
	}
	
	public void setPressEventForKey(int keyboardIndex, int rowIndex, int keyIndex, int keyCode, boolean isEvent){
		getKey(keyboardIndex, rowIndex, keyIndex).setTag(TAG_NP,keyCode+":"+isEvent);
	}
	
	public void setLongPressEventForKey(int keyboardIndex, int rowIndex, int keyIndex, int keyCode){
		setLongPressEventForKey(keyboardIndex, rowIndex, keyIndex, keyCode, true);
	}
	
	public void setLongPressEventForKey(int keyboardIndex, int rowIndex, int keyIndex, int keyCode, boolean isEvent){
		getKey(keyboardIndex, rowIndex, keyIndex).setTag(TAG_LP,keyCode+":"+isEvent);
	}
	
	public void setDisablePopup(boolean val){
		dpopup = val;
	}
	
	public void closeKeyboard(){
		getServiceContext().requestHideSelf(0);
	}
	
	public int findSymbolKeyboardIndex(){
		for(int i = 0;i < getChildCount();i++){
			if(getChildAt(i).getTag() != null && 
			   getChildAt(i).getTag().equals(KeyboardType.SYMBOL)){
				return i;
			}
		}
		Log.e(getClass().getSimpleName(),"No symbol keyboard set, falling back to normal keyboard ...");
		return findNormalKeyboardIndex();
	}
	
	public int findNormalKeyboardIndex(){
		for(int i = 0;i < getChildCount();i++){
			if(getChildAt(i).getTag() != null &&
			   getChildAt(i).getTag().equals(KeyboardType.TEXT)){
				return i;
			}
		}
		Log.e(getClass().getSimpleName(),"No normal keyboard set, crashing ...");
		throw new RuntimeException("You must set a normal keyboard for input");
	}
	
	public int findNumberKeyboardIndex(){
		for(int i = 0;i < getChildCount();i++){
			if(getChildAt(i).getTag() != null && 
				getChildAt(i).getTag().equals(KeyboardType.NUMBER)){
				return i;
			}
		}
		Log.e(getClass().getSimpleName(),"No number keyboard set, falling back to normal keyboard ...");
		return findNormalKeyboardIndex();
	}
	
	public void playSound(int event){
		
	}
	
	
	public enum KeyboardType { TEXT, SYMBOL, NUMBER }

	protected class Row extends LinearLayout {

		public Row(Context c){
			super(c);
			setLayoutParams(new LinearLayout.LayoutParams(-1,-1,1));
		}

		void addKey(Key k){
			if(k != null){
				addView(k);
			} else {
				throw new NullPointerException("Key is not be null");
			}
		}
		
		@SuppressLint("ResourceType")
		void setKeyWidths(){
			for(int i = 0;i < getChildCount();i++){
				Key k = (Key) getChildAt(i);
				if(k.getId() < 1)
					k.setId(100 / getChildCount());
				k.getLayoutParams().width = DensityUtils.wpInt(k.getId());
			}
		}
	}

	protected class Key extends RelativeLayout {
		
		TextView label, sublabel;
		ImageView icon;
		protected int shr = 0, shc = 0, txtst = 0;
		
		public boolean isKeyIconSet(){
			return icon.getDrawable() != null;
		}
		
		public int getKeyWidth(){
			return getLayoutParams().width;
		}
		
		protected int getTextColor(){
			return keyclr;
		}
		
		Key(Context context){
			super(context);
			setLayoutParams(new LinearLayout.LayoutParams(-1,-1,1));
			label = new TextView(context);
			label.setLayoutParams(new RelativeLayout.LayoutParams(-1,-1));
			sublabel = new TextView(context);
			RelativeLayout.LayoutParams subParams = new RelativeLayout.LayoutParams(-2,-2);
			subParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
			subParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
			int margin = DensityUtils.mpInt(1.5f);
			subParams.rightMargin = subParams.topMargin = margin;
			sublabel.setLayoutParams(subParams);
			icon = new ImageView(context);
			RelativeLayout.LayoutParams iconParams = new RelativeLayout.LayoutParams(-1,-1);
			iconParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
			icon.setLayoutParams(iconParams);
			addView(label);
			addView(sublabel);
			addView(icon);
			icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
			setKeyImageVisible(false);
			label.setTextColor(keyclr!=-1?keyclr:(keyclr=0xFFDEDEDE));
			label.setSingleLine();
			label.setGravity(CENTER);
			label.setHintTextColor(0);
			setKeyShadow(shrad,shclr!=-1?shclr:(shclr=keyclr));
			setKeyTextSize(txtsze!=1?txtsze:(txtsze=DensityUtils.mp(1.25f)));
			setBackground(keybg);
			setKeyTextStyle(txts);
			setKeyItemColor(txtclr);
			setOnTouchListener(SuperBoard.this);
		}

		public void setBackground(Drawable b){
			setBackgroundDrawable(b);
		}

		@Override
		public void setBackgroundDrawable(Drawable b){
			super.setBackgroundDrawable(b == null ? null : b.getConstantState().newDrawable());
		}
		
		public void setKeyIcon(Drawable dr){
			setKeyImageVisible(true);
			icon.setImageDrawable(dr);
			dr.setColorFilter(txtclr,PorterDuff.Mode.SRC_ATOP);
		}
		
		public void setKeyIcon(int res){
			setKeyIcon(getResources().getDrawable(res));
		}
		
		public void setKeyItemColor(int color){
			label.setTextColor(txtclr=color);
			sublabel.setTextColor(ColorUtils.convertARGBtoRGB(color) - 0x66000000);
			if(isKeyIconSet()){
				getKeyIcon().setColorFilter(color,PorterDuff.Mode.SRC_ATOP);
			}
		}
		
		public void setText(CharSequence text){
			setKeyImageVisible(false);
			label.setText(text);
		}
		
		public void setSubText(CharSequence text){
			setKeyImageVisible(false);
			sublabel.setText(text);
		}
		
		public CharSequence getText(){
			return label.getText();
		}
		
		public CharSequence getSubText(){
			return sublabel.getText();
		}
		
		public Drawable getKeyIcon(){
			return icon.getDrawable();
		}
		
		protected CharSequence getHint(){
			return label.getHint();
		}
		
		public void setKeyImageVisible(boolean visible){
			icon.setVisibility(visible?VISIBLE:GONE);
			label.setVisibility(visible?GONE:VISIBLE);
			sublabel.setVisibility(ppreview&&!visible?VISIBLE:GONE);
		}
		
		protected void setHint(CharSequence cs){
			label.setHint(cs);
		}
		
		private void setKeyTextSize(float size){
			label.setTextSize(txtsze=size);
			sublabel.setTextSize(label.getTextSize() / 3);
			ViewGroup.LayoutParams vp = icon.getLayoutParams();
			vp.width = -1;
			vp.height = (int)(size*iconmulti);
		}
		
		private void setKeyShadow(int radius, int color){
			label.setShadowLayer(shr=radius,0,0,shc=color);
		}
		
		public void setKeyTextStyle(int style){
			TextType[] arr = TextType.values();
			setKeyTextStyle(arr[(arr.length - 1) < style ? 0 : style]);
			txtst = style;
		}
		
		public void setKeyTextStyle(TextType style){
			if(style == null){
				style = TextType.regular;
			}
			switch(style){
				case regular:
					label.setTypeface(Typeface.DEFAULT);
					break;
				case bold:
					label.setTypeface(Typeface.DEFAULT_BOLD);
					break;
				case italic:
					label.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.ITALIC));
					break;
				case bold_italic:
					label.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD_ITALIC));
					break;
				case condensed:
					label.setTypeface(Typeface.create("sans-serif-condensed",Typeface.NORMAL));
					break;
				case condensed_bold:
					label.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD));
					break;
				case condensed_italic:
					label.setTypeface(Typeface.create("sans-serif-condensed",Typeface.ITALIC));
					break;
				case condensed_bold_italic:
					label.setTypeface(Typeface.create("sans-serif-condensed",Typeface.BOLD_ITALIC));
					break;
				case serif:
					label.setTypeface(Typeface.SERIF);
					break;
				case serif_bold:
					label.setTypeface(Typeface.create(Typeface.SERIF,Typeface.BOLD));
					break;
				case serif_italic:
					label.setTypeface(Typeface.create(Typeface.SERIF,Typeface.ITALIC));
					break;
				case serif_bold_italic:
					label.setTypeface(Typeface.create(Typeface.SERIF,Typeface.BOLD_ITALIC));
					break;
				case monospace:
					label.setTypeface(Typeface.MONOSPACE);
					break;
				case monospace_bold:
					label.setTypeface(Typeface.create(Typeface.MONOSPACE,Typeface.BOLD));
					break;
				case monospace_italic:
					label.setTypeface(Typeface.create(Typeface.MONOSPACE,Typeface.ITALIC));
					break;
				case monospace_bold_italic:
					label.setTypeface(Typeface.create(Typeface.MONOSPACE,Typeface.BOLD_ITALIC));
					break;
				case serif_monospace:
					label.setTypeface(Typeface.create("serif-monospace",Typeface.NORMAL));
					break;
				case serif_monospace_bold:
					label.setTypeface(Typeface.create("serif-monospace",Typeface.BOLD));
					break;
				case serif_monospace_italic:
					label.setTypeface(Typeface.create("serif-monospace",Typeface.ITALIC));
					break;
				case serif_monospace_bold_italic:
					label.setTypeface(Typeface.create("serif-monospace",Typeface.BOLD_ITALIC));
					break;
				case custom:
					// Contains a system problem about custom font files,
					// Custom fonts applying too slowly and I can't fix it!
					label.setTypeface(cFont);
					break;
			}
			sublabel.setTypeface(label.getTypeface());
		}
		
		protected TextView getTextView(){
			return label;
		}
		
		protected TextView getSubTextView(){
			return sublabel;
		}
		
		protected ImageView getImageView(){
			return icon;
		}
		
		@Override
		public Key clone(){
			return clone(false);
		}
		
		public Key clone(boolean disableTouchEvent){
			return clone(new Key(getContext()),disableTouchEvent);
		}
		
		public Key clone(Key k){
			return clone(k,false);
		}
		
		@SuppressLint("ClickableViewAccessibility")
		public Key clone(Key k, boolean disableTouchEvent){
			Rect r = getBackground().getBounds();
			k.getLayoutParams().width = r.right;
			k.getLayoutParams().height = r.bottom;
			k.setBackgroundDrawable(getBackground());
			k.setHint(getHint());
			k.setKeyShadow(shr,shc);
			k.setKeyItemColor(keyclr);
			k.getTextView().setSingleLine();
			k.getSubTextView().setSingleLine();
			k.setId(getId());
			k.setKeyTextSize(label.getTextSize()/2.5f);
			k.setKeyTextStyle(txts);
			if(disableTouchEvent) k.setOnTouchListener(null);
			if(isKeyIconSet()){
				k.setKeyIcon(getKeyIcon());
			} else {
				k.setText(getText());
				k.setSubText(getSubText());
			}
			return k;
		}

		public float getX() {
			if(Build.VERSION.SDK_INT >= 11) {
				return super.getX();
			}

			return getTranslationX() + getLeft();
		}

		public float getY() {
			if(Build.VERSION.SDK_INT >= 11) {
				return super.getY();
			}

			return getTranslationY() + getTop();
		}

		public void setX(float x) {
			if(Build.VERSION.SDK_INT >= 11) {
				super.setX(x);
				return;
			}

			setTranslationX(x - getLeft());
		}

		public void setY(float y) {
			if(Build.VERSION.SDK_INT >= 11) {
				super.setY(y);
				return;
			}

			setTranslationY(y - getTop());
		}

		public void setTranslationY(float y) {
			if(Build.VERSION.SDK_INT >= 11) {
				super.setTranslationY(y);
			}
		}

		public float getTranslationY() {
			if(Build.VERSION.SDK_INT >= 11) {
				super.getTranslationY();
			}

			return 0;
		}

		public void setTranslationX(float x) {
			if(Build.VERSION.SDK_INT >= 11) {
				super.setTranslationX(x);
			}
		}

		public float getTranslationX() {
			if(Build.VERSION.SDK_INT >= 11) {
				super.getTranslationX();
			}

			return 0;
		}
	}
	
	public enum TextType {
		regular,
		bold,
		italic,
		bold_italic,
		condensed,
		condensed_bold,
		condensed_italic,
		condensed_bold_italic,
		serif,
		serif_bold,
		serif_italic,
		serif_bold_italic,
		monospace,
		monospace_bold,
		monospace_italic,
		monospace_bold_italic,
		serif_monospace,
		serif_monospace_bold,
		serif_monospace_italic,
		serif_monospace_bold_italic,
		custom
	}

	public float getX() {
		if(Build.VERSION.SDK_INT >= 11) {
			return super.getX();
		}

		return getTranslationX() + getLeft();
	}

	public float getY() {
		if(Build.VERSION.SDK_INT >= 11) {
			return super.getY();
		}

		return getTranslationY() + getTop();
	}

	public void setX(float x) {
		if(Build.VERSION.SDK_INT >= 11) {
			super.setX(x);
			return;
		}
		
		setTranslationX(x - getLeft());
	}

	public void setY(float y) {
		if(Build.VERSION.SDK_INT >= 11) {
			super.setY(y);
			return;
		}
		
		setTranslationY(y - getTop());
	}

	public void setTranslationY(float y) {
		if(Build.VERSION.SDK_INT >= 11) {
			super.setTranslationY(y);
		}
	}

	public float getTranslationY() {
		if(Build.VERSION.SDK_INT >= 11) {
			super.getTranslationY();
		}

		return 0;
	}

	public void setTranslationX(float x) {
		if(Build.VERSION.SDK_INT >= 11) {
			super.setTranslationX(x);
		}
	}

	public float getTranslationX() {
		if(Build.VERSION.SDK_INT >= 11) {
			super.getTranslationX();
		}

		return 0;
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent m){
		v.setSelected(m.getAction() != MotionEvent.ACTION_UP);

		switch(m.getAction()){
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_SCROLL:
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_OUTSIDE:
				v.setSelected(false);
				mHandler.removeMessages(3);
				break;
		}

		if(isHasPopup(v) || isHasLongPressEvent(v) || isKeyRepeat(v)){
			if(isHasPopup(v) && dpopup){
				normalPress(v,m);
				return true;
			}
			switch(m.getAction()){
				case MotionEvent.ACTION_UP:
					act = MotionEvent.ACTION_UP;
					if(!isKeyRepeat(v) && mHandler.hasMessages(1)){
						sendDefaultKeyboardEvent(v);
					}
					mHandler.removeMessages(3);
					mHandler.sendEmptyMessage(3);
					break;
				case MotionEvent.ACTION_DOWN:
					act = MotionEvent.ACTION_DOWN;
					mHandler.removeMessages(1);
					onKeyboardEvent(v);
					Message msg = mHandler.obtainMessage(1,v);
					if(isKeyRepeat(v)){
						mHandler.sendMessage(msg);
					} else {
						mHandler.sendMessageDelayed(msg, 250L *mult);
					}
					break;
			}
		} else {
			normalPress(v,m);
		}
		return true;
	}
	
	private void normalPress(View v, MotionEvent m){
		switch(m.getAction()){
			case MotionEvent.ACTION_UP:
				mHandler.removeMessages(3);
				mHandler.sendEmptyMessage(3);
				break;
			case MotionEvent.ACTION_DOWN:
				sendDefaultKeyboardEvent(v);
				onKeyboardEvent(v);
				break;
		}
	}
}
