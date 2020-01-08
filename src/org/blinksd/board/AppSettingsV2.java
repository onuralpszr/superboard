package org.blinksd.board;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import org.blinksd.*;
import org.blinksd.utils.color.*;
import org.blinksd.utils.image.*;
import org.blinksd.utils.layout.*;
import org.superdroid.db.*;
import yandroid.widget.*;

public class AppSettingsV2 extends Activity {
	
	ScrollView scroller;
	LinearLayout main;
	SuperDB sdb;
	static View dialogView;
	SettingMap sMap;

	@Override
	protected void onCreate(Bundle b){
		super.onCreate(b);
		main = LayoutCreator.createFilledVerticalLayout(FrameLayout.class,this);
		int dp = DensityUtils.dpInt(16);
		main.setPadding(dp,dp,dp,dp);
		scroller = new ScrollView(this);
		scroller.setLayoutParams(new FrameLayout.LayoutParams(-1,-1));
		scroller.addView(main);
		setContentView(scroller);
		sdb = SuperBoardApplication.getApplicationDatabase();
		sMap = new SettingMap();
		try {
			createMainView();
		} catch(Throwable e){
			Log.e("MainView","Error:",e);
		}
	}
	
	private void createMainView() throws Throwable {
		ArrayList<String> keys = new ArrayList<String>(sMap.keySet());
		for(String key : keys){
			SettingType z = sMap.get(key);
			switch(z){
				case BOOL:
					main.addView(createBoolSelector(key));
					break;
				case IMAGE:
					main.addView(createImageSelector(key));
					break;
				case COLOR_SELECTOR:
					main.addView(createColorSelector(key));
					break;
				case LANG_SELECTOR:
					List<String> keySet = SuperBoardApplication.getLanguageHRNames();
					String value = sdb.getString(key,(String)sMap.getDefaults(key));
					int val = LayoutUtils.getKeyListFromLanguageList().indexOf(value);
					main.addView(createRadioSelector(key,val,keySet));
					break;
				case SELECTOR:
					List<String> selectorKeys = getArrayAsList(key);
					int x = sdb.getInteger(key,(int)sMap.getDefaults(key));
					main.addView(createRadioSelector(key,x,selectorKeys));
					break;
				case DECIMAL_NUMBER:
				case MM_DECIMAL_NUMBER:
				case FLOAT_NUMBER:
					main.addView(createNumberSelector(key,z == SettingType.FLOAT_NUMBER));
					break;
			}
		}
	}
	
	private final View createNumberSelector(String key, boolean isFloat){
		int num = sdb.getInteger(key,(int) sMap.getDefaults(key));
		LinearLayout numSelector = LayoutCreator.createFilledHorizontalLayout(LinearLayout.class,this);
		numSelector.getLayoutParams().height = -2;
		TextView img = LayoutCreator.createTextView(this);
		img.setId(android.R.id.text1);
		int height = (int) getListPreferredItemHeight();
		img.setGravity(Gravity.CENTER);
		img.setTextColor(0xFFFFFFFF);
		img.setText(isFloat ? getFloatNumberFromInt(num)+"" : num+"");
		img.setLayoutParams(LayoutCreator.createLayoutParams(LinearLayout.class,height,height));
		int pad = height / 4;
		img.setPadding(pad,pad,pad,pad);
		TextView btn = LayoutCreator.createTextView(this);
		btn.setGravity(Gravity.CENTER_VERTICAL);
		btn.setTextColor(0xFFFFFFFF);
		btn.setMinHeight(height);
		btn.setText(getTranslation(key));
		numSelector.setTag(key);
		numSelector.setMinimumHeight(height);
		numSelector.setOnClickListener(numberSelectorListener);
		numSelector.addView(img);
		numSelector.addView(btn);
		return numSelector;
	}
	
	private final View createColorSelector(String key){
		int color = sdb.getInteger(key,(int) sMap.getDefaults(key));
		LinearLayout colSelector = LayoutCreator.createFilledHorizontalLayout(LinearLayout.class,this);
		colSelector.getLayoutParams().height = -2;
		ImageView img = LayoutCreator.createImageView(this);
		img.setId(android.R.id.icon);
		int height = (int) getListPreferredItemHeight();
		img.setLayoutParams(LayoutCreator.createLayoutParams(LinearLayout.class,height,height));
		img.setScaleType(ImageView.ScaleType.FIT_CENTER);
		int pad = height / 4;
		img.setPadding(pad,pad,pad,pad);
		GradientDrawable gd = new GradientDrawable();
		gd.setColor(color);
		gd.setCornerRadius(1000);
		img.setImageDrawable(gd);
		TextView btn = LayoutCreator.createTextView(this);
		btn.setGravity(Gravity.CENTER_VERTICAL);
		btn.setTextColor(0xFFFFFFFF);
		btn.setMinHeight(height);
		btn.setText(getTranslation(key));
		colSelector.setTag(key);
		colSelector.setMinimumHeight(height);
		colSelector.setOnClickListener(colorSelectorListener);
		colSelector.addView(img);
		colSelector.addView(btn);
		return colSelector;
	}
	
