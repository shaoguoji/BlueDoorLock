package com.FFX.bluedoorlock;

import java.util.ArrayList;
import java.util.Set;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class SelectDevice extends Activity implements OnItemClickListener{
	private final String TAG="SelectDevice";
	private static final int REQUEST_ENABLE_CODE = 0x1003;
	private BluetoothAdapter mBluetoothAdapter;
	private Button btn_scan;
	private ListView list_dev;
	private ArrayAdapter<String>adapter;
	private ArrayList<String> mArrayAdapter= new ArrayList<String>();
	private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		// 请求显示进度条
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.scan_device);
		//初始化控件
		init();	
		// 打开并查找蓝牙设备
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); 
		if (mBluetoothAdapter == null) {     
			Log.e(TAG, "Your device is not support Bluetooth!");
			return;
		}
		if (!mBluetoothAdapter.isEnabled()) {   
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_CODE); 
		}else{
			findBTDevice();
		}
		// 用来接收到设备查找到的广播和扫描完成的广播
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		// 动态注册广播接收器
		// 用来接收扫描到的设备信息
		registerReceiver(mReceiver, filter); 
	}
	private void init(){
		//初始化控件
		btn_scan = (Button) findViewById(R.id.imageButton_scan);
		btn_scan.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(!mBluetoothAdapter.isDiscovering()){
					mBluetoothAdapter.startDiscovery();
					setProgressBarIndeterminateVisibility(true);
				}
			 }
		});
		list_dev = (ListView) findViewById(R.id.listView_dev);
		list_dev.setOnItemClickListener(this);
		adapter = new ArrayAdapter<String>(this, 
					android.R.layout.simple_list_item_1, mArrayAdapter);
		list_dev.setAdapter(adapter);	
	}
	private void findBTDevice(){
		// 用来保存已经配对的蓝牙设备对象
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices(); 
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) { 
				// 将已经配对设备信息添加到ListView中
				mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
				mDeviceList.add(device);
			} 
		}
		adapter.notifyDataSetChanged();
	}
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) { 
			String action = intent.getAction();
			// 扫描到新的蓝牙设备
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// 获得蓝牙设备对象
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				//防止设备对象添加重复
				if(mDeviceList.contains(device)){
					return;
				}
				mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
				System.out.println(device.getName() + "\n" + device.getAddress());
				mDeviceList.add(device);
				adapter.notifyDataSetChanged();
			}else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
				// 扫描完成，关闭显示进度条
				setProgressBarIndeterminateVisibility(false);
			}
		} 
	}; 
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		String targetDev = mArrayAdapter.get(arg2);
		System.out.println(targetDev);
		Intent data = new Intent();
		data.putExtra("DEVICE", mDeviceList.get(arg2));
		// 当用于点击某项设备时，将该设备对象返回给调用者（MainActivity）
		setResult(RESULT_OK, data);
		this.finish();
	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_ENABLE_CODE){
			if(resultCode ==  RESULT_OK){
				System.out.println("设备打开成功");
				findBTDevice();
			}else{
				System.out.println("设备打开失败");
			}
		}
	}
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}
}
