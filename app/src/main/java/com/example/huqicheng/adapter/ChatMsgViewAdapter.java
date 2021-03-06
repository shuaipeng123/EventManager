package com.example.huqicheng.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.huqicheng.entity.ChatMsgEntity;
import com.example.huqicheng.pm.R;
import com.example.huqicheng.utils.AsyncImageLoader;

import java.util.ArrayList;
import java.util.List;


/**
 * 消息ListView的Adapter
 * 
 * @author way
 */
public class ChatMsgViewAdapter extends BaseAdapter {

	public static interface IMsgViewType {
		int IMVT_COM_MSG = 0;
		int IMVT_TO_MSG = 1;// 自己发送出去的消
	}

	private static final int ITEMCOUNT = 3;// 消息类型的总数
	private List<ChatMsgEntity> coll;// 消息对象数组
	private LayoutInflater mInflater;
	private AsyncImageLoader loader;

	public ChatMsgViewAdapter(Context context, List<ChatMsgEntity> coll, final ListView lvMsgs) {
		if(coll == null){
			coll = new ArrayList<>();
		}
		this.coll = coll;
		mInflater = LayoutInflater.from(context);

		loader = new AsyncImageLoader(new AsyncImageLoader.Callback() {
			@Override
			public void imageLoaded(AsyncImageLoader.ImageLoadTask task) {
				if(task != null){
					ImageView iv = (ImageView)lvMsgs.findViewWithTag(task.getPath());
					if(task.getBm() != null){
						iv.setImageBitmap(task.getBm());
					}else{
						iv.setImageResource(R.drawable.mini_avatar_shadow);
					}
				}
			}
		});
}

	public void addFront(ChatMsgEntity entity){
		this.coll.add(entity);
	}

	public void addBack(ChatMsgEntity entity){

	}
	public int getCount() {
		return coll.size();
	}

	public Object getItem(int position) {
		return coll.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	/**
	 * 得到Item的类型，是对方发过来的消息，还是自己发送出去的
	 */
	public int getItemViewType(int position) {
		ChatMsgEntity entity = coll.get(position);

		if (entity.getMsgType()) {//收到的消息
			return IMsgViewType.IMVT_COM_MSG;
		} else {//自己发送的消息
			return IMsgViewType.IMVT_TO_MSG;
		}
	}

	/**
	 * Item类型的总数
	 */
	public int getViewTypeCount() {
		return ITEMCOUNT;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		ChatMsgEntity entity = coll.get(position);
		boolean isComMsg = entity.getMsgType();

		ViewHolder viewHolder = null;
		if (convertView == null) {
			if (isComMsg) {
				convertView = mInflater.inflate(
						R.layout.chatting_item_msg_text_left, null);
			} else {
				convertView = mInflater.inflate(
						R.layout.chatting_item_msg_text_right, null);

			}

			viewHolder = new ViewHolder();
			viewHolder.ivUserAvatar = (ImageView) convertView.findViewById(R.id.iv_userhead);
			viewHolder.tvSendTime = (TextView) convertView
					.findViewById(R.id.tv_sendtime);
			viewHolder.tvUserName = (TextView) convertView
					.findViewById(R.id.tv_username);
			viewHolder.tvContent = (TextView) convertView
					.findViewById(R.id.tv_chatcontent);
			viewHolder.isComMsg = isComMsg;

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		viewHolder.tvSendTime.setText(entity.getDate());
		viewHolder.tvUserName.setText(entity.getName());
		viewHolder.tvContent.setText(entity.getMessage());

		viewHolder.ivUserAvatar.setTag(entity.getAvatar());
		Bitmap bm = loader.loadImage(entity.getAvatar());
		if(bm != null){
			viewHolder.ivUserAvatar.setImageBitmap(bm);
		}else{
			// initialize the image as a default image
			viewHolder.ivUserAvatar.setImageResource(R.drawable.mini_avatar_shadow);
		}


		return convertView;
	}

	static class ViewHolder {
		public TextView tvSendTime;
		public TextView tvUserName;
		public TextView tvContent;
		public ImageView ivUserAvatar;
		public boolean isComMsg = true;
	}

}
