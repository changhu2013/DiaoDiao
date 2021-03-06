package mobi.dadoudou.diaodiao;

import android.app.Activity;
import android.app.Application;

import java.util.LinkedList;
import java.util.List;

public class SysApplication extends Application {

	private List<Activity> list = new LinkedList<Activity>();

	private static SysApplication instance;

	public synchronized static SysApplication getInstance() {
		if (null == instance) {
			instance = new SysApplication();
		}
		return instance;
	}

	public void addActivity(Activity activity) {
		list.add(activity);
	}

	public void exit() {
		try {
			for (Activity activity : list) {
				if (activity != null)
					activity.finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		System.gc();
	}

}