	private final View createImageSelector(String key){
		TextView btn = LayoutCreator.createTextView(this);
		btn.setGravity(Gravity.CENTER_VERTICAL);
		btn.setTextColor(0xFFFFFFFF);
		btn.setMinHeight((int) getListPreferredItemHeight());
		btn.setText(getTranslation(key));
		btn.setTag(key);
		btn.setOnClickListener(imageSelectorListener);
		return btn;
	}
	
	private final YSwitch createBoolSelector(String key){
		boolean val = sdb.getBoolean(key,(boolean) sMap.getDefaults(key));
		YSwitch swtch = LayoutCreator.createFilledYSwitch(LinearLayout.class,this,getTranslation(key),val,switchListener);
		swtch.setMinHeight((int) getListPreferredItemHeight());
		swtch.setTag(key);
		return swtch;
	}
	
	private static final int TAG1 = R.string.app_name, TAG2 = R.string.hello_world;
	
	private final View createRadioSelector(String key, int value, List<String> items) throws Throwable {
		View base = createImageSelector(key);
		base.setTag(TAG1,value);
		base.setTag(TAG2,items);
		base.setOnClickListener(radioSelectorListener);
		return base;
	}
	
	private final View.OnClickListener colorSelectorListener = new View.OnClickListener(){

		@Override
		public void onClick(final View p1){
			AlertDialog.Builder build = new AlertDialog.Builder(p1.getContext());
			final String tag = p1.getTag().toString();
			build.setTitle(getTranslation(tag));
			final int val = sdb.getInteger(tag, (int)sMap.getDefaults(tag));
			dialogView = ColorSelectorLayout.getColorSelectorLayout(AppSettingsV2.this,p1.getTag().toString());
			build.setView(dialogView);
			build.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface p1, int p2){
						p1.dismiss();
					}

				});
			build.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface d1, int p2){
						int tagVal = (int) dialogView.findViewById(android.R.id.tabs).getTag();
						if(tagVal != val){
							sdb.putInteger(tag,tagVal);
							sdb.onlyWrite();
							ImageView img = p1.findViewById(android.R.id.icon);
							GradientDrawable gd = new GradientDrawable();
							gd.setColor(tagVal);
							gd.setCornerRadius(1000);
							img.setImageDrawable(gd);
							restartKeyboard();
						}
						d1.dismiss();
					}

				});
			build.show();
		}

	};
	
	private final View.OnClickListener numberSelectorListener = new View.OnClickListener(){

		@Override
		public void onClick(final View p1){
			AlertDialog.Builder build = new AlertDialog.Builder(p1.getContext());
			final String tag = p1.getTag().toString();
			build.setTitle(getTranslation(tag));
			AppSettingsV2 act = (AppSettingsV2) p1.getContext();
			final boolean isFloat = sMap.get(tag) == SettingType.FLOAT_NUMBER;
			int[] minMax = sMap.getMinMaxNumbers(tag);
			final int val = sdb.getInteger(tag,sMap.getDefaults(tag));
			dialogView = NumberSelectorLayout.getNumberSelectorLayout(act,isFloat,minMax[0],minMax[1],val);
			build.setView(dialogView);
			build.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface p1, int p2){
						p1.dismiss();
					}

				});
			build.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface d1, int p2){
						int tagVal = (int) dialogView.getTag();
						if(tagVal != val){
							sdb.putInteger(tag,tagVal);
							sdb.onlyWrite();
							TextView tv = p1.findViewById(android.R.id.text1);
							tv.setText(isFloat ? getFloatNumberFromInt(tagVal) + "" : tagVal + "");
							restartKeyboard();
						}
						d1.dismiss();
					}

				});
			build.show();
		}

	};
	
	private final View.OnClickListener imageSelectorListener = new View.OnClickListener(){

		@Override
		public void onClick(View p1){
			AlertDialog.Builder build = new AlertDialog.Builder(p1.getContext());
			build.setTitle(getTranslation(p1.getTag().toString()));
			build.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface p1, int p2){
						p1.dismiss();
					}
				
				});
			build.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface p1, int p2){
						ImageView img = dialogView.findViewById(android.R.id.custom);
						Drawable d = img.getDrawable();
						if(d != null){
							try {
								Bitmap bmp = ((BitmapDrawable) d).getBitmap();
								setColorsFromBitmap(bmp);
								FileOutputStream fos = new FileOutputStream(getBackgroundImageFile());
								bmp.compress(Bitmap.CompressFormat.PNG,100,fos);
							} catch(Throwable e){}
							restartKeyboard();
							recreate();
						}
						p1.dismiss();
					}

				});
			AlertDialog dialog = build.create();
			dialogView = ImageSelectorLayout.getImageSelectorLayout(dialog,AppSettingsV2.this,p1.getTag().toString());
			dialog.setView(dialogView);
			dialog.show();
		}
		
	};
	
	private final YSwitch.OnCheckedChangeListener switchListener = new YSwitch.OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(YCompoundButton buttonView, boolean isChecked){
			String str = (String) buttonView.getTag();
			sdb.putBoolean(str,isChecked);
			sdb.onlyWrite();
			restartKeyboard();
		}
		
	};
	
	private final View.OnClickListener radioSelectorListener = new View.OnClickListener(){

		@Override
		public void onClick(final View p1){
			AlertDialog.Builder build = new AlertDialog.Builder(p1.getContext());
			final String tag = p1.getTag().toString();
			int val;
			final boolean langSelector = sMap.get(tag) == SettingType.LANG_SELECTOR;
			if(langSelector){
				String value = sdb.getString(tag,(String)sMap.getDefaults(tag));
				val = LayoutUtils.getKeyListFromLanguageList().indexOf(value);
			} else {
				val = sdb.getInteger(tag,(int) sMap.getDefaults(tag));
			}
			build.setTitle(getTranslation(tag));
			dialogView = RadioSelectorLayout.getRadioSelectorLayout(AppSettingsV2.this,(int)p1.getTag(TAG1),(List<String>)p1.getTag(TAG2));
			build.setView(dialogView);
			build.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface p1, int p2){
						p1.dismiss();
					}

				});
			final int xval = val;
			build.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface p1, int p2){
						int tagVal = (int) dialogView.getTag();
						if(tagVal != xval){
							if(langSelector){
								String index = LayoutUtils.getKeyListFromLanguageList().get(tagVal);
								sdb.putString(tag,index);
							} else sdb.putInteger(tag,tagVal);
							sdb.onlyWrite();
							restartKeyboard();
						}
						p1.dismiss();
					}

				});
			build.show();
		}
		
	};
	
	private void setColorsFromBitmap(Bitmap b){
		if(b == null) return;
		int c = ColorUtils.getBitmapColor(b);
		sdb.putInteger(SettingMap.SET_KEYBOARD_BGCLR,c-0xAA000000);
		int keyClr = c-0xAA000000;
		sdb.putInteger(SettingMap.SET_KEY_BGCLR,keyClr);
		sdb.putInteger(SettingMap.SET_KEY2_BGCLR,SuperBoard.getColorWithState(c,true));
		sdb.putInteger(SettingMap.SET_ENTER_BGCLR,ColorUtils.satisfiesTextContrast(c) ? SuperBoard.getColorWithState(keyClr,true) : 0xFFFFFFFF);
		keyClr = ColorUtils.satisfiesTextContrast(c) ? 0xFF212121 : 0xFFDEDEDE;
		sdb.putInteger(SettingMap.SET_KEY_TEXTCLR,keyClr);
		sdb.putInteger(SettingMap.SET_KEY_SHADOWCLR,keyClr);
		sdb.onlyWrite();
	}
	
	private List<String> getArrayAsList(String key){
		int id = getResources().getIdentifier("settings_" + key, "array", getPackageName());
		String[] arr = getResources().getStringArray(id);
		List<String> out = new ArrayList<String>();
		for(String str : arr){
			out.add(str);
		}
		return out;
	}
	
	private final float getListPreferredItemHeight(){
		TypedValue value = new TypedValue();
		getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, value, true);	
		return TypedValue.complexToDimension(value.data, getResources().getDisplayMetrics());
	}
	
	public String getTranslation(String key){
		String requestedKey = "settings_" + key;
		try {
			return getString(getResources().getIdentifier(requestedKey, "string", getPackageName()));
		} catch(Throwable t){}
		return requestedKey;
	}
	
	public static float getFloatNumberFromInt(int i){
		return i / 10.0f;
	}
	
	public static int getIntNumberFromFloat(float i){
		return (int)(i * 10);
	}

	public static void restartKeyboard(){
		SuperBoardApplication.getApplication().sendBroadcast(new Intent(InputService.COLORIZE_KEYBOARD));
	}
	
	public static File getBackgroundImageFile(){
		return new File(SuperBoardApplication.getApplication().getFilesDir()+"/bg");
	}
	
	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data){
		super.onActivityResult(requestCode,resultCode,data);
		if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
			Uri uri = data.getData();
			new ImageTask().execute(getContentResolver(),uri);
		}
	}
	
	private static class ImageTask extends AsyncTask<Object,Bitmap,Bitmap> {

		@Override
		protected Bitmap doInBackground(Object[] p1){
			try {
				return MediaStore.Images.Media.getBitmap((ContentResolver)p1[0],(Uri)p1[1]);
			} catch(Throwable e){}
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap result){
			super.onPostExecute(result);
			if(result != null){
				result = ImageUtils.getMinimizedBitmap(result);
				ImageView img = dialogView.findViewById(android.R.id.custom);
				img.setImageBitmap(result);
			}
		}

	}
	
	private class SettingMap extends TreeMap<String,SettingType> {
		
		private static final String SET_KEYBOARD_LANG_SELECT = "keyboard_lang_select",
		SET_KEYBOARD_TEXTTYPE_SELECT = "keyboard_texttype_select",
		SET_KEYBOARD_BGIMG = "keyboard_bgimg",
		SET_KEYBOARD_BGBLUR = "keyboard_bgblur",
		SET_KEYBOARD_HEIGHT = "keyboard_height",
		SET_KEYBOARD_BGCLR = "keyboard_bgclr",
		SET_KEYBOARD_SHOW_POPUP = "keyboard_show_popup",
		SET_KEYBOARD_LC_ON_EMOJI = "keyboard_lc_on_emoji",
		SET_PLAY_SND_PRESS = "play_snd_press",
		SET_KEY_BGCLR = "key_bgclr",
		SET_KEY2_BGCLR = "key2_bgclr",
		SET_ENTER_BGCLR = "enter_bgclr",
		SET_KEY_SHADOWCLR = "key_shadowclr",
		SET_KEY_PADDING = "key_padding",
		SET_KEY_RADIUS = "key_radius",
		SET_KEY_TEXTSIZE = "key_textsize",
		SET_KEY_SHADOWSIZE = "key_shadowsize",
		SET_KEY_VIBRATE_DURATION = "key_vibrate_duration",
		SET_KEY_LONGPRESS_DURATION = "key_longpress_duration",
		SET_KEY_TEXTCLR = "key_textclr";

		public SettingMap(){
			put(SET_KEYBOARD_LANG_SELECT,SettingType.LANG_SELECTOR);
			put(SET_KEYBOARD_TEXTTYPE_SELECT,SettingType.SELECTOR);
			put(SET_KEYBOARD_BGIMG,SettingType.IMAGE);
			put(SET_KEYBOARD_BGBLUR,SettingType.DECIMAL_NUMBER);
			put(SET_KEYBOARD_HEIGHT,SettingType.MM_DECIMAL_NUMBER);
			put(SET_KEYBOARD_BGCLR,SettingType.COLOR_SELECTOR);
			put(SET_KEYBOARD_SHOW_POPUP,SettingType.BOOL);
			put(SET_KEYBOARD_LC_ON_EMOJI,SettingType.BOOL);
			put(SET_PLAY_SND_PRESS,SettingType.BOOL);
			put(SET_KEY_BGCLR,SettingType.COLOR_SELECTOR);
			put(SET_KEY2_BGCLR,SettingType.COLOR_SELECTOR);
			put(SET_ENTER_BGCLR,SettingType.COLOR_SELECTOR);
			put(SET_KEY_SHADOWCLR,SettingType.COLOR_SELECTOR);
			put(SET_KEY_TEXTCLR,SettingType.COLOR_SELECTOR);
			put(SET_KEY_PADDING,SettingType.FLOAT_NUMBER);
			put(SET_KEY_RADIUS,SettingType.FLOAT_NUMBER);
			put(SET_KEY_TEXTSIZE,SettingType.FLOAT_NUMBER);
			put(SET_KEY_SHADOWSIZE,SettingType.FLOAT_NUMBER);
			put(SET_KEY_VIBRATE_DURATION,SettingType.DECIMAL_NUMBER);
			put(SET_KEY_LONGPRESS_DURATION,SettingType.MM_DECIMAL_NUMBER);
		}
		
		public ArrayList<String> getSelector(final String key) throws Throwable {
			switch(key){
				case SET_KEYBOARD_LANG_SELECT:
					return new ArrayList<String>(LayoutUtils.getLanguageList(SuperBoardApplication.getApplication()).keySet());
				case SET_KEYBOARD_TEXTTYPE_SELECT:
					ArrayList<String> textTypes = new ArrayList<String>();
					for(SuperBoard.TextType type : SuperBoard.TextType.values())
						textTypes.add(type.name());
					return textTypes;
			}
			return new ArrayList<String>();
		}
		
		public Object getDefaults(final String key){
			if(containsKey(key)){
				switch(key){
					case SET_KEYBOARD_BGBLUR:
						return Defaults.KEYBOARD_BACKGROUND_BLUR;
					case SET_KEY_VIBRATE_DURATION:
						return Defaults.KEY_VIBRATE_DURATION;
					case SET_KEYBOARD_HEIGHT:
						return Defaults.KEYBOARD_HEIGHT;
					case SET_KEY_LONGPRESS_DURATION:
						return Defaults.KEY_LONGPRESS_DURATION;
					case SET_KEY_PADDING:
						return Defaults.KEY_PADDING;
					case SET_KEY_SHADOWSIZE:
						return Defaults.KEY_TEXT_SHADOW_SIZE;
					case SET_KEY_RADIUS:
						return Defaults.KEY_RADIUS;
					case SET_KEY_TEXTSIZE:
						return Defaults.KEY_TEXT_SIZE;
					case SET_KEYBOARD_LANG_SELECT:
						return Defaults.KEYBOARD_LANGUAGE_KEY;
					case SET_KEYBOARD_TEXTTYPE_SELECT:
						return Defaults.KEY_FONT_TYPE;
					case SET_KEYBOARD_BGCLR:
						return Defaults.KEYBOARD_BACKGROUND_COLOR;
					case SET_KEYBOARD_SHOW_POPUP:
						return Defaults.KEYBOARD_SHOW_POPUP;
					case SET_KEYBOARD_LC_ON_EMOJI:
						return Defaults.KEYBOARD_LC_ON_EMOJI;
					case SET_PLAY_SND_PRESS:
						return Defaults.KEYBOARD_TOUCH_SOUND;
					case SET_KEY_BGCLR:
						return Defaults.KEY_BACKGROUND_COLOR;
					case SET_KEY2_BGCLR:
						return Defaults.KEY2_BACKGROUND_COLOR;
					case SET_ENTER_BGCLR:
						return Defaults.ENTER_BACKGROUND_COLOR;
					case SET_KEY_SHADOWCLR:
						return Defaults.KEY_TEXT_SHADOW_COLOR;
					case SET_KEY_TEXTCLR:
						return Defaults.KEY_TEXT_COLOR;
				}
			}
			return null;
		}
		
		public int[] getMinMaxNumbers(final String key){
			int[] nums = new int[2];
			if(containsKey(key)){
				switch(get(key)){
					case DECIMAL_NUMBER:
						nums[0] = 0;
						switch(key){
							case SET_KEYBOARD_BGBLUR:
								nums[1] = Constants.MAX_OTHER_VAL;
								break;
							case SET_KEY_VIBRATE_DURATION:
								nums[1] = Constants.MAX_VIBR_DUR;
								break;
						}
						break;
					case MM_DECIMAL_NUMBER:
						switch(key){
							case SET_KEYBOARD_HEIGHT:
								nums[0] = Constants.MIN_KEYBD_HGT;
								nums[1] = Constants.MAX_KEYBD_HGT;
								break;
							case SET_KEY_LONGPRESS_DURATION:
								nums[0] = Constants.MIN_LPRESS_DUR;
								nums[1] = Constants.MAX_LPRESS_DUR;
								break;
						}
						break;
					case FLOAT_NUMBER:
						nums[0] = 0;
						switch(key){
							case SET_KEY_PADDING:
							case SET_KEY_SHADOWSIZE:
								nums[1] = Constants.MAX_OTHER_VAL;
								break;
							case SET_KEY_RADIUS:
								nums[1] = Constants.MAX_RADS_DUR;
								break;
							case SET_KEY_TEXTSIZE:
								nums[0] = Constants.MIN_TEXT_SIZE;
								nums[1] = Constants.MAX_TEXT_SIZE;
								break;
						}
						break;
				}
			}
			return nums;
		}

	}
	
	public static enum SettingType {
		BOOL,
		COLOR_SELECTOR,
		LANG_SELECTOR,
		SELECTOR,
		DECIMAL_NUMBER,
		FLOAT_NUMBER,
		MM_DECIMAL_NUMBER,
		IMAGE,
	}
	
}
