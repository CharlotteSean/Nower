package com.project.enjoyit.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import me.drakeet.materialdialog.MaterialDialog;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.project.enjoyit.R;
import com.project.enjoyit.UserInfoActivity;
import com.project.enjoyit.adapter.MyShowsListViewAdapter.ViewHolder;
import com.project.enjoyit.entity.Comment;
import com.project.enjoyit.entity.Show;
import com.project.enjoyit.global.MyApplication;
import com.project.enjoyit.global.MyURL;

import dmax.dialog.SpotsDialog;
import android.R.integer;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.NoCopySpan.Concrete;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MyCommentsListViewAdapter extends BaseAdapter {
	private static final String TAG = "MyCommentsListViewAdapter";

	private ArrayList<Comment> comments;
	private Context context;
	private String username;
	private int show_isanonymous;

	public MyCommentsListViewAdapter(Context context,
			ArrayList<Comment> comments, String username, int show_isanonymous) {
		this.comments = comments;
		this.context = context;
		this.username = username;
		this.show_isanonymous = show_isanonymous;
	}

	@Override
	public int getCount() {
		return comments.size();
	}

	@Override
	public Object getItem(int arg0) {
		return comments.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.comment_list_item, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.tvText = (TextView) convertView
					.findViewById(R.id.tv_text);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.tvText.setMovementMethod(LinkMovementMethod.getInstance());
		viewHolder.tvText.setText(addClickablePart(comments.get(position)));
		
		final String text = viewHolder.tvText.getText().toString();
		
		//Log.e(TAG, "0 " + comments.size());

		viewHolder.tvText.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				if (MyApplication.getUser().getUsername().equals(comments.get(position).getComment_user())){
					Toast.makeText(context, "���ܶ��Լ�����Ŷ��", Toast.LENGTH_SHORT).show();
					return true;
				}
				final MaterialDialog dialog = new MaterialDialog(context);
				View view = LayoutInflater.from(context).inflate(
						R.layout.comment_dialog, null);
				TextView tv_text = (TextView)view.findViewById(R.id.tv_text);
				tv_text.setText(text);
				final EditText et_content = (EditText)view.findViewById(R.id.et_content);
				
				//((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);
				et_content.requestFocus();
				//et_content.requestFocusFromTouch();
				et_content.setFocusable(true);
				et_content.setFocusableInTouchMode(true);
				et_content.requestFocus();
				InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);  
				imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
//				InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
//				imm.showSoftInput(et_content,InputMethodManager.SHOW_FORCED);  
				
				dialog.setPositiveButton("ȷ��", new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						String content = et_content.getText().toString().trim();
						if (content.isEmpty()){
							Toast.makeText(context, "���ܿ�Ŷ", Toast.LENGTH_SHORT).show();
							return;
						}
						postComment(et_content, position, content, comments.get(position), dialog);
					}
				}).setNegativeButton("ȡ��", new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						dialog.dismiss();
					}
				});
				dialog.setView(view).show();
				
				return true;
			}
		});

		return convertView;
	}
	protected void postComment(final EditText et_content, final int position, final String content, final Comment comment, final MaterialDialog dialog) {
		final AlertDialog loading = new SpotsDialog(context);
		loading.show();
		Listener<String> listener = new Listener<String>() {
			@Override
			public void onResponse(String response) {
				try {
					JSONObject jsonObject = new JSONObject(response);
					int code = jsonObject.getInt("code");
					if (code == 1){
						Toast.makeText(context, "���۳ɹ�", Toast.LENGTH_SHORT).show();
						Comment comment = new Comment();
						comment.setComment_user(MyApplication.getUser().getUsername());
						comment.setCommented_user(comments.get(position).getComment_user());
						comment.setContent(content);
						comment.setIs_anonymous(0);
						comments.add(position+1, comment);
						MyCommentsListViewAdapter.this.notifyDataSetChanged();
						dialog.dismiss();
						
						InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
//						imm.showSoftInput(etUsername, InputMethodManager.SHOW_FORCED);
						imm.hideSoftInputFromWindow(et_content.getWindowToken(), 0); //ǿ�����ؼ��� 
					}else{
						Toast.makeText(context, "����ʧ��", Toast.LENGTH_SHORT).show();
					}
					
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					loading.dismiss();
				}
			}
		};
		ErrorListener errorListener = new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e(TAG, "����ʧ��"+error.toString());
				Toast.makeText(context, "����ʧ�ܣ�������������...", Toast.LENGTH_SHORT).show();
				loading.dismiss();
			}
		};
		Map<String, String> map = new HashMap<String, String>();
		map.put("token", MyApplication.getUser().getToken());
		map.put("username", MyApplication.getUser().getUsername());
		Log.e(TAG, comment.getComment_id()+"");
		map.put("to_comment_id", comment.getComment_id()+"");
		map.put("content", content);
		map.put("is_anonymous", 0+"");
		
		MyApplication.getMyVolley().addPostStringRequest(MyURL.POST_COMMENT_COMMENT_URL, listener, errorListener, map, TAG);
	}
	public final class ViewHolder {
		public TextView tvText;
	}

	private SpannableStringBuilder addClickablePart(Comment comment) {
		SpannableStringBuilder ssb = new SpannableStringBuilder();
		String comment_user = comment.getComment_user();
		String commented_user = comment.getCommented_user();

		int start = 0;
		int end = 0;

		if (comment.getIs_anonymous() == 1) {
			String str = "������";
			ssb.append(str);
			start += str.length();
		} else {
			MyClickableSpan clickableSpan1 = new MyClickableSpan(comment_user);
			ssb.append(comment_user);
			end = start + comment_user.length();
			ssb.setSpan(clickableSpan1, start, end, 0);
			start = end;
		}
		String str = "�ظ�";
		ssb.append(str);
		start += str.length();

		if ((!comment_user.equals(username)) && (show_isanonymous == 1 || !commented_user.equals(username))) {
			MyClickableSpan clickableSpan2 = new MyClickableSpan(commented_user);
			ssb.append(commented_user);
			end = start + commented_user.length();
			ssb.setSpan(clickableSpan2, start, end, 0);
		}

		ssb.append("��" + comment.getContent());
		return ssb;
	}

	class MyClickableSpan extends ClickableSpan {
		private String text;

		public MyClickableSpan(String text) {
			this.text = text;
		}

		@Override
		public void onClick(View arg0) {
//			Toast.makeText(context, "�����" + text, Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(context, UserInfoActivity.class);
			intent.putExtra("username", text);
			context.startActivity(intent);
			
		}

		@Override
		public void updateDrawState(TextPaint ds) {
			super.updateDrawState(ds);
			// ds.setColor(Color.RED); // �����ı���ɫ
			// ȥ���»���
			ds.setUnderlineText(false);
		}
	}
}