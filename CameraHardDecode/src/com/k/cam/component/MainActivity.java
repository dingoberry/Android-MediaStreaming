package com.k.cam.component;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.bbq.w.library.LogLib;
import com.k.cam.Configuration;
import com.k.cam.R;

public class MainActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogLib.d("onCreate");
		setContentView(R.layout.activity_main);

		Fragment fragment;
		switch (Configuration.CURRENT_COMPONENT) {
		case Configuration.COMPONENT_SURFACE:
			fragment = new NormalSurfaceFragment();
			break;

		case Configuration.COMPONENT_GL_SURFACE:
			fragment = new GLOptimizeFragment();
			break;

		default:
			fragment = null;
			break;
		}

		if (fragment != null) {
			loadFragment(fragment);
		}
	}

	private void loadFragment(Fragment fragment) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.main_container, fragment);
		ft.commit();
	}
}
