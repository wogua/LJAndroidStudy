package com.lijun.androidstudy.welcomeViewPager;

import java.util.ArrayList;
import java.util.List;

import com.lijun.androidstudy.R;
import com.lijun.androidstudy.launcher.LJLauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class WelcomeActivity extends Activity implements OnClickListener, OnPageChangeListener{

	private ViewPager vp;
	private WelcomeViewPagerAdapter vpAdapter;
	private List<View> views;
	
	//����ͼƬ��Դ
	private static final int[] pics = { R.drawable.welcome_0, R.drawable.welcome_1,
		R.drawable.welcome_2, R.drawable.welcome_3, R.drawable.welcome_4 };
	
	//�ײ�С��ͼƬ
	private ImageView[] dots ;
	
	//��¼��ǰѡ��λ��
	private int currentIndex;
	
	private Button startButton;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	//����ȫ��
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,    
    	        WindowManager.LayoutParams.FLAG_FULLSCREEN); 
        setContentView(R.layout.welcome_layout);
        
        views = new ArrayList<View>();
       
        LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
        		LinearLayout.LayoutParams.WRAP_CONTENT);
        
        //��ʼ������ͼƬ�б�
        LayoutInflater mInflater = getLayoutInflater();
        for(int i=0; i<pics.length; i++) {
        	if(i == (pics.length -1)){
        		View v = mInflater.inflate(R.layout.last_welcome_view, null);
        		startButton = (Button)(v.findViewById(R.id.start_button));
        		views.add(v);
        	}else{
        		ImageView iv = new ImageView(this);
            	iv.setLayoutParams(mParams);
            	iv.setImageResource(pics[i]);
            	views.add(iv);
        	}
        	
        }
        vp = (ViewPager) findViewById(R.id.viewpager);
        //��ʼ��Adapter
        vpAdapter = new WelcomeViewPagerAdapter(views);
        vp.setAdapter(vpAdapter);
        //�󶨻ص�
        vp.setOnPageChangeListener(this);
        
        //��ʼ���ײ�С��
        initDots();
		if (startButton != null) {
			startButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					SharedPreferences preferences = getSharedPreferences(
							"first_pref", MODE_PRIVATE);
					Editor editor = preferences.edit();
					editor.putBoolean("isFirstLoad", false);
					editor.commit();
					Intent i = new Intent(WelcomeActivity.this,
							LJLauncher.class);
					WelcomeActivity.this.startActivity(i);
					WelcomeActivity.this.finish();
				}
			});
		}
       
        
    }
    
    private void initDots() {
		LinearLayout ll = (LinearLayout) findViewById(R.id.dots_group);

		dots = new ImageView[pics.length];

		//ѭ��ȡ��С��ͼƬ
		for (int i = 0; i < pics.length; i++) {
			dots[i] = (ImageView) ll.getChildAt(i);
			dots[i].setEnabled(true);//����Ϊ��ɫ
			dots[i].setOnClickListener(this);
			dots[i].setTag(i);//����λ��tag������ȡ���뵱ǰλ�ö�Ӧ
		}

		currentIndex = 0;
		dots[currentIndex].setEnabled(false);//����Ϊ��ɫ����ѡ��״̬
    }
    
    /**
     *���õ�ǰ������ҳ 
     */
    private void setCurView(int position)
    {
		if (position < 0 || position >= pics.length) {
			return;
		}

		vp.setCurrentItem(position);
    }

    /**
     *��ֻ��ǰ����С���ѡ�� 
     */
    private void setCurDot(int positon)
    {
		if (positon < 0 || positon > pics.length - 1 || currentIndex == positon) {
			return;
		}

		dots[positon].setEnabled(false);
		dots[currentIndex].setEnabled(true);

		currentIndex = positon;
    }

    //������״̬�ı�ʱ����
	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	//����ǰҳ�汻����ʱ����
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	//���µ�ҳ�汻ѡ��ʱ����
	@Override
	public void onPageSelected(int arg0) {
		//���õײ�С��ѡ��״̬
		setCurDot(arg0);
	}

	@Override
	public void onClick(View v) {
		int position = (Integer)v.getTag();
		setCurView(position);
		setCurDot(position);
	}
}
