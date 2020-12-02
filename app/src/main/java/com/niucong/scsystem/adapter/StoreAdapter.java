package com.niucong.scsystem.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.niucong.scsystem.EnterActivity;
import com.niucong.scsystem.R;
import com.niucong.scsystem.app.App;
import com.niucong.scsystem.dao.DBUtil;
import com.niucong.scsystem.dao.DrugInfo;
import com.niucong.scsystem.dao.StoreList;
import com.niucong.scsystem.view.NiftyDialogBuilder;

import java.util.List;

/**
 * Created by think on 2018/1/2.
 */

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.MyViewHolder> {

    private Context context;
    List<StoreList> sls;

    private int defItem = -1;
    private OnItemListener onItemListener;

    public boolean isRefersh;

    public StoreAdapter(Context context, List<StoreList> sls) {
        this.context = context;
        this.sls = sls;
    }

    public void setOnItemListener(OnItemListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    public interface OnItemListener {
        void onClick(View v, int pos);

        void onDelete(StoreList sl);
    }

    public void setDefSelect(int position) {
        this.defItem = position;
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_store, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final StoreList sl = sls.get(position);
        holder.tv_code.setText("" + sl.getBarCode());
        holder.tv_num.setText("库存：" + sl.getNumber() + " 售价：" + App.app.showPrice(sl.getPrice()));

        final DrugInfo di = DBUtil.getDaoSession().getDrugInfoDao().load(sl.getBarCode());
        holder.tv_name.setText(di.getName());
        holder.tv_factory.setText(di.getFactory());

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                final NiftyDialogBuilder submitDia = NiftyDialogBuilder.getInstance(context);
                submitDia.withTitle(di.getName());
                submitDia.withButton1Text("取消", 0).setButton1Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        submitDia.dismiss();
                    }
                });
                submitDia.withButton2Text("删除", 0).setButton2Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DBUtil.getDaoSession().getStoreListDao().delete(sl);
                        onItemListener.onDelete(sl);
                        sls.remove(position);
                        notifyDataSetChanged();

                        submitDia.dismiss();
                    }
                });
                submitDia.withButton3Text("编辑", 0).setButton3Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isRefersh = true;
                        ((Activity) context).startActivityForResult(new Intent(context, EnterActivity.class)
                                .putExtra("BarCode", sl.getBarCode()), 1);

                        submitDia.dismiss();
                    }
                });
                submitDia.withMessage(null).withDuration(400);
                submitDia.isCancelable(false);
                submitDia.show();
                return false;
            }
        });

        if (defItem != -1) {
            if (defItem == position) {
                holder.itemView.setBackgroundResource(R.color.gainsboro);
            } else {
                holder.itemView.setBackgroundResource(R.color.transparent);
            }
        } else {
            holder.itemView.setBackgroundResource(R.color.transparent);
        }
    }

    @Override
    public int getItemCount() {
        return sls.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name, tv_code, tv_factory, tv_num;
        ImageView iv_delete;

        public MyViewHolder(View view) {
            super(view);
            tv_name = (TextView) view.findViewById(R.id.item_store_name);
            tv_code = (TextView) view.findViewById(R.id.item_store_code);
            tv_factory = (TextView) view.findViewById(R.id.item_store_factory);
            tv_num = (TextView) view.findViewById(R.id.item_store_num);

            iv_delete = (ImageView) view.findViewById(R.id.item_store_delete);
            iv_delete.setVisibility(View.GONE);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemListener != null) {
                        onItemListener.onClick(v, getLayoutPosition());
                    }
                }
            });
        }
    }
}
