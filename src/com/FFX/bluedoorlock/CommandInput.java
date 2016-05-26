package com.FFX.bluedoorlock;

import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class CommandInput extends Activity{
	private ListView listview;
	private EditText edit;
	private Button btn_title;
	private ArrayAdapter<String>  listAdapter;
	// 聊天内容保存对象 
	private ArrayList<String>  chatContent;
	private BluetoothAdapter mBluetoothAdapter;
			
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		//设置自定义标题栏
		 requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	     setContentView(R.layout.command_input);
	     getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
		 getWindow().setSoftInputMode(WindowManager.
				LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		 
		//初始化控件
		init();
	}
	private void init(){
		listview=(ListView) findViewById(R.id.listView_command);
		edit=(EditText) findViewById(R.id.editText_command);
		btn_title=(Button) findViewById(R.id.button_title);
	    btn_title.setOnClickListener(new android.view.View.OnClickListener() {		
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub		
				mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); 
				mBluetoothAdapter.disable();
			}
		});
		chatContent = new ArrayList<String>();
		// ListView的Adapter
		listAdapter = new ArrayAdapter<String>(this, 
				// 项显示布局
				android.R.layout.simple_list_item_1, 
				// 显示的数据源
				chatContent);
		listview.setAdapter(listAdapter);
	}
	//发送按钮监听事件
	public void onSendClick(View v){
		String msg = edit.getText().toString().trim();
		if(msg.length() <= 0){
			Toast.makeText(this, "消息不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); 
		// 蓝牙设备不被支持
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "该设备没有蓝牙设备", Toast.LENGTH_LONG).show();
			return ;
		}
		// 将用户输入的消息添加到ListView中去显示
		chatContent.add(mBluetoothAdapter.getName() + ":" + msg);
		// 更新ListView显示
		listAdapter.notifyDataSetChanged();
		// 将发送消息任务提交给后台服务
		BluetoothService.newTask(new BluetoothService(mHandler, BluetoothService.TASK_SEND_MSG, new Object[]{msg}));
		// 清空输入框
		edit.setText("");
	}
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BluetoothService.TASK_SEND_MSG:
				Toast.makeText(CommandInput.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
				break;
			case BluetoothService.TASK_RECV_MSG:
				// 获得远程设备发送的消息
				chatContent.add(msg.obj.toString());
				listAdapter.notifyDataSetChanged();
				break;
			case BluetoothService.TASK_GET_REMOTE_STATE:
				setTitle(msg.obj.toString());
				break;
			default:
				break;
			}
		}
	};
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.commandinput, menu);
		return true;
	}
	//菜单点击事件
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		//获取门锁状态
		case R.id.button_opstate:
			String msg="*getstate#";
			BluetoothService.newTask(new BluetoothService(mHandler, 
					BluetoothService.TASK_SEND_MSG, new Object[]{msg}));
			break;
		//获取关门提醒状态
		case R.id.button_restate:
			String msg2="*getremind#";
			BluetoothService.newTask(new BluetoothService(mHandler, 
					BluetoothService.TASK_SEND_MSG, new Object[]{msg2}));
			break;
		//获取开门报警状态
		case R.id.button_alstate:
			String msg3="*getalarm#";
			BluetoothService.newTask(new BluetoothService(mHandler, 
					BluetoothService.TASK_SEND_MSG, new Object[]{msg3}));
			break;
		//设置用户名
		case R.id.change_name:
			AlertDialog.Builder dlg = new AlertDialog.Builder(this);
			final EditText devNameEdit = new EditText(this);
			dlg.setView(devNameEdit);
			dlg.setTitle("请输入用户名");
			dlg.setPositiveButton("设置", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				if(devNameEdit.getText().toString().length() != 0)
					// 设置蓝牙设备名
					mBluetoothAdapter.setName(devNameEdit.getText().toString());
				}
			});
			dlg.create();
			dlg.show();
			break;
		}
		return true;
	}
}
